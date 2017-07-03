package com.harmonycloud.dto.business;

public class TcpRuleDto {
	private String name;
	private String targetPort;
	private String protocol;
	private String port;
	private Boolean isEdit;
	public String getTargetPort() {
		return targetPort;
	}
	public void setTargetPort(String targetPort) {
		this.targetPort = targetPort;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean getIsEdit() {
		return isEdit;
	}
	public void setIsEdit(Boolean isEdit) {
		this.isEdit = isEdit;
	}
	
}
