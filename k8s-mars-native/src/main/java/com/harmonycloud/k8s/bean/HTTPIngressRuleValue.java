package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HTTPIngressRuleValue {

	private List<HTTPIngressPath> paths;

	public List<HTTPIngressPath> getPaths() {
		return paths;
	}

	public void setPaths(List<HTTPIngressPath> paths) {
		this.paths = paths;
	}
	
}
