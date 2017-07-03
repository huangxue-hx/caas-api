package com.harmonycloud.dao.user.bean;

import java.util.List;

public class InitClusterRole {
	private String name;
	private String index;
	private List<RoleResource> resource;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public List<RoleResource> getResource() {
		return resource;
	}

	public void setResource(List<RoleResource> resource) {
		this.resource = resource;
	}

}
