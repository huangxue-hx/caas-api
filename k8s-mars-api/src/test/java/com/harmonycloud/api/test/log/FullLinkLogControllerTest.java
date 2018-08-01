package com.harmonycloud.api.test.log;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.api.test.BaseTest;
import com.harmonycloud.common.util.JsonUtil;

import com.harmonycloud.dto.log.FullLinkQueryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;


import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by zhangkui on 2018/6/7.
 */
public class FullLinkLogControllerTest extends BaseTest {

    protected Logger logger= LoggerFactory.getLogger(FullLinkLogControllerTest.class);

    @Test
    public void testGetPod() throws Exception {
        String url = "/tenants/9297e547411948cbb313a2ddfbc7d0db/projects/a98eac503a7c4ee4a22fa2d2d96a27ae/apps/jeeshop/linklogs/pod";
        String result = mockMvc.perform(get(url)
                .param("deployName","")
                .param("namespace","aby0605-test")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
        result = mockMvc.perform(get(url)
                .param("deployName","jeeshopuser")
                .param("namespace","aby0605-test")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }

    @Test
    public void testGetAnalysis() throws Exception {
        String url = "/tenants/9297e547411948cbb313a2ddfbc7d0db/projects/a98eac503a7c4ee4a22fa2d2d96a27ae/apps/jeeshop/linklogs/erroranalysis";
        String result = mockMvc.perform(get(url)
                .param("fromTime","2018-07-18 12:56:26")
                .param("toTime","2018-07-24 12:56:26")
                .param("namespace","aby0605-test")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }

    @Test
    public void testGetErrorTransaction() throws Exception {
        String url = "/tenants/9297e547411948cbb313a2ddfbc7d0db/projects/a98eac503a7c4ee4a22fa2d2d96a27ae/apps/jeeshop/linklogs/errortransactions";
        String result = mockMvc.perform(get(url)
                .param("fromTime","2018-07-18 12:56:26")
                .param("toTime","2018-07-24 12:56:26")
                .param("namespace","aby0605-test")
                .param("serverUrl","/jeeshopclient/")
                .param("order","desc")
                .param("orderedField","interval")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }

    @Test
    public void testGetTransactionTrace() throws Exception {
        String url = "/tenants/9297e547411948cbb313a2ddfbc7d0db/projects/a98eac503a7c4ee4a22fa2d2d96a27ae/apps/jeeshop/linklogs/transactiontraces/65e62bd702a6040c99b50db592d7a86a1^2";
        String result = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }

    @Test
    public void testGetLinkLogInfo() throws Exception {
        String url = "/tenants/9297e547411948cbb313a2ddfbc7d0db/projects/a98eac503a7c4ee4a22fa2d2d96a27ae/apps/jeeshop/linklogs";
        String result = mockMvc.perform(get(url)
                .param("deployName","webapi")
                .param("namespace","kube-system")
                .param("clusterId","cluster-top--top")
                .param("transactionId","2220e31952a284cdbb4ef49534934851d^19482")
                .param("pod","webapi-589797488b-8hdlk")
                .param("fromTime","2018-07-31 10:50:44")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map resultMap = JsonUtil.convertJsonToMap(result);
        assertTrue((boolean)resultMap.get("success"));
    }
}
