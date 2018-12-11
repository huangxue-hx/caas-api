package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.bean.BaseResource;

/**
 * Create by weg on 18-11-30.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualService extends BaseResource {

    private VirtualServiceSpec spec;

    public VirtualServiceSpec getSpec() {
        return spec;
    }

    public void setSpec(VirtualServiceSpec spec) {
        this.spec = spec;
    }
}
