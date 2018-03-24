package com.harmonycloud.dto.scale;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class ResourceMetricScaleDto {
    private String name ;
    @Min(value=1, message="CPU使用率不能小于1%")
    @Max(value=99, message="CPU使用率不能大于99%")
    private Integer targetUsage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTargetUsage() {
        return targetUsage;
    }

    public void setTargetUsage(Integer targetUsage) {
        this.targetUsage = targetUsage;
    }
}
