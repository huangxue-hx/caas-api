package com.harmonycloud.api.ci;

import com.harmonycloud.common.enumm.CtsCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cicd.TestResultDto;
import com.harmonycloud.service.platform.service.ci.IntegrationTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * Created by anson on 18/1/5.
 */

@RestController
public class IntegrationTestController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IntegrationTestService integrationTestService;

    @RequestMapping(value = "/tenants/{tenantId}/projects/{projectId}/cicdjobs/testsuites", method = RequestMethod.GET)
    public ActionReturnUtil getTestSuite(@PathVariable("projectId")String projectId,@RequestParam(value = "type") String type) throws Exception{
         return ActionReturnUtil.returnSuccessWithData(integrationTestService.getTestSuites(projectId, type));
    }

    @RequestMapping(value="/cicdjobs/stage/{stageId}/result/{buildNum}/testcallback", method = RequestMethod.POST)
    public ActionReturnUtil updateTestResult(@PathVariable("stageId") Integer stageId, @PathVariable("buildNum") Integer buildNum, @RequestBody TestResultDto testResult) throws Exception{
//        logger.info("test callback: stageId {}, buildNum {}",stageId, buildNum);
        return integrationTestService.updateTestResult(stageId, buildNum, testResult);
    }
}
