package com.harmonycloud.k8s.bean;

public class ObjectMetricStatus {
    private String currentValue;
    private String metricName;
    private CrossVersionObjectReference target;

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public CrossVersionObjectReference getTarget() {
        return target;
    }

    public void setTarget(CrossVersionObjectReference target) {
        this.target = target;
    }
}
