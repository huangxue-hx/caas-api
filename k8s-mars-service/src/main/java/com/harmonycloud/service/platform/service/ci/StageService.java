package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.ci.bean.StageType;
import com.harmonycloud.dto.cicd.StageDto;

/**
 * Created by anson on 17/7/13.
 */
public interface StageService {
    ActionReturnUtil updateStage(StageDto stage) throws Exception;

    ActionReturnUtil addStage(StageDto stage) throws Exception;

    ActionReturnUtil deleteStage(Integer id) throws Exception;

    ActionReturnUtil stageDetail(Integer id) throws Exception;

    ActionReturnUtil listStageType(String tenantId) throws Exception;

    ActionReturnUtil addStageType(StageType stageType);

    ActionReturnUtil deleteStageType(Integer id) throws Exception;

    ActionReturnUtil getBuildList(Integer id);
}
