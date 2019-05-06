package com.harmonycloud.api.log;


import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.enumm.EsSearchTypeEnum;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dto.log.FullLinkQueryDto;
import com.harmonycloud.dto.log.LogQueryDto;
import com.harmonycloud.service.platform.bean.LogQuery;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.FullLinkLogService;
import com.harmonycloud.service.platform.service.LogService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.TimeZone;

import static com.harmonycloud.common.Constant.CommonConstant.DEFAULT_PAGE_SIZE_200;
import static com.harmonycloud.common.Constant.CommonConstant.NUM_TWO;

/**
 * 全链路日志相关控制器
 * @author zhangkui
 */
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/apps/{appName}/linklogs")
public class FullLinkLogController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FullLinkLogService linkLogService;

    @Autowired
    private LogService logService;

    @ResponseBody
    @RequestMapping(value="/pod", method= RequestMethod.GET)
    public ActionReturnUtil listPod(@PathVariable("appName") String appName,
                                    @ModelAttribute FullLinkQueryDto queryDto){
        queryDto.setAppName(appName);
        return linkLogService.listPod(queryDto);
    }

    @ResponseBody
    @RequestMapping(value="/erroranalysis", method = RequestMethod.GET)
    public ActionReturnUtil getErrorAnalysis(@ModelAttribute FullLinkQueryDto queryDto){
        logger.debug("查询全链路日志错误路径参数: queryDto:{}", JSONObject.toJSONString(queryDto));
        return linkLogService.errorAnalysis(queryDto);
    }

    @ResponseBody
    @RequestMapping(value="/errortransactions", method = RequestMethod.GET)
    public ActionReturnUtil getErrorTransactions(@ModelAttribute FullLinkQueryDto queryDto){
        logger.debug("查询全链路日志业务信息参数:{} ",JSONObject.toJSONString(queryDto));
        return linkLogService.errorTransactions(queryDto);
    }

    @ResponseBody
    @RequestMapping(value="/transactiontraces/{transactionId}", method = RequestMethod.GET)
    public ActionReturnUtil getTransactionTrace(@PathVariable("transactionId") String transactionId){
        return linkLogService.transactionTrace(transactionId);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil getLinkLogInfo(
            @RequestParam(value="deployName") String deployName,
            @RequestParam(value="namespace") String namespace,
            @RequestParam(value="size",required = false) Integer size,
            @RequestParam(value="scrollId",required = false) String scrollId,
            @RequestParam(value="clusterId",required = false) String clusterId,
            @RequestParam(value="transactionId") String transactionId,
            @RequestParam(value="fromTime",required = false) String fromTime,
            @RequestParam(value="pod",required = false) String pod) throws Exception{
        LogQueryDto logQueryDto = new LogQueryDto();
        logQueryDto.setNamespace(namespace);
        logQueryDto.setClusterId(clusterId);
        logQueryDto.setAppName(deployName);
        logQueryDto.setAppType(Constant.DEPLOYMENT);
        logQueryDto.setPod(pod);
        logQueryDto.setSearchType(EsSearchTypeEnum.MATCH_PHRASE.getCode());
        logQueryDto.setSearchWord(transactionId);
        logQueryDto.setPageSize(size==null?DEFAULT_PAGE_SIZE_200:size);
        logQueryDto.setScrollId(scrollId);
        logQueryDto.setLogTimeStart(fromTime);
        formatQueryTime(logQueryDto);
        LogQuery logQuery = logService.transLogQuery(logQueryDto);
        ActionReturnUtil result = logService.fileLog(logQuery);
        return result;
    }

    /**
     * 根据transactionId的开始时间计算查询日志内容的区间，为前后两小时
     * @param logQueryDto
     * @return
     */
    private void formatQueryTime(LogQueryDto logQueryDto){
        if(StringUtils.isBlank(logQueryDto.getLogTimeStart())){
            return;
        }
        Date fromTime = DateUtil.StringToDate(logQueryDto.getLogTimeStart(),DateStyle.YYYY_MM_DD_HH_MM_SS);
        Date toTime = DateUtil.addHour(fromTime, NUM_TWO);
        fromTime = DateUtil.addHour(fromTime, -NUM_TWO);
        String style = DateUtil.getTimezoneFormatStyle(TimeZone.getDefault());
        logQueryDto.setLogTimeStart(DateUtil.DateToString(fromTime, style));
        logQueryDto.setLogTimeEnd(DateUtil.DateToString(toTime, style));
    }



}
