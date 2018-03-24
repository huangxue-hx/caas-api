package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageOSVolumeSource {

	private String fsType;
	
	private boolean readOnly;
	
	private LocalObjectReference secretRef;
	
	private String volumeName;
	
	private String volumeNamespace;

	public String getFsType() {
		return fsType;
	}

	public void setFsType(String fsType) {
		this.fsType = fsType;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public LocalObjectReference getSecretRef() {
		return secretRef;
	}

	public void setSecretRef(LocalObjectReference secretRef) {
		this.secretRef = secretRef;
	}

	public String getVolumeName() {
		return volumeName;
	}

	public void setVolumeName(String volumeName) {
		this.volumeName = volumeName;
	}

	public String getVolumeNamespace() {
		return volumeNamespace;
	}

	public void setVolumeNamespace(String volumeNamespace) {
		this.volumeNamespace = volumeNamespace;
	}

}
