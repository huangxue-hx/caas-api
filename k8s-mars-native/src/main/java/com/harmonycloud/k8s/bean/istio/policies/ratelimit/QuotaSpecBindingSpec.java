package com.harmonycloud.k8s.bean.istio.policies.ratelimit;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * update by weg on 18-11-27.
 */
public class QuotaSpecBindingSpec {

    private List<Map<String, String>> quotaSpecs;

    private List<Map<String, String>> services;

    public List<Map<String, String>> getQuotaSpecs() {
        return quotaSpecs;
    }

    public void setQuotaSpecs(List<Map<String, String>> quotaSpecs) {
        this.quotaSpecs = quotaSpecs;
    }

    public List<Map<String, String>> getServices() {
        return services;
    }

    public void setServices(List<Map<String, String>> services) {
        this.services = services;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        QuotaSpecBindingSpec quotaSpecBindingSpec = (QuotaSpecBindingSpec) o;
        if (!CollectionUtils.isEmpty(this.quotaSpecs)) {
            if (CollectionUtils.isEmpty(quotaSpecBindingSpec.quotaSpecs)) {
                return false;
            } else if (!checkList(this.quotaSpecs, quotaSpecBindingSpec.quotaSpecs)) {
                return false;
            }
        } else {
            if (!CollectionUtils.isEmpty(quotaSpecBindingSpec.quotaSpecs)) {
                return false;
            }
        }
        if (!CollectionUtils.isEmpty(this.services)) {
            if (CollectionUtils.isEmpty(quotaSpecBindingSpec.services)) {
                return false;
            } else if (!checkList(this.services, quotaSpecBindingSpec.services)) {
                return false;
            }
        } else {
            if (!CollectionUtils.isEmpty(quotaSpecBindingSpec.services)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkList(List<Map<String, String>> thisObj, List<Map<String, String>> obj) {
        if (thisObj.size() != obj.size()) {
            return false;
        }
        for (int i = 0; i < thisObj.size(); i++) {
            if (!checkMap(thisObj.get(i), obj.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean checkMap(Map<String, String> thisObj, Map<String, String> obj){
        if (thisObj.size() != obj.size()) {
            return false;
        }
        Set<String> keyStrs = thisObj.keySet();
        for (String key : keyStrs) {
            if (!thisObj.get(key).equals(obj.get(key))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getQuotaSpecs() != null ? getQuotaSpecs().hashCode() : 0;
        result = 31 * result + (getServices() != null ? getServices().hashCode() : 0);
        return result;
    }
}
