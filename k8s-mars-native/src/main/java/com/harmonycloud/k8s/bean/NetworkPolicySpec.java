package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkPolicySpec {

	private LabelSelector podSelector;
	
	private List<NetworkPolicyIngressRule> ingress;

	public LabelSelector getPodSelector() {
		return podSelector;
	}

	public void setPodSelector(LabelSelector podSelector) {
		this.podSelector = podSelector;
	}

	public List<NetworkPolicyIngressRule> getIngress() {
		return ingress;
	}

	public void setIngress(List<NetworkPolicyIngressRule> ingress) {
		this.ingress = ingress;
	}
	
	
}
