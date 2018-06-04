package com.harmonycloud.service.platform.bean.microservice;

import java.util.List;
import java.util.Map;

/**
 * @Author jiangmi
 * @Description  部署微服务组件和返回组件实例详情的Deployment的bean
 * @Date created in 2017-12-5
 * @Modified
 */
public class MsfDeployment {
    private MsfDeploymentMetadata metadata;

    private MsfDeploymentTemplate template;

    private MsfDeploymentSpec spec;

    private List<MsfDeploymentPort> ports;

    private List<MsfDeploymentVolume> volumes;

    private Map<String, String> affinity;

    public MsfDeploymentMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(MsfDeploymentMetadata metadata) {
        this.metadata = metadata;
    }

    public MsfDeploymentTemplate getTemplate() {
        return template;
    }

    public void setTemplate(MsfDeploymentTemplate template) {
        this.template = template;
    }

    public MsfDeploymentSpec getSpec() {
        return spec;
    }

    public void setSpec(MsfDeploymentSpec spec) {
        this.spec = spec;
    }

    public List<MsfDeploymentPort> getPorts() {
        return ports;
    }

    public void setPorts(List<MsfDeploymentPort> ports) {
        this.ports = ports;
    }

    public List<MsfDeploymentVolume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<MsfDeploymentVolume> volumes) {
        this.volumes = volumes;
    }

    public Map<String, String> getAffinity() {
        return affinity;
    }

    public void setAffinity(Map<String, String> affinity) {
        this.affinity = affinity;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    private Map<String, String> environment;

}
