package com.harmonycloud.service.tenant.impl;

import com.harmonycloud.dao.user.RoleMapper;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.service.tenant.RoleService;

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

    @Override
    public Role findByName(String roleName) throws Exception{
        return roleMapper.findByName(roleName);
    }
}
