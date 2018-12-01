package com.harmonycloud.task;

import com.harmonycloud.service.platform.service.harbor.HarborImageCleanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CleanRepoTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanRepoTask.class);

    @Autowired
    private HarborImageCleanService harborImageCleanService;

    public void run(){
        try {
            harborImageCleanService.cleanRepo();
        } catch (Exception e) {
            LOGGER.error("清理镜像出错", e);
        }
    }
}
