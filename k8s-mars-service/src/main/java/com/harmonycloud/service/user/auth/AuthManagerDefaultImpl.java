package com.harmonycloud.service.user.auth;

import java.util.Objects;

import com.harmonycloud.service.user.AuthManagerDefault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.customs.CustomUserMapper;
import com.harmonycloud.service.user.AuthManager;

/**
 * @Title AuthManagerDefaultImpl.java
 * @author yj
 * @date 2017年5月3日
 * @Description 默认认证方式,数据库方式
 * @version V1.0
 */
@Service
public class AuthManagerDefaultImpl implements AuthManagerDefault {

    @Autowired
    private CustomUserMapper userMapper;

    @Override
    public String auth(String userName, String password) throws Exception {

        User user = userMapper.findByUsername(userName);
        if (user == null || !Objects.equals(user.getPassword(), StringUtil.convertToMD5(password))) {
            return null;
        }
        return userName;
    }

}
