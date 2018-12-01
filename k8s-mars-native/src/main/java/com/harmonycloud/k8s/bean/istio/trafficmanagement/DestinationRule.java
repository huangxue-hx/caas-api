package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.bean.BaseResource;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DestinationRule extends BaseResource {

    private DestinationRuleSpec spec;

    public DestinationRuleSpec getSpec() {
        return spec;
    }

    public void setSpec(DestinationRuleSpec spec) {
        this.spec = spec;
    }
}