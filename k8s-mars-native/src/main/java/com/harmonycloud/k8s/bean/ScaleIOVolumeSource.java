package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScaleIOVolumeSource {
	
	private String fsType;
	
	private String gateway;
	
	private String protectionDomain;
	
	private boolean readOnly;
	
	private LocalObjectReference secretRef;
	
	private boolean sslEnabled;
	
	private String storageMode;
	
	private String storagePool;
	
	private String system;
	
	private String volumeName;

	public String getFsType() {
		return fsType;
	}

	public void setFsType(String fsType) {
		this.fsType = fsType;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	public String getProtectionDomain() {
		return protectionDomain;
	}

	public void setProtectionDomain(String protectionDomain) {
		this.protectionDomain = protectionDomain;
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

	public boolean isSslEnabled() {
		return sslEnabled;
	}

	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	public String getStorageMode() {
		return storageMode;
	}

	public void setStorageMode(String storageMode) {
		this.storageMode = storageMode;
	}

	public String getStoragePool() {
		return storagePool;
	}

	public void setStoragePool(String storagePool) {
		this.storagePool = storagePool;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public String getVolumeName() {
		return volumeName;
	}

	public void setVolumeName(String volumeName) {
		this.volumeName = volumeName;
	}
	
}
