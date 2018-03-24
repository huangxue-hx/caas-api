package com.harmonycloud.k8s.bean;

public class ResourceMetricSource {
    private String name;
    private Integer targetAverageUtilization;
//    private Quantity targetAverageValue;


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
