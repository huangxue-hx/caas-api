package com.harmonycloud.k8s.bean.cluster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.k8s.bean.BaseResource;

import java.io.Serializable;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterBaseCRD extends BaseResource implements Serializable {

    private static final long serialVersionUID = -6695364676997532904L;
    private ClusterInfo spec;
    public ClusterInfo getSpec() {
        return spec;
    }

    public void setSpec(ClusterInfo spec) {
        this.spec = spec;
    }
}
