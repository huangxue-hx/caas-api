package com.harmonycloud.aop;

import java.util.Date;

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

	@Pointcut("execution(public * com.harmonycloud.api..*.*(..))")
	public void auditController() {

	}

	@Before("auditController()")
	public void doBefore(JoinPoint joinPoint) {
		Date date = TenantUtils.getUtctime();
		date = new Date(date.getTime() + 8 * 60 * 60 * 1000L);

		String opDate = DateUtil.timeFormat.format(date.getTime());
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		String url = request.getRequestURI();
		String moduleKey = DicUtil.parseModelName(url, 3);
		final String moduleName = DicUtil.get(moduleKey);
		String method = request.getMethod();
		String opFun = DicUtil.get(DicUtil.parseDicKey(url, method));
		if (StringUtils.isNotBlank(moduleName) && StringUtils.isNotBlank(opFun)) {
			try {
				HttpSession session = request.getSession();

				String username = (String) session.getAttribute("username");
				String reqParam = DicUtil.parseParams(request);

				StringBuffer requestParams = new StringBuffer();
				if (StringUtils.isNotBlank(reqParam)) {
					requestParams.append(reqParam);
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
				SearchResult sr = new SearchResult();
				sr.setOpTime(opDate);
				sr.setUser(username);
				sr.setModule(moduleName);
				sr.setMethod(method);
				sr.setOpFun(opFun);
				sr.setRequestParams(args);
				sr.setRemoteIp(remoteIp);
				sr.setPath(url);
				result.set(sr);
				log.info("REQUEST: URL: {}, HTTP_METHOD: {}, ARGS: {}, REMOTE_IP: {}, CLASS_METHOD: {}", new String[] {
						url, method, args, remoteIp,
						joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName() });
			} catch (Exception e) {
				log.error("spring aop before exception", e);
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
						opStatus = reString.substring(reString.lastIndexOf("success") + 8, reString.length() - 1);
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

	@AfterThrowing(throwing = "ex", pointcut = "execution(* com.bean.*.*(..))")
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