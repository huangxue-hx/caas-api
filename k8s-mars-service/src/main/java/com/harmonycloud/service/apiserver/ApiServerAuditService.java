package com.harmonycloud.service.apiserver;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.apiserver.ApiServerAuditSearchDto;

/**
 * @author liangli
 */
public interface ApiServerAuditService {

    ActionReturnUtil searchByQuery(ApiServerAuditSearchDto searchDto) throws Exception;

    ActionReturnUtil getAuditCount(ApiServerAuditSearchDto userAuditSearch) throws Exception;
    ActionReturnUtil getAuditLogsNamespace() throws Exception;

    ActionReturnUtil getUrlHistogram(String clusterId, String verbName, String url, String rangeType) throws Exception;
}
