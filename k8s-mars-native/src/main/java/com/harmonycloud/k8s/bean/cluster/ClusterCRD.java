package com.harmonycloud.k8s.bean.cluster;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.k8s.bean.BaseResource;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterCRD extends BaseResource implements Serializable {
    private static final long serialVersionUID = -8462732934187165654L;
    private ClusterSpec spec ;

    private ClusterStatus status ;

    public ClusterStatus getStatus() {
        return status;
    }

    public void setStatus(ClusterStatus status) {
        this.status = status;
    }

    public ClusterSpec getSpec() {
        return spec;
    }

    public void setSpec(ClusterSpec spec) {
        this.spec = spec;
    }


}
