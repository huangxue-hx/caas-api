package com.harmonycloud.dto.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePortDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String port;
	
	private String protocol;
	
	private String expose;
	
	private String containerPort;

	private String name;

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getPort() {
		return port;	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getExpose() {
		return expose;
	}

	public void setExpose(String expose) {
		this.expose = expose;
	}

	public String getContainerPort() {
		return containerPort;
	}

	public void setContainerPort(String containerPort) {
		this.containerPort = containerPort;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
