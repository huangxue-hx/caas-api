package com.harmonycloud.service.user;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.SearchResult;
import com.harmonycloud.common.util.UserAuditSearch;
import com.harmonycloud.dto.config.AuditRequestInfo;
import org.elasticsearch.index.query.BoolQueryBuilder;

import java.io.IOException;

/**
 * Created by czm on 2017/3/29.
 */
public interface UserAuditService {
    ActionReturnUtil searchByQuery(UserAuditSearch userAuditSearch) throws Exception;

    ActionReturnUtil searchModule(String username) throws Exception;
    
    ActionReturnUtil getAuditCount(UserAuditSearch userAuditSearch) throws Exception;

    ActionReturnUtil insertToEsIndex(AuditRequestInfo auditRequestInfo) throws Exception;

}
