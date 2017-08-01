package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cicd.DependenceDto;

/**
 * Created by anson on 17/7/29.
 */
public interface DependenceService {
    ActionReturnUtil listByTenantId(String tenantId, String name) throws Exception;

    ActionReturnUtil add(DependenceDto dependenceDto) throws Exception;

    ActionReturnUtil delete(String name) throws Exception;
}
