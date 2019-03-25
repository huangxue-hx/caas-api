package com.harmonycloud.task;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.service.system.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TrialtimeTask {

    private static final Logger LOG = LoggerFactory.getLogger(TrialtimeTask.class);

    @Autowired
    private SystemConfigService systemConfigService;

    public void run() {

        try {
            SystemConfig validConfig  = this.systemConfigService.findByConfigName(CommonConstant.TRIAL_TIME);
            if (validConfig != null) {
                int currentV = Integer.parseInt(validConfig.getConfigValue());
                if (currentV > 0) {
                    Integer v = Integer.parseInt(validConfig.getConfigValue()) - 1;
                    validConfig.setConfigValue(v.toString());
                    validConfig.setUpdateTime(new Date());
                    this.systemConfigService.updateSystemConfig(validConfig);
                }

            }

        } catch (Exception e) {
            LOG.error("更新试用时间出错", e);
        }

    }


}
