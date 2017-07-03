package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;

/**
 * 日志监控接口
 * @author jmi
 *
 */
public interface LogMonitService {
	
	public ActionReturnUtil setLogMonitor(String module, String logMonitor, String severity, String restartMonitor) throws Exception;
	
	public ActionReturnUtil listLogMonitor() throws Exception;
	
	public ActionReturnUtil setLogAlertType(String alertType) throws Exception;
	
	public ActionReturnUtil listLogAlertType() throws Exception;
	
	public ActionReturnUtil setLogAlertEmail(String smtp, String port, String user, String password, String from, String to, String cc) throws Exception;
	
	public ActionReturnUtil listLogAlertEmail() throws Exception;
	
}
