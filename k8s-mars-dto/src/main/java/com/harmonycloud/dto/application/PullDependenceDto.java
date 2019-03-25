package com.harmonycloud.dto.application;

import java.io.Serializable;

/**
 * Created by chencheng on 18-8-1
 *
 * 拉取依赖
 */
public class PullDependenceDto implements Serializable {

    private String pullWay;//拉取方式

    private String repoUrl;//地址

    private String branch;//分支

    private String tag;//标签

    private String username;//用户名

    private String password;//密码

    private String container;//容器

    private String mountPath;//挂载路径

    public String getPullWay() {
        return pullWay;
    }

    public void setPullWay(String pullWay) {
        this.pullWay = pullWay;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getMountPath() {
        return mountPath;
    }

    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }
}
