package com.harmonycloud.service.platform.bean.microservice;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-5
 * @Modified
 */
public class MsfDeploymentTemplate {
    private String image;

    private String repo;

    private String labels;    //以逗号分隔

    public MsfDeploymentTemplate(String image, String repo, String labels) {
        this.image = image;
        this.repo = repo;
        this.labels = labels;
    }

    public String getImage() {
        return image;
    }

    public MsfDeploymentTemplate() {
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

}
