package com.harmonycloud.service.user;

import com.harmonycloud.dto.user.LdapConfigDto;

/**
 * 
 * @Title AuthManager.java
 * @author yj
 * @date 2017年5月3日
 * @Description 认证方式接口,如果需要扩展其他认证方式，需要实现该接口,并且配置在applicationContext.xml中配置认证方式
 * @version V1.0
 */
public interface AuthManager4Ldap {
    String auth(String userName, String password, LdapConfigDto ldapConfigDto) throws Exception;
}
