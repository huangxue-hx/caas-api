package com.harmonycloud.service.platform.service.harbor;

import com.harmonycloud.common.util.ActionReturnUtil;

/**
 * Created by zsl on 2017/1/22.
 * harbor security相关接口
 */
public interface HarborSecurityService {

    /**
     * 展示Clair对repo的扫描结果
     *
     * @param flagName 扫描纬度 user or project
     * @param name     username or projectName
     * @return
     */
    ActionReturnUtil clairStatistcs(String harborHost, String flagName, String name) throws Exception;

    /**
     * 扫描总结
     *
     * @param repoName repo name
     * @param tag      tag
     * @return
     * @throws Exception
     */
    ActionReturnUtil vulnerabilitySummary(String harborHost,String repoName, String tag) throws Exception;

    /**
     * 扫描总结 -package纬度
     *
     * @param repoName repo name
     * @param tag      tag
     * @return
     * @throws Exception
     */
    ActionReturnUtil vulnerabilitiesByPackage(String harborHost,String repoName, String tag) throws Exception;

    /**
     * 在repository纬度展示Clair对repo的扫描结果
     *
     * @param repositoryName repository name
     * @return
     * @throws Exception
     */
    ActionReturnUtil clairStatistcsOfProject(String harborHost, String repositoryName) throws Exception;

    ActionReturnUtil getRepositoryClairStatistcs(Integer repositoryId) throws Exception;

    ActionReturnUtil manifestsOfTag(Integer repositoryId, String imageName, String tag) throws Exception;

    ActionReturnUtil getManifestsDetail(String harborHost, String repoName, String tag) throws Exception;

    /**
     * tag详情
     *
     * @param repoName repo name
     * @param tag      tag
     * @return
     */
    ActionReturnUtil manifestsOfTag(String harborHost, String repoName, String tag) throws Exception;

    /**
     * 在user纬度展示Clair对repo的扫描结果
     *
     * @param username username
     * @return
     * @throws Exception
     */
    ActionReturnUtil clairStatistcsOfUser(String harborHost, String username) throws Exception;
    
    /**
     * 刷新镜像库详情
     * @return
     * @throws Exception
     */
    public ActionReturnUtil refreshImageRepo(String harborHost) throws Exception;

}
