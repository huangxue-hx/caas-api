package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.StageType;

import java.util.List;

/**
 * Created by anson on 17/7/13.
 */
public interface StageTypeMapper {
    void insertStageType(StageType stageType);
    void deleteStageType(Integer id);
    List<StageType> queryByTenantId(String tenantId);

    List<StageType> queryByTenant(String tenant);

    StageType queryById(Integer stageTypeId);
}
