package com.harmonycloud.service.platform.bean;

import java.util.List;

/**
 * Created by root on 5/19/17.
 */
public class UserProjectBiding {
    private String harborHost;
    private Integer harborProjectId;
    //角色id列表 1:admin 2:dev 3:watcher
    private Integer harborRoleType;
    private List<String> users;

    public List<String> getUserNames() {
        return users;
    }

    public void setUserNames(List<String> users) {
        this.users = users;
    }


    public String getHarborHost() {
        return harborHost;
    }

    public void setHarborHost(String harborHost) {
        this.harborHost = harborHost;
    }

    public Integer getHarborProjectId() {
        return harborProjectId;
    }

    public void setHarborProjectId(Integer harborProjectId) {
        this.harborProjectId = harborProjectId;
    }

    public Integer getHarborRoleType() {
        return harborRoleType;
    }

    public void setHarborRoleType(Integer harborRoleType) {
        this.harborRoleType = harborRoleType;
    }
}
