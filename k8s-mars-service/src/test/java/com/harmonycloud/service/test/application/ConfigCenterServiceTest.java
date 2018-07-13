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

   /* @Test
    public void testDeleteConfig() throws Exception {
        configCenterService.deleteConfig("13e54a9ddcaa41d9ae860a4ec6cfe558","1");
    }

    @Test
    public void testDeleteConfigByProject() throws Exception {
        configCenterService.deleteConfigByProject(projectId);
    }

    @Test
    public void testDeleteConfigMap() throws Exception {
        configCenterService.deleteConfigMap("az","aabc0a6f31d543e6a27f6042cddd91ad",devClusterId);
    }
    @Test
    public void testGetConfigMap() throws Exception {
        ActionReturnUtil configMap = configCenterService.getConfigMap("05d7e9f4769642c2aaa73f1741168495");
        String toJSONString = JSONObject.toJSONString(configMap.getData());
        System.out.println(toJSONString);
    }
    @Test
    public void testGetLatestConfigMap() throws Exception {
        ActionReturnUtil configMap = configCenterService.getLatestConfigMap("asas","aabc0a6f31d543e6a27f6042cddd91ad","library/jftomcat",devClusterId,"1.1");
        String toJSONString = JSONObject.toJSONString(configMap.getData());
        System.out.println(toJSONString);
    }
    @Test
    public void testDeleteByClusterId() throws Exception {
        configCenterService.deleteByClusterId(devClusterId);
    }
    @Test
    public void testGetConfigByNameAndTag() throws Exception {
        ConfigFile configFile = configCenterService.getConfigByNameAndTag("test", "1.0", "136ce5cb971948d48a29432209fc8533", devClusterId);
        System.out.println(JSONObject.toJSONString(configFile));
    }
    @Test
    public void testGetConfigMapByName() throws Exception {
        ActionReturnUtil configMapByName = configCenterService.getConfigMapByName("asas", devClusterId, "aabc0a6f31d543e6a27f6042cddd91ad");
        System.out.println(JSONObject.toJSONString(configMapByName));
    }*/




}
