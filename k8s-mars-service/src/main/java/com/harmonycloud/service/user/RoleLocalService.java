package com.harmonycloud.service.user;
import com.harmonycloud.common.enumm.HarborMemberEnum;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dto.cluster.DataCenterDto;
import com.harmonycloud.dto.user.RoleDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;

import java.util.List;
import java.util.Set;

/**
 * 角色接口
 *
 * @author zgl
 * @date 2017年12月8日
 */
public interface RoleLocalService {

    /**
     * 获取所有的角色列表
     * @return
     * @throws Exception
     */
    public List<Role> getAllRoleList() throws Exception;
    /**
     * 获取所有可用的角色列表
     * @return
     * @throws Exception
     */
    public List<Role> getAvailableRoleList() throws Exception;

    /**
     * 获取某用户的角色列表
     * @param username
     * @return
     * @throws Exception
     */
    public List<Role> getRoleListByUsername(String username) throws Exception;
    /**
     * 获取项目下某用户的角色列表
     * @param username
     * @param projectId
     * @return
     * @throws Exception
     */
    public List<Role> getRoleListByUsernameAndTenantIdAndProjectId(String username,String tenantId,String projectId) throws Exception;

    /**
     * 获取租户下某用户的角色列表
     * @param username
     * @param tenantId
     * @return
     * @throws Exception
     */
    public List<Role> getRoleListByUsernameAndTenantId(String username,String tenantId) throws Exception;

    /**
     * 获取某用户所在的角色作用域（集群）列表
     * @param username
     * @return
     * @throws Exception
     */
    public List<Cluster> getClusterListByUsername(String username) throws Exception;

    /**
     * 获取角色id获取集群列表
     * @param roleId
     * @return
     * @throws Exception
     */
    public List<Cluster> getClusterListByRoleId(Integer roleId) throws Exception;

    /**
     * 获取当前用户角色的可操作集群列表
     * @return
     * @throws Exception
     */
    public List<Cluster> listCurrentUserRoleCluster() throws Exception;

    /**
     * 获取当前用户角色的可操作集群id列表
     * @return
     * @throws Exception
     */
    public List<String> listCurrentUserRoleClusterIds() throws Exception;

    /**
     * 获取当前用户选择的数据中心
     * @return
     * @throws Exception
     */
    String getCurrentDataCenter() throws Exception;

    /**
     * 根据id获取角色
     * @param id
     * @return
     * @throws Exception
     */
    public Role getRoleById(Integer id) throws Exception;

    /**
     * 根据昵称获取角色
     * @param nickName
     * @return
     * @throws Exception
     */
    public Role getRoleByNickName(String nickName) throws Exception;

    /**
     * 创建角色
     * @param roleDto
     * @throws Exception
     */
    public void createRole(RoleDto roleDto) throws Exception;
    /**
     * 根据角色名删除角色
     * @param roleName
     * @throws Exception
     */
    public void deleteRoleByRoleName(String roleName) throws Exception;

    /**
     * 根据昵称删除角色
     * @param nickName
     * @throws Exception
     */
    public void deleteRoleByNickName(String nickName) throws Exception;

    /**
     * 根据id删除角色
     * @param id
     * @throws Exception
     */
    public void deleteRoleById(Integer id) throws Exception;
    /**
     * 修改角色
     * @param roleDto
     * @throws Exception
     */
    public void updateRole(RoleDto roleDto) throws Exception;

    /**
     * 更新角色
     * @param role
     * @throws Exception
     */
    public void updateRole(Role role) throws Exception;
    /**
     * 根据角色名获取角色
     * @param roleName
     * @throws Exception
     */
    public Role getRoleByRoleName(String roleName) throws Exception;
    /**
     * 启用角色
     * @param roleId
     * @param clusterIds 作用域
     * @throws Exception
     */
    public void enableRoleByRoleId(Integer roleId,String clusterIds) throws Exception;
    /**
     * 禁用角色
     * @param roleId
     * @throws Exception
     */
    public void disableRoleByRoleId(Integer roleId) throws Exception;

    /**
     * 处理添加用户harbor的角色权限
     * @param targetMember
     * @param projectId
     * @param username
     * @param roleId
     * @throws Exception
     */
    public void addHarborUserRole(HarborMemberEnum targetMember, String projectId, String username, Integer roleId) throws Exception;

    /**
     * 更新用户harbor的角色权限
     * @param targetMember
     * @param projectId
     * @param userName
     * @throws Exception
     */
    public void updateHarborUserRole(HarborMemberEnum targetMember,String projectId,String userName)throws Exception;
    /**
     * 根据角色id复制新角色
     * @param roleId
     * @param nickName
     * @throws Exception
     */
    public void copyRoleByRoleId(Integer roleId,String nickName) throws Exception;
    /**
     * 删除集群时将角色的作用域的集群删除
     * @param clusterId
     * @throws Exception
     */
    int deleteRoleCluster(String clusterId) throws Exception;

    /**
     * 初始化harbor用户角色
     * @throws Exception
     */
    public void initHarborRole() throws Exception;


    /**
     * 切换数据中心
     * @return
     * @throws Exception
     */
    void switchDataCenter(Integer roleId, String dataCenter) throws Exception;

    /**
     * 切换数据中心
     * @return
     * @throws Exception
     */
    Set<DataCenterDto> getRoleDataCenter(Integer roleId) throws Exception;
}
