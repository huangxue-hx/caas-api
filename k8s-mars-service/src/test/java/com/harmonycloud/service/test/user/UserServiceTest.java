package com.harmonycloud.service.test.user;

import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.service.test.BaseTest;
import com.harmonycloud.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Created by anson on 18/6/28.
 */
public class UserServiceTest extends BaseTest{
    @Autowired
    UserService userService;

    @Test
    public void testListUserByProjectId(){
        List<User> userList = userService.listUserByProjectId(projectId);
        assertNotNull(userList);
    }
}
