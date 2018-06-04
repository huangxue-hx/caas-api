package com.harmonycloud.service.user;

import com.harmonycloud.dao.user.bean.UserProject;
import java.util.List;
import java.util.Map;

/**
 *
 * @Title
 * @author zgl
 * @date 2017年12月11日
 * @Description
 * @version V1.0
 */
public interface UserProjectService {
    /**
     * 根据id获取项目下的用户关系
     * @return
     * @throws Exception
     */
    public UserProject getUserProjectById(int id) throws Exception;
    /**
     * 根据用户名与项目id获取项目用户
     * @return
     * @throws Exception
     */
    public UserProject getUserProjectByUsernameAndProjectId(String username,String projectId) throws Exception;

    /**
     * 根据项目id获取项目用户列表 带分页
     * @param projectId
     * @return
     * @throws Exception
     */
    public List<UserProject> getUserProjectByProjectId(String projectId,Integer limit,Integer offset) throws Exception;
    /**
     * 根据项目id获取项目用户列表 不带分页
     * @param projectId
     * @return
     * @throws Exception
     */
    public List<UserProject> getUserProjectByProjectId(String projectId) throws Exception;
    /**
     * 创建项目下用户
     * @param userProject
     * @throws Exception
     */
    public void createUserProject(UserProject userProject) throws Exception;
    /**
     * 更新项目下用户
     * @param userProject
     * @throws Exception
     */
    public void updateUserProject(UserProject userProject) throws Exception;
    /**
     * 根据id删除项目下用户
     * @param id
     * @throws Exception
     */
    public void deleteUserProjectById(Integer id) throws Exception;

    /**
     * 根据项目id删除用户
     * @param projectId
     * @throws Exception
     */
    public void deleteUserProjectByProjectId(String projectId) throws Exception;

}
