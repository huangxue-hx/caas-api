package com.harmonycloud.service.platform.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zsl on 2017/1/19.
 * harbor role bean
 */
public class HarborRole implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String username;            //用户名
    private List<Integer> roleList;     //角色id列表 1:admin 2:dev 3:watcher

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Integer> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<Integer> roleList) {
        this.roleList = roleList;
    }
}
