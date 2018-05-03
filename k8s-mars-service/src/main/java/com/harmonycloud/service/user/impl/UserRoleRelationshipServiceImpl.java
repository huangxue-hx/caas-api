package com.harmonycloud.service.user.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.user.UserRoleRelationshipMapper;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dao.user.bean.UserRoleRelationship;
import com.harmonycloud.dao.user.bean.UserRoleRelationshipExample;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserRoleRelationshipService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserRoleRelationshipServiceImpl implements UserRoleRelationshipService {
    @Autowired
    private UserRoleRelationshipMapper userRoleRelationshipMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private RoleLocalService roleLocalService;
    @Autowired
    private TenantService tenantService;

    /**
     * 根据用户名获取当前用户在当前项目下的所有角色
     *
     * @param username
     * @return
     * @throws Exception
     */
    @Override
    public List<Map> getRoleByUsername(String username) throws Exception {
        List<Map> list= null;
        //根据用户名查询用户角色关系
        List<UserRoleRelationship> userRoleRelationships = this.getUserRoleRelationship(username);
        //处理用户角色关系，遍历去重
        list = this.dealRoleRelationships(userRoleRelationships);
        return list;
    }

    /**
     * 根据用户名获取当前用户在当前项目下的所有角色
     *
     * @param username
     * @param projectId
     * @return
     * @throws Exception
     */
    @Override
    public List<Map> getRoleByUsernameAndProject(String username, String projectId) throws Exception {
        List<Map> list= null;
        //根据用户名与项目id查询用户角色关系
        List<UserRoleRelationship> userRoleRelationships = this.getUserRoleRelationship(username,projectId);
        //处理用户角色关系，遍历去重
        list = this.dealRoleRelationships(userRoleRelationships);
        return list;
    }

    /**
     * 根据用户名获取当前用户在当前租户下的所有角色
     *
     * @param username
     * @param tenantId
     * @return
     * @throws Exception
     */
    @Override
    public List<Map> getRoleByUsernameAndTenantId(String username, String tenantId) throws Exception {
        List<Map> list= null;
        //根据用户名与项目id查询用户角色关系
        List<UserRoleRelationship> userRoleRelationships = this.getUserRoleRelationshipByTenantId(username,tenantId);
        //处理用户角色关系，遍历去重
        list = this.dealRoleRelationships(userRoleRelationships);
        return list;
    }

    /**
     * 处理用户角色关系，遍历去重
     * @param userRoleRelationships
     * @return
     * @throws Exception
     */
    private List<Map> dealRoleRelationships(List<UserRoleRelationship> userRoleRelationships) throws Exception{
        List<Map> list= new ArrayList<Map>();
        //如果为空直接返回
        if (CollectionUtils.isEmpty(userRoleRelationships)){
            return list;
        }
        Map<String,Object> map = new HashMap<String,Object>();
        //遍历去重
        for (UserRoleRelationship userRoleRelationship: userRoleRelationships) {
            Integer roleId = userRoleRelationship.getRoleId();
            Role roleById = roleLocalService.getRoleById(roleId);
            if (!Objects.isNull(roleById) && map.get(roleId.toString()) == null && roleById.getAvailable()){
                Map<String,Object> role = new HashMap<String,Object>();
                role.put(CommonConstant.ROLEID,roleId);
                role.put(CommonConstant.ROLENAME,roleById.getName());
                list.add(role);
            }
        }
        return list;
    }
    //查询项目下关系列表
    private List<UserRoleRelationship> getUserRoleRelationship(String username) throws Exception {
        //查询用户角色关系
        UserRoleRelationshipExample example = this.getExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<UserRoleRelationship> userRoleRelationships = userRoleRelationshipMapper.selectByExample(example);
        return userRoleRelationships;
    }
    //查询用户当前项目下关系列表
    private List<UserRoleRelationship> getUserRoleRelationship(String username,String projectId) throws Exception {
        //查询用户角色关系
        UserRoleRelationshipExample example = this.getExample();
        //TODO 后期页面调整完成后测试
        if (StringUtils.isBlank(projectId)|| (StringUtils.isBlank(projectId)&&!Objects.isNull(this.userService.getCurrentRoleId()) && this.userService.getCurrentRoleId() <= CommonConstant.TM_ROLEID)){
            example.createCriteria().andUsernameEqualTo(username);
        }else {
            example.createCriteria().andUsernameEqualTo(username).andProjectIdEqualTo(projectId);
        }
        List<UserRoleRelationship> userRoleRelationships = userRoleRelationshipMapper.selectByExample(example);
        return userRoleRelationships;
    }
    //查询用户当前项目下关系列表
    private List<UserRoleRelationship> getUserRoleRelationshipByTenantId(String username,String tenantId) throws Exception {
        //查询用户角色关系
        UserRoleRelationshipExample example = this.getExample();
        if (StringUtils.isBlank(tenantId) && this.userService.getCurrentRoleId() <= CommonConstant.ADMIN_ROLEID){
            example.createCriteria().andUsernameEqualTo(username);
        }else {
            example.createCriteria().andUsernameEqualTo(username).andTenantIdEqualTo(tenantId);
        }
        List<UserRoleRelationship> userRoleRelationships = userRoleRelationshipMapper.selectByExample(example);
        return userRoleRelationships;
    }
    /**
     * 根据用户名查询用户是否有角色
     * @param username
     * @return
     * @throws Exception
     */
    public Boolean hasRole(String username) throws Exception{
        //根据用户名查询用户角色关系
        //查询用户角色关系
        UserRoleRelationshipExample example = this.getExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<UserRoleRelationship> userRoleRelationships = userRoleRelationshipMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(userRoleRelationships)){
            return false;
        }
        return true;
    }
    /**
     * 根据id获取用户角色关系
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public UserRoleRelationship getUserRoleRelationshipById(int id) throws Exception {
        //查询用户角色关系
        UserRoleRelationship userRoleRelationships = userRoleRelationshipMapper.selectByPrimaryKey(id);
        return userRoleRelationships;
    }

    /**
     * 根据用户名与项目id获取用户角色关系列表
     *
     * @param username
     * @param projectId
     * @return
     * @throws Exception
     */
    @Override
    public List<UserRoleRelationship> getUserRoleRelationshipByUsernameAndProjectId(String username, String projectId) throws Exception {
        //查询用户角色关系
        UserRoleRelationshipExample example = this.getExample();
        example.createCriteria().andUsernameEqualTo(username).andProjectIdEqualTo(projectId);
        List<UserRoleRelationship> userRoleRelationships = userRoleRelationshipMapper.selectByExample(example);
        return userRoleRelationships;
    }

    /**
     * 创建用户角色关系
     *
     * @param userRoleRelationship
     * @throws Exception
     */
    @Override
    public void createUserRoleRelationship(UserRoleRelationship userRoleRelationship) throws Exception {
        this.userRoleRelationshipMapper.insertSelective(userRoleRelationship);
    }

    /**
     * 更新用户角色关系
     *
     * @param userRoleRelationship
     * @throws Exception
     */
    @Override
    public void updateUserRoleRelationship(UserRoleRelationship userRoleRelationship) throws Exception {
        this.userRoleRelationshipMapper.updateByPrimaryKeySelective(userRoleRelationship);
    }

    @Override
    public void setLocalRoleFlag(String projectId, List<String> usernames) {
        //先将项目的所有成员是否包含局部角色改成false，再将指定成员的局部角色标识改成true
        userRoleRelationshipMapper.updateLocalRoleFlag(projectId,null,Boolean.FALSE);
        if(!CollectionUtils.isEmpty(usernames)) {
            userRoleRelationshipMapper.updateLocalRoleFlag(projectId, usernames, Boolean.TRUE);
        }
    }

    /**
     * 根据id删除用户角色关系
     *
     * @param id
     * @throws Exception
     */
    @Override
    public void deleteUserRoleRelationshipById(Integer id) throws Exception {
        this.userRoleRelationshipMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据租户id删除用户角色关系
     *
     * @param tenantId
     * @throws Exception
     */
    @Override
    public void deleteUserRoleRelationshipByTenantId(String tenantId) throws Exception {
        UserRoleRelationshipExample example = this.getExample();
        example.createCriteria().andTenantIdEqualTo(tenantId);
        userRoleRelationshipMapper.deleteByExample(example);
    }

    /**
     * 根据项目id删除用户角色关系
     *
     * @param projectId
     * @throws Exception
     */
    @Override
    public void deleteUserRoleRelationshipByProjectId(String projectId) throws Exception {
        UserRoleRelationshipExample example = this.getExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        userRoleRelationshipMapper.deleteByExample(example);
    }

    /**
     * 根据角色名获取项目下的所有用户列表
     *
     * @param roleId
     * @param projectId
     * @throws Exception
     */
    @Override
    public List<UserRoleRelationship> listUserByRoleNameAndProjectId(int roleId, String projectId) throws Exception {
        UserRoleRelationshipExample example = this.getExample();
        UserRoleRelationshipExample.Criteria criteria = example.createCriteria();
        if (projectId == null){
            criteria.andRoleIdEqualTo(roleId);
        }else {
            criteria.andRoleIdEqualTo(roleId).andProjectIdEqualTo(projectId);
        }

        List<UserRoleRelationship> userRoleRelationships = this.userRoleRelationshipMapper.selectByExample(example);
//        List<String> users= new ArrayList<>();
//        //转换为用户名列表
//        for (UserRoleRelationship userRoleRelationship:userRoleRelationships) {
//            users.add(userRoleRelationship.getUsername());
//        }
        return userRoleRelationships;
    }

    /**
     * 根据租户id获取租户下的所有租户管理员用户列表
     *
     * @param tenantId
     * @return
     * @throws Exception
     */
    @Override
    public List<UserRoleRelationship> listTmByTenantId(String tenantId) throws Exception {
        Role role = roleLocalService.getRoleByRoleName(CommonConstant.TM);
        UserRoleRelationshipExample example = this.getExample();
        example.createCriteria().andTenantIdEqualTo(tenantId).andRoleIdEqualTo(role.getId());
        List<UserRoleRelationship> userRoleRelationships = this.userRoleRelationshipMapper.selectByExample(example);
        return userRoleRelationships;
    }
    /**
     * 根据租户id与用户名获取租户下的租户管理员用户
     * @param tenantId, username
     * @return
     * @throws Exception
     */
    @Override
    public UserRoleRelationship getTmByTenantIdAndUsername(String tenantId,String username) throws Exception {
        //获取租户管理员角色id
        Role role = roleLocalService.getRoleByRoleName(CommonConstant.TM);
        UserRoleRelationshipExample example = this.getExample();
        example.createCriteria().andTenantIdEqualTo(tenantId).andRoleIdEqualTo(role.getId()).andUsernameEqualTo(username);
        List<UserRoleRelationship> userRoleRelationships = this.userRoleRelationshipMapper.selectByExample(example);
        if (userRoleRelationships.isEmpty()){
            return null;
        }
        return userRoleRelationships.get(0);
    }

    /**
     * 根据项目id与用户名获取项目下的项目管理员用户
     *
     * @param projectId
     * @param username
     * @return
     * @throws Exception
     */
    @Override
    public UserRoleRelationship getPmByProjectIdAndUsername(String projectId, String username) throws Exception {
        //获取项目管理员角色id
        Role role = roleLocalService.getRoleByRoleName(CommonConstant.PM);
        return this.getUser(projectId, username, role.getId());
    }
    /**
     * 根据项目id，用户名，角色id获取用户角色
     * @param projectId
     * @param username
     * @param roleId
     * @return
     * @throws Exception
     */
    @Override
    public UserRoleRelationship getUser(String projectId, String username, Integer roleId) throws Exception {
        UserRoleRelationshipExample example = this.getExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andRoleIdEqualTo(roleId).andUsernameEqualTo(username);
        List<UserRoleRelationship> userRoleRelationships = this.userRoleRelationshipMapper.selectByExample(example);
        if (userRoleRelationships.isEmpty()){
            return null;
        }
        return userRoleRelationships.get(0);
    }

    /**
     * 根据tenantid获取租户下的所有用户列表
     *
     * @param tenantId
     * @throws Exception
     */
    @Override
    public List<String> listUserByTenantId(String tenantId) throws Exception {
        List<String> list= new ArrayList<String>();
        UserRoleRelationshipExample example = this.getExample();
        example.createCriteria().andTenantIdEqualTo(tenantId);
        List<UserRoleRelationship> userRoleRelationships = this.userRoleRelationshipMapper.selectByExample(example);
        //如果为空直接返回
        if (userRoleRelationships.isEmpty()){
            return list;
        }
        Map<String,Object> map = new HashMap<String,Object>();
        //遍历去重
        for (UserRoleRelationship userRoleRelationship: userRoleRelationships) {
            String username = userRoleRelationship.getUsername();
            if (map.get(username) == null){
                map.put(username,userRoleRelationship);
                list.add(username);
            }
        }
        return list;
    }

    /**
     * 根据nickName获取租户下的所有用户列表
     *
     * @param nickName
     * @return
     * @throws Exception
     */
    @Override
    public List<UserRoleRelationship> listUserByNickName(String nickName) throws Exception {
        Role role = roleLocalService.getRoleByNickName(nickName);
        if (Objects.isNull(role)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        List<UserRoleRelationship> userRoleRelationships = this.listUserByRoleId(role.getId());
        return userRoleRelationships;
    }

    /**
     * 根据roleId获取租户下的所有用户列表
     *
     * @param roleId
     * @return
     * @throws Exception
     */
    @Override
    public List<UserRoleRelationship> listUserByRoleId(Integer roleId) throws Exception {
        //查看角色是否存在
        Role role = roleLocalService.getRoleById(roleId);
        if (Objects.isNull(role)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        UserRoleRelationshipExample example = this.getExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        List<UserRoleRelationship> userRoleRelationships = userRoleRelationshipMapper.selectByExample(example);
        return userRoleRelationships;
    }

    /**
     * 根据projectId获取项目下的所有用户列表
     *
     * @param projectId
     * @return
     * @throws Exception
     */
    @Override
    public List<UserRoleRelationship> listUserByProjectId(String projectId) throws Exception {
        UserRoleRelationshipExample example = this.getExample();
        example.setOrderByClause("username asc");
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<UserRoleRelationship> userRoleRelationships = this.userRoleRelationshipMapper.selectByExample(example);
        return userRoleRelationships;
    }

    /**
     * 根据projectId获取项目下的所有的项目管理员用户列表
     *
     * @param projectId
     * @return
     * @throws Exception
     */
    @Override
    public List<UserRoleRelationship> listPmByTenantAndProjectId(String tenantId,String projectId) throws Exception {
        Role role = roleLocalService.getRoleByRoleName(CommonConstant.PM);
        UserRoleRelationshipExample example = this.getExample();
        example.createCriteria().
                andTenantIdEqualTo(tenantId).
                andProjectIdEqualTo(projectId).
                andRoleIdEqualTo(role.getId());
        List<UserRoleRelationship> userRoleRelationships = this.userRoleRelationshipMapper.selectByExample(example);
        return userRoleRelationships;
    }

    /**
     * 判断用户是否为租户下的管理员
     * @param username
     * @return
     * @throws Exception
     */
    @Override
    public Boolean isTmUser(String tenantId,String username,Integer roleId) throws Exception{
        UserRoleRelationshipExample example = this.getExample();
        example.createCriteria().andRoleIdEqualTo(roleId).andTenantIdEqualTo(tenantId).andUsernameEqualTo(username);
        List<UserRoleRelationship> userRoleRelationships = userRoleRelationshipMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(userRoleRelationships)){
            return CommonConstant.FALSE;
        }
        return CommonConstant.TRUE;
    }
    /**
     * 根据用户获取当前用户租户列表
     *
     * @param username
     * @throws Exception
     */
    @Override
    public List<TenantBinding> listTenantByUsername(String username) throws Exception {
        List<TenantBinding> list= new ArrayList<TenantBinding>();
        //根据用户名查询用户角色关系
        List<UserRoleRelationship> userRoleRelationships = this.getUserRoleRelationship(username);
        //如果为空直接返回
        if (CollectionUtils.isEmpty(userRoleRelationships)){
            return list;
        }
        Map<String,Boolean> map = new HashMap<>();
        //组装租户列表
        for (UserRoleRelationship userRoleRelationship: userRoleRelationships) {
            String tenantId = userRoleRelationship.getTenantId();
            TenantBinding tenantByTenantid = tenantService.getTenantByTenantid(tenantId);
            if (!Objects.isNull(tenantByTenantid) && Objects.isNull(map.get(tenantId))){
                list.add(tenantByTenantid);
                map.put(tenantId,Boolean.TRUE);
            }
        }
        return list;
    }

    /**
     * 根据租户id与用户名获取当前用户project列表
     *
     * @param tenantId
     * @param username
     * @return
     * @throws Exception
     */
    @Override
    public List<Project> listProjectByTenantIdAndUsername(String tenantId, String username) throws Exception {
        //查询用户角色关系
        UserRoleRelationshipExample example = this.getExample();
        if (CommonConstant.TM_ROLEID.equals(this.userService.getCurrentRoleId())){
            example.createCriteria().andTenantIdEqualTo(tenantId);
        }else {
            example.createCriteria().andUsernameEqualTo(username).andTenantIdEqualTo(tenantId);
        }
        List<UserRoleRelationship> userRoleRelationships = userRoleRelationshipMapper.selectByExample(example);
        List<Project> list = new ArrayList<>();
        //为空返回
        if (CollectionUtils.isEmpty(userRoleRelationships)){
            return list;
        }
        Map<String,Boolean> map = new HashMap<>();
        //组装项目列表
        for (UserRoleRelationship userRoleRelationship: userRoleRelationships) {
            String projectId = userRoleRelationship.getProjectId();
            if (StringUtils.isNotBlank(projectId)){
                Project project = this.projectService.getProjectByProjectId(projectId);
                if (!Objects.isNull(project) && Objects.isNull(map.get(projectId))){
                    list.add(project);
                    map.put(projectId,Boolean.TRUE);
                }
            }
        }
        return list;
    }

    /**
     * 根据用户名与角色id获取UserRoleRelationship列表
     *
     * @param username
     * @param roleId
     * @return
     * @throws Exception
     */
    @Override
    public List<UserRoleRelationship> getUserRoleRelationshipList(String username, Integer roleId) throws Exception {
        //查询用户角色关系
        UserRoleRelationshipExample example = this.getExample();
        example.createCriteria().andUsernameEqualTo(username).andRoleIdEqualTo(roleId);
        List<UserRoleRelationship> userRoleRelationships = userRoleRelationshipMapper.selectByExample(example);
        return userRoleRelationships;
    }

    /**
     * 根据租户id与用户名获取用户关系列表
     *
     * @param username
     * @param tenantId
     * @return
     * @throws Exception
     */
    @Override
    public List<UserRoleRelationship> getUserRoleRelationshipList(String username, String tenantId) throws Exception {
        //查询用户角色关系
        UserRoleRelationshipExample example = this.getExample();
        example.createCriteria().andUsernameEqualTo(username).andTenantIdEqualTo(tenantId);
        List<UserRoleRelationship> userRoleRelationships = userRoleRelationshipMapper.selectByExample(example);
        return userRoleRelationships;
    }

    private UserRoleRelationshipExample getExample() throws Exception {
        UserRoleRelationshipExample example = new UserRoleRelationshipExample();
        return example;
    }
}
