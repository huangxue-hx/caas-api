package com.harmonycloud.service.platform.serviceImpl.harbor;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RemovedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public class DockerPushTask implements Callable<Boolean>{
    private static final Logger logger = LoggerFactory.getLogger(DockerPushTask.class);
    private DockerClient dockerClient;
    private String imageName;
    private RegistryAuth registryAuth;
    private File imageFile;


    public DockerPushTask(DockerClient dockerClient, String imageName, File imageFile, RegistryAuth registryAuth){
        this.dockerClient = dockerClient;
        this.imageName = imageName;
        this.imageFile = imageFile;
        this.registryAuth = registryAuth;
    }


    @Override
    public Boolean call() throws Exception {
        try {
            Set<String> loadedImages = dockerClient.load(new FileInputStream(imageFile));
            if (loadedImages.size() == 0) {
                throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_LOAD_ERROR);
            }
            if (loadedImages.size() > 1) {
                throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_UPLOAD_SINGLE);
            }
            String tarFileImage = "";
            for (String loadedImage : loadedImages) {
                tarFileImage = loadedImage;
            }
            dockerClient.tag(tarFileImage, imageName);
            dockerClient.push(imageName, registryAuth);
            logger.info("{}镜像上传完成", imageName);
            List<RemovedImage> removedImages = dockerClient.removeImage(tarFileImage);
            removedImages.addAll(dockerClient.removeImage(imageName));
            removedImages.stream().forEach(image -> logger.info("删除镜像：{}", image.imageId()));
            imageFile.delete();
            return true;
        } catch (Exception e) {
            logger.error("上传镜像失败",e);
            return false;
        } finally{
            imageFile.delete();
        }
    }
}
