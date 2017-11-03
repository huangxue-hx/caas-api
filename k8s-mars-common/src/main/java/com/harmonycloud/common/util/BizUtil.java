package com.harmonycloud.common.util;

public class BizUtil {

    public static boolean isPodWithDeployment(String podName, String deployment) throws IllegalArgumentException{
        String[] podNamePart = podName.split("-");
        if(podNamePart.length <3){
            throw new IllegalArgumentException("pod名称格式错误： " + podName);
        }else{
            //舍弃最后一个“-”后面的字符串
            String deploymentByPodName = podName.substring(0, podName.lastIndexOf("-"));
            //舍弃倒数第二个“-”后面的字符串，剩下的是deployment名称
            deploymentByPodName = deploymentByPodName.substring(0, deploymentByPodName.lastIndexOf("-"));
            //根据容器查询的判断pod名称前缀是不是服务名称deployment
            if(!deployment.equals(deploymentByPodName)){
                return false;
            }
            return true;
        }
    }

}
