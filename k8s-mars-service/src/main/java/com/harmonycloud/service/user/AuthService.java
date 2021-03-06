package com.harmonycloud.service.user;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.harmonycloud.common.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.dao.user.UserMapper;
import com.harmonycloud.dao.user.bean.User;


@Service
public class AuthService {
    @Autowired
    private UserMapper userMapper;
	/**
	 * 根据用户名密码,认证用户, 认证成功:返回用户对象, 认证失败:返回null
	 * 
	 * @param
	 * @return
	 */
	public User AuthUser(String username, String password) throws Exception {
		User authUser = userMapper.findByUsername(username);

		try {
			if (authUser.getPassword().equals(StringUtil.convertToMD5(password))) {
				return authUser;
			} 
			return null;
		} catch (Exception e) {
			throw e;
		}
	}
    /**
     * @param username 用户名
     * @param password 密码
     * @throws Exception
     * 根据用户名密码,认证用户, 认证成功:返回用户对象, 认证失败:返回null
     */
    public User authUser(String username, String password) throws Exception {
        User authUser = userMapper.findByUsername(username);
        try {
            if (authUser.getPassword().equals(StringUtil.convertToMD5(password))) {
                return authUser;
            }
            return null;
        } catch (Exception exception) {
            throw exception;
        }
    }

    /**
     * 生成token,返回用户名和token的map
     * 
     * @param user
     * @return
     * @throws Exception
     */
    public Map<String, Object> generateToken(User user) throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        String token = null;
        // 机器账号和admin的token持久化
        if (1 == user.getIsAdmin() || 1 == user.getIsMachine()) {
            if (user.getToken() != null && org.apache.commons.lang3.StringUtils.isNotBlank(user.getToken())) {
                data.put("token", user.getToken());
                data.put("username", user.getUsername());
                return data;
            }
        }
        token = UUIDUtil.getUUID();
        // 将token存入数据库
        user.setToken(token);
        user.setTokenCreate(new Date());
        user.setId(user.getId());
        userMapper.updateByPrimaryKeySelective(user);

        data.put("token", token);
        data.put("username", user.getUsername());
        return data;
    }

    /**
     * 验证token有效性,token有效返回改用户对象，token无效，返回null;
     * 
     * @param token
     * @return
     * @throws Exception
     */
    public User validateToken(String token) throws Exception {
        // 从数据库中查询token
        User user = userMapper.findUserByToken(token);
        if (user != null) {
            return user;
        }
        return null;
    }
}
