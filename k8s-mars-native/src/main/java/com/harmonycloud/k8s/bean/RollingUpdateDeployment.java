package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RollingUpdateDeployment {

	private Object maxUnavailable;
	
	private Object maxSurge;

	public Object getMaxSurge() {
		return maxSurge;
	}

	public void setMaxSurge(Object maxSurge) {
		this.maxSurge = maxSurge;
	}

	public Object getMaxUnavailable() {
		return maxUnavailable;
	}

	public void setMaxUnavailable(Object maxUnavailable) {
		this.maxUnavailable = maxUnavailable;
	}

}
