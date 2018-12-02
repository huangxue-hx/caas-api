package com.harmonycloud.api.log;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dto.log.LogQueryDto;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.platform.bean.LogQuery;
import com.harmonycloud.service.platform.service.LogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 应用日志相关控制器
 * @author zhangkui
 */
@Controller
@Api(description = "应用日志相关控制器")
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/deploys/{deployName}/applogs")
public class LogController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DeploymentsService deploymentService;

    @Autowired
    private LogService logService;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil queryLog(@PathVariable("deployName") String deployName,
                                     @ModelAttribute LogQueryDto logQueryDto){
        try {
            logger.info("查询文件日志内容,logQuery:{}",JSONObject.toJSONString(logQueryDto));
            logQueryDto.setDeployment(deployName);
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
                                     @ModelAttribute LogQueryDto logQueryDto,
                                      HttpServletResponse response) throws Exception{
//        logger.info("导出日志, deployName:{},params:{} ", deployName, logQueryDto.toString());
        logQueryDto.setDeployment(deployName);
        LogQuery logQuery = logService.transLogQuery(logQueryDto);
        logService.exportLog(logQuery, response);
    }

    @ResponseBody
    @RequestMapping(value="/filenames", method= RequestMethod.GET)
    public ActionReturnUtil listLogFilenames(@PathVariable("deployName") String deployName,
                                             @ModelAttribute LogQueryDto logQueryDto) throws Exception{

        try {
            logQueryDto.setDeployment(deployName);
            logger.info("获取服务的日志文件列表,logQuery:{}",JSONObject.toJSONString(logQueryDto));
            LogQuery logQuery = logService.transLogQuery(logQueryDto);
            return logService.listfileName(logQuery);
        } catch (Exception e) {
            logger.error("获取服务日志文件列表失败：deploymentName:{}", deployName, e);
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
    @RequestMapping(value="/stderrlogs", method= RequestMethod.GET)
    public ActionReturnUtil getPodAppLog(@RequestParam(value="pod") String pod,
                                         @RequestParam(value="namespace") String namespace,
                                         @RequestParam(value="container", required=false) String container,
                                         @RequestParam(value="recentTimeNum", required=false) Integer recentTimeNum,
                                         @RequestParam(value="recentTimeUnit", required=false) String recentTimeUnit,
                                         @RequestParam(value="clusterId", required=false) String clusterId){
        try {
            Integer sinceSeconds = DateUtil.getSinceSeconds(recentTimeNum, recentTimeUnit);
            return deploymentService.getPodAppLog(namespace, container, pod, sinceSeconds, clusterId);
        }catch(IllegalArgumentException e){
            logger.error("获取pod的应用日志失败",e);
            return ActionReturnUtil.returnErrorWithData(e.getMessage());
        }catch(MarsRuntimeException e){
            logger.error("获取pod的应用日志失败",e);
            return ActionReturnUtil.returnErrorWithData(e.getMessage());
        }catch (Exception e) {
            logger.error("获取pod的应用日志失败",e);
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.UNKNOWN);
        }
    }

    @ApiOperation(value = "获取日志目录下的列表", notes = "根据条件过滤查询集群列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pod", value = "pod容器", paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "命名空间", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "path", value = "日志目录", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query", dataType = "String") })
    @ResponseBody
    @RequestMapping(value="/containerfiles", method= RequestMethod.GET)
    public ActionReturnUtil queryLogFile(@RequestParam(value="pod") String pod,
                                         @RequestParam(value="container", required = false) String container,
                                         @RequestParam(value="namespace") String namespace,
                                         @RequestParam(value="path") String path,
                                         @RequestParam(value="clusterId") String clusterId) throws Exception{
       List<String> logfile =  logService.queryLogFile(pod,container,namespace,path,clusterId);

       return ActionReturnUtil.returnSuccessWithData(logfile);

    }


}
