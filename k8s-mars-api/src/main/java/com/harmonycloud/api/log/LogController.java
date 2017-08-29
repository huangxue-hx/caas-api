package com.harmonycloud.api.log;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import com.harmonycloud.common.exception.MarsRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dto.log.LogQueryDto;
import com.harmonycloud.service.platform.bean.ContainerLog;
import com.harmonycloud.service.platform.bean.LogQuery;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.EsService;

/**
 * 日志相关控制器
 * @author zhangkui
 */
@Controller
@RequestMapping("/log")
public class LogController {

    private static final int MAX_PAGE_SIZE = 1000;
    private static final int DEFAULT_PAGE_SIZE = 200;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    EsService esService;

    @Autowired
    DeploymentsService deploymentService;

    @ResponseBody
    @RequestMapping(value="/logfile/search", method = RequestMethod.POST)
    public ActionReturnUtil searchContainerLog(@RequestBody LogQueryDto logQueryDto){
        try {
            logger.info("根据日志路径获取container日志, params: " + logQueryDto.toString());
            return esService.fileLog(this.transLogQuery(logQueryDto));
        }catch (IllegalArgumentException ie) {
            logger.warn("根据日志路径获取container日志参数有误", ie);
            return ActionReturnUtil.returnErrorWithData(ie.getMessage());
        }catch (Exception e) {
            logger.error("根据日志路径获取container日志失败：logQueryDto:{}",
                    logQueryDto.toString(), e.getMessage());
            return ActionReturnUtil.returnErrorWithData("未知异常");
        }

    }

    @ResponseBody
    @RequestMapping(value="/logfile/list", method= RequestMethod.GET)
    public ActionReturnUtil listContainerFile(@RequestParam(value="container") String container,
                                              @RequestParam(value="namespace", required=true) String namespace,
                                              @RequestParam(value="clusterId", required=false) String clusterId) throws Exception{

        try {
            logger.info("获取container的日志文件列表");
            return esService.listfileName(container, namespace, clusterId);
        } catch (Exception e) {
            logger.error("获取容器日志文件列表失败：namespace:{}", container,e);
            return ActionReturnUtil.returnErrorWithMsg(e.getMessage());
        }

    }

    @ResponseBody
    @RequestMapping(value="/container/logs", method= RequestMethod.GET)
    public ActionReturnUtil getPodAppLog(@RequestParam(value="pod") String pod,
                                         @RequestParam(value="namespace") String namespace,
                                         @RequestParam(value="container", required=false) String container,
                                         @RequestParam(value="recentTimeNum", required=false) Integer recentTimeNum,
                                         @RequestParam(value="recentTimeUnit", required=false) String recentTimeUnit,
                                         @RequestParam(value="clusterId", required=false) String clusterId){
        try {
            Integer sinceSeconds = this.getSinceSeconds(recentTimeNum, recentTimeUnit);
            return deploymentService.getPodAppLog(namespace, container, pod, sinceSeconds, clusterId);
        }catch(IllegalArgumentException e){
            logger.error("获取pod的应用日志失败",e);
            return ActionReturnUtil.returnErrorWithData(e.getMessage());
        }catch(MarsRuntimeException e){
            logger.error("获取pod的应用日志失败",e);
            return ActionReturnUtil.returnErrorWithData(e.getMessage());
        }catch (Exception e) {
            logger.error("获取pod的应用日志失败",e);
            return ActionReturnUtil.returnErrorWithData("未知异常");
        }
    }

    /**
     * 参数校验，并将接口日志查询对象转换为内部服务查询对象
     * @param logQueryDto 对外接口日志查询对象
     * @return 内部服务日志查询对象
     */
    private LogQuery transLogQuery(LogQueryDto logQueryDto) throws IllegalArgumentException{
        if(StringUtils.isNotBlank(logQueryDto.getScrollId())){
            LogQuery logQuery = new LogQuery();
            logQuery.setScrollId(logQueryDto.getScrollId());
            return logQuery;
        }
        Assert.notNull(logQueryDto,"query params cannot be null");
        Assert.hasText(logQueryDto.getContainer(),"container cannot be null");
        Assert.hasText(logQueryDto.getNamespace(),"namespace cannot be null");
        Assert.hasText(logQueryDto.getLogDir(),"logDir cannot be null");
        String fromDate = "";
        String toDate ="";
        if(logQueryDto.getRecentTimeNum() != null && logQueryDto.getRecentTimeNum() != 0){
            Assert.hasText(logQueryDto.getRecentTimeUnit(),"recentTimeUnit cannot be null");
            SimpleDateFormat format = new SimpleDateFormat(DateStyle.YYYY_MM_DD_T_HH_MM_SS_0000.getValue());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date current = new Date();
            Date from = DateUtil.addTime(current, logQueryDto.getRecentTimeUnit(),
                    -logQueryDto.getRecentTimeNum());
            fromDate = format.format(from);
            toDate = format.format(current);
        }else {
            Assert.hasText(logQueryDto.getLogTimeStart(),"LogTimeStart cannot be null");
            Assert.hasText(logQueryDto.getLogTimeEnd(),"LogTimeEnd cannot be null");
            //前端传过来的时间是标准时间UTC
            if(logQueryDto.getLogTimeStart().length()>20) {
                fromDate = DateUtil.StringToString(logQueryDto.getLogTimeStart(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z_SSS,
                        DateStyle.YYYY_MM_DD_T_HH_MM_SS_0000);
                toDate = DateUtil.StringToString(logQueryDto.getLogTimeEnd(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z_SSS,
                        DateStyle.YYYY_MM_DD_T_HH_MM_SS_0000);
                if (fromDate == null || toDate == null) {
                    throw new IllegalArgumentException("日期时间格式错误");
                }
            }else {
                //cst时间
                Date from = DateUtil.StringToDate(logQueryDto.getLogTimeStart(), DateStyle.YYYY_MM_DD_HH_MM_SS);
                Date to = DateUtil.StringToDate(logQueryDto.getLogTimeEnd(), DateStyle.YYYY_MM_DD_HH_MM_SS);
                from = DateUtil.addHour(from, -8);
                to = DateUtil.addHour(to, -8);
                fromDate = DateUtil.DateToString(from, DateStyle.YYYY_MM_DD_T_HH_MM_SS_0000);
                toDate = DateUtil.DateToString(to,  DateStyle.YYYY_MM_DD_T_HH_MM_SS_0000);
            }
        }
        logger.info("Query log time, fromDate:{},toDate:{}", fromDate, toDate);
        LogQuery logQuery = new LogQuery();
        BeanUtils.copyProperties(logQueryDto,logQuery);
        logQuery.setLogDateStart(fromDate);
        logQuery.setLogDateEnd(toDate);
        if(logQueryDto.getPageSize() == null){
            logQuery.setPageSize(DEFAULT_PAGE_SIZE);
        }
        if(logQueryDto.getPageSize() > MAX_PAGE_SIZE){
            logQuery.setPageSize(MAX_PAGE_SIZE);
        }
        return logQuery;
    }

    private Integer getSinceSeconds(Integer num, String timeUnit){
        if(num == null || StringUtils.isBlank(timeUnit)){
            return null;
        }
        Integer sinceSeconds = 0;
        String unit = timeUnit.toLowerCase();
        switch (unit){
            case "m":
                sinceSeconds = 60 * num;
                break;
            case "h":
                sinceSeconds = 60 * 60 * num;
                break;
            case "d":
                sinceSeconds = 60 * 60 * 24 * num;
                break;
            default:
                sinceSeconds = 0;
        }
        return sinceSeconds;
    }

}
