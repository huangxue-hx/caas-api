package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkPolicy extends BaseResource{
	
	private NetworkPolicySpec spec;

	public NetworkPolicySpec getSpec() {
		return spec;
	}

	public void setSpec(NetworkPolicySpec spec) {
		this.spec = spec;
	}
	
	
}
