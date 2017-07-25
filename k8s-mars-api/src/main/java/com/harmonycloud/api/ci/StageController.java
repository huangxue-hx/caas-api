package com.harmonycloud.api.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.ci.bean.StageType;
import com.harmonycloud.dto.cicd.StageDto;
import com.harmonycloud.service.platform.service.ci.StageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by anson on 17/7/13.
 */

@RequestMapping("/cicd/stage")
@Controller
public class StageController {

    @Autowired
    StageService stageService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil addStage(@RequestBody StageDto stageDto){
        logger.info("add stage");
        try {
            return stageService.addStage(stageDto);
        } catch (Exception e) {
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateStage(@RequestBody StageDto stageDto){
        logger.info("update stage");
        try {
            return stageService.updateStage(stageDto);
        } catch (Exception e) {
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteStage(@RequestParam(value="id") Integer id){
        logger.info("delete stage");
        try {
            return stageService.deleteStage(id);
        } catch (Exception e) {
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil stageDetail(@RequestParam(value="id") Integer id){
        try{
            return stageService.stageDetail(id);
        } catch (Exception e){
            return ActionReturnUtil.returnErrorWithData(e.getMessage());
        }
    }

    @RequestMapping(value = "/buildList", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listStageBuildList(@RequestParam(value="id") Integer id){
        try{
            return stageService.getBuildList(id);
        }catch(Exception e){
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(value = "/type", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil addStageType(@RequestBody StageType stageType){
        try{
            return stageService.addStageType(stageType);
        }catch(Exception e){
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(value = "/type", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteStageType(@RequestParam(value = "id") Integer id){
        try{
            return stageService.deleteStageType(id);
        }catch(Exception e){
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(value = "/type", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listStageType(@RequestParam(value="tenantId") String tenantId){
        try{
            return stageService.listStageType(tenantId);
        }catch(Exception e){
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(value = "/buildEnvironment", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listbuildenvironment(){
        return stageService.listBuildEnvironemnt();
    }
}
