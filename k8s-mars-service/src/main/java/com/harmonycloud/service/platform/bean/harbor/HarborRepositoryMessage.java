package com.harmonycloud.service.platform.bean.harbor;

import java.util.Date;
import java.util.List;

/**
 * Created by lily on 5/19/17.
 */
public class HarborRepositoryMessage {
    private String  Repository;
    private String  fullNameRepo;
    private List<HarborManifest> repositoryDetial;
    private List<String> tags;
    private Date lastUpdateDate;
  //  private HarborManifest repositoryDetial;
    private Date repoCreateDate;
    private String harborProjectName;

    public String getFullNameRepo() {
        return fullNameRepo;
    }

    public void setFullNameRepo(String fullNameRepo) {
        this.fullNameRepo = fullNameRepo;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getRepository() {
        return Repository;
    }

    public void setRepository(String repository) {
        Repository = repository;
    }

    public List<HarborManifest> getRepositoryDetial() {
        return repositoryDetial;
    }

    public void setRepositoryDetial(List<HarborManifest> repositoryDetial) {
        this.repositoryDetial = repositoryDetial;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Date getRepoCreateDate() {
        return repoCreateDate;
    }

    public void setRepoCreateDate(Date repoCreateDate) {
        this.repoCreateDate = repoCreateDate;
    }

    public String getHarborProjectName() {
        return harborProjectName;
    }

    public void setHarborProjectName(String harborProjectName) {
        this.harborProjectName = harborProjectName;
    }
}
