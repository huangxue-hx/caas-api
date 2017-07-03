package com.harmonycloud.service.platform.serviceImpl.monitor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpClientResponse;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.service.platform.service.LogMonitService;

@Service
public class LogMonitorServiceImpl implements LogMonitService{

	@Value("#{propertiesReader['es.logMonit']}")
	private String logMonitServer;

	public ActionReturnUtil setLogMonitor(String module, String logMonitor, String severity, String restartMonitor)
			throws Exception {
		Integer logM = Integer.valueOf(logMonitor);
		Integer logRtM = Integer.valueOf(restartMonitor);
		Map<String, Object> requestParams = new HashMap<String, Object>();
		requestParams.put("module", module);
		requestParams.put("log_monitor", logM);
		requestParams.put("severity", severity);
		requestParams.put("restart_monitor", logRtM);
		Map<String, Object> requestHeader = new HashMap<String, Object>();
		requestHeader.put("Content-type", "application/json");
		String url = logMonitServer + "/api/log/setmonitor";
		HttpClientResponse response = HttpClientUtil.httpPostJsonRequest(url, requestHeader, requestParams);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
		}
        return ActionReturnUtil.returnSuccessWithData(JsonUtil.convertJsonToMap(response.getBody()));
	}

	public ActionReturnUtil listLogMonitor() throws Exception {
		String url = logMonitServer + "/api/log/monitor";
		HttpClientResponse response = HttpClientUtil.httpGetRequestNew(url, null, null);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
		}
        return ActionReturnUtil.returnSuccessWithData(JsonUtil.JsonToMapList(response.getBody()));
	}

	public ActionReturnUtil setLogAlertType(String alertType) throws Exception {
		Map<String, Object> requestHeader = new HashMap<String, Object>();
		requestHeader.put("Content-type", "application/json");
		String url = logMonitServer + "/api/log/setalerttype";
		Map<String, Object> requestParams = new HashMap<String, Object>();
		requestParams.put("alert_type", alertType);
		HttpClientResponse response = HttpClientUtil.httpPostJsonRequest(url, requestHeader, requestParams);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
		}
        return ActionReturnUtil.returnSuccessWithData(JsonUtil.convertJsonToMap(response.getBody()));
	}

	public ActionReturnUtil listLogAlertType() throws Exception {
		String url = logMonitServer + "/api/log/alerttype";
		HttpClientResponse response = HttpClientUtil.httpGetRequestNew(url, null, null);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
		}
        return ActionReturnUtil.returnSuccessWithData(JsonUtil.JsonToMapList(response.getBody()));
	}

	public ActionReturnUtil setLogAlertEmail(String smtp, String port, String user, String password, String from, String to, String cc) throws Exception {
		Map<String, Object> requestHeader = new HashMap<String, Object>();
		requestHeader.put("Content-type", "application/json");
		String url = logMonitServer + "/api/log/setalertemail";
		Map<String, Object> requestParams = new HashMap<String, Object>();
		requestParams.put("smtp", smtp);
		requestParams.put("port", port);
		requestParams.put("user", user);
		requestParams.put("password", password);
		requestParams.put("from", from);;
		requestParams.put("to", to);
		requestParams.put("cc", cc);
		HttpClientResponse response = HttpClientUtil.httpPostJsonRequest(url, requestHeader, requestParams);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
		}
        return ActionReturnUtil.returnSuccessWithData(JsonUtil.convertJsonToMap(response.getBody()));
	}

	public ActionReturnUtil listLogAlertEmail() throws Exception {
		String url = logMonitServer + "/api/log/alertemail";
		HttpClientResponse response = HttpClientUtil.httpGetRequestNew(url, null, null);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
		}
        return ActionReturnUtil.returnSuccessWithData(JsonUtil.JsonToMapList(response.getBody()));
	}

	
	/**
	 * set,get方法
	 * @return
	 */
	public String getLogMonitServer() {
		return logMonitServer;
	}

	public void setLogMonitServer(String logMonitServer) {
		this.logMonitServer = logMonitServer;
	}

}
