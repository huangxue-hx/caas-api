package com.harmonycloud.dao.cluster.bean;

import java.io.Serializable;

public class IngressControllerPort implements Serializable {
    private Integer id;

    private String name;

    private String clusterId;

    private Integer httpPort;

    private Integer httpsPort;

    private Integer healthPort;

    private Integer statusPort;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
    }

    public Integer getHealthPort() {
        return healthPort;
    }

    public void setHealthPort(Integer healthPort) {
        this.healthPort = healthPort;
    }

    public Integer getStatusPort() {
        return statusPort;
    }

    public void setStatusPort(Integer statusPort) {
        this.statusPort = statusPort;
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
        IngressControllerPort other = (IngressControllerPort) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getClusterId() == null ? other.getClusterId() == null : this.getClusterId().equals(other.getClusterId()))
            && (this.getHttpPort() == null ? other.getHttpPort() == null : this.getHttpPort().equals(other.getHttpPort()))
            && (this.getHttpsPort() == null ? other.getHttpsPort() == null : this.getHttpsPort().equals(other.getHttpsPort()))
            && (this.getHealthPort() == null ? other.getHealthPort() == null : this.getHealthPort().equals(other.getHealthPort()))
            && (this.getStatusPort() == null ? other.getStatusPort() == null : this.getStatusPort().equals(other.getStatusPort()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getClusterId() == null) ? 0 : getClusterId().hashCode());
        result = prime * result + ((getHttpPort() == null) ? 0 : getHttpPort().hashCode());
        result = prime * result + ((getHttpsPort() == null) ? 0 : getHttpsPort().hashCode());
        result = prime * result + ((getHealthPort() == null) ? 0 : getHealthPort().hashCode());
        result = prime * result + ((getStatusPort() == null) ? 0 : getStatusPort().hashCode());
        return result;
    }
}