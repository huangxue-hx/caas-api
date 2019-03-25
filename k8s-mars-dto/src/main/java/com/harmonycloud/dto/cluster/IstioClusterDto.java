package com.harmonycloud.dto.cluster;

import java.io.Serializable;

public class IstioClusterDto implements Serializable {
    private static final long serialVersionUID = -3106477616706352502L;

    private  String clusterId;

    private  String clusterName;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
