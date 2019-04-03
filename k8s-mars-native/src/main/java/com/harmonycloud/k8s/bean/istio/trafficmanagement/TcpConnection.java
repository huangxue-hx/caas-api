package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TcpConnection {

    private Integer maxConnections;

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        TcpConnection tcpConnection = (TcpConnection) o;
        if (this.maxConnections != null) {
            if (tcpConnection.maxConnections == null) {
                return false;
            } else if (this.maxConnections.intValue() != tcpConnection.maxConnections.intValue()) {
                return false;
            }
        } else {
            if (tcpConnection.maxConnections != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return maxConnections.hashCode();
    }
}
