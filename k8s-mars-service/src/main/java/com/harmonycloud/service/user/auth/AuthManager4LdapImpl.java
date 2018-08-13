package com.harmonycloud.service.user.auth;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.naming.Name;


import com.harmonycloud.dao.user.UserMapper;
import com.harmonycloud.dto.user.LdapConfigDto;
import com.harmonycloud.service.user.AuthManager4Ldap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.dao.user.AuthUserMapper;
import com.harmonycloud.dao.user.bean.AuthUser;
import com.harmonycloud.dao.user.bean.AuthUserExample;
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


    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuthUserMapper authUserMapper;

    private String searchType;

    private String object_class;

    @Override
    public String auth(String userName, String password, LdapConfigDto ldapConfigDto) throws Exception {
        // ladp 认证
        if (searchType == null || userName == null || password == null || object_class == null) {
            throw new RuntimeException();
        }
        if (Objects.equals("admin", userName)) {
            return "admin";
        }
        if (!this.isUserInLdap(userName, password, ldapConfigDto)) {
            return null;
        }
        // 对ldap认证通过的用户,判断是否已经记录,如果已记录并且已修改,修改记录,并且更新Harbor
        AuthUserExample example = new AuthUserExample();
        example.createCriteria().andNameEqualTo(userName);
        List<AuthUser> isFound = this.authUserMapper.selectByExample(example);
        if (isFound == null || isFound.size() == 0) {
            return insertUser(userName, password);
        }
        if (isFound != null) {
            if (!isFound.get(0).getPassword().equals(password)) {
                userService.updateLdapUser(userName, password);
            }
        }
        return userName;
    }

    private boolean isUserInLdap(String userName, String password, LdapConfigDto ldapConfigDto) {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://"+ldapConfigDto.getIp()+":"+ldapConfigDto.getPort()+"");
        contextSource.setBase(ldapConfigDto.getBase());
        contextSource.setUserDn(ldapConfigDto.getUserdn());
        contextSource.setPassword(ldapConfigDto.getPassword());

        try {
            contextSource.afterPropertiesSet();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        LdapTemplate template = new LdapTemplate();

        template.setContextSource(contextSource);
        return template.authenticate(getDnForUser(userName,contextSource), "(objectclass="+object_class+")", password);
    }

    // 插入Harbor用户
    private String insertUser(String userName, String password) throws Exception {
        String md5Password = StringUtil.convertToMD5(password);
        User user = new User();
        user.setPassword(md5Password);
        user.setCreateTime(new Date());
        userMapper.insert(user);
        userService.addLdapUser(userName, password, null);
        return userName;
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
    private String getDnForUser(String cn,LdapContextSource contextSource) {
        LdapTemplate template = new LdapTemplate();
        template.setContextSource(contextSource);
        List<String> results = template.search("", "(&(objectclass="+object_class+")("+searchType+"=" + cn + "))", new DnMapper());

        if (results.size() != 1) {

            throw new RuntimeException("User not found or not unique");
        }
        return results.get(0);
    }

    /**
     * 节点的 Dn映射
     */
    class DnMapper implements ContextMapper {
        @Override
        public String mapFromContext(Object ctx) {
            DirContextAdapter context = (DirContextAdapter) ctx;
            Name name = context.getDn();
            String dn = name.toString();
            return dn;
        }
    }
}
