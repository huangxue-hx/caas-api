package com.harmonycloud.k8s.bean.istio.policies.ratelimit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.bean.BaseResource;

/**
 * update by weg on 18-11-27.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedisQuota extends BaseResource {

    private RedisQuotaSpec spec;

    public RedisQuotaSpec getSpec() {
        return spec;
    }

    public void setSpec(RedisQuotaSpec spec) {
        this.spec = spec;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        RedisQuota redisQuota = (RedisQuota) o;
        if (this.spec != null) {
            if (!this.spec.equals(redisQuota.spec)) {
                return false;
            }
        } else {
            if (redisQuota.spec != null) {
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
