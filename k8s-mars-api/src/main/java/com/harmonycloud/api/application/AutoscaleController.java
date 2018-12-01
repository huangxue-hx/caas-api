package com.harmonycloud.api.application;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.scale.HPADto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.EsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * Created by root on 5/22/17.
 */
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/deploys/{deployName}/hpa")
@Controller
public class AutoscaleController {

    @Autowired
    private DeploymentsService dpService;

    @Autowired
    private EsService esService;
    @Autowired
    private HttpSession session;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil getAutoScaleApp(@PathVariable(value = "deployName") String deploymentName,
                                            @RequestParam(value = "namespace") String namespace) throws Exception {
        logger.info("获取应用自动伸缩");
        return dpService.getAutoScaleDeployment(deploymentName, namespace);
    }

    /**
     * 设置自动伸缩
     * @param hpaDto
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil addAutoScaleApp(@RequestBody HPADto hpaDto) throws Exception {
        logger.info("设置应用自动伸缩");
        return dpService.autoScaleDeployment(hpaDto);
    }


    @ResponseBody
    @RequestMapping(method = RequestMethod.PUT)
    public ActionReturnUtil updateAutoScaleApp(@RequestBody HPADto hpaDto) throws Exception {
        logger.info("更新应用自动伸缩");
        return dpService.updateAutoScaleDeployment(hpaDto);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.DELETE)
    public ActionReturnUtil deleteAutoScaleApp(@PathVariable(value = "deployName") String deploymentName,
                                               @RequestParam(value = "namespace") String namespace) throws Exception {

        logger.info("删除应用自动伸缩");
        return dpService.deleteAutoScaleDeployment(deploymentName, namespace);
    }

}
