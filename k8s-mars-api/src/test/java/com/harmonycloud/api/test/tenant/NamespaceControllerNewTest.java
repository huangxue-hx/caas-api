package com.harmonycloud.api.test.tenant;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dto.tenant.TenantDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author xc
 * @date 2018/7/4 0:49
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext-test.xml"})
@WebAppConfiguration
public class NamespaceControllerNewTest {

    private Logger LOGGER= LoggerFactory.getLogger(NamespaceControllerNewTest.class);
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Before()
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void createNamespace() throws Exception {
        String url = "/tenants/0c2ae6230a6643bd857a5da19c19ecb3/namespaces";
        String result = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "test-xc-abc")
                .param("aliasName", "abc")
                .param("clusterId", "cluster-top--dev")
                .param("tenantId","0c2ae6230a6643bd857a5da19c19ecb3")
                .param("quota.cpu","0.1")
                .param("quota.memory","0.1Gi")
                .param("storageClassQuotaList[0].name","xc-test2")
                .param("storageClassQuotaList[0].quota","2")
                .param("storageClassQuotaList[0].totalQuota","2")
                .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }

//    @Test
//    public void listTenant() throws Exception {
//        String url = "/tenants";
//        String result = mockMvc.perform(get(url).contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                .param("username", "xuchao")
//                .accept(MediaType.APPLICATION_JSON))
//                .andDo(print()).andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//        Map resultMap = JsonUtil.convertJsonToMap(result);
//        List<TenantDto> tenantDtoList = JSONObject.parseArray(JsonUtil.convertToJson(resultMap.get("data")), TenantDto.class);
//        int includePlatformSize = 0;
//        if(tenantDtoList != null){
//            includePlatformSize = tenantDtoList.size();
//            for (TenantDto tenantDto1 : tenantDtoList) {
//                if ("test-xc".equals(tenantDto1.getAliasName())) {
//                    tenantId = tenantDto1.getTenantId();
//                    break;
//                }
//            }
//        } else {
//            LOGGER.error("系统中无任何租户");
//        }
//        LOGGER.info("include platform size:{}", includePlatformSize);
//        assertTrue(includePlatformSize > 0);
//    }
//
//    @Test
//    public void createClusterQuota() throws Exception {
//        String url = "/tenants/0c2ae6230a6643bd857a5da19c19ecb3";
//        String result = mockMvc.perform(put(url).contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                .param("tenantName", "test-xc")
//                .param("aliasName", "test-xc")
//                .param("tenantId", "0c2ae6230a6643bd857a5da19c19ecb3")
//                .param("clusterQuota[0].clusterName","dev")
//                .param("clusterQuota[0].clusterAliasName","开发集群")
//                .param("clusterQuota[0].clusterId","cluster-top--dev")
//                .param("clusterQuota[0].id","96")
//                .param("clusterQuota[0].memoryUsed","0")
//                .param("clusterQuota[0].memoryUsable","6")
//                .param("clusterQuota[0].memoryQuota","102.4")
//                .param("clusterQuota[0].memoryQuotaType","MB")
//                .param("clusterQuota[0].cpuUsed","0")
//                .param("clusterQuota[0].cpuUsable","1.7")
//                .param("clusterQuota[0].cpuQuota","0.1")
//                .param("clusterQuota[0].cpuQuotaType","Core")
//                .param("clusterQuota[0].memoryUsableMax","6.0")
//                .param("clusterQuota[0].cpuUsableMax","1.7")
//                .param("clusterQuota[0].storageQuota[0].name","xc-test2")
//                .param("clusterQuota[0].storageQuota[0].storageQuota","2")
//                .param("clusterQuota[0].storageQuota[0].totalStorage","2")
//                .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//        Map resultMap = JsonUtil.convertJsonToMap(result);
//        assertTrue((boolean)resultMap.get("success"));
//    }
}
