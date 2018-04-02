package com.harmonycloud.service.tenant.impl;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.dao.user.bean.*;
import com.harmonycloud.dto.user.UserGroupDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dao.tenant.bean.*;
import com.harmonycloud.dto.tenant.*;
import com.harmonycloud.service.application.PersistentVolumeService;
import com.harmonycloud.service.cache.ClusterCacheManager;
import com.harmonycloud.service.platform.bean.NodeDto;
import com.harmonycloud.service.platform.service.NodeService;
import com.harmonycloud.service.tenant.*;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.RolePrivilegeService;
import com.harmonycloud.service.user.UserRoleRelationshipService;
import jnr.ffi.annotations.Synchronized;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.network.NetworkCalicoMapper;
import com.harmonycloud.dao.tenant.TenantBindingMapper;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.service.NetworkPolicyService;
import com.harmonycloud.k8s.service.PersistentvolumeService;
import com.harmonycloud.k8s.service.RoleBindingService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.ConfigCenterService;
import com.harmonycloud.service.platform.service.ExternalService;
import com.harmonycloud.service.user.UserService;
import org.springframework.util.CollectionUtils;

/**
 * Created by andy on 17-1-9.
 */

@Service("tenantService")
@Transactional(rollbackFor = Exception.class)
public class TenantServiceImpl implements TenantService {

    @Autowired
    TenantBindingMapper tenantBindingMapper;
    @Autowired
    NamespaceService namespaceService;
    @Autowired
    com.harmonycloud.k8s.service.NamespaceService namespaceService1;
    @Autowired
    PersistentvolumeService persistentvolumeService;
    @Autowired
    RoleBindingService roleBindingService;
    @Autowired
    NetworkCalicoMapper networkCalicoMapper;
    @Autowired
    NetworkService networkService;
    @Autowired
    PersistentVolumeService persistentVolumeService;
    @Autowired
    NetworkPolicyService networkPolicyService;
    @Autowired
    ClusterService clusterService;
    @Autowired
    ExternalService externalService;

    @Autowired
    ConfigCenterService configCenterService;
    @Autowired
    UserService userService;
    @Autowired
    HttpSession session;
    @Autowired
    UserRoleRelationshipService userRoleRelationshipService;
    @Autowired
    ProjectService projectService;
    @Autowired
    NamespaceLocalService namespaceLocalService;
    @Autowired
    RoleLocalService roleLocalService;
    @Autowired
    TenantClusterQuotaService tenantClusterQuotaService;
    @Autowired
    TenantPrivateNodeService tenantPrivateNodeService;
    @Autowired
    NodeService nodeService;
    @Autowired
    ClusterCacheManager clusterCacheManager;
    @Autowired
    private RolePrivilegeService rolePrivilegeService;

    public static final String PROJECTMGR = "0005";
    //租户类型
    public static final String CATEGORY_TENANT = "0";
    //项目类型
    public static final String CATEGORY_PROJECT = "1";

//    @Value("#{propertiesReader['network.networkFlag']}")
    private String networkFlag;

    private static final Logger logger = LoggerFactory.getLogger(TenantServiceImpl.class);

    /**
     * 切换租户
     *
     * @param tenantId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String,Object> switchTenant(String tenantId) throws Exception {
        Map<String,Object> result = new HashMap<>();
        //有效值判断
        TenantBinding tenant = this.getTenantByTenantid(tenantId);
        if (Objects.isNull(tenant)){
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_TENANTID);
        }
        //获取当前用户名
        String username = this.userService.getCurrentUsername();
        List<TenantBinding> tenantBindings = this.tenantListByUsernameInner(username);
        //检查切换的租户是否在用户能切换的租户范围之内
        boolean contains = tenantBindings.contains(tenant);
        if (!contains){
            throw new MarsRuntimeException(ErrorCodeMessage.SWITCH_TENANT_INCORRECT,tenant.getTenantName(),Boolean.TRUE);
        }
        boolean admin = userService.isAdmin(username);
        //设置租户id到session
        session.setAttribute(CommonConstant.TENANT_ID, tenantId);
        session.setAttribute(CommonConstant.TENANT_ALIASNAME, tenant.getAliasName());
        String sessionId = session.getId();
        logger.info("sessionId:" + sessionId);
        List<Project> projects = null;
        Boolean tm = this.isTm(tenantId);
        if(admin || tm){
            //为系统管理员或者租户管理员获取该租户下所有项目列表
            session.setAttribute(CommonConstant.ROLEID, CommonConstant.ADMIN_ROLEID);
            projects = this.projectService.listTenantProjectByTenantidInner(tenantId);
        } else {
            //不为系统管理员获取该租户下用户所拥有的项目列表
            projects = this.projectService.listTenantProjectByUsername(tenantId, username);
        }
        List<Role> roleList = this.roleLocalService.getRoleListByUsernameAndTenantId(username,tenantId);
        if (!CollectionUtils.isEmpty(projects) && !CollectionUtils.isEmpty(roleList) && !admin && !tm){
            Map<String, Project> collect = projects.stream().collect(Collectors.toMap(Project::getProjectId, project -> project));
            projects.clear();
            Map<String,Object> map = new HashMap<>();
            for (Role role:roleList) {
                List<UserRoleRelationship> userRoleRelationshipList = this.userRoleRelationshipService.getUserRoleRelationshipList(username, role.getId());
                for (UserRoleRelationship userRoleRelationship : userRoleRelationshipList) {
                    Object object = map.get(userRoleRelationship.getProjectId());
                    if (Objects.isNull(object)){
                        Project project = collect.get(userRoleRelationship.getProjectId());
                        if (!Objects.isNull(project)){
                            projects.add(project);
                            map.put(project.getProjectId(),project);
                        }
                    }
                }
            }
        }
        //设置默认角色id为角色列表的第一个角色id
        if (!CollectionUtils.isEmpty(roleList)){
            session.setAttribute(CommonConstant.ROLEID, roleList.get(0).getId());
        }
        if (roleList.size() <= 0){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_DISABLE);
        }
        if (!CollectionUtils.isEmpty(projects)){
            roleList = this.projectService.switchProject(tenantId, projects.get(0).getProjectId());
        } else {
            //项目为空不切换项目
            session.removeAttribute(CommonConstant.PROJECTID);
            rolePrivilegeService.switchRole(roleList.get(0).getId());
        }
        result.put(CommonConstant.PROJECTLIST,projects);
        result.put(CommonConstant.ROLELIST,roleList);
        return result;
    }

    @Override
    public List<TenantDto> tenantList() throws Exception {
        //获取当前用户名
        Object usernameObj = session.getAttribute("username");
        if(usernameObj == null){
            return Collections.emptyList();
        }
        String username = usernameObj.toString();
        //判断用户是否为系统管理员
        boolean isAdmin = userService.isAdmin(username);
        List<TenantBinding> tenantList = null;
        List<TenantDto> list = new ArrayList<TenantDto>();
        if (isAdmin){
            //如果是admin则查询所有的租户
            tenantList = this.listAllTenant();
        }else{
            //如果不是admin则查询当前用户的租户
            tenantList = userRoleRelationshipService.listTenantByUsername(username);
        }
        //如果租户列表不为空，组装数据返回
        if (!CollectionUtils.isEmpty(tenantList)){
            for (TenantBinding tenantBinding:tenantList) {
                TenantDto tenantDto = new TenantDto();
                String tenantId = tenantBinding.getTenantId();
                tenantDto.setCreateTime(tenantBinding.getCreateTime());
                tenantDto.setUpdateTime(tenantBinding.getUpdateTime());
                tenantDto.setTenantId(tenantId);
                tenantDto.setAliasName(tenantBinding.getAliasName());
                tenantDto.setTenantName(tenantBinding.getTenantName());
                tenantDto.setCreateUserAccount(tenantBinding.getCreateUserAccount());
                tenantDto.setCreateUserName(tenantBinding.getCreateUserName());

                //获取租户下的项目简单列表
                List<Project> projectList = projectService.listTenantProjectByTenantidInner(tenantId);
                tenantDto.setProjectNum(projectList.size());
                //获取租户下的分区简单列表
                List<NamespaceLocal> namespaceList = namespaceLocalService.getAllNamespaceListByTenantId(tenantId);
                List<UserRoleRelationship> userRoleRelationships = userRoleRelationshipService.listTmByTenantId(tenantId);
                List<String> tmList = new ArrayList<>();
                for (UserRoleRelationship userRoleRelationship:userRoleRelationships) {
                    tmList.add(userRoleRelationship.getUsername());
                }
                int nsNum = 0;
                if (!CollectionUtils.isEmpty(namespaceList)){
                    nsNum = namespaceList.size();
                }
                tenantDto.setNamespaceNum(nsNum);
                tenantDto.setTmNum(tmList.size());
                list.add(tenantDto);
            }
        }

        return list;
    }
    @Override
    public List<TenantBinding> tenantListByUsernameInner(String username) throws Exception {
        List<TenantBinding> tenantList = null;
        boolean isAdmin = userService.isAdmin(username);
        if (isAdmin){
            //如果是admin则查询所有的租户
            tenantList = this.listAllTenant();
        }else{
            //如果不是admin则查询当前用户的租户
            tenantList = userRoleRelationshipService.listTenantByUsername(username);
        }
        return tenantList;
    }

    @Override
    public TenantDto getTenantDetail(String tenantId) throws Exception {
        // 初始化判断1
        AssertUtil.notBlank(tenantId, DictEnum.TENANT_ID);
        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andTenantIdEqualTo(tenantId);
        List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
        if (list == null || list.size() <= 0 || list.get(0) == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_TENANTID);
        }
        TenantBinding tenantBinding = list.get(0);
        //组装tenantDto
        TenantDto tenantDto = this.generateTenantDto(tenantBinding);
        return tenantDto;
    }

    /**
     * 根据租户生产tenantDto
     * @param tenantBinding
     * @return
     * @throws Exception
     */
    private TenantDto generateTenantDto(TenantBinding tenantBinding) throws Exception{
        TenantDto tenantDto = new TenantDto();
        String tenantId = tenantBinding.getTenantId();
        List<UserRoleRelationship> userRoleRelationships = this.listTenantTm(tenantId);
        List<User> tmList = new ArrayList<>();
        for (UserRoleRelationship userRoleRelationship:userRoleRelationships) {
            User user = this.userService.getUser(userRoleRelationship.getUsername());
            tmList.add(user);
        }
        tenantDto.setTenantId(tenantId);
        tenantDto.setCreateUserName(tenantBinding.getCreateUserName());
        tenantDto.setCreateTime(tenantBinding.getCreateTime());
        tenantDto.setUpdateTime(tenantBinding.getUpdateTime());
        tenantDto.setTenantName(tenantBinding.getTenantName());
        tenantDto.setTmUserList(tmList);
        tenantDto.setTmNum(tmList.size());
        tenantDto.setAliasName(tenantBinding.getAliasName());
        tenantDto.setAnnotation(tenantBinding.getAnnotation());
        tenantDto.setCreateUserAccount(tenantBinding.getCreateUserAccount());
        tenantDto.setUpdateUserAccount(tenantBinding.getUpdateUserAccount());
        //设置项目列表
        List<ProjectDto> projectList = this.projectService.listTenantProjectByTenantid(tenantId);
        tenantDto.setProjectList(projectList);
        List<Map<String, Object>> namespaceListByTenantid = namespaceService.getNamespaceListByTenantid(tenantId);
        //设置租户分区列表
        tenantDto.setNamespaceList(namespaceListByTenantid);
        tenantDto.setNamespaceNum(namespaceListByTenantid.size());
//        Map<String, List> clusterQuotaListByTenantid = namespaceService.getClusterQuotaListByTenantid(tenantId);
//        tenantDto.setClusterQuotaUsage(clusterQuotaListByTenantid);
        //获取租户集群配额
        List<ClusterQuotaDto> tenantClusterQuotas = tenantClusterQuotaService.listClusterQuotaByTenantid(tenantId,null);
        Iterator<ClusterQuotaDto> iterator = tenantClusterQuotas.iterator();
        Integer currentRoleId = this.userService.getCurrentRoleId();
        Role role = this.roleLocalService.getRoleById(currentRoleId);
        if (Objects.isNull(role)){
            return null;
        }
        String clusterIds = role.getClusterIds();
        List<String> clusterIdList = null;
        if (StringUtils.isNotBlank(clusterIds)){
            clusterIdList = Arrays.stream(clusterIds.split(CommonConstant.COMMA)).collect(Collectors.toList());
        } else {
            List<Cluster> clusterList = clusterService.listCluster();
            clusterIdList = clusterList.stream().map(cluster -> cluster.getId()).collect(Collectors.toList());
        }
        //如果集群状态不可用，不显示已经设置的配额
        while(iterator.hasNext()){
            ClusterQuotaDto clusterQuotaDto = iterator.next();
            if(clusterService.findClusterById(clusterQuotaDto.getClusterId()) == null
                    || !clusterService.findClusterById(clusterQuotaDto.getClusterId()).getIsEnable()
                    || !clusterIdList.contains(clusterQuotaDto.getClusterId())){
                iterator.remove();
            }
        }
        List<TenantPrivateNode> tenantPrivateNodeList = tenantPrivateNodeService.listTenantPrivateNode(tenantId);
        tenantDto.setClusterQuota(tenantClusterQuotas);
        tenantDto.setTenantPrivateNode(tenantPrivateNodeList);
        tenantDto.setNodeNum(tenantPrivateNodeList.size());
        return tenantDto;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void createTenant(TenantDto tenantDto) throws Exception {
        //查询要创建的租户是否存在
        String tenantName = tenantDto.getTenantName();
        String tenantId = tenantDto.getTenantId();
        String aliasName = tenantDto.getAliasName();
        TenantBinding tenantBytenantName = this.getTenantBytenantName(tenantName.trim());
        if (!Objects.isNull(tenantBytenantName)) {
            throw new MarsRuntimeException(ErrorCodeMessage.TENANTNAME_EXIST,tenantName.trim(),Boolean.TRUE);
        }
        TenantBinding tenantByAliasName = getTenantByAliasName(aliasName.trim());
        if (!Objects.isNull(tenantByAliasName)) {
            throw new MarsRuntimeException(ErrorCodeMessage.TENANTALIASNAME_EXIST,aliasName.trim(),Boolean.TRUE);
        }
        // 如果没有tenantId生成tenantId
        if (StringUtils.isBlank(tenantId)){
            tenantId = this.getid();
        }
        tenantDto.setTenantId(tenantId);

        // 组装tmUsers
        List<String> tmList = tenantDto.getTmList();
        // 组装TenantBinding
        TenantBinding tenantBinding = new TenantBinding();
        Date date = DateUtil.getCurrentUtcTime();
        //设置租户备注
        tenantBinding.setAnnotation(tenantDto.getAnnotation());
        //设置租户id
        tenantBinding.setTenantId(tenantId);
        //设置租户名称
        tenantBinding.setAliasName(aliasName);
        //设置租户创建时间
        tenantBinding.setCreateTime(date);
        tenantBinding.setTmUsernames(CommonConstant.EMPTYSTRING);
        //设置租户名
        tenantBinding.setTenantName(tenantName.trim());
        //设置租户同步devops项目集主数据盈科方面数据
        tenantBinding.setTenantSystemCode(tenantDto.getSysCode());
        tenantBinding.setUpdateUserAccount(tenantDto.getUpdateUserAccount());
        tenantBinding.setUpdateUserId(tenantDto.getUpdateUserId());
        tenantBinding.setUpdateUserName(tenantDto.getUpdateUserName());
        tenantBinding.setCreateUserAccount(tenantDto.getCreateUserAccount());
        tenantBinding.setCreateUserId(tenantDto.getCreateUserId());
        tenantBinding.setCreateUserName(tenantDto.getCreateUserName());
        //创建租户
        tenantBindingMapper.insertSelective(tenantBinding);
        //添加租户在每个集群下的配额
        this.createClusterQuotaByTenantId(tenantDto);
        //创建租户用户群组
        UserGroup usergGroup = new UserGroup();
        usergGroup.setGroupname(tenantName.trim());
        userService.createGroup(usergGroup);
        if(!CollectionUtils.isEmpty(tmList)){
            // 创建租户管理员
            this.createTm(tenantId,tmList);
        }
    }

    @Override
    public TenantDto getTenantDetailByTenantName(String tenantName) throws Exception {
        // 初始化判断1
        AssertUtil.notBlank(tenantName, DictEnum.TENANT_NAME);
        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andTenantIdEqualTo(tenantName);
        List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
        if (list == null || list.size() <= 0 || list.get(0) == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_TENANTNAME);
        }
        TenantDto tenantDto = generateTenantDto(list.get(0));
        return tenantDto;
    }

    @SuppressWarnings(CommonConstant.UNCHECKED)
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteTenantByTenantId(String tenantId) throws Exception {
        // 有效值判断
        TenantBinding tenantBinding = this.getTenantByTenantid(tenantId);
        if (tenantBinding == null){
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_TENANTID);
        }
        // 调用namespace接口查询是否有namespace
        Boolean isExitNamespace = isExitNamespace(tenantId);
        if (isExitNamespace) {
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_DELETE_FIRST);
        }
        // 调用项目接口查询是否有项目
        Boolean isExitProject = isExitProject(tenantId);
        if (isExitProject) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_DELETE_FIRST);
        }

        // 删除tenant信息
        tenantBindingMapper.deleteByPrimaryKey(tenantBinding.getId());
        // 删除租户的管理员关联
        userRoleRelationshipService.deleteUserRoleRelationshipByTenantId(tenantId);
        // 删除租户的集群配额
        tenantClusterQuotaService.deleteClusterQuotaByTenantId(tenantId);
        //删除租户成员群组
        UserGroup group = userService.getGroupByGroupName(tenantBinding.getTenantName());
        if (!Objects.isNull(group)){
            userService.deleteGroupbyId(group.getId());
        }
        List<TenantPrivateNode> tenantPrivateNodes = this.tenantPrivateNodeService.listTenantPrivateNode(tenantId);
        for (TenantPrivateNode tenantPrivateNode:tenantPrivateNodes) {
            this.dealDeletePrivateNode(tenantPrivateNode,tenantBinding);
        }
    }
    /**
     * 修改租户在集群下的配额
     * @param tenantId
     * @param clusterQuota
     * @throws Exception
     */
    @Override
    public void updateTenant(String tenantId,List<ClusterQuotaDto> clusterQuota) throws Exception{
        // 有效值判断
        TenantBinding tenantBinding = this.getTenantByTenantid(tenantId);
        if (tenantBinding == null){
            throw new MarsRuntimeException(ErrorCodeMessage.NOT_FOUND, DictEnum.TENANT.phrase(),true);
        }
        //修改配额有效值检查
        Boolean checkClusterQuota = this.checkClusterQuota(clusterQuota,tenantId);
        if (!checkClusterQuota){
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTERQUOTA_INCORRECT);
        }
        for (ClusterQuotaDto clusterQuotaDto:clusterQuota) {
            this.updateClusterQuotaByTenantid(clusterQuotaDto.getId(),clusterQuotaDto.getCpuQuota(),clusterQuotaDto.getMemoryQuota());
        }
    }
    //根据id更新集群配额
    private void updateClusterQuotaByTenantid(Integer id,Double cpu,Double memory) throws Exception{
        //使用用户分配的配额更新集群配额
        TenantClusterQuota quota = tenantClusterQuotaService.getClusterQuotaById(id);
        if (quota == null){
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        quota.setCpuQuota(cpu);
        quota.setMemoryQuota(memory);
        Date date = DateUtil.getCurrentUtcTime();
        quota.setUpdateTime(date);
        tenantClusterQuotaService.updateClusterQuota(quota);
    }
    //修改配额有效值检查
    private Boolean checkClusterQuota(List<ClusterQuotaDto> clusterQuota,String tenantId) throws Exception{
        Lock lock = new ReentrantLock();
        lock.lock();
        try{
            Boolean status = Boolean.TRUE;
            for (ClusterQuotaDto clusterQuotaDto:clusterQuota) {
                //新建时id为null
                Integer id = clusterQuotaDto.getId();
                TenantClusterQuota quota = null;
                if (!Objects.isNull(id)){
                    quota = this.tenantClusterQuotaService.getClusterQuotaById(id);
                }
                Double cpuQuota = clusterQuotaDto.getCpuQuota();
                Double memoryQuota = clusterQuotaDto.getMemoryQuota();
                Double lastCpu = 0d;
                Double lastMemory = 0d;
                String clusterId = null;
                if (!Objects.isNull(quota)){
                    lastCpu = quota.getCpuQuota();
                    lastMemory = quota.getMemoryQuota();
                    clusterId = quota.getClusterId();
                }else {
                    clusterId = clusterQuotaDto.getClusterId();
                }

                //空值判断
                if (Objects.isNull(cpuQuota) || Objects.isNull(memoryQuota)){
                    throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
                }
                Map<String, Map<String, Object>> clusterAllocatedResources = clusterService.getClusterAllocatedResources(clusterId);
                Map<String, Object> stringObjectMap = clusterAllocatedResources.get(clusterId);
                Object clusterCpuAllocatedObj = stringObjectMap.get("clusterCpuAllocatedResources");
                Object clusterMemoryAllocatedObj = stringObjectMap.get("clusterMemoryAllocatedResources");
                if (Objects.isNull(clusterCpuAllocatedObj) || Objects.isNull(clusterMemoryAllocatedObj)){
                    throw new MarsRuntimeException(ErrorCodeMessage.UNKNOWN);
                }
                Double clusterCpuAllocatedResources = Double.valueOf(clusterCpuAllocatedObj.toString());
                Double clusterMemoryAllocatedResources = Double.valueOf(clusterMemoryAllocatedObj.toString());
                //获取集群使用配额
                this.tenantClusterQuotaService.getClusterUsage(tenantId,clusterId,clusterQuotaDto);
                //整个集群已经使用的配额
//            Double clusterUsedCpu = clusterQuotaDto.getClusterUsedCpu();
//            Double clusterUsedMemory = clusterQuotaDto.getClusterUsedMemory();
//            String clusterUsedMemoryType = clusterQuotaDto.getClusterUsedMemoryType();
                //租户内已经使用的配额
                Double usedCpu = clusterQuotaDto.getUsedCpu();
                Double usedMemory = clusterQuotaDto.getUsedMemory();
                String usedMemoryType = clusterQuotaDto.getUsedMemoryType();
                //总配额
//            Double totalCpu = clusterQuotaDto.getTotalCpu();
//            Double totalMemory = clusterQuotaDto.getTotalMomry();
//            String totalMemoryType = clusterQuotaDto.getTotalMemoryType();

//            //类型不一样，同一转化为MB
//            totalMemory = this.convertValue(totalMemoryType,totalMemory);
                if (!Objects.isNull(usedMemoryType)){
                    usedMemory = this.convertValue(usedMemoryType,usedMemory);
                }else {
                    usedMemory = 0d;
                    usedCpu = 0d;
                }
//            if (Objects.isNull(clusterUsedMemoryType)){
//                clusterUsedMemory = 0d;
//            }else {
//                clusterUsedMemory = this.convertValue(clusterUsedMemoryType,clusterUsedMemory);
//            }

                Double changeCpuValue = cpuQuota - lastCpu;
                Double changeMemoryValue = memoryQuota - lastMemory;
                if (cpuQuota < 0
                        || memoryQuota < 0
                        || changeCpuValue - (clusterCpuAllocatedResources) >= 0.1
                        || cpuQuota < usedCpu
                        || changeMemoryValue - (clusterMemoryAllocatedResources * 1024) >= 0.1
                        || memoryQuota < usedMemory){
                    status = Boolean.FALSE;
                }
            }
            return status;
        }finally {
            lock.unlock();
        }
    }
    private Double convertValue(String type,Double value){
        Double transformValue = 0D;
        switch (type){
            case CommonConstant.MB :
                transformValue = value;
                break;
            case CommonConstant.GB :
                transformValue = value * 1024;
                break;
            case CommonConstant.TB :
                transformValue = value * 1024 * 1024;
                break;
            case CommonConstant.PB :
                transformValue = value * 1024 * 1024 * 1024;
                break;
            default:
                throw new MarsRuntimeException(ErrorCodeMessage.INVALID_MEMORY_UNIT_TYPE);
        }
        return transformValue;
    }

    /**
     * 修改租户
     *
     * @param tenantDto
     * @throws Exception
     */
    @Override
    public void updateTenant(TenantDto tenantDto) throws Exception {
        String tenantId = tenantDto.getTenantId();
        TenantBinding tenant = this.getTenantByTenantid(tenantId);
        String aliasName = tenantDto.getAliasName();
        if (!Objects.isNull(aliasName)){
            tenant.setAliasName(aliasName);
        }
        //如果备注不为空，更新备注
        String annotation = tenantDto.getAnnotation();
        if (!Objects.isNull(annotation)){
            tenant.setAnnotation(annotation);
        }
        List<ClusterQuotaDto> clusterQuota = tenantDto.getClusterQuota();
        //如果配额不为空，更新配额
        if (!CollectionUtils.isEmpty(clusterQuota)){
            this.updateTenant(tenantId,clusterQuota);
        }
        List<TenantPrivateNode> addNodeList = tenantDto.getAddTenantPrivateNode();
        List<TenantPrivateNode> removeNode = tenantDto.getRemoveTenantPrivateNode();
        //如果有添加租户独占主机列表则处理
        if (!CollectionUtils.isEmpty(addNodeList)){
            for (TenantPrivateNode tenantPrivateNode : addNodeList) {
                tenantPrivateNode.setTenantId(tenantId);
                tenantPrivateNode.setCreateTime(DateUtil.getCurrentUtcTime());
                this.tenantPrivateNodeService.createTenantPrivateNode(tenantPrivateNode);
                // 更新node节点状态
                Map<String, String> newLabels = new HashMap<String, String>();
                newLabels.put(CommonConstant.HARMONYCLOUD_STATUS, CommonConstant.LABEL_STATUS_D);
                newLabels.put(CommonConstant.HARMONYCLOUD_TENANTNAME_NS, tenant.getTenantName());
                for (TenantPrivateNode node : addNodeList) {
                    ActionReturnUtil addNodeLabels = nodeService.addNodeLabels(node.getNodeName(), newLabels, node.getClusterId());
                    if ((Boolean) addNodeLabels.get(CommonConstant.SUCCESS) == false) {
                        throw new MarsRuntimeException(ErrorCodeMessage.NODE_LABEL_CREATE_ERROR);
                    }
                }
            }
        }
        //如果有删除租户独占主机列表则处理
        this.tenantPrivateNodeService.listTenantPrivateNode(tenantId);
        if (!CollectionUtils.isEmpty(removeNode)){
            for (TenantPrivateNode tenantPrivateNode : removeNode) {
                this.dealDeletePrivateNode(tenantPrivateNode,tenant);
            }
        }
        tenant.setUpdateTime(DateUtil.getCurrentUtcTime());
        this.tenantBindingMapper.updateByPrimaryKeySelective(tenant);
    }
    //处理删除租户独占主机
    private void dealDeletePrivateNode(TenantPrivateNode tenantPrivateNode,TenantBinding tenant )throws Exception{
        String nodeName = tenantPrivateNode.getNodeName();
        String clusterId = tenantPrivateNode.getClusterId();
        Cluster cluster = this.clusterService.findClusterById(clusterId);
        // 删除node节点状态
        Map<String, String> nodeStatusLabels = nodeService.listNodeStatusLabels(nodeName, cluster);
        Map<String, String> removelabels = new HashMap<String, String>();
        String HarmonyCloud_Status = nodeStatusLabels.get(CommonConstant.HARMONYCLOUD_STATUS);
        if (StringUtils.isBlank(HarmonyCloud_Status)) {
            throw new MarsRuntimeException(ErrorCodeMessage.NODE_LABEL_ERROR);
        }
        if (!HarmonyCloud_Status.equals(CommonConstant.LABEL_STATUS_D)) {
            throw new MarsRuntimeException(ErrorCodeMessage.NODE_LABEL_ERROR,CommonConstant.HARMONYCLOUD_STATUS
                    + CommonConstant.EQUALITY_SIGN
                    + HarmonyCloud_Status,true);
        }
        String tenantNsLabel = nodeStatusLabels.get(CommonConstant.HARMONYCLOUD_TENANTNAME_NS);
        if (StringUtils.isBlank(tenantNsLabel) || !tenantNsLabel.equals(tenant.getTenantName())) {
            throw new MarsRuntimeException(ErrorCodeMessage.NODE_CANNOT_REMOVED_FORTENANT);
        }
        removelabels.put(CommonConstant.HARMONYCLOUD_TENANTNAME_NS, nodeStatusLabels.get(CommonConstant.HARMONYCLOUD_TENANTNAME_NS));
        nodeStatusLabels.put(CommonConstant.HARMONYCLOUD_STATUS, CommonConstant.LABEL_STATUS_B);
        nodeStatusLabels.remove(CommonConstant.HARMONYCLOUD_TENANTNAME_NS);
        nodeService.addNodeLabels(nodeName, nodeStatusLabels, cluster.getId());
        nodeService.removeNodeLabels(nodeName, removelabels, cluster);
        this.tenantPrivateNodeService.deleteTenantPrivateNode(tenant.getTenantId(), clusterId,nodeName);
    }
    @Override
    public List<NodeDto> getTenantPrivateNodeList(String tenantid,String namespace,String clusterId) throws Exception {
        Cluster cluster = null;
        if (StringUtils.isNotBlank(namespace)){
            cluster = this.namespaceLocalService.getClusterByNamespaceName(namespace);
        } else {
            cluster = this.clusterService.findClusterById(clusterId);
        }
        TenantBinding tenant = this.getTenantByTenantid(tenantid);
        List<NodeDto> nodeDtos = new ArrayList<>();
        String label = CommonConstant.HARMONYCLOUD_TENANTNAME_NS + CommonConstant.EQUALITY_SIGN + tenant.getTenantName();
        List<NodeDto> nodeList = this.nodeService.listPrivateNodeByLabel(label, cluster);
        nodeDtos.addAll(nodeList);
        return nodeDtos;
    }

    /**
     * 租户是否包含存在的namespace
     *
     * @param
     * @return
     */
    private Boolean isExitNamespace(String tenantId) throws Exception {
        List<NamespaceLocal> namespaceList = namespaceLocalService.getAllNamespaceListByTenantId(tenantId);
        // 租户未绑定namespace，返回true
        if (CollectionUtils.isEmpty(namespaceList)) {
            return CommonConstant.FALSE;
        }
        return CommonConstant.TRUE;
    }
    /**
     * 租户是否包含存在的Project
     *
     * @param
     * @return
     */
    private Boolean isExitProject(String tenantId) throws Exception {
        List<Project> projectList = projectService.listTenantProjectByTenantidInner(tenantId);
        // 租户未绑定Project，返回true
        if (CollectionUtils.isEmpty(projectList)) {
            return CommonConstant.FALSE;
        }
        return CommonConstant.TRUE;
    }

    /**
     * 创建租户在每个集群下的配额
     * @param tenantDto
     * @throws Exception
     */
    private void createClusterQuotaByTenantId(TenantDto tenantDto)throws Exception{
        List<ClusterQuotaDto> clusterQuota = tenantDto.getClusterQuota();
        Map<String,ClusterQuotaDto> clusterQuotaMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(clusterQuota)){
            //修改配额有效值检查
            Boolean checkClusterQuota = this.checkClusterQuota(tenantDto.getClusterQuota(),tenantDto.getTenantId());
            if (!checkClusterQuota){
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTERQUOTA_INCORRECT);
            }
            clusterQuota.stream().forEach(quota -> {clusterQuotaMap.put(quota.getClusterId().toString(),quota);});
        }
        List<Cluster> clusterList = clusterService.listCluster();
        if (CollectionUtils.isEmpty(clusterList)){
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_LIST_NOT_BLANK);
        }
        Date date = DateUtil.getCurrentUtcTime();
        for (Cluster cluster:clusterList) {
            String tenantId = tenantDto.getTenantId();
            TenantClusterQuota quota = tenantClusterQuotaService.getClusterQuotaByTenantIdAndClusterId(tenantId, cluster.getId());
            if (Objects.isNull(quota)){
                quota = new TenantClusterQuota();
                quota.setCreateTime(date);
                quota.setClusterName(cluster.getName());
                quota.setTenantId(tenantId);
                quota.setClusterId(cluster.getId());
                if (clusterQuotaMap != null && clusterQuotaMap.get(cluster.getId().toString()) != null){
                    //使用用户分配的配额创建集群配额
                    ClusterQuotaDto clusterQuotaDto = clusterQuotaMap.get(cluster.getId().toString());
                    quota.setCpuQuota(clusterQuotaDto.getCpuQuota());
                    quota.setMemoryQuota(clusterQuotaDto.getMemoryQuota());
                }else {
                    //使用默认配额创建集群配额 0
                    quota.setCpuQuota(0d);
                    quota.setMemoryQuota(0d);
                }
                tenantClusterQuotaService.createClusterQuota(quota);
            }
        }
    }

    //删除租户管理员
    private void deleteUserRoleRaletionship(List<String> tmList,String tenantId) throws Exception{
        for (String tmUsername:tmList) {
            this.deleteTm(tenantId,tmUsername);
        }
    }

    @Override
    public TenantBinding getTenantBytenantName(String tenantName) throws Exception {
        AssertUtil.notBlank(tenantName, DictEnum.TENANT_NAME);
        TenantBindingExample exsit = new TenantBindingExample();
        exsit.createCriteria().andTenantNameEqualTo(tenantName);
        List<TenantBinding> listTenant = tenantBindingMapper.selectByExample(exsit);
        if (CollectionUtils.isEmpty(listTenant)){
            return null;
        }
        TenantBinding tenantBinding = listTenant.get(0);
        return tenantBinding;
    }
    public TenantBinding getTenantByAliasName(String aliasName) throws Exception {
        TenantBindingExample exsit = new TenantBindingExample();
        exsit.createCriteria().andAliasNameEqualTo(aliasName);
        List<TenantBinding> listTenant = tenantBindingMapper.selectByExample(exsit);
        if (CollectionUtils.isEmpty(listTenant)){
            return null;
        }
        TenantBinding tenantBinding = listTenant.get(0);
        return tenantBinding;
    }

    /**
     * 向租户下添加租户管理员
     *
     * @param tenantId
     * @param tmList
     * @return
     * @throws Exception
     */
    @Override
    public void createTm(String tenantId, List<String> tmList) throws Exception {
        //检查租户的有效性
        TenantBinding tenantBinding = this.getTenantByTenantid(tenantId);
        if (Objects.isNull(tenantBinding)){
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_TENANTID);
        }
        //获取租户管理员
        List<UserRoleRelationship> userRoleRelationships = this.listTenantTm(tenantId);
        Map<String,String> tmMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(userRoleRelationships)){
            userRoleRelationships.stream().forEach(tm -> {tmMap.put(tm.getUsername(),tm.getUsername());});
        }
        //处理添加多个租户管理员
        for (String user:tmList) {
            User user1 = this.userService.getUser(user);
            if (Objects.isNull(user1)){
                throw new MarsRuntimeException(ErrorCodeMessage.USER_NOT_EXIST);
            }
            UserGroup group = this.userService.getGroupByGroupName(tenantBinding.getTenantName());
            //如果已经是管理员则提示返回
            if (!Objects.isNull(tmMap) &&  StringUtils.isNotBlank(tmMap.get(user.trim()))){
                throw new MarsRuntimeException(ErrorCodeMessage.TENANT_TM_EXIST,user.trim(),true);
            }
            this.createTmRole(tenantId,user.trim());
            //如果成员不存在则添加
            UserGroupRelation userGroupRelation = this.userService.getGroup(user1.getId(), group.getId());
            if (Objects.isNull(userGroupRelation)){
                List<String> addUsers = new ArrayList<>();
                addUsers.add(user.trim());
                this.updateTenantMember(tenantId,addUsers,null);
            }
        }
        //更新TM关系至租户表
        String tms = tenantBinding.getTmUsernames();
        Date date = DateUtil.getCurrentUtcTime();
        if (StringUtils.isNotBlank(tms)){
            String tmUsers = (tms + CommonConstant.COMMA + StringUtils.join(tmList, CommonConstant.COMMA)).
                    replaceAll(CommonConstant.BLANKSTRING, CommonConstant.EMPTYSTRING);
            tenantBinding.setTmUsernames(tmUsers);
        }else{
            String tmUsers = (StringUtils.join(tmList,CommonConstant.COMMA)).
                    replaceAll(CommonConstant.BLANKSTRING, CommonConstant.EMPTYSTRING);
            tenantBinding.setTmUsernames(tmUsers);
        }
        tenantBinding.setUpdateTime(date);
        this.tenantBindingMapper.updateByPrimaryKeySelective(tenantBinding);
    }
    private void createTmRole(String tenantId, String tmUsername) throws Exception {
        User user = userService.getUser(tmUsername);
        if (user == null){
            throw new MarsRuntimeException(ErrorCodeMessage.NOT_FOUND, DictEnum.TENANT_MANAGER.phrase(),true);
        }
        Role role = roleLocalService.getRoleByRoleName(CommonConstant.TM);
        Integer roleId = role.getId();
        //添加用户至用户角色表
        UserRoleRelationship userRoleRelationship = new UserRoleRelationship();
        Date date = DateUtil.getCurrentUtcTime();
        userRoleRelationship.setCreateTime(date);
        userRoleRelationship.setRoleId(roleId);
        userRoleRelationship.setUsername(tmUsername);
        userRoleRelationship.setTenantId(tenantId);
        userRoleRelationshipService.createUserRoleRelationship(userRoleRelationship);
        //更新用户角色状态
        Boolean status = clusterCacheManager.getRolePrivilegeStatusForTenantOrProject(roleId,tmUsername,tenantId, null);
        if (status){
            clusterCacheManager.updateRolePrivilegeStatusForTenantOrProject(roleId,tmUsername,tenantId,null,Boolean.FALSE);
        }
    }
    /**
     * 租户移除租户管理员
     *
     * @param tenantId
     * @param username
     * @throws Exception
     */
    @Override
    public void deleteTm(String tenantId, String username) throws Exception {
        //处理空格
        final String newUsername = username.trim();
        UserRoleRelationship tmByTenantIdAndUsername = userRoleRelationshipService.getTmByTenantIdAndUsername(tenantId, newUsername);
        if (tmByTenantIdAndUsername == null){
            throw new MarsRuntimeException(ErrorCodeMessage.NOT_FOUND, DictEnum.TENANT_MANAGER.phrase(),true);
        }
        Integer roleId = tmByTenantIdAndUsername.getRoleId();
        //从用户角色表删除用户
        userRoleRelationshipService.deleteUserRoleRelationshipById(tmByTenantIdAndUsername.getId());
        //更新TM关系至租户表
        TenantBinding tenantBinding = this.getTenantByTenantid(tenantId);
        String tmUsernames = tenantBinding.getTmUsernames();
        if (!StringUtils.isEmpty(tmUsernames)){
            String[] tmUserList = tmUsernames.split(CommonConstant.COMMA);
            List<String> usersList = Arrays.stream(tmUserList).filter(tm -> !newUsername.equals(tm)).collect(Collectors.toList());
            String users = StringUtils.join(usersList, CommonConstant.COMMA);
            tenantBinding.setTmUsernames(users);
            Date date = DateUtil.getCurrentUtcTime();
            tenantBinding.setUpdateTime(date);
            this.tenantBindingMapper.updateByPrimaryKeySelective(tenantBinding);
        }
        //更新redis中用户的状态
        clusterCacheManager.updateRolePrivilegeStatusForTenantOrProject(roleId,username,tenantId,null,Boolean.TRUE);
    }

    public String getid() {
        // 通过uuid生成token
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        // 去掉"-"符号
        String id = str.replaceAll(CommonConstant.LINE, CommonConstant.EMPTYSTRING);
        return id;
    }

    @Override
    public TenantBinding getTenantByTenantid(String tenantid) {
        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andTenantIdEqualTo(tenantid);
        // 根据tenantid查询租户绑定信息
        List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
        if (list == null || list.size() <= 0 || list.get(0) == null) {
            return null;
        }
        return list.get(0);

    }

    @Override
    public Boolean isTm(String tenantId) throws Exception {
        //获取session的用户名
        Object currentUserObj = session.getAttribute("username");
        if(currentUserObj == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        String currentUserName = currentUserObj.toString();
        //获取租户管理员的列表
        List<UserRoleRelationship> tmList = this.listTenantTm(tenantId);
        //如果租户管理员列表为空则返回false
        if (CollectionUtils.isEmpty(tmList)){
            return false;
        }
        //查询当前用户是否在租户管理员列表内
        for (UserRoleRelationship tm:tmList) {
            if (currentUserName.equals(tm.getUsername())){
                return true;
            }
        }
        return false;
    }
    @Override
    public List<UserRoleRelationship> listTenantTm(String tenantid) throws Exception {
        // 初始化判断1
        AssertUtil.notBlank(tenantid, DictEnum.TENANT_ID);
        List<UserRoleRelationship> userRoleRelationships = userRoleRelationshipService.listTmByTenantId(tenantid);
        return userRoleRelationships;
    }

    @Override
    public Map<String, Object> listTenantQuota(String tenantId) throws Exception {
        // 初始化判断1
        AssertUtil.notBlank(tenantId, DictEnum.TENANT_ID);
        // 租户有效性判断
        TenantBinding tenantByTenant = this.getTenantByTenantid(tenantId);
        if (tenantByTenant == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_TENANTID);
        }
        // 获取租户分区列表
        //List<ClusterQuotaDto> tenantClusterQuotas = tenantClusterQuotaService.listClusterQuotaByTenantid(tenantId,null);
        List<Map<String, Object>> namespaceDataList = namespaceService.getNamespaceListByTenantid(tenantId);
        Map<String, Object> map = this.getTotalQuotaByNamespaceDataList(namespaceDataList);
        map.put(CommonConstant.TENANTNAME, tenantByTenant.getTenantName());
        map.put(CommonConstant.TENANTID, tenantByTenant.getTenantId());
        return map;
    }
    /**
     * 根据namespaces列表获取分区资源使用量
     * @param namespaceDataList
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> getTotalQuotaByNamespaceDataList(List<Map<String, Object>> namespaceDataList) throws Exception{
        Map<String, Object> map = new HashMap<String, Object>();
        //如果为空返回
        if (CollectionUtils.isEmpty(namespaceDataList)){
            return map;
        }
        double limitMen = 0;
        double useMen = 0;
        double limitCpu = 0;
        double useCpu = 0;
        for (Map<String, Object> map2 : namespaceDataList) {
            // 处理内存
            String usedtype = map2.get(CommonConstant.USEDTYPE).toString();
            List<String> memory = (List<String>) map2.get(CommonConstant.MEMORY);
            String str = null;
            switch (usedtype) {
                case CommonConstant.MB :
                    str= memory.size() == 2 ? memory.get(1) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    useMen = useMen + Double.parseDouble(str);
                    break;
                case CommonConstant.GB :
                    str= memory.size() == 2 ? memory.get(1) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    useMen = useMen + Double.parseDouble(str) * 1024;
                    break;
                case CommonConstant.TB :
                    str= memory.size() == 2 ? memory.get(1) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    useMen = useMen + Double.parseDouble(str) * 1024 * 1024;
                    break;
                case CommonConstant.PB :
                    str= memory.size() == 2 ? memory.get(1) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    useMen = useMen + Double.parseDouble(str) * 1024 * 1024 * 1024;
                    break;
            }
            String hardtype = map2.get(CommonConstant.HARDTYPE).toString();
            switch (hardtype) {
                case CommonConstant.MB :
                    str= memory.size() == 2 ? memory.get(0) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    limitMen = limitMen + Double.parseDouble(str);
                    break;
                case CommonConstant.GB :
                    str= memory.size() == 2 ? memory.get(0) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    limitMen = limitMen + Double.parseDouble(str) * 1024;
                    break;
                case CommonConstant.TB :
                    str= memory.size() == 2 ? memory.get(0) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    limitMen = limitMen + Double.parseDouble(str) * 1024 * 1024;
                    break;
                case CommonConstant.PB :
                    str= memory.size() == 2 ? memory.get(0) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    limitMen = limitMen + Double.parseDouble(str) * 1024 * 1024 * 1024;
                    break;
            }
            // 处理cpu
            List<String> cpu = (List<String>) map2.get(CommonConstant.CPU);
            limitCpu = limitCpu + Double.parseDouble(cpu.size() == 2 ? cpu.get(0) : CommonConstant.ZERONUM);
            useCpu = useCpu + Double.parseDouble(cpu.size() == 2 ? cpu.get(1) : CommonConstant.ZERONUM);
        }
        int hardnum = 1;
        int usednum = 1;
        while (limitMen >= 1024) {
            limitMen = limitMen / 1024;
            hardnum = hardnum + 1;
        }
        while (useMen >= 1024) {
            useMen = useMen / 1024;
            usednum = usednum + 1;
        }
        if (usednum == 1 && useMen == 0) {
            usednum = hardnum;
        }

        switch (hardnum) {
            case 1 :
                map.put(CommonConstant.HARDTYPE, CommonConstant.MB);
                break;
            case 2 :
                map.put(CommonConstant.HARDTYPE, CommonConstant.GB);
                break;
            case 3 :
                map.put(CommonConstant.HARDTYPE, CommonConstant.TB);
                break;
            case 4 :
                map.put(CommonConstant.HARDTYPE, CommonConstant.PB);
                break;
        }
        switch (usednum) {
            case 1 :
                map.put(CommonConstant.USEDTYPE, CommonConstant.MB);
                break;
            case 2 :
                map.put(CommonConstant.USEDTYPE, CommonConstant.GB);
                break;
            case 3 :
                map.put(CommonConstant.USEDTYPE, CommonConstant.TB);
                break;
            case 4 :
                map.put(CommonConstant.USEDTYPE, CommonConstant.PB);
                break;
        }
        // 保留两位小数 四舍五入
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.UP);
        nf.setGroupingUsed(false);
        List<Object> cpu = new LinkedList<>();
        List<Object> memory = new LinkedList<>();
        cpu.add(limitCpu % 1.0 == 0 ? (long) limitCpu : limitCpu);
        cpu.add(useCpu % 1.0 == 0 ? (long) useCpu : useCpu);
        memory.add(limitMen % 1.0 == 0 ? (long) limitMen : nf.format(limitMen));
        memory.add(useMen % 1.0 == 0 ? (long) useMen : nf.format(useMen));
        map.put(CommonConstant.MEMORY, memory);
        map.put(CommonConstant.CPU, cpu);
        return map;
    }
    @Override
    public List<String> listUserByTenantName(String tenantName) throws Exception {
        TenantBinding tenantBytenantName = this.getTenantBytenantName(tenantName);
        if (tenantBytenantName == null){
            throw new MarsRuntimeException(ErrorCodeMessage.NOT_FOUND, DictEnum.TENANT.phrase(),true);
        }
        List<String> lists = userRoleRelationshipService.listUserByTenantId(tenantBytenantName.getTenantId());
        return lists;
    }

    @Override
    public List<TenantBinding> listTenantsByUserName(String userName) throws Exception {
        List<TenantBinding> tenantList = null;
        //判断用户是否为系统管理员
        boolean isAdmin = userService.isAdmin(userName);
        if (isAdmin){
            //如果是admin则查询所有的租户
            tenantList = this.listAllTenant();
        }else{
            tenantList = userRoleRelationshipService.listTenantByUsername(userName);
        }
        // Map<String,Object> tenantlist = new HashMap<>();
        return tenantList;
    }

    /**
     * 根据租户id获取租户成员
     *
     * @param tenantId
     * @return
     * @throws Exception
     */
    @Override
    public List<User> listTenantMember(String tenantId) throws Exception {
        //获取租户检查有效性
        TenantBinding tenant = this.getTenantByTenantid(tenantId);
        if (Objects.isNull(tenant)){
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_TENANTID);
        }
        List<User> users = userService.searchUsersGroupname(tenant.getTenantName());
        return users;
    }

    @Override
    public ActionReturnUtil listTenantsByUserNameForAudit(String userName, boolean isAdmin) throws Exception {
        List tenants = new ArrayList();
        if (isAdmin){
            //如果是admin则查询所有的租户
            tenants = this.listAllTenant();
        }else{
            tenants = userRoleRelationshipService.listTenantByUsername(userName);
        }

        if (tenants != null && tenants.size() > 0) {
            TenantBinding tb = new TenantBinding();
            tb.setTenantName("全部");
            tenants.add(0, tb);
        }

        return ActionReturnUtil.returnSuccessWithData(tenants);
    }
    /**
     * 获取所有租户列表
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<TenantBinding> listAllTenant() throws Exception {
        TenantBindingExample example = this.getExample();
        List<TenantBinding> tenantList = tenantBindingMapper.selectByExample(example);
        return tenantList;
    }

    /**
     * 给新集群的添加租户配额
     * @param clusterId
     * @return
     * @throws Exception
     */
    @Override
    public void dealQuotaWithNewCluster(String clusterId) throws Exception {
        Cluster cluster = this.clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)){
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        Date date = DateUtil.getCurrentUtcTime();
        //获取所有租户列表
        List<TenantBinding> tenantBindings = this.listAllTenant();
        //对每个租户添加集群默认配额
        for (TenantBinding tenant:tenantBindings) {
            String tenantId = tenant.getTenantId();
            TenantClusterQuota quota = tenantClusterQuotaService.getClusterQuotaByTenantIdAndClusterId(tenantId, clusterId);
            if (Objects.isNull(quota)){
                //组装数据
                TenantClusterQuota tenantClusterQuota = new TenantClusterQuota();
                tenantClusterQuota.setCreateTime(date);
                tenantClusterQuota.setTenantId(tenantId);
                tenantClusterQuota.setCpuQuota(0d);
                tenantClusterQuota.setMemoryQuota(0d);
                tenantClusterQuota.setClusterId(clusterId);
                tenantClusterQuota.setClusterName(cluster.getName());
                tenantClusterQuotaService.createClusterQuota(tenantClusterQuota);
            }
        }
    }

    /**
     * @param tenantDto
     * @return
     * @throws Exception
     */
    @Override
    public void importCdsSystem(TenantDto tenantDto) throws Exception {

        String sysName = tenantDto.getSysName();
        String sysId = tenantDto.getSysId();
        String sysCode = tenantDto.getSysCode();
        String remark = tenantDto.getRemark();
        String shortFlag = tenantDto.getShortFlag();
        String name = shortFlag.trim().toLowerCase();
        //根据分类创建租户或者项目
        if (CATEGORY_TENANT.equals(tenantDto.getCategory())){
            //组装参数
            tenantDto.setTenantId(sysId);
            tenantDto.setSysCode(sysCode);
            tenantDto.setTenantName(name);
            tenantDto.setAliasName(sysName);
            tenantDto.setAnnotation(remark);
            TenantBinding tenant = this.getTenantByTenantid(sysId);
            if (Objects.isNull(tenant)){
                //创建租户
                this.createTenant(tenantDto);
            }else {
                //更新租户
                this.updateTenant(tenantDto);
            }

        } else if (CATEGORY_PROJECT.equals(tenantDto.getCategory())){
            //组装参数
            ProjectDto projectDto = new ProjectDto();
            projectDto.setProjectId(sysId);
            projectDto.setProjectName(name);
            projectDto.setAliasName(sysName);
            projectDto.setTenantId(tenantDto.getParentId());
            projectDto.setProjectSystemCode(tenantDto.getSysCode());
            Project project = this.projectService.getProjectByProjectId(sysId);
            if (Objects.isNull(project)){
                //创建项目
                this.projectService.createProject(projectDto);
            }else {
                //更新项目
                this.projectService.updateProject(projectDto);
            }

        }
    }

    /**
     * 导入CDP用户
     *
     * @param cdpUserDto
     * @return
     * @throws Exception
     */
    @Override
    public void importCdsUserAccount(CDPUserDto cdpUserDto) throws Exception {
        String userAccount = cdpUserDto.getUserAccount();
        String userName = cdpUserDto.getUserName();
        String email = cdpUserDto.getEmail();
        String tel = cdpUserDto.getTel();
        User user = this.userService.getUser(userAccount);
        if (Objects.isNull(user)){
            //创建用户
            user = new User();
            user.setUsername(userAccount);
            user.setRealName(userName);
            user.setEmail(email);
            user.setPhone(tel);
            user.setPause(CommonConstant.NORMAL);
            user.setIsAdmin(0);
            user.setCreateTime(DateUtil.getCurrentUtcTime());
            this.userService.insertUser(user);
        } else {
            //更新用户
            if (StringUtils.isNotBlank(userName)){
                user.setRealName(userName);
            }
            if (StringUtils.isNotBlank(email)){
                user.setEmail(email);
            }
            if (StringUtils.isNotBlank(tel)){
                user.setPhone(tel);
            }
            if (Objects.isNull(user.getCreateTime())){
                user.setCreateTime(DateUtil.getCurrentUtcTime());
            }
            user.setUpdateTime(DateUtil.getCurrentUtcTime());
            this.userService.updateUser(user);
        }
    }

    /**
     * 导入CDP项目用户关系
     *
     * @param cdpUserDto
     * @return
     * @throws Exception
     */
    @Override
    public void importCdsUserRelationship(CDPUserDto cdpUserDto) throws Exception {
        String tenantId = null;
        String sysId = cdpUserDto.getSysId();
        String userAccount = cdpUserDto.getUserAccount();
        //只接收项目经理
        if (PROJECTMGR.equals(cdpUserDto.getRoleCode())){
            List<String> tmList = new ArrayList<>();
            tmList.add(userAccount);
            //根据分类导入租户或者项目管理员
            if (CATEGORY_TENANT.equals(cdpUserDto.getCategory())){
                this.createTm(sysId,tmList);
            } else if (CATEGORY_PROJECT.equals(cdpUserDto.getCategory())){
                String parentId = cdpUserDto.getParentId();
                this.projectService.createPm(parentId,sysId,tmList);
            }
        }
        //根据分类导入租户成员
        if (CATEGORY_TENANT.equals(cdpUserDto.getCategory())){
            tenantId = cdpUserDto.getSysId();
        } else if (CATEGORY_PROJECT.equals(cdpUserDto.getCategory())){
            tenantId = cdpUserDto.getParentId();
        }
        List <String> addusers = new ArrayList<>();
        addusers.add(userAccount);
        this.updateTenantMember(tenantId,addusers,null);
    }
    public void updateTenantMember(String tenantId, List <String> addUsers, List <String> deleteUsers) throws Exception {
        //查询租户
        TenantBinding tenantBinding = this.getTenantByTenantid(tenantId);
        //查询用户组
        UserGroup group = userService.getGroupByGroupName(tenantBinding.getTenantName());
        UserGroupDto usergroupdto = new UserGroupDto();
        usergroupdto.setUpdategroupname(group.getGroupname());
        if (!CollectionUtils.isEmpty(addUsers)){
            //添加组用户
            usergroupdto.setAddusers(addUsers);
        }
        if (!CollectionUtils.isEmpty(deleteUsers)){
            //删除组用户
            usergroupdto.setDelusers(deleteUsers);
        }
        usergroupdto.setUsergroup(group);
        this.userService.updateGroup(usergroupdto);
    }
    /**
     * 删除CDP项目用户关系
     *
     * @param cdpUserDto
     * @return
     * @throws Exception
     */
    @Override
    public void removeCdsUserRelationship(CDPUserDto cdpUserDto) throws Exception {
        //只接收项目经理
        if (PROJECTMGR.equals(cdpUserDto.getRoleCode())){
            String sysId = cdpUserDto.getSysId();
            String userAccount = cdpUserDto.getUserAccount();
            //根据分类删除租户或者项目管理员
            if (CATEGORY_TENANT.equals(cdpUserDto.getCategory())){
                this.deleteTm(sysId,userAccount);
            } else if (CATEGORY_PROJECT.equals(cdpUserDto.getCategory())){
                String parentId = cdpUserDto.getParentId();
                this.projectService.deletePm(parentId,sysId,userAccount,Boolean.TRUE);
            }
        }
    }

    @Override
    public List<TenantBinding> testTime(Integer domain) throws Exception {
        Date date=new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -domain);
        Date leftDate = calendar.getTime();
        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andCreateTimeBetween(leftDate, date).andCreateTimeIsNotNull();
        List<TenantBinding> listTenantBinding = tenantBindingMapper.selectByExample(example);
        return listTenantBinding;
    }
    private TenantBindingExample getExample(){
        return new TenantBindingExample();
    }
}
