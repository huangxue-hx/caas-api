package com.harmonycloud.dto.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistentVolumeDto {

	private String name; //本来就存在的volume名称

	private String type; //pv emptyDir hostPath 
	
	private Boolean readOnly;
	
	private String volumeName;//pv name
	
	private String path;
	
	private String gitUrl;
	
	private String revision;
	
	private String emptyDir;
	
	private String hostPath;

	//PVC parameters

    private String pvcName;

    private String capacity;

    private String tenantId;

//    private String pvcReadonly;

    private Boolean bindOne;

	private String projectId;

    private String namespace;

    private String serviceType;

    private String serviceName;

	private String clusterId;

	private String storageClassType;

	private String storageClassName;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVolumeName() {
		return volumeName;
	}

	public void setVolumeName(String volumeName) {
		this.volumeName = volumeName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getGitUrl() {
		return gitUrl;
	}

	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
	}

    public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

    public String getPvcName() {
        return pvcName;
    }

    public void setPvcName(String pvcName) {
        this.pvcName = pvcName;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }


	public String getEmptyDir() {
		return emptyDir;
	}

	public void setEmptyDir(String emptyDir) {
		this.emptyDir = emptyDir;
	}

	public String getHostPath() {
		return hostPath;
	}

	public void setHostPath(String hostPath) {
		this.hostPath = hostPath;
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

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public Boolean getBindOne() {
		return bindOne;
	}

	public void setBindOne(Boolean bindOne) {
		this.bindOne = bindOne;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public String getStorageClassType() {
		return storageClassType;
	}

	public void setStorageClassType(String storageClassType) {
		this.storageClassType = storageClassType;
	}

	public String getStorageClassName() {
		return storageClassName;
	}

	public void setStorageClassName(String storageClassName) {
		this.storageClassName = storageClassName;
	}
}
