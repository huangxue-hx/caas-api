package com.harmonycloud.dto.user;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="用户查询筛选条件")
public class UserQueryDto {

	@ApiModelProperty(value="是否查询系统管理员",name="isAdmin",example="true",required = false)
	private Boolean isAdmin;
	@ApiModelProperty(value="是否查询机器账号",name="isMachine",example="false",required = false)
	private Boolean isMachine;
	@ApiModelProperty(value="是否查询普通用户",name="isCommon",example="false",required = false)
	private Boolean isCommon;
	@ApiModelProperty(value="是否查询所有用户",name="all",example="false",required = false)
	private Boolean all;
	@ApiModelProperty(value="查询指定用户id，逗号分割",name="userIds",example="1,2",required = false)
	private String userIds;


	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean admin) {
		isAdmin = admin;
	}

	public Boolean getIsMachine() {
		return isMachine;
	}

	public void setIsMachine(Boolean machine) {
		isMachine = machine;
	}

	public Boolean getIsCommon() {
		return isCommon;
	}

	public void setIsCommon(Boolean common) {
		isCommon = common;
	}

	public Boolean getAll() {
		return all;
	}

	public void setAll(Boolean all) {
		this.all = all;
	}

	public String getUserIds() {
		return userIds;
	}

	public void setUserIds(String userIds) {
		this.userIds = userIds;
	}

}
