package com.harmonycloud.service.platform.bean;

/**
 * 镜像仓库信息
 *
 */
public class RepositoryInfo {
    private String clusterId;
    private String tenantId;
    private Float quotaSize;
    /**
     * 用户自定义项目名称
     */
    private String repositorySuffixName;
    /**
     * 项目id（非harbor project）
     */
    private String projectId;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 在harbor创建的项目名称(项目名+环境名)
     */
    private String harborProjectName;
    private Boolean isDefault;
    private Boolean isPublic;
    private String harborHost;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Float getQuotaSize() {
        return quotaSize;
    }

    public void setQuotaSize(Float quotaSize) {
        this.quotaSize = quotaSize;
    }

    public String getRepositorySuffixName() {
        return repositorySuffixName;
    }

    public void setRepositorySuffixName(String repositorySuffixName) {
        this.repositorySuffixName = repositorySuffixName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getHarborProjectName() {
        return harborProjectName;
    }

    public void setHarborProjectName(String harborProjectName) {
        this.harborProjectName = harborProjectName;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getHarborHost() {
        return harborHost;
    }

    public void setHarborHost(String harborHost) {
        this.harborHost = harborHost;
    }
}
