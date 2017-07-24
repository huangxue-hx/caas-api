package com.harmonycloud.service.system;

import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dto.user.LdapConfigDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(rollbackFor = Exception.class)
public interface SystemConfigService {

    SystemConfig findById(String id);

    void addSystemConfig(SystemConfig systemConfig);

    void updateSystemConfig(SystemConfig systemConfig);

    void addLdapConfig(LdapConfigDto ldapConfigDto);

    LdapConfigDto findByConfigType(String configType);
}