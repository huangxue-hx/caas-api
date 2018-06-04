package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VolumeDevice {

	private String devicePath;
	
	private String name;

	public String getDevicePath() {
		return devicePath;
	}

	public void setDevicePath(String devicePath) {
		this.devicePath = devicePath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
