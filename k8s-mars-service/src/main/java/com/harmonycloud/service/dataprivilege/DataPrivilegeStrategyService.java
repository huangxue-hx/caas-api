package com.harmonycloud.service.dataprivilege;

import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeStrategy;

import java.util.List;

/**
 * Created by anson on 18/6/25.
 */
public interface DataPrivilegeStrategyService {
    List<DataPrivilegeStrategy> selectStrategy(String tenantId, String projectId, Integer resourceType);
}
