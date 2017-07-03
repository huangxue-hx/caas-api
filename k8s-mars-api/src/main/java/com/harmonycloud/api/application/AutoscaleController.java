package com.harmonycloud.api.application;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.EsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by root on 5/22/17.
 */
@RequestMapping("/autoscale")
@Controller
public class AutoscaleController {

    @Autowired
    DeploymentsService dpService;

    @Autowired
    EsService esService;
    @Autowired
    HttpSession session;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 设置自动伸缩
     * @param deploymentName
     * @param namespace
     * @param max
     * @param min
     * @param cpu
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil AutoScaleApp(@RequestParam(value = "deploymentName") String deploymentName,
                                         @RequestParam(value = "namespace") String namespace, @RequestParam(value = "max") Integer max,
                                         @RequestParam(value = "min") Integer min, @RequestParam(value = "cpu") Integer cpu) throws Exception {

        try {
            logger.info("设置应用自动伸缩");
            String userName = (String) session.getAttribute("username");
			if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}
			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
            return dpService.autoScaleDeployment(deploymentName, namespace, max, min, cpu, cluster);
        } catch (Exception e) {
            throw e;
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.PUT)
    public ActionReturnUtil updateAutoScaleApp(@RequestParam(value = "deploymentName") String deploymentName,
                                               @RequestParam(value = "namespace") String namespace, @RequestParam(value = "max") Integer max,
                                               @RequestParam(value = "min") Integer min, @RequestParam(value = "cpu") Integer cpu) throws Exception {
        try {
            logger.info("设置应用自动伸缩");
            String userName = (String) session.getAttribute("username");
            if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}
			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
            return dpService.updateAutoScaleDeployment(deploymentName, namespace, max, min, cpu, cluster);
        } catch (Exception e) {
            throw e;
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.DELETE)
    public ActionReturnUtil deleteAutoScaleApp(@RequestParam(value = "deploymentName") String deploymentName,
                                               @RequestParam(value = "namespace") String namespace) throws Exception {
        try {
            logger.info("设置应用自动伸缩");
            String userName = (String) session.getAttribute("username");
            if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}
			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
            return dpService.deleteAutoScaleDeployment(deploymentName, namespace, cluster);
        } catch (Exception e) {
            throw e;
        }
    }

}
