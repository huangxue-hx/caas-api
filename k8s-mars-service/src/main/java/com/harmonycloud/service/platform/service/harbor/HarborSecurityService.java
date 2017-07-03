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
    ActionReturnUtil clairStatistcs(String flagName, String name) throws Exception;

    /**
     * 扫描总结
     *
     * @param repoName repo name
     * @param tag      tag
     * @return
     * @throws Exception
     */
    ActionReturnUtil vulnerabilitySummary(String repoName, String tag) throws Exception;

    /**
     * 扫描总结 -package纬度
     *
     * @param repoName repo name
     * @param tag      tag
     * @return
     * @throws Exception
     */
    ActionReturnUtil vulnerabilitiesByPackage(String repoName, String tag) throws Exception;
    
    /**
     * 刷新镜像库详情
     * @return
     * @throws Exception
     */
    public ActionReturnUtil refreshImageRepo() throws Exception;

}
