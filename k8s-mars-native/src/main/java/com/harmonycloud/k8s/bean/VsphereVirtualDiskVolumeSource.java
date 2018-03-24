package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VsphereVirtualDiskVolumeSource {

	private String fsType;
	
	private String storagePolicyID;
	
	private String storagePolicyName;
	
	private String volumePath;

	public String getFsType() {
		return fsType;
	}

	public void setFsType(String fsType) {
		this.fsType = fsType;
	}

	public String getStoragePolicyID() {
		return storagePolicyID;
	}

	public void setStoragePolicyID(String storagePolicyID) {
		this.storagePolicyID = storagePolicyID;
	}

	public String getStoragePolicyName() {
		return storagePolicyName;
	}

	public void setStoragePolicyName(String storagePolicyName) {
		this.storagePolicyName = storagePolicyName;
	}

	public String getVolumePath() {
		return volumePath;
	}

	public void setVolumePath(String volumePath) {
		this.volumePath = volumePath;
	}
	
	
}
