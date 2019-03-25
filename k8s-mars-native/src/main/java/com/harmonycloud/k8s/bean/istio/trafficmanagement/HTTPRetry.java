package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Create by weg on 18-11-30.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HTTPRetry {

    private Integer attempts;

    private String perTryTimeout;

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public String getPerTryTimeout() {
        return perTryTimeout;
    }

    public void setPerTryTimeout(String perTryTimeout) {
        this.perTryTimeout = perTryTimeout;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        HTTPRetry httpRetry = (HTTPRetry) o;
        if (this.attempts != null) {
            if (httpRetry.attempts == null) {
                return false;
            } else if (this.attempts.intValue() != httpRetry.attempts.intValue()) {
                return false;
            }
        } else {
            if (httpRetry.attempts != null) {
                return false;
            }
        }
        if (this.perTryTimeout != null) {
            if (!this.perTryTimeout.equals(httpRetry.perTryTimeout)) {
                return false;
            }
        } else {
            if (httpRetry.perTryTimeout != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getAttempts() != null ? getAttempts().hashCode() : 0;
        result = 31 * result + (getPerTryTimeout() != null ? getPerTryTimeout().hashCode() : 0);
        return result;
    }
}
