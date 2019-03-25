package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.k8s.constant.Constant;

import java.util.List;

/**
 * Created by czm on 2017/4/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceAccount extends BaseResource{

    private LocalObjectReference imagePullSecrets;
    private List<ObjectReference> secrets;
    private boolean automountServiceAccountToken;

    public ServiceAccount() {
        this.setKind("ServiceAccount");
        this.setApiVersion(Constant.V1_VERSION);
    }

    public boolean isAutomountServiceAccountToken() {
        return automountServiceAccountToken;
    }

    public void setAutomountServiceAccountToken(boolean automountServiceAccountToken) {
        this.automountServiceAccountToken = automountServiceAccountToken;
    }

    public LocalObjectReference getImagePullSecrets() {
        return imagePullSecrets;
    }

    public void setImagePullSecrets(LocalObjectReference imagePullSecrets) {
        this.imagePullSecrets = imagePullSecrets;
    }

    public List<ObjectReference> getSecrets() {
        return secrets;
    }

    public void setSecrets(List<ObjectReference> secrets) {
        this.secrets = secrets;
    }
}
