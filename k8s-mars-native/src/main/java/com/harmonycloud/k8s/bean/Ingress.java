package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.constant.Constant;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ingress extends BaseResource{

	private IngressSpec spec;
	
	private IngressStatus status;
	
	public Ingress() {
		this.setKind("Ingress");
		this.setApiVersion(Constant.INGRESS_VERSION);
	}

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
