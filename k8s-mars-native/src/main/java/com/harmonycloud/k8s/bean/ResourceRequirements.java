package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceRequirements {

	private Object limits;
	
	private Object requests;

	public Object getLimits() {
		return limits;
	}

	public void setLimits(Object limits) {
		this.limits = limits;
	}

	public Object getRequests() {
		return requests;
	}

	public void setRequests(Object requests) {
		this.requests = requests;
	}
	
	
}
