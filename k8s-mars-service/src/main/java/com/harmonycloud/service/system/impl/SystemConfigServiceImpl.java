package com.harmonycloud.service.system.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.system.SystemConfigMapper;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dto.cicd.CicdConfigDto;
import com.harmonycloud.dto.user.LdapConfigDto;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.util.SsoClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

import static com.harmonycloud.common.Constant.CommonConstant.FLAG_TRUE;

/**
 * Created by hongjie
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SystemConfigServiceImpl implements SystemConfigService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private HttpSession session;

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
    public LdapConfigDto findLdapConfig() {
        LdapConfigDto ldapConfigDto = new LdapConfigDto();
        List<SystemConfig> list = this.systemConfigMapper.findByConfigType(CommonConstant.CONFIG_TYPE_LDAP);
        if(list != null && list.size() > 0) {
            for(SystemConfig sc : list) {
                switch (sc.getConfigName()){
                    case CommonConstant.LDAP_IP:
                        ldapConfigDto.setIp(sc.getConfigValue());
                        break;
                    case CommonConstant.LDAP_PORT:
                        ldapConfigDto.setPort(sc.getConfigValue());
                        break;
                    case CommonConstant.LDAP_BASE:
                        ldapConfigDto.setBase(sc.getConfigValue());
                        break;
                    case CommonConstant.LDAP_USERDN:
                        ldapConfigDto.setUserdn(sc.getConfigValue());
                        break;
                    case CommonConstant.LDAP_PASSWORD:
                        ldapConfigDto.setPassword(sc.getConfigValue());
                        break;
                    case CommonConstant.LDAP_IS_ON:
                        ldapConfigDto.setIsOn(Integer.parseInt(sc.getConfigValue()));
                        break;
                    case CommonConstant.LDAP_OBJECT_CLASS:
                        ldapConfigDto.setObjectClass(sc.getConfigValue());
                        break;
                    case CommonConstant.LDAP_SEARCH_ATTR:
                        ldapConfigDto.setSearchAttribute(sc.getConfigValue());
                        break;
                    default:
                        break;
                }
            }
        }
        return ldapConfigDto;
    }

    @Override
    public boolean getLocalUserFlag() {
        if(SsoClient.isOpen()){
            return false;
        }
        LdapConfigDto ldapConfigDto = this.findLdapConfig();
        if(ldapConfigDto != null && ldapConfigDto.getIsOn() != null && ldapConfigDto.getIsOn() == FLAG_TRUE){
            return false;
        }
        return true;
    }

    @Override
    public List<SystemConfig> findByConfigType(String configType) {
        return this.systemConfigMapper.findByConfigType(configType);
    }

    @Override
    public SystemConfig findByConfigName(String configName) {
        return this.systemConfigMapper.findByConfigName(configName);
    }

    @Override
    public String findConfigValueByName(String configName) {
        SystemConfig config = this.findByConfigName(configName);
        if(config == null){
            return null;
        }
        return config.getConfigValue();
    }

    @Override
    public SystemConfig findMaintenanceStatus() {
        SystemConfig config = systemConfigMapper.findByConfigName(CommonConstant.MAINTENANCE_STATUS);
        if(config == null){
            config = new SystemConfig();
            config.setConfigName(CommonConstant.MAINTENANCE_STATUS);
            config.setConfigType(CommonConstant.CONFIG_TYPE_MAINTENANCE);
            config.setConfigValue(String.valueOf(CommonConstant.FALSE));
            systemConfigMapper.addSystemConfig(config);
        }
        return config;
    }

    @Override
    public void updateMaintenanceStatus(String status) {
        SystemConfig config = systemConfigMapper.findByConfigName(CommonConstant.MAINTENANCE_STATUS);
        String username = (String)session.getAttribute(CommonConstant.USERNAME);
        if(config == null){
            config = new SystemConfig();
            config.setConfigName(CommonConstant.MAINTENANCE_STATUS);
            config.setConfigType(CommonConstant.CONFIG_TYPE_MAINTENANCE);
            config.setConfigValue(status);
            config.setCreateUser(username);
            config.setCreateTime(DateUtil.getCurrentUtcTime());
            systemConfigMapper.addSystemConfig(config);
        }else{
            config.setConfigValue(status);
            config.setUpdateUser(username);
            config.setUpdateTime(DateUtil.getCurrentUtcTime());
            systemConfigMapper.updateSystemConfig(config);
        }
    }

    @Override
    public CicdConfigDto getCicdConfig() {
        CicdConfigDto cicdConfigDto = new CicdConfigDto();
        List<SystemConfig> list = this.systemConfigMapper.findByConfigType(CommonConstant.CONFIG_TYPE_CICD);
        if(list != null && list.size() > 0) {
            for(SystemConfig sc : list) {
                if (CommonConstant.CICD_RESULT_REMAIN_NUM.equals(sc.getConfigName())) {
                    cicdConfigDto.setRemainNumber(StringUtils.isBlank(sc.getConfigValue()) ? null : Integer.valueOf(sc.getConfigValue()));
                }else if(CommonConstant.CICD_IS_TYPE_MERGE.equals(sc.getConfigName())){
                    cicdConfigDto.setTypeMerge(Boolean.valueOf(sc.getConfigValue()));
                }
            }
        }
        return cicdConfigDto;
    }

    @Override
    public void updateCicdConfig(CicdConfigDto cicdConfigDto) {
        String username = (String) session.getAttribute(CommonConstant.USERNAME);
        SystemConfig cicdConfig  = this.systemConfigMapper.findByConfigName(CommonConstant.CICD_RESULT_REMAIN_NUM);
        if(cicdConfig == null){
            cicdConfig = new SystemConfig();
            cicdConfig.setConfigName(CommonConstant.CICD_RESULT_REMAIN_NUM);
            cicdConfig.setConfigType(CommonConstant.CONFIG_TYPE_CICD);
            cicdConfig.setConfigValue(String.valueOf(cicdConfigDto.getRemainNumber()));
            cicdConfig.setCreateTime(DateUtil.getCurrentUtcTime());
            cicdConfig.setCreateUser(username);
            systemConfigMapper.addSystemConfig(cicdConfig);
        }else{
            cicdConfig.setConfigValue(cicdConfigDto.getRemainNumber() == null ? null:String.valueOf(cicdConfigDto.getRemainNumber()));
            cicdConfig.setUpdateUser(username);
            cicdConfig.setUpdateTime(DateUtil.getCurrentUtcTime());
            systemConfigMapper.updateSystemConfig(cicdConfig);
        }
    }
}
