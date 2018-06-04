package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CephFSPersistentVolumeSource {

	private List<String> monitors;
	
	private String path;
	
	private boolean readOnly;
	
	private String secretFile;
	
	private SecretReference secretRef;
	
	private String user;

	public List<String> getMonitors() {
		return monitors;
	}

	public void setMonitors(List<String> monitors) {
		this.monitors = monitors;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public String getSecretFile() {
		return secretFile;
	}

	public void setSecretFile(String secretFile) {
		this.secretFile = secretFile;
	}

	public SecretReference getSecretRef() {
		return secretRef;
	}

	public void setSecretRef(SecretReference secretRef) {
		this.secretRef = secretRef;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
}
