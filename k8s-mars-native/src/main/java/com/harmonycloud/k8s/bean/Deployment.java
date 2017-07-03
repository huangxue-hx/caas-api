package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Deployment extends BaseResource{

	private DeploymentSpec spec;
	
	private DeploymentStatus status;

	public DeploymentStatus getStatus() {
		return status;
	}

	public void setStatus(DeploymentStatus status) {
		this.status = status;
	}

	public DeploymentSpec getSpec() {
		return spec;
	}

	public void setSpec(DeploymentSpec spec) {
		this.spec = spec;
	}
	
	public Deployment(){
		this.setKind("Deployment");
		this.setApiVersion("");
	}
}
