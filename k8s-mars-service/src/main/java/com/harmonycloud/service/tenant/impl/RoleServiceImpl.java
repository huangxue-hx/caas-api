package com.harmonycloud.service.tenant.impl;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.user.RoleMapper;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dao.user.bean.RoleExample;
import com.harmonycloud.service.tenant.RoleService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by zsl on 16/10/25.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 获取role列表
     * @return
     * @throws Exception
     */
    public List<Role> getRoleList() throws Exception {
        RoleExample example = new RoleExample();
        example.createCriteria().andAvailableEqualTo(true);
        List<Role> list = roleMapper.selectByExample(example);
        return list;
    }

    @Override
    public Role getRoleByRoleName(String roleName) throws Exception {
        RoleExample example = new RoleExample();
        example.createCriteria().andAvailableEqualTo(true).andNameEqualTo(roleName);
        List<Role> list = roleMapper.selectByExample(example);
        if(list != null && list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    @Override
    public void EnableRoleByRoleName(String roleName) throws Exception {
        RoleExample example = new RoleExample();
        example.createCriteria().andNameEqualTo(roleName);
        List<Role> list = roleMapper.selectByExample(example);
        if(list == null && list.size() <= 0){
            throw new MarsRuntimeException("启用的角色不存在");
        }
        Role role = list.get(0);
        role.setAvailable(true);
        roleMapper.updateByPrimaryKey(role);
    }

    @Override
    public void DisableRoleByRoleName(String roleName) throws Exception {
        RoleExample example = new RoleExample();
        example.createCriteria().andNameEqualTo(roleName);
        List<Role> list = roleMapper.selectByExample(example);
        if(list == null && list.size() <= 0){
            throw new MarsRuntimeException("禁用的角色不存在");
        }
        Role role = list.get(0);
        role.setAvailable(false);
        roleMapper.updateByPrimaryKey(role);
        
    }

    @Override
    public void addRole(Role role) throws Exception {
        roleMapper.insertSelective(role);
    }

    @Override
    public void deleteRole(String roleName) throws Exception {
        RoleExample example = new RoleExample();
        example.createCriteria().andNameEqualTo(roleName);
        roleMapper.deleteByExample(example);
    }

    @Override
    public void updateRole(Role role) throws Exception {
       roleMapper.updateByPrimaryKey(role);
    }
}
