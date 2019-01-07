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

	/**
	 * 伸缩控制器类型 hpa-k8s的水平自动伸缩控制器，cpa-自定义crd自动伸缩器
	 */
	private String uid;
	private String controllerType;
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
	@Min(value=1, message="内存使用率不能小于1%")
	@Max(value=99, message="内存使用率不能大于99%")
	private Integer targetMemoryUsage;
	private Integer currentMemoryUsage;
	@Min(value=1, message="每秒请求数不能小于1")
	private Long targetTps;
	private Long currentTps;
	private String lastScaleTime;
	private Integer currentReplicas;
    private List<TimeMetricScaleDto> timeMetricScales;
	private List<CustomMetricScaleDto> customMetricScales;
    private String serviceType;

	/**
	 * 伸缩告警收件人，多个用分号分割
	 */
	private String toEmail;
	/**
	 * 伸缩告警抄送人，多个用分号分割
	 */
	private String ccEmail;

	public List<CustomMetricScaleDto> getCustomMetricScales() {
		return customMetricScales;
	}

	public void setCustomMetricScales(List<CustomMetricScaleDto> customMetricScales) {
		this.customMetricScales = customMetricScales;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
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

	public Integer getTargetMemoryUsage() {
		return targetMemoryUsage;
	}

	public void setTargetMemoryUsage(Integer targetMemoryUsage) {
		this.targetMemoryUsage = targetMemoryUsage;
	}

	public Integer getCurrentMemoryUsage() {
		return currentMemoryUsage;
	}

	public void setCurrentMemoryUsage(Integer currentMemoryUsage) {
		this.currentMemoryUsage = currentMemoryUsage;
	}

	public String getControllerType() {
		return controllerType;
	}

	public void setControllerType(String controllerType) {
		this.controllerType = controllerType;
	}

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

	public String getToEmail() {
		return toEmail;
	}

	public void setToEmail(String toEmail) {
		this.toEmail = toEmail;
	}

	public String getCcEmail() {
		return ccEmail;
	}

	public void setCcEmail(String ccEmail) {
		this.ccEmail = ccEmail;
	}

}
