package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 
 * @author jmi
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Probe {
	
	private ExecAction exec;
	
	private HTTPGetAction httpGet;
	
	private TCPSocketAction tcpSocket;
	
	private Integer initialDelaySeconds;
	
	private Integer timeoutSeconds;
	
	private Integer periodSeconds;
	
	private Integer successThreshold;
	
	private Integer failureThreshold;

	public boolean isEmpty(){
		boolean flag = true;
		/*if(this.exec!=null&&!this.exec.getCommand().isEmpty()&&this.exec.getCommand()!=null){*/
		if(this.exec!=null){
			flag=false;
		}
/*		if(this.httpGet!=null&&!StringUtils.isEmpty(this.httpGet.getHost())||!StringUtils.isEmpty(this.httpGet.getHoString())||!StringUtils.isEmpty(this.httpGet.getPath())||!StringUtils.isEmpty(this.httpGet.getScheme())||(this.httpGet.getHttpHeaders()!=null && !this.httpGet.getHttpHeaders().isEmpty()) ){
*/		
		if(this.httpGet!=null){
			flag=false;
		}
		if(this.getTcpSocket()!=null){
			flag=false;
		}
		if(this.getInitialDelaySeconds()!=null){
			flag=false;
		}
		if(this.getPeriodSeconds()!=null){
			flag=false;
		}
		if(this.getTimeoutSeconds()!=null){
			flag=false;
		}
		if(this.getSuccessThreshold()!=null){
			flag=false;
		}
		if(this.getFailureThreshold()!=null){
			flag=false;
		}
		return flag;
	}
	
	public ExecAction getExec() {
		return exec;
	}

	public void setExec(ExecAction exec) {
		this.exec = exec;
	}

	public HTTPGetAction getHttpGet() {
		return httpGet;
	}

	public void setHttpGet(HTTPGetAction httpGet) {
		this.httpGet = httpGet;
	}

	public TCPSocketAction getTcpSocket() {
		return tcpSocket;
	}

	public void setTcpSocket(TCPSocketAction tcpSocket) {
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
