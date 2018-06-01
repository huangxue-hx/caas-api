package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.log.LogQueryDto;
import com.harmonycloud.service.platform.bean.LogQuery;

import javax.servlet.http.HttpServletResponse;

/**
 * 日志Service接口
 */
public interface LogService {

    /**
     * 导出主机上的日志文件
     */
    void exportLog(String namespace, String podName, String clusterId, String logName, HttpServletResponse response) throws Exception;

    /**
     * 将es的查询结果导出txt文件
     */
    void exportLog(LogQuery logQuery,HttpServletResponse response) throws Exception;

    ActionReturnUtil fileLog(LogQuery logQuery) throws Exception;

    ActionReturnUtil listfileName(LogQuery logQuery) throws Exception;

    ActionReturnUtil getProcessLog(String rangeType, String processName, String node, String clusterId) throws Exception;

    LogQuery transLogQuery(LogQueryDto logQueryDto) throws Exception;

}
