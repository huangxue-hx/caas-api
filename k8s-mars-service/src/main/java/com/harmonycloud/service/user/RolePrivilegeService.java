package com.harmonycloud.service.user;

import com.harmonycloud.common.enumm.HarborMemberEnum;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dao.user.bean.RolePrivilege;
import com.harmonycloud.dto.user.PrivilegeDto;

import java.util.List;
import java.util.Map;

/**
 * Created by zgl on 2017/8/10.
 */
public interface RolePrivilegeService {


    /**
     * 切换角色
     * @return
     * @throws Exception
     */
    public Map<String, Object> switchRole(Integer roleId) throws Exception;

    /**
     * 设置当前角色信息到session
     * @param role
     * @return
     * @throws Exception
     */
    Map<String, Object> setCurrentRoleInfo(Role role) throws Exception;

    /**
     * 更新对应角色的权限列表
     * @param roleId
     * @param rolePrivilegeList
     * @return
     * @throws Exception
     */
    public void updateRolePrivilege(Integer roleId,List<PrivilegeDto> rolePrivilegeList) throws Exception;

    /**
     * 获取当前角色的harbormember
     * @param roleId
     * @throws Exception
     */
    public HarborMemberEnum getHarborRole(Integer roleId) throws Exception;

    /**
     * 同步角色菜单
     * @param roleId
     * @param privilegeId
     * @param status
     * @throws Exception
     */
    public void syncRoleMenu(Integer roleId,Integer privilegeId,Boolean status) throws Exception;
    /**
     * 根据roleId获取已经被分配的权限
     * @param roleId
     * @throws Exception
     */
    public Map<String, Object> getAvailablePrivilegeByRoleId(Integer roleId) throws Exception;
    /**
     * 根据roleId获取所有权限 修改权限的时候加载角色所有权限使用
     * @param roleId
     * @throws Exception
     */
    public Map<String, Object> getAllPrivilegeByRoleId(Integer roleId) throws Exception;
    /**
     * 获取基本权限操作
     * @param
     * @return
     * @throws Exception
     */
    public Map<String, Object> getAllSystemPrivilege() throws Exception;

    /**
     * 创建角色权限
     * @param rolePrivilege
     * @throws Exception
     */
    public void createRolePrivilege(RolePrivilege rolePrivilege) throws Exception;
    /**
     * 根据roleId删除rolePrivilege
     * @param roleId
     * @throws Exception
     */
    public void deleteRolePrivilegeByRoleId(Integer roleId) throws Exception;

    /**
     * 更新角色权限
     * @param rolePrivilege
     * @throws Exception
     */
    public void updateRolePrivilege(RolePrivilege rolePrivilege) throws Exception;

    /**
     * 根据id删除rolePrivilege
     * @param id
     * @throws Exception
     */
    public void deleteRolePrivilegeById(Integer id) throws Exception;
    /**
     * 根据角色id获取RolePrivilege列表
     * @param roleId
     * @return
     */
    public List<RolePrivilege> getRolePrivilegeByRoleId(Integer roleId,Boolean available)throws Exception;

    /**
     * 根据角色id重置角色权限（只针对默认初始角色）
     * @param roleId
     * @throws Exception
     */
    public void resetRolePrivilegeByRoleId(Integer roleId)throws Exception;
}
