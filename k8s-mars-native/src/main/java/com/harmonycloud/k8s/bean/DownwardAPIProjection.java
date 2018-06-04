package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DownwardAPIProjection {

	private List<DownwardAPIVolumeFile> items;

	public List<DownwardAPIVolumeFile> getItems() {
		return items;
	}

	public void setItems(List<DownwardAPIVolumeFile> items) {
		this.items = items;
	}
	
	
}
