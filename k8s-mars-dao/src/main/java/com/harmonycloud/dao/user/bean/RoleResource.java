package com.harmonycloud.dao.user.bean;

import java.util.List;

/**
 * 资源权限
 * @author yj
 * @date 2017年1月6日
 */
public class RoleResource {
	
	private String name;
	// 资源对应的操作
	private List<String> operations;
	private List<String> apiGroups;
	
	public List<String> getApiGroups() {
		return apiGroups;
	}
	public void setApiGroups(List<String> apiGroups) {
		this.apiGroups = apiGroups;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getOperations() {
		return operations;
	}
	public void setOperations(List<String> operations) {
		this.operations = operations;
	}
	
}
