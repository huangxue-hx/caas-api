package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Create by weg on 18-11-30.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Delay {

    private Integer percent;

    private String fixedDelay;

    public Integer getPercent() {
        return percent;
    }

    public void setPercent(Integer percent) {
        this.percent = percent;
    }

    public String getFixedDelay() {
        return fixedDelay;
    }

    public void setFixedDelay(String fixedDelay) {
        this.fixedDelay = fixedDelay;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        Delay delay = (Delay) o;
        if (this.percent != null) {
            if (delay.percent == null) {
                return false;
            } else if (this.percent.intValue() != delay.percent.intValue()) {
                return false;
            }
        } else {
            if (delay.percent != null) {
                return false;
            }
        }
        if (this.fixedDelay != null) {
            if (!this.fixedDelay.equals(delay.fixedDelay)) {
                return false;
            }
        } else {
            if (delay.fixedDelay != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getPercent() != null ? getPercent().hashCode() : 0;
        result = 31 * result + (getFixedDelay() != null ? getFixedDelay().hashCode() : 0);
        return result;
    }
}
