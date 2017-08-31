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

	private Integer currentAverageValue;


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

	public Integer getCurrentAverageValue() {
		return currentAverageValue;
	}

	public void setCurrentAverageValue(Integer currentAverageValue) {
		this.currentAverageValue = currentAverageValue;
	}
}
