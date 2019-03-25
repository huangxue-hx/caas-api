package com.harmonycloud.api.test.cluster;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.api.test.BaseTest;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by zhangkui on 2018/6/7.
 */
public class ClusterControllerTest  extends BaseTest {

        protected Logger logger= LoggerFactory.getLogger(ClusterControllerTest.class);

        @Test
        public void testListCluster() throws Exception {
            String url = "/clusters";
            String result = mockMvc.perform(get(url).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("includePlatformCluster","true").accept(MediaType.APPLICATION_JSON))
                    .andDo(print()).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            Map resultMap = JsonUtil.convertJsonToMap(result);
            List<Cluster> clusterList = JSONObject.parseArray(JsonUtil.convertToJson(resultMap.get("data")), Cluster.class);
            int includePlatformSize = clusterList.size();
            logger.info("include platform size:{}", includePlatformSize);
            assertTrue(includePlatformSize > 0);

            result = mockMvc.perform(get(url).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("includePlatformCluster","false").accept(MediaType.APPLICATION_JSON))
                    .andDo(print()).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            resultMap = JsonUtil.convertJsonToMap(result);
            clusterList = JSONObject.parseArray(JsonUtil.convertToJson(resultMap.get("data")), Cluster.class);
            int size = clusterList.size();
            assertTrue(includePlatformSize > size);
        }
}
