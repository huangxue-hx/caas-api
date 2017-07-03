package com.harmonycloud.service.platform.service.harbor;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.bean.HarborProject;
import com.harmonycloud.service.platform.bean.HarborProjectQuota;
/**
 * Created by zsl on 2017/1/18.
 * harbor常规接口
 */
public interface HarborService {

    /**
     * harbor 登录接口
     *
     * @param username 用户名
     * @param password 密码
     * @return
     * @throws Exception
     */
    ActionReturnUtil login(String username, String password) throws Exception;

    /**
     * 分页获取harbor project list
     *
     * @param page     页号
     * @param pageSize 页码
     * @return
     * @throws Exception
     */
    ActionReturnUtil projectList(Integer page, Integer pageSize) throws Exception;

    /**
     * 根据projectId获取harbor project详情
     *
     * @param projectId id
     * @return
     * @throws Exception
     */
    ActionReturnUtil getProjectById(Integer projectId) throws Exception;

    /**
     * 根据projectId获取harbor repository列表
     *
     * @param projectId id
     * @return
     * @throws Exception
     */
    ActionReturnUtil repoListById(Integer projectId) throws Exception;

    /**
     * 根据repository name获取tags
     *
     * @param repoName repoName
     * @return
     * @throws Exception
     */
    ActionReturnUtil getTagsByRepoName(String repoName) throws Exception;

    /**
     * 获取manifests
     *
     * @param repoName repoName
     * @param tag      tag
     * @return
     * @throws Exception
     */
    ActionReturnUtil getManifests(String repoName, String tag) throws Exception;

    /**
     * 创建harbor project
     *
     * @param harborProject bean
     * @return
     * @throws Exception
     */
    ActionReturnUtil createProject(HarborProject harborProject) throws Exception;

    /**
     * 删除harbor project
     *
     * @param projectId projectId
     * @return
     * @throws Exception
     */
    ActionReturnUtil deleteProject(Integer projectId) throws Exception;
    
    /**
     * 删除repo
     * 
     * @param repo
     * @param tag
     * @return
     * @throws Exception
     */
    ActionReturnUtil deleteRepo(String repo, String tag) throws Exception;
    
    /**
     * 获取harbor信息
     * @return
     * @throws Exception
     */
    ActionReturnUtil listProvider() throws Exception;
    
    /**
     * 根据用户名获取Harbor-project
     * @return
     * @throws Exception
     */
    ActionReturnUtil getProjectByUser(String username) throws Exception;
     /*
	 * @lili
	 */
    /**
     * 查看project配额
     * @return
     * @throws Exception
     */
    ActionReturnUtil getProjectQuota(String projectname) throws Exception;
    /**
     * 更新project配额
     * @return
     * @throws Exception
     */
    ActionReturnUtil updateProjectQuota(Integer projectID,HarborProjectQuota harborProjectQuota) throws Exception;
    /**
     * get repository detail
     * @return
     * @throws Exception
     */
    ActionReturnUtil getRepositoryDetailByProjectId(Integer projectId) throws Exception ;
    /**
     * 得到project information clair result && quota
     * @return
     * @throws Exception
     */
    ActionReturnUtil getPolicyDetailList(String projectName) throws Exception;
    /**
     * 模糊查询镜像repository
     * @return
     * @throws Exception
     */
    ActionReturnUtil getRepoFuzzySearch(String query,String tenantID) throws Exception;
    /**
     * 查询指定租户的镜像
     * @return
     * @throws Exception
     */
    ActionReturnUtil getImageByTenantID(String tenantID) throws Exception;
}
