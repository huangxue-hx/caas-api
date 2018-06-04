package com.harmonycloud.service.platform.bean.microservice;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-7
 * @Modified 接口修改需要增加端口名称
 */
public class MsfDeploymentPort {

    private String name;

    private String container_port;

    private String service_port;

    private String node_port;

    private String external_type;

    private String expose_port;

    private String http_path;

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContainer_port() {
        return container_port;
    }

    public void setContainer_port(String container_port) {
        this.container_port = container_port;
    }

    public String getService_port() {
        return service_port;
    }

    public void setService_port(String service_port) {
        this.service_port = service_port;
    }

    public String getNode_port() {
        return node_port;
    }

    public void setNode_port(String node_port) {
        this.node_port = node_port;
    }

    public String getExternal_type() {
        return external_type;
    }

    public void setExternal_type(String external_type) {
        this.external_type = external_type;
    }

    public String getHttp_path() {
        return http_path;
    }

    public void setHttp_path(String http_path) {
        this.http_path = http_path;
    }

    public String getExpose_port() {
        return expose_port;
    }

    public void setExpose_port(String expose_port) {
        this.expose_port = expose_port;
    }
}
