package com.harmonycloud.service.platform.bean.harbor;

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
	private Integer userId;
	private String username;        //用户名
    private String password;
    private String realName;
    private String email;
    private String comment;
    private String roleName;        //用户角色
    private String creationTime;    //用户创建时间

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
