package com.harmonycloud.k8s.bean;

import java.util.List;

/**
 * Created by czm on 2017/4/18.
 */
public class ServiceAccountList{
    private List<ServiceAccount> items;

    private String apiVersion;

    private String kind;

    private UnversionedListMeta metadata;

    public List<ServiceAccount> getItems() {
        return items;
    }

    public void setItems(List<ServiceAccount> items) {
        this.items = items;
    }

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

    public UnversionedListMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(UnversionedListMeta metadata) {
        this.metadata = metadata;
    }
}
