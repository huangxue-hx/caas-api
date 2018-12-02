package com.harmonycloud.service.test.application;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.application.StatefulSetsService;
import com.harmonycloud.service.platform.bean.AppDetail;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.test.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;


/**
 * Created by anson on 18/8/15.
 */
public class StatefulSetServiceTest extends BaseTest {
    private Logger LOGGER = LoggerFactory.getLogger(StatefulSetServiceTest.class);
    @Autowired
    private ServiceService serviceService;

    @Autowired
    private StatefulSetsService statefulSetsService;


    private String name = "ststest";
    private String containerName = "container";
    private String initContainerName = "initContainer";


    @BeforeClass
    public void setUp() throws Exception {
        LOGGER.info("StatefulSetServiceTest start.");
    }

    @Test
    public void test1CreateStatefulSet() throws Exception {
        LOGGER.info("testCreateStatefulSet.");
        ServiceDeployDto serviceDeployDto = new ServiceDeployDto();
        serviceDeployDto.setNamespace(namespaceName);
        ServiceTemplateDto serviceTemplate = new ServiceTemplateDto();
        serviceTemplate.setName(name);
        serviceTemplate.setProjectId(projectId);
        serviceTemplate.setTenant(TEST_NAME);
        serviceTemplate.setExternal(0);
        serviceTemplate.setType(1);
        serviceTemplate.setPublic(false);
        StatefulSetDetailDto statefulSetDetail = new StatefulSetDetailDto();
        statefulSetDetail.setName(name);
        statefulSetDetail.setPodManagementPolicy("OrderedReady");
        statefulSetDetail.setRestartPolicy("Always");
        statefulSetDetail.setInstance("1");
        statefulSetDetail.setHostIPC(false);
        statefulSetDetail.setHostPID(false);
        statefulSetDetail.setHostNetwork(false);
        CreateContainerDto container = new CreateContainerDto();
        container.setName(containerName);
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
        initContainer.setName(initContainerName);
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
        serviceService.deployService(serviceDeployDto, adminUserName);

        assertNotNull(statefulSetsService.getStatefulSetDetail(namespaceName, name));
    }

    @Test
    public void test2ListStatefulSet() throws Exception {
        LOGGER.info("testListStatefulSet.");
        Map<String, Object> bodys = new HashMap<String, Object>();
        String labelSelector = Constant.NODESELECTOR_LABELS_PRE + Constant.LABEL_PROJECT_ID + "=" + projectId;
        bodys.put("labelSelector", labelSelector);
        List<Map<String, Object>> list = statefulSetsService.listStatefulSets(tenantId, null, namespaceName, null, projectId, devClusterId);
        assertNotNull(list);
    }


    @Test
    public void test3StopStatefulSet() throws Exception {
        statefulSetsService.stopStatefulSet(name, namespaceName, adminUserName);
        AppDetail detail = statefulSetsService.getStatefulSetDetail(namespaceName, name);
        assertTrue(Constant.SERVICE_START.equals(detail.getStatus()) || Constant.SERVICE_STARTING.equals(detail.getStatus()));

    }

    @Test
    public void test4StartStatefulSet() throws Exception {
        statefulSetsService.startStatefulSet(name, namespaceName, adminUserName);
        AppDetail detail = statefulSetsService.getStatefulSetDetail(namespaceName, name);
        assertTrue(Constant.SERVICE_STOP.equals(detail.getStatus()) || Constant.SERVICE_STOPPING.equals(detail.getStatus()));
    }

    @Test
    public void test5DeleteStatefulSet() throws Exception {
        DeployedServiceNamesDto deployedServiceNamesDto = new DeployedServiceNamesDto();
        ServiceNameNamespace serviceNameNamespace = new ServiceNameNamespace();
        serviceNameNamespace.setName(name);
        serviceNameNamespace.setNamespace(namespaceName);
        serviceNameNamespace.setServiceType(Constant.STATEFULSET);
        deployedServiceNamesDto.setServiceList(Arrays.asList(serviceNameNamespace));
        serviceService.deleteDeployedService(deployedServiceNamesDto, adminUserName);
        try {
            statefulSetsService.getStatefulSetDetail(namespaceName, name);
            Assert.fail();
        }catch(MarsRuntimeException e){
            assertEquals(ErrorCodeMessage.DEPLOYMENT_GET_FAILURE,e.getErrorCode());
        }

    }
}
