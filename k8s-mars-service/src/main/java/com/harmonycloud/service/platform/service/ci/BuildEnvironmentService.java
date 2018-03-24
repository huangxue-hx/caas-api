package com.harmonycloud.service.platform.service.ci;


import com.harmonycloud.dao.ci.bean.BuildEnvironment;

import java.util.List;


public interface BuildEnvironmentService {
    List<BuildEnvironment> listBuildEnvironment(String projectId, String clusterId, String name) throws Exception;

    BuildEnvironment getBuildEnvironment(Integer id) throws Exception;

    void addBuildEnvironment(BuildEnvironment buildEnvironment) throws Exception;

    void updateBuildEnvironment(BuildEnvironment buildEnvironment) throws Exception;

    void deleteBuildEnvironment(Integer id) throws Exception;

    int deleteByClusterId(String clusterId);
}