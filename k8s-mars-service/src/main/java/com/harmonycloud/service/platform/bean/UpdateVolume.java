package com.harmonycloud.service.platform.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateVolume {

	private String name;

	private String readOnly;

	private String mountPath;

	private String subPath;
	
	private String emptyDir;
	
	private String hostPath;
	
	private String type;

	private String gitUrl;

	private String revision;

	private String pvcName;

	private String pvcCapacity;

	private String projectId;

	private String pvcBindOne;

	public String getPvcCapacity() {
		return pvcCapacity;
	}

	public void setPvcCapacity(String pvcCapacity) {
		this.pvcCapacity = pvcCapacity;
	}

	public String getPvcBindOne() {
		return pvcBindOne;
	}

	public void setPvcBindOne(String pvcBindOne) {
		this.pvcBindOne = pvcBindOne;
	}

	public String getPvcName() {
		return pvcName;
	}

	public void setPvcName(String pvcName) {
		this.pvcName = pvcName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(String readOnly) {
		this.readOnly = readOnly;
	}

	public String getMountPath() {
		return mountPath;
	}

	public void setMountPath(String mountPath) {
		this.mountPath = mountPath;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

    public String getSubPath() {
        return subPath;
    }

    public void setSubPath(String subPath) {
        this.subPath = subPath;
    }

	public String getHostPath() {
		return hostPath;
	}

	public void setHostPath(String hostPath) {
		this.hostPath = hostPath;
	}

	public String getEmptyDir() {
		return emptyDir;
	}

	public void setEmptyDir(String emptyDir) {
		this.emptyDir = emptyDir;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
}
