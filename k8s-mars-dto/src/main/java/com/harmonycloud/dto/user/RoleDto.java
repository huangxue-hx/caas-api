package com.harmonycloud.dto.user;

import com.harmonycloud.dao.user.bean.RoleResource;

public class RoleDto {
	
	private Integer id;
	private String name;
	//角色描述
	private String description;
	private String createDate;
	private String updateDate;
	//角色是否可用
	private Integer available;
	//角色类型
	private String type;
	//角色权重
	private String index;
	//角色被绑定次数
	private Integer bindCount;
	//角色拥有的资源
	private RoleResource[] resources;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	public Integer getAvailable() {
		return available;
	}
	public void setAvailable(Integer available) {
		this.available = available;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public Integer getBindCount() {
		return bindCount;
	}
	public void setBindCount(Integer bindCount) {
		this.bindCount = bindCount;
	}
	public RoleResource[] getResources() {
		return resources;
	}
	public void setResources(RoleResource[] resources) {
		this.resources = resources;
	}
}
