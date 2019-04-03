package com.harmonycloud.service.util;

import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.k8s.bean.ObjectMeta;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Set;

import static com.harmonycloud.common.Constant.CommonConstant.COLON;
import static com.harmonycloud.common.Constant.CommonConstant.LINE;
import static com.harmonycloud.service.platform.constant.Constant.TOPO_LABEL_KEY;

public class BizUtil {

    public static boolean isPodWithDeployment(String podName, Set<String> deployments) throws IllegalArgumentException{
        String[] podNamePart = podName.split("-");
        if(podNamePart.length <3){
            throw new IllegalArgumentException("pod名称格式错误： " + podName);
        }else{
            //舍弃最后一个“-”后面的字符串
            String deploymentByPodName = podName.substring(0, podName.lastIndexOf("-"));
            //舍弃倒数第二个“-”后面的字符串，剩下的是deployment名称
            deploymentByPodName = deploymentByPodName.substring(0, deploymentByPodName.lastIndexOf("-"));
            //根据容器查询的判断pod名称前缀是不是服务名称deployment
            if(!deployments.contains(deploymentByPodName)){
                return false;
            }
            return true;
        }
    }

    public static String[] getImageInfoFromName(String imageFullName) throws MarsRuntimeException{
        if(StringUtils.isBlank(imageFullName)){
            return null;
        }
        if(imageFullName.indexOf("/") <0 || imageFullName.indexOf(":")<0){
            throw new MarsRuntimeException(DictEnum.IMAGE_NAME.phrase(),ErrorCodeMessage.FORMAT_ERROR);
        }
        String harborHost = imageFullName.substring(0,imageFullName.indexOf("/"));
        if(harborHost.contains(COLON)){
            harborHost = harborHost.substring(0,imageFullName.indexOf(COLON));
        }
        String repo = imageFullName.substring(imageFullName.indexOf("/")+1,imageFullName.lastIndexOf(":"));
        String tag = imageFullName.substring(imageFullName.lastIndexOf(":")+1);
        return new String[]{harborHost, repo,tag};
    }

    public static String getTopoLabelKey(String projectId, String appName){
        Assert.hasText(projectId);
        Assert.hasText(appName);
        return TOPO_LABEL_KEY + LINE + projectId + LINE + appName;
    }

    /**
     * 获取资源的修改时间，如果为空，则取创建时间
     * @param objectMeta
     * @return
     */
    public static String getUpdateTime(ObjectMeta objectMeta){
        if (objectMeta == null) {
            return null;
        }
        Map<String, Object> annotations = objectMeta.getAnnotations();
        if (annotations == null || annotations.get("updateTimestamp") == null) {
            return objectMeta.getCreationTimestamp();
        }
        return annotations.get("updateTimestamp").toString();
    }

}
