package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeDaemonEndpoints {

	private DaemonEndpoint kubeletEndpoint;

	public DaemonEndpoint getKubeletEndpoint() {
		return kubeletEndpoint;
	}

	public void setKubeletEndpoint(DaemonEndpoint kubeletEndpoint) {
		this.kubeletEndpoint = kubeletEndpoint;
	}
	
}
