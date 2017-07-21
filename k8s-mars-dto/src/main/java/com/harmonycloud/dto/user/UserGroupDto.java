package com.harmonycloud.dto.user;

import java.util.List;

import com.harmonycloud.dao.user.bean.UserGroup;

public class UserGroupDto {

	private UserGroup usergroup;
	
	private String updategroupname;
	
	private String updatedescribe;
	
	private List<String> addusers;
	
	private List<String> delusers;

	public UserGroup getUsergroup() {
		return usergroup;
	}

	public void setUsergroup(UserGroup usergroup) {
		this.usergroup = usergroup;
	}

	public String getUpdategroupname() {
		return updategroupname;
	}

	public void setUpdategroupname(String updategroupname) {
		this.updategroupname = updategroupname;
	}

	public String getUpdatedescribe() {
		return updatedescribe;
	}

	public void setUpdatedescribe(String updatedescribe) {
		this.updatedescribe = updatedescribe;
	}

	public List<String> getAddusers() {
		return addusers;
	}

	public void setAddusers(List<String> addusers) {
		this.addusers = addusers;
	}

	public List<String> getDelusers() {
		return delusers;
	}

	public void setDelusers(List<String> delusers) {
		this.delusers = delusers;
	}
	
}
