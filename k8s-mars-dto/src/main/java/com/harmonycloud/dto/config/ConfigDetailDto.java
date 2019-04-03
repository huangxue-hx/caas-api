package com.harmonycloud.dto.config;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataPrivilegeField;
import com.harmonycloud.common.enumm.DataPrivilegeType;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.dao.application.bean.ConfigFileItem;
import com.harmonycloud.k8s.bean.Deployment;

import java.io.Serializable;
import java.util.List;
@DataPrivilegeType(type = DataResourceTypeEnum.CONFIGFILE)
public class ConfigDetailDto implements Serializable {

    private static final long serialVersionUID = -3551823489609889016L;
    private String id;
    @DataPrivilegeField(type = CommonConstant.DATA_FIELD)
    private String name;
    private String description;
    private String tenantId;
    @DataPrivilegeField(type = CommonConstant.PROJECTID_FIELD)
    private String projectId;
    @DataPrivilegeField(type = CommonConstant.CLUSTERID_FIELD)
    private String clusterId;
    private String clusterName;
    private String clusterAliasName;
    private String repoName;
    private String isCreate;
    private String tags;
    private List<ConfigFileItem> configFileItemList;
    private List<Deployment> deploymentList;
    private boolean appStore;

    public List<Deployment> getDeploymentList() {
        return deploymentList;
    }

    public void setDeploymentList(List<Deployment> deploymentList) {
        this.deploymentList = deploymentList;
    }

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

    public String getClusterAliasName() {
        return clusterAliasName;
    }

    public void setClusterAliasName(String clusterAliasName) {
        this.clusterAliasName = clusterAliasName;
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

    public boolean getAppStore() {
        return appStore;
    }

    public void setAppStore(boolean appStore) {
        this.appStore = appStore;
    }
}
