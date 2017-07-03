package com.harmonycloud.service.platform.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jdk.nashorn.internal.ir.annotations.Ignore;

import java.io.Serializable;
import java.util.List;

/**
 * Created by czm on 2017/5/11.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CanaryDeployment implements Serializable {

    private int instances;

    private int seconds;

    private String name; //Deployment 名字


    private String namespace; //Deployment 所属命名空间


    private List<UpdateContainer> containers;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UpdateContainer> getContainers() {
        return containers;
    }

    public void setContainers(List<UpdateContainer> containers) {
        this.containers = containers;
    }

    public int getInstances() {
        return instances;
    }

    public void setInstances(int instances) {
        this.instances = instances;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

}
