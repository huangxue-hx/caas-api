package com.harmonycloud.dto.application;

/**
 * @Author jiangmi
 * @Description 应用亲和度对象（node，pod）
 * @Date created in 2017-12-20
 * @Modified
 */
public class AffinityDto {

    /**是否强制*/
    private boolean required;

    /**标签*/
    private String label;

    private String namespace;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}