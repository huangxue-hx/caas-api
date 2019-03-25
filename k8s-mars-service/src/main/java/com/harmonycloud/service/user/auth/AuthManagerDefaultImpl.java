package com.harmonycloud.service.user.auth;

import java.util.Objects;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.user.UserMapper;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.user.AuthManagerDefault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.dao.user.bean.User;
import org.springframework.util.StringUtils;

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
    private UserMapper userMapper;
    @Autowired
    private SystemConfigService systemConfigService;
    @Override
    public String auth(String userName, String password) throws Exception {

        User user = userMapper.findByUsername(userName);
        if (user == null){//用户不存在
            return null;
        }

        int loginFailTimeLimit = Integer.valueOf(systemConfigService.
                findConfigValueByName("loginFailTimeLimit"));//锁定时间限制    如:30分钟
        int loginFailCountLimit = Integer.valueOf(systemConfigService.
                findConfigValueByName("loginFailCountLimit"));//失败次数限制   如:10次
        int SingleTimeLimit = Integer.valueOf(systemConfigService.
                findConfigValueByName("SingleTimeLimit"));//单位时间限制  如:1分钟内
        int loginFailCount = user.getLoginFailCount();  //失败次数
        String loginFailTime = user.getLoginFailTime();   //上次失败时间
        long diff = loginFailTimeLimit;//时间差 默认锁定时间限制
        if(!StringUtils.isEmpty(loginFailTime)){
            diff = (System.currentTimeMillis() - Long.valueOf( loginFailTime))/ 1000 ;
        }
        if (!Objects.equals(user.getPassword(), StringUtil.convertToMD5(password))) { //密码错误
            if(loginFailCount >= loginFailCountLimit && diff < loginFailTimeLimit){
                throw new MarsRuntimeException(ErrorCodeMessage.USER_LOCKED);
            }
            if(diff > SingleTimeLimit){//如果超过单位时间重新计算
                loginFailTime = String.valueOf(System.currentTimeMillis());
                loginFailCount=0;
            }else{//单位时间内累计
                loginFailTime = String.valueOf(System.currentTimeMillis());
                loginFailCount += 1;
            }
            user.setLoginFailCount(loginFailCount);
            user.setLoginFailTime(loginFailTime);
            userMapper.updateByPrimaryKey(user);//更新
            return null;
        }else{//密码正确
            if(loginFailCount >= loginFailCountLimit && diff < loginFailTimeLimit){
                throw new MarsRuntimeException(ErrorCodeMessage.USER_LOCKED);
            }
            loginFailTime = "";
            loginFailCount=0;
            user.setLoginFailCount(loginFailCount);
            user.setLoginFailTime(loginFailTime);
            userMapper.updateByPrimaryKey(user);//更新
        }
        return userName;
    }
}
