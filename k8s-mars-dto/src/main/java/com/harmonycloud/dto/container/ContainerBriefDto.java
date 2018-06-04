package com.harmonycloud.dto.container;

import java.util.List;

public class ContainerBriefDto {

	private List<String> containers;

	private List<String> pods;

	private String deploymentName;

	public ContainerBriefDto() {
		super();
	}

	public List<String> getContainers() {
		return containers;
	}

	public void setContainers(List<String> containers) {
		this.containers = containers;
	}

	public List<String> getPods() {
		return pods;
	}

	public void setPods(List<String> pods) {
		this.pods = pods;
	}

	public String getDeploymentName() {
		return deploymentName;
	}

	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}
}
