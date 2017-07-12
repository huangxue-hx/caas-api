package com.harmonycloud.dao.application.bean;

import java.io.Serializable;

public class Service implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5713551599193391675L;

    private Integer id;

    private String name;

    private Integer businessId;

    private Integer serviceTemplateId;

    private Integer isExternal;

    private String pvc;

    private String ingress;
    
    private String namespace;

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

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public Integer getServiceTemplateId() {
        return serviceTemplateId;
    }

    public void setServiceTemplateId(Integer serviceTemplateId) {
        this.serviceTemplateId = serviceTemplateId;
    }

    public Integer getIsExternal() {
        return isExternal;
    }

    public void setIsExternal(Integer isExternal) {
        this.isExternal = isExternal;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getPvc() {
        return pvc;
    }

    public void setPvc(String pvc) {
        this.pvc = pvc;
    }

    public String getIngress() {
        return ingress;
    }

    public void setIngress(String ingress) {
        this.ingress = ingress;
    }

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
}

