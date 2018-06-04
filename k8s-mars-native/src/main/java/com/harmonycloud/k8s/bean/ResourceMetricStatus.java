package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceMetricStatus {
    private Integer currentAverageUtilization;
    private String currentAverageValue;
    private String name;

    public Integer getCurrentAverageUtilization() {
        return currentAverageUtilization;
    }

    public void setCurrentAverageUtilization(Integer currentAverageUtilization) {
        this.currentAverageUtilization = currentAverageUtilization;
    }

    public String getCurrentAverageValue() {
        return currentAverageValue;
    }

    public void setCurrentAverageValue(String currentAverageValue) {
        this.currentAverageValue = currentAverageValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
