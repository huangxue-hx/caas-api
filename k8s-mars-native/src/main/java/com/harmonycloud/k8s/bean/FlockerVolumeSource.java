package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlockerVolumeSource {
	
	private String datasetName;
	
	private String datasetUUID;

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public String getDatasetUUID() {
		return datasetUUID;
	}

	public void setDatasetUUID(String datasetUUID) {
		this.datasetUUID = datasetUUID;
	}
	
	
}
