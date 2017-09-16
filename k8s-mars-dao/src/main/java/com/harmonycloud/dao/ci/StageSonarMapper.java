package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.StageSonar;
import org.apache.ibatis.annotations.Param;

/**
 * Created by anson on 17/7/12.
 */
public interface StageSonarMapper {

    void insertStageSonar(StageSonar stageSonar);
    StageSonar queryByStageId(Integer id);
    void updateStageSonar(StageSonar stageSonar);
    void deleteByStageId(@Param("stageId") Integer stageId);
}
