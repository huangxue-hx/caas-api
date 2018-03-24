package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-18
 * @Modified
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RollingUpdateDaemonSet {

    private Object maxUnavailable;

    public Object getMaxUnavailable() {
        return maxUnavailable;
    }

    public void setMaxUnavailable(Object maxUnavailable) {
        this.maxUnavailable = maxUnavailable;
    }
}
