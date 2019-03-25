package com.harmonycloud.k8s.bean.istio.policies.ratelimit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * update by weg on 18-11-27.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedisQuotaSpec {

    private Integer connectionPoolSize;

    private String redisServerUrl;

    private List<RedisQuotaSpecQuota> quotas;

    public Integer getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public void setConnectionPoolSize(Integer connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
    }

    public String getRedisServerUrl() {
        return redisServerUrl;
    }

    public void setRedisServerUrl(String redisServerUrl) {
        this.redisServerUrl = redisServerUrl;
    }

    public List<RedisQuotaSpecQuota> getQuotas() {
        return quotas;
    }

    public void setQuotas(List<RedisQuotaSpecQuota> quotas) {
        this.quotas = quotas;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        RedisQuotaSpec spec = (RedisQuotaSpec) o;
        if (this.connectionPoolSize != null) {
            if (spec.connectionPoolSize == null) {
                return false;
            } else if (this.connectionPoolSize.intValue() != spec.connectionPoolSize.intValue()) {
                return false;
            }
        } else {
            if (spec.connectionPoolSize != null) {
                return false;
            }
        }
        if (this.redisServerUrl != null) {
            if (!this.redisServerUrl.equals(spec.redisServerUrl)) {
                return false;
            }
        } else {
            if (spec.redisServerUrl != null) {
                return false;
            }
        }
        if (!CollectionUtils.isEmpty(this.quotas)) {
            if (CollectionUtils.isEmpty(spec.quotas)) {
                return false;
            } else if (!checkList(this.quotas, spec.quotas)) {
                return false;
            }
        } else {
            if (!CollectionUtils.isEmpty(spec.quotas)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkList(List<RedisQuotaSpecQuota> thisQuotas, List<RedisQuotaSpecQuota> objQuotas) {
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
        int result = getConnectionPoolSize() != null ? getConnectionPoolSize().hashCode() : 0;
        result = 31 * result + (getRedisServerUrl() != null ? getRedisServerUrl().hashCode() : 0);
        result = 31 * result + (getQuotas() != null ? getQuotas().hashCode() : 0);
        return result;
    }
}
