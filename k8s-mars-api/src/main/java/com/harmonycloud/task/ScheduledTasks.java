package com.harmonycloud.task;


import com.harmonycloud.service.cache.ImageCacheManager;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.apache.commons.lang3.StringUtils;

import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.io.File;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.harmonycloud.common.Constant.CommonConstant.IMAGE_FILE_DOWNLOAD_PATH;
import static com.harmonycloud.common.Constant.CommonConstant.REDIS_KEY_IMAGE_PULL_STATUS;




@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final String CLEANREPO = "cleanrepo";
    private static final String BACKUPAPP = "backupapp";
    private static final String DELETEIMAGEFILE = "deleteimagefile";
    private static final String IMAGEREFRESH = "imagerefresh";
    private static final String SCHEDULED = "scheduled" ;

    @Autowired
    private TrialtimeTask trialtimeTask;

    @Autowired
    private CleanRepoTask cleanRepoTask;

    @Autowired
    private BackupAppLogTask backupAppLogTask;

    @Autowired
    ImageCacheManager imageCacheManager;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Value("#{propertiesReader['upload.path']}")
    private String tempPath;


    //启动后延迟30秒每15分钟刷新缓存
    @Scheduled(fixedRate = 10 * 60 * 1000, initialDelay =  30 * 1000)
    public void freshRepository() {
        if(checkLeader(IMAGEREFRESH,15, TimeUnit.MINUTES)) {
            imageCacheManager.freshRepository();
            stringRedisTemplate.delete(IMAGEREFRESH);
        }else {
            log.info("freshRepository has been scgeduled");
        }
    }

    /**
     * 24小时更新一次试用时间
     */
    @Scheduled(fixedRate = 60000*60*24)
    public void emailAlert() {
        long startTime = System.currentTimeMillis();
        trialtimeTask.run();
        long endTime = System.currentTimeMillis();
        log.info("task[trialtimeTask],execute cost time[" + (endTime - startTime)/1000 + "] s");
    }

    /**
     * 每天闲时3点执行镜像清理任务
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanRepo() {
        long startTime = System.currentTimeMillis();
        boolean status = checkLeader(CLEANREPO,12, TimeUnit.HOURS);
        if (status) {
            cleanRepoTask.run();
        } else {
            log.info("has been scgeduled");
            return ;
        }

        long endTime = System.currentTimeMillis();
        log.info("task[cleanRepoTask],execute cost time[" + (endTime - startTime)/1000 + "] s");
    }

    /**
     * 每天闲时1点执行备份应用日志任务
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void backupAppLog() {
        long startTime = System.currentTimeMillis();
        boolean status = checkLeader(BACKUPAPP,12, TimeUnit.HOURS);
        if (status) {
            backupAppLogTask.run();
        } else {
            log.info("has been scgeduled");
            return ;
        }
        long endTime = System.currentTimeMillis();
        log.info("task[backupAppLogTask],execute cost time[" + (endTime - startTime)/1000 + "] s");
    }

    /**
     * 每天闲时2点删除镜像临时文件，防止镜像文件长期存放导致磁盘爆满
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void deleteImageTempFile() {
        long startTime = System.currentTimeMillis();
        boolean status = checkLeader(DELETEIMAGEFILE, 12, TimeUnit.HOURS);
        if (status) {
            //清空镜像下载状态
            BoundHashOperations<String, String, String> statusHashOps = stringRedisTemplate
                    .boundHashOps(REDIS_KEY_IMAGE_PULL_STATUS);
            Set<String> keys = statusHashOps.keys();
            for(String key : keys){
                statusHashOps.delete(REDIS_KEY_IMAGE_PULL_STATUS, key);
            }
            String filePath = tempPath + File.separator + IMAGE_FILE_DOWNLOAD_PATH;
            try {
                FileUtils.forceDelete(new File(filePath));
            }catch (Exception e){
                log.info("task[deleteImageTempFile], 删除镜像下载临时文件失败",e);
            }
        } else {
            log.info("has been scgeduled");
            return ;
        }
        long endTime = System.currentTimeMillis();
        log.info("task[deleteImageTempFile],execute cost time[" + (endTime - startTime)/1000 + "] s");
    }

    /**
     * 在多后端情况下，认证避免多次定时任务触发
     */
    public boolean checkLeader(String leaderKey, Integer expire, TimeUnit timeUnit) {
        String value  = stringRedisTemplate.opsForValue().get(leaderKey);
        if(StringUtils.isBlank(value)){
            boolean status = stringRedisTemplate.opsForValue().setIfAbsent(leaderKey,SCHEDULED);
            if (status) {
                stringRedisTemplate.expire(leaderKey, expire, timeUnit);
            }
            return status;
        }
        return false ;
    }



}
