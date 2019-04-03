package com.harmonycloud.k8s.bean.istio.policies;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * create  by  ljf  2018/12/11
 */

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Endpoint {

    private  String  address;

    private Map<String , Long> ports;

    private Map<String , String> labels;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Map<String, Long> getPorts() {
        return ports;
    }

    public void setPorts(Map<String, Long> ports) {
        this.ports = ports;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
}
