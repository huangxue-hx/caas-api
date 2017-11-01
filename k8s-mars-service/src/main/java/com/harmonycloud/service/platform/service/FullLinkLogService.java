package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.log.FullLinkQueryDto;
import com.harmonycloud.dto.log.LogQueryDto;

import java.util.List;

/**
 * 日志监控接口
 * @author jmi
 *
 */
public interface FullLinkLogService {

	ActionReturnUtil listPod(FullLinkQueryDto queryDto);

	ActionReturnUtil errorAnalysis(FullLinkQueryDto queryDto);

	ActionReturnUtil errorTransactions(FullLinkQueryDto queryDto);

	ActionReturnUtil transactionTrace(String transactionId);
	
}
