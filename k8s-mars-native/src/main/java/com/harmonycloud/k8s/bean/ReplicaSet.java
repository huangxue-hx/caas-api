package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicaSet extends BaseResource{

	private ReplicaSetSpec spec;
	
	private ReplicaSetStatus status;

	public ReplicaSetSpec getSpec() {
		return spec;
	}

	public void setSpec(ReplicaSetSpec spec) {
		this.spec = spec;
	}

	public ReplicaSetStatus getStatus() {
		return status;
	}

	public void setStatus(ReplicaSetStatus status) {
		this.status = status;
	}
	
}
