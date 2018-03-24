package com.harmonycloud.service.user;

import com.harmonycloud.dao.user.bean.ResourceMenu;
import com.harmonycloud.dao.user.bean.ResourceMenuRole;
import com.harmonycloud.dto.user.MenuDto;

import java.util.List;
import java.util.Map;

public interface ResourceMenuRoleService {
    /**
     * 根据角色id获取系统菜单列表
     * @return
     * @throws Exception
     */
    public List<MenuDto> getResourceMenuByRoleId(Integer roleId) throws Exception;

    /**
     * 根据角色id获取ResourceMenuRole列表
     * @param roleId
     * @return
     * @throws Exception
     */
    public List<ResourceMenuRole> listResourceMenuRole(Integer roleId) throws Exception;

    /**
     * 根据角色id获取菜单id为key的map
     * @return
     * @throws Exception
     */
    public Map<Integer, ResourceMenuRole> getResourceMenuIdsByRoleId(Integer roleId) throws Exception;

    /**
     * 根据角色id删除角色菜单
     * @param roleId
     * @throws Exception
     */
    public void deleteResourceMenuRoleByRoleId(Integer roleId) throws Exception;

    /**
     * 新增角色菜单
     * @param roleId
     * @throws Exception
     */
    public void createResourceMenuRole(Integer roleId) throws Exception;

    /**
     * 新增角色菜单
     * @param resourceMenuRole
     * @throws Exception
     */
    public void createResourceMenuRoleNative(ResourceMenuRole resourceMenuRole) throws Exception;
    /**
     * 更新角色菜单状态
     * @param roleId
     * @param rmid
     * @param status
     * @throws Exception
     */
    public void updateResourceMenuRole(Integer roleId,Integer rmid,Boolean status) throws Exception;

    /**
     * 更新角色菜单状态
     * @param resourceMenuRole
     * @throws Exception
     */
    public void updateResourceMenuRole(ResourceMenuRole resourceMenuRole) throws Exception;

    /**
     * 根据角色id与rmid获取ResourceMenuRole
     * @param roleId
     * @param rmid
     * @return
     * @throws Exception
     */
    public ResourceMenuRole getResourceMenuRole(Integer roleId,Integer rmid) throws Exception;

    /**
     * 获取角色id下租户列表，我的租户，我的项目菜单map
     * @param roleId
     * @return
     * @throws Exception
     */
    public Map<Integer,ResourceMenuRole> getResourceTenantMenuRole(Integer roleId) throws Exception;

    public ResourceMenuRole getResourceMenuRoleById(Integer id) throws Exception;
}
