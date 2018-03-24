package com.harmonycloud.api.application;


import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.DaemonSetDetailDto;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.DaemonSetsService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @Author jiangmi
 * @Description Daemonset相关接口
 * @Date created in 2017-12-18
 * @Modified
 */
@Controller
@RequestMapping("/clusters")
public class DaemonSetController {

    @Autowired
    private DaemonSetsService daemonSetsService;

    @Autowired
    HttpSession session;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 创建DaemonSet
     *
     * @param daemonSetDetail
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/daemonsets", method = RequestMethod.POST)
    public ActionReturnUtil createDaemonSet(@ModelAttribute DaemonSetDetailDto daemonSetDetail) throws Exception {
        logger.info("创建DaemonSet");
        String userName = (String) session.getAttribute("username");
        if (StringUtils.isEmpty(userName)) {
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return daemonSetsService.createDaemonSet(daemonSetDetail, userName);
    }

    /**
     * 更新DaemonSet
     *
     * @param daemonSetDetail
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/daemonsets/{name}", method = RequestMethod.PUT)
    public ActionReturnUtil updateDaemonSet(@ModelAttribute DaemonSetDetailDto daemonSetDetail) throws Exception {
        logger.info("更新DaemonSet");
        String userName = (String) session.getAttribute("username");
        if (StringUtils.isEmpty(userName)) {
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return daemonSetsService.updateDaemonSet(daemonSetDetail, userName);
    }

    /**
     * 删除DaemonSet
     *
     * @param name
     * @param namespace
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/daemonsets/{name}", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteDeployments(@PathVariable(value = "name") String name,
                                              @RequestParam(value = "namespace", required = true) String namespace,
                                              @PathVariable(value = "clusterId") String clusterId) throws Exception {
        logger.info("删除DaemonSet");
        String userName = (String) session.getAttribute("username");
        if (StringUtils.isEmpty(userName)) {
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return daemonSetsService.deleteDaemonSet(name, namespace, userName, clusterId);
    }

    /**
     * 获取DaemonSet列表
     * @param labels
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/daemonsets", method = RequestMethod.GET)
    public ActionReturnUtil listDaemonSets(@RequestParam(value = "labels", required = false) String labels) throws Exception {
        logger.info("获取DaemonSet列表");
        return daemonSetsService.listDaemonSets(labels);
    }

    /**
     * 获取DaemonSet详情
     *
     * @param name
     * @param namespace
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/daemonsets/{name:.+}", method = RequestMethod.GET)
    public ActionReturnUtil getDaemonSetDetail(@PathVariable(value = "name") String name,
                                               @RequestParam(value = "namespace", required = true) String namespace,
                                               @PathVariable(value = "clusterId") String clusterId) throws Exception {
        logger.info("获取DaemonSet详情：{}", name);
        String userName = (String) session.getAttribute("username");
        if (StringUtils.isEmpty(userName)) {
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return daemonSetsService.getDaemonSetDetail(name, namespace, userName, clusterId);
    }

    /**
     * 获取DaemonSetPodList事件
     * @param name
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/daemonsets/{name}/pods", method = RequestMethod.GET)
    public ActionReturnUtil listDaemonSetPods(@PathVariable(value = "name") String name,
                                              @RequestParam(value = "namespace", required = true) String namespace,
                                              @PathVariable(value = "clusterId") String clusterId) throws Exception {
        logger.info("获取DaemonSet的POD列表：{}", name);
        String userName = (String) session.getAttribute("username");
        if (StringUtils.isEmpty(userName)) {
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return daemonSetsService.listPods(name, namespace, clusterId);

    }

    /**
     * 获取DaemonSetEvent事件
     * @param namespace
     * @param name
     * @param clusterId
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/daemonsets/{name}/events", method = RequestMethod.GET)
    public ActionReturnUtil listDaemontSetEvents(@PathVariable(value = "name") String name,
                                                 @RequestParam(value = "namespace", required = true) String namespace,
                                                 @PathVariable(value = "clusterId") String clusterId) throws Exception {
        logger.info("获取DaemonSet的Events：{}", name);
        String userName = (String) session.getAttribute("username");
        if (StringUtils.isEmpty(userName)) {
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return daemonSetsService.listEvents(name, namespace, clusterId);
    }

}
