package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Create by weg on 18-11-30.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class L4MatchAttributes {

    private List<String> destinationSubnets;

    private Integer port;

    private Map<String, String> sourceLabels;

    private List<String> gateways;

    public List<String> getDestinationSubnets() {
        return destinationSubnets;
    }

    public void setDestinationSubnets(List<String> destinationSubnets) {
        this.destinationSubnets = destinationSubnets;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Map<String, String> getSourceLabels() {
        return sourceLabels;
    }

    public void setSourceLabels(Map<String, String> sourceLabels) {
        this.sourceLabels = sourceLabels;
    }

    public List<String> getGateways() {
        return gateways;
    }

    public void setGateways(List<String> gateways) {
        this.gateways = gateways;
    }
}
