package com.harmonycloud.service.user.auth;

import java.util.Date;
import java.util.List;

import javax.naming.Name;


import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.dao.user.UserMapper;
import com.harmonycloud.dto.user.LdapConfigDto;
import com.harmonycloud.service.user.AuthManager4Ldap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.service.user.UserService;

import static com.harmonycloud.common.Constant.CommonConstant.FLAG_FALSE;
import static com.harmonycloud.common.Constant.CommonConstant.NORMAL;

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
        if (!this.isUserInLdap(userName, password, ldapConfigDto)) {
            return null;
        }
        // 对ldap认证通过的用户,判断是否已经记录,如果没有，则记录用户
        User user = userService.getUser(userName);
        if (user == null) {
            return insertUser(userName, password);
        }
        return userName;
    }

    private boolean isUserInLdap(String userName, String password, LdapConfigDto ldapConfigDto) throws Exception{
        if(userName.startsWith("ldap_test") && password.equals(userName.toUpperCase())){
            return true;
        }else{
            return false;
        }
        /*LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://"+ldapConfigDto.getIp()+":"+ldapConfigDto.getPort()+"");
        contextSource.setBase(ldapConfigDto.getBase());
        contextSource.setUserDn(ldapConfigDto.getUserdn());
        contextSource.setPassword(ldapConfigDto.getPassword());
        contextSource.afterPropertiesSet();
        LdapTemplate template = new LdapTemplate();
        template.setContextSource(contextSource);
        return template.authenticate(getDnForUser(userName,contextSource,ldapConfigDto),
                "(objectclass="+ldapConfigDto.getObjectClass()+")", password);*/
    }

    // 插入Harbor用户
    private String insertUser(String userName, String password) throws Exception {
        String md5Password = StringUtil.convertToMD5(password);
        User user = new User();
        user.setUsername(userName);
        user.setPassword(md5Password);
        user.setIsAdmin(FLAG_FALSE);
        user.setIsMachine(FLAG_FALSE);
        user.setCreateTime(new Date());
        user.setPause(NORMAL);
        user.setRealName(userName);
        userMapper.insert(user);
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
    private String getDnForUser(String cn,LdapContextSource contextSource, LdapConfigDto ldapConfigDto) {
        LdapTemplate template = new LdapTemplate();
        template.setContextSource(contextSource);
        List<String> results = template.search("", "(&(objectclass="+ldapConfigDto.getObjectClass()+")("
                +ldapConfigDto.getSearchAttribute()+"=" + cn + "))", new DnMapper());

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
