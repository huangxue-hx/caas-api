package com.harmonycloud.service.user;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.user.bean.UserRoleRelationship;
import java.util.List;
import java.util.Map;

/**
 *
 * @Title
 * @author zgl
 * @date 2017年12月18日
 * @Description
 * @version V1.0
 */
public interface UserRoleRelationshipService {
    /**
     * 根据用户名获取所有角色
     * @return
     * @throws Exception
     */
    public List<Map> getRoleByUsername(String username) throws Exception;
    /**
     * 根据用户名获取当前用户在当前项目下的所有角色
     * @return
     * @throws Exception
     */
    public List<Map> getRoleByUsernameAndProject(String username,String projectId) throws Exception;
    /**
     * 根据用户名获取当前用户在当前租户下的所有角色
     * @return
     * @throws Exception
     */
    public List<Map> getRoleByUsernameAndTenantId(String username,String tenantId) throws Exception;

    /**
     * 根据用户名查询用户是否有角色
     * @param username
     * @return
     * @throws Exception
     */
    public Boolean hasRole(String username) throws Exception;
    /**
     * 根据id获取用户角色关系
     * @return
     * @throws Exception
     */
    public UserRoleRelationship getUserRoleRelationshipById(int id) throws Exception;
    /**
     * 根据用户名与项目id获取用户角色关系列表
     * @return
     * @throws Exception
     */
    public List<UserRoleRelationship> getUserRoleRelationshipByUsernameAndProjectId(String username,String projectId) throws Exception;
    /**
     * 创建用户角色关系
     * @param userRoleRelationship
     * @throws Exception
     */
    public void createUserRoleRelationship(UserRoleRelationship userRoleRelationship) throws Exception;
    /**
     * 更新用户角色关系
     * @param userRoleRelationship
     * @throws Exception
     */
    public void updateUserRoleRelationship(UserRoleRelationship userRoleRelationship) throws Exception;

    /**
     * 将项目下的指定成员的是否有局部角色改成true
     * @param projectId
     * @param userNames
     */
    void setLocalRoleFlag(String projectId, List<String> userNames);
    /**
     * 根据id删除用户角色关系
     * @param id
     * @throws Exception
     */
    public void deleteUserRoleRelationshipById(Integer id) throws Exception;

    /**
     * 根据租户id删除用户角色关系
     * @param tenantId
     * @throws Exception
     */
    public void deleteUserRoleRelationshipByTenantId(String tenantId) throws Exception;

    /**
     * 根据项目id删除用户角色关系
     * @param projectId
     * @throws Exception
     */
    public void deleteUserRoleRelationshipByProjectId(String projectId) throws Exception;
    /**
     * 根据用户获取当前用户租户列表
     * @param username
     * @throws Exception
     */
    public List<TenantBinding> listTenantByUsername(String username) throws Exception;

    /**
     * 根据租户id与用户名获取当前用户project列表
     * @param tenantId
     * @param username
     * @return
     * @throws Exception
     */
    public List<Project> listProjectByTenantIdAndUsername(String tenantId, String username) throws Exception;
    /**
     * 根据角色名获取项目下的所有用户列表
     * @param roleId,projectId
     * @throws Exception
     */
    public List<UserRoleRelationship> listUserByRoleNameAndProjectId(int roleId,String projectId) throws Exception;

    /**
     * 根据租户id获取租户下的所有租户管理员用户列表
     * @param tenantId
     * @return
     * @throws Exception
     */
    public List<UserRoleRelationship> listTmByTenantId(String tenantId) throws Exception;
    /**
     * 根据租户id与用户名获取租户下的租户管理员用户
     * @param tenantId
     * @return
     * @throws Exception
     */
    public UserRoleRelationship getTmByTenantIdAndUsername(String tenantId,String username) throws Exception;
    /**
     * 根据tenantid获取租户下的所有用户列表
     * @param tenantId
     * @throws Exception
     */
    public List<String> listUserByTenantId(String tenantId) throws Exception;

    /**
     * 根据roleName获取租户下的所有用户列表
     * @param roleName
     * @return
     * @throws Exception
     */
    public List<UserRoleRelationship> listUserByNickName(String roleName) throws Exception;

    /**
     * 根据roleId获取租户下的所有用户列表
     * @param roleId
     * @return
     * @throws Exception
     */
    public List<UserRoleRelationship> listUserByRoleId(Integer roleId) throws Exception;

    /**
     * 根据projectId获取项目下的所有用户列表
     * @param projectId
     * @return
     * @throws Exception
     */
    public List<UserRoleRelationship> listUserByProjectId(String projectId) throws Exception;

    /**
     * 根据projectId获取项目下的所有的项目管理员用户列表
     * @param projectId
     * @return
     * @throws Exception
     */
    public List<UserRoleRelationship> listPmByTenantAndProjectId(String tenantId,String projectId) throws Exception;
    /**
     * 判断用户是否为租户下的管理员
     * @param username
     * @return
     * @throws Exception
     */
    public Boolean isTmUser(String tenantId,String username,Integer roleId) throws Exception;
    /**
     * 根据租户id与用户名获取租户下的租户管理员用户
     * @param projectId,username
     * @return
     * @throws Exception
     */
    public UserRoleRelationship getPmByProjectIdAndUsername(String projectId,String username) throws Exception;

    /**
     * 根据项目id，用户名，角色id获取用户角色
     * @param projectId
     * @param username
     * @param roleId
     * @return
     * @throws Exception
     */
    public UserRoleRelationship getUser(String projectId,String username,Integer roleId) throws Exception;

    /**
     * 根据用户名与角色id获取UserRoleRelationship列表
     * @param username
     * @param roleId
     * @return
     * @throws Exception
     */
    public List<UserRoleRelationship> getUserRoleRelationshipList(String username,Integer roleId) throws Exception;

    /**
     * 根据租户id与用户名获取用户关系列表
     * @param username
     * @param tenantId
     * @return
     * @throws Exception
     */
    public List<UserRoleRelationship> getUserRoleRelationshipList(String username,String tenantId) throws Exception;
    /**
     * 根据用户名删除
     * @param userName
     * @return
     * @throws Exception
     */
    public void deleteUserRoleRelationshipByProjectUserName(String userName) throws Exception;

}
