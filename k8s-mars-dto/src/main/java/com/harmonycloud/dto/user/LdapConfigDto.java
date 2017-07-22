package com.harmonycloud.dto.user;

import java.io.Serializable;
import java.util.Date;

public class LdapConfigDto implements Serializable {
    private Integer id;

    private String ip;

    private String port;

    private String base;

    private String userdn;

    private String password;

    private Integer isOn;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getUserdn() {
        return userdn;
    }

    public void setUserdn(String userdn) {
        this.userdn = userdn;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getIsOn() {
        return isOn;
    }

    public void setIsOn(Integer isOn) {
        this.isOn = isOn;
    }
}