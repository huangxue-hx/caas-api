package com.harmonycloud.k8s.bean.istio.policies.ratelimit;

import com.harmonycloud.k8s.bean.BaseResource;

/**
 * update by weg on 18-11-27.
 */
public class QuotaSpec extends BaseResource {

    private QuotaSpecSpec spec;

    public QuotaSpecSpec getSpec() {
        return spec;
    }

    public void setSpec(QuotaSpecSpec spec) {
        this.spec = spec;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        QuotaSpec quotaSpec = (QuotaSpec) o;
        if (this.spec != null) {
            if (!this.spec.equals(quotaSpec.spec)) {
                return false;
            }
        } else {
            if (quotaSpec.spec != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getSpec() != null ? getSpec().hashCode() : 0;
    }
}
