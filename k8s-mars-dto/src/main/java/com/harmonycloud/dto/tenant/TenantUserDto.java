package com.harmonycloud.dto.tenant;

public class TenantUserDto {
	private String name;
	private String namespace;
	private String role;
	private String roleBindingName;
	private String time;

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

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
