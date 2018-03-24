package com.harmonycloud.k8s.bean.cluster;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterTemplate implements Serializable {
    private static final long serialVersionUID = 6686498863496652474L;
    private String serviceName ;
    private String namespace ;
    private String type ;
    private List<ServicePort> servicePort ;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ServicePort> getServicePort() {
        return servicePort;
    }

    public void setServicePort(List<ServicePort> servicePort) {
        this.servicePort = servicePort;
    }


}
