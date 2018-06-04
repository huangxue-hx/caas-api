package com.harmonycloud.k8s.bean;

import java.util.List;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-18
 * @Modified
 */
public class DaemonSetList {

    private String kind;

    private String apiVersion;

    private UnversionedListMeta metadata;

    private List<DaemonSet> items;

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

    public List<DaemonSet> getItems() {
        return items;
    }

    public void setItems(List<DaemonSet> items) {
        this.items = items;
    }
}
