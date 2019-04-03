package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DestinationRuleSpec {

    private String host;

    private TrafficPolicy trafficPolicy;

    private List<Subset> subsets;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public TrafficPolicy getTrafficPolicy() {
        return trafficPolicy;
    }

    public void setTrafficPolicy(TrafficPolicy trafficPolicy) {
        this.trafficPolicy = trafficPolicy;
    }

    public List<Subset> getSubsets() {
        return subsets;
    }

    public void setSubsets(List<Subset> subsets) {
        this.subsets = subsets;
    }
}
