package com.harmonycloud.service.platform.bean.microservice;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-7
 * @Modified
 */
public class MsfDeploymentSpec {

    private String replicas;

    private String cpu;

    private String memory;

    private String service_name;

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String getReplicas() {
        return replicas;
    }

    public void setReplicas(String replicas) {
        this.replicas = replicas;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

}
