package com.harmonycloud.service.platform.integrationService;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.bean.HarborProject;
import com.harmonycloud.service.platform.bean.HarborRole;

/**
 * Created by zsl on 2017/1/19.
 * harbor相关业务处理层
 */
public interface HarborIntegrationService {

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
     * 根据projectId获取repo详情 repo+tag+domain
     *
     * @param projectId projectId
     * @return
     * @throws Exception
     */
    ActionReturnUtil getRepoDomainDetailByProjectId(Integer projectId) throws Exception;

    /**
     * 根据projectId获取project下的成员列表
     *
     * @param projectId projectId
     * @return
     * @throws Exception
     */
    ActionReturnUtil usersOfProject(Integer projectId) throws Exception;

    /**
     * 创建project下的role
     *
     * @param projectId  projectId
     * @param harborRole role bean
     * @return
     * @throws Exception
     */
    ActionReturnUtil createRole(Integer projectId, HarborRole harborRole) throws Exception;

    /**
     * 更新project下的role
     *
     * @param projectId  projectId
     * @param userId     userId
     * @param harborRole role bean
     * @return
     * @throws Exception
     */
    ActionReturnUtil updateRole(Integer projectId, Integer userId, HarborRole harborRole) throws Exception;

    /**
     * 删除project下的role
     *
     * @param projectId projectId
     * @param userId    userId
     * @return
     * @throws Exception
     */
    ActionReturnUtil deleteRole(Integer projectId, Integer userId) throws Exception;


    /**
     * 在project纬度展示Clair对repo的扫描结果
     *
     * @param projectName project name
     * @return
     * @throws Exception
     */
    ActionReturnUtil clairStatistcsOfProject(String projectName) throws Exception;

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
     * tag详情
     *
     * @param repoName repo name
     * @param tag      tag
     * @return
     */
    ActionReturnUtil manifestsOfTag(String repoName, String tag) throws Exception;

    /**
     * 在user纬度展示Clair对repo的扫描结果
     *
     * @param username username
     * @return
     * @throws Exception
     */
    ActionReturnUtil clairStatistcsOfUser(String username) throws Exception;

    /**
     * 查询namespace下harbor用户列表
     * @param tenantname
     * @param namespace
     * @return
     */
    ActionReturnUtil getUserList(String tenantname, String namespace) throws Exception;
}
