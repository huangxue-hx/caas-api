package com.harmonycloud.api.application;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dto.log.LogQueryDto;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.ApplicationDeployService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.platform.bean.LogQuery;
import com.harmonycloud.service.platform.bean.UpdateDeployment;
import com.harmonycloud.service.platform.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Author jiangmi
 * @Description 针对普通应用以外的相关接口
 * @Date created in 2018-1-31
 * @Modified
 */
@Controller
@RequestMapping("/tenants/{tenantId}")
public class MsfApplicationController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    HttpSession session;

    @Autowired
    ApplicationDeployService applicationDeployService;

    @Autowired
    DeploymentsService dpService;

    @Autowired
    LogService logService;

    @Autowired
    RouterService routerService;

    @ResponseBody
    @RequestMapping(value = "/msf/apps/{appName}", method = RequestMethod.GET)
    public ActionReturnUtil getApplicationDetail(@RequestParam(value = "id", required = true) String id,
                                                 @PathVariable(value = "appName") String appName,
                                                 @RequestParam(value = "namespace", required = false) String namespace) throws Exception {
        logger.info("获取微服务组件应用详情");
        return applicationDeployService.selectApplicationById(id, appName, namespace);
    }

    @ResponseBody
    @RequestMapping(value = "/msf/deploys/{deployName}", method = RequestMethod.GET)
    public ActionReturnUtil deploymentDetail(@PathVariable(value = "deployName") String name,
                                             @RequestParam(value = "namespace", required = true) String namespace) throws Exception {

        logger.info("获取微服务组件服务详情");
        return dpService.getDeploymentDetail(namespace, name);
    }

    @ResponseBody
    @RequestMapping(value = "/msf/deploys/{deployName}/start", method = RequestMethod.POST)
    public ActionReturnUtil startDeployment(@PathVariable(value = "deployName") String name,
                                            @RequestParam(value = "namespace", required = true) String namespace) throws Exception {
        logger.info("启动微服务组件服务");
        String userName = (String) session.getAttribute("username");
        if (userName == null) {
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return dpService.startDeployments(name, namespace, userName);

    }

    @ResponseBody
    @RequestMapping(value = "/msf/deploys/{deployName}/stop", method = RequestMethod.POST)
    public ActionReturnUtil stopDeployment(@PathVariable(value = "deployName") String name,
                                           @RequestParam(value = "namespace", required = true) String namespace) throws Exception {
        logger.info("停止微服务组件服务");
        String userName = (String) session.getAttribute("username");
        if (userName == null) {
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return dpService.stopDeployments(name, namespace, userName);
    }

    @ResponseBody
    @RequestMapping(value = "/msf/deploys/{deployName}/scale", method = RequestMethod.POST)
    public ActionReturnUtil scaleDeployment(@PathVariable(value = "deployName") String name,
                                            @RequestParam(value = "namespace", required = true) String namespace,
                                            @RequestParam(value = "scale") Integer scale) throws Exception {
        logger.info("改变微服务组件服务实例数量");
        String userName = (String) session.getAttribute("username");
        if (userName == null) {
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return dpService.scaleDeployment(namespace, name, scale, userName);
    }

    @ResponseBody
    @RequestMapping(value = "/msf/deploys/{deployName}/pods", method = RequestMethod.GET)
    public ActionReturnUtil podList(@PathVariable(value = "deployName") String name,
                                    @RequestParam(value = "namespace") String namespace) throws Exception {

        logger.info("获取微服务组件服务的pod列表");
        String userName = (String) session.getAttribute("username");
        if (userName == null) {
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return dpService.podList(name, namespace);

    }

    @ResponseBody
    @RequestMapping(value = "/msf/deploys/{deployName}/events", method = RequestMethod.GET)
    public ActionReturnUtil getAppEvents(@PathVariable(value = "deployName") String name,
                                         @RequestParam(value = "namespace", required = true) String namespace,
                                         @RequestParam(value = "clusterId", required = false) String clusterId) throws Exception {

        logger.info("获取微服务组件服务的事件");
        String userName = (String) session.getAttribute("username");
        if (userName == null) {
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return dpService.getDeploymentEvents(namespace, name);
    }

    @ResponseBody
    @RequestMapping(value = "/msf/deploys/{deployName}/containers", method = RequestMethod.GET)
    public ActionReturnUtil getDeploymentContainer(@PathVariable(value = "deployName") String name,
                                                   @RequestParam(value = "namespace", required = true) String namespace) throws Exception {
        logger.info("获取微服务组件pod的cantainer");
        String userName = (String) session.getAttribute("username");
        if (userName == null) {
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return dpService.deploymentContainer(namespace, name);
    }

    @ResponseBody
    @RequestMapping(value = "/msf/deploys/{deployName}", method = RequestMethod.PUT)
    public ActionReturnUtil updateDeployments(@ModelAttribute UpdateDeployment deploymentDetail) throws Exception {
        logger.info("更新微服务组件服务");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return dpService.updateAppDeployment(deploymentDetail,userName);
    }

    @ResponseBody
    @RequestMapping(value="/msf/deploys/{deployName}/applogs/filenames", method= RequestMethod.GET)
    public ActionReturnUtil listLogFilenames(@PathVariable("deployName") String deployName,
                                             @ModelAttribute LogQueryDto logQueryDto) throws Exception{

        try {
            logger.info("获取微服务组件服务的日志文件列表");
            logQueryDto.setDeployment(deployName);
            LogQuery logQuery = logService.transLogQuery(logQueryDto);
            return logService.listfileName(logQuery);
        } catch (Exception e) {
            logger.error("获取微服务组件服务日志文件列表失败：deploymentName:{}", deployName, e);
            return ActionReturnUtil.returnErrorWithMsg(e.getMessage());
        }

    }

    /**
     * 调k8s api获取标准输出日志
     * @param pod
     * @param namespace
     * @param container
     * @param recentTimeNum
     * @param recentTimeUnit
     * @param clusterId
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/msf/deploys/{deployName}/applogs/stderrlogs", method= RequestMethod.GET)
    public ActionReturnUtil getPodAppLog(@RequestParam(value="pod") String pod,
                                         @RequestParam(value="namespace") String namespace,
                                         @RequestParam(value="container", required=false) String container,
                                         @RequestParam(value="recentTimeNum", required=false) Integer recentTimeNum,
                                         @RequestParam(value="recentTimeUnit", required=false) String recentTimeUnit,
                                         @RequestParam(value="clusterId", required=false) String clusterId){
        try {
            Integer sinceSeconds = DateUtil.getSinceSeconds(recentTimeNum, recentTimeUnit);
            return dpService.getPodAppLog(namespace, container, pod, sinceSeconds, clusterId);
        }catch(IllegalArgumentException e){
            logger.error("获取微服务组件pod的应用日志失败",e);
            return ActionReturnUtil.returnErrorWithData(e.getMessage());
        }catch(MarsRuntimeException e){
            logger.error("获取微服务组件pod的应用日志失败",e);
            return ActionReturnUtil.returnErrorWithData(e.getMessage());
        }catch (Exception e) {
            logger.error("获取微服务组件pod的应用日志失败",e);
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.UNKNOWN);
        }
    }

    @ResponseBody
    @RequestMapping(value="/msf/deploys/{deployName}/applogs/logfile/{fileName}/export", method= RequestMethod.GET)
    public void exportLog(@RequestParam(value="namespace") String namespace,
                          @RequestParam(value="pod") String podName,
                          @RequestParam(value="clusterId") String clusterId,
                          @PathVariable("fileName") String fileName, HttpServletResponse response) throws Exception{
        logService.exportLog(namespace, podName, clusterId, fileName, response);
    }

    @ResponseBody
    @RequestMapping(value = "/msf/deploys/rules", method = RequestMethod.GET)
    public ActionReturnUtil listRouter(@RequestParam(value = "namespace") String namespace,
                                       @RequestParam(value = "nameList") String nameList) throws Exception{
        return routerService.listExposedRouterWithIngressAndNginx(namespace, nameList);
    }

    @ResponseBody
    @RequestMapping(value="/msf/deploys/{deployName}/applogs", method = RequestMethod.GET)
    public ActionReturnUtil queryLog(@PathVariable("deployName") String deployName,
                                     @ModelAttribute LogQueryDto logQueryDto){
        try {
            logger.info("根据日志路径获取微服务组件container日志, params: " + logQueryDto.toString());
            logQueryDto.setDeployment(deployName);
            LogQuery logQuery = logService.transLogQuery(logQueryDto);
            logQuery.setMathPhrase(true);
            return logService.fileLog(logQuery);
        }catch (IllegalArgumentException ie) {
            logger.warn("根据日志路径获取微服务组件container日志参数有误", ie);
            return ActionReturnUtil.returnErrorWithData(ie.getMessage());
        }catch (Exception e) {
            logger.error("根据日志路径获取微服务组件container日志失败：logQueryDto:{}",
                    logQueryDto.toString(), e);
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.UNKNOWN);
        }

    }

}

