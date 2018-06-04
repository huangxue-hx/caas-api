package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HorizontalPodAutoscalerMetricStatus {
    private ObjectMetricStatus object;
    private PodsMetricStatus pods;
    private ResourceMetricStatus resource;
    private String type;

    public ObjectMetricStatus getObject() {
        return object;
    }

    public void setObject(ObjectMetricStatus object) {
        this.object = object;
    }

    public PodsMetricStatus getPods() {
        return pods;
    }

    public void setPods(PodsMetricStatus pods) {
        this.pods = pods;
    }

    public ResourceMetricStatus getResource() {
        return resource;
    }

    public void setResource(ResourceMetricStatus resource) {
        this.resource = resource;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
