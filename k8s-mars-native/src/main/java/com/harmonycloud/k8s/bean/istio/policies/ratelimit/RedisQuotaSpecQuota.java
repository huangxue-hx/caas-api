package com.harmonycloud.k8s.bean.istio.policies.ratelimit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.OutlierDetection;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * update by weg on 18-11-27.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedisQuotaSpecQuota {

    private String name;

    private Integer maxAmount;

    private String rateLimitAlgorithm;

    private List<QuotaOverride> overrides;

    private String validDuration;

    private String bucketDuration;

    public String getRateLimitAlgorithm() {
        return rateLimitAlgorithm;
    }

    public void setRateLimitAlgorithm(String rateLimitAlgorithm) {
        this.rateLimitAlgorithm = rateLimitAlgorithm;
    }

    public Integer getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Integer maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValidDuration() {
        return validDuration;
    }

    public void setValidDuration(String validDuration) {
        this.validDuration = validDuration;
    }

    public String getBucketDuration() {
        return bucketDuration;
    }

    public void setBucketDuration(String bucketDuration) {
        this.bucketDuration = bucketDuration;
    }

    public List<QuotaOverride> getOverrides() {
        return overrides;
    }

    public void setOverrides(List<QuotaOverride> overrides) {
        this.overrides = overrides;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        RedisQuotaSpecQuota redisQuotaSpecQuota = (RedisQuotaSpecQuota) o;
        if (this.name != null) {
            if (!this.name.equals(redisQuotaSpecQuota.name)) {
                return false;
            }
        } else {
            if (redisQuotaSpecQuota.name != null) {
                return false;
            }
        }
        if (this.maxAmount != null) {
            if (redisQuotaSpecQuota.maxAmount == null) {
                return false;
            } else if (this.maxAmount.intValue() != redisQuotaSpecQuota.maxAmount.intValue()) {
                return false;
            }
        } else {
            if (redisQuotaSpecQuota.maxAmount != null) {
                return false;
            }
        }
        if (this.rateLimitAlgorithm != null) {
            if (!this.rateLimitAlgorithm.equals(redisQuotaSpecQuota.rateLimitAlgorithm)) {
                return false;
            }
        } else {
            if (redisQuotaSpecQuota.rateLimitAlgorithm != null) {
                return false;
            }
        }
        if (!CollectionUtils.isEmpty(this.overrides)) {
            if (CollectionUtils.isEmpty(redisQuotaSpecQuota.overrides)) {
                return false;
            } else if (!checkList(this.overrides, redisQuotaSpecQuota.overrides)) {
                return false;
            }
        } else {
            if (!CollectionUtils.isEmpty(redisQuotaSpecQuota.overrides)) {
                return false;
            }
        }
        if (this.validDuration != null) {
            if (!this.validDuration.equals(redisQuotaSpecQuota.validDuration)) {
                return false;
            }
        } else {
            if (redisQuotaSpecQuota.validDuration != null) {
                return false;
            }
        }
        if (this.bucketDuration != null) {
            if (!this.bucketDuration.equals(redisQuotaSpecQuota.bucketDuration)) {
                return false;
            }
        } else {
            if (redisQuotaSpecQuota.bucketDuration != null) {
                return false;
            }
        }
        return true;
    }

    private boolean checkList(List<QuotaOverride> thisOverrides, List<QuotaOverride> objOverrides) {
        if (thisOverrides.size() != objOverrides.size()) {
            return false;
        }
        for (int i = 0; i < thisOverrides.size(); i++) {
            if (!thisOverrides.get(i).equals(objOverrides.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getMaxAmount() != null ? getMaxAmount().hashCode() : 0);
        result = 31 * result + (getRateLimitAlgorithm() != null ? getRateLimitAlgorithm().hashCode() : 0);
        result = 31 * result + (getOverrides() != null ? getOverrides().hashCode() : 0);
        result = 31 * result + (getValidDuration() != null ? getValidDuration().hashCode() : 0);
        result = 31 * result + (getBucketDuration() != null ? getBucketDuration().hashCode() : 0);
        return result;
    }
}
