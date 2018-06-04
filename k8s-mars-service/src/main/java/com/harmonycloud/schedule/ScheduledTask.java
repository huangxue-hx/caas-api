package com.harmonycloud.schedule;

import com.harmonycloud.service.platform.service.ci.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by anson on 17/8/26.
 */
@Component
public class ScheduledTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTask.class);

    @Autowired
    JobService jobService;

    @Scheduled(fixedRate = 1000*60*30, initialDelay = 1000*60)
    public void destoryCicdPod(){
        try {
            jobService.destroyCicdPod(null);
        }catch (Exception e){
            LOGGER.error("销毁cicd pod失败",e);
        }
    }
}
