package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.common.util.ActionReturnUtil;

/**
 * Created by riven on 17-9-12.
 */
public interface StageSonarService {

    ActionReturnUtil getConditions(Integer stageId) throws Exception;

    ActionReturnUtil getStageSonar(Integer stageId) throws Exception;
}
