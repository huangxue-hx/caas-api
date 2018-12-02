package com.harmonycloud.k8s.bean.istio.trafficmanagement;

public class OutlierDetection {

    private Integer consecutiveErrors;
    private String interval;
    private String baseEjectionTime;

    public Integer getConsecutiveErrors() {
        return consecutiveErrors;
    }

    public void setConsecutiveErrors(Integer consecutiveErrors) {
        this.consecutiveErrors = consecutiveErrors;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getBaseEjectionTime() {
        return baseEjectionTime;
    }

    public void setBaseEjectionTime(String baseEjectionTime) {
        this.baseEjectionTime = baseEjectionTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        OutlierDetection outlierDetection = (OutlierDetection) o;
        if (this.consecutiveErrors != null) {
            if (outlierDetection.consecutiveErrors == null) {
                return false;
            } else if (this.consecutiveErrors.intValue() != outlierDetection.consecutiveErrors.intValue()) {
                return false;
            }
        } else {
            if (outlierDetection.consecutiveErrors != null) {
                return false;
            }
        }
        if (this.interval != null) {
            if (!this.interval.equals(outlierDetection.interval)) {
                return false;
            }
        } else {
            if (outlierDetection.interval != null) {
                return false;
            }
        }
        if (this.baseEjectionTime != null) {
            if (!this.baseEjectionTime.equals(outlierDetection.baseEjectionTime)) {
                return false;
            }
        } else {
            if (outlierDetection.baseEjectionTime != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = consecutiveErrors.hashCode();
        result = 31 * result + interval.hashCode();
        result = 31 * result + baseEjectionTime.hashCode();
        return result;
    }
}
