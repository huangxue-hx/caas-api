package com.harmonycloud.api.test.user;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by zhangkui on 2018/6/7.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext-test.xml"})
@Transactional
@WebAppConfiguration
public class UserControllerTest {

        protected Logger logger= LoggerFactory.getLogger(UserControllerTest.class);
        private MockMvc mockMvc;

        @Autowired
        WebApplicationContext wac;

        @Before()
        public void setup() {
            mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        }

        @Test
        public void testAddUser() throws Exception {
            String url = "/users";
            String result = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON)
                    .param("username","testuser")
                    .param("realName","测试用户")
                    .param("password","Ab123456")
                    .param("email","testuser@harmonycloud.cn")
                    .param("phone","13056965485")
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(print()).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            Map resultMap = JsonUtil.convertJsonToMap(result);
            assertTrue((boolean)resultMap.get("success"));
        }
}
