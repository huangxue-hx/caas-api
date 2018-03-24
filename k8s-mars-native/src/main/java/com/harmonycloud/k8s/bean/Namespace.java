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
public class Namespace extends BaseResource{

	private NamespaceSpec spec;
	
	private NamespaceStatus status;
	
	public Namespace() {
		this.setApiVersion(Constant.NAMESPACE_API_VERSION);
		this.setKind("Namespace");
	}

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
