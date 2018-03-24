package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
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
