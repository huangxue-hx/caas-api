package com.harmonycloud.service.test.application;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.application.bean.ConfigFile;
import com.harmonycloud.dao.application.bean.ConfigFileItem;
import com.harmonycloud.dto.config.ConfigDetailDto;
import com.harmonycloud.service.platform.service.ConfigCenterService;
import com.harmonycloud.service.test.JUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zqf on 2018/6/13.
 */
@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
@WebAppConfiguration
public class ConfigCenterServiceTest {
    protected Logger logger= LoggerFactory.getLogger(ConfigCenterServiceTest.class);

    @Autowired
    ConfigCenterService configCenterService;

    @Test
    public void testSaveConfig() throws Exception {

        ConfigDetailDto configDetail = new ConfigDetailDto();
        configDetail.setClusterId("cluster-top--dev");
        configDetail.setProjectId("1");
        configDetail.setRepoName(null);
        configDetail.setTenantId("1");
        configDetail.setName("aa");
        List<ConfigFileItem> configFileItemList = new ArrayList<>();
        ConfigFileItem configFileItem = new ConfigFileItem();
        configFileItem.setContent("asdasd");
        configFileItem.setFileName("config1");
        configFileItem.setPath(null);
        ConfigFileItem configFileItem1 = new ConfigFileItem();
        configFileItem1.setContent("1212");
        configFileItem1.setFileName("config2");
        configFileItem1.setPath(null);
        configFileItemList.add(0,configFileItem);
        configFileItemList.add(1,configFileItem1);
        configDetail.setConfigFileItemList(configFileItemList);
        String username = "admin";
        configCenterService.saveConfig(configDetail,username);
    }


    @Test
    public void testUpdateConfig() throws Exception {
        ConfigDetailDto configDetail = new ConfigDetailDto();
        configDetail.setClusterId("cluster-top--dev");
        configDetail.setProjectId("12");
        configDetail.setRepoName(null);
        configDetail.setTenantId("12");
        configDetail.setName("aa12");
        configDetail.setId("2cf1acbea11e41f3bf02ff181e9e5f4f");
        List<ConfigFileItem> configFileItemList = new ArrayList<>();
        ConfigFileItem configFileItem = new ConfigFileItem();
        configFileItem.setContent("asdasd12");
        configFileItem.setFileName("config12");
        configFileItem.setPath(null);
        ConfigFileItem configFileItem1 = new ConfigFileItem();
        configFileItem1.setContent("1212");
        configFileItem1.setFileName("config22");
        configFileItem1.setPath(null);
        configFileItemList.add(0,configFileItem);
        configFileItemList.add(1,configFileItem1);
        configDetail.setConfigFileItemList(configFileItemList);
        String username = "admin12";
        configCenterService.updateConfig(configDetail,username);
    }

    @Test
    public void testDeleteConfig() throws Exception {
        configCenterService.deleteConfig("2cf1acbea11e41f3bf02ff181e9e5f4f","1");
    }

    @Test
    public void testDeleteConfigByProject() throws Exception {
        configCenterService.deleteConfigByProject("df6c41577c214590bdbaff088766aa36");
    }

    @Test
    public void testDeleteConfigMap() throws Exception {
        configCenterService.deleteConfigMap("az","aabc0a6f31d543e6a27f6042cddd91ad","cluster-top--dev");
    }
    @Test
    public void testGetConfigMap() throws Exception {
        ActionReturnUtil configMap = configCenterService.getConfigMap("700d3d176a9c4288817dab2a97f22f2b");
        String toJSONString = JSONObject.toJSONString(configMap.getData());
        System.out.println(toJSONString);
    }
    @Test
    public void testGetLatestConfigMap() throws Exception {
        ActionReturnUtil configMap = configCenterService.getLatestConfigMap("config2","ac6d46a2a39b47de9e10a1ec763e95bc","onlineshop/tomcat","cluster-top--dev","1.0");
        String toJSONString = JSONObject.toJSONString(configMap.getData());
        System.out.println(toJSONString);
    }
    @Test
    public void testSearchConfig() throws Exception {
        ActionReturnUtil configMap = configCenterService.searchConfig("ac6d46a2a39b47de9e10a1ec763e95bc","cluster-top--dev","onlineshop/tomcat","con");
        String toJSONString = JSONObject.toJSONString(configMap.getData());
        System.out.println(toJSONString);
    }
    @Test
    public void testDeleteByClusterId() throws Exception {
        configCenterService.deleteByClusterId("2");
    }
    @Test
    public void testGetConfigByNameAndTag() throws Exception {
        ConfigFile configFile = configCenterService.getConfigByNameAndTag("config2", "1.0", "ac6d46a2a39b47de9e10a1ec763e95bc", "cluster-top--dev");
        System.out.println(JSONObject.toJSONString(configFile));
    }
    @Test
    public void testGetConfigMapByName() throws Exception {
        ActionReturnUtil configMapByName = configCenterService.getConfigMapByName("config1", "cluster-top--dev", "ac6d46a2a39b47de9e10a1ec763e95bc");
        System.out.println(JSONObject.toJSONString(configMapByName));
    }




}
