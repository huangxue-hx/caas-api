package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkPolicyIngressRule {

	private List<NetworkPolicyPort> ports;
	
	private List<NetworkPolicyPeer> from;

	public List<NetworkPolicyPort> getPorts() {
		return ports;
	}

	public void setPorts(List<NetworkPolicyPort> ports) {
		this.ports = ports;
	}

	public List<NetworkPolicyPeer> getFrom() {
		return from;
	}

	public void setFrom(List<NetworkPolicyPeer> from) {
		this.from = from;
	}
	
	
}
