package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.bean.LogQuery;

/**
 * es查看日志的接口
 * 
 * 
 * @author jmi
 *
 */
public interface EsService {

	ActionReturnUtil fileLog(LogQuery logQuery) throws Exception;
	
	ActionReturnUtil listfileName(String container, String namespace, String clusterId) throws Exception;
	
	ActionReturnUtil getProcessLog(String rangeType, String processName, String node) throws Exception;
	
	ActionReturnUtil listProvider() throws Exception;
}
