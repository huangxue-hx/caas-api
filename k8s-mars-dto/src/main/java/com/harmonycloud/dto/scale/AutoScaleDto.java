package com.harmonycloud.dto.scale;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by root on 4/10/17.
 */
public class AutoScaleDto {

	@NotBlank(message="分区不能为空")
	private String namespace;
	@NotBlank(message="服务名不能为空")
	private String deploymentName;
	@NotNull(message="最小实例数不能为空")
	@Min(value=1, message="最小实例数不能小于1")
	private Integer minPods;
	@NotNull(message="最大实例数不能为空")
	@Min(value=2, message="最大实例数不能小于2")
	private Integer maxPods;
	@Min(value=1, message="CPU使用率不能小于1%")
	@Max(value=99, message="CPU使用率不能大于99%")
	private Integer targetCpuUsage;
	private Integer currentCpuUsage;
	@Min(value=1, message="每秒请求数不能小于1")
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
