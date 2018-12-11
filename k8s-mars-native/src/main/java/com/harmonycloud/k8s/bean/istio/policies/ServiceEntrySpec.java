package com.harmonycloud.k8s.bean.istio.policies;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * create by  ljf  18/12/6
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceEntrySpec {
    private List<String> hosts;

    private List<String> addresses;

    private List<Port> ports;

    private String location;

    private String resolution;

    private List<Map<String,Object>> endpoints;

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public List<Port> getPorts() {
        return ports;
    }

    public void setPorts(List<Port> ports) {
        this.ports = ports;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public List<Map<String, Object>> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Map<String, Object>> endpoints) {
        this.endpoints = endpoints;
    }
}
