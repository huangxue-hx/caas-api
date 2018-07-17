package com.harmonycloud.service.test;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ClusterLevelEnum;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.application.StorageClassDto;
import com.harmonycloud.dto.tenant.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.application.StorageClassService;
import com.harmonycloud.service.cluster.ClusterService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    protected List<Cluster> clusters;
    protected String devClusterId;
    protected Cluster devCluster;
    protected String qasClusterId;
    protected String platfromClusterId;
    protected String tenantId;
    protected String projectId;
    protected String adminUserName = "admin";
    protected List<NamespaceLocal> namespaces;

    @Autowired
    ClusterService clusterService;
    @Autowired
    ProjectService projectService;
    @Autowired
    TenantService tenantService;
    @Autowired
    UserService userService;
    @Autowired
    StorageClassService storageClassService;
    @Autowired
    NamespaceService namespaceService;
    @Autowired
    HttpSession session;

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
                platfromClusterId = cluster.getId();
            }
        }
        User user = getTestUser();
        if(user == null){
            throw new Exception("创建测试用户失败.");
        }
        tenantId = getTenant().getTenantId();
        namespaces = getNamespace();
        projectId = getProject().getProjectId();

    }

    private User getTestUser() throws Exception{
        User user = userService.getUser(TEST_NAME);
        if(user == null){
            user = new User();
            user.setUsername(TEST_NAME);
            user.setPassword("Ab123456");
            user.setRealName("单元测试");
            user.setEmail("k8sdev@harmonycloud.cn");
            user.setPhone("13066666666");
            user.setIsAdmin(1);
            userService.addUser(user);
        }
        return userService.getUser(TEST_NAME);
    }

    private TenantBinding getTenant() throws Exception{
        TenantBinding tenant = tenantService.getTenantBytenantName(TEST_NAME);
        if(tenant == null) {
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
        }
        return tenant;

    }

    private Project getProject() throws Exception{
        Project project = projectService.getProjectByProjectName(TEST_NAME);
        if(project == null){
            ProjectDto projectDto = new ProjectDto();
            projectDto.setTenantId(tenantId);
            projectDto.setAliasName(TEST_ALISA_NAME);
            projectDto.setProjectName(TEST_NAME);
            List<String> pm = new ArrayList<>();
            pm.add(TEST_NAME);
            projectDto.setPmList(pm);
            projectService.createProject(projectDto);
            project = projectService.getProjectByProjectName(TEST_NAME);
        }
        return project;

    }


    private List<NamespaceLocal> getNamespace() throws Exception{
        List<NamespaceLocal> namespaces = namespaceService.listNamespaceNameByTenantid(tenantId);
        if(CollectionUtils.isEmpty(namespaces)) {
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
        }
        return namespaces;
    }

    private StorageClassDto getStorageClass() throws Exception{
        ActionReturnUtil response = storageClassService.getStorageClass(TEST_NAME, devClusterId);
        if(response.isSuccess() && response.getData() != null){
            return (StorageClassDto) response.getData();
        }
        StorageClassDto storageClassDto = new StorageClassDto();
        storageClassDto.setClusterId(devClusterId);
        storageClassDto.setType("NFS");
        storageClassDto.setStorageLimit("5");
        storageClassDto.setName(TEST_NAME);
        Map<String,String> configMap = new HashMap<>();
        configMap.put("NFS_SERVER",NFS_SERVER);
        configMap.put("NFS_PATH", "/nfs/" + devCluster.getName() + "/"+TEST_NAME);
        storageClassDto.setConfigMap(configMap);
        storageClassService.createStorageClass(storageClassDto);
        return storageClassDto;
    }

}
