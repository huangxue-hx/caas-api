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
    ActionReturnUtil serachByQuery(UserAuditSearch userAuditSearch) throws Exception;

    ActionReturnUtil serachByUserName(String username) throws Exception;

    ActionReturnUtil serachByModule(String username, String module, boolean isAdmin) throws Exception;

    ActionReturnUtil serachAuditsByUser(String username, boolean isAdmin) throws Exception;
    
    ActionReturnUtil getAuditCount(UserAuditSearch userAuditSearch) throws Exception;

    ActionReturnUtil insertToEsIndex(AuditRequestInfo auditRequestInfo) throws Exception;

}
