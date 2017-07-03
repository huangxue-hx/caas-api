package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkPolicyPeer {

	private List<LabelSelector> podSelector;
	
	private List<LabelSelector> namespaceSelector;

	public List<LabelSelector> getPodSelector() {
		return podSelector;
	}

	public void setPodSelector(List<LabelSelector> podSelector) {
		this.podSelector = podSelector;
	}

	public List<LabelSelector> getNamespaceSelector() {
		return namespaceSelector;
	}

	public void setNamespaceSelector(List<LabelSelector> namespaceSelector) {
		this.namespaceSelector = namespaceSelector;
	}
	
	
}
