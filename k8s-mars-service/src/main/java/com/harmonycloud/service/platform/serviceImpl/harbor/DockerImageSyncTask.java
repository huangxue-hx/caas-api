package com.harmonycloud.service.platform.serviceImpl.harbor;

import com.harmonycloud.service.util.BizUtil;
import com.harmonycloud.service.platform.bean.harbor.HarborLog;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.RegistryAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 从harbor拉镜像至本地，并保存到tar文件
 */
public class DockerImageSyncTask implements Callable<Boolean>{
    private static final Logger logger = LoggerFactory.getLogger(DockerImageSyncTask.class);
    private DockerClient dockerClient;
    private String sourceImageName;
    private String destImageName;
    private RegistryAuth sourceRegistryAuth;
    private RegistryAuth destRegistryAuth;
    private LinkedBlockingQueue<HarborLog> queue;



    public DockerImageSyncTask(DockerClient dockerClient, String sourceImageName, String destImageName,
                               RegistryAuth sourceRegistryAuth, RegistryAuth destRegistryAuth, LinkedBlockingQueue queue){
        this.dockerClient = dockerClient;
        this.sourceImageName = sourceImageName;
        this.destImageName = destImageName;
        this.sourceRegistryAuth = sourceRegistryAuth;
        this.destRegistryAuth = destRegistryAuth;
        this.queue = (LinkedBlockingQueue<HarborLog>)queue;
    }


    @Override
    public Boolean call() throws Exception {
        logger.info("start sync image,source:{},dest:{}",sourceImageName, destImageName);
        dockerClient.pull(sourceImageName, sourceRegistryAuth);
        dockerClient.tag(sourceImageName, destImageName);
        dockerClient.push(destImageName, destRegistryAuth);
        dockerClient.removeImage(sourceImageName);
        dockerClient.removeImage(destImageName);
        String[] imagePart = BizUtil.getImageInfoFromName(destImageName);
        HarborLog harborLog = new HarborLog(imagePart[0],imagePart[1],imagePart[2]);
        queue.put(harborLog);
        logger.info("end sync image,source:{},dest:{}",sourceImageName, destImageName);
        return true;
    }
}
