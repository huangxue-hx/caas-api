package com.harmonycloud.service.tenant.impl;

import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.dao.user.RoleMapper;
import com.harmonycloud.dao.user.bean.*;
import com.harmonycloud.service.tenant.RoleService;
import com.harmonycloud.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * Created by zsl on 16/10/25.
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;
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

    /**
     * 根据角色id获取Role
     *
     * @param roleId
     * @return
     * @throws Exception
     */
    @Override
    public Role getRoleByRoleId(Integer roleId) throws Exception {
        return roleMapper.selectByPrimaryKey(roleId);
    }

    @Override
    public void addRole(Role role,List<Map<String, Object>> rolePrivilegeList) throws Exception {
//        Integer isAdmin = (Integer)session.getAttribute("isAdmin");
//        if(isAdmin!=1){
//            throw new MarsRuntimeException("管理员才能添加角色");
//        }
//        String roleName = role.getName();
//        RoleExample example = new RoleExample();
//        example.createCriteria().andNameEqualTo(roleName);
//        List<Role> list = roleMapper.selectByExample(example);
//        if(list != null && list.size() > 0 && list.get(0).getAvailable() == Boolean.TRUE){
//            throw new MarsRuntimeException("角色"+role.getName()+"已经存在");
//        }
//        RoleExample preexample2 = new RoleExample();
//        preexample2.createCriteria().andDescriptionEqualTo(role.getDescription());
//        List<Role> prelist2 = roleMapper.selectByExample(preexample2);
//        if(prelist2 != null && prelist2.size() > 0 && prelist2.get(0).getAvailable() == Boolean.TRUE){
//            throw new MarsRuntimeException("角色名"+role.getDescription()+"已经存在");
//        }
//
//        if(list != null && list.size() > 0 && list.get(0).getAvailable() == Boolean.FALSE){
//            Role role2 = list.get(0);
//            role2.setAvailable(role.getAvailable());
//            role2.setUpdateTime(role.getUpdateTime());
//            role2.setDescription(role.getDescription());
//            roleMapper.updateByPrimaryKeySelective(role2);
//        }else {
//            roleMapper.insertSelective(role);
//        }
//        resourceService.addNewRoleMenu(role.getName());
//        List<RolePrivilege> rolePrivilegeByRoleName = this.rolePrivilegeService.getRolePrivilegeByRoleName(CommonConstant.DEFAULT);
//        if(rolePrivilegeList==null || rolePrivilegeList.isEmpty()){
//            //没有初始权限，使用default权限
//            for (RolePrivilege rolePrivilege : rolePrivilegeByRoleName) {
//                rolePrivilege.setId(null);
//                rolePrivilege.setRole(roleName);
//                rolePrivilege.setUpdateTime(new Date());
//                this.rolePrivilegeService.addModule(rolePrivilege);
//            }
//            //TODO
//            InitClusterRole initClusterRole = JsonUtil.jsonToPojo(InitClusterRoleEnum.defaultRole.getJson(), InitClusterRole.class);
//            initClusterRole.setName(roleName);
//            roleService.createClusterRole(initClusterRole);
//        }else{
//            //有初始权限，更新初始权限
//            HashMap<Integer, Boolean> newPrivilege = new HashMap<>();
//            for (Map<String, Object> rolePrivilege : rolePrivilegeList) {
//                newPrivilege.put((Integer) rolePrivilege.get("rpid"), (Boolean) rolePrivilege.get("status"));
//            }
//            for (RolePrivilege rolePrivilege : rolePrivilegeByRoleName) {
//                rolePrivilege.setId(null);
//                rolePrivilege.setRole(roleName);
//                rolePrivilege.setUpdateTime(new Date());
//                if(newPrivilege.get(rolePrivilege.getRpid()) != null){
//                    rolePrivilege.setStatus(newPrivilege.get(rolePrivilege.getRpid()));
//                }
//                this.rolePrivilegeService.addModule(rolePrivilege);
//            }
//            rolePrivilegeService.updateRoleMenu(roleName);
//            //TODO
//            InitClusterRole initClusterRole = JsonUtil.jsonToPojo(InitClusterRoleEnum.defaultRole.getJson(), InitClusterRole.class);
//            initClusterRole.setName(roleName);
//            roleService.createClusterRole(initClusterRole);
//        }
    }

    @Override
    public void deleteRole(String roleName) throws Exception {

//        //删除角色绑定　rolebinding
//        List<UserRoleRelationship> users = userRoleRelationshipService.listUserByRoleName(roleName);
//        if (!users.isEmpty()){
//            throw new MarsRuntimeException("该角色下有用户绑定，请先解除绑定");
//        }
//
//        //删除用户角色关联
////        userTenantService.deleteUserByRoleName(roleName);
//
//        //删除角色
//        RoleExample example = new RoleExample();
//        example.createCriteria().andNameEqualTo(roleName);
//        roleMapper.deleteByExample(example);
//
//        //删除角色权限
//        RolePrivilegeExample rolePrivilegeExample = new RolePrivilegeExample();
//        rolePrivilegeExample.createCriteria().andRoleEqualTo(roleName);
//        rolePrivilegeMapper.deleteByExample(rolePrivilegeExample);
//
//        //删除角色菜单资源
//        ResourceExample resourceExample = new ResourceExample();
//        resourceExample.createCriteria().andRoleEqualTo(roleName);
//        resourceMapper.deleteByExample(resourceExample);
//        roleService.deleteClusterrole(roleName);


    }

    @Override
    public void updateRole(Role role) throws Exception {
       roleMapper.updateByPrimaryKey(role);
    }

    @Override
    public Role getRoleByUserNameAndTenant(String userName, String projectId) throws Exception {
        AssertUtil.notBlank(userName, DictEnum.USERNAME);
        User user = userService.getUser(userName);
        Role role = null;
        if (user.getIsAdmin() == 1) {
            role = this.getRoleByRoleName("admin");
        } else {
            //TODO  其他角色后续角色部分做
//            if(StringUtils.isEmpty(tenantid)){
//                throw new MarsRuntimeException("租户id不能为空");
//            }
//            UserTenant userTenant = userTenantService.getUserByUserNameAndTenantid(userName, tenantid);
//            if(userTenant == null){
//                throw new MarsRuntimeException("用户名与所在的租户不匹配");
//            }
//            role = this.getRoleByRoleName(userTenant.getRole());
        }
        return role;
    }

}
