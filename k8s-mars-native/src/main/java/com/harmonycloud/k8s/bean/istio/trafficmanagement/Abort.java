package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Create by weg on 18-11-30.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Abort {

    private Integer percent;

    private Integer httpStatus;

    public Integer getPercent() {
        return percent;
    }

    public void setPercent(Integer percent) {
        this.percent = percent;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        Abort abort = (Abort) o;
        if (this.percent != null) {
            if (abort.percent == null) {
                return false;
            } else if (this.percent.intValue() != abort.percent.intValue()) {
                return false;
            }
        } else {
            if (abort.percent != null) {
                return false;
            }
        }
        if (this.httpStatus != null) {
            if (abort.httpStatus == null) {
                return false;
            } else if (this.httpStatus.intValue() != abort.httpStatus.intValue()) {
                return false;
            }
        } else {
            if (abort.httpStatus != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getPercent() != null ? getPercent().hashCode() : 0;
        result = 31 * result + (getHttpStatus() != null ? getHttpStatus().hashCode() : 0);
        return result;
    }
}
