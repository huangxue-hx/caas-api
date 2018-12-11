package com.harmonycloud.k8s.bean.istio.policies;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.k8s.bean.Service;
import com.harmonycloud.k8s.bean.UnversionedListMeta;

import java.util.List;

/**
 * create  by  ljf
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceEntryList {

    private String kind;

    private String apiVersion;

    private UnversionedListMeta metadata;

    private List<ServiceEntry> items;

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

    public List<ServiceEntry> getItems() {
        return items;
    }

    public void setItems(List<ServiceEntry> items) {
        this.items = items;
    }
}
