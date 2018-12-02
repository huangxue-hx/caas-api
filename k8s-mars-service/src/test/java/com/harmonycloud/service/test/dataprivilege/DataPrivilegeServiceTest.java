package com.harmonycloud.service.test.dataprivilege;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMapping;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.application.DeploymentDetailDto;
import com.harmonycloud.dto.cicd.JobDto;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMappingService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeService;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.test.BaseTest;
import com.harmonycloud.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.util.CollectionUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by anson on 18/6/27.
 */
public class DataPrivilegeServiceTest extends BaseTest{

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DataPrivilegeService dataPrivilegeService;

    @Autowired
    private DataPrivilegeGroupMemberService dataPrivilegeGroupMemberService;

    @Autowired
    private HttpSession session;

    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    @Autowired
    private DataPrivilegeGroupMappingService dataPrivilegeGroupMappingService;

    private static DeploymentDetailDto deploymentDetailDto = new DeploymentDetailDto();
    private static DataPrivilegeDto dataPrivilegeDto = new DataPrivilegeDto();
    private static JobDto jobDto = new JobDto();
    private static DataPrivilegeDto pipelineDataPrivilegeDto = new DataPrivilegeDto();

    @BeforeClass
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

        //流水线
        jobDto.setTenantId(tenantId);
        jobDto.setTenant(testUserName);
        jobDto.setProjectId(projectId);
        jobDto.setClusterId(devClusterId);
        jobDto.setName("testjob");
        int jobId= jobService.createJob(jobDto);
        jobDto.setId(jobId);
        pipelineDataPrivilegeDto.setData(String.valueOf(jobId));
        pipelineDataPrivilegeDto.setClusterId(devClusterId);
        pipelineDataPrivilegeDto.setProjectId(projectId);
        pipelineDataPrivilegeDto.setDataResourceType(DataResourceTypeEnum.PIPELINE.getCode());
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
        List<DataPrivilegeGroupMapping> mappingList = dataPrivilegeGroupMappingService.listDataPrivilegeGroupMapping(dataPrivilegeDto);
        assertTrue(CollectionUtils.isEmpty(mappingList));

    }

    @Test
    public void testPipeline() throws Exception {
        Map<String, Object> resultMap =  dataPrivilegeGroupMemberService.listGroupMemberForData(pipelineDataPrivilegeDto);
        assertNotNull(resultMap.get("roGroupId"));
        assertNotNull(resultMap.get("rwGroupId"));
        assertNotNull(resultMap.get("roList"));
        assertNotNull(resultMap.get("rwList"));

        jobService.deleteJob(jobDto.getId());
        List<DataPrivilegeGroupMapping> mappingList = dataPrivilegeGroupMappingService.listDataPrivilegeGroupMapping(pipelineDataPrivilegeDto);
        assertTrue(CollectionUtils.isEmpty(mappingList));
    }

    @AfterClass
    @Rollback(false)
    public void deleteData() throws Exception {
        Job job = jobService.getJobById(jobDto.getId());
        if(job != null) {
            jobService.deleteJob(jobDto.getId());
        }
    }

}
