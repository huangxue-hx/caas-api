package com.harmonycloud.aop;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.MicroServiceCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.exception.MsfException;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.SsoClient;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dao.user.bean.LocalRolePrivilege;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dao.user.bean.UrlDic;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.tenant.TenantDto;
import com.harmonycloud.service.cache.ClusterCacheManager;
import com.harmonycloud.service.common.PrivilegeHelper;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.*;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  权限拦截AOP
 * @author
 *
 */
@Aspect
@Order(CommonConstant.NUM_ONE)
@Component
public class PrivilegeAspect {
	private static final String LEFT_BRACKET = "{";
	private static final String RIGHT_BRACKET = "}";
	private static final String BACKSLASH = "\\";
	private static final String STAR = "*";
	private static final String SLASHSTAR = "/*";
	private static final String POINTSTAR = ".*";
	//白名单
	private static final String WHITELIST = "whitelist";
	private static final int NUMONE = 1;
	private static final String GET = "GET";
	private static final String POST = "POST";
	private static final String PUT = "PUT";
	private static final String DELETE = "DELETE";
	private static final String PATCH = "PATCH";
	private static final String MSF = "msf";
	private static final String TENANT_ID = "tenant_id";
	private static final String REQUESTBODY = "requestBody";
	private static final String TENANTMGR = "tenantmgr";
    private static final String BASIC = "basic";
    private static final String SYSTEM = "system";
	//系统管理员角色id
	private static final Integer ADMIN_ROLEID = 1;
	//租户管理员角色id
	private static final Integer TM_ROLEID = 2;

	private static final String OVERURL = "/clusters/*/nodes/*/schedule,/clusters/*/nodes/*/drainPod";
	//日志查询
	private static final String LOGURL = "/clusters/*/namespaces/*/deploys/*/logs";
	//镜像推送
	private static final String IMAGEURL = "/tenants/*/projects/*/repositories/*/images/*/tags/*/syncImage";
	//分区
	private static final String NAMESPACE = "namespace";

	
	@Autowired
	private UrlDicService urlDicService;
	@Autowired
	private UserService userService;
	@Autowired
	private RoleLocalService roleLocalService;
	@Autowired
	private RolePrivilegeService rolePrivilegeService;
	@Autowired
	private UserRoleRelationshipService userRoleRelationshipService;
	@Autowired
	private ClusterCacheManager clusterCacheManager;
	@Autowired
	private PrivilegeHelper privilegeHelper;
	@Autowired
	private TenantService tenantService;
	@Autowired
	private SystemConfigService systemConfigService;

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static Map<String, UrlDic> urlDicMap = null;

	@Pointcut("execution(public * com.harmonycloud.api..*.*(..))")
	public void privilegeAspect() {

	}

	@SuppressWarnings("unchecked")
	@Before("privilegeAspect()")
	public void doBefore(JoinPoint joinPoint) throws Exception{

//		long startTime=System.currentTimeMillis();   //获取开始时间
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		HttpSession session = request.getSession();
		Map<String,String> attribute = (Map<String,String>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		String url = (String)request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		//处理url
		while (url.contains(LEFT_BRACKET)){
			int start = url.indexOf(LEFT_BRACKET);
			int end = url.indexOf(RIGHT_BRACKET);
			url = url.replaceAll(BACKSLASH + url.substring(start,end + NUMONE), STAR);
		}
		if (url.endsWith(POINTSTAR)){
			int i = url.lastIndexOf(POINTSTAR);
			url = url.substring(0, i);
		}
		if (url.endsWith(SLASHSTAR)){
			int i = url.lastIndexOf(SLASHSTAR);
			url = url.substring(0, i);
		}
		//加载权限字典
		if (CollectionUtils.isEmpty(urlDicMap)){
			urlDicMap = this.urlDicService.getUrlDicMap();
		}
		UrlDic urlDic = urlDicMap.get(url);
		if (Objects.isNull(urlDic)){
			throw new MarsRuntimeException(ErrorCodeMessage.URL_PERMISSION_DENIED,url,Boolean.TRUE);
		}
		String moduleName = urlDic.getModule();
		String resourceName = urlDic.getResource();
		String method = request.getMethod();
		Map<String, String[]> parameterMap = request.getParameterMap();
		//判断url是否在白名单内
		if (!WHITELIST.equals(moduleName)){
			Integer roleId = this.userService.getCurrentRoleId();
			String username = this.userService.getCurrentUsername();
			SystemConfig systemConfig = systemConfigService.findMaintenanceStatus();
			if(Boolean.valueOf(systemConfig.getConfigValue()) && !userService.isAdmin(username)){
				this.dealHeaderWithMaintenance(attributes.getResponse());
				throw new MarsRuntimeException(ErrorCodeMessage.SYSTEM_IN_MAINTENANCE);
			}
			//处理微服务 只允许系统管理员与租户管理员
			if (MSF.equals(moduleName) && !GET.equals(method)){
				User user = userService.getUser(username);
				if (CommonConstant.PAUSE.equals(user.getPause())) {
					throw new MsfException(MicroServiceCodeMessage.USER_DISABLED);
				}
				boolean admin = this.userService.isAdmin(username);
				//管理员直接通过返回
				if(admin){
					return;
				}
				Object requestBody = session.getAttribute(REQUESTBODY);
				String tenantId = null;
				Integer currentRoleId = this.userService.getCurrentRoleId();
				if (!Objects.isNull(requestBody)){
					Map<String, Object> stringObjectMap = JsonUtil.convertJsonToMap(requestBody.toString());
					if (stringObjectMap.containsKey(TENANT_ID) && !Objects.isNull(stringObjectMap.get(TENANT_ID))){
						tenantId = stringObjectMap.get(TENANT_ID).toString();
						//角色为租户管理员直接通过返回
						Boolean tmUser = this.userRoleRelationshipService.isTmUser(tenantId, username,TM_ROLEID);
						if (tmUser){
							return;
						}
					}
				}
				throw new MsfException(MicroServiceCodeMessage.NON_PRIVILEGED);
			}
			//不在白名单内,并且不是微服务模块，检查角色是否为空
			if (Objects.isNull(roleId) && StringUtils.isNotBlank(username)){
				List<TenantDto> tenantDtos = tenantService.tenantList();
				if(CollectionUtils.isEmpty(tenantDtos)){
					List<Role> roleList = roleLocalService.getRoleListByUsernameAndTenantIdAndProjectId(username, null, null);
					if (!CollectionUtils.isEmpty(roleList)) {
						session.setAttribute(CommonConstant.ROLEID, roleList.get(0).getId());
						rolePrivilegeService.switchRole(roleList.get(0).getId());
					}
				}else{
					tenantService.switchTenant(tenantDtos.get(0).getTenantId());
					rolePrivilegeService.switchRole((Integer)session.getAttribute(CommonConstant.ROLEID));
				}
				roleId = this.userService.getCurrentRoleId();
			}
			if (Objects.isNull(roleId)||StringUtils.isBlank(username)){
				this.dealHeader(attributes);
				throw new MarsRuntimeException(ErrorCodeMessage.USER_NOT_AUTH_OR_TIMEOUT);
			}
			//检查用户是否有权限变更或者用户被阻止
			Boolean userStatus = clusterCacheManager.getUserStatus(username);
			if (userStatus){
				this.dealHeader(attributes);
				throw new MarsRuntimeException(ErrorCodeMessage.USER_DISABLED);
			}
			Boolean rolePrivilegeStatus = clusterCacheManager.getRolePrivilegeStatus(roleId,null);
			Boolean userPrivilegeStatus = null;
			//检查角色是否被停用或者用户是否被移除对应的角色组
			if (CommonConstant.ADMIN_ROLEID.equals(roleId)){
				userPrivilegeStatus = clusterCacheManager.getRolePrivilegeStatus(roleId,username);
			}else if (CommonConstant.TM_ROLEID.equals(roleId)){
				String tenantId = this.userService.getCurrentTenantId();
				userPrivilegeStatus = clusterCacheManager.getRolePrivilegeStatusForTenantOrProject(roleId,username,tenantId,null);
			}else {
				String projectId = this.userService.getCurrentProjectId();
				userPrivilegeStatus = clusterCacheManager.getRolePrivilegeStatusForTenantOrProject(roleId,username,null,projectId);
			}
			if (rolePrivilegeStatus || userPrivilegeStatus){
				//有权限变更，刷新最新的权限
				this.rolePrivilegeService.switchRole(roleId);
			}
//			privilegeStatus = clusterCacheManager.getRolePrivilegeStatus(roleId,username);
//			clusterCacheManager.updateRolePrivilegeStatus(roleId,Boolean.TRUE);
			Object roleStatus = session.getAttribute(CommonConstant.ROLESTATUS);
			if (!Objects.isNull(roleStatus) && !(Boolean)roleStatus){
				this.dealHeader(attributes);
				throw new MarsRuntimeException(ErrorCodeMessage.ROLE_DISABLE);
			}

			//角色为非管理员进行角色权限认证
			if (roleId > ADMIN_ROLEID){
				Object privilegeObj = session.getAttribute(CommonConstant.PRIVILEGE);
				if (!Objects.isNull(privilegeObj)){
					Map<String, Map<String, Object>> privilege = (Map<String, Map<String, Object>>)privilegeObj;
					Map<String, Object> stringObjectMap = privilege.get(moduleName);
					Map<String, Boolean> privilegeMap = null;
					Boolean denyUrl = Boolean.FALSE;
					//处理系统内跨权限请求（例如租户管理员，处理独占分区主机）
					if (!Objects.isNull(stringObjectMap)){
						privilegeMap = (Map<String, Boolean>)stringObjectMap.get(resourceName);
						if (Objects.isNull(privilegeMap)){
							denyUrl = true;
						}
					}else {
						denyUrl = true;
					}
					if (denyUrl){
						this.dealOverPrivilege(privilegeMap,privilege,roleId,method,url);
						return;
					}
					Boolean passed = false;
					//根据请求方法在权限树获取对应的权限
					if (!Objects.isNull(privilegeMap)){
						switch (method){
							case GET:
								passed = privilegeMap.get(CommonConstant.GET);
								break;
							case POST:
								passed = privilegeMap.get(CommonConstant.CREATE);
								break;
							case DELETE:
								passed = privilegeMap.get(CommonConstant.DELETE);
								break;
							case PUT:
								passed = privilegeMap.get(CommonConstant.UPDATE);
								break;
							case PATCH:
								passed = privilegeMap.get(CommonConstant.EXECUTE);
								break;
							default:
								passed = false;
								break;
						}
					}
					//如果权限未通过或者权限通过scope检查不通过则返回权限不足
					boolean scope = false;
					if (!(passed && (scope = this.checkScope(attribute,parameterMap,url,passed)))){
						if (rolePrivilegeStatus){
							throw new MarsRuntimeException(ErrorCodeMessage.USER_PERMISSION_DENIED_FOR_PRIVILEGE_CHANGE,url,Boolean.TRUE);
						} else {
							throw new MarsRuntimeException(ErrorCodeMessage.USER_PERMISSION_DENIED,url,Boolean.TRUE);
						}
					}
					// 增删改操作的数据权限控制
					if (!StringUtils.equals(method, GET)){
						List<LocalRolePrivilege> localRolePrivileges = userService.getCurrentLocalPrivilegeList();
						if (!CollectionUtils.isEmpty(localRolePrivileges)){
						privilegeHelper.authorize(resourceName, parameterMap);
						}
					}

				}
			}
		}
//		long endTime=System.currentTimeMillis(); //获取结束时间
//		long time = endTime - startTime;//获取消耗时间，调试使用
//		System.out.println("消耗时间："+time);
	}
	private void dealHeader (ServletRequestAttributes attributes){
		HttpServletRequest request = attributes.getRequest();
		HttpSession session = request.getSession();
		if(SsoClient.isOpen()) {
			HttpServletResponse response = attributes.getResponse();
			SsoClient.setRedirectResponse(response);
			session.invalidate();
			SsoClient.clearToken(response);
		}else {
			//HttpServletResponse response = attributes.getResponse();
			//SsoClient.setRedirectResponse(response);
			session.invalidate();
		}
	}

	private void dealHeaderWithMaintenance (HttpServletResponse response){
		response.setHeader("Access-Control-Expose-Headers","Maintenance");
		response.setHeader("Maintenance","true");
	}

	//处理系统内跨权限请求（例如租户管理员，处理独占分区主机）
	private void dealOverPrivilege(Map<String, Boolean> privilegeMap,Map<String, Map<String, Object>> privilege,Integer roleId,String method,String url){
		String[] urls = OVERURL.split(CommonConstant.COMMA);
		List<String> collect = Arrays.stream(urls).filter(string -> string.equals(url)).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(collect)){
			Map<String, Object> stringObjectMap = privilege.get(CommonConstant.TENANT);
			Map<String, Boolean> privilegeMapMgr = (Map<String, Boolean>)stringObjectMap.get(CommonConstant.TENANTMGR);
			Map<String, Boolean> privilegeMapBasic = (Map<String, Boolean>)stringObjectMap.get(CommonConstant.BASIC);
			Boolean passed = false;
			if (!Objects.isNull(privilegeMapMgr)||!Objects.isNull(privilegeMapBasic)){
				if (!Objects.isNull(privilegeMapBasic)){
					passed = privilegeMapBasic.get(CommonConstant.UPDATE);
				}
				if (!passed && !Objects.isNull(privilegeMapMgr)){
					passed = privilegeMapMgr.get(CommonConstant.UPDATE);
				}
				if (passed){
					return;
				}
			}
		}
		if (!GET.equals(method)){
            throw new MarsRuntimeException(ErrorCodeMessage.USER_PERMISSION_DENIED);
        }
    }
	private Boolean checkScope(Map<String,String> attribute,Map<String, String[]> parameterMap,String url,Boolean passed) throws Exception{
		Integer roleId = this.userService.getCurrentRoleId();
		String tenantId = attribute.get(CommonConstant.TENANT_ID);

		if (url.equals(IMAGEURL)){
			return Boolean.TRUE;
		}
		String namespace = attribute.get(NAMESPACE);
		//排除系统日志在上层集群的作用域不符合
		if (url.contains(LOGURL)
				&& !Objects.isNull(namespace)
				&& CommonConstant.KUBE_SYSTEM.equals(namespace)
				&& passed){
			return Boolean.TRUE;
		}
		//如果带有集群信息，检查对应作用域
		String[] clusterIdParameter = parameterMap.get(CommonConstant.CLUSTERID);
		String clusterIdUrl = attribute.get(CommonConstant.CLUSTERID);
		if ((!Objects.isNull(clusterIdParameter) && StringUtils.isNoneBlank(clusterIdParameter) && clusterIdParameter.length > 0)|| StringUtils.isNotBlank(clusterIdUrl)){
			String clusterId = Objects.isNull(clusterIdParameter)?clusterIdUrl:clusterIdParameter[0];
			Set<String> currentUserCluster = roleLocalService.listCurrentUserRoleClusterIds();
			//与传入的集群id与当前角色的作用域不匹配
			if (!currentUserCluster.contains(clusterId)){
				throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_IN_SCOPE,clusterId,Boolean.TRUE);
			}
		}
		return Boolean.TRUE;
	}


}