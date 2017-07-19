package com.harmonycloud.dao.user.bean;

import java.util.List;

public class UserGroup {
    private Integer id;

    private String groupname;

    private String userGroupDescribe;
    
    private List<User> users;

    public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname == null ? null : groupname.trim();
    }

    public String getUserGroupDescribe() {
        return userGroupDescribe;
    }

    public void setUserGroupDescribe(String userGroupDescribe) {
        this.userGroupDescribe = userGroupDescribe == null ? null : userGroupDescribe.trim();
    }
}