package com.harmonycloud.task;

import com.harmonycloud.service.application.AppLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BackupAppLogTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackupAppLogTask.class);

    @Autowired
    private AppLogService appLogService;

    public void  run(){
        try {
            appLogService.backupAppLog();
        } catch (Exception e) {
            LOGGER.error("备份应用日志出错", e);
        }
    }

}
