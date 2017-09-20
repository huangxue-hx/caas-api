package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.SonarConfig;
import com.harmonycloud.dao.ci.bean.StageSonar;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by anson on 17/7/12.
 */
public interface SonarConfigMapper {

    void insertSonarConfig(SonarConfig sonarConfig);
    List<SonarConfig> queryByAll();
}
