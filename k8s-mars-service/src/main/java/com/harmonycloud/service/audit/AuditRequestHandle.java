package com.harmonycloud.service.audit;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.AuditModuleEnum;
import com.harmonycloud.common.enumm.AuditQueryDbEnum;
import com.harmonycloud.common.enumm.AuditUrlEnum;
import com.harmonycloud.common.enumm.IstioPolicyEnum;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.application.LogBackupRuleMapper;
import com.harmonycloud.dao.application.bean.LogBackupRule;
import com.harmonycloud.dao.ci.bean.BuildEnvironment;
import com.harmonycloud.dao.ci.bean.DockerFile;
import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.user.bean.LocalRole;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.bean.UserGroup;
import com.harmonycloud.dto.config.AuditRequestInfo;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.ci.BuildEnvironmentService;
import com.harmonycloud.service.platform.service.ci.DockerFileService;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.LocalRoleService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import com.harmonycloud.service.util.SsoClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author jiangmi
 * @Description 拦截请求后解析request
 * @Date created in 2018-1-10
 * @Modified
 */
@Component
public class AuditRequestHandle {
    private static Logger logger = LoggerFactory.getLogger(AuditRequestHandle.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RoleLocalService roleLocalService;

    @Autowired
    private BuildEnvironmentService buildEnvironmentService;

    @Autowired
    private DockerFileService dockerFileService;

    @Autowired
    private JobService jobService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private HarborProjectService harborProjectService;

    @Autowired
    private LocalRoleService localRoleService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private LogBackupRuleMapper logBackupRuleMapper;

    @Autowired
    private ProjectService projectService;

    private static final String CDP = "Continue Deliver Platform";

    private static final String CDP_DEFAULT_USER = "cdp_default_user";

    protected static final Map<String, AuditUrlEnum> AUDIT_URL_MAP = new LinkedHashMap<>(
            AuditUrlEnum.values().length);

    static {
        for (AuditUrlEnum url : EnumSet.allOf(AuditUrlEnum.class)) {
            AUDIT_URL_MAP.put(url.getUrlRegex(), url);
        }
    }

    private AuditRequestHandle() {}

    public AuditRequestInfo parseRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String url = request.getRequestURI();
        if (url.indexOf(CommonConstant.URL_PRFFIX) > -1) {
            url = url.substring(url.indexOf(CommonConstant.URL_PRFFIX) + CommonConstant.URL_PRFFIX.length());
        }
        String method = request.getMethod();
        url += "_" + method;

        //循环所有的URL正则表达式
        for (Map.Entry<String, AuditUrlEnum> urlMap : AUDIT_URL_MAP.entrySet()) {
            Pattern p = Pattern.compile(urlMap.getKey());
            Matcher m = p.matcher(url);
            if (m.find()) {
                AuditUrlEnum urlEnum = urlMap.getValue();

                //获取path参数
                String subject = Objects.isNull(urlEnum.getParamIndex()) ? null : m.group(urlEnum.getParamIndex());
                String params = null;
                String tenant = null;
                //获取请求参数
                String bodyData = Objects.nonNull(request.getSession().getAttribute("requestBody")) ?
                        String.valueOf(request.getSession().getAttribute("requestBody")) : null;
                if (StringUtils.isNotBlank(bodyData) && HttpClientUtil.isApplicationJsonType(request)) {
                    //获取subject
                    Map<String, Object> data = JsonUtil.convertJsonToMap(bodyData);
                    if (StringUtils.isNotBlank(urlEnum.getParamName())) {
                        subject = data.containsKey(urlEnum.getParamName()) ? data.get(urlEnum.getParamName()).toString() : null;
                    }
                    if (url.indexOf("/msf") > -1) {
                        tenant = data.containsKey("tenant_id") ? tenantService.getTenantByTenantid(data.get("tenant_id").toString()).getAliasName() : null;
                    }
                    params = bodyData;
                } else {
                    Map<String, Object> requestParams = parseRequestParams(request, urlEnum.getParamName());
                    params = requestParams.get("allParams").toString();
                    if (Objects.nonNull(requestParams.get("value"))) {
                        subject = requestParams.get("value").toString();
                    }
                }
                //判断是否用数据库查询
                if (StringUtils.isNotBlank(urlEnum.getQueryDb()) && StringUtils.isNotBlank(subject)) {
                    //查询service对应的code
                    AuditQueryDbEnum auditQueryDbEnum = AuditQueryDbEnum.valueOf(urlEnum.getQueryDb().toUpperCase());
                    subject = queryDb(auditQueryDbEnum, subject);
                }

                if (StringUtils.isBlank(tenant) && !url.contains("/tenants/projects")) {
                    String regexUrl = "(?<=tenants/)[0-9a-zA-Z]*";
                    Pattern urlCompile = Pattern.compile(regexUrl);
                    Matcher matcher = urlCompile.matcher(url);
                    if (matcher.find()) {
                        tenant = tenantService.getTenantByTenantid(matcher.group(0)).getAliasName();
                    }
                }

                //组装
                AuditRequestInfo requestInfo = new AuditRequestInfo();
                requestInfo.setMethod(method);
                requestInfo.setUrl(request.getRequestURI());
                requestInfo.setModuleChDesc(AuditModuleEnum.valueOf(urlEnum.getModule()).getChDesc());
                requestInfo.setModuleEnDesc(AuditModuleEnum.valueOf(urlEnum.getModule()).getEnDesc());
                //istio 策略操作
                if (urlMap.getValue().toString().contains("ISTIO_POLICY") && StringUtils.isNotEmpty(params) && params.contains("ruleType")) {
                    String typeParam = request.getParameter("ruleType");
                    String chPolicyName = IstioPolicyEnum.valueOf(typeParam.toUpperCase()).getChPolicyName();
                    String enPolicyName = IstioPolicyEnum.valueOf(typeParam.toUpperCase()).getEnPolicyName();
                    requestInfo.setActionChDesc(String.format(urlEnum.getChDesc(), chPolicyName));
                    requestInfo.setActionEnDesc(String.format(urlEnum.getEnDesc(), enPolicyName));
                } else if (urlMap.getValue().toString().contains("POLICY_SWITCH") && StringUtils.isNotEmpty(params) && params.contains("status")) {
                    boolean isOpen = Boolean.parseBoolean(request.getParameter("status"));
                    requestInfo.setActionChDesc(String.format(urlEnum.getChDesc(), isOpen ? "开启" : "关闭"));
                    requestInfo.setActionEnDesc(String.format(urlEnum.getEnDesc(), isOpen ? "open" : "close"));
                } else {
                    requestInfo.setActionChDesc(urlEnum.getChDesc());
                    requestInfo.setActionEnDesc(urlEnum.getEnDesc());
                }
                requestInfo.setRequestParams(params);
                requestInfo.setSubject(subject);
                requestInfo.setTenant(tenant);
                //获取当前用户
                requestInfo.setUser(userService.getCurrentUsername());
                //单点登录
                if ("/users/current_GET".equals(url)) {
                    if (StringUtils.isNotBlank(subject) && Boolean.valueOf(subject)) {
                        //User user = SsoClient.getLoginUser(request,response);
                        HttpServletRequest httpRequest = (HttpServletRequest)request;
                        HttpSession session = httpRequest.getSession();
                        String userName = (String)session.getAttribute("username");
                        requestInfo.setUser(userName);
                        requestInfo.setSubject(userName);
                    } else {
                        requestInfo.setUser(null);
                    }
                }
                //如果是登录请求，从参数内获取,并且将参数加密
                if ("/users/auth/login_POST".equals(url)) {
                    requestInfo.setRequestParams("******");
                    String username = request.getParameter("username");
                    requestInfo.setUser(username);
                    requestInfo.setSubject(username);
                }
                if ("/users/auth/logout_POST".equals(url)) {
                    HttpSession session = request.getSession();
                    String username = (String) session.getAttribute("username");
                    requestInfo.setUser(username);
                    requestInfo.setSubject(username);
                }
//                if(url.indexOf("/msf/") < 0) {
//                    HttpSession session = request.getSession();
//                    requestInfo.setTenant((String) session.getAttribute(CommonConstant.TENANT_ALIASNAME));
//                    requestInfo.setProject((String) session.getAttribute(CommonConstant.PROJECT_ALIASNAME));
//                }
                if (CDP.equals(requestInfo.getModuleEnDesc())) {
                    requestInfo.setUser(CDP_DEFAULT_USER);
                }
                return requestInfo;
            }
        }
        return null;
    }

    public boolean checkUrlInCollection() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String url = request.getRequestURI();
        if (url.indexOf(CommonConstant.URL_PRFFIX) > -1) {
            url = url.substring(url.indexOf(CommonConstant.URL_PRFFIX) + CommonConstant.URL_PRFFIX.length());
        }
        String method = request.getMethod();
        url += "_" + method;

        //循环所有的URL正则表达式
        for (Map.Entry<String, AuditUrlEnum> urlMap : AUDIT_URL_MAP.entrySet()) {
            Pattern p = Pattern.compile(urlMap.getKey());
            Matcher m = p.matcher(url);
            if (m.find()) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> parseRequestParams(HttpServletRequest request, String key){
        StringBuilder sb = new StringBuilder();
        Enumeration<String> enu = request.getParameterNames();
        Map<String, Object> res = new HashMap<String, Object>();
        String value = null;
        while (enu.hasMoreElements()) {
            String paraName = enu.nextElement();
            String paraValue = request.getParameter(paraName);
            if ("passwd".equals(paraName) || paraName.contains("password")) {
                paraValue = "******";
            }
            sb.append(paraName + "->" + paraValue + ";");
            if (StringUtils.isNotBlank(key) && paraName.equals(key)) {
                value = request.getParameter(paraName);
            }
        }
        res.put("allParams", sb.toString());
        res.put("value", value);
        return res;
    }

    private String queryDb(AuditQueryDbEnum auditQueryDbEnum, String query) {
        try {
            switch (auditQueryDbEnum) {
                case USERSERVICE:
                    List<UserGroup> groups = userService.get_groups();
                    for (UserGroup  userGroup : groups) {
                        if (String.valueOf(userGroup.getId()).equals(query) ) {
                            return userGroup.getGroupname();
                        }
                    }
                    break;
                case ROLELOCALSERVICE:
                    Role role = roleLocalService.getRoleById(Integer.valueOf(query));
                    return role.getNickName();
                case BUILDENVIRONMENTSERVICE:
                    BuildEnvironment buildEnvironment = buildEnvironmentService.getBuildEnvironment(Integer.valueOf(query));
                    return buildEnvironment.getName();
                case DOCKERFILESERVICE:
                    DockerFile dockerFile = dockerFileService.selectDockerFileById(Integer.valueOf(query));
                    return dockerFile.getName();
                case JOBSERVICE:
                    Job job = jobService.getJobById(Integer.valueOf(query));
                    return job.getName();
                case TENANTSERVICE:
                    TenantBinding tenant = tenantService.getTenantByTenantid(query);
                    return tenant.getAliasName();
                case NAMESPACELOCALSERVICE:
                    NamespaceLocal namespace = namespaceLocalService.getNamespaceByNamespaceId(query);
                    return namespace.getAliasName();
                case HARBORPROJECTSERVICE:
                    ImageRepository repo = harborProjectService.findRepositoryById(Integer.valueOf(query));
                    return repo.getRepositoryName();
                case LOCALROLESERVICE:
                    LocalRole localRole = localRoleService.getLocalRoleById(Integer.valueOf(query));
                    return localRole.getDescription();
                case CLUSTERSERVICE:
                    Cluster cluster = clusterService.findClusterById(query);
                    return cluster.getAliasName();
                case LOGBACKUPRULEMAPPER:
                    LogBackupRule logBackupRule = logBackupRuleMapper.selectByPrimaryKey(Integer.valueOf(query));
                    Cluster cluster2 = clusterService.findClusterById(logBackupRule.getClusterId());
                    return cluster2.getAliasName();
                case PROJECT:
                    return projectService.getProjectNameByProjectId(query);
                default: return null;
            }
        } catch (Exception e) {
            logger.error("queryDb failed", e);
        }
        return null;
    }

    public String getRemoteIp(HttpServletRequest request) throws Exception{
        String forwardIp = getHeader(request, "X-Forwarded-For");
        logger.debug("nginx forwardIp:{}", forwardIp);
        forwardIp = StringUtils.isNotEmpty(forwardIp)? forwardIp.split(CommonConstant.COMMA)[0] : "";
        return StringUtils.isEmpty(forwardIp)? request.getRemoteAddr() : forwardIp;
    }

    private String getHeader(HttpServletRequest request, String headName) {
        String value = request.getHeader(headName);
        logger.debug("header value:{}", value);
        return (StringUtils.isNotBlank(value) && !"unknown".equalsIgnoreCase(value)) ? value : "";
    }
}
