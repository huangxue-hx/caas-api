package com.harmonycloud.k8s.bean.cluster;

import java.io.Serializable;

public class ClusterRedis implements Serializable {
    private static final long serialVersionUID = 5494071110627557L;
    private String type;
    private String password;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
