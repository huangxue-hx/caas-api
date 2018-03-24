package com.harmonycloud.service.user;

import com.harmonycloud.dao.user.bean.RolePrivilegeReplication;

import java.util.List;

/**
 * Created by zgl on 2018/1/3.
 */
public interface RolePrivilegeReplicationService {

    /**
     * 根据roleId获取RolePrivilegeReplication列表
     * @param roleId
     * @throws Exception
     */
    public List<RolePrivilegeReplication> getRolePrivilegeReplicationByRoleId(Integer roleId) throws Exception;

    /**
     * 根据d获取RolePrivilegeReplication
     * @param id
     * @return
     * @throws Exception
     */
    public RolePrivilegeReplication getRolePrivilegeReplicationById(Integer id) throws Exception;
}
