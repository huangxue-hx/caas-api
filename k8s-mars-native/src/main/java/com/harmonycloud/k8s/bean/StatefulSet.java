package com.harmonycloud.k8s.bean;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.constant.Constant;

/**
 * @author yekan
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatefulSet extends BaseResource{

    private StatefulSetSpec spec;

    private StatefulSetStatus status;

    public StatefulSetSpec getSpec() {
        return spec;
    }

    public void setSpec(StatefulSetSpec spec) {
        this.spec = spec;
    }

    public StatefulSetStatus getStatus() {
        return status;
    }

    public void setStatus(StatefulSetStatus status) {
        this.status = status;
    }

    public StatefulSet(){
        this.setKind("StatefulSet");
        this.setApiVersion(Constant.STATEFULSET_VERSION);
    }
}
