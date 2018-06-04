package com.harmonycloud.service.platform.bean;

import java.util.List;

/**
 * Created by root on 5/19/17.
 */
public class ProjectUserBinding {

    private String userName;
    private Long userId;
    private String harborHost;
    private Integer harborRoleType;
    private List<Integer> projects;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String user) {
        this.userName = user;
    }

    public List<Integer> getProjects() {
        return projects;
    }

    public void setProjects(List<Integer> projects) {
        this.projects = projects;
    }

    public String getHarborHost() {
        return harborHost;
    }

    public void setHarborHost(String harborHost) {
        this.harborHost = harborHost;
    }

    public Integer getHarborRoleType() {
        return harborRoleType;
    }

    public void setHarborRoleType(Integer harborRoleType) {
        this.harborRoleType = harborRoleType;
    }
}
