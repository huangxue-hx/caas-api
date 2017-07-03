package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Namespace extends BaseResource{

	private NamespaceSpec spec;
	
	private NamespaceStatus status;

	public NamespaceSpec getSpec() {
		return spec;
	}

	public void setSpec(NamespaceSpec spec) {
		this.spec = spec;
	}

	public NamespaceStatus getStatus() {
		return status;
	}

	public void setStatus(NamespaceStatus status) {
		this.status = status;
	}
	
	
}
