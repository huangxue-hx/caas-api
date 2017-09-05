package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by root on 8/18/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResourceList {
    private List<BaseResource> items;

    public List<BaseResource> getItems() {
        return items;
    }

    public void setItems(List<BaseResource> items) {
        this.items = items;
    }
}