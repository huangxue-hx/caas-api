package com.harmonycloud.dto.application.istio;

/**
 * @author xc
 * @date 2018/9/29 11:00
 */
public class WhiteServiceDto {

    private String name;

    private String namespace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

}
