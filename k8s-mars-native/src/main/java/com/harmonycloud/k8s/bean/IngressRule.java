package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
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
