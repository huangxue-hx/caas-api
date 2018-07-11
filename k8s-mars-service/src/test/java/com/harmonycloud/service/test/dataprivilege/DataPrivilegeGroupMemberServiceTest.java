package com.harmonycloud.service.test.dataprivilege;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMember;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.application.DeploymentDetailDto;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.test.BaseTest;
import com.harmonycloud.service.user.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by chencheng on 18-6-20
 */

public class DataPrivilegeGroupMemberServiceTest extends BaseTest{

    @Autowired
    DataPrivilegeGroupMemberService dataPrivilegeGroupMemberService;

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Autowired
    DataPrivilegeService dataPrivilegeService;

    @Autowired
    TenantService tenantService;

    @Autowired
    HttpSession session;

    User user;

    DeploymentDetailDto deploymentDetailDto;

    DataPrivilegeDto dataPrivilegeDto;

    @BeforeMethod
    public void initData() throws Exception {
        user = new User();
        user.setUsername("testuser123");
        user.setPassword("Ab123456");
        user.setEmail("testuser123@abc.com");
        user.setRealName("test");
        userService.addUser(user);
        user = userService.getUser(user.getUsername());

        session.setAttribute(CommonConstant.USERNAME, adminUserName);
        User currentUser = userService.getUser(adminUserName);
        session.setAttribute(CommonConstant.USERID, currentUser.getId());
        deploymentDetailDto = new DeploymentDetailDto();
        deploymentDetailDto.setName("nginx123");
        deploymentDetailDto.setNamespace("test-ns");
        deploymentDetailDto.setProjectId(projectId);
        deploymentDetailDto.setClusterIP(devClusterId);
        dataPrivilegeDto = new DataPrivilegeDto();
        dataPrivilegeDto.setData("nginx123");
        dataPrivilegeDto.setProjectId(projectId);
        dataPrivilegeDto.setDataResourceType(DataResourceTypeEnum.SERVICE.getCode());
        dataPrivilegeDto.setNamespace("test-ns");
        dataPrivilegeDto.setDataResourceType(DataResourceTypeEnum.SERVICE.getCode());
    }

    /**
     * 向组中添加用户
     */
    @Test
    public void testAddAndDelMemberToGroup() throws Exception {
        dataPrivilegeService.addResource(deploymentDetailDto, null, null);
        Map map = dataPrivilegeGroupMemberService.listGroupMemberForData(dataPrivilegeDto);
        int roGroupId = (Integer) map.get("roGroupId");
        List roList = (List) map.get("roList");

        DataPrivilegeGroupMember dataPrivilegeGroupMember = new DataPrivilegeGroupMember();
        dataPrivilegeGroupMember.setMemberId(user.getId().intValue());
        dataPrivilegeGroupMember.setUsername(user.getUsername());
        dataPrivilegeGroupMember.setGroupId(roGroupId);
        dataPrivilegeGroupMemberService.addMemberToGroup(dataPrivilegeGroupMember);
        List<DataPrivilegeGroupMember> list = dataPrivilegeGroupMemberService.listMemberInGroup(roGroupId);

        assertNotNull(list);
        assertEquals(roList.size() + 1, list.size());

        dataPrivilegeGroupMemberService.delMemberFromGroup(dataPrivilegeGroupMember);
        list = dataPrivilegeGroupMemberService.listMemberInGroup(roGroupId);

        assertEquals(roList.size(), list.size());
    }



    @Test
    public void testAddAndDelProjectMemberToGroup() throws Exception {
        dataPrivilegeService.addResource(deploymentDetailDto, null, null);
        //封闭策略增用户
        tenantService.updateTenantStrategy(tenantId, CommonConstant.DATA_CLOSED_STRATEGY);
        Project project = projectService.getProjectByProjectId(projectId);
        List<String> userList = new ArrayList<>();
        userList.add(user.getUsername());
        dataPrivilegeGroupMemberService.addNewProjectMemberToGroup(project, userList);
        Map map = dataPrivilegeGroupMemberService.listGroupMemberForData(dataPrivilegeDto);
        List<DataPrivilegeGroupMember> roList = (List<DataPrivilegeGroupMember>) map.get("roList");
        List<String> roUserList = roList.stream().map(DataPrivilegeGroupMember::getUsername).collect(Collectors.toList());
        List<DataPrivilegeGroupMember> rwList = (List<DataPrivilegeGroupMember>) map.get("rwList");
        List<String> rwUserList = rwList.stream().map(DataPrivilegeGroupMember::getUsername).collect(Collectors.toList());
        assertFalse(roUserList.contains(user.getUsername()));
        assertFalse(rwUserList.contains(user.getUsername()));

        //删用户
        dataPrivilegeGroupMemberService.deleteProjectMemberFromGroup(projectId, user.getUsername());

        //半开放策略增用户
        tenantService.updateTenantStrategy(tenantId, CommonConstant.DATA_SEMIOPEN_STRATEGY);
        dataPrivilegeGroupMemberService.addNewProjectMemberToGroup(project, userList);
        map = dataPrivilegeGroupMemberService.listGroupMemberForData(dataPrivilegeDto);
        roList = (List<DataPrivilegeGroupMember>) map.get("roList");
        roUserList = roList.stream().map(DataPrivilegeGroupMember::getUsername).collect(Collectors.toList());
        rwList = (List<DataPrivilegeGroupMember>) map.get("rwList");
        rwUserList = rwList.stream().map(DataPrivilegeGroupMember::getUsername).collect(Collectors.toList());
        assertTrue(roUserList.contains(user.getUsername()));
        assertFalse(rwUserList.contains(user.getUsername()));

        //删用户
        dataPrivilegeGroupMemberService.deleteProjectMemberFromGroup(projectId, user.getUsername());
        map = dataPrivilegeGroupMemberService.listGroupMemberForData(dataPrivilegeDto);
        roList = (List<DataPrivilegeGroupMember>) map.get("roList");
        roUserList = roList.stream().map(DataPrivilegeGroupMember::getUsername).collect(Collectors.toList());
        rwList = (List<DataPrivilegeGroupMember>) map.get("rwList");
        rwUserList = rwList.stream().map(DataPrivilegeGroupMember::getUsername).collect(Collectors.toList());
        assertFalse(roUserList.contains(user.getUsername()));
        assertFalse(rwUserList.contains(user.getUsername()));

        //开放策略增用户
        tenantService.updateTenantStrategy(tenantId, CommonConstant.DATA_OPEN_STRATEGY);
        dataPrivilegeGroupMemberService.addNewProjectMemberToGroup(project, userList);
        map = dataPrivilegeGroupMemberService.listGroupMemberForData(dataPrivilegeDto);
        roList = (List<DataPrivilegeGroupMember>) map.get("roList");
        roUserList = roList.stream().map(DataPrivilegeGroupMember::getUsername).collect(Collectors.toList());
        rwList = (List<DataPrivilegeGroupMember>) map.get("rwList");
        rwUserList = rwList.stream().map(DataPrivilegeGroupMember::getUsername).collect(Collectors.toList());
        assertFalse(roUserList.contains(user.getUsername()));
        assertTrue(rwUserList.contains(user.getUsername()));

        //删用户
        dataPrivilegeGroupMemberService.deleteProjectMemberFromGroup(projectId, user.getUsername());
        map = dataPrivilegeGroupMemberService.listGroupMemberForData(dataPrivilegeDto);
        roList = (List<DataPrivilegeGroupMember>) map.get("roList");
        roUserList = roList.stream().map(DataPrivilegeGroupMember::getUsername).collect(Collectors.toList());
        rwList = (List<DataPrivilegeGroupMember>) map.get("rwList");
        rwUserList = rwList.stream().map(DataPrivilegeGroupMember::getUsername).collect(Collectors.toList());
        assertFalse(roUserList.contains(user.getUsername()));
        assertFalse(rwUserList.contains(user.getUsername()));
    }

}