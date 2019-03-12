package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;

public interface MonitorCenterService {

    ActionReturnUtil getProjectMonit(String tenantId, String projectId, String namespaceList, String rangeType) throws Exception;
}
