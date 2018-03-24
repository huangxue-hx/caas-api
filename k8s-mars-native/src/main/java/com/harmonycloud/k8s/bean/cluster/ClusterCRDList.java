package com.harmonycloud.k8s.bean.cluster;

import com.harmonycloud.k8s.bean.UnversionedListMeta;

import java.io.Serializable;
import java.util.List;

public class ClusterCRDList implements Serializable {
    private static final long serialVersionUID = -681399618693091154L;
    private String kind;

    private String apiVersion;

    private UnversionedListMeta metadata;

    private List<ClusterCRD> items;

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

    public List<ClusterCRD> getItems() {
        return items;
    }

    public void setItems(List<ClusterCRD> items) {
        this.items = items;
    }
}
