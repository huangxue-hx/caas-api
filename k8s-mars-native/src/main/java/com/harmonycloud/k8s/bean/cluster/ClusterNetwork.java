package com.harmonycloud.k8s.bean.cluster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterNetwork implements Serializable {
    private static final long serialVersionUID = -7655680940691835131L;
    private String version ;
    private String networkFlag;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNetworkFlag() {
        return networkFlag;
    }

    public void setNetworkFlag(String networkFlag) {
        this.networkFlag = networkFlag;
    }
}
