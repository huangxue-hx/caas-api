package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Create by weg on 18-11-30.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Destination {

    private String host;

    private String subset;

    private PortSelector port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSubset() {
        return subset;
    }

    public void setSubset(String subset) {
        this.subset = subset;
    }

    public PortSelector getPort() {
        return port;
    }

    public void setPort(PortSelector port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        Destination destination = (Destination) o;
        if (this.host != null) {
            if (!this.host.equals(destination.host)) {
                return false;
            }
        } else {
            if (destination.host != null) {
                return false;
            }
        }
        if (this.subset != null) {
            if (!this.subset.equals(destination.subset)) {
                return false;
            }
        } else {
            if (destination.subset != null) {
                return false;
            }
        }
        if (this.port != null) {
            if (!this.port.equals(destination.port)) {
                return false;
            }
        } else {
            if (destination.port != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getHost() != null ? getHost().hashCode() : 0;
        result = 31 * result + (getSubset() != null ? getSubset().hashCode() : 0);
        result = 31 * result + (getPort() != null ? getPort().hashCode() : 0);
        return result;
    }
}
