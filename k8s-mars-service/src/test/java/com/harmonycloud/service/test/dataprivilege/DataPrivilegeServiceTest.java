package com.harmonycloud.service.test.dataprivilege;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.application.DeploymentDetailDto;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.test.BaseTest;
import com.harmonycloud.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by anson on 18/6/27.
 */
public class DataPrivilegeServiceTest extends BaseTest{

    @Autowired
    ProjectService projectService;

    @Autowired
    DataPrivilegeService dataPrivilegeService;

    @Autowired
    DataPrivilegeGroupMemberService dataPrivilegeGroupMemberService;

    @Autowired
    HttpSession session;

    @Autowired
    UserService userService;

    DeploymentDetailDto deploymentDetailDto = new DeploymentDetailDto();
    DataPrivilegeDto dataPrivilegeDto = new DataPrivilegeDto();

    @BeforeMethod
    void initData() throws Exception {
        session.setAttribute(CommonConstant.USERNAME, adminUserName);
        User user = userService.getUser(adminUserName);
        session.setAttribute(CommonConstant.USERID, user.getId());
        deploymentDetailDto.setName("nginx");
        deploymentDetailDto.setNamespace("test-ns");
        deploymentDetailDto.setProjectId(projectId);
        deploymentDetailDto.setClusterIP(devClusterId);
        dataPrivilegeDto.setData("nginx");
        dataPrivilegeDto.setProjectId(projectId);
        dataPrivilegeDto.setNamespace("test-ns");
        dataPrivilegeDto.setDataResourceType(DataResourceTypeEnum.SERVICE.getCode());
    }

    @Test
    public void testAddAndDelResource() throws Exception {
        dataPrivilegeService.addResource(deploymentDetailDto, null, null);
        Map<String, Object> resultMap =  dataPrivilegeGroupMemberService.listGroupMemberForData(dataPrivilegeDto);
        assertNotNull(resultMap.get("roGroupId"));
        assertNotNull(resultMap.get("rwGroupId"));
        assertNotNull(resultMap.get("roList"));
        assertNotNull(resultMap.get("rwList"));

        dataPrivilegeService.deleteResource(deploymentDetailDto);
        resultMap =  dataPrivilegeGroupMemberService.listGroupMemberForData(dataPrivilegeDto);
        assertNull(resultMap.get("roGroupId"));
        assertNull(resultMap.get("rwGroupId"));
        assertEquals(resultMap.get("roList"), Collections.emptyList());
        assertEquals(resultMap.get("rwList"), Collections.emptyList());

    }

}
