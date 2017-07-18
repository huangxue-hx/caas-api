package com.harmonycloud.dto.container;

public class ContainerBriefDto {

	private String name;

	private String deploymentName;

	public ContainerBriefDto() {
		super();
	}

	public ContainerBriefDto(String name, String deploymentName) {
		this.name = name;
		this.deploymentName = deploymentName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDeploymentName() {
		return deploymentName;
	}

	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}
}
