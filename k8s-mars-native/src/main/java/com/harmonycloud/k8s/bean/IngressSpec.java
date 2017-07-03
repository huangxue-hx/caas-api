package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IngressSpec {

	private IngressBackend backend;
	
	private List<IngressTLS> tls;
	
	private List<IngressRule> rules;

	public List<IngressRule> getRules() {
		return rules;
	}

	public void setRules(List<IngressRule> rules) {
		this.rules = rules;
	}

	public IngressBackend getBackend() {
		return backend;
	}

	public void setBackend(IngressBackend backend) {
		this.backend = backend;
	}

	public List<IngressTLS> getTls() {
		return tls;
	}

	public void setTls(List<IngressTLS> tls) {
		this.tls = tls;
	}
	
}
