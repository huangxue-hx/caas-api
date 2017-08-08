package com.harmonycloud.service.application.Util;

import com.harmonycloud.dto.business.DeploymentDetailDto;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.bean.Service;
import com.harmonycloud.service.platform.convert.K8sResultConvert;

/**
 * Created by root on 8/8/17.
 */
public class TemplateToYamlUtil {

    public static Deployment templateToDeployment(DeploymentDetailDto deploymentDetailDto){
        Deployment deployment = new Deployment();
        try {
            deployment = K8sResultConvert.convertAppCreate(deploymentDetailDto,null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return deployment;
    }

    public static Service templateToService(DeploymentDetailDto deploymentDetailDto){
        Service service = new Service();
        try {
            service = K8sResultConvert.convertAppCreateOfService(deploymentDetailDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return service;
    }

}
