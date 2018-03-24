package com.harmonycloud.service.platform.bean.harbor;
import java.util.List;

/**
 * Created by root on 5/24/17.
 */
public class HarborProjectInfo {
    //private Integer use_num;
    private Integer repositoryId;
    private String project_name;
    private Integer project_id;
    private Float   use_size;
    private Float  use_rate;
   // private Integer use_num;
    private Float   quota_size;
    private HarborSecurityClairStatistcs harborSecurityClairStatistcs;
    private List<HarborRepositoryMessage> harborRepositoryMessagesList;
    private String harborHost;
    private String referredClusterNames;

    public Integer getProject_id() {
        return project_id;
    }

    public void setProject_id(Integer project_id) {
        this.project_id = project_id;
    }

    public List<HarborRepositoryMessage> getHarborRepositoryMessagesList() {
        return harborRepositoryMessagesList;
    }

    public void setHarborRepositoryMessagesList(List<HarborRepositoryMessage> harborRepositoryMessagesList) {
        this.harborRepositoryMessagesList = harborRepositoryMessagesList;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public Float getUse_size() {
        return use_size;
    }

    public void setUse_size(Float use_size) {
        this.use_size = use_size;
    }

    public Float getUse_rate() {
        return use_rate;
    }

    public void setUse_rate(Float use_rate) {
        this.use_rate = use_rate;
    }

    public Float getQuota_size() {
        return quota_size;
    }

    public void setQuota_size(Float quota_size) {
        this.quota_size = quota_size;
    }

    public HarborSecurityClairStatistcs getHarborSecurityClairStatistcs() {
        return harborSecurityClairStatistcs;
    }

    public void setHarborSecurityClairStatistcs(HarborSecurityClairStatistcs harborSecurityClairStatistcs) {
        this.harborSecurityClairStatistcs = harborSecurityClairStatistcs;
    }

    public Integer getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Integer repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getHarborHost() {
        return harborHost;
    }

    public void setHarborHost(String harborHost) {
        this.harborHost = harborHost;
    }

    public String getReferredClusterNames() {
        return referredClusterNames;
    }

    public void setReferredClusterNames(String referredClusterNames) {
        this.referredClusterNames = referredClusterNames;
    }
}
