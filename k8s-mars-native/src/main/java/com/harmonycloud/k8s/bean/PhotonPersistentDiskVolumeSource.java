package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhotonPersistentDiskVolumeSource {

	private String fsType;
	
	private String pdID;

	public String getFsType() {
		return fsType;
	}

	public void setFsType(String fsType) {
		this.fsType = fsType;
	}

	public String getPdID() {
		return pdID;
	}

	public void setPdID(String pdID) {
		this.pdID = pdID;
	}
	
}
