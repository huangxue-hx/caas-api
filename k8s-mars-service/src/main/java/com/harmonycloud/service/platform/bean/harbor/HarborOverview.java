package com.harmonycloud.service.platform.bean.harbor;

import com.harmonycloud.dao.harbor.bean.ImageCleanRule;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.k8s.bean.cluster.HarborServer;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zsl on 2017/1/19.
 * harbor log bean
 */
public class HarborOverview implements Serializable {

	private static final long serialVersionUID = 1L;
    //harbor服务器信息
	private HarborServer harborServer;
    //镜像仓库列表
    private List<ImageRepository> repositories;
    //清理规则列表
    private List<ImageCleanRule> cleanRules;
    //备份同步规则列表
    private List<HarborPolicyDetail> policies;

    public HarborServer getHarborServer() {
        return harborServer;
    }

    public void setHarborServer(HarborServer harborServer) {
        this.harborServer = harborServer;
    }

    public List<ImageRepository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<ImageRepository> repositories) {
        this.repositories = repositories;
    }

    public List<ImageCleanRule> getCleanRules() {
        return cleanRules;
    }

    public void setCleanRules(List<ImageCleanRule> cleanRules) {
        this.cleanRules = cleanRules;
    }

    public List<HarborPolicyDetail> getPolicies() {
        return policies;
    }

    public void setPolicies(List<HarborPolicyDetail> policies) {
        this.policies = policies;
    }
}
