package com.harmonycloud.k8s.bean.scale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.k8s.bean.UnversionedListMeta;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComplexPodScaleList {
    private String kind;

    private String apiVersion;

    private UnversionedListMeta metadata;

    private List<ComplexPodScale> items;

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

    public List<ComplexPodScale> getItems() {
        return items;
    }

    public void setItems(List<ComplexPodScale> items) {
        this.items = items;
    }
}
