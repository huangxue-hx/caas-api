package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PodDisruptionBudgetList {
    private String kind;

    private String apiVersion;

    private UnversionedListMeta metadata;

    private List<PodDisruptionBudget> items;

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

    public List<PodDisruptionBudget> getItems() {
        return items;
    }

    public void setItems(List<PodDisruptionBudget> items) {
        this.items = items;
    }
}
