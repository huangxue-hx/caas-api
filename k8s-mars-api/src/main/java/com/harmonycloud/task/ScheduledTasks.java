package com.harmonycloud.task;


import com.harmonycloud.service.cache.ImageCacheManager;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
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
    private static final String IMAGEREFRESH = "imagerefreshall";
    private static final String IMAGEREFRESHLOG = "imagerefreshlog";
    private static final String SCHEDULED = "scheduled" ;
    private static final String DELETEBUILDRESULT = "deletebuildresult";

    @Autowired
    private TrialtimeTask trialtimeTask;

    @Autowired
    private CleanRepoTask cleanRepoTask;

    @Autowired
    private BackupAppLogTask backupAppLogTask;

    @Autowired
    private ImageCacheManager imageCacheManager;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JobService jobService;
    @Value("#{propertiesReader['upload.path']}")
    private String tempPath;

    @Autowired
    private HarborProjectService harborProjectService;

    //启动后延迟1分钟每30分钟根据harbor的操作日志刷新缓存
    @Scheduled(fixedRate = 30 * 60 * 1000, initialDelay =  60 * 1000)
    public void freshRepositoryByLog() {
        log.info("fresh repository by log");
        try {
            if(checkLeader(IMAGEREFRESHLOG,18, TimeUnit.MINUTES)) {
                imageCacheManager.freshRepositoryByLog();
            }else {
                log.info("freshRepository has been scgeduled");
            }
        }catch (Exception e){
            log.error("根据harbor的操作日志刷新镜像缓存失败",e);
        }
    }

    //每天5点全量刷新镜像缓存
    @Scheduled(cron = "0 0 5 * * ?")
    public void freshAllRepository() {
        log.info("begin fresh all repository");
        if(checkLeader(IMAGEREFRESH,8, TimeUnit.HOURS)) {
            log.info("start fresh all repository");
            imageCacheManager.freshRepository();
        }else {
            log.info("freshAllRepository has been scheduled");
        }
    }

    /**
     * 24小时更新一次试用时间
     */
    //@Scheduled(fixedRate = 60000*60*24)
    public void trialTimeTask() {
        long startTime = System.currentTimeMillis();
        trialtimeTask.run();
        long endTime = System.currentTimeMillis();
        log.info("task[trialtimeTask],execute cost time[" + (endTime - startTime)/1000 + "] s");
    }

    /**
     * 每天闲时4点执行镜像清理任务
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanRepo() {
        long startTime = System.currentTimeMillis();
        boolean status = checkLeader(CLEANREPO,12, TimeUnit.HOURS);
        if (status) {
            log.info("start cleanrepo task");
            cleanRepoTask.run();
        } else {
            log.info("cleanrepo has been scheduled");
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
     * 每天闲时3点删除流水线构建记录
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void deleteBuildResult() {
        long startTime = System.currentTimeMillis();
        boolean status = checkLeader(DELETEBUILDRESULT, 12, TimeUnit.HOURS);
        if(status) {
            try {
                jobService.deleteBuildResult();
            } catch (Exception e) {
                log.error("delete build result failed. {}", e);
            }
        } else {
            log.info("has been scheduled");
            return ;
        }
        long endTime = System.currentTimeMillis();
        log.info("task[deleteBuildResult],execute cost time[" + (endTime - startTime)/1000 + "] s");
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


    /**
     * 一分钟频率检测harbor日志
     */
    //
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void syncLocalHarborLog() {
        long startTime = System.currentTimeMillis();
        try {
            harborProjectService.syncLocalHarborLog();
        }catch (Exception e){
            log.info("harbor 镜像触发流水线 error",e);
        }
        long endTime = System.currentTimeMillis();
        log.info("task[trialtimeTask],execute cost time[" + (endTime - startTime)/1000 + "] s");
    }


}
