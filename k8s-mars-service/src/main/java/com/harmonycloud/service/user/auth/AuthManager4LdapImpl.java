package com.harmonycloud.service.user.auth;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import com.harmonycloud.dto.user.LdapConfigDto;
import com.harmonycloud.service.user.AuthManager4Ldap;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.HarborUtil;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.dao.user.AuthUserMapper;
import com.harmonycloud.dao.user.bean.AuthUser;
import com.harmonycloud.dao.user.bean.AuthUserExample;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.customs.CustomUserMapper;
import com.harmonycloud.service.user.AuthManager;
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

    @Value("#{propertiesReader['image.host']}")
    private String harborIp;
    @Value("#{propertiesReader['image.port']}")
    private String harborPort;
    @Value("#{propertiesReader['image.username']}")
    private String harborUser;
    @Value("#{propertiesReader['image.password']}")
    private String harborPassword;
    @Value("#{propertiesReader['image.timeout']}")
    private String harborTimeout;
    @Autowired
    private HarborUtil harborUtil;


    @Autowired
    private UserService userService;

    @Autowired
    private CustomUserMapper userMapper;

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
        if (!this.isUserInLdap(userName, password, ldapConfigDto)) {
            return null;
        }
        if (Objects.equals("admin", userName)) {
            return "admin";
        }
        // 对ldap认证通过的用户,判断是否已经记录,如果已记录并且已修改,修改记录,并且更新Harbor
        AuthUserExample example = new AuthUserExample();
        example.createCriteria().andNameEqualTo(userName);
        List<AuthUser> isFound = this.authUserMapper.selectByExample(example);
        if (isFound == null || isFound.size() == 0) {
            return insertHarborUser(userName, password);
        }
        if (isFound != null) {
            if (!isFound.get(0).getPassword().equals(password)) {
                userService.updateLdapUser(userName, password);
            }
        }
        return userName;
    }

    private boolean isUserInLdap(String userName, String password, LdapConfigDto ldapConfigDto) {
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", object_class)).and(new EqualsFilter(searchType, userName)).and(new EqualsFilter("userPassword", password));
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
        @SuppressWarnings("rawtypes")
        List search = template.search("", filter.encode(), new AttributesMapper() {
            @Override
            public Object mapFromAttributes(Attributes attributes) throws NamingException {
                return attributes;
            }
        });
        if (search.size() == 1) {
            return true;
        }
        return false;
    }

    // 插入Harbor用户
    private String insertHarborUser(String userName, String password) throws Exception {
        final String addUrl = "http://" + harborIp + ":" + harborPort + "/api/users";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("username", userName);
        params.put("password", password);
        params.put("realname", userName);
        params.put("comment", "");
        params.put("email", userName + "@cloud" + ".com");
        String cookie = harborUtil.checkCookieTimeout();
        Map<String, Object> header = new HashMap<String, Object>();
        header.put("Cookie", cookie);
        header.put("Content-type", "application/json");
        String harborUId = null;

        CloseableHttpResponse response = HttpClientUtil.doBodyPost(addUrl, params, header);
        if (HttpStatusUtil.isSuccessStatus(response.getStatusLine().getStatusCode())) {
            // 密码md5加密
            // 获取harbor用户uuid作为user id
            Header[] headers = response.getHeaders("Location");
            if (headers.length > 0) {
                Header location = headers[0];
                harborUId = location.getValue().substring(location.getValue().lastIndexOf("/") + 1);
                String md5Password = StringUtil.convertToMD5(password);
                User user = new User();
                user.setPassword(md5Password);
                user.setId(Long.valueOf(harborUId));
                user.setCreateTime(new Date());
                userMapper.addUser(user);
                userService.addLdapUser(userName, password, harborUId);
            } else {
                throw new RuntimeException("add harbor user fail");
            }
        } else {
            throw new RuntimeException("ping harbor fail");
        }
        return userName;
    }

    public String getHarborIp() {
        return harborIp;
    }

    public void setHarborIp(String harborIp) {
        this.harborIp = harborIp;
    }

    public String getHarborPort() {
        return harborPort;
    }

    public void setHarborPort(String harborPort) {
        this.harborPort = harborPort;
    }

    public String getHarborUser() {
        return harborUser;
    }

    public void setHarborUser(String harborUser) {
        this.harborUser = harborUser;
    }

    public String getHarborPassword() {
        return harborPassword;
    }

    public void setHarborPassword(String harborPassword) {
        this.harborPassword = harborPassword;
    }

    public String getHarborTimeout() {
        return harborTimeout;
    }

    public void setHarborTimeout(String harborTimeout) {
        this.harborTimeout = harborTimeout;
    }

    public HarborUtil getHarborUtil() {
        return harborUtil;
    }

    public void setHarborUtil(HarborUtil harborUtil) {
        this.harborUtil = harborUtil;
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
}
