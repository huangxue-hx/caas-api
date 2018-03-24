package com.harmonycloud.dto.cluster;

import java.io.Serializable;

public class ClusterLoadbalanceDto implements Serializable {
    private Integer lbId;

    private String clusterId;

    private String loadbalanceName;

    private String loadbalanceIp;

    private String loadbalancePort;

    private static final long serialVersionUID = 1L;

    public Integer getLbId() {
        return lbId;
    }

    public void setLbId(Integer lbId) {
        this.lbId = lbId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getLoadbalanceName() {
        return loadbalanceName;
    }

    public void setLoadbalanceName(String loadbalanceName) {
        this.loadbalanceName = loadbalanceName == null ? null : loadbalanceName.trim();
    }

    public String getLoadbalanceIp() {
        return loadbalanceIp;
    }

    public void setLoadbalanceIp(String loadbalanceIp) {
        this.loadbalanceIp = loadbalanceIp == null ? null : loadbalanceIp.trim();
    }

    public String getLoadbalancePort() {
        return loadbalancePort;
    }

    public void setLoadbalancePort(String loadbalancePort) {
        this.loadbalancePort = loadbalancePort == null ? null : loadbalancePort.trim();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        ClusterLoadbalanceDto other = (ClusterLoadbalanceDto) that;
        return (this.getLbId() == null ? other.getLbId() == null : this.getLbId().equals(other.getLbId()))
            && (this.getClusterId() == null ? other.getClusterId() == null : this.getClusterId().equals(other.getClusterId()))
            && (this.getLoadbalanceName() == null ? other.getLoadbalanceName() == null : this.getLoadbalanceName().equals(other.getLoadbalanceName()))
            && (this.getLoadbalanceIp() == null ? other.getLoadbalanceIp() == null : this.getLoadbalanceIp().equals(other.getLoadbalanceIp()))
            && (this.getLoadbalancePort() == null ? other.getLoadbalancePort() == null : this.getLoadbalancePort().equals(other.getLoadbalancePort()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getLbId() == null) ? 0 : getLbId().hashCode());
        result = prime * result + ((getClusterId() == null) ? 0 : getClusterId().hashCode());
        result = prime * result + ((getLoadbalanceName() == null) ? 0 : getLoadbalanceName().hashCode());
        result = prime * result + ((getLoadbalanceIp() == null) ? 0 : getLoadbalanceIp().hashCode());
        result = prime * result + ((getLoadbalancePort() == null) ? 0 : getLoadbalancePort().hashCode());
        return result;
    }
}