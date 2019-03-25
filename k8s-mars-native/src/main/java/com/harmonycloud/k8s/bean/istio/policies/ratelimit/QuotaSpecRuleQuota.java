package com.harmonycloud.k8s.bean.istio.policies.ratelimit;

/**
 * update by weg on 18-11-27.
 */
public class QuotaSpecRuleQuota {

    private String charge;

    private String quota;

    public String getCharge() {
        return charge;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }

    public String getQuota() {
        return quota;
    }

    public void setQuota(String quota) {
        this.quota = quota;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        QuotaSpecRuleQuota quota = (QuotaSpecRuleQuota) o;
        if (this.charge != null) {
            if (!this.charge.equals(quota.charge)) {
                return false;
            }
        } else {
            if (quota.charge != null) {
                return false;
            }
        }
        if (this.quota != null) {
            if (!this.quota.equals(quota.quota)) {
                return false;
            }
        } else {
            if (quota.quota != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getCharge() != null ? getCharge().hashCode() : 0;
        result = 31 * result + (getQuota() != null ? getQuota().hashCode() : 0);
        return result;
    }
}
