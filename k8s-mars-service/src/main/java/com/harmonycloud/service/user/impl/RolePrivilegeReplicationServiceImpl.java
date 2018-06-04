package com.harmonycloud.service.user.impl;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.tenant.RolePrivilegeCustomMapper;
import com.harmonycloud.dao.user.RolePrivilegeMapper;
import com.harmonycloud.dao.user.RolePrivilegeReplicationMapper;
import com.harmonycloud.dao.user.bean.*;
import com.harmonycloud.service.tenant.RoleService;
import com.harmonycloud.service.user.PrivilegeService;
import com.harmonycloud.service.user.ResourceService;
import com.harmonycloud.service.user.RolePrivilegeReplicationService;
import com.harmonycloud.service.user.RolePrivilegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zgl on 2018/1/3.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RolePrivilegeReplicationServiceImpl implements RolePrivilegeReplicationService {

    @Autowired
    private RolePrivilegeReplicationMapper rolePrivilegeReplicationMapper;

    /**
     * 根据roleId获取RolePrivilegeReplication列表
     *
     * @param roleId
     * @throws Exception
     */
    @Override
    public List<RolePrivilegeReplication> getRolePrivilegeReplicationByRoleId(Integer roleId) throws Exception {
        RolePrivilegeReplicationExample example = this.getExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        List<RolePrivilegeReplication> rolePrivileges = this.rolePrivilegeReplicationMapper.selectByExample(example);
        return rolePrivileges;
    }

    /**
     * 根据id获取RolePrivilegeReplication
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public RolePrivilegeReplication getRolePrivilegeReplicationById(Integer id) throws Exception {
        RolePrivilegeReplication rolePrivilegeReplication = this.rolePrivilegeReplicationMapper.selectByPrimaryKey(id);
        return rolePrivilegeReplication;
    }

    private RolePrivilegeReplicationExample getExample(){
        return new RolePrivilegeReplicationExample();
    }
}
