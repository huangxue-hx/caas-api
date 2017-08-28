package com.harmonycloud.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    TrialtimeTask trialtimeTask;

    /**
     * 1小时更新一次试用时间
     */
    @Scheduled(fixedRate = 60000*60)
    public void emailAlert() {
        long startTime = System.currentTimeMillis();
        System.out.println(new Date());
        trialtimeTask.run();
        long endTime = System.currentTimeMillis();
        log.info("task[trialtimeTask],execute cost time[" + (endTime - startTime)/1000 + "] s");
    }




}
