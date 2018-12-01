package com.harmonycloud.k8s.bean.istio.policies.ratelimit;

import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * update by weg on 18-11-27.
 */
public class QuotaSpecSpec {

    private List<QuotaSpecRule> rules;

    public List<QuotaSpecRule> getRules() {
        return rules;
    }

    public void setRules(List<QuotaSpecRule> rules) {
        this.rules = rules;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        QuotaSpecSpec spec = (QuotaSpecSpec) o;
        if (!CollectionUtils.isEmpty(this.rules)) {
            if (CollectionUtils.isEmpty(spec.rules)) {
                return false;
            } else if (!checkList(this.rules, spec.rules)) {
                return false;
            }
        } else {
            if (!CollectionUtils.isEmpty(spec.rules)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkList(List<QuotaSpecRule> thisRules, List<QuotaSpecRule> objRules) {
        if (thisRules.size() != objRules.size()) {
            return false;
        }
        for (int i = 0; i < thisRules.size(); i++) {
            if (!thisRules.get(i).equals(objRules.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getRules() != null ? getRules().hashCode() : 0;
    }
}
