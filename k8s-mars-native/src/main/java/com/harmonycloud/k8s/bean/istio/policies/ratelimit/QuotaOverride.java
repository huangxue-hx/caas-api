package com.harmonycloud.k8s.bean.istio.policies.ratelimit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.Set;

/**
 * update by weg on 18-11-27.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuotaOverride {

    private Map<String, String> dimensions;

    private Integer maxAmount;

    public Map<String, String> getDimensions() {
        return dimensions;
    }

    public void setDimensions(Map<String, String> dimensions) {
        this.dimensions = dimensions;
    }

    public Integer getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Integer maxAmount) {
        this.maxAmount = maxAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        QuotaOverride quotaOverride = (QuotaOverride) o;
        if (this.dimensions != null && this.dimensions.size() > 0) {
            if (quotaOverride.dimensions == null || quotaOverride.dimensions.size() == 0) {
                return false;
            } else if (!checkMap(this.dimensions, quotaOverride.dimensions)) {
                return false;
            }
        } else {
            if (quotaOverride.dimensions != null && quotaOverride.dimensions.size() > 0) {
                return false;
            }
        }
        if (this.maxAmount != null) {
            if (quotaOverride.maxAmount == null) {
                return false;
            } else if (this.maxAmount.intValue() != quotaOverride.maxAmount.intValue()) {
                return false;
            }
        } else {
            if (quotaOverride.maxAmount != null) {
                return false;
            }
        }
        return true;
    }

    private boolean checkMap(Map<String, String> thisMap, Map<String, String> objMap){
        if (thisMap.size() != objMap.size()) {
            return false;
        }
        Set<String> keyStrs = thisMap.keySet();
        for (String key : keyStrs) {
            if (!thisMap.get(key).equals(objMap.get(key))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getDimensions() != null ? getDimensions().hashCode() : 0;
        result = 31 * result + (getMaxAmount() != null ? getMaxAmount().hashCode() : 0);
        return result;
    }
}
