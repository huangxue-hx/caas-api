package com.harmonycloud.schedule;

import com.harmonycloud.service.platform.service.ci.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by anson on 17/8/26.
 */
@Component
public class ScheduledTask {

    @Autowired
    JobService jobService;

    @Scheduled(fixedRate = 1000*60*30, initialDelay = 1000*60)
    public void destoryCicdPod(){
        jobService.destroyCicdPod(null);
    }
}
