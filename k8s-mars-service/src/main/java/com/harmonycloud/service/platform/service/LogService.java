package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.log.LogQueryDto;
import com.harmonycloud.service.platform.bean.LogQuery;
import org.springframework.web.socket.WebSocketSession;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 日志Service接口
 */
public interface LogService {

    /**
     * 标准输出
     */
    public  static final String LOG_TYPE_STDOUT = "0";

    /**
     * 日志文件
     */
    public static final String LOG_TYPE_LOGFILE = "1";

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

    List<String> queryLogFile(String pod, String namespace, String path, String clusterId);

}
