package com.harmonycloud.k8s.bean.istio.policies.ratelimit;

import java.util.Map;
import java.util.Set;

/**
 * update by weg on 18-11-27.
 */
public class QuotaInstanceSpec {

    private Map<String, String> dimensions;

    public Map<String, String> getDimensions() {
        return dimensions;
    }

    public void setDimensions(Map<String, String> dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        QuotaInstanceSpec spec = (QuotaInstanceSpec) o;
        if (this.dimensions != null && this.dimensions.size() > 0) {
            if (spec.dimensions == null || spec.dimensions.size() == 0) {
                return false;
            } else if (!checkMap(this.dimensions, spec.dimensions)) {
                return false;
            }
        } else {
            if (spec.dimensions != null && spec.dimensions.size() > 0) {
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
        return getDimensions().hashCode();
    }
}
