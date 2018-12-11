package com.harmonycloud.service.test;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ClusterLevelEnum;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.ObjConverter;
import com.harmonycloud.dao.application.bean.ConfigFile;
import com.harmonycloud.dao.application.bean.ConfigFileItem;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.dto.config.ConfigDetailDto;
import com.harmonycloud.dto.tenant.*;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.bean.DeploymentList;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.application.ApplicationDeployService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.application.StorageClassService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.ConfigCenterService;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.CollectionUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.http.HttpSession;
import java.util.*;

import static com.harmonycloud.common.Constant.CommonConstant.FLAG_FALSE;
import static com.harmonycloud.common.Constant.CommonConstant.FLAG_TRUE;

/**
 * Created by zhangkui on 2018/6/19.
 */
@Test
@WebAppConfiguration
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class BaseTest extends AbstractTransactionalTestNGSpringContextTests {
    protected Logger logger= LoggerFactory.getLogger(BaseTest.class);
    private static final String NFS_SERVER = "10.10.101.91";
    protected static final String TEST_NAME = "unittest";
    protected static final String TEST_ALISA_NAME = "单元测试";
    protected static final String DEFAULT_SERVICE_NAME = "tomcat";
    protected static final String DEFAULT_CONFIG_NAME = "configMapName";
    protected static final String TEST_REPONAME = "onlineshop/tomcat";
    protected static final String DEFAULT_CONFIG_TAG = "1.0";
    protected List<Cluster> clusters;
    protected String devClusterId;
    protected Cluster devCluster;
    protected String qasClusterId;
    protected String platformClusterId;
    protected String tenantId;
    protected String tenantName;
    protected String projectId;
    protected String adminUserName = "admin";
    protected String testUserName = TEST_NAME;
    protected List<NamespaceLocal> namespaces;
    protected String namespaceName;
    protected ConfigFile configMap;
    protected ServiceDeployDto serviceDeployDto;
    protected  ApplicationTemplateDto applicationTemplateDto;


    @Autowired
    private ClusterService clusterService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private UserService userService;
    @Autowired
    private StorageClassService storageClassService;
    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private ServiceService serviceService;
    @Autowired
    private ApplicationDeployService applicationDeployService;
    @Autowired
    private DeploymentsService deploymentsService;
    @Autowired
    private ConfigCenterService configCenterService;
    @Autowired
    private HttpSession session;

    @BeforeClass
    public void initTestData() throws Exception{
        if(clusters != null){
            return;
        }
        logger.info("初始化测试数据......");
        //初始化集群数据
        clusters = clusterService.listAllCluster(null);
        if(CollectionUtils.isEmpty(clusters)){
            throw new Exception("cluster list is empty.");
        }
        session.setAttribute(CommonConstant.ROLEID,1);
        for(Cluster cluster: clusters){
            if(cluster.getLevel() == ClusterLevelEnum.DEV.getLevel()){
                devClusterId = cluster.getId();
                devCluster = cluster;
            }else if(cluster.getLevel() == ClusterLevelEnum.QAS.getLevel()){
                qasClusterId = cluster.getId();
            }else if(cluster.getLevel() == ClusterLevelEnum.PLATFORM.getLevel()){
                platformClusterId = cluster.getId();
            }
        }
        User user = getTestUser();
        if(user == null){
            throw new Exception("创建测试用户失败.");
        }
        TenantBinding tenant = getTenant();
        tenantId = tenant.getTenantId();
        tenantName = tenant.getTenantName();
        namespaces = getNamespace();
        if(CollectionUtils.isEmpty(namespaces)){
            throw new Exception("未创建单元测试分区.");
        }
        namespaceName = namespaces.get(0).getNamespaceName();
        projectId = getProject().getProjectId();
        configMap = getConfigMap();
        if(configMap == null){
            throw new Exception("创建测试配置组失败.");
        }
        getTomcatService();
        serviceDeployDto = getStatefulSetDto();
        applicationTemplateDto = getApplicationTemplateDto();

    }

    private User getTestUser() throws Exception{
        User user = userService.getUser(TEST_NAME);
        if(user != null){
            return user;
        }
        logger.info("创建单元测试用户......");
        user = new User();
        user.setUsername(TEST_NAME);
        user.setPassword("Ab123456");
        user.setRealName("单元测试");
        user.setEmail("k8sdev@harmonycloud.cn");
        user.setPhone("13066666666");
        user.setIsAdmin(1);
        userService.addUser(user);
        return userService.getUser(TEST_NAME);
    }

    private TenantBinding getTenant() throws Exception{
        TenantBinding tenant = tenantService.getTenantBytenantName(TEST_NAME);
        if(tenant != null) {
            return tenant;
        }
        logger.info("创建单元测试租户......");
        TenantDto tenantDto = new TenantDto();
        tenantDto.setAliasName(TEST_ALISA_NAME);
        tenantDto.setTenantName(TEST_NAME);
        tenantDto.setStrategy(3);
        List<String> tmList = new ArrayList<>();
        tmList.add(TEST_NAME);
        tenantDto.setTmList(tmList);

        ClusterQuotaDto clusterQuotaDto = new ClusterQuotaDto();
        clusterQuotaDto.setClusterName(devCluster.getName());
        clusterQuotaDto.setClusterAliasName(devCluster.getAliasName());
        clusterQuotaDto.setClusterId(devCluster.getId());
        clusterQuotaDto.setCpuQuota(1d);
        clusterQuotaDto.setCpuQuotaType("Core");
        clusterQuotaDto.setMemoryQuota(2048d);
        clusterQuotaDto.setMemoryQuotaType("MB");
        StorageClassDto storageClassDto = this.getStorageClass();
        List<StorageDto>  storageDtos= new ArrayList<>();
        StorageDto storageDto = new StorageDto();
        storageDto.setName(storageClassDto.getName());
        storageDto.setStorageType("GB");
        storageDto.setTotalStorage(storageClassDto.getStorageLimit());
        storageDto.setStorageQuota("3");
        storageDtos.add(storageDto);
        clusterQuotaDto.setStorageQuota(storageDtos);
        List<ClusterQuotaDto> clusterQuotaDtos = new ArrayList<>();
        clusterQuotaDtos.add(clusterQuotaDto);
        tenantDto.setClusterQuota(clusterQuotaDtos);
        tenantService.createTenant(tenantDto);
        tenant = tenantService.getTenantBytenantName(TEST_NAME);
        return tenant;

    }

    private Project getProject() throws Exception{
        Project project = projectService.getProjectByProjectName(TEST_NAME);
        if(project != null){
            return project;
        }
        logger.info("创建单元测试项目......");
        ProjectDto projectDto = new ProjectDto();
        projectDto.setTenantId(tenantId);
        projectDto.setAliasName(TEST_ALISA_NAME);
        projectDto.setProjectName(TEST_NAME);
        List<String> pm = new ArrayList<>();
        pm.add(TEST_NAME);
        projectDto.setPmList(pm);
        projectService.createProject(projectDto);
        project = projectService.getProjectByProjectName(TEST_NAME);
        return project;
    }


    private List<NamespaceLocal> getNamespace() throws Exception{
        List<NamespaceLocal> namespaces = namespaceService.listNamespaceNameByTenantid(tenantId);
        if(!CollectionUtils.isEmpty(namespaces)) {
            return namespaces;
        }
        logger.info("创建单元测试分区......");
        NamespaceDto namespaceDto = new NamespaceDto();
        namespaceDto.setAliasName(TEST_ALISA_NAME);
        namespaceDto.setName(TEST_NAME+"-"+TEST_NAME);
        namespaceDto.setClusterId(devClusterId);
        namespaceDto.setTenantId(tenantId);
        QuotaDto quotaDto = new QuotaDto();
        quotaDto.setCpu("0.5");
        quotaDto.setMemory("1Gi");
        namespaceDto.setQuota(quotaDto);
        StorageClassQuotaDto storageClassQuotaDto = new StorageClassQuotaDto();
        storageClassQuotaDto.setQuota("1");
        storageClassQuotaDto.setTotalQuota("5");
        storageClassQuotaDto.setName(TEST_NAME);
        List<StorageClassQuotaDto> storageClassQuotaDtos = new ArrayList<>();
        storageClassQuotaDtos.add(storageClassQuotaDto);
        namespaceDto.setStorageClassQuotaList(storageClassQuotaDtos);
        namespaceService.createNamespace(namespaceDto);
        //创建第二个分区
        namespaceDto.setAliasName(TEST_ALISA_NAME+"2");
        namespaceDto.setName(TEST_NAME+"-"+TEST_NAME+"2");
        namespaceService.createNamespace(namespaceDto);
        namespaces = namespaceService.listNamespaceNameByTenantid(tenantId);
        return namespaces;
    }

    private StorageClassDto getStorageClass() throws Exception{
        ActionReturnUtil response = storageClassService.getStorageClass(TEST_NAME, devClusterId);
        if(response.isSuccess() && response.getData() != null){
            return (StorageClassDto) response.getData();
        }
        logger.info("创建单元测试存储StorageClass......");
        List<StorageClassDto> storageClassDtos = storageClassService.listStorageClass(devClusterId);
        String nfsServer = null;
        String nfsPath = null;
        if(!CollectionUtils.isEmpty(storageClassDtos)){
            for(StorageClassDto storageClassDto : storageClassDtos){
                if(storageClassDto.getStatus() == FLAG_TRUE){
                    nfsServer = storageClassDto.getConfigMap().get("NFS_SERVER");
                    nfsPath = storageClassDto.getConfigMap().get("NFS_PATH");
                }
            }
        }
        StorageClassDto storageClassDto = new StorageClassDto();
        storageClassDto.setClusterId(devClusterId);
        storageClassDto.setType("NFS");
        storageClassDto.setStorageLimit("5");
        storageClassDto.setName(TEST_NAME);
        Map<String,String> configMap = new HashMap<>();
        configMap.put("NFS_SERVER",nfsServer == null ? NFS_SERVER : nfsServer);
        configMap.put("NFS_PATH", nfsPath == null ? "/nfs/" + devCluster.getName() + "/"+TEST_NAME : nfsPath);
        storageClassDto.setConfigMap(configMap);
        storageClassService.createStorageClass(storageClassDto);
        return storageClassDto;
    }

    private ConfigFile getConfigMap() throws Exception{
        ConfigFile configByNameAndTag = configCenterService.getConfigByNameAndTag(DEFAULT_CONFIG_NAME, DEFAULT_CONFIG_TAG, projectId, devClusterId);
        if(configByNameAndTag != null){
            return configByNameAndTag;
        }
        logger.info("创建单元测试配置组......");
        configByNameAndTag = new ConfigFile();
        configByNameAndTag.setClusterId(devClusterId);
        Date local_date = new Date();
        configByNameAndTag.setName(DEFAULT_CONFIG_NAME);
        configByNameAndTag.setProjectId(projectId);
        configByNameAndTag.setTags(DEFAULT_CONFIG_TAG);
        configByNameAndTag.setTenantId(tenantId);
        configByNameAndTag.setRepoName(TEST_REPONAME);

        List<ConfigFileItem> configFileItemList = new ArrayList<>();
        ConfigFileItem configFileItem = new ConfigFileItem();
        configFileItem.setContent("a=b");
        configFileItem.setFileName("config1");
        configFileItem.setPath("/tmp2");
        ConfigFileItem configFileItem1 = new ConfigFileItem();
        configFileItem1.setContent("b=c");
        configFileItem1.setFileName("config2");
        configFileItem1.setPath("/tmp");
        configFileItemList.add(0,configFileItem);
        configFileItemList.add(1,configFileItem1);
        configByNameAndTag.setConfigFileItemList(configFileItemList);

        ConfigDetailDto configDetailDto = ObjConverter.convert(configByNameAndTag, ConfigDetailDto.class);
        ActionReturnUtil actionReturnUtil = configCenterService.saveConfig(configDetailDto, adminUserName);
        System.out.println(actionReturnUtil.getData());
        return configCenterService.getConfigByNameAndTag(DEFAULT_CONFIG_NAME, DEFAULT_CONFIG_TAG, projectId, devClusterId);
    }

    private Deployment getTomcatService() throws Exception{
        DeploymentList deploymentList = deploymentsService.listDeployments(namespaceName, projectId);
        if(deploymentList != null || !CollectionUtils.isEmpty(deploymentList.getItems())){
            List<Deployment> deployments = deploymentList.getItems();
            for(Deployment deployment : deployments){
                if(deployment.getMetadata().getName().equalsIgnoreCase(DEFAULT_SERVICE_NAME)){
                    return deployment;
                }
            }
        }
        logger.info("创建单元测试应用服务Tomcat......");
        ApplicationDeployDto applicationDeployDto = new ApplicationDeployDto();
        applicationDeployDto.setNamespace(namespaceName);
        applicationDeployDto.setProjectId(projectId);
        applicationDeployDto.setAppName(DEFAULT_SERVICE_NAME);
        ApplicationTemplateDto applicationTemplateDto = new ApplicationTemplateDto();
        applicationTemplateDto.setClusterId(devClusterId);
        applicationTemplateDto.setIsDeploy(FLAG_TRUE);
        applicationTemplateDto.setTenant(TEST_NAME);
        applicationTemplateDto.setProjectId(projectId);
        applicationTemplateDto.setName(DEFAULT_SERVICE_NAME);

        List<ServiceTemplateDto> serviceTemplateDtos = new ArrayList<>();
        ServiceTemplateDto serviceTemplateDto = new ServiceTemplateDto();
        serviceTemplateDto.setName(DEFAULT_SERVICE_NAME);
        serviceTemplateDto.setTenant(TEST_NAME);
        serviceTemplateDto.setExternal(FLAG_FALSE);
        serviceTemplateDto.setId(1);
        serviceTemplateDto.setFlag(1);

        DeploymentDetailDto deploymentDetailDto = new DeploymentDetailDto();
        deploymentDetailDto.setRestartPolicy("Always");
        deploymentDetailDto.setName(DEFAULT_SERVICE_NAME);
        deploymentDetailDto.setInstance("1");

        List<CreateContainerDto> containers = new ArrayList<>();
        CreateContainerDto createContainerDto = new CreateContainerDto();

        List<CreateConfigMapDto> CreateConfigMapDtoList = new ArrayList<>();
        List<ConfigFileItem> configFileItemList = configMap.getConfigFileItemList();
        for (ConfigFileItem configFileItem : configFileItemList) {
            CreateConfigMapDto createConfigMapDto = new CreateConfigMapDto();
            createConfigMapDto.setPath(configFileItem.getPath());
            createConfigMapDto.setTag(configMap.getTags());
            createConfigMapDto.setConfigMapId(configFileItem.getConfigfileId());
            createConfigMapDto.setFile(configFileItem.getFileName());
            createConfigMapDto.setValue(configFileItem.getContent());
            CreateConfigMapDtoList.add(createConfigMapDto);
        }
        createContainerDto.setConfigmap(CreateConfigMapDtoList);

        List<CreateEnvDto> env = new ArrayList<>();
        CreateEnvDto createEnvDto = new CreateEnvDto();
        createEnvDto.setKey("JAVA_OPT");
        createEnvDto.setValue("-Xmx256m");
        CreateEnvDto createEnvDto1 = new CreateEnvDto();
        createEnvDto1.setKey("TZ");
        createEnvDto1.setValue("Asia/Shanghai");
        env.add(createEnvDto);
        env.add(createEnvDto1);
        createContainerDto.setEnv(env);
        createContainerDto.setImg("onlineshop/tomcat");
        createContainerDto.setName(DEFAULT_SERVICE_NAME);

        List<CreatePortDto> ports = new ArrayList<>();
        CreatePortDto createPortDto = new CreatePortDto();
        createPortDto.setExpose("true");
        createPortDto.setPort("8080");
        createPortDto.setProtocol("TCP");
        ports.add(createPortDto);
        createContainerDto.setPorts(ports);

        CreateResourceDto createResourceDto = new CreateResourceDto();
        createResourceDto.setCpu("200m");
        createResourceDto.setMemory("256");
        createContainerDto.setResource(createResourceDto);
        createContainerDto.setTag("v8.0");

        containers.add(createContainerDto);
        deploymentDetailDto.setContainers(containers);

        serviceTemplateDtos.add(serviceTemplateDto);
        serviceTemplateDto.setDeploymentDetail(deploymentDetailDto);
        applicationTemplateDto.setServiceList(serviceTemplateDtos);
        applicationDeployDto.setAppTemplate(applicationTemplateDto);

        applicationDeployService.deployApplicationTemplate(applicationDeployDto, TEST_NAME);
        //每隔3秒检查服务是否已经创建
        for(int i=0;i<10;i++){
            deploymentList = deploymentsService.listDeployments(namespaceName, projectId);
            if(deploymentList == null || CollectionUtils.isEmpty(deploymentList.getItems())){
                Thread.sleep(3000);
                continue;
            }
            List<Deployment> deployments = deploymentList.getItems();
            for(Deployment deployment : deployments){
                if(deployment.getMetadata().getName().equalsIgnoreCase(DEFAULT_SERVICE_NAME)){
                    return deployment;
                }
            }
        }
        return null;

    }

    private ServiceDeployDto getStatefulSetDto(){
        ServiceDeployDto serviceDeployDto = new ServiceDeployDto();
        serviceDeployDto.setNamespace(namespaceName);
        ServiceTemplateDto serviceTemplate = new ServiceTemplateDto();
        serviceTemplate.setName("ststest");
        serviceTemplate.setProjectId(projectId);
        serviceTemplate.setTenant(TEST_NAME);
        serviceTemplate.setExternal(0);
        serviceTemplate.setType(1);
        serviceTemplate.setPublic(false);
        StatefulSetDetailDto statefulSetDetail = new StatefulSetDetailDto();
        statefulSetDetail.setName("ststest");
        statefulSetDetail.setPodManagementPolicy("OrderedReady");
        statefulSetDetail.setRestartPolicy("Always");
        statefulSetDetail.setInstance("1");
        statefulSetDetail.setHostIPC(false);
        statefulSetDetail.setHostPID(false);
        statefulSetDetail.setHostNetwork(false);
        CreateContainerDto container = new CreateContainerDto();
        container.setName("container");
        container.setImg("onlineshop/nginx");
        container.setTag("latest");
        CreateResourceDto resource = new CreateResourceDto();
        resource.setCpu("100m");
        resource.setMemory("128");
        container.setResource(resource);
        CreateEnvDto env = new CreateEnvDto();
        env.setKey("TZ");
        env.setValue("Asia/Shanghai");
        container.setEnv(Arrays.asList(env));
        CreatePortDto port = new CreatePortDto();
        port.setProtocol("TCP");
        port.setPort("80");
        port.setExpose("true");
        container.setPorts(Arrays.asList(port));
        container.setImagePullPolicy("IfNotPresent");
        SecurityContextDto securityContext = new SecurityContextDto();
        securityContext.setSecurity(false);
        securityContext.setPrivileged(false);
        container.setSecurityContext(securityContext);
        statefulSetDetail.setContainers(Arrays.asList(container));
        CreateContainerDto initContainer = new CreateContainerDto();
        initContainer.setName("initContainer");
        initContainer.setPorts(null);
        initContainer.setCommand(Arrays.asList("sleep"));
        initContainer.setArgs(Arrays.asList("10"));
        initContainer.setImg("onlineshop/nginx");
        initContainer.setTag("latest");
        initContainer.setEnv(Arrays.asList(env));
        initContainer.setPorts(Arrays.asList(port));
        initContainer.setImagePullPolicy("IfNotPresent");
        initContainer.setSecurityContext(securityContext);
        statefulSetDetail.setInitContainers(Arrays.asList(initContainer));
        serviceTemplate.setStatefulSetDetail(statefulSetDetail);
        serviceDeployDto.setServiceTemplate(serviceTemplate);
        return serviceDeployDto;
    }

    private ApplicationTemplateDto getApplicationTemplateDto(){
        ApplicationTemplateDto applicationTemplateDto = new ApplicationTemplateDto();
        applicationTemplateDto.setClusterId(devClusterId);
        applicationTemplateDto.setProjectId(projectId);
        applicationTemplateDto.setName("apptest");
        applicationTemplateDto.setTenant(tenantName);
        applicationTemplateDto.setIsDeploy(0);
        return applicationTemplateDto;
    }

}
