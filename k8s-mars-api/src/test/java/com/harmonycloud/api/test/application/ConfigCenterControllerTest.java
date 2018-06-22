package com.harmonycloud.api.test.application;

import com.harmonycloud.api.test.BaseTest;
import com.harmonycloud.common.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by user on 2018/6/13.
 */
public class ConfigCenterControllerTest extends BaseTest {

    protected Logger logger= LoggerFactory.getLogger(ConfigCenterControllerTest.class);

    @Test
    public void testSaveConfigMap() throws Exception {
        String url = "/tenants/103303bb68ea4511abce4b5da0c054f4/projects/aabc0a6f31d543e6a27f6042cddd91ad/configmap";

        String result = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .param("clusterId","cluster-top--dev")
//                .param("repoName",null)
                .param("name","aa")
                .param("configFileItemList[0].content","asas")
                .param("configFileItemList[0].fileName","config1")
//                .param("configFileItemList[0].path",null)
                .param("configFileItemList[1].content","zxzx")
                .param("configFileItemList[1].fileName","config2")
//                .param("configFileItemList[1].path",null)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }

    @Test
    public void testUpdateConfigMap() throws Exception {
        String url = "/tenants/6faf1e76d992450aa111bc700cf3ca4f/projects/ac6d46a2a39b47de9e10a1ec763e95bc/configmap";

        String result = mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON)
                .param("clusterId","cluster-top--dev")
//                .param("repoName",null)
                .param("description","test")
                .param("id","24b0a22cea9146b59b9470a6f3d41a73")
                .param("configFileItemList[0].content","asas22")
                .param("configFileItemList[0].fileName","config12")
//                .param("configFileItemList[0].path",null)
                .param("configFileItemList[1].content","zxzx22")
                .param("configFileItemList[1].fileName","config22")
//                .param("configFileItemList[1].path",null)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }

    @Test
    public void testDeleteConfigMap() throws Exception {
        String url = "/tenants/103303bb68ea4511abce4b5da0c054f4/projects/aabc0a6f31d543e6a27f6042cddd91ad/configmap/336245d83c6d461897d94685497d8053";
        String result = mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }


    @Test
    public void testDeleteConfig() throws Exception {
        String url = "/tenants/103303bb68ea4511abce4b5da0c054f4/projects/aabc0a6f31d543e6a27f6042cddd91ad/configmap";
        String result = mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON)
                .param("name","abc")
                .param("clusterId","cluster-top--dev")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }

    @Test
    public void testGetConfigMap() throws Exception {
        String url = "/tenants/103303bb68ea4511abce4b5da0c054f4/projects/aabc0a6f31d543e6a27f6042cddd91ad/configmap/12c3b356be534b7d992ed57873e766e4";
        String result = mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }

    @Test
    public void testGetLatestConfigMap() throws Exception {
        String url = "/tenants/6faf1e76d992450aa111bc700cf3ca4f/projects/ac6d46a2a39b47de9e10a1ec763e95bc/configmap/latest";
        String result = mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)
                .param("name","config2")
                .param("reponame","onlineshop/tomcat")
                .param("clusterId","cluster-top--dev")
                .param("tags","1.0")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }
    @Test
    public void testSearchConfigMap() throws Exception {
        String url = "/tenants/6faf1e76d992450aa111bc700cf3ca4f/projects/ac6d46a2a39b47de9e10a1ec763e95bc/configmap/search";
        String result = mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)
                .param("clusterId","cluster-top--dev")
                .param("keyword","con")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }

    @Test
    public void testGetConfigMapByName() throws Exception {
        String url = "/tenants/6faf1e76d992450aa111bc700cf3ca4f/projects/ac6d46a2a39b47de9e10a1ec763e95bc/configmap/listTags/config2";
        String result = mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)
                .param("clusterId","cluster-top--dev")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }





}
