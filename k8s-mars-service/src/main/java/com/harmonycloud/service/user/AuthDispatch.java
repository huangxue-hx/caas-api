package com.harmonycloud.service.user;

/**
 * 
 * @Title AuthDispatch.java
 * @author yj
 * @date 2017年5月3日
 * @Description 认证转发器,转发到具体认证实现类
 * @version V1.0
 */
public interface AuthDispatch {
    String login(String userName, String password) throws Exception;
}
