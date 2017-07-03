package com.harmonycloud.dto.tenant;

import java.io.Serializable;

/**
 * Created by andy on 17-1-20.
 */
public class HardDto implements Serializable{

    private static final long serialVersionUID = 4851500257094035242L;
    private String pods;

    private String cpu;

    private String memory;

    private String configmaps;

    private String persistentvolumeclaims;

    private Integer replicationcontrollers;

    private Integer resourcequotas;

    private String services;

    private String secrets;

    public String getPods() {
        return pods;
    }

    public void setPods(String pods) {
        this.pods = pods;
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

    public String getConfigmaps() {
        return configmaps;
    }

    public void setConfigmaps(String configmaps) {
        this.configmaps = configmaps;
    }

    public String getPersistentvolumeclaims() {
        return persistentvolumeclaims;
    }

    public void setPersistentvolumeclaims(String persistentvolumeclaims) {
        this.persistentvolumeclaims = persistentvolumeclaims;
    }

    public Integer getReplicationcontrollers() {
        return replicationcontrollers;
    }

    public void setReplicationcontrollers(Integer replicationcontrollers) {
        this.replicationcontrollers = replicationcontrollers;
    }

    public Integer getResourcequotas() {
        return resourcequotas;
    }

    public void setResourcequotas(Integer resourcequotas) {
        this.resourcequotas = resourcequotas;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public String getSecrets() {
        return secrets;
    }

    public void setSecrets(String secrets) {
        this.secrets = secrets;
    }
}
