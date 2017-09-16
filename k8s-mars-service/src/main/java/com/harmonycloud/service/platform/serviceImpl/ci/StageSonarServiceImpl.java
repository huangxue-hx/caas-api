package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.ci.StageSonarMapper;
import com.harmonycloud.dao.ci.bean.StageSonar;
import com.harmonycloud.service.platform.service.ci.StageSonarService;
import com.harmonycloud.sonarqube.webapi.client.SonarQualitygatesService;
import com.harmonycloud.sonarqube.webapi.model.qualitygates.Conditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by riven on 17-9-12.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class StageSonarServiceImpl implements StageSonarService {

    @Autowired
    private StageSonarMapper stageSonarMapper;

    @Autowired
    private SonarQualitygatesService sonarQualitygatesService;

    @Override
    public ActionReturnUtil getConditions(Integer stageId) throws Exception {
        StageSonar stageSonar = stageSonarMapper.queryByStageId(stageId);
        if(stageSonar!=null){
            Conditions conditions = sonarQualitygatesService.getConditions(stageSonar.getQualitygatesId());
            return ActionReturnUtil.returnSuccessWithData(conditions);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil getStageSonar(Integer stageId) throws Exception {
        StageSonar stageSonar = stageSonarMapper.queryByStageId(stageId);
        return ActionReturnUtil.returnSuccessWithData(stageSonar);
    }
}
