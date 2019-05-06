package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrafficPolicy {

    private ConnectionPool connectionPool;

    private OutlierDetection outlierDetection;

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public OutlierDetection getOutlierDetection() {
        return outlierDetection;
    }

    public void setOutlierDetection(OutlierDetection outlierDetection) {
        this.outlierDetection = outlierDetection;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        TrafficPolicy trafficPolicy = (TrafficPolicy) o;
        if (this.connectionPool != null) {
            if (!this.connectionPool.equals(trafficPolicy.connectionPool)) {
                return false;
            }
        } else {
            if (trafficPolicy.connectionPool != null) {
                return false;
            }
        }
        if (this.outlierDetection != null) {
            if (!this.outlierDetection.equals(trafficPolicy.outlierDetection)) {
                return false;
            }
        }else {
            if (trafficPolicy.outlierDetection != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = connectionPool.hashCode();
        result = 31 * result + outlierDetection.hashCode();
        return result;
    }
}
