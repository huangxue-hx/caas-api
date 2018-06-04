package com.harmonycloud.k8s.bean.cluster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterExternal implements Serializable {
    private static final long serialVersionUID = 2278089085788335861L;
    private String type;
    private Map<String, Object> labels;
    private String topLb ;
    private String tcpConfig ;
    private String udpConfig ;
    private Integer minPort ;
    private Integer maxPort;

    public Integer getMinPort() {
        return minPort;
    }

    public void setMinPort(Integer minPort) {
        this.minPort = minPort;
    }

    public Integer getMaxPort() {
        return maxPort;
    }

    public void setMaxPort(Integer maxPort) {
        this.maxPort = maxPort;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, Object> labels) {
        this.labels = labels;
    }

    public String getTopLb() {
        return topLb;
    }

    public void setTopLb(String topLb) {
        this.topLb = topLb;
    }

    public String getTcpConfig() {
        return tcpConfig;
    }

    public void setTcpConfig(String tcpConfig) {
        this.tcpConfig = tcpConfig;
    }

    public String getUdpConfig() {
        return udpConfig;
    }

    public void setUdpConfig(String udpConfig) {
        this.udpConfig = udpConfig;
    }
}
