package com.harmonycloud.dao.ci.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by anson on 17/7/13.
 */
public class StageType  implements Serializable{

    private Integer id;
    private String name;
    private boolean userDefined;
    private String tenantId;
    private Integer templateType;

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

    public boolean isUserDefined() {
        return userDefined;
    }

    public void setUserDefined(boolean userDefined) {
        this.userDefined = userDefined;
    }

    @JsonIgnore
    public String getTenantId() {
        return tenantId;
    }
    @JsonProperty
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getTemplateType() {
        return templateType;
    }

    public void setTemplateType(Integer templateType) {
        this.templateType = templateType;
    }
}
