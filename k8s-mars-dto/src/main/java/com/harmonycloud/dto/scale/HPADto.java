package com.harmonycloud.dto.scale;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

public class HPADto {
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
    List<ResourceMetricScaleDto> resource;


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

    public List<ResourceMetricScaleDto> getResource() {
        return resource;
    }

    public void setResource(List<ResourceMetricScaleDto> resource) {
        this.resource = resource;
    }
}
