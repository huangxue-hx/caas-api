package com.harmonycloud.service.platform.bean;

import java.util.List;

/**
 * Created by root on 5/19/17.
 */
public class ProjectUserBinding {

    private String userName;
    private Integer userId;
    private List<String> projects;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String user) {
        this.userName = user;
    }

    public List<String> getProjects() {
        return projects;
    }

    public void setProjects(List<String> projects) {
        this.projects = projects;
    }
}
