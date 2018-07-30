package com.harmonycloud.common.util;

import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import org.apache.commons.lang3.StringUtils;
import java.util.Set;

import static com.harmonycloud.common.Constant.CommonConstant.COLON;

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

}
