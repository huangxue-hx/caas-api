package com.harmonycloud.dto.config;

import com.harmonycloud.dao.application.bean.ConfigFileItem;

import java.io.Serializable;
import java.util.List;

public class ConfigDetailDto implements Serializable {

    private static final long serialVersionUID = -3551823489609889016L;
    private String id;
    private String name;
    private String description;
    private String tenantId;
    private String projectId;
    private String clusterId;
    private String clusterName;
    private String repoName;
    private String isCreate;
    private String tags;
    private List<ConfigFileItem> configFileItemList;

    public List<ConfigFileItem> getConfigFileItemList() {
        return configFileItemList;
    }

    public void setConfigFileItemList(List<ConfigFileItem> configFileItemList) {
        this.configFileItemList = configFileItemList;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getIsCreate() {
        return isCreate;
    }

    public void setIsCreate(String isCreate) {
        this.isCreate = isCreate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

}
