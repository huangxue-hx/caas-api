package com.harmonycloud.service.test.application;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.application.bean.ConfigFile;
import com.harmonycloud.dao.application.bean.ConfigFileItem;
import com.harmonycloud.dto.config.ConfigDetailDto;
import com.harmonycloud.service.platform.service.ConfigCenterService;
import com.harmonycloud.service.test.BaseTest;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

/**
 * Created by zqf on 2018/6/13.
 */
public class ConfigCenterServiceTest extends BaseTest {
    protected Logger logger= LoggerFactory.getLogger(ConfigCenterServiceTest.class);

    @Autowired
    ConfigCenterService configCenterService;
    @Autowired
    MockHttpSession session;

    private static String testConfigName = "configtestname";
    private static String testRepoName = "library/tomcat";
    private static ConfigDetailDto configDetail;

    @BeforeMethod
    public void createConfigData(){
        configDetail = new ConfigDetailDto();
        configDetail.setClusterId(devClusterId);
        configDetail.setProjectId(projectId);
        configDetail.setRepoName(testRepoName);
        configDetail.setTenantId(tenantId);
        configDetail.setName(testConfigName);
        List<ConfigFileItem> configFileItemList = new ArrayList<>();
        ConfigFileItem configFileItem = new ConfigFileItem();
        configFileItem.setContent("a=b");
        configFileItem.setFileName("config1");
        configFileItem.setPath(null);
        ConfigFileItem configFileItem1 = new ConfigFileItem();
        configFileItem1.setContent("b=c");
        configFileItem1.setFileName("config2");
        configFileItem1.setPath("/tmp");
        configFileItemList.add(0,configFileItem);
        configFileItemList.add(1,configFileItem1);
        configDetail.setConfigFileItemList(configFileItemList);
    }


    @Test
    public void testSaveConfig() throws Exception {
        ActionReturnUtil response = configCenterService.saveConfig(configDetail,adminUserName);
        assertTrue(response.isSuccess());
        JSONObject jsonObject = (JSONObject)response.getData();
        String tag = jsonObject.get("tag").toString();
        ActionReturnUtil configMap = configCenterService.getLatestConfigMap(testConfigName,projectId,testRepoName,devClusterId,tag);
        assertTrue(configMap.isSuccess());
        assertNotNull(configMap.getData());
        ConfigDetailDto configDetailDto = (ConfigDetailDto)configMap.getData();
        List<ConfigFileItem> configFileItems = configDetailDto.getConfigFileItemList();
        assertEquals(configFileItems.size(),2);
        assertEquals(configFileItems.get(0).getContent(),"a=b");
        assertEquals(configFileItems.get(0).getFileName(),"config1");
        assertNull(configFileItems.get(0).getPath());
        assertEquals(configFileItems.get(1).getContent(),"b=c");
        assertEquals(configFileItems.get(1).getFileName(),"config2");
        assertEquals(configFileItems.get(1).getPath(),"/tmp");
        response = configCenterService.saveConfig(configDetail,adminUserName);
        assertTrue(response.isSuccess());
        jsonObject = (JSONObject)response.getData();
        tag = jsonObject.get("tag").toString();
        assertEquals("1.1",tag);
    }


    @Test
    public void testUpdateConfig() throws Exception {
        ActionReturnUtil response = configCenterService.saveConfig(configDetail,adminUserName);
        assertTrue(response.isSuccess());
        JSONObject jsonObject = (JSONObject)response.getData();
        String tag = jsonObject.get("tag").toString();
        ActionReturnUtil configMap = configCenterService.getLatestConfigMap(testConfigName,projectId,testRepoName,devClusterId,tag);
        assertTrue(configMap.isSuccess());
        assertNotNull(configMap.getData());
        String id = ((ConfigDetailDto)configMap.getData()).getId();
        ConfigDetailDto configDetail = new ConfigDetailDto();
        configDetail.setClusterId(devClusterId);
        configDetail.setProjectId(projectId);
        configDetail.setRepoName(testRepoName);
        configDetail.setTenantId(tenantId);
        configDetail.setName(testConfigName);
        configDetail.setId(id);
        List<ConfigFileItem> configFileItemList = new ArrayList<>();
        ConfigFileItem configFileItem = new ConfigFileItem();
        configFileItem.setContent("a=a");
        configFileItem.setFileName("config12");
        configFileItem.setPath(null);
        ConfigFileItem configFileItem1 = new ConfigFileItem();
        configFileItem1.setContent("b=b");
        configFileItem1.setFileName("config22");
        configFileItem1.setPath("/tmp");
        configFileItemList.add(0,configFileItem);
        configFileItemList.add(1,configFileItem1);
        configDetail.setConfigFileItemList(configFileItemList);
        configCenterService.updateConfig(configDetail,adminUserName);
        configMap = configCenterService.getLatestConfigMap(testConfigName,projectId,testRepoName,devClusterId,tag);
        assertTrue(configMap.isSuccess());
        assertNotNull(configMap.getData());
        ConfigDetailDto configDetailDto = (ConfigDetailDto)configMap.getData();
        List<ConfigFileItem> configFileItems = configDetailDto.getConfigFileItemList();
        assertEquals(configFileItems.size(),2);
        assertEquals(configFileItems.get(0).getContent(),"a=a");
        assertEquals(configFileItems.get(0).getFileName(),"config12");
        assertEquals(configFileItems.get(1).getContent(),"b=b");
        assertEquals(configFileItems.get(1).getFileName(),"config22");
        assertEquals(configFileItems.get(1).getPath(),"/tmp");
    }

    @Test
    public void testSearchConfig() throws Exception {
        session.setAttribute(CommonConstant.ROLEID,1);
        ActionReturnUtil response = configCenterService.saveConfig(configDetail,adminUserName);
        assertTrue(response.isSuccess());
        ActionReturnUtil configMap = configCenterService.searchConfig(projectId,devClusterId,testRepoName,"configtest");
        assertTrue(configMap.isSuccess());
        Collection<ConfigFile> configFiles = (Collection<ConfigFile>)configMap.getData();
        assertTrue(configFiles.size() == 1);
    }

    @Test
    public void testDeleteConfig() throws Exception {
        ActionReturnUtil response = configCenterService.saveConfig(configDetail,adminUserName);
        assertTrue(response.isSuccess());
        JSONObject jsonObject = (JSONObject)response.getData();
        String tag = jsonObject.get("tag").toString();
        ActionReturnUtil configMap = configCenterService.getLatestConfigMap(testConfigName,projectId,testRepoName,devClusterId,tag);
        assertTrue(configMap.isSuccess());
        assertNotNull(configMap.getData());
        ConfigDetailDto data = (ConfigDetailDto) configMap.getData();
        configCenterService.deleteConfig(data.getId(),configDetail.getProjectId());
        assertNotNull(data.getId());
    }
    @Test
    public void testDeleteConfigByProject() throws Exception {
        assertNotNull(configDetail.getProjectId());
        ActionReturnUtil response = configCenterService.deleteConfigByProject(projectId);
        assertTrue(response.isSuccess());
    }


    @Test
    public void testDeleteConfigMap() throws Exception {
        ActionReturnUtil response = configCenterService.deleteConfigMap(testConfigName, configDetail.getProjectId(), devClusterId);
        assertTrue(response.isSuccess());
    }

    @Test
    public void testGetConfigMap() throws Exception {
        ActionReturnUtil response = configCenterService.saveConfig(configDetail,adminUserName);
        assertTrue(response.isSuccess());
        JSONObject jsonObject = (JSONObject)response.getData();
        String tag = jsonObject.get("tag").toString();
        ActionReturnUtil configMap = configCenterService.getLatestConfigMap(testConfigName,projectId,testRepoName,devClusterId,tag);
        assertTrue(configMap.isSuccess());
        assertNotNull(configMap.getData());
        ConfigDetailDto data = (ConfigDetailDto) configMap.getData();
        ActionReturnUtil config = configCenterService.getConfigMap(data.getId());
        assertTrue(config.isSuccess());
        assertNotNull(config.getData());
    }

    @Test
    public void testGetLatestConfigMap() throws Exception {
        ActionReturnUtil response = configCenterService.saveConfig(configDetail,adminUserName);
        assertTrue(response.isSuccess());
        JSONObject jsonObject = (JSONObject)response.getData();
        String tag = jsonObject.get("tag").toString();
        ActionReturnUtil configMap = configCenterService.getLatestConfigMap(testConfigName,projectId,testRepoName,devClusterId,tag);
        assertTrue(configMap.isSuccess());
        assertNotNull(configMap.getData());
    }


    @Test
    public void testDeleteByClusterId() throws Exception {
        configCenterService.deleteByClusterId(devClusterId);
    }


    @Test
    public void testGetConfigByNameAndTag() throws Exception {
        ActionReturnUtil response = configCenterService.saveConfig(configDetail,adminUserName);
        assertTrue(response.isSuccess());
        JSONObject jsonObject = (JSONObject)response.getData();
        String tag = jsonObject.get("tag").toString();
        ConfigFile configFile = configCenterService.getConfigByNameAndTag(testConfigName, tag, configDetail.getProjectId(), devClusterId);
        assertNotNull(configFile);
    }

    @Test
    public void testGetConfigMapByName() throws Exception {
        ActionReturnUtil configMapByName = configCenterService.getConfigMapByName(testConfigName, devClusterId, configDetail.getProjectId());
        assertTrue(configMapByName.isSuccess());
        assertNotNull(configMapByName.getData());
    }




}
