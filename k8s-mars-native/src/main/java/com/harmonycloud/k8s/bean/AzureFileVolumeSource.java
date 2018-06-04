package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AzureFileVolumeSource {

	private boolean readOnly;
	
	private String secretName;
	
	private String shareName;

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public String getSecretName() {
		return secretName;
	}

	public void setSecretName(String secretName) {
		this.secretName = secretName;
	}

	public String getShareName() {
		return shareName;
	}

	public void setShareName(String shareName) {
		this.shareName = shareName;
	}
	
	
}
