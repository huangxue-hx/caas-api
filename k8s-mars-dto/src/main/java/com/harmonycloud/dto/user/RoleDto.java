package com.harmonycloud.dto.user;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class RoleDto {
	
	private Integer id;
	//角色名
	private String name;
	//角色昵称
	private String nickName;
	//创建时间
	private Date createTime;
	//更新时间
	private Date updateTime;
	//角色是否可用
	private Boolean available;
	//角色作用域集群ids
	private String clusterIds;
	//角色作用域集群分区名
	private String namespaceNames;

	private List<PrivilegeDto> rolePrivilegeList;

	public List<PrivilegeDto> getRolePrivilegeList() {
		return rolePrivilegeList;
	}

	public void setRolePrivilegeList(List<PrivilegeDto> rolePrivilegeList) {
		this.rolePrivilegeList = rolePrivilegeList;
	}

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

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Boolean getAvailable() {
		return available;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
	}

	public String getClusterIds() {
		return clusterIds;
	}

	public void setClusterIds(String clusterIds) {
		this.clusterIds = clusterIds;
	}

	public String getNamespaceNames() {
		return namespaceNames;
	}

	public void setNamespaceNames(String namespaceNames) {
		this.namespaceNames = namespaceNames;
	}
}
