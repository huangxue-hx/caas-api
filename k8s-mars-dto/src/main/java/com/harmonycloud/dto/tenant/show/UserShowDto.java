package com.harmonycloud.dto.tenant.show;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by andy on 17-2-6.
 */
public class UserShowDto {

    private Long id;

    private String name;

    private String email;
    
    private String nikeName;
    
    private String createTime;

    private String comment;
    
    private Boolean isTm;

    private String updateTime;
    
    private String pause;

    private String phone;
    private Integer isAuthorize;

    private String roleName;

    private String roleDiscription;

    public String getRoleDiscription() {
        return roleDiscription;
    }

    public void setRoleDiscription(String roleDiscription) {
        this.roleDiscription = roleDiscription;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Integer getIsAuthorize() {
        return isAuthorize;
    }

    public void setIsAuthorize(Integer isAuthorize) {
        this.isAuthorize = isAuthorize;
    }

    private String groupName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPause() {
        return pause;
    }

    public void setPause(String pause) {
        this.pause = pause;
    }

    public String getUpdateTime() {
    	return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

    public Boolean getIsTm() {
        return isTm;
    }

    public void setIsTm(Boolean isTm) {
        this.isTm = isTm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNikeName() {
        return nikeName;
    }

    public void setNikeName(String nikeName) {
        this.nikeName = nikeName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}