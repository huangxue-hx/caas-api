package com.harmonycloud.service.platform.bean;

/**
 * 
 * @author jmi
 *
 */
public class VolumeMountExt {

	private String name;
	
	private String hostPath;
	
	private String emptyDir;

	private Boolean readOnly;

	private String mountPath;

	private String subPath;
	
	private String type;
	
	private String gitUrl;
	
	private String revision;
	
	private String configMapName;
	
	private String pvcname;
	
	public VolumeMountExt() {
		
	}
	
	public VolumeMountExt(String name, Boolean readOnly, String mountPath, String subPath) {
		this.name = name;
		this.readOnly = readOnly;
		this.mountPath = mountPath;
		this.subPath = subPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public String getMountPath() {
		return mountPath;
	}

	public void setMountPath(String mountPath) {
		this.mountPath = mountPath;
	}

	public String getSubPath() {
		return subPath;
	}

	public void setSubPath(String subPath) {
		this.subPath = subPath;
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

	public String getConfigMapName() {
		return configMapName;
	}

	public void setConfigMapName(String configMapName) {
		this.configMapName = configMapName;
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

	public String getPvcname() {
		return pvcname;
	}

	public void setPvcname(String pvcname) {
		this.pvcname = pvcname;
	}

}
