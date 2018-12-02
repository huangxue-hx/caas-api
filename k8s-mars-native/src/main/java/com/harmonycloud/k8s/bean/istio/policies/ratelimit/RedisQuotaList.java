package com.harmonycloud.k8s.bean.istio.policies.ratelimit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.k8s.bean.UnversionedListMeta;

import java.util.List;

/**
 * Created by jmi on 18-9-11.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedisQuotaList {

    private String kind;

    private String apiVersion;

    private UnversionedListMeta metadata;

    private List<RedisQuota> items;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public UnversionedListMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(UnversionedListMeta metadata) {
        this.metadata = metadata;
    }

    public List<RedisQuota> getItems() {
        return items;
    }

    public void setItems(List<RedisQuota> items) {
        this.items = items;
    }
}
