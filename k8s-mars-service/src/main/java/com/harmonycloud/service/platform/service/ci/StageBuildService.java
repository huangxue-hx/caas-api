package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.dao.ci.bean.StageBuild;

import java.util.List;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-1-9
 * @Modified
 */
public interface StageBuildService {
    void updateStageBuildByStageIdAndBuildNum(StageBuild stageBuild);

    List<StageBuild> selectStageBuildByObject(StageBuild stageBuild);

    String getStageLogByObject(StageBuild stageBuild);
}
