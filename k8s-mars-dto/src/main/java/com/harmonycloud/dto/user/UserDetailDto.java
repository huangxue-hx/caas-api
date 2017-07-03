package com.harmonycloud.dto.user;

import java.util.List;

public class UserDetailDto {
	//角色名称
	private String name;
	//角色类型
	private String type;
	private String namespace;
	//该角色用户名称
	private List<String> uses;
	private List<String> userIds;
	//该用户角色
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
	
	public List<String> getUses() {
		return uses;
	}
	public void setUses(List<String> uses) {
		this.uses = uses;
	}
	public List<String> getUserIds() {
		return userIds;
	}
	public void setUserIds(List<String> userIds) {
		this.userIds = userIds;
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
