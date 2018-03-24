package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.dao.ci.bean.Stage;
import com.harmonycloud.dao.ci.bean.StageType;
import com.harmonycloud.dto.cicd.StageDto;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;

/**
 * Created by anson on 17/7/13.
 */
public interface StageService {
    void updateStage(StageDto stage) throws Exception;

    Integer addStage(StageDto stage) throws Exception;

    void deleteStage(Integer id) throws Exception;

    ActionReturnUtil stageDetail(Integer id) throws Exception;

    List<StageType> listStageType(String type) throws Exception;

    ActionReturnUtil addStageType(StageType stageType);

    ActionReturnUtil deleteStageType(Integer id) throws Exception;

    ActionReturnUtil getBuildList(Integer id, Integer pageSize, Integer page);

    ActionReturnUtil listBuildEnvironemnt();
    
    List<Map> getStageBuildFromJenkins(Job job, Integer buildNum) throws Exception;

    void stageBuildSync(Job job, Integer buildNum, Map stageMap, int stageOrder) throws Exception;

    void getStageLogWS(WebSocketSession session, Integer id, Integer buildNum);

    long countByExample(Stage stage) throws Exception;

    List<Stage> selectByExample(Stage stage) throws Exception;

    Stage selectByPrimaryKey(Integer id) throws Exception;

    String  getStageLog(Integer stageId, Integer buildNum) throws Exception;

    void insert(Stage stage) throws Exception;

    void createOrUpdateCredential(Integer stageId, String username, String password) throws Exception;
}
