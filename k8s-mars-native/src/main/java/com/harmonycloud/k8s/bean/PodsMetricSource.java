package com.harmonycloud.k8s.bean;

public class PodsMetricSource {
    private String metricName;
//    private Quantity targetAverageValue;

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }
}
