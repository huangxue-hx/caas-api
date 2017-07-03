package com.harmonycloud.service.platform.bean;

import java.io.Serializable;

/**
 * Created by zsl on 2017/1/19.
 * harbor user bean
 */
public class HarborUser implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String username;        //用户名
    private String roleName;        //用户角色
    private String creationTime;    //用户创建时间

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }
}
