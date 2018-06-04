package com.harmonycloud.service.platform.bean.harbor;

import com.harmonycloud.dao.harbor.bean.ImageCleanRule;
import com.harmonycloud.dao.harbor.bean.ImageRepository;

import java.util.List;


public class ImageCleanRuleDetail {

    private String projectKey;

    private ImageCleanRule rule;

    private ImageRepository imageRepository;

    private List<String> repoList;

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public ImageCleanRule getRule() {
        return rule;
    }

    public void setRule(ImageCleanRule rule) {
        this.rule = rule;
    }

    public List<String> getRepoList() {
        return repoList;
    }

    public void setRepoList(List<String> repoList) {
        this.repoList = repoList;
    }


    public ImageRepository getImageRepository() {
        return imageRepository;
    }

    public void setImageRepository(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }
}
