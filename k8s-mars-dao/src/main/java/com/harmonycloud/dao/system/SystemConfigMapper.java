package com.harmonycloud.dao.system;


import com.harmonycloud.dao.system.bean.SystemConfig;

import java.util.List;

public interface SystemConfigMapper {

    SystemConfig findById(String id);

    SystemConfig findByConfigName(String configName);

    List<SystemConfig> findByConfigType(String configType);

    void addSystemConfig(SystemConfig systemConfig);

    void updateSystemConfig(SystemConfig systemConfig);


}