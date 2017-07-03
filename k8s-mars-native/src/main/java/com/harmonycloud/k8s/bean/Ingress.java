package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ingress extends BaseResource{

	private IngressSpec spec;
	
	private IngressStatus status;

	public IngressSpec getSpec() {
		return spec;
	}

	public void setSpec(IngressSpec spec) {
		this.spec = spec;
	}

	public IngressStatus getStatus() {
		return status;
	}

	public void setStatus(IngressStatus status) {
		this.status = status;
	}
	
	
}
