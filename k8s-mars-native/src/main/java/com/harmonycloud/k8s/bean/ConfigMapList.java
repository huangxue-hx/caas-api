package com.harmonycloud.k8s.bean;

import java.util.List;

/**
 * Created by czm on 2017/4/19.
 */
public class ConfigMapList extends BaseResource {
    private List<ConfigMap> items;

    public List<ConfigMap> getItems() {
        return items;
    }

    public void setItems(List<ConfigMap> items) {
        this.items = items;
    }
}
