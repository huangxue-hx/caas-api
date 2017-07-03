package com.harmonycloud.service.platform.bean;

import java.util.List;

public class PodDetail {
	
	private String name;
	
	private String namespace;
	
	private String status;
	
	private String ip;
	
	private String nodeIp;
	
	private String startTime;
	
	private List<ContainerWithStatus> containers;
	
	public PodDetail() {
		
	}
	
	public PodDetail(String name, String namespace, String status, String ip, String nodeIp, String startTime) {
		this.name = name;
		this.namespace = namespace;
		this.status = status;
		this.ip = ip;
		this.nodeIp = nodeIp;
		this.startTime = startTime;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getNodeIp() {
		return nodeIp;
	}

	public void setNodeIp(String nodeIp) {
		this.nodeIp = nodeIp;
	}

	public List<ContainerWithStatus> getContainers() {
		return containers;
	}

	public void setContainers(List<ContainerWithStatus> containers) {
		this.containers = containers;
	}

}
