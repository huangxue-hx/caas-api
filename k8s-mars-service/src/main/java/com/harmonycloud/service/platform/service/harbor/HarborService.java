package com.harmonycloud.service.platform.service.harbor;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.bean.harbor.*;

import java.util.List;

/**
 * Created by zsl on 2017/1/18.
 * harbor常规接口, 与harbor api接口交互服务类
 */
public interface HarborService {

    HarborOverview getHarborOverview(String harborHost, String userName) throws Exception;

    /**
     * 分页获取harbor project list
     *
     * @param page     页号
     * @param pageSize 页码
     * @return
     * @throws Exception
     */
    List<HarborProject> listProject(String harborHost, Integer page, Integer pageSize) throws Exception;

    /**
     * 根据projectId获取harbor project详情
     *
     * @param harborProjectId id
     * @return
     * @throws Exception
     */
    HarborProject getHarborProjectById(String harborHost, Integer harborProjectId) throws Exception;

    /**
     * 根据projectId获取harbor repository列表
     *
     * @param harborProjectId id
     * @return
     * @throws Exception
     */
    ActionReturnUtil repoListById(String harborHost, Integer harborProjectId) throws Exception;

    /**
     * 根据repository name获取tags
     *
     * @param repoName repoName
     * @return
     * @throws Exception
     */
    ActionReturnUtil getTagsByRepoName(String harborHost, String repoName) throws Exception;

    /**
     * 获取manifests
     *
     * @param repoName repoName
     * @param tag      tag
     * @return
     * @throws Exception
     */
    ActionReturnUtil getManifests(String harborHost, String repoName, String tag) throws Exception;

    /**
     * 创建harbor project
     *
     * @param harborProject bean
     * @return
     * @throws Exception
     */
    ActionReturnUtil createProject(String harborHost, HarborProject harborProject) throws Exception;

    /**
     * 删除harbor project
     *
     * @param projectId projectId
     * @return
     * @throws Exception
     */
    ActionReturnUtil deleteProject(String harborHost, Integer projectId) throws Exception;
    
    /**
     * 删除repo
     * 
     * @param repo
     * @param tag
     * @return
     * @throws Exception
     */
    ActionReturnUtil deleteRepo(String harborHost, String repo, String tag) throws Exception;

     /*
	 * @lili
	 */
    /**
     * 查看project配额
     * @return
     * @throws Exception
     */
    HarborProject getProjectQuota(String harborHost, String harborProjectName) throws Exception;
    /**
     * 更新project配额
     * @return
     * @throws Exception
     */
    ActionReturnUtil updateProjectQuota(String harborHost, HarborProjectQuota harborProjectQuota) throws Exception;
    /**
     * get repository detail
     * @return
     * @throws Exception
     */
    ActionReturnUtil getRepositoryDetailByProjectId(String harborHost, Integer projectId) throws Exception ;


    /**
     * 得到project information clair result && quota
     * @return
     * @throws Exception
     */
    ActionReturnUtil getRepositorySummary(String harborHost, String harborProjectName) throws Exception;
    /**
     * 模糊查询镜像repository
     * @return
     * @throws Exception
     */
    ActionReturnUtil getRepoFuzzySearch(String query,String projectId, Boolean isPublic) throws Exception;

    ActionReturnUtil getFuzzySearch(String harborHost, String query) throws Exception;
    /**
     * 查询指定项目的镜像
     * @return
     * @throws Exception
     */
    ActionReturnUtil listImageDetail(String projectId) throws Exception;

    ActionReturnUtil getFirstImage(String projectId, String clusterId, String harborProjectName, String repoName) throws Exception;

    List<HarborLog> projectOperationLogs(String harborHost,Integer projectId, Integer begin, Integer end,
                                         String keywords) throws Exception;

    HarborRepositoryMessage getHarborRepositoryDetail(String harborHost, String repoName) throws Exception;

    ActionReturnUtil getImagesByProjectId(String projectId, String clusterId) throws Exception;
}
