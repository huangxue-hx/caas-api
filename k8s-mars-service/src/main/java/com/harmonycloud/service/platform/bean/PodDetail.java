package com.harmonycloud.service.platform.bean;

import java.util.List;

public class PodDetail {
	
	private String name;
	
	private String namespace;
	
	private String status;
	
	private String ip;
	
	private String nodeIp;
	
	private String startTime;
	
	private String tag;
	
	private List<ContainerWithStatus> containers;

	private String deployment;

	private String deployVersion;

	private Boolean isTerminating;
	
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

	public String getDeployVersion() {
		return deployVersion;
	}

	public void setDeployVersion(String deployVersion) {
		this.deployVersion = deployVersion;
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

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getDeployment() {
		return deployment;
	}

	public void setDeployment(String deployment) {
		this.deployment = deployment;
	}

	public Boolean getTerminating() {
		return isTerminating;
	}

	public void setTerminating(Boolean terminating) {
		isTerminating = terminating;
	}
}
