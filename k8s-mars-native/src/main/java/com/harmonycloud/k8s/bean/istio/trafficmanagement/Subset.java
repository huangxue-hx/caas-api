package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subset {

    private String name;

    private Map<String, String> labels;

    private TrafficPolicy trafficPolicy;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public TrafficPolicy getTrafficPolicy() {
        return trafficPolicy;
    }

    public void setTrafficPolicy(TrafficPolicy trafficPolicy) {
        this.trafficPolicy = trafficPolicy;
    }
}
