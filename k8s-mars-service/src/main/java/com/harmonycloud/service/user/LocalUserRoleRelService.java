package com.harmonycloud.service.user;

import com.harmonycloud.dao.user.bean.LocalUserRoleRel;
import com.harmonycloud.dao.user.bean.LocalUserRoleRelExample;

import java.util.List;

/**
 * 用户角色关系Service // created by czl
 */
public interface LocalUserRoleRelService {

    /**
     * 用户是否存在局部角色
     *
     * @param projectId
     * @param userName
     * @return
     */
    boolean hasLocalRole(String projectId, String userName);

    /**
     * 创建用户局部角色关系
     *
     * @param localRoleRel
     * @return
     */
    int insert(LocalUserRoleRel localRoleRel);

    /**
     * 批量创建用户局部角色关系
     *
     * @param localRoleRels
     * @return
     */
    int insert(List<LocalUserRoleRel> localRoleRels);

    /**
     * 带条件查询用户局部角色关系
     *
     * @param locarRoleRel
     * @return
     */
    List<LocalUserRoleRel> listLocalUserRoleRels(LocalUserRoleRelExample locarRoleRel);

    /**
     * 更新用户局部角色关系
     *
     * @param locarRoleRel
     * @return
     */
    int update(LocalUserRoleRel locarRoleRel);

    /**
     * 删除用户局部角色关系
     *
     * @param id
     * @return
     */
    int delete(Integer id);

    /**
     * 删除某个角色的用户局部角色关系
     *
     * @param localRoleId
     * @return
     */
    int delete(String projectId, Integer localRoleId);

}
