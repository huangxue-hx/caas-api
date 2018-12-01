package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.k8s.bean.UnversionedListMeta;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DestinationRuleList {

    private String apiVersion;

    private String kind;

    private List<DestinationRule> items;

    private UnversionedListMeta metadata;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public List<DestinationRule> getItems() {
        return items;
    }

    public void setItems(List<DestinationRule> items) {
        this.items = items;
    }

    public UnversionedListMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(UnversionedListMeta metadata) {
        this.metadata = metadata;
    }
}
