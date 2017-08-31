package com.harmonycloud.k8s.bean.scale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomMetricSource {


	private String metricName;

	private String  metricApiUrl;

	private Long targetAverageValue;

	public CustomMetricSource(String metricName, String metricApiUrl, Long targetAverageValue) {
		this.metricName = metricName;
		this.metricApiUrl = metricApiUrl;
		this.targetAverageValue = targetAverageValue;
	}

	public CustomMetricSource() {
	}

	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	public String getMetricApiUrl() {
		return metricApiUrl;
	}

	public void setMetricApiUrl(String metricApiUrl) {
		this.metricApiUrl = metricApiUrl;
	}

	public Long getTargetAverageValue() {
		return targetAverageValue;
	}

	public void setTargetAverageValue(Long targetAverageValue) {
		this.targetAverageValue = targetAverageValue;
	}
}
