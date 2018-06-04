package com.harmonycloud.k8s.bean.cluster;

import com.harmonycloud.k8s.bean.UnversionedListMeta;

import java.io.Serializable;
import java.util.List;

public class TemplateList implements Serializable {
    private static final long serialVersionUID = 1976547247637342461L;
    private String kind;

    private String apiVersion;

    private UnversionedListMeta metadata;

    private List<Template> items;

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

    public List<Template> getItems() {
        return items;
    }

    public void setItems(List<Template> items) {
        this.items = items;
    }
}
