package com.harmonycloud.service.user;

import com.harmonycloud.dao.user.bean.LocalPrivilege;
import com.harmonycloud.dao.user.bean.LocalPrivilegeExample;

import java.util.List;

/**
 * 局部角色权限规则Service // created by czl
 */
public interface LocalPrivilegeService {

    /**
     * 根据角色id查询局部角色的权限规则
     *
     * @param id
     * @return
     */
    List<LocalPrivilege> listPrivilegeRuleByRoleId(Integer id);

    /**
     * 根据角色id，resourceType查询局部角色的权限规则
     *
     * @param roleId
     * @param resourceType
     *
     * @return
     */
    public List<LocalPrivilege> listRuleByResourceType(Integer roleId, String resourceType);

    /**
     * 创建局部角色权限规则
     *
     * @param LocalPrivilege
     * @return
     */
    int insert(LocalPrivilege LocalPrivilege);

    /**
     * 带条件查询局部角色权限规则
     *
     * @param condition
     * @return
     */
    List<LocalPrivilege> listLocalPrivileges(LocalPrivilegeExample condition);

    /**
     * 更新局部角色权限规则
     *
     * @param LocalPrivilege
     * @return
     */
    int update(LocalPrivilege LocalPrivilege);

    /**
     * 删除局部角色权限规则
     *
     * @param id
     * @return
     */
    int delete(Integer id);

    /**
     * 根据条件删除规则
     *
     * @param condition
     * @return
     */
    public int deleteByExample(LocalPrivilegeExample condition);

}
