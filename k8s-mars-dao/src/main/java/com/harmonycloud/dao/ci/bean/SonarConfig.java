package com.harmonycloud.dao.ci.bean;

import java.io.Serializable;

/**
 * Created by riven on 17/19/9.
 */
public class SonarConfig implements Serializable{
    private Integer id;
    private String name;
    private String token;
    private String url;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
