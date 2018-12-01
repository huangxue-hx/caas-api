package com.harmonycloud.k8s.bean.istio.policies.ratelimit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.bean.BaseResource;

/**
 * update by weg on 18-11-27.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuotaSpecBinding extends BaseResource {

    private QuotaSpecBindingSpec spec;

    public QuotaSpecBindingSpec getSpec() {
        return spec;
    }

    public void setSpec(QuotaSpecBindingSpec spec) {
        this.spec = spec;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        QuotaSpecBinding quotaSpecBinding = (QuotaSpecBinding) o;
        if (this.spec != null) {
            if (!this.spec.equals(quotaSpecBinding.spec)) {
                return false;
            }
        } else {
            if (quotaSpecBinding.spec != null) {
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
