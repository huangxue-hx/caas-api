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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anson on 17/7/13.
 */

@RequestMapping("/tenants/{tenantId}/projects/{projectId}/cicdjobs/{jobId}/stages")
@Controller
public class StageController {

    @Autowired
    StageService stageService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil addStage(@RequestBody StageDto stageDto) throws Exception {
        logger.info("add stage");
        Integer stageId = stageService.addStage(stageDto);
        Map map = new HashMap();
        map.put("id", stageId);
        return ActionReturnUtil.returnSuccessWithData(map);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateStage(@RequestBody StageDto stageDto) throws Exception {
        logger.info("update stage");
        stageService.updateStage(stageDto);
        return ActionReturnUtil.returnSuccess();
    }

    @RequestMapping(value = "/{stageId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteStage(@PathVariable("stageId") Integer stageId) throws Exception{
        logger.info("delete stage");
        stageService.deleteStage(stageId);
        return ActionReturnUtil.returnSuccess();
    }

    @RequestMapping(value = "/{stageId}", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil stageDetail(@PathVariable("stageId") Integer stageId){
        try{
            return stageService.stageDetail(stageId);
        } catch (Exception e){
            return ActionReturnUtil.returnErrorWithData(e.getMessage());
        }
    }

    @RequestMapping(value = "/{stageId}/result", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listStageBuildResult(@PathVariable("stageId") Integer stageId,
                                                 @RequestParam(value="pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                 @RequestParam(value="page", required = false, defaultValue = "1") Integer page){
        try{
            return stageService.getBuildList(stageId, pageSize, page);
        }catch(Exception e){
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(value = "/stagetypes", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil addStageType(@RequestBody StageType stageType){
        try{
            return stageService.addStageType(stageType);
        }catch(Exception e){
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(value = "/stagetypes", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteStageType(@RequestParam(value = "id") Integer id){
        try{
            return stageService.deleteStageType(id);
        }catch(Exception e){
            return ActionReturnUtil.returnError();
        }
    }

    @RequestMapping(value = "/stagetypes", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listStageType(@PathVariable("tenantId") String tenantId, @RequestParam(value="type") String type) throws Exception{
        return ActionReturnUtil.returnSuccessWithData(stageService.listStageType(type));
    }

    @RequestMapping(value = "/{stageId}/log", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getStageLog(@PathVariable("stageId")Integer stageId, @RequestParam(value="buildNum")Integer buildNum) throws Exception{
        return ActionReturnUtil.returnSuccessWithData(stageService.getStageLog(stageId, buildNum));
    }


}
