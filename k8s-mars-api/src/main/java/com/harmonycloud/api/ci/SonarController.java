package com.harmonycloud.api.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.service.ci.StageSonarService;
import com.harmonycloud.sonarqube.webapi.client.SonarQualitygatesService;
import com.harmonycloud.sonarqube.webapi.model.qualitygates.Condition;
import com.harmonycloud.sonarqube.webapi.model.qualitygates.ConditionParamsParent;
import com.harmonycloud.sonarqube.webapi.model.qualitygates.Conditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/cicd/sonar")
public class SonarController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SonarQualitygatesService sonarQualitygatesService;

    @Autowired
    private StageSonarService stageSonarService;

    @RequestMapping(value = "/createCondition", method = RequestMethod.GET)
    public ActionReturnUtil createCondition(@RequestBody Condition condition){
        logger.info("sonar createCondition.");
        try {
            Condition result = sonarQualitygatesService.createCondition(condition);
        return ActionReturnUtil.returnSuccessWithData(result);
        } catch (IOException e) {
            e.printStackTrace();
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(value = "/updateCondition", method = RequestMethod.GET)
    public ActionReturnUtil updateCondition(@RequestBody Condition condition){
        logger.info("sonar updateCondition.");
        try {
            Condition result = sonarQualitygatesService.updateCondition(condition);
            return ActionReturnUtil.returnSuccessWithData(result);
        } catch (IOException e) {
            e.printStackTrace();
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(value = "/deleteCondition", method = RequestMethod.GET)
    public ActionReturnUtil deleteCondition(@RequestParam Integer id){
        logger.info("sonar deleteCondition.");
        try {
            sonarQualitygatesService.deleteCondition(id);
            return ActionReturnUtil.returnSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(value = "/getConditions", method = RequestMethod.GET)
    public ActionReturnUtil getConditions(@RequestParam Integer id){
        logger.info("sonar getConditions.");
        try {
            return stageSonarService.getConditions(id);
        } catch (Exception e) {
            e.printStackTrace();
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(value = "/getConditionParams", method = RequestMethod.GET)
    public ActionReturnUtil getConditionParams(){
        logger.info("sonar getConditionParams.");
        try {
            List<ConditionParamsParent> conditions = sonarQualitygatesService.getConditionParams();
            return ActionReturnUtil.returnSuccessWithData(conditions);
        } catch (Exception e) {
            e.printStackTrace();
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(value = "/getStageSonar", method = RequestMethod.GET)
    public ActionReturnUtil getStageSonar(@RequestParam Integer stageId){
        logger.info("sonar getStageSonar.");
        try {
            return stageSonarService.getStageSonar(stageId);
        } catch (Exception e) {
            e.printStackTrace();
            return ActionReturnUtil.returnError();
        }
    }
}
