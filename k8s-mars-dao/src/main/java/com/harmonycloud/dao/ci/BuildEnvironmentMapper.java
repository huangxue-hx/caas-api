package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.BuildEnvironment;

import java.util.List;

/**
 * Created by anson on 17/7/25.
 */
public interface BuildEnvironmentMapper {
    List<BuildEnvironment> queryAll();

    BuildEnvironment queryById(String id);
}
