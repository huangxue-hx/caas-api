package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RollingUpdateDeployment {

	private String maxUnavailable;
	
	private String maxSurge;

	public String getMaxSurge() {
		return maxSurge;
	}

	public void setMaxSurge(String maxSurge) {
		this.maxSurge = maxSurge;
	}

	public String getMaxUnavailable() {
		return maxUnavailable;
	}

	public void setMaxUnavailable(String maxUnavailable) {
		this.maxUnavailable = maxUnavailable;
	}

	
}
