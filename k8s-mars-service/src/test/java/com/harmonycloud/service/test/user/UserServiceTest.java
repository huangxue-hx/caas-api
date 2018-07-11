package com.harmonycloud.service.test.user;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.tenant.show.UserShowDto;
import com.harmonycloud.dto.user.UserQueryDto;
import com.harmonycloud.service.test.BaseTest;
import com.harmonycloud.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testListUsers() throws Exception{
        UserQueryDto userQueryDto = new UserQueryDto();
        ActionReturnUtil response = userService.listUsers(userQueryDto);
        assertTrue(response.isSuccess());
        List<UserShowDto> userShowDtos = (List)response.getData();
        assertTrue(userShowDtos.size() > 0);
        userQueryDto.setIsMachine(Boolean.TRUE);
        response = userService.listUsers(userQueryDto);
        assertTrue(response.isSuccess());
        userShowDtos = (List)response.getData();
        assertTrue(userShowDtos.size() == 1);
        userQueryDto = new UserQueryDto();
        userQueryDto.setIsAdmin(Boolean.TRUE);
        response = userService.listUsers(userQueryDto);
        assertTrue(response.isSuccess());
        userShowDtos = (List)response.getData();
        assertTrue(userShowDtos.size() > 0);
        userQueryDto = new UserQueryDto();
        String userIds = "1,2,3,4,5,6,7,8,9,10";
        userQueryDto.setUserIds(userIds);
        response = userService.listUsers(userQueryDto);
        assertTrue(response.isSuccess());
        userShowDtos = (List)response.getData();
        assertTrue(userShowDtos.size() > 0);

    }
}
