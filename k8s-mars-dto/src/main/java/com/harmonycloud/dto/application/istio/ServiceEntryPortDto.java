package com.harmonycloud.dto.application.istio;

public class ServiceEntryPortDto {

    private Integer number;

    private  String  protocol;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
