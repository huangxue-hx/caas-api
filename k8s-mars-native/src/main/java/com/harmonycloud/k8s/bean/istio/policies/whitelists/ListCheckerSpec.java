package com.harmonycloud.k8s.bean.istio.policies.whitelists;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * update by weg on 18-11-27.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListCheckerSpec {

    private String providerUrl;

    private String refreshInterval;

    private String ttl;

    private String cachingInterval;

    private Integer cachingUseCount;

    private List<String> overrides;

    /*
      EntryType:
      1.STRINGS: 列表条目被视为纯字符串
      2.CASE_INSENSITIVE_STRINGS: 列表条目被视为不区分大小写的字符串
      3.IP_ADDRESSES: 列表条目被视为IP地址和范围
      4.REGEX: 列表条目被视为re2 regexp
     */
    private String entryType;

    private Boolean blacklist;

    public String getProviderUrl() {
        return providerUrl;
    }

    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    public String getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(String refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

    public String getCachingInterval() {
        return cachingInterval;
    }

    public void setCachingInterval(String cachingInterval) {
        this.cachingInterval = cachingInterval;
    }

    public Integer getCachingUseCount() {
        return cachingUseCount;
    }

    public void setCachingUseCount(Integer cachingUseCount) {
        this.cachingUseCount = cachingUseCount;
    }

    public List<String> getOverrides() {
        return overrides;
    }

    public void setOverrides(List<String> overrides) {
        this.overrides = overrides;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public Boolean getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(Boolean blacklist) {
        this.blacklist = blacklist;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        ListCheckerSpec spec = (ListCheckerSpec) o;
        if (this.providerUrl != null) {
            if (!this.providerUrl.equals(spec.providerUrl)) {
                return false;
            }
        } else {
            if (spec.providerUrl != null) {
                return false;
            }
        }
        if (this.refreshInterval != null) {
            if (!this.refreshInterval.equals(spec.refreshInterval)) {
                return false;
            }
        } else {
            if (spec.refreshInterval != null) {
                return false;
            }
        }
        if (this.ttl != null) {
            if (!this.ttl.equals(spec.ttl)) {
                return false;
            }
        } else {
            if (spec.ttl != null) {
                return false;
            }
        }
        if (this.cachingInterval != null) {
            if (!this.cachingInterval.equals(spec.cachingInterval)) {
                return false;
            }
        } else {
            if (spec.cachingInterval != null) {
                return false;
            }
        }
        if (this.cachingUseCount != null) {
            if (spec.cachingUseCount == null) {
                return false;
            } else if (this.cachingUseCount.intValue() != spec.cachingUseCount.intValue()) {
                return false;
            }
        } else {
            if (spec.cachingUseCount != null) {
                return false;
            }
        }
        if (this.entryType != null) {
            if (!this.entryType.equals(spec.entryType)) {
                return false;
            }
        } else {
            if (spec.entryType != null) {
                return false;
            }
        }
        if (!CollectionUtils.isEmpty(this.overrides)) {
            if (CollectionUtils.isEmpty(spec.overrides)) {
                return false;
            } else if (!checkList(this.overrides, spec.overrides)) {
                return false;
            }
        } else {
            if (!CollectionUtils.isEmpty(spec.overrides)) {
                return false;
            }
        }
        if (this.blacklist != null) {
            if (this.blacklist != spec.blacklist) {
                return false;
            }
        } else {
            if (spec.blacklist != null) {
                return false;
            }
        }
        return true;
    }

    private boolean checkList(List<String> thisInstance, List<String> objInstance) {
        if (thisInstance.size() != objInstance.size()) {
            return false;
        }
        for (int i = 0; i < thisInstance.size(); i++) {
            if (!thisInstance.get(i).equals(objInstance.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getProviderUrl() != null ? getProviderUrl().hashCode() : 0;
        result = 31 * result + (getRefreshInterval() != null ? getRefreshInterval().hashCode() : 0);
        result = 31 * result + (getTtl() != null ? getTtl().hashCode() : 0);
        result = 31 * result + (getCachingInterval() != null ? getCachingInterval().hashCode() : 0);
        result = 31 * result + (getCachingUseCount() != null ? getCachingUseCount().hashCode() : 0);
        result = 31 * result + (getOverrides() != null ? getOverrides().hashCode() : 0);
        result = 31 * result + (getEntryType() != null ? getEntryType().hashCode() : 0);
        result = 31 * result + (getBlacklist() != null ? getBlacklist().hashCode() : 0);
        return result;
    }
}
