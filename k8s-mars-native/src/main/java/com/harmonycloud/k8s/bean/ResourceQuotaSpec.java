package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceQuotaSpec {

	private Object hard;
	
	private Object scopes;

	public Object getHard() {
		return hard;
	}

	public void setHard(Object hard) {
		this.hard = hard;
	}

	public Object getScopes() {
		return scopes;
	}

	public void setScopes(Object scopes) {
		this.scopes = scopes;
	}
	
	
}
