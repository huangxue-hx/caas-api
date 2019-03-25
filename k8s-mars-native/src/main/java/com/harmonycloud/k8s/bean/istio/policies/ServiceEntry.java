package com.harmonycloud.k8s.bean.istio.policies;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.bean.BaseResource;
import com.harmonycloud.k8s.bean.istio.policies.Rule;
import com.harmonycloud.k8s.bean.istio.policies.ServiceEntrySpec;


@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceEntry extends BaseResource {

    private ServiceEntrySpec spec;

    public ServiceEntrySpec getSpec() {
        return spec;
    }

    public void setSpec(ServiceEntrySpec spec) {
        this.spec = spec;
    }

}
