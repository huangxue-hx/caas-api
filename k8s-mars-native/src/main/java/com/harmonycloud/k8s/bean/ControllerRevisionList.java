package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by jmi on 18-7-2.
 */
@JsonInclude(value= JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ControllerRevisionList {

    private String kind;

    private String apiVersion;

    private ListMeta metadata;

    private List<ControllerRevision> items;

    public List<ControllerRevision> getItems() {
        return items;
    }

    public void setItems(List<ControllerRevision> items) {
        this.items = items;
    }

    public ListMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(ListMeta metadata) {
        this.metadata = metadata;
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
}
