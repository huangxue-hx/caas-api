package com.harmonycloud.service.tenant.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.tenant.RolePrivilegeMapper;
import com.harmonycloud.dao.tenant.bean.RolePrivilege;
import com.harmonycloud.dao.tenant.bean.RolePrivilegeExample;
import com.harmonycloud.dao.user.RoleMapper;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.service.tenant.RolePrivilegeService;
import com.harmonycloud.service.tenant.RoleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Created by zgl on 2017/8/10.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RolePrivilegeServiceImpl implements RolePrivilegeService {

    @Autowired
    private RolePrivilegeMapper rolePrivilegeMapper;
    @Autowired
    RoleService roleService;

    @Override
    public List<RolePrivilege> getModuleByParentId(Integer parentId,String roleName) throws Exception {
        RolePrivilegeExample example = new RolePrivilegeExample ();
        example.createCriteria().andParentidEqualTo(parentId).andRoleEqualTo(roleName);
        List<RolePrivilege> list = rolePrivilegeMapper.selectByExample(example);
        return list;
    }

    @Override
    public void addModule(RolePrivilege rolePrivilege) throws Exception {
        rolePrivilegeMapper.insertSelective(rolePrivilege);
    }

    @Override
    public void deleteModuleById(Integer id) throws Exception {
        rolePrivilegeMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void updateModule(RolePrivilege rolePrivilege) throws Exception {
        rolePrivilegeMapper.updateByPrimaryKeySelective(rolePrivilege);
    }

    @Override
    public Map<String, Object> getPrivilegeByRole(String roleName) throws Exception {
        if(StringUtils.isEmpty(roleName)){
            throw new MarsRuntimeException("角色名不能为空！");
        }
        Role role = roleService.getRoleByRoleName(roleName);
        if(role == null){
            throw new MarsRuntimeException("角色名:"+roleName+"不存在！");
        }
        Map<String, Object> result = new HashMap<>();
        List<RolePrivilege> list = this.getModuleByParentId(0,roleName);
        if(list.size()<=0){
            return result;
        }
        for (RolePrivilege rolePrivilege : list) {
            dealWithPrivilege(rolePrivilege,result);
        }
        return result;
    }
    @SuppressWarnings("unchecked")
    private Map<String, Object> dealWithPrivilege(RolePrivilege rolePrivilege,Map<String, Object> result) throws Exception {
        if(!rolePrivilege.getIsparent()){
            List <Object> privlege = null;
            if(result.get(CommonConstant.PRIVILEGE)!=null){
                privlege = (List <Object>)result.get(CommonConstant.PRIVILEGE);
                privlege.add(rolePrivilege);
                result.put(CommonConstant.PRIVILEGE, privlege);
            }else{
                privlege = new ArrayList<>();
                privlege.add(rolePrivilege);
                result.put(CommonConstant.PRIVILEGE, privlege);
            }
            
            return result;
        }
        List<RolePrivilege> moduleByParentId = this.getModuleByParentId(rolePrivilege.getId(),rolePrivilege.getRole());
        Map<String, Object> sonMap = new HashMap<>();
        result.put(rolePrivilege.getFirstModule(), sonMap);
        for (RolePrivilege rolePrivilege2 : moduleByParentId) {
            dealWithPrivilege(rolePrivilege2, sonMap);
        }
        return result;
    }
}
