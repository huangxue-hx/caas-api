package com.harmonycloud.service.application.Util;

import com.harmonycloud.dto.application.DeploymentDetailDto;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.bean.Service;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by root on 8/8/17.
 */
public class TemplateToYamlUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateToYamlUtil.class);
    public static Deployment templateToDeployment(DeploymentDetailDto deploymentDetailDto){
        Deployment deployment = new Deployment();
        try {
            deployment = K8sResultConvert.convertAppCreate(deploymentDetailDto,null,null,null);
        } catch (Exception e) {
            LOGGER.warn("templateToDeployment失败", e);
        }

        return deployment;
    }

    public static Service templateToService(DeploymentDetailDto deploymentDetailDto){
        Service service = new Service();
        try {
            service = K8sResultConvert.convertAppCreateOfService(deploymentDetailDto,null, Constant.DEPLOYMENT);
        } catch (Exception e) {
            LOGGER.warn("templateToService失败", e);
        }
        return service;
    }

}
