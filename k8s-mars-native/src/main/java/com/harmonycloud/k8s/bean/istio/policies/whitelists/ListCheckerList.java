package com.harmonycloud.k8s.bean.istio.policies.whitelists;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.k8s.bean.UnversionedListMeta;
import com.harmonycloud.k8s.bean.istio.policies.ratelimit.RedisQuota;

import java.util.List;

/**
 * @author xc
 * @date 2018/9/17 17:52
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListCheckerList {

    private String kind;

    private String apiVersion;

    private UnversionedListMeta metadata;

    private List<ListChecker> items;

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

    public List<ListChecker> getItems() {
        return items;
    }

    public void setItems(List<ListChecker> items) {
        this.items = items;
    }
}
