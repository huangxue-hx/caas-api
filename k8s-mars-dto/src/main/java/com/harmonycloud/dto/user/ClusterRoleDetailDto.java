package com.harmonycloud.dto.user;

import java.util.List;

import com.harmonycloud.dao.user.bean.RoleResource;



public class ClusterRoleDetailDto {
	private Integer bindCount;
	private Integer index;
	private String name;
	private List<String> noneResource;
	private List<RoleResource> resource;
	private String time;
	private String type;
	
	
	public Integer getBindCount() {
		return bindCount;
	}
	public void setBindCount(Integer bindCount) {
		this.bindCount = bindCount;
	}
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getNoneResource() {
		return noneResource;
	}
	public void setNoneResource(List<String> noneResource) {
		this.noneResource = noneResource;
	}
	
	public List<RoleResource> getResource() {
		return resource;
	}
	public void setResource(List<RoleResource> resource) {
		this.resource = resource;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
