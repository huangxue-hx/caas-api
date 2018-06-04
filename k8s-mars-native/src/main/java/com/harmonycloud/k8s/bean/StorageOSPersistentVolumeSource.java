package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageOSPersistentVolumeSource {

	private String fsType;
	
	private boolean readOnly;
	
	private ObjectReference secretRef;
	
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

	public ObjectReference getSecretRef() {
		return secretRef;
	}

	public void setSecretRef(ObjectReference secretRef) {
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
