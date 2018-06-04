package com.harmonycloud.service.platform.bean;

public class PodDto {
	private String ip;
	private String name;
	private String namespace;
	private String startTime;
	private String status;
	private String nodeName;
	private String ownerReferenceKind;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
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

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getOwnerReferenceKind() {
		return ownerReferenceKind;
	}

	public void setOwnerReferenceKind(String ownerReferenceKind) {
		this.ownerReferenceKind = ownerReferenceKind;
	}
}
