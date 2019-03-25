package com.harmonycloud.service.system;

import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dto.cicd.CicdConfigDto;
import com.harmonycloud.dto.user.LdapConfigDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional(rollbackFor = Exception.class)
public interface SystemConfigService {

    SystemConfig findById(String id);

    void addSystemConfig(SystemConfig systemConfig);

    void updateSystemConfig(SystemConfig systemConfig);

    void addLdapConfig(LdapConfigDto ldapConfigDto);

    LdapConfigDto findLdapConfig();

    /**
     * 获取是否使用平台自身的用户系统
     * @return 开启ldap或sso登录，返回fasle，其他true
     */
    boolean getLocalUserFlag();

    List<SystemConfig> findByConfigType(String configType);

    SystemConfig findByConfigName(String configName);

    String findConfigValueByName(String configName);

    SystemConfig findMaintenanceStatus();

    void updateMaintenanceStatus(String status);

    CicdConfigDto getCicdConfig();

    void updateCicdConfig(CicdConfigDto cicdConfigDto);
}
