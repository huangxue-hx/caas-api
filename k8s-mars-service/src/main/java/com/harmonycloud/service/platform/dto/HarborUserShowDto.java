package com.harmonycloud.service.platform.dto;

import java.util.List;

/**
 * Created by andy on 17-2-9.
 */
public class HarborUserShowDto {

    private String harborid;

    private String harborProjectName;

    private String namespace;

    private String role;

    private List<String> user;

    private List<String> userId;

    private String time;

    public String getHarborid() {
        return harborid;
    }

    public void setHarborid(String harborid) {
        this.harborid = harborid;
    }

    public String getHarborProjectName() {
        return harborProjectName;
    }

    public void setHarborProjectName(String harborProjectName) {
        this.harborProjectName = harborProjectName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getUser() {
        return user;
    }

    public void setUser(List<String> user) {
        this.user = user;
    }

    public List<String> getUserId() {
        return userId;
    }

    public void setUserId(List<String> userId) {
        this.userId = userId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
