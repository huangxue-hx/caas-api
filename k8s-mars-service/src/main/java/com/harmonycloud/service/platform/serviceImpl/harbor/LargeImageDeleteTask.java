package com.harmonycloud.service.platform.serviceImpl.harbor;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.service.cache.ImageCacheManager;
import com.harmonycloud.service.common.HarborHttpsClientUtil;
import com.harmonycloud.service.platform.client.HarborClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.harmonycloud.common.Constant.CommonConstant.COLON;
import static com.harmonycloud.common.Constant.CommonConstant.REDIS_KEY_IMAGE_DELETING;
import static com.harmonycloud.common.Constant.CommonConstant.SLASH;

/**
 * 删除超大镜像需要一些时间，通过异步任务进行删除
 */
public class LargeImageDeleteTask implements Callable<Boolean>{
    private static final Logger logger = LoggerFactory.getLogger(LargeImageDeleteTask.class);
    //删除大镜像超时时间60分钟
    private static final int SOCKET_TIME_OUT = 60 * 60 * 1000;
    private HarborServer harborServer;
    private Map<String, Object> headers;
    private StringRedisTemplate stringRedisTemplate;
    private ImageCacheManager imageCacheManager;
    private String repoName;


    public LargeImageDeleteTask(HarborServer harborServer, Map<String, Object> headers, StringRedisTemplate stringRedisTemplate,
                                ImageCacheManager imageCacheManager, String repoName){
        this.harborServer = harborServer;
        this.headers = headers;
        this.stringRedisTemplate = stringRedisTemplate;
        this.imageCacheManager = imageCacheManager;
        this.repoName = repoName;
    }


    @Override
    public Boolean call() throws Exception {
        try {
            String apiUrl = HarborClient.getHarborUrl(harborServer) + "/api/repositories/?repo_name=" + repoName;
            ActionReturnUtil response = HarborHttpsClientUtil.httpDoDelete(apiUrl, null, headers, SOCKET_TIME_OUT);
            if(response.isSuccess()){
                imageCacheManager.deleteRepoMessage(harborServer.getHarborHost(), repoName);
                return true;
            }
            //删除失败，将已经删除的tag更新到缓存
            imageCacheManager.freshRepositoryByTags(harborServer.getHarborHost(), repoName);
            logger.error("异步删除镜像失败，response：{}", JSONObject.toJSONString(response));
            return false;
        } catch (Exception e) {
            logger.error("异步删除镜像失败",e);
            return false;
        }finally {
            String key = REDIS_KEY_IMAGE_DELETING + COLON + harborServer.getHarborHost() + SLASH + repoName;
            logger.info("去掉正在删除的镜像标识redis key：{}",key);
            stringRedisTemplate.delete(key);
        }
    }
}
