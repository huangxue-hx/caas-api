package com.harmonycloud.dto.tenant.show;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by andy on 17-2-6.
 */
public class UserShowDto {

    private String name;

    private String email;
    
    private String nikeName;
    
    private String createTime;

    private String comment;
    
    private Boolean isTm;

    private String updateTime;

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
}