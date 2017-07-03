package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DaemonEndpoint {

	private Integer Port;

	public Integer getPort() {
		return Port;
	}

	public void setPort(Integer port) {
		Port = port;
	}
	
}
