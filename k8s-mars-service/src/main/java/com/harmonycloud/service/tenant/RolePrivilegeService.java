package com.harmonycloud.service.tenant;

import java.util.List;
import java.util.Map;

import com.harmonycloud.dao.tenant.bean.RolePrivilege;

/**
 * Created by zgl on 2017/8/10.
 */
public interface RolePrivilegeService {

    /**
     * 根据rolename重置权限
     * @param roleName
     * @throws Exception
     */
    public void resetRolePrivilegeByRoleName(String roleName) throws Exception;

    /**
     * 根据父id和角色名获取所有状态的权限列表
     * @param parentId
     * @param roleName
     * @return
     * @throws Exception
     */
    public List<RolePrivilege> getAllStatusModuleByParentId(Integer parentId,String roleName) throws Exception;

    /**
     * 更新角色菜单(仅供后台数据库同步使用)
     * @param roleName
     * @throws Exception
     */
    public void updateRoleMenu(String roleName) throws Exception;

    /**
     * 根据rpid获取rolePrivilege
     * @param id
     * @return
     * @throws Exception
     */
    public RolePrivilege getRolePrivilegeByRpId(Integer rpid,String roleName) throws Exception;

    /**
     * 更新对应角色的权限列表
     * @param roleName
     * @param rolePrivilegeList
     * @return
     * @throws Exception
     */
    public Map<String, Object> updateRolePrivilege(String roleName,List<Map<String, Object>> rolePrivilegeList) throws Exception;

    /**
     * 根据角色名获取所有状态的权限菜单
     * @param parentId
     * @param roleName
     * @return
     * @throws Exception
     */
    public Map<String, Object> getAllStatusPrivilegeMenuByRoleName(String roleName) throws Exception;

    /**
     * 根据rolename获取rolePrivilege
     * @param roleName
     * @return
     * @throws Exception
     */
    public List<RolePrivilege> getRolePrivilegeByRoleName(String roleName) throws Exception;
    /**
     * 根据parentid获取模块列表
     * @return
     * @throws Exception
     */
    public List<RolePrivilege> getModuleByParentId(Integer parentId,String roleName) throws Exception;
    /**
     * 添加rolePrivilege
     * @param rolePrivilege
     * @return
     * @throws Exception
     */
    public void addModule(RolePrivilege rolePrivilege) throws Exception;
    /**
     * 根据id删除rolePrivilege
     * @param id
     * @return
     * @throws Exception
     */
    public void deleteModuleById(Integer id) throws Exception;
    /**
     * 更新rolePrivilege
     * @param rolePrivilege
     * @return
     * @throws Exception
     */
    public void updateModule(RolePrivilege rolePrivilege) throws Exception;
    /**
     * 根据rolename获取权限
     * @param roleName
     * @throws Exception
     */
    public Map<String, Object> getPrivilegeByRole(String roleName) throws Exception;
}
