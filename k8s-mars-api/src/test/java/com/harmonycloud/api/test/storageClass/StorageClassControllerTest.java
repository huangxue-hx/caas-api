package com.harmonycloud.api.test.storageClass;

import com.harmonycloud.common.util.JsonUtil;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by lenovo on 2018/6/19.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext-test.xml"})
@Transactional
@WebAppConfiguration
public class StorageClassControllerTest {
    protected Logger logger= LoggerFactory.getLogger(StorageClassControllerTest.class);
    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    @Before()
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testStorageClassQuotaLimit() throws  Exception{
        String url = "/tenants/c3073d4b19c0429aa4cd0ae18d099f2a/namespaces";
        String result = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name","gxd-yy01211")
                .param("aliasName","测试别名")
                .param("clusterId","cluster-top--dev")
                .param("tenantId","c3073d4b19c0429aa4cd0ae18d099f2a")
                .param("quota.cpu","0.2")
                .param("quota.memory","0.1Gi")
                .param("storageClassDtos[0].name","storage001")
                .param("storageClassDtos[0].quotaLimit","2")
                .param("storageClassDtos[1].name","storage002")
                .param("storageClassDtos[1].quotaLimit","3")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }
}
