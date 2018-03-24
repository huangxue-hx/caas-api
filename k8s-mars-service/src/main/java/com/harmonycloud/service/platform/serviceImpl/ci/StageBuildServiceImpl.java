package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.dao.ci.StageBuildMapper;
import com.harmonycloud.dao.ci.bean.StageBuild;
import com.harmonycloud.service.platform.service.ci.StageBuildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-1-9
 * @Modified
 */
@Service
public class StageBuildServiceImpl implements StageBuildService {
    @Autowired
    StageBuildMapper stageBuildMapper;

    @Override
    public void updateStageBuildByStageIdAndBuildNum(StageBuild stageBuild){
        stageBuildMapper.updateByStageIdAndBuildNum(stageBuild);
    }

    @Override
    public List<StageBuild> selectStageBuildByObject(StageBuild stageBuild) {
        return stageBuildMapper.queryByObject(stageBuild);
    }

    @Override
    public String getStageLogByObject(StageBuild stageBuild){
        return stageBuildMapper.queryLogByObject(stageBuild);
    }
}
