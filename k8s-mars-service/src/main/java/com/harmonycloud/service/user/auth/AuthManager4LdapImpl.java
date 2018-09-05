package com.harmonycloud.service.user.auth;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.dao.user.UserMapper;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.user.LdapConfigDto;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.harbor.HarborUser;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.harbor.HarborUserService;
import com.harmonycloud.service.user.AuthManager4Ldap;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Title AuthManager4LdapImpl.java
 * @author yj
 * @date 2017年5月3日
 * @Description ldap方式实现认证接口
 * @version V1.0
 */
@Service
public class AuthManager4LdapImpl implements AuthManager4Ldap {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthManager4LdapImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    HarborUserService harborUserService;

    @Autowired
    ClusterService clusterService;

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
        // 对ldap认证通过的用户,判断是否已经记录,如果已记录并且已修改,修改记录,并且更新Harbor

        User user = userService.getUser(userName);
        if (user != null) {
            return userName;
        }
        //向harbor中同步用户
        Set<HarborServer> harborServers = clusterService.listAllHarbors();
        List<HarborServer> harborServerList = harborServers.stream().collect(Collectors.toList());
        for (HarborServer harborServer :harborServerList) {
            HarborUser harborUser = this.harborUserService.getUserByName(harborServer,userName);
            if(harborUser == null){
                User userNew = new User();
                userNew.setUsername(userName);
                userNew.setPassword(password);
                //todo
                userNew.setUsername("admin");
                userNew.setPassword("123");
                try {
                    harborUserService.harborUserLogin(harborServer, userNew);
                }catch (Exception e){
                    LOGGER.error("登录harbor失败，确认harbor是否也启用ladp验证, username:{}", userName, e);
                }
            }
        }
        insertUser(userName);
        return userName;
    }

    private boolean isUserInLdap(String userName, String password, LdapConfigDto ldapConfigDto) {
        //todo
        if(StringUtils.isNotBlank(userName)){
            return true;
        }
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", ldapConfigDto.getObjectClass()))
                .and(new EqualsFilter(ldapConfigDto.getSearchAttribute(), userName));
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://" + ldapConfigDto.getIp() + ":" + ldapConfigDto.getPort() + "");
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

        return template.authenticate(getDnForUser(userName,contextSource,ldapConfigDto),
                "(objectclass="+ldapConfigDto.getObjectClass()+")", password);

    }

    // 插入Harbor用户,不带密码
    private void insertUser(String userName) throws Exception {
        User user = new User();
        user.setUsername(userName);
        user.setPause(CommonConstant.NORMAL);
        user.setIsAdmin(Constant.NON_ADMIN_ACCOUNT);
        user.setIsMachine(Constant.NON_MACHINE_ACCOUNT);
        user.setRealName(userName);
        user.setIsLdapUser(Boolean.TRUE);
        user.setCreateTime(new Date());
        userMapper.insert(user);
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

    /**
     * 根据cn 构建出 entry 的 Dn
     *
     * @param cn
     * @return
     */

    @SuppressWarnings({"unused", "unchecked"})
    private String getDnForUser(String cn,LdapContextSource contextSource, LdapConfigDto ldapConfigDto) {
        LdapTemplate template = new LdapTemplate();
        template.setContextSource(contextSource);
        List<String> results = template.search("", "(&(objectclass="+ldapConfigDto.getObjectClass()+")("
                +ldapConfigDto.getSearchAttribute()+"=" + cn + "))", new DnMapper());
        if (results.size() != 1) {

            throw new RuntimeException("User not found or not unique");
        }
        System.out.println(results.get(0));
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
