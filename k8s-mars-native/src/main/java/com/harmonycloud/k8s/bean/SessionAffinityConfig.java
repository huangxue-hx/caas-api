package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionAffinityConfig {
	
	private ClientIPConfig clientIP;

	public ClientIPConfig getClientIP() {
		return clientIP;
	}

	public void setClientIP(ClientIPConfig clientIP) {
		this.clientIP = clientIP;
	}

}
