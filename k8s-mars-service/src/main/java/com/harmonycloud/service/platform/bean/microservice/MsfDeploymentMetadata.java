package com.harmonycloud.service.platform.bean.microservice;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-5
 * @Modified
 */
public class MsfDeploymentMetadata {
    private String instance_id;

    private String deploy_type;

    private String dockerfile_url;

    private String deployment_name;

    public MsfDeploymentMetadata() {

    }

    public MsfDeploymentMetadata(String instance_id, String deploy_type, String dockerfile_url) {
        this.instance_id = instance_id;
        this.deploy_type = deploy_type;
        this.dockerfile_url = dockerfile_url;
    }

    public String getDeployment_name() {
        return deployment_name;
    }

    public void setDeployment_name(String deployment_name) {
        this.deployment_name = deployment_name;
    }

    public String getInstance_id() {
        return instance_id;
    }

    public void setInstance_id(String instance_id) {
        this.instance_id = instance_id;
    }

    public String getDeploy_type() {
        return deploy_type;
    }

    public void setDeploy_type(String deploy_type) {
        this.deploy_type = deploy_type;
    }

    public String getDockerfile_url() {
        return dockerfile_url;
    }

    public void setDockerfile_url(String dockerfile_url) {
        this.dockerfile_url = dockerfile_url;
    }

}
