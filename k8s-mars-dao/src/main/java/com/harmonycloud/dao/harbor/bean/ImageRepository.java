package com.harmonycloud.dao.harbor.bean;

import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional(rollbackFor = Exception.class)
public class ImageRepository {

    private Integer id;
    private Integer harborProjectId;
    private String harborProjectName;
    private String repositoryName;
    private String tenantId;
    private String clusterId;
    private String harborHost;
    private String projectId;
    private Date createTime;
    private Float quotaSize;
    private Float usageSize;
    private Float usageRate;
    private Boolean isDefault;
    private Boolean isPublic;
    private String clusterName;
    private Boolean isNormal;
    private Integer imageCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getHarborProjectId() {
        return harborProjectId;
    }

    public void setHarborProjectId(Integer harborProjectId) {
        this.harborProjectId = harborProjectId;
    }

    public String getHarborProjectName() {
        return harborProjectName;
    }

    public void setHarborProjectName(String harborProjectName) {
        this.harborProjectName = harborProjectName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean isPublic() {
        return isPublic;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }



    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Boolean isNormal() {
        return isNormal;
    }

    public Boolean getIsNormal() {
        return isNormal;
    }

    public void setIsNormal(Boolean normal) {
        isNormal = normal;
    }

    public Integer getImageCount() {
        return imageCount;
    }

    public void setImageCount(Integer imageCount) {
        this.imageCount = imageCount;
    }


    public Float getQuotaSize() {
        return quotaSize;
    }

    public void setQuotaSize(Float quotaSize) {
        this.quotaSize = quotaSize;
    }

    public Float getUsageSize() {
        return usageSize;
    }

    public void setUsageSize(Float usageSize) {
        this.usageSize = usageSize;
    }

    public Float getUsageRate() {
        return usageRate;
    }

    public void setUsageRate(Float usageRate) {
        this.usageRate = usageRate;
    }

    public String getHarborHost() {
        return harborHost;
    }

    public void setHarborHost(String harborHost) {
        this.harborHost = harborHost;
    }

}
