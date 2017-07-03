package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RollingUpdateDeployment {

	private Integer maxUnavailable;
	
	private Integer maxSurge;

	public Integer getMaxUnavailable() {
		return maxUnavailable;
	}

	public void setMaxUnavailable(Integer maxUnavailable) {
		this.maxUnavailable = maxUnavailable;
	}

	public Integer getMaxSurge() {
		return maxSurge;
	}

	public void setMaxSurge(Integer maxSurge) {
		this.maxSurge = maxSurge;
	}
	
	
}
