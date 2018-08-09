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
public class StorageClassParameters extends BaseResource {
    private String monitors;
    private String pool;
    private String adminId;
    private String adminSecretName;
    private String userId;
    private String userSecretName;

    public String getMonitors() {
        return monitors;
    }

    public void setMonitors(String monitors) {
        this.monitors = monitors;
    }

    public String getPool() {
        return pool;
    }

    public void setPool(String pool) {
        this.pool = pool;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAdminSecretName() {
        return adminSecretName;
    }

    public void setAdminSecretName(String adminSecretName) {
        this.adminSecretName = adminSecretName;
    }

    public String getUserSecretName() {
        return userSecretName;
    }

    public void setUserSecretName(String userSecretName) {
        this.userSecretName = userSecretName;
    }
}
