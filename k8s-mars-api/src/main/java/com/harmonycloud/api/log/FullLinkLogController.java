package com.harmonycloud.api.log;


import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.EsSearchTypeEnum;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dto.log.FullLinkQueryDto;
import com.harmonycloud.service.platform.bean.LogQuery;
import com.harmonycloud.service.platform.service.FullLinkLogService;
import com.harmonycloud.service.platform.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

import static com.harmonycloud.common.Constant.CommonConstant.DEFAULT_PAGE_SIZE_20;
import static com.harmonycloud.common.Constant.CommonConstant.ONE_WEEK_DAYS;

/**
 * 全链路日志相关控制器
 * @author zhangkui
 */
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/deploys/{deployName}/linklogs")
public class FullLinkLogController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    FullLinkLogService linkLogService;

    @Autowired
    LogService logService;

    @ResponseBody
    @RequestMapping(value="/pod", method= RequestMethod.GET)
    public ActionReturnUtil listPod(@PathVariable("deployName") String deployName,
                                    @RequestParam(value="namespace") String namespace) throws Exception{
        try {
            FullLinkQueryDto queryDto = new FullLinkQueryDto();
            queryDto.setNamespace(namespace);
            queryDto.setDeployment(deployName);
            Date to = new Date();
            Date from = DateUtil.addDay(to, -ONE_WEEK_DAYS);
            queryDto.setFromTime(DateUtil.DateToString(from, DateStyle.YYYY_MM_DD_HH_MM_SS.getValue()));
            queryDto.setToTime(DateUtil.DateToString(to, DateStyle.YYYY_MM_DD_HH_MM_SS.getValue()));
            return linkLogService.listPod(queryDto);
        } catch (Exception e) {
            logger.error("获取服务全链路日志失败.",  e);
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.LIST_LOG_ERROR);
        }

    }

    @ResponseBody
    @RequestMapping(value="/erroranalysis", method = RequestMethod.GET)
    public ActionReturnUtil getErrorAnalysis(@PathVariable("deployName") String deployName,
                                             @RequestBody FullLinkQueryDto queryDto){
        try {
            queryDto.setDeployment(deployName);
            logger.debug("查询全链路日志错误路径参数: queryDto:{}", JSONObject.toJSONString(queryDto));
            return linkLogService.errorAnalysis(queryDto);
        }catch (IllegalArgumentException ie) {
            logger.warn("查询全链路日志错误路径参数有误", ie);
            return ActionReturnUtil.returnErrorWithData(ie.getMessage());
        }catch (Exception e) {
            logger.error("查询全链路日志错误路径失败：queryDto:{}",
                    queryDto.toString(), e.getMessage());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.UNKNOWN);
        }

    }

    @ResponseBody
    @RequestMapping(value="/errortransactions", method = RequestMethod.GET)
    public ActionReturnUtil getErrorTransactions(@PathVariable("deployName") String deployName,
                                                 @RequestBody FullLinkQueryDto queryDto){
        try {
            queryDto.setDeployment(deployName);
            logger.debug("查询全链路日志业务信息参数: " + JSONObject.toJSONString(queryDto));
            return linkLogService.errorTransactions(queryDto);
        }catch (IllegalArgumentException ie) {
            logger.warn("查询全链路日志业务信息参数有误", ie);
            return ActionReturnUtil.returnErrorWithData(ie.getMessage());
        }catch (Exception e) {
            logger.error("查询全链路日志业务信息失败：queryDto:{}",
                    queryDto.toString(), e.getMessage());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.UNKNOWN);
        }

    }

    @ResponseBody
    @RequestMapping(value="/transactiontraces/{transactionId}", method = RequestMethod.GET)
    public ActionReturnUtil getTransactionTrace(@PathVariable("transactionId") String transactionId){
        try {
            return linkLogService.transactionTrace(transactionId);
        }catch (Exception e) {
            logger.error("查询全链路拓扑图失败：transactionId:{}",
                    transactionId, e.getMessage());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.QUERY_LOG_TOPOLOGY_ERROR);
        }

    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil getLinkLogInfo(
            @PathVariable("deployName") String deployName,
            @RequestParam(value="namespace") String namespace,
            @RequestParam(value="size",required = false) Integer size,
            @RequestParam(value="scrollId",required = false) String scrollId,
            @RequestParam(value="transactionId") String transactionId,
            @RequestParam(value="pod",required = false) String pod){
        try {
            if(transactionId.indexOf(CommonConstant.AT) == -1 || transactionId.indexOf(CommonConstant.COLON) == -1){
                logger.error("transactionId：{} 格式错误", transactionId);
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FORMAT_ERROR, "transactionId", true);
            }
            LogQuery logQuery = new LogQuery();
            logQuery.setNamespace(namespace);
            logQuery.setDeployment(deployName);
            logQuery.setPod(pod);
            logQuery.setSearchType(EsSearchTypeEnum.MATCH_PHRASE.getCode());
            logQuery.setSearchWord(transactionId);
            logQuery.setPageSize(size==null?DEFAULT_PAGE_SIZE_20:size);
            logQuery.setScrollId(scrollId);
            ActionReturnUtil result = logService.fileLog(logQuery);
            return result;
        }catch (Exception e) {
            logger.error("获取全链路日志内容失败：transactionId:{}",
                    transactionId, e);
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.QUERY_LOG_CONTENT_ERROR);
        }

    }



}
