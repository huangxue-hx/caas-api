package com.harmonycloud.k8s.bean.cluster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterDomain implements Serializable{
    private static final long serialVersionUID = 8564671550190648646L;
    private List<ClusterDomainAddress> address;
    private List<ClusterDomainPort> port;

    public List<ClusterDomainAddress> getAddress() {
        return address;
    }

    public void setAddress(List<ClusterDomainAddress> address) {
        this.address = address;
    }

    public List<ClusterDomainPort> getPort() {
        return port;
    }

    public void setPort(List<ClusterDomainPort> port) {
        this.port = port;
    }
}
