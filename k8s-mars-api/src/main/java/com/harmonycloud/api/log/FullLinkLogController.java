package com.harmonycloud.api.log;


import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dto.log.FullLinkQueryDto;
import com.harmonycloud.service.application.EsService;
import com.harmonycloud.service.platform.bean.LogQuery;
import com.harmonycloud.service.platform.service.FullLinkLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 全链路相关控制器
 * @author zhangkui
 */
@Controller
@RequestMapping("/fulllink")
public class FullLinkLogController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    FullLinkLogService linkLogService;

    @Autowired
    EsService esService;

    @ResponseBody
    @RequestMapping(value="/podlist", method= RequestMethod.GET)
    public ActionReturnUtil list(@RequestParam(value="namespace") String namespace,
                                 @RequestParam(value="deployment") String deployment) throws Exception{
        try {
            FullLinkQueryDto queryDto = new FullLinkQueryDto();
            queryDto.setNamespace(namespace);
            queryDto.setDeployment(deployment);
            Date to = new Date();
            Date from = DateUtil.addDay(to, -7);
            queryDto.setFromTime(DateUtil.DateToString(from, DateStyle.YYYY_MM_DD_HH_MM_SS.getValue()));
            queryDto.setToTime(DateUtil.DateToString(to, DateStyle.YYYY_MM_DD_HH_MM_SS.getValue()));
            return linkLogService.listPod(queryDto);
        } catch (Exception e) {
            logger.error("获取服务全链路日志失败.",  e);
            return ActionReturnUtil.returnErrorWithData("获取服务日志列表失败");
        }

    }

    @ResponseBody
    @RequestMapping(value="/errorAnalysis", method = RequestMethod.POST)
    public ActionReturnUtil errorAnalysis(@RequestBody FullLinkQueryDto queryDto){
        try {
            logger.debug("查询全链路日志错误路径参数: " + JSONObject.toJSONString(queryDto));
            return linkLogService.errorAnalysis(queryDto);
        }catch (IllegalArgumentException ie) {
            logger.warn("查询全链路日志错误路径参数有误", ie);
            return ActionReturnUtil.returnErrorWithData(ie.getMessage());
        }catch (Exception e) {
            logger.error("查询全链路日志错误路径失败：queryDto:{}",
                    queryDto.toString(), e.getMessage());
            return ActionReturnUtil.returnErrorWithData("未知异常");
        }

    }

    @ResponseBody
    @RequestMapping(value="/errorTransactions", method = RequestMethod.POST)
    public ActionReturnUtil errorTransactions(@RequestBody FullLinkQueryDto queryDto){
        try {
            logger.debug("查询全链路日志业务信息参数: " + JSONObject.toJSONString(queryDto));
            return linkLogService.errorTransactions(queryDto);
        }catch (IllegalArgumentException ie) {
            logger.warn("查询全链路日志业务信息参数有误", ie);
            return ActionReturnUtil.returnErrorWithData(ie.getMessage());
        }catch (Exception e) {
            logger.error("查询全链路日志业务信息失败：queryDto:{}",
                    queryDto.toString(), e.getMessage());
            return ActionReturnUtil.returnErrorWithData("未知异常");
        }

    }

    @ResponseBody
    @RequestMapping(value="/transactionTrace", method = RequestMethod.POST)
    public ActionReturnUtil transactionTrace(@RequestParam(value="transactionId") String transactionId){
        try {
            return linkLogService.transactionTrace(transactionId);
        }catch (Exception e) {
            logger.error("查询全链路拓扑图失败：transactionId:{}",
                    transactionId, e.getMessage());
            return ActionReturnUtil.returnErrorWithData("查询全链路拓扑图失败");
        }

    }

    @ResponseBody
    @RequestMapping(value="/loginfo", method = RequestMethod.POST)
    public ActionReturnUtil loginfo(
            @RequestParam(value="namespace") String namespace,
            @RequestParam(value="transactionId") String transactionId){
        try {
            if(transactionId.indexOf("@") == -1 || transactionId.indexOf(":") == -1){
                logger.error("transactionId：{} 格式错误", transactionId);
            }
            LogQuery logQuery = new LogQuery();
            logQuery.setNamespace(namespace);
            logQuery.setPod(transactionId.substring(transactionId.indexOf("@")+1, transactionId.indexOf(":")));
            logQuery.setSearchWord(transactionId);
            ActionReturnUtil result = esService.fileLog(logQuery);
            return result;
        }catch (Exception e) {
            logger.error("获取全链路日志内容失败：transactionId:{}",
                    transactionId, e.getMessage());
            return ActionReturnUtil.returnErrorWithData("获取全链路日志内容失败");
        }

    }



}
