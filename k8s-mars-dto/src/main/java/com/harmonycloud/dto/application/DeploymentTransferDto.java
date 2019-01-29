package com.harmonycloud.dto.application;

public class DeploymentTransferDto {

	/**迁移目标的集群Id*/
	private String clusterId;
	/**迁移目标的项目要Id*/
	private String projectId;
	/**迁移目标的namespace*/
	private String namespace;
	/**迁移目标的租户Id*/
    private String tenantId;
    /**当前的集群Id*/
    private String currentClusterId;
    /**当前的租户Id*/
    private String currentTenantId;
    /**当前的服务名称*/
    private String currentDeployName;
    /**当前的服务类型*/
    private String currentServiceType;
    /**当前的分区名称*/
    private String currentNameSpace;
    /**当前的项目Id*/
    private String currentProjectId;
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
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
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
	public String getCurrentTenantId() {
		return currentTenantId;
	}
	public void setCurrentTenantId(String currentTenantId) {
		this.currentTenantId = currentTenantId;
	}
	public String getCurrentDeployName() {
		return currentDeployName;
	}
	public void setCurrentDeployName(String currentDeployName) {
		this.currentDeployName = currentDeployName;
	}
	public String getCurrentServiceType() {
		return currentServiceType;
	}
	public void setCurrentServiceType(String currentServiceType) {
		this.currentServiceType = currentServiceType;
	}
	public String getCurrentNameSpace() {
		return currentNameSpace;
	}
	public void setCurrentNameSpace(String currentNameSpace) {
		this.currentNameSpace = currentNameSpace;
	}

	public String getCurrentProjectId() {
		return currentProjectId;
	}
	public void setCurrentProjectId(String currentProjectId) {
		this.currentProjectId = currentProjectId;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DeploymentTransferDto [clusterId=");
		builder.append(clusterId);
		builder.append(", projectId=");
		builder.append(projectId);
		builder.append(", namespace=");
		builder.append(namespace);
		builder.append(", tenantId=");
		builder.append(tenantId);
		builder.append(", currentClusterId=");
		builder.append(currentClusterId);
		builder.append(", currentTenantId=");
		builder.append(currentTenantId);
		builder.append(", currentDeployName=");
		builder.append(currentDeployName);
		builder.append(", currentServiceType=");
		builder.append(currentServiceType);
		builder.append(", currentNameSpace=");
		builder.append(currentNameSpace);
		builder.append(", currentProjectId=");
		builder.append(currentProjectId);
		builder.append("]");
		return builder.toString();
	}
	

    
}
