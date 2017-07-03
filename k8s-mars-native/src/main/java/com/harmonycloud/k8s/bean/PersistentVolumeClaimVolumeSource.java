package com.harmonycloud.k8s.bean;

public class PersistentVolumeClaimVolumeSource {
	
	private String claimName;
	
	private Boolean readOnly;

	public String getClaimName() {
		return claimName;
	}

	public void setClaimName(String claimName) {
		this.claimName = claimName;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

}
