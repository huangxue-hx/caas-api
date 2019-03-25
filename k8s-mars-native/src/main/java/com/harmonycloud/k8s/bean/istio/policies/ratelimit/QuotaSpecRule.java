package com.harmonycloud.k8s.bean.istio.policies.ratelimit;

import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * update by weg on 18-11-27.
 */
public class QuotaSpecRule {

    private List<QuotaSpecRuleQuota> quotas;

    public List<QuotaSpecRuleQuota> getQuotas() {
        return quotas;
    }

    public void setQuotas(List<QuotaSpecRuleQuota> quotas) {
        this.quotas = quotas;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        QuotaSpecRule rule = (QuotaSpecRule) o;
        if (!CollectionUtils.isEmpty(this.quotas)) {
            if (CollectionUtils.isEmpty(rule.quotas)) {
                return false;
            } else if (!checkList(this.quotas, rule.quotas)) {
                return false;
            }
        } else {
            if (!CollectionUtils.isEmpty(rule.quotas)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkList(List<QuotaSpecRuleQuota> thisQuotas, List<QuotaSpecRuleQuota> objQuotas) {
        if (thisQuotas.size() != objQuotas.size()) {
            return false;
        }
        for (int i = 0; i < thisQuotas.size(); i++) {
            if (!thisQuotas.get(i).equals(objQuotas.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getQuotas() != null ? getQuotas().hashCode() : 0;
    }
}
