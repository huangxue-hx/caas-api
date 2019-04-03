package com.harmonycloud.service.user.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.HarborMemberEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.UUIDUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.user.RoleMapper;
import com.harmonycloud.dao.user.bean.*;
import com.harmonycloud.dto.cluster.DataCenterDto;
import com.harmonycloud.dto.user.PrivilegeDto;
import com.harmonycloud.dto.user.RoleDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.application.DataCenterService;
import com.harmonycloud.service.cache.ClusterCacheManager;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.harbor.HarborRole;
import com.harmonycloud.service.platform.bean.harbor.HarborUser;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborUserService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.user.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.PM_ROLEID;
import static com.harmonycloud.service.platform.constant.Constant.TOP_DATACENTER;

@Service("roleLocalService")
public class RoleLocalServiceImpl implements RoleLocalService {
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserRoleRelationshipService userRoleRelationshipService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private RolePrivilegeService rolePrivilegeService;
    @Autowired
    private PrivilegeService privilegeService;
    @Autowired
    private UserService userService;
    @Autowired
    private ResourceMenuRoleService resourceMenuRoleService;
    @Autowired
    private ClusterCacheManager clusterCacheManager;
    @Autowired
    private HarborUserService harborUserService;
    @Autowired
    private HarborProjectService harborProjectService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private DataCenterService dataCenterService;

    @Value("#{propertiesReader['harbor.role.enable']}")
    private boolean enableHarborRole;

    private static final Logger log = LoggerFactory.getLogger(RolePrivilegeServiceImpl.class);

    @Autowired
    private HttpSession session;
//   租户项目管理员作用域模式
//   1 租户项目管理员为默认全局作用域不能修改，
//   2 租户项目管理员为默认全局作用域,租户管理员不能修改作用域，项目管理员作用域可以修改
//   3 租户项目管理员为默认全局作用域,作用域可以修改
    @Value("#{propertiesReader['scope.tmAndPm']}")
    private String scopeType;

    private static final String ONE = "1";
    private static final String TWO = "2";
    private static final String THREE = "3";
    private static final int TENANTMGR = 3;
    private static final int MYTENANT = 4;
    private static final int MYPROJECT = 5;

    /**
     * 获取所有的角色列表
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<Role> getAllRoleList() throws Exception {
        RoleExample example = this.getExample();
        List<Role> roleList = this.roleMapper.selectByExample(example);
        return roleList;
    }

    /**
     * 获取所有可用的角色列表
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<Role> getAvailableRoleList() throws Exception {
        RoleExample example = this.getExample();
        example.createCriteria().andAvailableEqualTo(Boolean.TRUE);
        List<Role> roleList = this.roleMapper.selectByExample(example);
        return roleList;
    }

    /**
     * 获取某用户的角色列表
     *
     * @param username
     * @return
     * @throws Exception
     */
    @Override
    public List<Role> getRoleListByUsername(String username) throws Exception {
        List<Role> list = null;
        //获取用户的角色列表
        List<Map> roleByUsername = this.userRoleRelationshipService.getRoleByUsername(username);
        list = this.transformRoleMap(roleByUsername,username,null,true);
        return list;
    }

    /**
     * 获取项目下某用户的角色列表
     *
     * @param username
     * @param projectId
     * @return
     * @throws Exception
     */
    @Override
    public List<Role> getRoleListByUsernameAndTenantIdAndProjectId(String username,String tenantId, String projectId) throws Exception {
        List<Role> list = null;
        //获取项目下用户的角色列表
        List<Map> roleByUsername = null;
        roleByUsername = this.userRoleRelationshipService.getRoleByUsernameAndProject(username,projectId);
        Boolean status = StringUtils.isNotBlank(projectId);
        list = this.transformRoleMap(roleByUsername,username,tenantId,status);
        return list;
    }

    /**
     * 获取租户下某用户的角色列表
     *
     * @param username
     * @param tenantId
     * @return
     * @throws Exception
     */
    @Override
    public List<Role> getRoleListByUsernameAndTenantId(String username, String tenantId) throws Exception {
        List<Role> list = null;
        //获取项目下用户的角色列表
        List<Map> roleByUsername = null;
        roleByUsername = this.userRoleRelationshipService.getRoleByUsernameAndTenantId(username,tenantId);
        Boolean status = StringUtils.isNotBlank(tenantId);
        list = this.transformRoleMap(roleByUsername,username,tenantId,status);
        return list;
    }

    //转化为角色列表 projectId 为空则为查询用户所有的角色
    private List<Role> transformRoleMap(List<Map> roleByUsername,String username,String tenantId,Boolean status) throws Exception{
        List<Role> list = new ArrayList<>();
        //判断用户是否为系统管理员
        boolean isAdmin = this.userService.isAdmin(username);
        Map <Integer,Role> map = new HashMap<>();
        //处理系统管理员无视projectId的特性
        if (isAdmin){
            Role role = this.getRoleByRoleName(CommonConstant.ADMIN);
            list.add(role);
            map.put(role.getId(),role);
        }
        if (StringUtils.isNotBlank(tenantId)){
            //判断用户在该项目所属的租户下是否为租户管理员
            Role tm = this.getRoleByRoleName(CommonConstant.TM);
            Boolean isTm = this.userRoleRelationshipService.isTmUser(tenantId, username, tm.getId());
            //如果为租户管理员则处理租户管理员
            if (isTm){
                if (Objects.isNull(map.get(tm.getId()))){
                    list.add(tm);
                    map.put(tm.getId(), tm);
                }
            }else if (!Objects.isNull(map.get(tm.getId()))){
                //如果不为该租户的租户管理员则删除多余数据
                list.remove(tm);
            }
        }
        if (!CollectionUtils.isEmpty(roleByUsername) && status){
            for (Map roleMap:roleByUsername) {
                Integer roleId = (Integer) roleMap.get(CommonConstant.ROLEID);
                //去重
                Role role = map.get(roleId);
                if (Objects.isNull(role)){
                    Role roleById = this.getRoleById(roleId);
                    if (!Objects.isNull(roleById)){
                        list.add(roleById);
                        map.put(roleById.getId(),roleById);
                    }
                }
            }
        }
        //除去被禁用的角色
        List<Role> availableRoleList = list.stream().filter(role -> role.getAvailable()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(availableRoleList)){
            //被禁用后该用户在该租户下项目下可用角色为空，处理被禁用的角色
            session.setAttribute("roleStatus",Boolean.FALSE);
//            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_DISABLE);
        }else {
            session.setAttribute("roleStatus",Boolean.TRUE);
        }
        return availableRoleList;
    }

    /**
     * 获取某用户所在的角色作用域（集群）列表
     *
     * @param username
     * @return
     * @throws Exception
     */
    @Override
    public List<Cluster> getClusterListByUsername(String username) throws Exception {
        if (StringUtils.isBlank(username)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        List<Role> roleList = this.getRoleListByUsername(username);
        List<Cluster> result = this.dealRoleCluster(roleList);
        return result;
    }

    /**
     * 获取角色id获取集群列表
     *
     * @param roleId
     * @return
     * @throws Exception
     */
    @Override
    public List<Cluster> getClusterListByRoleId(Integer roleId) throws Exception {
        //空值判断
        if (Objects.isNull(roleId)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Role role = this.getRoleById(roleId);
        List<Role> roleList = new ArrayList<>();
        roleList.add(role);
        List<Cluster> result = this.dealRoleCluster(roleList);
        return result;
    }

    @Override
    public List<Cluster> listCurrentUserRoleCluster() throws Exception {
        Integer roleId = userService.getCurrentRoleId();
        List<Cluster> clusters = this.getClusterListByRoleId(roleId);
        String currentDataCenter = this.getCurrentDataCenter();
        if(currentDataCenter != null){
            return clusters.stream().filter(cluster -> cluster.getDataCenter().equals(currentDataCenter)).collect(Collectors.toList());
        }
        return clusters;
    }

    @Override
    public List<String> listCurrentUserRoleClusterIds() throws Exception {
        List<Cluster> clusters = this.listCurrentUserRoleCluster();
        if(CollectionUtils.isEmpty(clusters)){
            return Collections.emptyList();
        }
        return clusters.stream().map(Cluster::getId).collect(Collectors.toList());

    }

    @Override
    public String getCurrentDataCenter() throws Exception {
        if(session.getAttribute(CommonConstant.DATA_CENTER) != null){
            return session.getAttribute(CommonConstant.DATA_CENTER).toString();
        }
        return "";
    }

    private List<Cluster> dealRoleCluster(List<Role> roleList) throws Exception {
        List<Cluster> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(roleList)){
            return result;
        }
        Map<String,Cluster> map = new HashMap<>();

        for (Role role:roleList){
            //获取角色的作用域
            String clusterIds = role.getClusterIds();
            if (StringUtils.isNotBlank(clusterIds)){
                List<String> list = Arrays.stream(clusterIds.split(CommonConstant.COMMA)).
                        collect(Collectors.toList());
                for (String clusterId:list) {
                    Cluster cluster = clusterService.findClusterById(clusterId);
                    //如果集群状态不可用，过滤该cluster
                    if(!cluster.getIsEnable()){
                        continue;
                    }
                    if (Objects.isNull(map.get(clusterId))){
                        map.put(clusterId,cluster);
                        result.add(cluster);
                    }
                }
            }else if (role.getId() <= CommonConstant.NUM_THREE){
                //角色为管理员
                List<Cluster> clusters = this.clusterService.listCluster();
                return clusters;
            }
        }
        return result;
    }
    /**
     * 根据id获取角色
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public Role getRoleById(Integer id) throws Exception {
        Role role = this.roleMapper.selectByPrimaryKey(id);
        return role;
    }

    /**
     * 根据昵称获取角色
     *
     * @param nickName
     * @return
     * @throws Exception
     */
    @Override
    public Role getRoleByNickName(String nickName) throws Exception {
        RoleExample example = this.getExample();
        example.createCriteria().andNickNameEqualTo(nickName);
        List<Role> roles = this.roleMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(roles)){
            return roles.get(0);
        }
        return null;
    }

    /**
     * 创建角色
     *
     * @param roleDto
     * @throws Exception
     */
    @Transactional
    @Override
    public void createRole(RoleDto roleDto) throws Exception {

        String nickName = roleDto.getNickName();
        String clusterIds = roleDto.getClusterIds();
        if (roleDto.getAvailable() || StringUtils.isNotBlank(clusterIds)){
            //去除无效的空集群id
            String[] split = clusterIds.split(CommonConstant.COMMA);
            List<String> clusteridList = Arrays.stream(split).
                    filter(clusterId -> StringUtils.isNotBlank(clusterId)).collect(Collectors.toList());
            clusterIds = StringUtils.join(clusteridList, CommonConstant.COMMA);
        }
        //检查角色是否存在
        Role role = this.getRoleByNickName(nickName.trim());
        if(!Objects.isNull(role)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_EXIST,nickName,Boolean.TRUE);
        }
        //组装角色
        role = new Role();
        String namespaceNames = roleDto.getNamespaceNames();
        String name = UUIDUtil.get16UUID();
        role.setName(name);
        if (Objects.isNull(roleDto.getAvailable())){
            role.setAvailable(Boolean.FALSE);
        }else {
            role.setAvailable(roleDto.getAvailable());
        }
        role.setNickName(nickName.trim());
        role.setClusterIds(clusterIds);
        //如果角色作用域选择分区将会设置对应的分区（现在未启用）
        if (StringUtils.isNotBlank(namespaceNames)){
            role.setNamespaceNames(namespaceNames);
        }
        role.setCreateTime(DateUtil.getCurrentUtcTime());
        this.roleMapper.insertSelective(role);
        List<PrivilegeDto> rolePrivilegeList = roleDto.getRolePrivilegeList();
        //处理角色菜单
        this.resourceMenuRoleService.createResourceMenuRole(role.getId());
        //处理角色权限
        this.dealPrivilegeWithRole(role.getId(),rolePrivilegeList);
    }
    private void dealPrivilegeWithRole(Integer roleId,List<PrivilegeDto> rolePrivilegeList) throws Exception{
        //默认权限
        List<Privilege> privileges = this.privilegeService.listAllPrivilege();
        if(CollectionUtils.isEmpty(rolePrivilegeList)){
            //使用初始权限
            if (!CollectionUtils.isEmpty(privileges)){
                for (Privilege privilege : privileges) {
                    RolePrivilege rolePrivilege = new RolePrivilege();
                    rolePrivilege.setCreateTime(DateUtil.getCurrentUtcTime());
                    rolePrivilege.setStatus(Boolean.FALSE);
                    rolePrivilege.setPid(privilege.getId());
                    rolePrivilege.setRoleId(roleId);
                    this.rolePrivilegeService.createRolePrivilege(rolePrivilege);
                }
            }
        }else{
            //有分配的权限，使用分配的权限
            Map<Integer, Boolean> collectPrivilege = rolePrivilegeList.stream().collect(Collectors.toMap(PrivilegeDto::getId, privilegeDto -> privilegeDto.getStatus()));
            if (!CollectionUtils.isEmpty(privileges)){
                for (Privilege privilege : privileges) {
                    RolePrivilege rolePrivilege = new RolePrivilege();
                    rolePrivilege.setCreateTime(DateUtil.getCurrentUtcTime());
                    Integer pid = privilege.getId();
                    Boolean status = collectPrivilege.get(pid);
                    if (Objects.isNull(status)){
                        status = Boolean.FALSE;
                    }
                    rolePrivilege.setStatus(status);
                    rolePrivilege.setPid(pid);
                    rolePrivilege.setRoleId(roleId);
                    this.rolePrivilegeService.createRolePrivilege(rolePrivilege);
                    if (status){
                        //权限同步对应角色菜单
                        this.rolePrivilegeService.syncRoleMenu(roleId,pid,status);
                    }
                }
                //如果新角色没有租户管理，我的租户，我的项目权限，处理其中的关系让角色拥有我的项目菜单。每个人至少拥有租户管理，我的租户，我的项目其中一个菜单
                Map<Integer, ResourceMenuRole> tenantMenuRole = this.resourceMenuRoleService.getResourceTenantMenuRole(roleId);
                ResourceMenuRole tenantMgrMenuRole = tenantMenuRole.get(TENANTMGR);
                ResourceMenuRole myTenantMenuRole = tenantMenuRole.get(MYTENANT);
                ResourceMenuRole myProjectMenuRole = tenantMenuRole.get(MYPROJECT);
                if (!tenantMgrMenuRole.getAvailable()
                        && !myTenantMenuRole.getAvailable()
                        && !myProjectMenuRole.getAvailable()){
                    myProjectMenuRole.setAvailable(Boolean.TRUE);
                    this.resourceMenuRoleService.updateResourceMenuRole(myProjectMenuRole);
                }
            }
        }
    }
    /**
     * 禁用角色
     * @param roleId
     * @throws Exception
     */
    public void disableRoleByRoleId(Integer roleId) throws Exception {
        //获取角色
        Role role = this.getRoleById(roleId);
        if (Objects.isNull(role)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        //初始化角色不允许禁用
        if (roleId <= PM_ROLEID){
            throw new MarsRuntimeException(ErrorCodeMessage.INIT_ROLE_CANNOT_DISABLE);
        }
        role.setUpdateTime(DateUtil.getCurrentUtcTime());
        //更新状态
        role.setAvailable(Boolean.FALSE);
        roleMapper.updateByPrimaryKey(role);
        clusterCacheManager.updateRolePrivilegeStatus(roleId,null,Boolean.TRUE);
        // 删除所有相关user的对应harbor权限
        if(enableHarborRole) {
            List<UserRoleRelationship> users = this.userRoleRelationshipService.listUserByRoleId(roleId);
            updateHarborMember(HarborMemberEnum.NONE, users);
        }

    }

    public void updateHarborMember(HarborMemberEnum targetMember , List<UserRoleRelationship> users) throws Exception{
        for (UserRoleRelationship userRoleRelationship : users) {
            this.updateHarborUserRole(targetMember,userRoleRelationship.getProjectId(),userRoleRelationship.getUsername());
        }
    }
    public void updateHarborUserRole(HarborMemberEnum targetMember,String projectId,String userName)throws Exception{
        List<ImageRepository> imageRepositories = harborProjectService.listRepository(projectId,null,Objects.isNull(projectId)?null:Boolean.FALSE, null);
        for(ImageRepository repository : imageRepositories) {
            List<HarborUser>  harborUsers =  harborUserService.usersOfProjectByUsername(repository.getHarborHost(), repository.getHarborProjectId(),userName);
            Integer currentlevel = 0;
            Integer userId = 0;
            if (!CollectionUtils.isEmpty(harborUsers)) {
                HarborUser harborUser = harborUsers.get(0);
                String rolename = harborUser.getRoleName();
                userId = harborUser.getUserId();
                currentlevel = HarborMemberEnum.getMemberLevel(rolename).getLevel();
            } else {
                continue;
            }
            Integer level = targetMember.getLevel();
            List<Map> userRoleList = userRoleRelationshipService.getRoleByUsernameAndProject(userName,repository.getProjectId());
            for(Map<String,Object> userRole : userRoleList) {
                Integer id = Integer.valueOf(userRole.get(CommonConstant.ROLEID).toString());
                if (id <= PM_ROLEID && id >= CommonConstant.ADMIN_ROLEID) {
                    level = HarborMemberEnum.PROJECTADMIN.getLevel() ;
                    continue;
                }
                HarborMemberEnum  findMember = rolePrivilegeService.getHarborRole(id);
                if (findMember.getLevel() < level) {
                    level = findMember.getLevel();
                }
            }
            if (level == HarborMemberEnum.NONE.getLevel()) {
                // delete
                harborUserService.deleteRole(repository.getHarborHost(),repository.getHarborProjectId(),userId);
                continue;
            }
            HarborRole harborRole = new HarborRole();
            harborRole.setUsername(userName);
            List<Integer> roleList = new ArrayList<>();
            roleList.add(level);
            harborRole.setRoleList(roleList);
            harborUserService.updateRole(repository.getHarborHost(),repository.getHarborProjectId(),userId,harborRole );
        }
    }
    /**
     * 根据角色id复制新角色
     *
     * @param roleId
     * @param nickName
     * @throws Exception
     */
    @Transactional
    @Override
    public void copyRoleByRoleId(Integer roleId, String nickName) throws Exception {
        //验证新添加的角色是否存在
        Role newRole = this.getRoleByNickName(nickName);
        if (!Objects.isNull(newRole)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_EXIST,nickName,Boolean.TRUE);
        }
        //获取角色
        Role role = this.getRoleById(roleId);
        if (Objects.isNull(role)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        //初始化角色不允许复制
        if (roleId <= CommonConstant.ADMIN_ROLEID){
            throw new MarsRuntimeException(ErrorCodeMessage.ADMIN_ROLE_CANNOT_DISABLE);
        }
        //创建新角色
        newRole = new Role();
        newRole.setNickName(nickName);
        String name = UUIDUtil.get16UUID();
        newRole.setName(name);
        newRole.setAvailable(Boolean.FALSE);
        newRole.setClusterIds(role.getClusterIds());
        newRole.setCreateTime(DateUtil.getCurrentUtcTime());
        this.roleMapper.insertSelective(newRole);
        //复制角色菜单
        List<ResourceMenuRole> resourceMenuRoles = this.resourceMenuRoleService.listResourceMenuRole(roleId);
        for (ResourceMenuRole resourceMenuRole : resourceMenuRoles) {
            resourceMenuRole.setId(null);
            resourceMenuRole.setRoleId(newRole.getId());
            this.resourceMenuRoleService.createResourceMenuRoleNative(resourceMenuRole);
        }
        //复制权限
        List<RolePrivilege> rolePrivilege = this.rolePrivilegeService.getRolePrivilegeByRoleId(roleId, Boolean.FALSE);
        for (RolePrivilege privilege:rolePrivilege) {
            privilege.setId(null);
            privilege.setRoleId(newRole.getId());
            this.rolePrivilegeService.createRolePrivilege(privilege);
        }
    }

    @Transactional
    @Override
    public int deleteRoleCluster(String clusterId) throws Exception{
        AssertUtil.notBlank(clusterId, DictEnum.CLUSTER);
        List<Role> roles = this.getAllRoleList();
        int count =0;
        for(Role role : roles){
            if(StringUtils.isBlank(role.getClusterIds())
                    || !role.getClusterIds().contains(clusterId)){
               continue;
            }
            if(clusterId.equalsIgnoreCase(role.getClusterIds().trim())){
                role.setUpdateTime(DateUtil.getCurrentUtcTime());
                role.setAvailable(Boolean.FALSE);
                role.setClusterIds(null);
                roleMapper.updateByPrimaryKey(role);
                count++;
                continue;
            }
            String[] clusterIds = role.getClusterIds().split(CommonConstant.COMMA);
            String strClusterIds = "";
            for(String id : clusterIds){
                if(id.equalsIgnoreCase(clusterId)){
                    continue;
                }
                strClusterIds += id + CommonConstant.COMMA;
            }
            strClusterIds = strClusterIds.substring(0, strClusterIds.length()-1);
            role.setUpdateTime(DateUtil.getCurrentUtcTime());
            role.setClusterIds(strClusterIds);
            roleMapper.updateByPrimaryKey(role);
            count ++ ;
        }
        return count;
    }

    /**
     * 启用角色
     * @param roleId
     * @param clusterIds 作用域
     * @throws Exception
     */
    public void enableRoleByRoleId(Integer roleId,String clusterIds) throws Exception {
        //获取角色
        Role role = this.getRoleById(roleId);
        if (Objects.isNull(role)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        int spType = 0;
        switch (this.scopeType){
            case ONE :
                spType = CommonConstant.NUM_ROLE_ADMIN;
                break;
            case TWO :
                spType = CommonConstant.NUM_ROLE_TM;
                break;
            case THREE :
                spType = CommonConstant.NUM_ROLE_PM;
                break;
            default:
                spType = CommonConstant.NUM_ROLE_ADMIN;
                break;
        }

        if (StringUtils.isNotBlank(clusterIds) && roleId > spType){//初始化管理员类不给设置作用域
            //去除无效的集群id
            String[] split = clusterIds.split(CommonConstant.COMMA);
            List<String> clusteridList = Arrays.stream(split).
                    filter(clusterId -> StringUtils.isNotBlank(clusterId)).collect(Collectors.toList());
            String newClusterIds = StringUtils.join(clusteridList, CommonConstant.COMMA);
            //设置作用域
            role.setClusterIds(newClusterIds);
        }
        if (StringUtils.isBlank(clusterIds) && roleId > spType ){
            //当非全局作用域角色为未启用的角色时，在启用时如果作用域为空必须要选择作用域
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_SCOPE_NOT_BLANK);
        }
        role.setUpdateTime(DateUtil.getCurrentUtcTime());
        //更新状态
        role.setAvailable(Boolean.TRUE);
        roleMapper.updateByPrimaryKey(role);
        Boolean status = clusterCacheManager.getRolePrivilegeStatus(roleId,null);
        if (status){
            clusterCacheManager.updateRolePrivilegeStatus(roleId,null,Boolean.FALSE);
        }
        //创建、更新 harbor  权限
        if(enableHarborRole) {
            HarborMemberEnum targetMember = rolePrivilegeService.getHarborRole(roleId);
            List<UserRoleRelationship> users = this.userRoleRelationshipService.listUserByRoleId(roleId);
            addHarborMember(targetMember, users);
        }
    }
    public void addHarborUserRole(HarborMemberEnum targetMember,String projectId,String username,Integer roleId) throws Exception{
//        UserRoleRelationship user = this.userRoleRelationshipService.getUser(projectId, username, roleId);

        // 根据project id  获取harbor project列表
        List<ImageRepository> imageRepositories = harborProjectService.listRepository(projectId, null, Objects.isNull(projectId)?null:Boolean.FALSE, null);
        for (ImageRepository repository : imageRepositories) {
            List<HarborUser> harborUsers = harborUserService.usersOfProjectByUsername(repository.getHarborHost(), repository.getHarborProjectId(), username);
            Integer targetlevel = targetMember.getLevel();
            boolean updateStatus = false;
            boolean createStatus = false;
            if (!CollectionUtils.isEmpty(harborUsers)) {
                String rolename = harborUsers.get(0).getRoleName();
                Integer currentlevel = HarborMemberEnum.getMemberLevel(rolename).getLevel();
                if (currentlevel <= targetlevel) {
                    targetlevel = currentlevel;
                } else {
                    updateStatus = true;
                }
            } else {
                createStatus = true;
            }
            HarborRole harborRole = new HarborRole();
            harborRole.setUsername(username);
            List<Integer> roleList = new ArrayList<>();
            roleList.add(targetlevel);
            harborRole.setRoleList(roleList);
            if (createStatus) {
                harborUserService.createRole(repository.getHarborHost(), repository.getHarborProjectId(), harborRole);
            } else if (updateStatus) {
                harborUserService.updateRole(repository.getHarborHost(), repository.getHarborProjectId(), harborUsers.get(0).getUserId(), harborRole);
            }

        }
    }
    public void addHarborMember(HarborMemberEnum targetMember, List<UserRoleRelationship> users) throws Exception{
        for (UserRoleRelationship userRoleRelationship : users) {
            this.addHarborUserRole(targetMember,userRoleRelationship.getProjectId(),userRoleRelationship.getUsername(),userRoleRelationship.getRoleId());
        }
    }
    /**
     * 根据角色名删除角色
     *
     * @param roleName
     * @throws Exception
     */
    @Override
    public void deleteRoleByRoleName(String roleName) throws Exception {
        RoleExample example = this.getExample();
        example.createCriteria().andNameEqualTo(roleName);
        List<Role> roles = this.roleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(roles)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        this.roleMapper.deleteByPrimaryKey(roles.get(0).getId());
    }

    /**
     * 根据昵称删除角色
     *
     * @param nickName
     * @throws Exception
     */
    @Transactional
    @Override
    public void deleteRoleByNickName(String nickName) throws Exception {
        //判断角色是否存在
        Role roleByNickName = this.getRoleByNickName(nickName);
        if (Objects.isNull(roleByNickName)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        Integer roleId = roleByNickName.getId();
        //检查用户角色关联
        List<UserRoleRelationship> users = this.userRoleRelationshipService.listUserByNickName(nickName);
        if (!CollectionUtils.isEmpty(users)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_USER_EXIST);
        }
        if (roleId <= CommonConstant.NUM_SEVEN){
            throw new MarsRuntimeException(ErrorCodeMessage.INIT_ROLE_CANNOT_DELETE);
        }
        // 删除角色权限
        this.rolePrivilegeService.deleteRolePrivilegeByRoleId(roleId);
        //TODO 删除角色菜单资源
        //删除角色
        this.roleMapper.deleteByPrimaryKey(roleId);
    }

    /**
     * 根据id删除角色
     *
     * @param id
     * @throws Exception
     */
    @Transactional
    @Override
    public void deleteRoleById(Integer id) throws Exception {
        Role role = this.roleMapper.selectByPrimaryKey(id);
        if (Objects.isNull(role)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        //检查用户角色关联
        List<UserRoleRelationship> users = this.userRoleRelationshipService.listUserByRoleId(id);
        if (!CollectionUtils.isEmpty(users)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_USER_EXIST);
        }
        if (id <= CommonConstant.NUM_SEVEN){
            throw new MarsRuntimeException(ErrorCodeMessage.INIT_ROLE_CANNOT_DELETE);
        }
        //更新redis中用户的状态
        clusterCacheManager.updateRolePrivilegeStatus(id,null,Boolean.TRUE);
        // 删除角色权限
        this.rolePrivilegeService.deleteRolePrivilegeByRoleId(id);
        // 删除角色菜单资源
        this.resourceMenuRoleService.deleteResourceMenuRoleByRoleId(id);
        //删除角色
        this.roleMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改角色
     *
     * @param roleDto
     * @throws Exception
     */
    @Transactional
    @Override
    public void updateRole(RoleDto roleDto) throws Exception {
        int roleId = roleDto.getId();
        Role oriRole = this.roleMapper.selectByPrimaryKey(roleId);

        //1.check role whether exist
        if (oriRole == null){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        //2 admin cannot be updated.
        if (roleId == CommonConstant.NUM_ROLE_ADMIN){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_PRIVILEGE_CANNOT_UPDATE);
        }
        //只修改项目管理员以下的作用域
        if (roleId > PM_ROLEID
                && (Objects.isNull(oriRole.getClusterIds())
                || !oriRole.getClusterIds().equals(roleDto.getClusterIds()))){
            oriRole.setClusterIds(roleDto.getClusterIds());
        }
        //只修改新增角色的昵称
        if( roleId > CommonConstant.UAT_ROLEID &&(!oriRole.getNickName().equals(roleDto.getNickName()))){
            oriRole.setNickName(roleDto.getNickName());
        }
        oriRole.setUpdateTime(DateUtil.getCurrentUtcTime());
        this.roleMapper.updateByPrimaryKeySelective(oriRole);
        //3 update role_privilege
        List<PrivilegeDto> rolePrivilegeList = roleDto.getRolePrivilegeList();


        if(!CollectionUtils.isEmpty(rolePrivilegeList)){
            rolePrivilegeService.updateRolePrivilege(roleId, rolePrivilegeList);
            clusterCacheManager.updateRolePrivilegeStatus(roleId,null,Boolean.TRUE);
        }
        List<UserRoleRelationship> users = this.userRoleRelationshipService.listUserByRoleId(roleId);
        if (CollectionUtils.isEmpty(users)){
            return ;
        }

        if(enableHarborRole) {
            //   获取当前角色的镜像权限
            HarborMemberEnum currentMember = rolePrivilegeService.getHarborRole(roleId);

            //   获取修改角色的镜像权限
            HarborMemberEnum targetMember = rolePrivilegeService.getHarborRole(roleId);
            // 判定修改角色 是否导致人员关于镜像库权限变更
            if (currentMember.getLevel() != targetMember.getLevel()) {
                if (currentMember == HarborMemberEnum.NONE) {
                    //add
                    addHarborMember(targetMember, users);
                } else {
                    //update or delete
                    updateHarborMember(targetMember, users);
                }

            }
        }

    }

    /**
     * 更新角色
     *
     * @param role
     * @throws Exception
     */
    @Override
    public void updateRole(Role role) throws Exception {
        Role oriRole = this.roleMapper.selectByPrimaryKey(role.getId());
        if (Objects.isNull(oriRole)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        this.roleMapper.updateByPrimaryKey(role);
    }

    /**
     * 根据角色名获取角色
     *
     * @param roleName
     * @throws Exception
     */
    @Override
    public Role getRoleByRoleName(String roleName) throws Exception {
        RoleExample example = this.getExample();
        example.createCriteria().andNameEqualTo(roleName);
        List<Role> roles = roleMapper.selectByExample(example);
        if (!roles.isEmpty()){
            return  roles.get(0);
        }
        return null;
    }

    @Override
    public void initHarborRole() throws Exception {
        List<Role> availableRoleList = this.getAvailableRoleList();
        for (Role role : availableRoleList) {
            Integer roleId = role.getId();
            List<UserRoleRelationship> userRoleRelationships = this.userRoleRelationshipService.listUserByRoleId(roleId);
            for (UserRoleRelationship userRoleRelationship : userRoleRelationships) {
                //处理harbor的角色权限关系
                if (roleId <= CommonConstant.NUM_ROLE_PM){
                    if (roleId == CommonConstant.TM_ROLEID){
                        try {
                            List<Project> projectList = this.projectService.listTenantProjectByTenantidInner(userRoleRelationship.getTenantId());
                            for (Project project : projectList) {
                                this.addHarborUserRole(HarborMemberEnum.PROJECTADMIN,project.getProjectId(),userRoleRelationship.getUsername(),roleId);
                            }
                        }catch (Exception e){
                            log.error("sync harbor member failed",e);
                        }
                    }else{
                        this.addHarborUserRole(HarborMemberEnum.PROJECTADMIN,userRoleRelationship.getProjectId(),userRoleRelationship.getUsername(),roleId);
                    }
                }else {
                    HarborMemberEnum targetMember =  rolePrivilegeService.getHarborRole(roleId);
                    this.addHarborUserRole(targetMember,userRoleRelationship.getProjectId(),userRoleRelationship.getUsername(),roleId);
                }
            }
        }

    }

    @Override
    public void switchDataCenter(Integer roleId, String dataCenter) throws Exception {
        //项目管理员及以上角色权限拥有所有数据中心的权限，不需要校验数据中心权限
        if (roleId <= PM_ROLEID) {
            session.setAttribute(CommonConstant.DATA_CENTER, dataCenter);
            return;
        }
        List<Cluster> clusters = this.getClusterListByRoleId(roleId);
        List<Cluster> filteredClusters = clusters.stream()
                .filter(cluster -> cluster.getDataCenter().equals(dataCenter)).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(filteredClusters)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_DATA_CENTER_ACCESS_DENIED);
        }
        session.setAttribute(CommonConstant.DATA_CENTER, dataCenter);
    }

    @Override
    public Set<DataCenterDto> getRoleDataCenter(Integer roleId) throws Exception {
        Set<DataCenterDto> dataCenterDtos = new HashSet<>();
        String currentDataCenter = this.getCurrentDataCenter();
        //项目管理员以上的角色权限拥有所有数据中心
        if (roleId <= PM_ROLEID) {
            ActionReturnUtil response = dataCenterService.listDataCenter(false, null);
            if (response.isSuccess()) {
                List<DataCenterDto> dataCenters = (List<DataCenterDto>)response.getData();
                for(DataCenterDto dataCenterDto : dataCenters){
                    if(dataCenterDto.getName().equals(TOP_DATACENTER)){
                        continue;
                    }
                    if(dataCenterDto.getName().equals(currentDataCenter)){
                        dataCenterDto.setIsCurrent(Boolean.TRUE);
                    }
                    dataCenterDtos.add(dataCenterDto);
                }
                return dataCenterDtos;
            }
        }
        List<Cluster> clusters = this.getClusterListByRoleId(roleId);
        for(Cluster cluster : clusters){
            DataCenterDto dataCenterDto = new DataCenterDto();
            dataCenterDto.setName(cluster.getDataCenter());
            dataCenterDto.setAnnotations(cluster.getDataCenterName());
            //设置是否是当前用户选择的数据中心
            dataCenterDto.setIsCurrent(cluster.getDataCenter().equals(currentDataCenter));
            dataCenterDtos.add(dataCenterDto);
        }
        return dataCenterDtos;
    }

    private RoleExample getExample() throws Exception {
        return  new RoleExample();
    }
}
