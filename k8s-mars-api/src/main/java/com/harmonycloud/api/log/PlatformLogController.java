package com.harmonycloud.api.log;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.log.LogQueryDto;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.LogQuery;
import com.harmonycloud.service.platform.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 平台日志相关控制器，非用户创建的应用日志, webapi,oam-api,oam-task
 * @author zhangkui
 */
@Controller
@RequestMapping("/clusters/{clusterId}/namespaces/{namespace}/deploys/{deployName}/logs")
public class PlatformLogController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DeploymentsService deploymentService;

    @Autowired
    private LogService logService;
    @Autowired
    private ClusterService clusterService;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil queryLog(@PathVariable("deployName") String deployName,
                                     @PathVariable("clusterId") String clusterId,
                                     @PathVariable("namespace") String namespace,
                                     @ModelAttribute LogQueryDto logQueryDto){
        try {
            logQueryDto.setDeployment(deployName);
            logQueryDto.setClusterId(clusterId);
            logQueryDto.setNamespace(namespace);
            LogQuery logQuery = logService.transLogQuery(logQueryDto);
            return logService.fileLog(logQuery);
        }catch (IllegalArgumentException ie) {
            logger.warn("根据日志路径获取container日志参数有误", ie);
            return ActionReturnUtil.returnErrorWithData(ie.getMessage());
        }catch (Exception e) {
            logger.error("根据日志路径获取container日志失败：logQueryDto:{}",
                    logQueryDto.toString(), e);
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.UNKNOWN);
        }

    }

    /**
     * 导出查询日志
     * @param deployName
     * @param logQueryDto
     * @param response
     */
    @RequestMapping(value="/export", method= RequestMethod.GET)
    public void exportLog(@PathVariable("deployName") String deployName,
                          @PathVariable("clusterId") String clusterId,
                          @PathVariable("namespace") String namespace,
                                     @ModelAttribute LogQueryDto logQueryDto,
                                      HttpServletResponse response) throws Exception{
        logger.info("导出日志, deployName:{},params:{} ", deployName, logQueryDto.toString());
        logQueryDto.setDeployment(deployName);
        logQueryDto.setClusterId(clusterId);
        logQueryDto.setNamespace(namespace);
        LogQuery logQuery = logService.transLogQuery(logQueryDto);
        logService.exportLog(logQuery, response);
    }

    @ResponseBody
    @RequestMapping(value="/filenames", method= RequestMethod.GET)
    public ActionReturnUtil listLogFilenames(@PathVariable("deployName") String deployName,
                                             @PathVariable("clusterId") String clusterId,
                                             @PathVariable("namespace") String namespace,
                                             @ModelAttribute LogQueryDto logQueryDto) throws Exception{

        try {
            logQueryDto.setDeployment(deployName);
            logQueryDto.setClusterId(clusterId);
            logQueryDto.setNamespace(namespace);
            LogQuery logQuery = logService.transLogQuery(logQueryDto);
            logger.info("获取服务的日志文件列表");
            return logService.listfileName(logQuery);
        } catch (Exception e) {
            logger.error("获取服务日志文件列表失败：deploymentName:{}", deployName, e);
            return ActionReturnUtil.returnErrorWithMsg(e.getMessage());
        }

    }

}
