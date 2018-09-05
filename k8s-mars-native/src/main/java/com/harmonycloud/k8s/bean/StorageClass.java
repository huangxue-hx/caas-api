package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.constant.Constant;

/**
 * @author xc
 * @date 2018/6/14 16:20
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageClass extends BaseResource {

    public StorageClass() {
        this.setApiVersion(Constant.STORAGECLASS_V1);
        this.setKind("StorageClass");
    }

    private String provisioner;

    private String reclaimPolicy;

    private StorageClassParameters parameters;

    public String getProvisioner() {
        return provisioner;
    }

    public void setProvisioner(String provisioner) {
        this.provisioner = provisioner;
    }

    public String getReclaimPolicy() {
        return reclaimPolicy;
    }

    public void setReclaimPolicy(String reclaimPolicy) {
        this.reclaimPolicy = reclaimPolicy;
    }

    public StorageClassParameters getParameters() {
        return parameters;
    }

    public void setParameters(StorageClassParameters parameters) {
        this.parameters = parameters;
    }
}
