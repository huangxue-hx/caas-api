package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RBDVolumeSource {

	private String fsType;
	
	private String image;
	
	private String keyring;
	
	private List<String> monitors;
	
	private String pool;
	
	private boolean readOnly;
	
	private LocalObjectReference secretRef;
	
	private String user;

	public String getFsType() {
		return fsType;
	}

	public void setFsType(String fsType) {
		this.fsType = fsType;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getKeyring() {
		return keyring;
	}

	public void setKeyring(String keyring) {
		this.keyring = keyring;
	}

	public List<String> getMonitors() {
		return monitors;
	}

	public void setMonitors(List<String> monitors) {
		this.monitors = monitors;
	}

	public String getPool() {
		return pool;
	}

	public void setPool(String pool) {
		this.pool = pool;
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

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}	
	
}
