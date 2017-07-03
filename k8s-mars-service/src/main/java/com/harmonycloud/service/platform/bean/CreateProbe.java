package com.harmonycloud.service.platform.bean;

import com.harmonycloud.k8s.bean.ExecAction;

public class CreateProbe {

	private ExecAction exec;

	private CreateHttpGet httpGet;

	private CreateTcpSocket tcpSocket;

	private Integer initialDelaySeconds;

	private Integer timeoutSeconds;

	private Integer periodSeconds;

	private Integer successThreshold;

	private Integer failureThreshold;

	public ExecAction getExec() {
		return exec;
	}

	public void setExec(ExecAction exec) {
		this.exec = exec;
	}

	public CreateHttpGet getHttpGet() {
		return httpGet;
	}

	public void setHttpGet(CreateHttpGet httpGet) {
		this.httpGet = httpGet;
	}

	public CreateTcpSocket getTcpSocket() {
		return tcpSocket;
	}

	public void setTcpSocket(CreateTcpSocket tcpSocket) {
		this.tcpSocket = tcpSocket;
	}

	public Integer getInitialDelaySeconds() {
		return initialDelaySeconds;
	}

	public void setInitialDelaySeconds(Integer initialDelaySeconds) {
		this.initialDelaySeconds = initialDelaySeconds;
	}

	public Integer getTimeoutSeconds() {
		return timeoutSeconds;
	}

	public void setTimeoutSeconds(Integer timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}

	public Integer getPeriodSeconds() {
		return periodSeconds;
	}

	public void setPeriodSeconds(Integer periodSeconds) {
		this.periodSeconds = periodSeconds;
	}

	public Integer getSuccessThreshold() {
		return successThreshold;
	}

	public void setSuccessThreshold(Integer successThreshold) {
		this.successThreshold = successThreshold;
	}

	public Integer getFailureThreshold() {
		return failureThreshold;
	}

	public void setFailureThreshold(Integer failureThreshold) {
		this.failureThreshold = failureThreshold;
	}

}
