package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FCVolumeSource {

	private String fsType;
	
	private Integer lun;
	
	private boolean readOnly;
	
	private List<String> targetWWNs;
	
	private List<String> wwids;

	public String getFsType() {
		return fsType;
	}

	public void setFsType(String fsType) {
		this.fsType = fsType;
	}

	public Integer getLun() {
		return lun;
	}

	public void setLun(Integer lun) {
		this.lun = lun;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public List<String> getTargetWWNs() {
		return targetWWNs;
	}

	public void setTargetWWNs(List<String> targetWWNs) {
		this.targetWWNs = targetWWNs;
	}

	public List<String> getWwids() {
		return wwids;
	}

	public void setWwids(List<String> wwids) {
		this.wwids = wwids;
	}
	
	
}
