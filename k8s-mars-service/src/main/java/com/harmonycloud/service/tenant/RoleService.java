package com.harmonycloud.service.tenant;

import java.util.List;
import java.util.Map;

import com.harmonycloud.dao.tenant.bean.RolePrivilege;
import com.harmonycloud.dao.user.bean.Role;

/**
 * Created by zgl on 17/8/9.
 */
public interface RoleService {
    /**
     * 获取role列表
     * @return
     * @throws Exception
     */
    public List<Role> getRoleList() throws Exception;
    /**
     * 根据角色名获取Role
     * @param roleName
     * @return
     * @throws Exception
     */
    public Role getRoleByRoleName(String roleName) throws Exception;
    /**
     * 启用角色
     * @param roleName
     * @throws Exception
     */
    public void EnableRoleByRoleName(String roleName) throws Exception;
    /**
     * 启用角色
     * @param roleName
     * @throws Exception
     */
    public void DisableRoleByRoleName(String roleName) throws Exception;
    /**
     * 添加角色
     * @param role
     * @throws Exception
     */
    public void addRole(Role role,List<Map<String, Object>> rolePrivilegeList) throws Exception;
    /**
     * 根据角色名删除角色
     * @param roleName
     * @throws Exception
     */
    public void deleteRole(String roleName) throws Exception;
    /**
     * 更新角色
     * @param resourceIds
     * @throws Exception
     */
    public void updateRole(Role role) throws Exception;
    /**
     * 根据用户名和tenantid获取角色
     * @param userName
     * @param tenantid
     * @return
     * @throws Exception
     */
    public Role getRoleByUserNameAndTenant(String userName,String tenantid) throws Exception;

    /**
     * 重置角色(保留1-5纪录)
     * @return
     * @throws Exception
     */
    public void resetRole() throws Exception;

}
