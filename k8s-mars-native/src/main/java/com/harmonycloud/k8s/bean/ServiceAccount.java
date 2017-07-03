package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by czm on 2017/4/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceAccount extends BaseResource{

    private LocalObjectReference imagePullSecrets;
    private List<ObjectReference> secrets;

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
