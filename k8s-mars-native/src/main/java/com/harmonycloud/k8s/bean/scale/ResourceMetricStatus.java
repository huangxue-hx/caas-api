package com.harmonycloud.k8s.bean.scale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceMetricStatus {

	private String  name;
	
	private Integer currentAverageUtilization;

	private String currentAverageValue;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCurrentAverageUtilization() {
		return currentAverageUtilization;
	}

	public void setCurrentAverageUtilization(Integer currentAverageUtilization) {
		this.currentAverageUtilization = currentAverageUtilization;
	}

	public String getCurrentAverageValue() {
		return currentAverageValue;
	}

	public void setCurrentAverageValue(String currentAverageValue) {
		this.currentAverageValue = currentAverageValue;
	}
}
