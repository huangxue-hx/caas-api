package com.harmonycloud.service.platform.bean;

public class UserProjectDto {
	private Integer project;
	private String projectName;
	private String role;
	private String roleBindingName;
	private String tenantName;
	private String tenantid;

	public Integer getProject() {
		return project;
	}

	public void setProject(Integer project) {
		this.project = project;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getRoleBindingName() {
		return roleBindingName;
	}

	public void setRoleBindingName(String roleBindingName) {
		this.roleBindingName = roleBindingName;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getTenantid() {
		return tenantid;
	}

	public void setTenantid(String tenantid) {
		this.tenantid = tenantid;
	}

}
