package com.harmonycloud.service.platform.bean;

import java.util.List;

public class TenantHarborDetail {
	private String name;
	private String type;
	private String namespace;
	private List<String> user;
	private String userId;
	private String role;
	private String imageVerb;
	private String harborProjectId;
	private String time;
	private String tenantName;
	private String tenantId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public List<String> getUser() {
		return user;
	}

	public void setUser(List<String> user) {
		this.user = user;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getImageVerb() {
		return imageVerb;
	}

	public void setImageVerb(String imageVerb) {
		this.imageVerb = imageVerb;
	}

	public String getHarborProjectId() {
		return harborProjectId;
	}

	public void setHarborProjectId(String harborProjectId) {
		this.harborProjectId = harborProjectId;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

}
