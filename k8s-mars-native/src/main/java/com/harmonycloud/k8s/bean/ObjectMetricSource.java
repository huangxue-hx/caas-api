package com.harmonycloud.k8s.bean;

public class ObjectMetricSource {
    private String metricName;
    private CrossVersionObjectReference target;
//    private Quantity targetValue;


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
