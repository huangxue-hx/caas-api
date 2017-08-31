package com.harmonycloud.k8s.bean.scale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceMetricSource {

	private String  name;
	
	private Integer targetAverageUtilization;

	public ResourceMetricSource(String name, Integer targetAverageUtilization) {
		this.name = name;
		this.targetAverageUtilization = targetAverageUtilization;
	}

	public ResourceMetricSource() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getTargetAverageUtilization() {
		return targetAverageUtilization;
	}

	public void setTargetAverageUtilization(Integer targetAverageUtilization) {
		this.targetAverageUtilization = targetAverageUtilization;
	}
}
