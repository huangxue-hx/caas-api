package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.constant.Constant;

/**
 * @Author jiangmi
 * @Description DaemonSet在k8s中的bean
 * @Date created in 2017-12-18
 * @Modified
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DaemonSet extends BaseResource{

    private ObjectMeta metadata;

    private DaemonSetSpec spec;

    private DaemonSetStatus status;

    public DaemonSet(){
        this.setKind("DaemonSet");
        this.setApiVersion(Constant.DAEMONSET_VERSION);
    }

    @Override
    public ObjectMeta getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
    }

    public DaemonSetSpec getSpec() {
        return spec;
    }

    public void setSpec(DaemonSetSpec spec) {
        this.spec = spec;
    }

    public DaemonSetStatus getStatus() {
        return status;
    }

    public void setStatus(DaemonSetStatus status) {
        this.status = status;
    }
}
