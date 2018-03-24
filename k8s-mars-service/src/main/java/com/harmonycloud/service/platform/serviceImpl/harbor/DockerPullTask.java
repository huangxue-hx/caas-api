package com.harmonycloud.service.platform.serviceImpl.harbor;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.RegistryAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.*;
import java.util.concurrent.Callable;

import static com.harmonycloud.common.Constant.CommonConstant.IMAGE_PULLING_STATUS_PULLED;
import static com.harmonycloud.common.Constant.CommonConstant.REDIS_KEY_IMAGE_PULL_STATUS;

/**
 * 从harbor拉镜像至本地，并保存到tar文件
 */
public class DockerPullTask implements Callable<Boolean>{
    private static final Logger logger = LoggerFactory.getLogger(DockerPullTask.class);
    private DockerClient dockerClient;
    private String imageName;
    private RegistryAuth registryAuth;
    private String tarFileName;
    private StringRedisTemplate stringRedisTemplate;


    public DockerPullTask(DockerClient dockerClient, String imageName, RegistryAuth registryAuth,
                          String tarFileName, StringRedisTemplate stringRedisTemplate){
        this.dockerClient = dockerClient;
        this.imageName = imageName;
        this.registryAuth = registryAuth;
        this.tarFileName = tarFileName;
        this.stringRedisTemplate = stringRedisTemplate;
    }


    @Override
    public Boolean call() throws Exception {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        BoundHashOperations<String, String, String> statusHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_IMAGE_PULL_STATUS);
        try {
            logger.info("start pull image:{}",imageName);
            File tarFile = new File(tarFileName);
            if(tarFile.exists()){
                tarFile.delete();
            }
            dockerClient.pull(imageName, registryAuth);
            inputStream = dockerClient.save(imageName);
            outputStream = new FileOutputStream(tarFile);
            //循环写入输出流
            byte[] b = new byte[2048];
            int length;
            while ((length = inputStream.read(b)) > 0) {
                outputStream.write(b, 0, length);
            }
            outputStream.flush();
            dockerClient.removeImage(imageName);
            statusHashOps.put(imageName, IMAGE_PULLING_STATUS_PULLED);
            logger.info("end pull image:{}",imageName);
        }catch (Exception e){
            logger.error("镜像pull/save失败，imageName:{}",imageName,e);
            //镜像拉取失败,删除拉取状态
            statusHashOps.delete(REDIS_KEY_IMAGE_PULL_STATUS, imageName);
            return false;
        }finally {
            if(inputStream != null){
                inputStream.close();
            }
            if(outputStream != null){
                outputStream.close();
            }
        }
        return true;
    }
}
