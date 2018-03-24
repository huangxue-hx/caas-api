package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
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
