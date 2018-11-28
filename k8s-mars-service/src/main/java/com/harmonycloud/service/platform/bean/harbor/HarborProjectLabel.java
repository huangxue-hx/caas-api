package com.harmonycloud.service.platform.bean.harbor;


import java.io.Serializable;

/**
 * Created by zsl on 2017/1/19.
 * HarborProjectLabel
 */
public class HarborProjectLabel implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Integer project_id;                  //id
    private Integer id;                  //id
    private String name;
    private String description;
    private String color;
    private String scope;
    private String creation_time;
    private String update_time;
    private boolean deleted;

    public Integer getProject_id() {
        return project_id;
    }

    public void setProject_id(Integer project_id) {
        this.project_id = project_id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getCreation_time() {
        return creation_time;
    }

    public void setCreation_time(String creation_time) {
        this.creation_time = creation_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
