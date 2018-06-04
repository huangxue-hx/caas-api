package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DownwardAPIVolumeSource {

	private Integer defaultMode;
	
	private List<DownwardAPIVolumeFile> items;

	public Integer getDefaultMode() {
		return defaultMode;
	}

	public void setDefaultMode(Integer defaultMode) {
		this.defaultMode = defaultMode;
	}

	public List<DownwardAPIVolumeFile> getItems() {
		return items;
	}

	public void setItems(List<DownwardAPIVolumeFile> items) {
		this.items = items;
	}
	
	
}
