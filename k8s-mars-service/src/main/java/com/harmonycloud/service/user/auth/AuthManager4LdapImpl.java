package com.harmonycloud.service.user.auth;

import java.util.*;
import java.util.stream.Collectors;

import javax.naming.Name;
import javax.naming.directory.Attributes;


import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.dto.user.LdapConfigDto;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.harbor.HarborUser;
import com.harmonycloud.service.platform.service.harbor.HarborUserService;
import com.harmonycloud.service.user.AuthManager4Ldap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.service.user.UserService;


/**
 * @Title AuthManager4LdapImpl.java
 * @author yj
 * @date 2017年5月3日
 * @Description ldap方式实现认证接口
 * @version V1.0
 */
@Service
public class AuthManager4LdapImpl implements AuthManager4Ldap {

    private static Logger LOGGER = LoggerFactory.getLogger(AuthManager4LdapImpl.class);
    
    private static final String LDAP_MAIL = "mail";
    private static final String LDAP_REAL_NAME = "displayname";
    private static final String LDAP_MOBILE = "mobile";

    @Autowired
    private UserService userService;

    @Autowired
    HarborUserService harborUserService;

    @Autowired
    ClusterService clusterService;

    private String searchType;

    private String object_class;

    @Override
    public String auth(String userName, String password, LdapConfigDto ldapConfigDto) throws Exception {
        AssertUtil.notBlank(userName, DictEnum.USERNAME);
        AssertUtil.notBlank(password, DictEnum.PASSWORD);
        if(StringUtils.isBlank(ldapConfigDto.getObjectClass())){
            ldapConfigDto.setObjectClass(object_class);
        }
        if(StringUtils.isBlank(ldapConfigDto.getSearchAttribute())){
            ldapConfigDto.setSearchAttribute(searchType);
        }
        Map<String,String> userAttributes = this.getUserFromLdap(userName, password, ldapConfigDto);
        if (userAttributes == null) {
            return null;
        }
        // 对ldap认证通过的用户,判断是否已经记录,如果没有，则记录用户
        saveUserInfo(userName, password, userAttributes);
        return userName;
    }

    private Map<String,String> getUserFromLdap(String userName, String password, LdapConfigDto ldapConfigDto) throws Exception{
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://"+ldapConfigDto.getIp()+":"+ldapConfigDto.getPort()+"");
        contextSource.setBase(ldapConfigDto.getBase());
        contextSource.setUserDn(ldapConfigDto.getUserdn());
        contextSource.setPassword(ldapConfigDto.getPassword());
        contextSource.afterPropertiesSet();
        LdapTemplate template = new LdapTemplate();
        template.setContextSource(contextSource);
        Map<String,String> userAttribute = getUserAttribute(userName,contextSource,ldapConfigDto);
        boolean authenticated = template.authenticate(userAttribute.get("dn"),
                "(objectclass="+ldapConfigDto.getObjectClass()+")", password);
        if(authenticated){
            return userAttribute;
        }
        return null;
    }

    /**
     * ldap验证通过保存或更新用户信息
     * @param userName
     * @param password
     * @param userAttributes
     * @throws Exception
     */
    private void saveUserInfo(String userName, String password,Map<String,String> userAttributes) throws Exception {
        User user = userService.getUser(userName);
        if (user == null) {
            user = new User();
            user.setUsername(userName);
            user.setPassword(StringUtil.convertToMD5(password));
            user.setEmail(userAttributes.get(LDAP_MAIL));
            user.setPhone(userAttributes.get(LDAP_MOBILE));
            user.setRealName(userAttributes.get(LDAP_REAL_NAME) == null? userName : userAttributes.get(LDAP_REAL_NAME));
            userService.insertUser(user);
            //ldap首次登录云平台，需要向harbor创建用户（harbor使用ldap登录）
            loginHarbor(userName, password);
        }
        boolean userInfoChanged = false;
        if(StringUtils.isNotBlank(userAttributes.get(LDAP_MAIL)) && !userAttributes.get(LDAP_MAIL).equals(user.getEmail())){
            user.setEmail(userAttributes.get(LDAP_MAIL));
            userInfoChanged = true;
        }
        if(StringUtils.isNotBlank(userAttributes.get(LDAP_MOBILE)) && !userAttributes.get(LDAP_MOBILE).equals(user.getPhone())){
            user.setPhone(userAttributes.get(LDAP_MOBILE));
            userInfoChanged = true;
        }
        if(StringUtils.isNotBlank(userAttributes.get(LDAP_REAL_NAME)) && !userAttributes.get(LDAP_REAL_NAME).equals(user.getRealName())){
            user.setRealName(userAttributes.get(LDAP_REAL_NAME));
            userInfoChanged = true;
        }
        if(userInfoChanged) {
            user.setUpdateTime(new Date());
            userService.updateUser(user);
        }
    }

    private void loginHarbor(String userName, String password) throws Exception{
        //向harbor中同步用户
        Set<HarborServer> harborServers = clusterService.listAllHarbors();
        List<HarborServer> harborServerList = harborServers.stream().collect(Collectors.toList());
        for (HarborServer harborServer :harborServerList) {
            HarborUser harborUser = harborUserService.getUserByName(harborServer,userName);
            if(harborUser == null){
                User user = new User();
                user.setUsername(userName);
                user.setPassword(password);
                harborUserService.harborUserLogin(harborServer, user);
            }
        }
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getObject_class() {
        return object_class;
    }

    public void setObject_class(String object_class) {
        this.object_class = object_class;
    }

    @SuppressWarnings({"unused", "unchecked"})
    private Map<String,String> getUserAttribute(String cn,LdapContextSource contextSource, LdapConfigDto ldapConfigDto) {
        LdapTemplate template = new LdapTemplate();
        template.setContextSource(contextSource);
        List<Map<String,String>> results = template.search("", "(&(objectclass="+ldapConfigDto.getObjectClass()+")("
                +ldapConfigDto.getSearchAttribute()+"=" + cn + "))", new DnMapper());

        if (CollectionUtils.isEmpty(results) || results.size() != 1) {
            throw new RuntimeException("User not found or not unique");
        }
        return results.get(0);
    }

    /**
     * 节点的 Dn映射
     */
    class DnMapper implements ContextMapper {
        @Override
        public Map<String,String> mapFromContext(Object ctx) {
            Map<String,String> result = new HashMap<>();
            DirContextAdapter context = (DirContextAdapter) ctx;
            Name name = context.getDn();
            result.put("dn",name.toString());
            try {
                Attributes attributes = context.getAttributes();
                if(attributes.get(LDAP_MAIL) != null) {
                    result.put(LDAP_MAIL, attributes.get(LDAP_MAIL).get().toString());
                }
                if(attributes.get(LDAP_MOBILE) != null) {
                    result.put(LDAP_MOBILE, attributes.get(LDAP_MOBILE).get().toString());
                }
                if(attributes.get(LDAP_REAL_NAME) != null) {
                    result.put(LDAP_REAL_NAME, attributes.get(LDAP_REAL_NAME).get().toString());
                }
            }catch (Exception e){
                LOGGER.error("获取用户信息错误，",e);
            }
            return result;
        }
    }
}
