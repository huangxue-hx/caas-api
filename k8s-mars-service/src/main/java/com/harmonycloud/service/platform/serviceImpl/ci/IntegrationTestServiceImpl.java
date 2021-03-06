package com.harmonycloud.service.platform.serviceImpl.ci;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.harmonycloud.common.enumm.CtsCodeMessage;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CtsClient;
import com.harmonycloud.common.util.HttpClientResponse;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.ci.bean.Stage;
import com.harmonycloud.dao.ci.bean.StageBuild;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dto.cicd.TestResultDto;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.ci.IntegrationTestService;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.service.ci.StageBuildService;
import com.harmonycloud.service.platform.service.ci.StageService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.tenant.TenantService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anson on 18/1/5.
 */
@Service
public class IntegrationTestServiceImpl implements IntegrationTestService{
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TenantService tenantService;

    @Autowired
    private StageBuildService stageBuildService;

    @Autowired
    private StageService stageService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private JobService jobService;

    @Override
    public List<Map> getTestSuites(String projectId, String type) throws Exception{
        Project project = projectService.getProjectByProjectId(projectId);
        String systemCode = project.getProjectSystemCode();
        Map param = new HashMap();
        param.put("sysCode", systemCode);
        param.put("type", type);
        HttpClientResponse response = CtsClient.exec(CtsClient.buildGetSuitsUrl(), HTTPMethod.POST, null, param);
        Map map = JsonUtil.convertJsonToMap(response.getBody());
        List<Map> allSuiteList = new ArrayList<>();
        if(CtsClient.SUCCESS_CODE.equals(String.valueOf(map.get("code")))){
            List<Map> resultMapList = (List<Map>)map.get("result");
            for(Map resultMap:resultMapList){
                List<Map> suiteList = (List<Map>)resultMap.get("suiteList");
                allSuiteList.addAll(suiteList);
            }
        }else{
            logger.error("获取测试套件失败:{}",map);
        }
        return allSuiteList;
    }

    @Override
    public ActionReturnUtil updateTestResult(Integer stageId, Integer buildNum, TestResultDto testResult) throws Exception{
        Stage stage = stageService.selectByPrimaryKey(stageId);
        if(stage == null){
            return ActionReturnUtil.returnCodeAndMsg(CtsCodeMessage.STAGE_NOT_EXIST, "");
        }
        StageBuild stageBuild = new StageBuild();
        stageBuild.setStageId(stageId);
        stageBuild.setBuildNum(buildNum);
        List<StageBuild> stageBuildList = stageBuildService.selectStageBuildByObject(stageBuild);
        if(CollectionUtils.isEmpty(stageBuildList)){
            return ActionReturnUtil.returnCodeAndMsg(CtsCodeMessage.BUILD_NOT_EXIST, "");
        }
        if(CtsCodeMessage.LAST_RUN_NOT_FINISHED.value() == testResult.getCode()){
            testResult.setMsg(CtsCodeMessage.LAST_RUN_NOT_FINISHED.getMessage());
        }
        if(CtsClient.SUCCESS.equalsIgnoreCase(testResult.getResult())){
            stageBuild.setStatus(Constant.PIPELINE_STATUS_SUCCESS);
        }else if(CtsClient.FAILURE.equalsIgnoreCase(testResult.getResult())){
            stageBuild.setStatus(Constant.PIPELINE_STATUS_FAILED);
        }

        stageBuild.setTestResult(testResult.getResult()+"("+testResult.getMsg()+")");
        stageBuild.setTestUrl(testResult.getLink());
        stageBuildService.updateStageBuildByStageIdAndBuildNum(stageBuild);
        jobService.syncJobStatus(jobService.getJobById(stage.getJobId()),buildNum);
        return ActionReturnUtil.returnCodeAndMsg(CtsCodeMessage.SUCCESS, "");
    }

    @Override
    public void executeTestSuite(String suiteId, Integer stageId, Integer buildNum) throws Exception{
        Map params = new HashMap();
        params.put("suiteId", suiteId);
        params.put("callback", CtsClient.buildCallBackUrl(stageId, buildNum));
        HttpClientResponse response = CtsClient.exec(CtsClient.buildExecuteSuiteUrl(), HTTPMethod.POST, null, params);
        if(HttpStatus.OK.value() == response.getStatus()){
            //获取调用的response，若测试结果为失败，则更新步骤的构建记录
            TestResultDto testResultDto = JSON.parseObject(response.getBody(), TestResultDto.class);
            if(CtsClient.FAILURE.equals(testResultDto.getResult())){
                updateTestResult(stageId, buildNum, testResultDto);
                throw new MarsRuntimeException(testResultDto.getMsg());
            }
        }else{
            throw new MarsRuntimeException(ErrorCodeMessage.EXECUTE_TEST_SUITE_ERROR);
        }
    }

}
