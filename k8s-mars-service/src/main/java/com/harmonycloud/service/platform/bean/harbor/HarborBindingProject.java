package com.harmonycloud.service.platform.bean.harbor;

public class HarborBindingProject {
	private String harborHost;
	private String projectId;
	private String role;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getHarborHost() {
		return harborHost;
	}

	public void setHarborHost(String harborHost) {
		this.harborHost = harborHost;
	}
}
