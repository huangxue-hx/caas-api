package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cicd.TestResultDto;

import java.util.List;
import java.util.Map;

/**
 * Created by anson on 18/1/5.
 */
public interface IntegrationTestService {
    List<Map> getTestSuites(String projectId, String type) throws  Exception;

    ActionReturnUtil updateTestResult(Integer stageId, Integer buildNum, TestResultDto testResult) throws Exception;

    void executeTestSuite(String suiteId, Integer stageId, Integer buildNum) throws Exception;
}
