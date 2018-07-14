package com.harmonycloud.service.test.dataprivilege;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.application.ApplicationDeployDto;
import com.harmonycloud.dto.application.ApplicationDetailDto;
import com.harmonycloud.dto.application.DeploymentDetailDto;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.service.common.DataPrivilegeHelper;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMappingService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.test.BaseTest;
import com.harmonycloud.service.user.UserService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by chencheng on 18-6-25
 */

public class DataPrivilegeHelperTest extends BaseTest{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DataPrivilegeHelper dataPrivilegeHelper;

    @Autowired
    DataPrivilegeGroupMappingService dataPrivilegeGroupMappingService;

    @Autowired
    HttpSession session;

    @Autowired
    UserService userService;

    @Autowired
    TenantService tenantService;

    @Autowired
    DataPrivilegeService dataPrivilegeService;

    User user1 = new User();
    User user2 = new User();
    DeploymentDetailDto deploymentDetailDto;
    DataPrivilegeDto dataPrivilegeDto;
    ApplicationDeployDto applicationDeployDto;
    ApplicationDetailDto applicationDetailDto;



    @BeforeMethod
    public void initData() throws Exception {
        user1.setUsername("testuser1");
        user1.setPassword("Ab123456");
        user1.setRealName("testone");
        user1.setEmail("testuser1@abc.com");
        user2.setUsername("testuser2");
        user2.setPassword("Ab123456");
        user2.setRealName("testtwo");
        user2.setEmail("testuser2@abc.com");
        userService.addUser(user1);
        userService.addUser(user2);
        user1 = userService.getUser(user1.getUsername());
        user2 = userService.getUser(user2.getUsername());
        tenantService.updateTenantStrategy(tenantId, CommonConstant.DATA_CLOSED_STRATEGY);

        session.setAttribute(CommonConstant.TENANT_ID, tenantId);
        session.setAttribute(CommonConstant.USERNAME, user1.getUsername());
        session.setAttribute(CommonConstant.USERID, user1.getId());
        session.setAttribute(CommonConstant.ROLEID, CommonConstant.UAT_ROLEID);
        applicationDeployDto = new ApplicationDeployDto();
        applicationDeployDto.setAppName("nginx");
        applicationDeployDto.setNamespace("test-ns");
        applicationDeployDto.setProjectId(projectId);

        applicationDetailDto = new ApplicationDetailDto();
        applicationDetailDto.setName("nginx");
        applicationDetailDto.setNamespace("test-ns");

        deploymentDetailDto = new DeploymentDetailDto();
        deploymentDetailDto.setName("nginx-service");
        deploymentDetailDto.setNamespace("test-ns");
        deploymentDetailDto.setProjectId(projectId);

        dataPrivilegeDto = new DataPrivilegeDto();
        dataPrivilegeDto.setData("nginx-service");
        dataPrivilegeDto.setProjectId(projectId);
        dataPrivilegeDto.setNamespace("test-ns");
        dataPrivilegeDto.setDataResourceType(DataResourceTypeEnum.SERVICE.getCode());

        dataPrivilegeService.addResource(applicationDeployDto, null, null);
        dataPrivilegeService.addResource(deploymentDetailDto, null, null);
    }

    @Test
    public void testFilter() throws Exception {

        ApplicationDetailDto filterApplicationDetailDto = dataPrivilegeHelper.filter(applicationDetailDto);
        assertNotNull(filterApplicationDetailDto);
        assertEquals(filterApplicationDetailDto.getDataPrivilege(), "rw");

        session.setAttribute(CommonConstant.USERNAME, user2.getUsername());
        session.setAttribute(CommonConstant.USERID, user2.getId());
        filterApplicationDetailDto = dataPrivilegeHelper.filter(applicationDetailDto);
        assertNull(filterApplicationDetailDto);

        session.setAttribute(CommonConstant.ROLEID, CommonConstant.ADMIN_ROLEID);
        filterApplicationDetailDto = dataPrivilegeHelper.filter(applicationDetailDto);
        assertNotNull(filterApplicationDetailDto);
        assertEquals(filterApplicationDetailDto.getDataPrivilege(), "rw");
    }

    @Test
    public void testFilterList() throws Exception{
        List<ApplicationDetailDto> list = new ArrayList<>();
        list.add(applicationDetailDto);

        List<ApplicationDetailDto> filterList = dataPrivilegeHelper.filter(list);
        assertEquals(filterList.size(), 1);
        ApplicationDetailDto filterApplicationDetailDto = filterList.get(0);
        assertEquals(filterApplicationDetailDto.getDataPrivilege(),"rw");

        session.setAttribute(CommonConstant.USERNAME, user2.getUsername());
        filterList = dataPrivilegeHelper.filter(list);
        assertEquals(filterList.size(), 0);

        session.setAttribute(CommonConstant.ROLEID, CommonConstant.ADMIN_ROLEID);
        filterList = dataPrivilegeHelper.filter(list);
        assertEquals(filterList.size(), 1);
        filterApplicationDetailDto = filterList.get(0);
        assertEquals(filterApplicationDetailDto.getDataPrivilege(),"rw");
    }

    @Test
    public void testFilterMap() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "nginx-service");
        map.put("namespace", "test-ns");

        dataPrivilegeDto.setData((String) map.get(CommonConstant.NAME));
        dataPrivilegeDto.setNamespace((String) map.get(CommonConstant.DATA_NAMESPACE));
        dataPrivilegeDto.setDataResourceType(DataResourceTypeEnum.SERVICE.getCode());
        Map filteredMap = dataPrivilegeHelper.filterMap(map, dataPrivilegeDto);
        assertNotNull(filteredMap);
        assertEquals(filteredMap.get("dataPrivilege"), "rw");
        map.remove("dataPrivilege");

        session.setAttribute(CommonConstant.USERNAME, user2.getUsername());
        filteredMap = dataPrivilegeHelper.filterMap(map, dataPrivilegeDto);
        assertNull(filteredMap);
        map.remove("dataPrivilege");

        session.setAttribute(CommonConstant.ROLEID, CommonConstant.ADMIN_ROLEID);
        filteredMap = dataPrivilegeHelper.filterMap(map, dataPrivilegeDto);
        assertNotNull(filteredMap);
        assertEquals(filteredMap.get("dataPrivilege"), "rw");
    }
}