package com.harmonycloud.k8s.bean;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DaemonSetCondition {

	private Date lastTransitionTime;
	
	private String message;
	
	private String reason;
	
	private String status;
	
	private String type;

	public Date getLastTransitionTime() {
		return lastTransitionTime;
	}

	public void setLastTransitionTime(Date lastTransitionTime) {
		this.lastTransitionTime = lastTransitionTime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
