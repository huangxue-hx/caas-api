package com.harmonycloud.service.tenant.impl;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.tenant.bean.UserTenant;
import com.harmonycloud.dao.user.RoleMapper;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dao.user.bean.RoleExample;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.service.tenant.RoleService;
import com.harmonycloud.service.tenant.UserTenantService;
import com.harmonycloud.service.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Created by zsl on 16/10/25.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserTenantService userTenantService;
    @Autowired
    private UserService userService;

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

    @Override
    public Role getRoleByUserNameAndTenant(String userName, String tenantid) throws Exception {
        if(StringUtils.isEmpty(userName)){
            throw new MarsRuntimeException("用户名不能为空");
        }
        User user = userService.getUser(userName);
        Role role = null;
        if (user.getIsAdmin() == 1) {
            role = this.getRoleByRoleName("admin");
        } else {
            if(StringUtils.isEmpty(tenantid)){
                throw new MarsRuntimeException("租户id不能为空");
            }
            UserTenant userTenant = userTenantService.getUserByUserNameAndTenantid(userName, tenantid);
            if(userTenant == null){
                throw new MarsRuntimeException("用户名与所在的租户不匹配");
            }
            role = this.getRoleByRoleName(userTenant.getRole());
        }
        return role;
    }
    
}
