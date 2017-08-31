package com.harmonycloud.k8s.bean.scale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomMetricStatus {

	private String  metricName;

	private Long currentAverageValue;

	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	public Long getCurrentAverageValue() {
		return currentAverageValue;
	}

	public void setCurrentAverageValue(Long currentAverageValue) {
		this.currentAverageValue = currentAverageValue;
	}
}
