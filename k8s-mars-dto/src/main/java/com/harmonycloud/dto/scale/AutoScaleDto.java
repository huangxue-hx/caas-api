package com.harmonycloud.dto.scale;

import java.util.List;

/**
 * Created by root on 4/10/17.
 */
public class AutoScaleDto {

    private String namespace;
	private String deploymentName;
	private Integer minPods;
	private Integer maxPods;
	private Integer targetCpuUsage;
	private Integer currentCpuUsage;
	private Long targetTps;
	private Long currentTps;
	private String lastScaleTime;
	private Integer currentReplicas;
    List<TimeMetricScaleDto> timeMetricScales;
	List<CustomMetricScaleDto> customMetricScales;

	public List<CustomMetricScaleDto> getCustomMetricScales() {
		return customMetricScales;
	}

	public void setCustomMetricScales(List<CustomMetricScaleDto> customMetricScales) {
		this.customMetricScales = customMetricScales;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getDeploymentName() {
		return deploymentName;
	}

	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}

	public Integer getMinPods() {
		return minPods;
	}

	public void setMinPods(Integer minPods) {
		this.minPods = minPods;
	}

	public Integer getMaxPods() {
		return maxPods;
	}

	public void setMaxPods(Integer maxPods) {
		this.maxPods = maxPods;
	}

	public Integer getTargetCpuUsage() {
		return targetCpuUsage;
	}

	public void setTargetCpuUsage(Integer targetCpuUsage) {
		this.targetCpuUsage = targetCpuUsage;
	}

	public Long getTargetTps() {
		return targetTps;
	}

	public void setTargetTps(Long targetTps) {
		this.targetTps = targetTps;
	}

	public List<TimeMetricScaleDto> getTimeMetricScales() {
		return timeMetricScales;
	}

	public void setTimeMetricScales(List<TimeMetricScaleDto> timeMetricScales) {
		this.timeMetricScales = timeMetricScales;
	}

	public String getLastScaleTime() {
		return lastScaleTime;
	}

	public void setLastScaleTime(String lastScaleTime) {
		this.lastScaleTime = lastScaleTime;
	}

	public Integer getCurrentReplicas() {
		return currentReplicas;
	}

	public void setCurrentReplicas(Integer currentReplicas) {
		this.currentReplicas = currentReplicas;
	}

	public Integer getCurrentCpuUsage() {
		return currentCpuUsage;
	}

	public void setCurrentCpuUsage(Integer currentCpuUsage) {
		this.currentCpuUsage = currentCpuUsage;
	}

	public Long getCurrentTps() {
		return currentTps;
	}

	public void setCurrentTps(Long currentTps) {
		this.currentTps = currentTps;
	}
}
