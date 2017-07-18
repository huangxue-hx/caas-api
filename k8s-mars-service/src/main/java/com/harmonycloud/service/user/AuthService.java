package com.harmonycloud.service.user;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.dao.user.UserMapper;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.customs.CustomUserMapper;


@Service
@Transactional(rollbackFor = Exception.class)
public class AuthService {
    @Autowired
    private CustomUserMapper userMapper;
    @Autowired
    private UserMapper userMapperNew;
	/**
	 * 根据用户名密码,认证用户, 认证成功:返回用户对象, 认证失败:返回null
	 * 
	 * @param user
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
        // 通过uuid生成token
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        // 去掉"-"符号
        token = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);
        // 将token存入数据库
        user.setToken(token);
        user.setTokenCreate(new Date());
        userMapperNew.updateByPrimaryKeySelective(user);
//        userMapper.updateUser(user);

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
