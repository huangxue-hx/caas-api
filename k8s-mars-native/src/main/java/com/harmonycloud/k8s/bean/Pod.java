package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pod extends BaseResource{

	private PodSpec spec;
	
	private PodStatus status;

	public PodSpec getSpec() {
		return spec;
	}

	public void setSpec(PodSpec spec) {
		this.spec = spec;
	}

	public PodStatus getStatus() {
		return status;
	}

	public void setStatus(PodStatus status) {
		this.status = status;
	}
	
}
