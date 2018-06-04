package com.harmonycloud.k8s.bean;

public class MetricSpec {
    private ObjectMetricSource object;
    private PodsMetricSource pods;
    private ResourceMetricSource resource;
    private String type;

    public ObjectMetricSource getObject() {
        return object;
    }

    public void setObject(ObjectMetricSource object) {
        this.object = object;
    }

    public PodsMetricSource getPods() {
        return pods;
    }

    public void setPods(PodsMetricSource pods) {
        this.pods = pods;
    }

    public ResourceMetricSource getResource() {
        return resource;
    }

    public void setResource(ResourceMetricSource resource) {
        this.resource = resource;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
