package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AzureDiskVolumeSource {
	
	private String cachingMode;
	
	private String diskName;
	
	private String diskURI;
	
	private String fsType;
	
	private String kind;
	
	private boolean readOnly;

	public String getCachingMode() {
		return cachingMode;
	}

	public void setCachingMode(String cachingMode) {
		this.cachingMode = cachingMode;
	}

	public String getDiskName() {
		return diskName;
	}

	public void setDiskName(String diskName) {
		this.diskName = diskName;
	}

	public String getDiskURI() {
		return diskURI;
	}

	public void setDiskURI(String diskURI) {
		this.diskURI = diskURI;
	}

	public String getFsType() {
		return fsType;
	}

	public void setFsType(String fsType) {
		this.fsType = fsType;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}	
	
}
