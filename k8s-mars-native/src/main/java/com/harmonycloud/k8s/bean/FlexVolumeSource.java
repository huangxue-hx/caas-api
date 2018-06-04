package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlexVolumeSource {

	private String driver;
	
	private String fsType;
	
	private Object options;
	
	private boolean readOnly;
	
	private LocalObjectReference secretRef;

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getFsType() {
		return fsType;
	}

	public void setFsType(String fsType) {
		this.fsType = fsType;
	}

	public Object getOptions() {
		return options;
	}

	public void setOptions(Object options) {
		this.options = options;
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
	
	
}
