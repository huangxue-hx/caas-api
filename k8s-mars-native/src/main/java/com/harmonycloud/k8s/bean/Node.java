package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Node extends BaseResource{

	private NodeSpec spec;
	
	private NodeStatus status;

	public NodeSpec getSpec() {
		return spec;
	}

	public void setSpec(NodeSpec spec) {
		this.spec = spec;
	}

	public NodeStatus getStatus() {
		return status;
	}

	public void setStatus(NodeStatus status) {
		this.status = status;
	}
	
	
}
