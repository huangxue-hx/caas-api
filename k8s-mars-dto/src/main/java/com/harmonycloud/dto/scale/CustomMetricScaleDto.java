package com.harmonycloud.dto.scale;

/**
 * Created by root on 4/10/17.
 */
public class CustomMetricScaleDto {

    private String metricName;
	private String metricApi;
	private Long targetValue;
	private Long currentValue;

	public CustomMetricScaleDto() {
	}

	public CustomMetricScaleDto(String metricName, String metricApi, Long targetValue) {
		this.metricName = metricName;
		this.metricApi = metricApi;
		this.targetValue = targetValue;
	}

	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	public String getMetricApi() {
		return metricApi;
	}

	public void setMetricApi(String metricApi) {
		this.metricApi = metricApi;
	}

	public Long getTargetValue() {
		return targetValue;
	}

	public void setTargetValue(Long targetValue) {
		this.targetValue = targetValue;
	}

	public Long getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(Long currentValue) {
		this.currentValue = currentValue;
	}
}
