package com.harmonycloud.k8s.bean.istio.policies.ratelimit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.bean.BaseResource;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.TrafficPolicy;

/**
 * update by weg on 18-11-27.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuotaInstance extends BaseResource {

    private QuotaInstanceSpec spec;

    public QuotaInstanceSpec getSpec() {
        return spec;
    }

    public void setSpec(QuotaInstanceSpec spec) {
        this.spec = spec;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        QuotaInstance quotaInstance = (QuotaInstance) o;
        if (this.spec != null) {
            if (!this.spec.equals(quotaInstance.spec)) {
                return false;
            }
        } else {
            if (quotaInstance.spec != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return spec != null ? spec.hashCode() : 0;
    }
}
