package com.harmonycloud.aop;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.DicUtil;
import com.harmonycloud.common.util.ESFactory;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.SearchResult;
import com.harmonycloud.common.util.TenantUtils;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.application.bean.Business;
import com.harmonycloud.dao.tenant.HarborProjectTenantMapper;
import com.harmonycloud.dao.tenant.TenantBindingMapper;
import com.harmonycloud.dao.tenant.bean.HarborProjectTenant;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantBindingExample;

/**
 * 
 * @author jmi
 *
 */
@Aspect
@Component
public class AuditControllerAspect {

	private static final Logger log = LoggerFactory.getLogger(AuditControllerAspect.class);
	ThreadLocal<SearchResult> result = new ThreadLocal<>();
	
	@Autowired
	private TenantBindingMapper tenantBindingMapper;

	
	@Autowired
	private HarborProjectTenantMapper hpTenantMapper;

	@Pointcut("execution(public * com.harmonycloud.api..*.*(..))")
	public void auditController() {

	}

	@SuppressWarnings("unchecked")
	@Before("auditController()")
	public void doBefore(JoinPoint joinPoint){
		Date date = DateUtil.getCurrentUtcTime();
		date = new Date(date.getTime() + 8 * 60 * 60 * 1000L);

		String opDate = DateUtil.timeFormat.format(date.getTime());
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		String url = request.getRequestURI();
		String moduleKey = DicUtil.parseModelName(url, 3);
		final String moduleName = DicUtil.get(moduleKey);
		String method = request.getMethod();
		String opFunKey = DicUtil.parseDicKey(url, method);
		String opFun = DicUtil.get(opFunKey);
		String opParams = DicUtil.get(DicUtil.parseDicKeyOfParams(url, method));
		if (StringUtils.isNotBlank(moduleName) && StringUtils.isNotBlank(opFun)) {
			String subject = null;
			String tenant = null;
			SearchResult sr = new SearchResult();
			try {
				HttpSession session = request.getSession();
				String username = (String) session.getAttribute("username");
				Map<String, Object> reqParams = DicUtil.parseRequestParams(request, opParams);
				List<String> values = (List<String>) reqParams.get("values");
				if (values != null && values.size() > 0 ) {
					subject = values.get(0);
				}
                if (moduleName.equals("租户模块")) {
                	if (opFunKey.equals("rest_tenant_deleteProject_DELETE")) {
                		HarborProjectTenant hProjectTenant = hpTenantMapper.getByHarborProjectId(Long.valueOf(values.get(1)));
                	    subject =  hProjectTenant.getHarborProjectName();
                	    tenant = values.get(0);
                	} else if (opFunKey.equals("rest_tenant_create_POST")) {
                		subject = values.get(0);
                		tenant = values.get(0);
                	} else if (opFunKey.equals("rest_tenant_addTrustmember_POST") || opFunKey.equals("rest_tenant_removeTrustmember_DELETE")) {
                		TenantBindingExample example = new TenantBindingExample();
                    	example.createCriteria().andTenantIdEqualTo(values.get(0));
                    	List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
                    	if (list != null) {
                    		tenant = list.get(0).getTenantName();
                    	}
                    	example.clear();
                    	example.createCriteria().andTenantIdEqualTo(values.get(1));
                    	List<TenantBinding> list2 = tenantBindingMapper.selectByExample(example);
                    	if (list2 != null) {
                    		subject = list2.get(0).getTenantName();
                    	}
                	} else if (opFunKey.equals("rest_tenant_removeUser_DELETE")) {
                		TenantBindingExample example = new TenantBindingExample();
                    	example.createCriteria().andTenantIdEqualTo(values.get(1));
                    	List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
                    	if (list != null) {
                    		tenant = list.get(0).getTenantName();
                    	}
                    	subject = values.get(0);
                	} else {
                		TenantBindingExample example = new TenantBindingExample();
                    	example.createCriteria().andTenantIdEqualTo(values.get(0));
                    	List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
                    	if (list != null) {
                    		tenant = list.get(0).getTenantName();
                    		subject = tenant;
                    	}
                    	if (values.size() > 1) {
                    		subject = values.get(1);
                    	}
                	}
                }
                if (moduleName.equals("业务模块")) {
                    if (opFunKey.equals("rest_business_deploy_DELETE") || opFunKey.equals("rest_business_deploy_stop_POST") || opFunKey.equals("rest_business_deploy_start_POST")){
						if (values.get(0).toString().contains("=") && values.get(0).toString().contains("-")){
							String[] sign = values.get(0).toString().split("=");
							if (sign != null){
								String[] buiness = sign[0].split("-");
								if (buiness.length >= 3){
									subject = buiness[2].toString();
								}
							}
						}
                	} else {
                		subject = values.get(0);
                	}
                }
				StringBuffer requestParams = new StringBuffer();
				if (StringUtils.isNotBlank(reqParams.get("allParams").toString())) {
					requestParams.append(reqParams.get("allParams"));
					if (url.contains("login") || url.indexOf("validation") >= 0 || url.indexOf("getToken") >= 0
							|| url.contains("changePwd") || url.contains("adminReset") || url.contains("userReset")) {
						requestParams.replace(0, requestParams.toString().length(), "请求参数:*****");
					}
				}

				if (url.indexOf("login") >= 0 || url.indexOf("validation") >= 0 || url.indexOf("getToken") >= 0) {

					username = request.getParameter("username");
				}
				String remoteIp = request.getRemoteAddr();
				String args = requestParams.toString();
				
				sr.setOpTime(opDate);
				sr.setUser(username);
				sr.setModule(moduleName);
				sr.setMethod(method);
				sr.setOpFun(opFun);
				sr.setRequestParams(args);
				sr.setRemoteIp(remoteIp);
				sr.setPath(url);
				sr.setTenant(tenant);
				sr.setSubject(subject);
				result.set(sr);
				log.info("REQUEST: URL: {}, HTTP_METHOD: {}, ARGS: {}, REMOTE_IP: {}, CLASS_METHOD: {}", new String[] {
						url, method, args, remoteIp,
						joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName() });
			} catch (Exception e) {
				log.error("spring aop before exception", e);
				result.set(sr);
			}
		}

	}

	@AfterReturning(returning = "res", pointcut = "auditController()")
	public void doAfterReturning(JoinPoint joinPoint, Object res) {
		try {
			if (doCheckInDic()) {
				SearchResult audit = result.get();
				if (audit != null) {
					String reString = JsonUtil.convertToJson(res);
					audit.setResponse(reString);
					String opStatus = "true";
					if (reString.lastIndexOf("success") > -1) {
						opStatus = reString.substring(reString.lastIndexOf("success") + 9, reString.length() - 1);
						if (opStatus.indexOf(",") > -1) {
							opStatus = opStatus.substring(0,opStatus.indexOf(","));
						}
					}
					audit.setOpStatus(opStatus);
					result.remove();
					doInsertOpToEs(audit);
				}
				log.debug("res:", res);
			}
		} catch (Exception e) {
			log.error("spring aop exception", e);
		}

	}

	@AfterThrowing(throwing = "ex", pointcut = "auditController()")
	public void doAfterThrowing(Exception ex) {
		try {
			if (doCheckInDic()) {
				SearchResult audit = result.get();
				if (audit != null) {
					audit.setResponse("exception");
					String opStatus = "false";
					audit.setOpStatus(opStatus);
					result.remove();
					doInsertOpToEs(audit);
				}
			}
			log.debug("exception:", ex);
		} catch (Exception e) {
			log.error("spring aop exception", e);
		}
	}

	private boolean doCheckInDic() throws Exception {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		String url = request.getRequestURI();
		String method = request.getMethod();
		String moduleKey = DicUtil.parseModelName(url, 3);
		String opFun = DicUtil.get(DicUtil.parseDicKey(url, method));
		String moduleName = DicUtil.get(moduleKey);
		if (StringUtils.isNotBlank(moduleName) && StringUtils.isNotBlank(opFun)) {
			return true;
		}
		return false;
	}

	private void doInsertOpToEs(SearchResult searchResult) throws Exception {
		Runnable worker = new Runnable() {
			@Override
			public void run() {
				ActionReturnUtil flag = new ActionReturnUtil();
				try {
					flag = ESFactory.insertToIndexBySR(searchResult);
					log.debug("用户操作插入es结束：" + searchResult.getPath() + ";;;" + searchResult.getRemoteIp());
				} catch (Exception e) {
					e.printStackTrace();
				}
				log.info("用户操作记录是否插入成功:" + flag.get("success"));
			}
		};

		if (StringUtils.isNotBlank(searchResult.getUser()) && StringUtils.isNotBlank(searchResult.getOpStatus())) {
			ESFactory.executor.execute(worker);
		}
	}
}