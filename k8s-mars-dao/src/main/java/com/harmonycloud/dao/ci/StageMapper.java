package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.Stage;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by anson on 17/7/12.
 */
public interface StageMapper {
    List<Stage> queryByJobId(Integer jobId);
    void insertStage(Stage Stage);

    void deleteStage(Integer id);
    void deleteStageByJob(Integer id);

    void increaseStageOrder(@Param("jobId") Integer jobId, @Param("stageOrder") Integer stageOrder);
    void decreaseStageOrder(@Param("jobId") Integer jobId, @Param("stageOrder") Integer stageOrder);

    void updateStage(Stage stage);

    Stage queryById(Integer id);
    List<Stage> queryByStageTypeId(@Param("stageTypeId") Integer stageTypeId);
    List<Stage> querySonarByJobId(Map<String,String> map);

    long countByExample(Stage stage);
    List<Stage> selectByExample(Stage stage);

    void updatePasswordByUsername(@Param("username")String username, @Param("password")String password);
}
