package com.harmonycloud.dto.cluster;

import java.io.Serializable;
import java.util.List;

/**
 * 分区迁移dto
 * @author youpeiyuan
 *
 */
public class ClusterTransferDto implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 租户Id
     */
    private String tenantId;

    /**
     * 当前的集群id
     */
    private String currentClusterId;

    /**
     * 目标集群Id
     */
    private String targetClusterId;

    /**
     * 项目Id的集合
     */
    private List<String> projectId;

    /**
     * 是否是断点续传
     */
    private boolean isContinue;

    /**
     * 分区绑定所需要的属性
     */
    private List<BindNameSpaceDto> bindNameSpaceDtos;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCurrentClusterId() {
        return currentClusterId;
    }

    public void setCurrentClusterId(String currentClusterId) {
        this.currentClusterId = currentClusterId;
    }

    public String getTargetClusterId() {
        return targetClusterId;
    }

    public void setTargetClusterId(String targetClusterId) {
        this.targetClusterId = targetClusterId;
    }

    public List<String> getProjectId() {
        return projectId;
    }

    public void setProjectId(List<String> projectId) {
        this.projectId = projectId;
    }

    public List<BindNameSpaceDto> getBindNameSpaceDtos() {
        return bindNameSpaceDtos;
    }

    public void setBindNameSpaceDtos(List<BindNameSpaceDto> bindNameSpaceDtos) {
        this.bindNameSpaceDtos = bindNameSpaceDtos;
    }

    public boolean isContinue() {
        return isContinue;
    }

    public void setContinue(boolean isContinue) {
        this.isContinue = isContinue;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ClusterTransferDto [tenantId=");
        builder.append(tenantId);
        builder.append(", currentClusterId=");
        builder.append(currentClusterId);
        builder.append(", targetClusterId=");
        builder.append(targetClusterId);
        builder.append(", projectId=");
        builder.append(projectId);
        builder.append(", isContinue=");
        builder.append(isContinue);
        builder.append(", bindNameSpaceDtos=");
        builder.append(bindNameSpaceDtos);
        builder.append("]");
        return builder.toString();
    }



}
