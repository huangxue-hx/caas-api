package com.harmonycloud.aop;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dto.config.AuditRequestInfo;
import com.harmonycloud.service.audit.AuditRequestHandle;
import com.harmonycloud.service.user.UserAuditService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 
 * @author jmi
 *
 */
@Aspect
@Order(0)
@Component
public class AuditControllerAspect {

	private static final Logger log = LoggerFactory.getLogger(AuditControllerAspect.class);
	ThreadLocal<AuditRequestInfo> result = new ThreadLocal<>();
	
	@Autowired
	private AuditRequestHandle requestHandle;

	@Autowired
	UserAuditService userAuditService;

	@Autowired
	UserService userService;

	@Pointcut("execution(public * com.harmonycloud.api..*.*(..))")
	public void auditController() {

	}

	@SuppressWarnings("unchecked")
	@Before("auditController()")
	public void doBefore(JoinPoint joinPoint){
		Date date = DateUtil.getCurrentUtcTime();
		date = new Date(date.getTime() + CommonConstant.NUM_EIGHT * 60 * 60 * 1000L);
		String opDate = DateUtil.timeFormat.format(date.getTime());
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		AuditRequestInfo requestInfo = new AuditRequestInfo();
		try {
			requestInfo = requestHandle.parseRequest(request);
			if (null != requestInfo) {
				String args = requestInfo.getRequestParams();
				String method = requestInfo.getMethod();
				//nginx转发获取用户ip
				String remoteIp = requestHandle.getRemoteIp(request);
				String url = requestInfo.getUrl();

				//将其余参数放入requestInfo对象内
				requestInfo.setRemoteIp(remoteIp);
				requestInfo.setActionTime(opDate);
				result.set(requestInfo);
				log.info("REQUEST: URL: {}, HTTP_METHOD: {}, ARGS: {}, REMOTE_IP: {}, CLASS_METHOD: {}", new String[]{
						url, method, args, remoteIp,
						joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName()});
			}
		} catch (Exception e) {
			log.error("spring aop before exception", e);
			result.set(requestInfo);
		}
	}

	@AfterReturning(returning = "res", pointcut = "auditController()")
	public void doAfterReturning(JoinPoint joinPoint, Object res) {
		try {
			if (requestHandle.checkUrlInCollection()) {
				AuditRequestInfo audit = result.get();
				if (audit != null) {
					String reString = JsonUtil.convertToJson(res);
					/*audit.setUser(audit.getUrl().indexOf("/current") > -1 ? userService.getCurrentUsername():audit.getUser());
					audit.setSubject(audit.getUrl().indexOf("/current") > -1 ? userService.getCurrentUsername():audit.getSubject());*/
					//current接口的参数与响应结果无需记录，所以将请求参数与响应结果置为null
					audit.setRequestParams(audit.getUrl().indexOf("/current") > -1 ? null : audit.getRequestParams());
					audit.setResponse(audit.getUrl().indexOf("/current") > -1 ? null : reString);
					String opStatus = reString.lastIndexOf("success") > -1 ?
							reString.substring(reString.lastIndexOf("success") + CommonConstant.NUM_NINE, reString.length() - 1) : "true";
					opStatus = opStatus.indexOf(",") > -1 ? opStatus.substring(0,opStatus.indexOf(",")) : opStatus;
					audit.setStatus(opStatus);
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
			if (requestHandle.checkUrlInCollection()) {
				AuditRequestInfo audit = result.get();
				if (audit != null) {
					audit.setResponse("exception");
					String opStatus = "false";
					audit.setStatus(opStatus);
					result.remove();
					doInsertOpToEs(audit);
				}
			}
			log.debug("exception:", ex);
		} catch (Exception e) {
			log.error("spring aop exception", e);
		}
	}

	private void doInsertOpToEs(AuditRequestInfo searchResult) throws Exception {
		Runnable worker = new Runnable() {
			@Override
			public void run() {
				ActionReturnUtil flag = new ActionReturnUtil();
				try {
					flag = userAuditService.insertToEsIndex(searchResult);
					log.debug("用户操作插入es结束：{}", searchResult.getUrl() + ";;;" + searchResult.getRemoteIp());
				} catch (Exception e) {
					log.error("用户操作记录插入失败", e);
				}
				log.info("用户操作记录是否插入成功: {}", flag.get("success"));
			}
		};

		log.debug("插入数据前:{}", searchResult.getUser() + ";" + searchResult.getStatus());
		if (StringUtils.isNotBlank(searchResult.getUser()) && StringUtils.isNotBlank(searchResult.getStatus())) {
			ESFactory.executor.execute(worker);
		}
	}
}