package com.harmonycloud.service.user;

import com.harmonycloud.dao.user.bean.LocalRolePrivilege;
import com.harmonycloud.dao.user.bean.LocalRolePrivilegeExample;

import java.util.List;

/**
 * 局部角色权限实例Service // created by czl
 */
public interface LocalRolePrivilegeService {

    /**
     * 创建局部角色权限实例
     *
     * @param localRolePrivilege
     * @return
     */
    int insert(LocalRolePrivilege localRolePrivilege);

    /**
     * 带条件查询局部角色权限实例
     *
     * @param condition
     * @return
     */
    List<LocalRolePrivilege> listLocalRolePrivileges(LocalRolePrivilegeExample condition);

    /**
     * 更新局部权限角色实例
     *
     * @param localRolePrivilege
     * @return
     */
    int update(LocalRolePrivilege localRolePrivilege);

    /**
     * 删除局部权限角色实例
     *
     * @param id
     * @return
     */
    int delete(Integer id);

    /**
     * 根据条件删除权限实例
     *
     * @param condition
     * @return
     */
    public int deleteByExample(LocalRolePrivilegeExample condition);

    /**
     * 根据id查询权限实例
     *
     * @param id
     * @return
     */
    public LocalRolePrivilege get(Integer id);

    /**
     * 带条件查询局部角色权限实例
     *
     * @return
     */
    public List<LocalRolePrivilege> listAllLocalRolePrivileges();
}
