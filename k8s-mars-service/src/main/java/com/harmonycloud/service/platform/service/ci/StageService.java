package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.dao.ci.bean.StageType;
import com.harmonycloud.dto.cicd.StageDto;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;

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

    ActionReturnUtil listBuildEnvironemnt();

    ActionReturnUtil listDeployImage(Integer jobId, Integer stageOrder);

    
    List<Map> getStageBuildFromJenkins(Job job, Integer buildNum) throws Exception;

    void stageBuildSync(Job job, Integer buildNum, Map stageMap, int stageOrder);

    void getStageLogWS(WebSocketSession session, Integer id, Integer buildNum);

    ActionReturnUtil updateJenkinsJob(Integer id) throws Exception;
}
