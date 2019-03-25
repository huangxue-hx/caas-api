package com.harmonycloud.api.test.user;

import com.harmonycloud.api.test.BaseTest;
import com.harmonycloud.common.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by zhangkui on 2018/6/7.
 */

public class UserControllerTest extends BaseTest{

        protected Logger logger= LoggerFactory.getLogger(UserControllerTest.class);

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
