package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IngressRule {

	private String host;
	
	private HTTPIngressRuleValue http;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public HTTPIngressRuleValue getHttp() {
		return http;
	}

	public void setHttp(HTTPIngressRuleValue http) {
		this.http = http;
	}
	
}
