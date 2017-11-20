package com.harmonycloud.service.tenant.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.tenant.RolePrivilegeMapper;
import com.harmonycloud.dao.tenant.bean.RolePrivilege;
import com.harmonycloud.dao.tenant.bean.RolePrivilegeExample;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.UserTenant;
import com.harmonycloud.dao.user.ResourceMapper;
import com.harmonycloud.dao.user.RoleMapper;
import com.harmonycloud.dao.user.bean.*;
import com.harmonycloud.k8s.service.RoleBindingService;
import com.harmonycloud.service.tenant.RolePrivilegeService;
import com.harmonycloud.service.tenant.RoleService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.tenant.UserTenantService;
import com.harmonycloud.service.user.ResourceService;
import com.harmonycloud.service.user.UserService;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;

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
    @Autowired
    private HttpSession session;
    @Autowired
    private RolePrivilegeService rolePrivilegeService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    private RolePrivilegeMapper rolePrivilegeMapper;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private RoleBindingService roleBindingService;
    @Autowired
    private com.harmonycloud.service.user.RoleService roleService;


    /**
     * 禁用角色
     * @param roleName
     * @throws Exception
     */
    public void disableRoleByRoleName(String roleName) throws Exception {
        RoleExample example = new RoleExample();
        example.createCriteria().andNameEqualTo(roleName);
        List<Role> list = roleMapper.selectByExample(example);
        if(list == null && list.size() <= 0){
            throw new MarsRuntimeException("禁用的角色不存在");
        }
        Role role = list.get(0);
        List<UserTenant> findUserByRoleName = userTenantService.findUserByRoleName(roleName);
        if(findUserByRoleName != null && findUserByRoleName.size() > 0){
            throw new MarsRuntimeException("角色:"+roleName+",还有用户绑定,请先解除绑定后再操作!");
        }
        role.setSecondResourceIds("pause");
        roleMapper.updateByPrimaryKey(role);
    }

    public void resetRole() throws Exception {
        RoleExample example = new RoleExample();
        example.createCriteria().andIdBetween(2, 5);
        Role record = new Role();
        record.setAvailable(Boolean.TRUE);
        record.setSecondResourceIds(null);
        roleMapper.updateByExampleSelective(record, example);
        List<Role> selectByExample = roleMapper.selectByExample(example);
      for (Role role : selectByExample) {
        	    //reset privilege
			rolePrivilegeService.resetRolePrivilegeByRoleName(role.getName());
       }
        RoleExample example1 = new RoleExample();
        example1.createCriteria().andIdGreaterThan(5);
        List<Role> deleteList = roleMapper.selectByExample(example1);
        for(Role role:deleteList){
            ResourceExample resourceExample = new ResourceExample();
            resourceExample.createCriteria().andRoleEqualTo(role.getName());
            resourceMapper.deleteByExample(resourceExample);
            RolePrivilegeExample rolePrivilegeExample = new RolePrivilegeExample();
            rolePrivilegeExample.createCriteria().andRoleEqualTo(role.getName());
            rolePrivilegeMapper.deleteByExample(rolePrivilegeExample);
        }
        roleMapper.deleteByExample(example1);
        List<Role> roleList = this.getRoleList();
        for (Role role : roleList) {
            rolePrivilegeService.resetRolePrivilegeByRoleName(role.getName());
        }
    }
    /**
     * 启用角色
     * @param roleName
     * @throws Exception
     */
    public void enableRoleByRoleName(String roleName) throws Exception {
        RoleExample example = new RoleExample();
        example.createCriteria().andNameEqualTo(roleName);
        List<Role> list = roleMapper.selectByExample(example);
        if(list == null && list.size() <= 0){
            throw new MarsRuntimeException("启用的角色不存在");
        }
        Role role = list.get(0);
        role.setSecondResourceIds(null);
        roleMapper.updateByPrimaryKey(role);
    }
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
        role.setSecondResourceIds(null);
        roleMapper.updateByPrimaryKey(role);
    }

    public void DisableRoleByRoleName(String roleName) throws Exception {
        RoleExample example = new RoleExample();
        example.createCriteria().andNameEqualTo(roleName);
        List<Role> list = roleMapper.selectByExample(example);
        if(list == null && list.size() <= 0){
            throw new MarsRuntimeException("禁用的角色不存在");
        }
        Role role = list.get(0);
        role.setSecondResourceIds("pause");
        roleMapper.updateByPrimaryKey(role);
    }

    @Override
    public void addRole(Role role,List<Map<String, Object>> rolePrivilegeList) throws Exception {
        Integer isAdmin = (Integer)session.getAttribute("isAdmin");
        if(isAdmin!=1){
            throw new MarsRuntimeException("管理员才能添加角色");
        }
        String roleName = role.getName();
        RoleExample example = new RoleExample();
        example.createCriteria().andNameEqualTo(roleName);
        List<Role> list = roleMapper.selectByExample(example);
        if(list != null && list.size() > 0 && list.get(0).getAvailable() == Boolean.TRUE){
            throw new MarsRuntimeException("角色"+role.getName()+"已经存在");
        }
        RoleExample preexample2 = new RoleExample();
        preexample2.createCriteria().andDescriptionEqualTo(role.getDescription());
        List<Role> prelist2 = roleMapper.selectByExample(preexample2);
        if(prelist2 != null && prelist2.size() > 0 && prelist2.get(0).getAvailable() == Boolean.TRUE){
            throw new MarsRuntimeException("角色名"+role.getDescription()+"已经存在");
        }

        if(list != null && list.size() > 0 && list.get(0).getAvailable() == Boolean.FALSE){
            Role role2 = list.get(0);
            role2.setAvailable(role.getAvailable());
            role2.setUpdateTime(role.getUpdateTime());
            role2.setDescription(role.getDescription());
            roleMapper.updateByPrimaryKeySelective(role2);
        }else {
            roleMapper.insertSelective(role);
        }
        resourceService.addNewRoleMenu(role.getName());
        List<RolePrivilege> rolePrivilegeByRoleName = this.rolePrivilegeService.getRolePrivilegeByRoleName(CommonConstant.DEFAULT);
        if(rolePrivilegeList==null || rolePrivilegeList.isEmpty()){
            //没有初始权限，使用default权限
            for (RolePrivilege rolePrivilege : rolePrivilegeByRoleName) {
                rolePrivilege.setId(null);
                rolePrivilege.setRole(roleName);
                rolePrivilege.setUpdateTime(new Date());
                this.rolePrivilegeService.addModule(rolePrivilege);
            }
            //TODO
            InitClusterRole initClusterRole = JsonUtil.jsonToPojo(InitClusterRoleEnum.defaultRole.getJson(), InitClusterRole.class);
            initClusterRole.setName(roleName);
            roleService.createClusterRole(initClusterRole);
        }else{
            //有初始权限，更新初始权限
            HashMap<Integer, Boolean> newPrivilege = new HashMap<>();
            for (Map<String, Object> rolePrivilege : rolePrivilegeList) {
                newPrivilege.put((Integer) rolePrivilege.get("rpid"), (Boolean) rolePrivilege.get("status"));
            }
            for (RolePrivilege rolePrivilege : rolePrivilegeByRoleName) {
                rolePrivilege.setId(null);
                rolePrivilege.setRole(roleName);
                rolePrivilege.setUpdateTime(new Date());
                if(newPrivilege.get(rolePrivilege.getRpid()) != null){
                    rolePrivilege.setStatus(newPrivilege.get(rolePrivilege.getRpid()));
                }
                this.rolePrivilegeService.addModule(rolePrivilege);
            }
            rolePrivilegeService.updateRoleMenu(roleName);
            //TODO
            InitClusterRole initClusterRole = JsonUtil.jsonToPojo(InitClusterRoleEnum.defaultRole.getJson(), InitClusterRole.class);
            initClusterRole.setName(roleName);
            roleService.createClusterRole(initClusterRole);
        }
    }

    @Override
    public void deleteRole(String roleName) throws Exception {

        //删除角色绑定　rolebinding
        List<UserTenant> userByRoleName = userTenantService.findUserByRoleName(roleName);
        Map<String,Object> map = new HashMap <String,Object>();
        for (UserTenant userTenant : userByRoleName) {
            Object o = map.get(userTenant.getTenantid());
            if (o == null){
                TenantBinding tenantByTenantid = tenantService.getTenantByTenantid(userTenant.getTenantid());
                Cluster cluster = tenantService.getClusterByTenantid(userTenant.getTenantid());
                tenantByTenantid.getK8sNamespaceList().forEach(item->{try {
                    roleBindingService.deleteRolebings(item,roleName+"-rb",cluster);
                }catch (Exception e){
                    e.printStackTrace();
                }
                });
            }
        }

        //删除用户角色关联
        userTenantService.deleteUserByRoleName(roleName);

        //删除角色
        RoleExample example = new RoleExample();
        example.createCriteria().andNameEqualTo(roleName);
        roleMapper.deleteByExample(example);

        //删除角色权限
        RolePrivilegeExample rolePrivilegeExample = new RolePrivilegeExample();
        rolePrivilegeExample.createCriteria().andRoleEqualTo(roleName);
        rolePrivilegeMapper.deleteByExample(rolePrivilegeExample);

        //删除角色菜单资源
        ResourceExample resourceExample = new ResourceExample();
        resourceExample.createCriteria().andRoleEqualTo(roleName);
        resourceMapper.deleteByExample(resourceExample);
        roleService.deleteClusterrole(roleName);


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
