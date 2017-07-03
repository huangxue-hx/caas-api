package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HTTPIngressPath {

	private String path;
	
	private IngressBackend backend;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public IngressBackend getBackend() {
		return backend;
	}

	public void setBackend(IngressBackend backend) {
		this.backend = backend;
	}
	
	
}
