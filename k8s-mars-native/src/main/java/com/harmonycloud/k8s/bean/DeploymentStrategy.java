package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeploymentStrategy {

	private String type;
	
	private RollingUpdateDeployment  rollingUpdate;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public RollingUpdateDeployment getRollingUpdate() {
		return rollingUpdate;
	}

	public void setRollingUpdate(RollingUpdateDeployment rollingUpdate) {
		this.rollingUpdate = rollingUpdate;
	}
	
	
}
