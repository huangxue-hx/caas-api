package com.harmonycloud.api.log;


import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.EsSearchTypeEnum;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.log.FullLinkQueryDto;
import com.harmonycloud.service.platform.bean.LogQuery;
import com.harmonycloud.service.platform.service.FullLinkLogService;
import com.harmonycloud.service.platform.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import static com.harmonycloud.common.Constant.CommonConstant.DEFAULT_PAGE_SIZE_200;

/**
 * 全链路日志相关控制器
 * @author zhangkui
 */
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/apps/{appName}/linklogs")
public class FullLinkLogController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    FullLinkLogService linkLogService;

    @Autowired
    LogService logService;

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
            @RequestParam(value="transactionId") String transactionId,
            @RequestParam(value="pod",required = false) String pod) throws Exception{
        if(transactionId.indexOf(CommonConstant.AT) == -1 || transactionId.indexOf(CommonConstant.COLON) == -1){
            logger.error("transactionId：{} 格式错误", transactionId);
            return ActionReturnUtil.returnErrorWithData("transactionId", ErrorCodeMessage.FORMAT_ERROR);
        }
        LogQuery logQuery = new LogQuery();
        logQuery.setNamespace(namespace);
        logQuery.setDeployment(deployName);
        logQuery.setPod(pod);
        logQuery.setSearchType(EsSearchTypeEnum.MATCH_PHRASE.getCode());
        logQuery.setSearchWord(transactionId);
        logQuery.setPageSize(size==null?DEFAULT_PAGE_SIZE_200:size);
        logQuery.setScrollId(scrollId);
        ActionReturnUtil result = logService.fileLog(logQuery);
        return result;
    }



}
