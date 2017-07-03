package com.harmonycloud.service.platform.bean;

import java.util.List;

/**
 * Created by root on 5/19/17.
 */
public class UserProjectBiding {
    private String project;
    private List<String> users;

    public List<String> getUserNames() {
        return users;
    }

    public void setUserNames(List<String> users) {
        this.users = users;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
