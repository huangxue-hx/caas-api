package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceQuotaStatus {

	private Object hard;
	
	private Object used;

	public Object getHard() {
		return hard;
	}

	public void setHard(Object hard) {
		this.hard = hard;
	}

	public Object getUsed() {
		return used;
	}

	public void setUsed(Object used) {
		this.used = used;
	}
	
	
}
