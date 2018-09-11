package com.harmonycloud.service.tenant.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.HarborMemberEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.dao.tenant.ProjectMapper;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.tenant.bean.ProjectExample;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.user.bean.*;
import com.harmonycloud.dto.tenant.DevOpsProjectUserDto;
import com.harmonycloud.dto.tenant.ProjectDto;
import com.harmonycloud.dto.user.UserRoleDto;
import com.harmonycloud.k8s.bean.BaseResource;
import com.harmonycloud.service.application.ApplicationDeployService;
import com.harmonycloud.service.application.ApplicationService;
import com.harmonycloud.service.application.PersistentVolumeService;
import com.harmonycloud.service.cache.ClusterCacheManager;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupService;
import com.harmonycloud.service.platform.bean.RepositoryInfo;
import com.harmonycloud.service.platform.service.ConfigCenterService;
import com.harmonycloud.service.platform.service.ExternalService;
import com.harmonycloud.service.platform.service.ci.BuildEnvironmentService;
import com.harmonycloud.service.platform.service.ci.DependenceService;
import com.harmonycloud.service.platform.service.ci.DockerFileService;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.LocalRoleService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserRoleRelationshipService;
import com.harmonycloud.service.user.UserService;
import com.harmonycloud.service.platform.service.harbor.HarborUserService;
import com.harmonycloud.service.user.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zgl on 17-12-14.
 */

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    ProjectMapper projectMapper;
    @Autowired
    UserRoleRelationshipService userRoleRelationshipService;
    @Autowired
    TenantService tenantService;
    @Autowired
    RoleLocalService roleLocalService;
    @Autowired
    ClusterService clusterService;
    @Autowired
    HarborService harborService;
    @Autowired
    HarborProjectService harborProjectService;
    @Autowired
    UserService userService;
    @Autowired
    HttpSession session;
    @Autowired
    ExternalService externalService;
    @Autowired
    PersistentVolumeService persistentVolumeService;
    @Autowired
    ConfigCenterService configCenterService;
    @Autowired
    ApplicationDeployService applicationDeployService;
    @Autowired
    ApplicationService applicationService;
    @Autowired
    LocalRoleService localRoleService;
    @Autowired
    ClusterCacheManager clusterCacheManager;
    @Autowired
    JobService jobService;
    @Autowired
    DockerFileService dockerFileService;
    @Autowired
    DependenceService dependenceService;
    @Autowired
    BuildEnvironmentService buildEnvironmentService;
    @Autowired
    HarborUserService harborUserService;
    @Autowired
    RolePrivilegeService rolePrivilegeService;
    @Autowired
    DataPrivilegeGroupMemberService dataPrivilegeGroupMemberService;
    @Autowired
    DataPrivilegeGroupService dataPrivilegeGroupService;

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    /**
     * 切换项目
     *
     * @param tenantId
     * @param projectId
     * @return
     * @throws Exception
     */
    @Override
    public List<Role> switchProject(String tenantId, String projectId) throws Exception {
        //项目有效值判断
        Project project = this.getProjectByProjectId(projectId);
        if (Objects.isNull(project) || !tenantId.equals(project.getTenantId())){
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
        }
        //获取用户名
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        session.setAttribute(CommonConstant.PROJECTID, projectId);
        session.setAttribute(CommonConstant.PROJECT_ALIASNAME, project.getAliasName());
        List<Role> roleList = this.roleLocalService.getRoleListByUsernameAndTenantIdAndProjectId(userName,tenantId, projectId);
        //设置是否能获取菜单
        if (CollectionUtils.isEmpty(roleList)){
            session.setAttribute(CommonConstant.GETMENU, Boolean.FALSE);
        }else {
            session.setAttribute(CommonConstant.GETMENU, Boolean.TRUE);
            //设置默认角色id为角色列表的第一个角色id
            session.setAttribute(CommonConstant.ROLEID, roleList.get(0).getId());
        }
        List<LocalRolePrivilege>  localRolePrivileges = localRoleService.listPrivilegeByProject(projectId, userName);
        session.setAttribute(CommonConstant.SESSION_DATA_PRIVILEGE_LIST, localRolePrivileges);
        if (CollectionUtils.isEmpty(roleList)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_DISABLE);
        }
        return roleList;
    }
    /**
     * 创建项目
     *
     * @param projectDto
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createProject(ProjectDto projectDto) throws Exception {
        String tenantId = projectDto.getTenantId();
        TenantBinding tenant = this.tenantService.getTenantByTenantid(tenantId);
        //有效值判断
        if (Objects.isNull(tenant)){
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_TENANTID);
        }
        String projectName = projectDto.getProjectName();
        Project projectByProjectName = this.getProjectByProjectName(projectName.trim());
        //判断项目是否存在
        if (!Objects.isNull(projectByProjectName)){
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECTNAME_EXIST,projectName.trim(),Boolean.FALSE);
        }
        String aliasName = projectDto.getAliasName();
        Project projectByAliasName = this.getProjectByAliasName(aliasName.trim());
        if (!Objects.isNull(projectByAliasName)){
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECTALIASNAME_EXIST,aliasName.trim(),Boolean.FALSE);
        }
        String projectId = projectDto.getProjectId();
        //如果项目id不存在则生成
        if (StringUtils.isBlank(projectId)){
            projectId = StringUtil.getId();
        }
        Project project = new Project();
        //设置项目名称
        project.setProjectName(projectName.trim());
        //设置项目id
        project.setProjectId(projectId);
        //设置租户id
        project.setTenantId(tenantId);
        //设置项目创建时间
        Date date = DateUtil.getCurrentUtcTime();
        project.setCreateTime(date);
        //设置项目备注
        project.setAnnotation(projectDto.getAnnotation());
        //设置项目名称
        project.setAliasName(projectDto.getAliasName());
        String projectSystemCode = projectDto.getProjectSystemCode();
        //设置项目同步devops平台盈科同步的数据
        project.setProjectSystemCode(projectSystemCode);
        project.setCreateUserAccount(projectDto.getCreateUserAccount());
        project.setCreateUserId(projectDto.getCreateUserId());
        project.setCreateUserName(project.getCreateUserName());
        project.setUpdateUserAccount(projectDto.getUpdateUserAccount());
        project.setUpdateUserId(projectDto.getUpdateUserId());
        project.setUpdateUserName(projectDto.getUpdateUserName());
        //创建项目
        this.projectMapper.insertSelective(project);
        //创建镜像仓库
        RepositoryInfo repositoryInfo = new RepositoryInfo();
        repositoryInfo.setProjectId(project.getProjectId());
        repositoryInfo.setProjectName(project.getProjectName());
        repositoryInfo.setTenantId(project.getTenantId());
        repositoryInfo.setIsDefault(Boolean.TRUE);
        repositoryInfo.setIsPublic(Boolean.FALSE);
        harborProjectService.createRepository(repositoryInfo);
        //如果有项目管理员则创建
        List pmList = projectDto.getPmList();
        if (!CollectionUtils.isEmpty(pmList)){
            this.createPm(tenantId,projectId,pmList);
        }

        // deal harbor user privilege
        try {
            String tmUsernames = tenant.getTmUsernames();
            if (StringUtils.isNotBlank(tmUsernames)){
                String[] split = tmUsernames.split(CommonConstant.COMMA);
                if (split.length > 0){
                    for (String tm : split) {
                        this.roleLocalService.addHarborUserRole(HarborMemberEnum.PROJECTADMIN,projectId,tm,CommonConstant.TM_ROLEID);
                    }
                }
            }

        }catch (Exception e){
            logger.error("sync harbor member failed",e);
        }
    }

    /**
     * 根据项目id删除项目
     *
     * @param projectId
     * @throws Exception
     */
    @Override
    public void deleteProjectByProjectId(String tenantId,String projectId) throws Exception {
        //有效性检查
        Project projectByProjectId = this.getProjectByProjectId(projectId);
        if (Objects.isNull(projectByProjectId) || !tenantId.equals(projectByProjectId.getTenantId())){
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
        }
        //删除镜像仓库
        harborProjectService.deleteRepository(projectId);
        //删除用户
        this.userRoleRelationshipService.deleteUserRoleRelationshipByProjectId(projectId);
        //删除外部服务
        externalService.deleteExtServiceByProject(projectId);
        //删除存储
        persistentVolumeService.deletePv(projectId);
        //删除配置文件
        configCenterService.deleteConfigByProject(projectId);
        //删除应用
        this.applicationDeployService.deleteProjectAppResource(projectId);
        //删除模板
        this.applicationService.deleteTemplatesInProject(projectId);
        //删除流水线
        this.jobService.deletePipelineByProject(projectId);
        //删除Dockerfile
        this.dockerFileService.deleteDockerfileByProject(projectId);
        //删除dependence
        this.dependenceService.deleteDependenceByProject(projectId);
        //删除环境
        this.buildEnvironmentService.deleteBuildEnvironmentByProject(projectId);
        //删除项目
        this.deleteProjectById(projectByProjectId.getId());
    }

    /**
     * 根据主键id删除项目
     *
     * @param id
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProjectById(Integer id) throws Exception {
        this.projectMapper.deleteByPrimaryKey(id);
    }

    /**
     * 更新项目
     *
     * @param projectDto
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProject(ProjectDto projectDto) throws Exception {
        Project projectByProjectId = this.getProjectByProjectId(projectDto.getProjectId());
        //有效值判断
        if (Objects.isNull(projectByProjectId) || !projectDto.getTenantId().equals(projectByProjectId.getTenantId())){
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
        }
        String aliasName = projectDto.getAliasName();

        //如果项目名不为空则更新项目名
        if (!Objects.isNull(aliasName)){
            Project projectByAliasName = this.getProjectByAliasName(aliasName);
            if (!Objects.isNull(projectByAliasName)){
                throw new MarsRuntimeException(ErrorCodeMessage.PROJECTALIASNAME_EXIST,aliasName,Boolean.TRUE);
            }
            projectByProjectId.setAliasName(aliasName);
        }
        //如果备注不为空则更新备注
        if (!Objects.isNull(projectDto.getAnnotation())){
            projectByProjectId.setAnnotation(projectDto.getAnnotation());
        }
        //设置更新时间
        projectByProjectId.setUpdateTime(DateUtil.getCurrentUtcTime());
        this.projectMapper.updateByPrimaryKeySelective(projectByProjectId);
    }

    /**
     * 根据租户id查询租户下所有项目列表
     *
     * @param tenantId
     * @return
     * @throws Exception
     */
    @Override
    public List<ProjectDto> listTenantProjectByTenantid(String tenantId) throws Exception {
        List<ProjectDto> projects = this.listTenantProjectByTenantid(tenantId, null, null);
        return projects;
    }

    /**
     * 根据租户id查询租户下所有项目列表 不带分页(内部接口调用)
     *
     * @param tenantId
     * @return
     * @throws Exception
     */
    @Override
    public List<Project> listTenantProjectByTenantidInner(String tenantId) throws Exception {
        List<Project> projectList = this.listTenantProjectByTenantidInner(tenantId, null, null);
        return projectList;
    }

    /**
     * 根据租户id查询租户下所有项目列表 带分页
     *
     * @param tenantId
     * @param limit
     * @param offset
     * @return
     * @throws Exception
     */
    @Override
    public List<ProjectDto> listTenantProjectByTenantid(String tenantId, Integer limit, Integer offset) throws Exception {
        List<Project> projectList = this.listTenantProjectByTenantidInner(tenantId, limit, offset);
        List<ProjectDto> projectDtos = generateProjectDto(projectList, Boolean.TRUE);
        return projectDtos;
    }
    /**
     * 根据租户id与用户名查询租户下所有项目列表(返回值带镜像仓库，应用数量)
     * @param tenantId
     * @param username
     * @return
     * @throws Exception
     */
    @Override
    public List<ProjectDto> listTenantProjectByUsernameDetail(String tenantId ,String username) throws Exception {
        List<Project> projectList = this.listTenantProjectByUsername(tenantId, username);
        List<ProjectDto> projectDtos = generateProjectDto(projectList, Boolean.TRUE);
        return projectDtos;
    }
    private List<Project> listTenantProjectByTenantidInner(String tenantId, Integer limit, Integer offset) throws Exception {
        ProjectExample example = this.getExample();
        //分页查询
        if (null != limit){
            example.setLimit(limit);
        }
        if (null != offset){
            example.setOffset(offset);
        }
        example.createCriteria().andTenantIdEqualTo(tenantId);
        List<Project> projectList = this.projectMapper.selectByExample(example);
        return projectList;
    }
    /**
     * 获取所有项目列表
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<Project> listAllProject() throws Exception {
        ProjectExample example = this.getExample();
        List<Project> projects = this.projectMapper.selectByExample(example);
        return projects;
    }
    /**
     * 根据用户名与租户id获取租户下的项目列表
     * @param username,tenantId
     * @return
     * @throws Exception
     */
    @Override
    public List<Project> listTenantProjectByUsername(String tenantId, String username) throws Exception {
        List<Project> projects = this.userRoleRelationshipService.listProjectByTenantIdAndUsername(tenantId, username);
        return projects;
    }

    /**
     * 根据项目id查询项目
     *
     * @param projectId@return
     */
    @Override
    public Project getProjectByProjectId(String projectId) throws Exception {
        ProjectExample example = this.getExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<Project> projects = this.projectMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(projects)){
            return  projects.get(0);
        }
        return null;
    }

    /**
     * 根据项目id查询项目详情
     *
     * @param projectId
     * @return
     */
    @Override
    public ProjectDto getProjectDetailByProjectId(String tenantId,String projectId) throws Exception {
        // 有效值判断
        Project projectByProjectId = this.getProjectByProjectId(projectId);
        if (Objects.isNull(projectByProjectId) || !tenantId.equals(projectByProjectId.getTenantId())) {
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_PROJECTID);
        }
        List<Project> projectList = new ArrayList<>();
        projectList.add(projectByProjectId);
        //转换为页面显示数据
        ProjectDto projectDto = this.generateProjectDto(projectList,Boolean.FALSE).get(0);
        return projectDto;
    }
    //组装projectDto返回给页面 isList为 false 则查询详情，isList为 true 则查询列表返回值
    private List<ProjectDto> generateProjectDto(List<Project> projectList,Boolean isList) throws Exception{
        List<ProjectDto> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(projectList)){
            return resultList;
        }
//        CountDownLatch countDownLatchApp = new CountDownLatch(projectList.size());
        for (Project project:projectList) {
            ProjectDto projectDto = new ProjectDto();
            //设置备注
            projectDto.setAnnotation(project.getAnnotation());
            //设置创建时间
            projectDto.setCreateTime(project.getCreateTime());
            //设置更新时间
            projectDto.setUpdateTime(project.getUpdateTime());
            List<ImageRepository> imageRepositories = null;
            projectDto.setId(project.getId());
            //设置租户id
            projectDto.setTenantId(project.getTenantId());
            //设置项目id
            projectDto.setProjectId(project.getProjectId());
            //设置项目名称
            projectDto.setProjectName(project.getProjectName());
            projectDto.setAliasName(project.getAliasName());
            String pmUsernames = project.getPmUsernames();
            if (StringUtils.isNotBlank(pmUsernames)){
                String[] split = pmUsernames.split(CommonConstant.COMMA);
                List<String> pmList = Arrays.stream(split).collect(Collectors.toList());
                projectDto.setPmList(pmList);
            }
            List<UserRoleRelationship> userRoleRelationships = userRoleRelationshipService.listUserByProjectId(project.getProjectId());
            if (isList){
                //设置项目用户数量
                if (!CollectionUtils.isEmpty(userRoleRelationships)){
                    Set<String> users = userRoleRelationships.stream().map(UserRoleRelationship::getUsername).collect(Collectors.toSet());
                    projectDto.setUserNum(users.size());
                }
                //设置项目应用数量
                List<BaseResource> baseResources = null;
                try {
                    baseResources = applicationDeployService.listApplicationByProject(project.getProjectId());
                }catch (Exception e){
                    logger.info("获取当前租户应用失败："+e.getMessage() , e);
                }

                if (CollectionUtils.isEmpty(baseResources)){
                    projectDto.setAppNum(0);
                }else {
                    projectDto.setAppNum(baseResources.size());
                }
            }else {
                List<LocalRole> localRoles = localRoleService.listLocalRoleByRoleName(project.getProjectId(), null);
                projectDto.setLocalRoleList(localRoles);
                List<Map<String,Object>> list = new ArrayList();
                //组装用户数据
                if (!CollectionUtils.isEmpty(userRoleRelationships)){
                    for (UserRoleRelationship userRoleRelationship:userRoleRelationships) {
                        String userName = userRoleRelationship.getUsername();
                        Integer roleId = userRoleRelationship.getRoleId();
                        //获取用户角色
                        Role role = roleLocalService.getRoleById(roleId);
                        User user = userService.getUser(userName);
                        Map<String,Object> result = new HashMap<>();
                        result.put(CommonConstant.USERNAME,userName);
                        result.put(CommonConstant.ID,userRoleRelationship.getId());
                        result.put(CommonConstant.ROLE,role);
                        result.put(CommonConstant.NICKNAME,Objects.isNull(user.getRealName())?CommonConstant.EMPTYSTRING:user.getRealName());
                        list.add(result);
                    }
                    projectDto.setUserDataList(list);
                }
                try {
                    imageRepositories = harborProjectService
                            .listRepositoryDetails(project.getProjectId(),null,Boolean.FALSE, null);
                }catch (Exception e){
                    logger.error("查询project repository 详情失败，", e);
//                throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_LIST_ERROR);
                }
                projectDto.setHarborRepositoryList(imageRepositories);
            }
            resultList.add(projectDto);

        }
        return resultList;
    }

    /**
     * 根据项目名查询项目
     *
     * @param projectName
     * @return
     * @throws Exception
     */
    @Override
    public Project getProjectByProjectName(String projectName) throws Exception {
        ProjectExample example = this.getExample();
        example.createCriteria().andProjectNameEqualTo(projectName);
        List<Project> projects = this.projectMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(projects)){
            return  projects.get(0);
        }
        return null;
    }
    public Project getProjectByAliasName(String aliasName) throws Exception {
        ProjectExample example = this.getExample();
        example.createCriteria().andAliasNameEqualTo(aliasName);
        List<Project> projects = this.projectMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(projects)){
            return  projects.get(0);
        }
        return null;
    }

    /**
     * 根据主键id查询项目
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public Project getProjectById(Integer id) throws Exception {
        Project project = this.projectMapper.selectByPrimaryKey(id);
        return project;
    }

    @Override
    public List<UserRoleRelationship> listProjectPm(String tenantId,String projectId) throws Exception {
        List<UserRoleRelationship> userRoleRelationships = this.userRoleRelationshipService.
                listPmByTenantAndProjectId(tenantId,projectId);
        return userRoleRelationships;
    }

    /**
     * 根据项目id查询项目管理员
     *
     * @param tenantId
     * @param projectId
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String,Object>> listProjectUser(String tenantId, String projectId) throws Exception {
        Project project = this.getProjectByProjectId(projectId);
        if (!tenantId.equals(project.getTenantId())){
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
        }
        List<Map<String,Object>> list = new ArrayList();
        List<UserRoleRelationship> userRoleRelationships = this.userRoleRelationshipService.listUserByProjectId(projectId);
        //组装用户数据
        if (!CollectionUtils.isEmpty(userRoleRelationships)){
            for (UserRoleRelationship userRoleRelationship:userRoleRelationships) {
                //获取用户角色
                Role role = roleLocalService.getRoleById(userRoleRelationship.getRoleId());
                User user = userService.getUser(userRoleRelationship.getUsername());
                if(user == null){
                    continue;
                }
                Map<String,Object> result = new HashMap<>();
                result.put(CommonConstant.USERNAME,userRoleRelationship.getUsername());
                result.put(CommonConstant.ID,userRoleRelationship.getId());
                result.put(CommonConstant.ROLE,role);
                result.put(CommonConstant.EMAIL,Objects.isNull(user.getEmail())?CommonConstant.EMPTYSTRING:user.getEmail());
                result.put(CommonConstant.MOBILEPHONE,Objects.isNull(user.getPhone())?CommonConstant.EMPTYSTRING:user.getPhone());
                result.put(CommonConstant.NICKNAME,Objects.isNull(user.getRealName())?CommonConstant.EMPTYSTRING:user.getRealName());
                list.add(result);
            }
        }
        return list;
    }

    /**
     * devops平台向内添加用户
     *
     * @param devOpsProjectUserDto
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDevOpsUser(DevOpsProjectUserDto devOpsProjectUserDto) throws Exception {
        //组装用户数据
        User user = new User();
        user.setCreate_time(DateUtil.getCurrentUtcTime());
        user.setPhone(devOpsProjectUserDto.getTel());
        user.setEmail(devOpsProjectUserDto.getEmail());
        user.setComment(devOpsProjectUserDto.getRemark());
        user.setPassword(CommonConstant.INITPASSWORD);
        user.setUsername(devOpsProjectUserDto.getUserAccount());
        user.setRealName(devOpsProjectUserDto.getUserName());
        //添加用户
        userService.addUser(user);
    }

    @Override
    public Boolean isPm(String tenantId,String projectId, String username) throws Exception {
        //获取租户管理员的列表
        List<UserRoleRelationship> pmList = this.listProjectPm(tenantId,projectId);
        //如果项目管理员列表为空则返回false
        if (CollectionUtils.isEmpty(pmList)){
            return false;
        }
        //查询当前用户是否在项目管理员列表内
        for (UserRoleRelationship pm:pmList) {
            if (username.equals(pm.getUsername())){
                return true;
            }
        }
        return false;
    }

    /**
     * 向项目下添加项目管理员
     *
     * @param projectId
     * @param pmList
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPm(String tenantId,String projectId, List<String> pmList) throws Exception {
        //检查租户的有效性
        TenantBinding tenantBinding = this.tenantService.getTenantByTenantid(tenantId);
        if (Objects.isNull(tenantBinding)){
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_TENANTID);
        }
        //检查项目的有效性
        Project project = this.getProjectByProjectId(projectId);
        if (Objects.isNull(project) || !tenantId.equals(project.getTenantId())){
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_PROJECTID);
        }
        //获取当前项目的项目管理员
        List<UserRoleRelationship> userRoleRelationships = this.listProjectPm(tenantId,projectId);
        Map<String,String> pmMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(userRoleRelationships)){
            userRoleRelationships.stream().forEach(pm -> {pmMap.put(pm.getUsername(),pm.getUsername());});
        }

        dataPrivilegeGroupMemberService.addNewProjectMemberToGroup(project, pmList);

        //处理添加多个项目管理员
        for (String userName:pmList) {
            User user1 = this.userService.getUser(userName);
            if (Objects.isNull(user1)){
                throw new MarsRuntimeException(ErrorCodeMessage.USER_NOT_EXIST);
            }
            //如果有空格处理空格
            userName = userName.trim();
            //如果已经是项目管理员则提示返回
            if (!Objects.isNull(pmMap) &&  StringUtils.isNotBlank(pmMap.get(userName))){
                throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_TM_EXIST,userName,true);
            }
            //创建项目管理员
            Role role = roleLocalService.getRoleByRoleName(CommonConstant.PM);
            Integer roleId = role.getId();
            this.createProjectRole(tenantId,projectId,userName,roleId);
            //如果成员不存在则添加
            UserGroup group = this.userService.getGroupByGroupName(tenantBinding.getTenantName());
            UserGroupRelation userGroupRelation = this.userService.getGroup(user1.getId(), group.getId());
            if (Objects.isNull(userGroupRelation)){
                List<String> addUsers = new ArrayList<>();
                addUsers.add(userName);
                this.tenantService.updateTenantMember(tenantId,addUsers,null);
            }
            Boolean status = clusterCacheManager.getRolePrivilegeStatusForTenantOrProject(roleId,userName,null, projectId);
            if (status){
                clusterCacheManager.updateRolePrivilegeStatusForTenantOrProject(roleId,userName,null,projectId,Boolean.FALSE);
            }
            //处理harbor的角色权限关系
            this.roleLocalService.addHarborUserRole(HarborMemberEnum.PROJECTADMIN,projectId,userName,roleId);
        }
        //更新PM关系至项目表
        String pmUsernames = project.getPmUsernames();
        Date date = DateUtil.getCurrentUtcTime();
        if (StringUtils.isNotBlank(pmUsernames)){
            project.setPmUsernames(pmUsernames + CommonConstant.COMMA + StringUtils.join(pmList,CommonConstant.COMMA));
        }else{
            project.setPmUsernames(StringUtils.join(pmList,CommonConstant.COMMA));
        }
        project.setUpdateTime(date);
        this.projectMapper.updateByPrimaryKeySelective(project);
    }
    //创建用户角色
    private void createProjectRole(String tenantId,String projectId,String username,Integer roleId) throws Exception{
        //检查用户是否在系统中
        User user = userService.getUser(username);
        if (Objects.isNull(user)){
            throw new MarsRuntimeException(ErrorCodeMessage.USER_NOT_EXIST,username,true);
        }
        Role role = this.roleLocalService.getRoleById(roleId);
        if (Objects.isNull(role)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        //检查添加的用户角色是否存在
        UserRoleRelationship relationshipUser = userRoleRelationshipService.getUser(projectId, username, roleId);
        if (!Objects.isNull(relationshipUser)){
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_ROLE_EXIST,username + CommonConstant.COLON + role.getNickName(),true);
        }
        //添加用户至用户角色表
        UserRoleRelationship userRoleRelationship = new UserRoleRelationship();
        Date date = DateUtil.getCurrentUtcTime();
        userRoleRelationship.setCreateTime(date);
        userRoleRelationship.setRoleId(roleId);
        userRoleRelationship.setProjectId(projectId);
        userRoleRelationship.setUsername(username);
        userRoleRelationship.setTenantId(tenantId);
        userRoleRelationshipService.createUserRoleRelationship(userRoleRelationship);
    }
    private void deleteProjectRole(String projectId, String username,Integer roleId) throws Exception{
        //查询项目管理员
        UserRoleRelationship pmUserRoleRelationship = userRoleRelationshipService.getUser(projectId,username,roleId);
        if (Objects.isNull(pmUserRoleRelationship)){
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_ROLE_NOT_EXIST);
        }
        //删除项目角色
        this.userRoleRelationshipService.deleteUserRoleRelationshipById(pmUserRoleRelationship.getId());
        //删除所有数据权限组中该用户
        List<UserRoleRelationship> list = userRoleRelationshipService.getUserRoleRelationshipByUsernameAndProjectId(username, projectId);
        if(CollectionUtils.isEmpty(list)){
            dataPrivilegeGroupMemberService.deleteProjectMemberFromGroup(projectId, username);
        }
    }
    /**
     * 向项目下删除项目管理员
     *
     * @param projectId
     * @param username
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePm(String tenantId,String projectId, String username,Boolean isCDPOperate) throws Exception {
        //检查项目的有效性
        Project project = this.getProjectByProjectId(projectId);
        if (Objects.isNull(project) || !tenantId.equals(project.getTenantId())){
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_PROJECTID);
        }
        //如果为云平台操作，项目管理员不能删除自己
        if (!isCDPOperate && this.userService.getCurrentUsername().equals(username) &&
                this.userService.getCurrentRoleId() >= CommonConstant.PM_ROLEID){
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_PM_CANNOT_DELETE);
        }
        Role role = roleLocalService.getRoleByRoleName(CommonConstant.PM);
        Integer roleId = role.getId();
        this.deleteProjectRole(projectId,username,roleId);
        //如果有空格处理空格
        final String newUsername = username.trim();
        //更新PM关系至项目表
        String pmUsernames = project.getPmUsernames();
        if (StringUtils.isNotBlank(pmUsernames)){
            String[] pmUserList = pmUsernames.split(CommonConstant.COMMA);
            List<String> usersList = Arrays.stream(pmUserList).filter(pm -> !newUsername.equals(pm)).collect(Collectors.toList());
            String users = StringUtils.join(usersList, CommonConstant.COMMA);
            project.setPmUsernames(users);
            Date date = DateUtil.getCurrentUtcTime();
            project.setUpdateTime(date);
            //更新到项目表
            this.projectMapper.updateByPrimaryKeySelective(project);
        }
        //更新redis中用户的状态
        clusterCacheManager.updateRolePrivilegeStatusForTenantOrProject(roleId,username,null,projectId,Boolean.TRUE);
        //处理harbor的角色权限关系
        this.roleLocalService.updateHarborUserRole(HarborMemberEnum.NONE,projectId,username);
    }
    private ProjectExample getExample(){
        ProjectExample example = new ProjectExample();
        return example;
    }

    /**
     * 添加项目中用户角色
     *
     * @param userRoleDto
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserRole(UserRoleDto userRoleDto) throws Exception {
        String projectId = userRoleDto.getProjectId();
        String tenantId = userRoleDto.getTenantId();
        //检查项目的有效性
        Project project = this.getProjectByProjectId(projectId);
        if (Objects.isNull(project) || !tenantId.equals(project.getTenantId())){
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_PROJECTID);
        }
        List<Integer> roleIdList = userRoleDto.getRoleIdList();
        List<String> usernameList = userRoleDto.getUsernameList();

        dataPrivilegeGroupMemberService.addNewProjectMemberToGroup(project, usernameList);
        //根据用户列表循环创建用户在项目的角色
        for (String username:usernameList) {
            for (Integer roleId:roleIdList) {//roleList一般是一个
                this.createProjectRole(tenantId,projectId,username,roleId);
                //更新用户角色状态
                Boolean status = clusterCacheManager.getRolePrivilegeStatusForTenantOrProject(roleId,username,null, projectId);
                if (status){
                    clusterCacheManager.updateRolePrivilegeStatusForTenantOrProject(roleId,username,null,projectId,Boolean.FALSE);
                }
                //处理harbor的角色权限关系
                HarborMemberEnum targetMember =  rolePrivilegeService.getHarborRole(roleId);
                this.roleLocalService.addHarborUserRole(targetMember,projectId,username,roleId);
            }
        }
    }

    /**
     * 删除项目中用户角色
     *
     * @param userRoleDto
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserRole(UserRoleDto userRoleDto) throws Exception {
        String projectId = userRoleDto.getProjectId();
        String tenantId = userRoleDto.getTenantId();
        String username = userRoleDto.getUsername();
        Integer roleId = userRoleDto.getRoleId();
        //检查项目的有效性
        Project project = this.getProjectByProjectId(projectId);
        if (Objects.isNull(project) || !tenantId.equals(project.getTenantId())){
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_PROJECTID);
        }
        this.deleteProjectRole(projectId,username,roleId);
        //更新redis中用户的状态
        clusterCacheManager.updateRolePrivilegeStatusForTenantOrProject(roleId,username,null,projectId,Boolean.TRUE);

        //处理harbor的角色权限关系
        this.roleLocalService.updateHarborUserRole(HarborMemberEnum.NONE,projectId,username);
    }

    @Override
    public String getProjectNameByProjectId(String projectId) throws Exception {
        Project project = getProjectByProjectId(projectId);
        if (null == project) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
        }
        return project.getProjectName();
    }

}
