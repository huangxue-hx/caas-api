package com.harmonycloud.service.user;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.UserAuditSearch;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created by czm on 2017/3/29.
 */
public interface UserAuditService {
    ActionReturnUtil serachByQuery(UserAuditSearch userAuditSearch, boolean isAdmin) throws Exception;

    ActionReturnUtil serachByUserName(String username, boolean isAdmin) throws IOException;

    ActionReturnUtil serachByModule(String username, String module, boolean isAdmin) throws IOException;

    ActionReturnUtil serachAuditsByUser(String username, boolean isAdmin) throws Exception;
}
