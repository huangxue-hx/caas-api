package com.harmonycloud.dao.harbor.bean;

import java.io.Serializable;
import java.util.Date;

public class ImageCleanRule implements Serializable {

    private static final long serialVersionUID = -5875405590484913716L;
    private Long id;
    private String name;
    private Integer type;
    private Integer repositoryId;
    private String harborProjectName;
    private String repoName;
    private Integer keepTagCount;
    private Integer timeBefore;
    private String tagNameExclude;
    private Date createTime;
    private Date updateTime;
    private String userName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public Integer getKeepTagCount() {
        return keepTagCount;
    }

    public Integer getTimeBefore() {
        return timeBefore;
    }

    public void setTimeBefore(Integer timeBefore) {
        this.timeBefore = timeBefore;
    }

    public void setKeepTagCount(Integer keepTagCount) {
        this.keepTagCount = keepTagCount;
    }

    public String getTagNameExclude() {
        return tagNameExclude;
    }

    public void setTagNameExclude(String tagNameExclude) {
        this.tagNameExclude = tagNameExclude;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Integer repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHarborProjectName() {
        return harborProjectName;
    }

    public void setHarborProjectName(String harborProjectName) {
        this.harborProjectName = harborProjectName;
    }
}
