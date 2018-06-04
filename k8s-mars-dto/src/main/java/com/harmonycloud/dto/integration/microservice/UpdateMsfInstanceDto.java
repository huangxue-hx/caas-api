package com.harmonycloud.dto.integration.microservice;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-4
 * @Modified
 */
public class UpdateMsfInstanceDto {

    private String instance_id;

    private String replicas;

    private String cpu;

    private String memory;

    public String getInstance_id() {
        return instance_id;
    }

    public void setInstance_id(String instance_id) {
        this.instance_id = instance_id;
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
