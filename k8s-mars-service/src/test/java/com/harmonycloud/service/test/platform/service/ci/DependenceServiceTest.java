package com.harmonycloud.service.test.platform.service.ci;

import com.harmonycloud.dto.cicd.DependenceDto;
import com.harmonycloud.service.platform.service.ci.DependenceService;
import com.harmonycloud.service.test.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import javax.validation.constraints.AssertTrue;

import java.util.List;

import static org.testng.Assert.*;

public class DependenceServiceTest extends BaseTest {
    /*test order
     * 0---testAdd()
     * 1---testUploadFile()
     * 2---testListByProjectIdAndClusterId()
     * 3---testListFile()
     * 4---testFindDependenceFileByKeyword()
     * 5---testDeleteFile()
     * 6---testDelete()
     * 7---testDeleteDependenceByProject()
     * */

    @Autowired
    DependenceService dependenceService;


    @Test(priority = 2)
    public void testListByProjectIdAndClusterId() throws Exception{
        List list = dependenceService.listByProjectIdAndClusterId(projectId, devClusterId, null);
    }


    @Test(priority = 0)
    public void testAdd() throws Exception {
        //common=true
        DependenceDto dependenceDtoCommon = new DependenceDto();
        dependenceDtoCommon.setProjectId(projectId);
        dependenceDtoCommon.setClusterId("");
        dependenceDtoCommon.setCommon(true);
        dependenceDtoCommon.setName("dependence-common-testng");
        dependenceService.add(dependenceDtoCommon);

        //common=false
        DependenceDto dependenceDtoPrivate = new DependenceDto();
        dependenceDtoPrivate.setProjectId(projectId);
        dependenceDtoPrivate.setClusterId(devClusterId);
        dependenceDtoPrivate.setCommon(false);
        dependenceDtoPrivate.setName("dependence-private-testng");
        dependenceService.add(dependenceDtoPrivate);
    }


    @Test(priority = 6)
    public void testDelete() throws Exception {
        dependenceService.delete("dependence-common-testng", projectId, null);
        dependenceService.delete("dependence-private-testng", projectId, devClusterId);
    }


    @Test(priority = 1)
    public void testUploadFile() {
    }


    @Test(priority = 3)
    public void testListFile() throws Exception {
        dependenceService.listFile("dependence-private-testng", projectId, devClusterId, null);
    }

    @Test(priority = 5)
    public void testDeleteFile() throws Exception {

    }

    @Test(priority = 7)
    public void testDeleteDependenceByProject() {
    }


    @Test(priority = 4)
    public void testFindDependenceFileByKeyword() throws Exception {
        dependenceService.findDependenceFileByKeyword("dependence-private-testng", projectId, devClusterId, "test");
    }

}