package com.harmonycloud.k8s.bean;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
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
