package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Service extends BaseResource{

	private ServiceSpec spec;
	
	private ServiceStatus status;

	public ServiceStatus getStatus() {
		return status;
	}

	public void setStatus(ServiceStatus status) {
		this.status = status;
	}

	public ServiceSpec getSpec() {
		return spec;
	}

	public void setSpec(ServiceSpec spec) {
		this.spec = spec;
	}
	
	public Service(){
		this.setApiVersion("");
		this.setKind("");
	}
}
