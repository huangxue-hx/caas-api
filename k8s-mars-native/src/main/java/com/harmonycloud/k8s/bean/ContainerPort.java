package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerPort {
	
	private String name;
	
	private Integer hostPort;
	
	private Integer containerPort;
	
	private String protocol;
	
	private String hostIP;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getHostPort() {
		return hostPort;
	}

	public void setHostPort(Integer hostPort) {
		this.hostPort = hostPort;
	}

	public Integer getContainerPort() {
		return containerPort;
	}

	public void setContainerPort(Integer containerPort) {
		this.containerPort = containerPort;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHostIP() {
		return hostIP;
	}

	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}
	
}
