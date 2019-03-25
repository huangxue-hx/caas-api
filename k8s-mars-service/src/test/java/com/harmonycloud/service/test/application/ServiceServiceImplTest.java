package com.harmonycloud.service.test.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.test.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertTrue;
/**
 * Created by chencheng on 18-8-3
 */
public class ServiceServiceImplTest extends BaseTest {

    protected Logger logger = LoggerFactory.getLogger(ServiceServiceImplTest.class);

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;


    @Test
    public void testDeployService() throws Exception{
        CreateResourceDto resource = new CreateResourceDto();
        resource.setCpu("100m");
        resource.setCurrentRate(0);
        resource.setMemory("128");

        SecurityContextDto securityContext = new SecurityContextDto();
        securityContext.setPrivileged(false);
        securityContext.setSecurity(false);

        CreatePortDto cpd = new CreatePortDto();
        cpd.setExpose("true");
        cpd.setPort("80");
        cpd.setProtocol("TCP");
        List<CreatePortDto> ports = new ArrayList<>();
        ports.add(cpd);


        CreateEnvDto ced = new CreateEnvDto();
        ced.setKey("TZ");
        ced.setValue("Asia/Shanghai");
        List<CreateEnvDto> env = new ArrayList<>();
        env.add(ced);

        CreateContainerDto ccd = new CreateContainerDto();
        ccd.setImagePullPolicy("Always");
        ccd.setName("apptest");
        ccd.setImg("library/apptest");
        ccd.setTag("latest");
        ccd.setSyncTimeZone(false);
        ccd.setEnv(env);
        ccd.setPorts(ports);
        ccd.setResource(resource);
        ccd.setSecurityContext(securityContext);
        List<CreateContainerDto> containers = new ArrayList<>();
        containers.add(ccd);

        ServiceDependenceDto serviceDependence = new ServiceDependenceDto();
        serviceDependence.setDetectWay("TCP");
        serviceDependence.setFailThreshold(1);
        serviceDependence.setSuccessThreshold(1);
        serviceDependence.setIntervalTime(3);
        serviceDependence.setPort("80");
        serviceDependence.setServiceName("test.shenzhe-ns2");
//        serviceDependence.setUrl("/b/c/d");


        PullDependenceDto pullDependence = new PullDependenceDto();
        pullDependence.setBranch("wanhua");
        pullDependence.setUsername("chencheng");
        pullDependence.setPullWay("git");
        pullDependence.setPassword("ccdmm123456");
        pullDependence.setRepoUrl("http://10.10.102.101:8000/gitlab/hongjie/k8s-oam.git");
        pullDependence.setContainer("apptest");
        pullDependence.setMountPath("/empty-dir");

        DeploymentDetailDto detail = new DeploymentDetailDto();
        detail.setHostIPC(false);
        detail.setHostNetwork(false);
        detail.setHostPID(false);
        detail.setInstance("1");
        detail.setName("cc2");
        detail.setRestartPolicy("Always");
        detail.setContainers(containers);
        detail.setServiceDependence(serviceDependence);
        detail.setPullDependence(pullDependence);


        ServiceTemplateDto std = new ServiceTemplateDto();
        std.setDeploymentDetail(detail);
        std.setExternal(0);
        std.setName("cc2");
        std.setProjectId(projectId);
        std.setPublic(false);
        std.setTenant(TEST_NAME);
        std.setType(1);


        ServiceDeployDto serviceDeploy = new ServiceDeployDto();
        serviceDeploy.setNamespace(namespaceName);
        serviceDeploy.setServiceTemplate(std);

        ActionReturnUtil returnUtil = serviceService.deployService(serviceDeploy, TEST_NAME);
        assertTrue(returnUtil.isSuccess());
    }
}
