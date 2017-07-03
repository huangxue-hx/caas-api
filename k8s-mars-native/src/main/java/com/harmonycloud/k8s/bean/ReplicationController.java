package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 * rc
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicationController extends BaseResource{	
	
	private ReplicationControllerSpec spec;
	
	private ReplicationControllerStatus status;
		
	public ReplicationControllerStatus getStatus() {
		return status;
	}

	public void setStatus(ReplicationControllerStatus status) {
		this.status = status;
	}

	public ReplicationControllerSpec getSpec() {
		return spec;
	}

	public void setSpec(ReplicationControllerSpec spec) {
		this.spec = spec;
	}

	ReplicationController(){
		this.setKind("ReplicationController");
		this.setApiVersion("");
	}
}
