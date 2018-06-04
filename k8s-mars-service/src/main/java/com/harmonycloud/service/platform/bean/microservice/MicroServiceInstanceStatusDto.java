package com.harmonycloud.service.platform.bean.microservice;

import com.harmonycloud.service.platform.bean.PodDetail;

import java.util.List;
import java.util.Map;

/**
 * @Author jiangmi
 * @Description 微服务查询组件实例运行状态接口返回参数对象
 * @Date created in 2017-12-5
 * @Modified 修改
 */
public class MicroServiceInstanceStatusDto {

    private String status;

    private String name;

    private String service_name;

    private String namespace;

    private String space_id;

    private List<MsfDeploymentPort> ports;

    private String create_time;

    private String update_time;

    private String replicas;

    private String labels;

    private Map<String, String> affinity;

    private List<Map<String, Object>> containers;

    private List<Map<String, Object>> external_services;

    private List<Map<String, Object>> pods;

    private List<MsfDeploymentVolume> volumes;

    private String instance_id;

    public String getInstance_id() {
        return instance_id;
    }

    public void setInstance_id(String instance_id) {
        this.instance_id = instance_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSpace_id() {
        return space_id;
    }

    public void setSpace_id(String space_id) {
        this.space_id = space_id;
    }

    public List<MsfDeploymentPort> getPorts() {
        return ports;
    }

    public void setPorts(List<MsfDeploymentPort> ports) {
        this.ports = ports;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getReplicas() {
        return replicas;
    }

    public void setReplicas(String replicas) {
        this.replicas = replicas;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public Map<String, String> getAffinity() {
        return affinity;
    }

    public void setAffinity(Map<String, String> affinity) {
        this.affinity = affinity;
    }

    public List<Map<String, Object>> getContainers() {
        return containers;
    }

    public void setContainers(List<Map<String, Object>> containers) {
        this.containers = containers;
    }

    public List<Map<String, Object>> getExternal_services() {
        return external_services;
    }

    public void setExternal_services(List<Map<String, Object>> external_services) {
        this.external_services = external_services;
    }

    public List<Map<String, Object>> getPods() {
        return pods;
    }

    public void setPods(List<Map<String, Object>> pods) {
        this.pods = pods;
    }

    public List<MsfDeploymentVolume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<MsfDeploymentVolume> volumes) {
        this.volumes = volumes;
    }

}
