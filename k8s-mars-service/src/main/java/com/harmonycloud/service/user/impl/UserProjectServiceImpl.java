package com.harmonycloud.service.user.impl;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.user.UserProjectMapper;
import com.harmonycloud.dao.user.bean.*;
import com.harmonycloud.service.user.UserProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserProjectServiceImpl implements UserProjectService {
    @Autowired
    private UserProjectMapper userProjectMapper;

    /**
     * 根据id获取项目下的用户关系
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public UserProject getUserProjectById(int id) throws Exception {
        UserProject userProject = this.userProjectMapper.selectByPrimaryKey(id);
        return userProject;
    }

    /**
     * 根据用户名与项目id获取项目用户
     *
     * @param username
     * @param projectId
     * @return
     * @throws Exception
     */
    @Override
    public UserProject getUserProjectByUsernameAndProjectId(String username, String projectId) throws Exception {
        UserProjectExample example = this.getExample();
        example.createCriteria().andUsernameEqualTo(username).andProjectIdEqualTo(projectId);
        List<UserProject> userProjects = this.userProjectMapper.selectByExample(example);
        if (!userProjects.isEmpty()){
            return userProjects.get(0);
        }
        return null;
    }

    /**
     * 根据项目id获取项目用户列表
     *
     * @param projectId
     * @return
     * @throws Exception
     */
    @Override
    public List<UserProject> getUserProjectByProjectId(String projectId,Integer limit,Integer offset) throws Exception {
        UserProjectExample example = this.getExample();
        //分页查询
        if (null != limit){
            example.setLimit(limit);
        }
        if (null != offset){
            example.setOffset(offset);
        }
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<UserProject> userProjects = this.userProjectMapper.selectByExample(example);
        return userProjects;
    }

    /**
     * 根据项目id获取项目用户列表 不带分页
     *
     * @param projectId
     * @return
     * @throws Exception
     */
    @Override
    public List<UserProject> getUserProjectByProjectId(String projectId) throws Exception {
        List<UserProject> userProjectByProjectId = this.getUserProjectByProjectId(projectId, null, null);
        return userProjectByProjectId;
    }

    /**
     * 创建项目下用户
     *
     * @param userProject
     * @throws Exception
     */
    @Override
    public void createUserProject(UserProject userProject) throws Exception {
        UserProject userProjectById = this.getUserProjectByUsernameAndProjectId(userProject.getUsername(),userProject.getProjectId());
        if (!Objects.isNull(userProjectById)){
            throw new MarsRuntimeException(ErrorCodeMessage.USER_NOT_EXIST);
        }
        this.userProjectMapper.insertSelective(userProject);
    }

    /**
     * 更新项目下用户
     *
     * @param userProject
     * @throws Exception
     */
    @Override
    public void updateUserProject(UserProject userProject) throws Exception {
        UserProject userProjectById = this.getUserProjectById(userProject.getId());
        if (null == userProjectById){
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_USERNAME);
        }
        this.userProjectMapper.updateByPrimaryKeySelective(userProject);
    }

    /**
     * 根据id删除项目下用户
     *
     * @param id
     * @throws Exception
     */
    @Override
    public void deleteUserProjectById(Integer id) throws Exception {
        UserProject userProjectById = this.getUserProjectById(id);
        if (null == userProjectById){
            throw new MarsRuntimeException(ErrorCodeMessage.USER_NOT_EXIST);
        }
        this.userProjectMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据项目id删除用户
     *
     * @param projectId
     * @throws Exception
     */
    @Override
    public void deleteUserProjectByProjectId(String projectId) throws Exception {
        List<UserProject> userProjectByProjectId = this.getUserProjectByProjectId(projectId);
        if (!userProjectByProjectId.isEmpty()){
            UserProjectExample example = this.getExample();
            example.createCriteria().andProjectIdEqualTo(projectId);
            this.userProjectMapper.deleteByExample(example);
        }
    }
    private UserProjectExample getExample() throws Exception {
        UserProjectExample example = new UserProjectExample();
        return example;
    }
}
