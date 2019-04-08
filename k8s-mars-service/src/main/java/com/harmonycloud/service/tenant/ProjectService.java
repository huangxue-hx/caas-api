package com.harmonycloud.service.tenant;

import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dao.user.bean.UserRoleRelationship;
import com.harmonycloud.dto.tenant.DevOpsProjectUserDto;
import com.harmonycloud.dto.tenant.ProjectDto;
import com.harmonycloud.dto.user.UserRoleDto;

import java.util.List;
import java.util.Map;

/**
 * Created by zgl on 17-12-10.
 */


/**
 * 项目业务接口
 */
public interface ProjectService {
    /**
     * 切换项目
     * @param tenantId
     * @param projectId
     * @return
     * @throws Exception
     */
    List<Role> switchProject(String tenantId, String projectId) throws Exception;
    /**
     * 创建项目
     * @param projectDto
     * @throws Exception
     */
    public String createProject(ProjectDto projectDto) throws Exception;

    /**
     * 根据项目id删除项目
     * @param projectId
     * @throws Exception
     */
    public void deleteProjectByProjectId(String tenantId,String projectId) throws Exception;

    /**
     * 根据主键id删除项目
     * @param id
     * @throws Exception
     */
    public void deleteProjectById(Integer id) throws Exception;

    /**
     * 更新项目
     * @param projectDto
     * @throws Exception
     */
    public void updateProject(ProjectDto projectDto) throws Exception;

    /**
     * 根据租户id查询租户下所有项目列表 不带分页
     * @return
     * @throws Exception
     */
    public List<ProjectDto> listTenantProjectByTenantid(String tenantId) throws Exception;
    /**
     * 根据租户id查询租户下所有项目列表 不带分页(内部接口调用)
     * @return
     * @throws Exception
     */
    public List<Project> listTenantProjectByTenantidInner(String tenantId) throws Exception;
    /**
     * 根据租户id查询租户下所有项目列表 带分页
     * @return
     * @throws Exception
     */
    public List<ProjectDto> listTenantProjectByTenantid(String tenantId,Integer limit,Integer offset) throws Exception;

    /**
     * 根据租户id与用户名查询租户下所有项目列表(返回值带镜像仓库，应用数量)
     * @param tenantId
     * @param username
     * @return
     * @throws Exception
     */
    public List<ProjectDto> listTenantProjectByUsernameDetail(String tenantId ,String username) throws Exception;
    /**
     * 获取所有项目列表
     * @return
     * @throws Exception
     */
    public List<Project> listAllProject() throws Exception;

    /**
     * 根据用户名与租户id获取租户下的项目列表
     * @param username,tenantId
     * @return
     * @throws Exception
     */
    public List<Project> listTenantProjectByUsername(String tenantId ,String username) throws Exception;
    /**
     * 根据项目id查询项目
     * @param
     * @return
     */
    public Project getProjectByProjectId(String projectId) throws Exception;
    /**
     * 根据项目id查询项目详情
     * @param
     * @return
     */
    public ProjectDto getProjectDetailByProjectId(String tenantId,String projectId) throws Exception;

    /**
     * 根据项目名查询项目
     * @param projectName
     * @return
     * @throws Exception
     */
    public Project getProjectByProjectName(String projectName) throws Exception;

    /**
     * 根据aliasName查询项目
     * @param aliasName
     * @return
     * @throws Exception
     */
    public Project getProjectByAliasName(String aliasName) throws Exception;

    /**
     * 根据主键id查询项目
     * @param id
     * @return
     * @throws Exception
     */
    public Project getProjectById(Integer id) throws Exception;

    /**
     * 根据项目id查询项目管理员
     * @param projectId
     * @return
     * @throws Exception
     */
    public List<UserRoleRelationship> listProjectPm(String tenantId,String projectId) throws Exception;

    /**
     * 根据项目id查询项目成员
     * @param tenantId
     * @param projectId
     * @return
     * @throws Exception
     */
    public List<Map<String,Object>> listProjectUser(String tenantId, String projectId) throws Exception;
    /**
     * 向项目下添加项目管理员
     * @return
     * @throws Exception
     */
    public void createPm(String tenantId,String projectId, List<String> pmList) throws Exception;
    /**
     * 向项目下删除项目管理员
     * @return
     * @throws Exception
     */
    public void deletePm(String tenantId,String projectId, String username,Boolean isCDPOperate) throws Exception;

    /**
     * 添加项目中用户角色
     * @param userRoleDto
     * @throws Exception
     */
    public void addUserRole(UserRoleDto userRoleDto) throws Exception;

    /**
     * 删除项目中用户角色
     * @param userRoleDto
     * @throws Exception
     */
    public void removeUserRole(UserRoleDto userRoleDto) throws Exception;
    /**
     * devops平台向内添加用户
     * @param devOpsProjectUserDto
     * @throws Exception
     */
    public void syncDevOpsUser(DevOpsProjectUserDto devOpsProjectUserDto) throws Exception;

    public Boolean isPm(String tenantId,String projectId,String username) throws Exception;

    String getProjectNameByProjectId(String projectId) throws Exception;
}
