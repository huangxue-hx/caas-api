package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author xc
 * @date 2018/6/21 12:03
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageClassList {

    private String kind;

    private String apiVersion;

    private UnversionedListMeta metadata;

    private List<StorageClass> items;

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

    public List<StorageClass> getItems() {
        return items;
    }

    public void setItems(List<StorageClass> items) {
        this.items = items;
    }
}
