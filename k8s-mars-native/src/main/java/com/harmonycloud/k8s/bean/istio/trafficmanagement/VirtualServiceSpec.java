package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Create by weg on 18-11-30.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualServiceSpec {

    private List<String> hosts;

    private List<String> gateways;

    private List<HTTPRoute> http;

    private List<TCPRoute> tcp;

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public List<String> getGateways() {
        return gateways;
    }

    public void setGateways(List<String> gateways) {
        this.gateways = gateways;
    }

    public List<HTTPRoute> getHttp() {
        return http;
    }

    public void setHttp(List<HTTPRoute> http) {
        this.http = http;
    }

    public List<TCPRoute> getTcp() {
        return tcp;
    }

    public void setTcp(List<TCPRoute> tcp) {
        this.tcp = tcp;
    }
}
