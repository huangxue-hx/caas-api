package com.harmonycloud.service.system.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dao.system.SystemConfigMapper;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dto.user.LdapConfigDto;
import com.harmonycloud.service.system.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * Created by hongjie
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SystemConfigServiceImpl implements SystemConfigService {

    @Autowired
    SystemConfigMapper systemConfigMapper;

    @Autowired
    HttpSession session;

    @Override
    public SystemConfig findById(String id) {
        return this.systemConfigMapper.findById(id);
    }



    @Override
    public void addSystemConfig(SystemConfig systemConfig) {
        this.systemConfigMapper.addSystemConfig(systemConfig);
    }

    @Override
    public void updateSystemConfig(SystemConfig systemConfig) {
        this.systemConfigMapper.updateSystemConfig(systemConfig);
    }

    @Override
    public void addLdapConfig(LdapConfigDto ldapConfigDto) {
        String username = (String) session.getAttribute("username");

        SystemConfig ldapIPConfig  = this.systemConfigMapper.findByConfigName(CommonConstant.LDAP_IP);
        boolean ldapIPBool = false;
        if(ldapIPConfig == null) {
            ldapIPConfig = new SystemConfig();
            ldapIPConfig.setCreateTime(new Date());
            ldapIPConfig.setCreateUser(username);
            ldapIPBool = true;
        }
        ldapIPConfig.setConfigName(CommonConstant.LDAP_IP);
        ldapIPConfig.setConfigValue(ldapConfigDto.getIp());
        ldapIPConfig.setUpdateTime(new Date());
        ldapIPConfig.setUpdateUser(username);
        ldapIPConfig.setConfigType(CommonConstant.CONFIG_TYPE_LDAP);
        if(ldapIPBool) {
            this.systemConfigMapper.addSystemConfig(ldapIPConfig);
        } else {
            this.systemConfigMapper.updateSystemConfig(ldapIPConfig);
        }


        SystemConfig ldapPortConfig  = this.systemConfigMapper.findByConfigName(CommonConstant.LDAP_PORT);
        boolean ldapPortBool = false;
        if(ldapPortConfig == null) {
            ldapPortConfig = new SystemConfig();
            ldapPortConfig.setCreateTime(new Date());
            ldapPortConfig.setCreateUser(username);
            ldapPortBool = true;
        }
        ldapPortConfig.setConfigName(CommonConstant.LDAP_PORT);
        ldapPortConfig.setConfigValue(ldapConfigDto.getPort());
        ldapPortConfig.setUpdateTime(new Date());
        ldapPortConfig.setUpdateUser(username);
        ldapPortConfig.setConfigType(CommonConstant.CONFIG_TYPE_LDAP);
        if(ldapPortBool) {
            this.systemConfigMapper.addSystemConfig(ldapPortConfig);
        } else {
            this.systemConfigMapper.updateSystemConfig(ldapPortConfig);
        }


        SystemConfig baseConfig  = this.systemConfigMapper.findByConfigName(CommonConstant.LDAP_BASE);
        boolean baseBool = false;
        if(baseConfig == null) {
            baseConfig = new SystemConfig();
            baseConfig.setCreateTime(new Date());
            baseConfig.setCreateUser(username);
            baseBool = true;
        }
        baseConfig.setConfigName(CommonConstant.LDAP_BASE);
        baseConfig.setConfigValue(ldapConfigDto.getBase());
        baseConfig.setUpdateTime(new Date());
        baseConfig.setUpdateUser(username);
        baseConfig.setConfigType(CommonConstant.CONFIG_TYPE_LDAP);
        if(baseBool) {
            this.systemConfigMapper.addSystemConfig(baseConfig);
        } else {
            this.systemConfigMapper.updateSystemConfig(baseConfig);
        }

        SystemConfig userdnConfig  = this.systemConfigMapper.findByConfigName(CommonConstant.LDAP_USERDN);
        boolean userdnBool = false;
        if(userdnConfig == null) {
            userdnConfig = new SystemConfig();
            userdnConfig.setCreateTime(new Date());
            userdnConfig.setCreateUser(username);
            userdnBool = true;
        }
        userdnConfig.setConfigName(CommonConstant.LDAP_USERDN);
        userdnConfig.setConfigValue(ldapConfigDto.getUserdn());
        userdnConfig.setUpdateTime(new Date());
        userdnConfig.setUpdateUser(username);
        userdnConfig.setConfigType(CommonConstant.CONFIG_TYPE_LDAP);
        if(userdnBool) {
            this.systemConfigMapper.addSystemConfig(userdnConfig);
        } else {
            this.systemConfigMapper.updateSystemConfig(userdnConfig);
        }

        SystemConfig passwordConfig  = this.systemConfigMapper.findByConfigName(CommonConstant.LDAP_PASSWORD);
        boolean passwordBool = false;
        if(passwordConfig == null) {
            passwordConfig = new SystemConfig();
            passwordConfig.setCreateTime(new Date());
            passwordConfig.setCreateUser(username);
            passwordBool = true;
        }
        passwordConfig.setConfigName(CommonConstant.LDAP_PASSWORD);
        passwordConfig.setConfigValue(ldapConfigDto.getPassword());
        passwordConfig.setUpdateTime(new Date());
        passwordConfig.setUpdateUser(username);
        passwordConfig.setConfigType(CommonConstant.CONFIG_TYPE_LDAP);
        if(passwordBool) {
            this.systemConfigMapper.addSystemConfig(passwordConfig);
        } else {
            this.systemConfigMapper.updateSystemConfig(passwordConfig);
        }

        SystemConfig isOnConfig  = this.systemConfigMapper.findByConfigName(CommonConstant.LDAP_IS_ON);
        boolean isOnBool = false;
        if(isOnConfig == null) {
            isOnConfig = new SystemConfig();
            isOnConfig.setCreateTime(new Date());
            isOnConfig.setCreateUser(username);
            isOnBool = true;
        }
        isOnConfig.setConfigName(CommonConstant.LDAP_IS_ON);
        isOnConfig.setConfigValue(ldapConfigDto.getIsOn() + "");
        isOnConfig.setUpdateTime(new Date());
        isOnConfig.setUpdateUser(username);
        isOnConfig.setConfigType(CommonConstant.CONFIG_TYPE_LDAP);
        if(isOnBool) {
            this.systemConfigMapper.addSystemConfig(isOnConfig);
        } else {
            this.systemConfigMapper.updateSystemConfig(isOnConfig);
        }


    }

    @Override
    public LdapConfigDto findByConfigType(String configType) {
        LdapConfigDto ldapConfigDto = new LdapConfigDto();
        List<SystemConfig> list = this.systemConfigMapper.findByConfigType(configType);
        if(list != null && list.size() > 0) {
            for(SystemConfig sc : list) {
                if(sc.getConfigName().equals(CommonConstant.LDAP_IP)) {
                    ldapConfigDto.setIp(sc.getConfigValue());
                }
                if(sc.getConfigName().equals(CommonConstant.LDAP_PORT)) {
                    ldapConfigDto.setPort(sc.getConfigValue());
                }
                if(sc.getConfigName().equals(CommonConstant.LDAP_BASE)) {
                    ldapConfigDto.setBase(sc.getConfigValue());
                }
                if(sc.getConfigName().equals(CommonConstant.LDAP_USERDN)) {
                    ldapConfigDto.setUserdn(sc.getConfigValue());
                }
                if(sc.getConfigName().equals(CommonConstant.LDAP_PASSWORD)) {
                    ldapConfigDto.setPassword(sc.getConfigValue());
                }
                if(sc.getConfigName().equals(CommonConstant.LDAP_IS_ON)) {
                    ldapConfigDto.setIsOn(Integer.parseInt(sc.getConfigValue()));
                }
            }
        }
        return ldapConfigDto;
    }
}