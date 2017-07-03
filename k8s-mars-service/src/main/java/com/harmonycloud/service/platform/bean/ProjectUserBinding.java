package com.harmonycloud.service.platform.bean;

import java.util.List;

/**
 * Created by root on 5/19/17.
 */
public class ProjectUserBinding {

    private String userName;
    private List<String> projects;

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
