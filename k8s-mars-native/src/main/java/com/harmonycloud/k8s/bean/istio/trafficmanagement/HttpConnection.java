package com.harmonycloud.k8s.bean.istio.trafficmanagement;

public class HttpConnection {

    private Integer http1MaxPendingRequests;

    private Integer http2MaxRequests;

    private Integer maxRequestsPerConnection;

    public Integer getHttp1MaxPendingRequests() {
        return http1MaxPendingRequests;
    }

    public void setHttp1MaxPendingRequests(Integer http1MaxPendingRequests) {
        this.http1MaxPendingRequests = http1MaxPendingRequests;
    }

    public Integer getHttp2MaxRequests() {
        return http2MaxRequests;
    }

    public void setHttp2MaxRequests(Integer http2MaxRequests) {
        this.http2MaxRequests = http2MaxRequests;
    }

    public Integer getMaxRequestsPerConnection() {
        return maxRequestsPerConnection;
    }

    public void setMaxRequestsPerConnection(Integer maxRequestsPerConnection) {
        this.maxRequestsPerConnection = maxRequestsPerConnection;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        HttpConnection httpConnection = (HttpConnection) o;
        if (this.http1MaxPendingRequests != null) {
            if (httpConnection.http1MaxPendingRequests == null) {
                return false;
            } else if (this.http1MaxPendingRequests.intValue() != httpConnection.http1MaxPendingRequests.intValue()) {
                return false;
            }
        } else {
            if (httpConnection.http1MaxPendingRequests != null) {
                return false;
            }
        }
        if (this.http2MaxRequests != null) {
            if (httpConnection.http2MaxRequests == null) {
                return false;
            } else if (this.http2MaxRequests.intValue() != httpConnection.http2MaxRequests.intValue()) {
                return false;
            }
        } else {
            if (httpConnection.http2MaxRequests != null) {
                return false;
            }
        }
        if (this.maxRequestsPerConnection != null) {
            if (httpConnection.maxRequestsPerConnection == null) {
                return false;
            } else if (this.maxRequestsPerConnection.intValue() != httpConnection.maxRequestsPerConnection.intValue()) {
                return false;
            }
        } else {
            if (httpConnection.maxRequestsPerConnection != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = http1MaxPendingRequests.hashCode();
        result = 31 * result + http2MaxRequests.hashCode();
        result = 31 * result + maxRequestsPerConnection.hashCode();
        return result;
    }
}
