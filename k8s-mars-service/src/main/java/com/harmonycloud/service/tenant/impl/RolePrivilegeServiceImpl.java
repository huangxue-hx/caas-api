package com.harmonycloud.service.tenant.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.tenant.RolePrivilegeCustomMapper;
import com.harmonycloud.dao.tenant.RolePrivilegeMapper;
import com.harmonycloud.dao.tenant.bean.RolePrivilege;
import com.harmonycloud.dao.tenant.bean.RolePrivilegeCustom;
import com.harmonycloud.dao.tenant.bean.RolePrivilegeCustomExample;
import com.harmonycloud.dao.tenant.bean.RolePrivilegeExample;
import com.harmonycloud.dao.user.ResourceMapper;
import com.harmonycloud.dao.user.RoleMapper;
import com.harmonycloud.dao.user.bean.Resource;
import com.harmonycloud.dao.user.bean.ResourceExample;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.service.tenant.RolePrivilegeService;
import com.harmonycloud.service.tenant.RoleService;

import java.util.*;

import com.harmonycloud.service.user.ResourceService;
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
    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    ResourceService resourceService;
    @Autowired
    private RolePrivilegeCustomMapper rolePrivilegeCustomMapper;


    @Override
    public Map<String, Object> updateRolePrivilege(String roleName, List<Map<String, Object>> rolePrivilegeList) throws Exception {
        for (Map<String, Object> rolePrivilege : rolePrivilegeList) {
            Integer rpid = (Integer) rolePrivilege.get("rpid");
            Boolean status = (Boolean)rolePrivilege.get("status");
            RolePrivilege rolePrivilegeById = this.getRolePrivilegeByRpId(rpid, roleName);
            if(rolePrivilegeById==null){
                throw new MarsRuntimeException("rpid与角色名不匹配,请检测后重新尝试!");
            }
            if(status^rolePrivilegeById.getStatus()){
                rolePrivilegeById.setStatus(status);
                this.updateModule(rolePrivilegeById);
            }
        }
        updateRoleMenu(roleName);
        return null;
    }
    private Resource getRolePrivateByMark(String mark,String roleName){
        ResourceExample example = new ResourceExample ();
        example.createCriteria().andNameEqualTo(mark).andRoleEqualTo(roleName);
        List<Resource> selectByExample = this.resourceMapper.selectByExample(example);
        if (selectByExample.isEmpty()){
            return null;
        }
        return selectByExample.get(0);
    }
    @SuppressWarnings("unchecked")
    @Override
    public void updateRoleMenu(String roleName) throws Exception {
        List<RolePrivilege> moduleByParentId = this.getAllStatusModuleByParentId(0, roleName);
        for (RolePrivilege map : moduleByParentId) {
            Resource resource = this.getRolePrivateByMark(map.getMark().equals("租户管理")?"我的租户":map.getMark(),roleName);
            if(resource == null){
                throw new MarsRuntimeException("系统异常,权限数据格式错误,请联系管理员");
            }
            Boolean status = map.getStatus();
            if(status ^ resource.getAvailable()){
                this.resourceService.updateRoleMenuResource(resource.getId(), status);
            }
            List<RolePrivilege> moduleByParentIdSecond = this.getAllStatusModuleByParentId(map.getRpid(), roleName);
            for (RolePrivilege rolePrivilegeSecond : moduleByParentIdSecond) {
                Resource resourceSecond = this.getRolePrivateByMark(rolePrivilegeSecond.getMark().equals("租户管理")?"我的租户":rolePrivilegeSecond.getMark(),roleName);
                if(resourceSecond != null){
                    Boolean statusSecond = rolePrivilegeSecond.getStatus();
                    if(statusSecond ^ resourceSecond.getAvailable()){
                        this.resourceService.updateRoleMenuResource(resourceSecond.getId(), statusSecond);
                    }
                }
            }
        }
    }
    @Override
    public List<RolePrivilege> getAllStatusModuleByParentId(Integer parentId,String roleName) throws Exception {
        RolePrivilegeExample example = new RolePrivilegeExample ();
        example.createCriteria().andParentRpidEqualTo(parentId).andRoleEqualTo(roleName);
        List<RolePrivilege> list = rolePrivilegeMapper.selectByExample(example);
        return list;
    }
    @Override
    public RolePrivilege getRolePrivilegeByRpId(Integer rpid,String roleName) throws Exception {
        RolePrivilegeExample example = new RolePrivilegeExample ();
        example.createCriteria().andRpidEqualTo(rpid).andRoleEqualTo(roleName);
        List<RolePrivilege> list = rolePrivilegeMapper.selectByExample(example);
        if(!list.isEmpty()){
            return list.get(0);
        }
        return null;
    }

    @Override
    public void resetRolePrivilegeByRoleName(String roleName) throws Exception {
        if(CommonConstant.ADMIN.equals(roleName)){
            return;
        }
        Role roleByRoleName = roleService.getRoleByRoleName(roleName);
        roleByRoleName.setAvailable(Boolean.TRUE);
        roleByRoleName.setSecondResourceIds(null);
        roleService.updateRole(roleByRoleName);
        RolePrivilegeCustomExample example = new RolePrivilegeCustomExample();
        example.createCriteria().andRoleEqualTo(roleName);
        List<RolePrivilegeCustom> selectByExample = rolePrivilegeCustomMapper.selectByExample(example);
        if(selectByExample.isEmpty()){
            throw new MarsRuntimeException("系统异常,权限数据备份格式错误,请联系管理员");
        }
        HashMap<Integer, RolePrivilegeCustom> oldPrivilege = new HashMap<>();
        for (RolePrivilegeCustom rolePrivilegeCustom : selectByExample) {
            oldPrivilege.put(rolePrivilegeCustom.getRpid(), rolePrivilegeCustom);
        }
        RolePrivilegeExample example1 = new RolePrivilegeExample ();
        example1.createCriteria().andRoleEqualTo(roleName);
        List<RolePrivilege> selectByExample2 = rolePrivilegeMapper.selectByExample(example1);
        for (RolePrivilege rolePrivilege : selectByExample2) {
            rolePrivilege.setStatus(oldPrivilege.get(rolePrivilege.getRpid()).getStatus());
            rolePrivilege.setUpdateTime(new Date());
            rolePrivilege.setParentRpid(oldPrivilege.get(rolePrivilege.getRpid()).getParentRpid());
            this.updateModule(rolePrivilege);
        }
        updateRoleMenu(roleName);

    }

    @Override
    public Map<String, Object> getAllStatusPrivilegeMenuByRoleName(String roleName) throws Exception {

        Map<String, Object> privilegeByRole = this.getPrivilegeByRole(roleName,Boolean.TRUE);
        return privilegeByRole;
    }
    private Map<String, Object> getPrivilegeByRole(String roleName,Boolean isMenu) throws Exception {
        Map<String, Object> result = new HashMap<>();
        if(!StringUtils.isEmpty(roleName)){
            if(roleName.equals(CommonConstant.ADMIN)){
                return result;
            }
            Role role = roleService.getRoleByRoleName(roleName);
            if(role == null){
                throw new MarsRuntimeException("角色名:"+roleName+"不存在！");
            }
        }
        List<RolePrivilege> list = null;
        if(isMenu){
            if(roleName==null||roleName.isEmpty()){
                list = this.getAllStatusModuleByParentId(0,CommonConstant.DEFAULT);
            }else{
                list = this.getAllStatusModuleByParentId(0,roleName);
            }
        }else{
            list = this.getModuleByParentRpId(0,roleName);
        }
//        if(list.size()<=0){
//            throw new MarsRuntimeException("默认角色权限数据异常,请联系管理员！");
//        }
        for (RolePrivilege rolePrivilege : list) {
            dealWithPrivilege(rolePrivilege,result,isMenu);
        }
        return result;
    }
    private Map<String, Object> dealWithPrivilege(RolePrivilege rolePrivilege,Map<String, Object> result,Boolean isMenu) throws Exception {
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
            if(rolePrivilege.getParentRpid() != 0){
                RolePrivilege rolePrivilegeById = this.getRolePrivilegeByRpId(rolePrivilege.getParentRpid(), rolePrivilege.getRole());
                if(rolePrivilegeById != null){
                    result.put(CommonConstant.STATUS, rolePrivilegeById.getStatus());
                }
            }
            result.put(CommonConstant.RPID, rolePrivilege.getParentRpid());
            return result;
        }
        List<RolePrivilege> list = null;
        if(isMenu){
            list = this.getAllStatusModuleByParentId(rolePrivilege.getRpid(),rolePrivilege.getRole());
            if(list.size()<=0){
                list = this.getAllStatusModuleByParentId(rolePrivilege.getRpid(),CommonConstant.DEFAULT);
            }
        }else{
            list = this.getModuleByParentRpId(rolePrivilege.getId(),rolePrivilege.getRole());
        }
        Map<String, Object> sonMap = new HashMap<>();
        result.put(rolePrivilege.getFirstModule(), sonMap);

        if(rolePrivilege.getParentRpid() != 0){
            RolePrivilege rolePrivilegeById = this.getRolePrivilegeByRpId(rolePrivilege.getParentRpid(),rolePrivilege.getRole());
            if(rolePrivilegeById != null){
                result.put(CommonConstant.STATUS, rolePrivilegeById.getStatus());
            }
        }
        result.put(CommonConstant.RPID, rolePrivilege.getParentRpid());
        for (RolePrivilege rolePrivilege2 : list) {
            dealWithPrivilege(rolePrivilege2, sonMap,isMenu);
        }
        return result;
    }
    @Override
    public List<RolePrivilege> getRolePrivilegeByRoleName(String roleName) throws Exception {
        RolePrivilegeExample example = new RolePrivilegeExample ();
        example.createCriteria().andRoleEqualTo(roleName);
        List<RolePrivilege> list = rolePrivilegeMapper.selectByExample(example);
        return list;
    }

    @Override
    public List<RolePrivilege> getModuleByParentRpId(Integer parentRpId,String roleName) throws Exception {
        RolePrivilegeExample example = new RolePrivilegeExample ();
        example.createCriteria().andParentRpidEqualTo(parentRpId).andRoleEqualTo(roleName).andStatusEqualTo(Boolean.TRUE);
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
        List<RolePrivilege> list = this.getModuleByParentRpId(0,roleName);
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
        List<RolePrivilege> moduleByParentId = this.getModuleByParentRpId(rolePrivilege.getRpid(),rolePrivilege.getRole());
        Map<String, Object> sonMap = new HashMap<>();
        result.put(rolePrivilege.getFirstModule(), sonMap);
        for (RolePrivilege rolePrivilege2 : moduleByParentId) {
            dealWithPrivilege(rolePrivilege2, sonMap);
        }
        return result;
    }
}
