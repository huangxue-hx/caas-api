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
}
