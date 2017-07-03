package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NamespaceSpec {

	private List<String> finalizers;

	public List<String> getFinalizers() {
		return finalizers;
	}

	public void setFinalizers(List<String> finalizers) {
		this.finalizers = finalizers;
	}	
	
	
}
