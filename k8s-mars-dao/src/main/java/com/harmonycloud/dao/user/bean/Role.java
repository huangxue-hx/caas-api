package com.harmonycloud.dao.user.bean;

import java.io.Serializable;
import java.util.Date;

public class Role  implements Serializable{
    private Integer id;

    private String name;

    private String description;

    private String resourceIds;

    private Date createTime;

    private Date updateTime;

    private Boolean available;

    private String secondResourceIds;

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
        this.name = name == null ? null : name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getResourceIds() {
        return resourceIds;
    }
    // public List<Long> getResourceIds() {
    //     if(resourceIds == null) {
    //         resourceIds = new ArrayList<Long>();
    //     }
    //     return resourceIds;
    // }
    public void setResourceIds(String resourceIds) {
        this.resourceIds = resourceIds == null ? null : resourceIds.trim();
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

    public String getSecondResourceIds() {
        return secondResourceIds;
    }

    public void setSecondResourceIds(String secondResourceIds) {
        this.secondResourceIds = secondResourceIds == null ? null : secondResourceIds.trim();
    }
}