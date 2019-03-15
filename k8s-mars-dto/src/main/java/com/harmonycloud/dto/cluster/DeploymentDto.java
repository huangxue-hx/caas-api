package com.harmonycloud.dto.cluster;

/**
 * 迁移分区指定服务dto
 * @author youpeiyuan
 *
 */
public class DeploymentDto {

    /**
     * 服务名称
     */
    private String deployName;

    /**
     * 项目id
     */
    private String projectId;
    // 应用名称
    private String appName;
    /**
     * 服务类型
     */
    private String serviceType;

    private String namespace;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getDeployName() {
        return deployName;
    }

    public void setDeployName(String deployName) {
        this.deployName = deployName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DeploymentDto [deployName=");
        builder.append(deployName);
        builder.append(", projectId=");
        builder.append(projectId);
        builder.append(", serviceType=");
        builder.append(serviceType);
        builder.append(", namespace=");
        builder.append(namespace);
        builder.append(", appName=");
        builder.append(appName);
        builder.append("]");
        return builder.toString();
    }


}
