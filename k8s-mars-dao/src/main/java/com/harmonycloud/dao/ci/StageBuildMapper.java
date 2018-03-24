package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.StageBuild;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by anson on 17/7/17.
 */
public interface StageBuildMapper {
    List<StageBuild> queryByObject(StageBuild stageBuild);
    void insert(StageBuild stageBuild);

    void updateWaitingStage(@Param("jobId")Integer jobId, @Param("buildNum")Integer buildNum);

    void updateByStageOrderAndBuildNum(@Param("stageBuild")StageBuild stageBuild);

    void deleteByJobId(Integer id);

    int countByObject(StageBuild stageBuildCondition);

    List<StageBuild> queryByObjectWithPagination(@Param("stageBuild")StageBuild stageBuild, @Param("offset")Integer offset, @Param("rows") Integer rows);

    void updateByStageIdAndBuildNum(StageBuild stageBuild);

    String queryLogByObject(StageBuild stageBuild);
}
