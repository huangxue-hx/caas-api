package com.harmonycloud.api.test.tenant;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.api.test.BaseTest;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dto.tenant.TenantDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author xc
 * @date 2018/7/3 15:02
 */
public class TenantControllerTest extends BaseTest {

    private Logger LOGGER = LoggerFactory.getLogger(TenantControllerTest.class);

    private static TenantDto tenantDto;

    private String tenantId;

    @BeforeClass
    public void createTenantData() {
        tenantDto = new TenantDto();
        tenantDto.setTenantName("test-xc");
        tenantDto.setAliasName("test-xc");
        List<String> tmList = new ArrayList<>();
        tmList.add(pmUsername);
        tenantDto.setTmList(tmList);
    }

    @Test
    public void tenantTest() throws Exception {
        //创建租户
        String url = "/tenants";
        String result = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("tenantName",tenantDto.getTenantName())
                .param("aliasName", tenantDto.getAliasName())
                .param("tmList[0]", tenantDto.getTmList().get(0))
                .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));

        //查询租户列表
        url = "/tenants";
        result = mockMvc.perform(get(url).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", pmUsername)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        resultMap = JsonUtil.convertJsonToMap(result);
        List<TenantDto> tenantDtoList = JSONObject.parseArray(JsonUtil.convertToJson(resultMap.get("data")), TenantDto.class);
        int includePlatformSize = 0;
        if(tenantDtoList != null){
            includePlatformSize = tenantDtoList.size();
            for (TenantDto tenantDto1 : tenantDtoList) {
                if (tenantDto.getAliasName().equals(tenantDto1.getAliasName())) {
                    tenantId = tenantDto1.getTenantId();
                    break;
                }
            }
        }
        LOGGER.info("include platform size:{}", includePlatformSize);
        assertTrue(includePlatformSize > 0);

        //删除租户
        url = "/tenants/" + tenantId;
        result = mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }

}
