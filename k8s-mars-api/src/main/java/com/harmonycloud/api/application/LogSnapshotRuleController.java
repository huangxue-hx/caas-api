package com.harmonycloud.api.application;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.application.bean.LogBackupRule;
import com.harmonycloud.dto.application.RestoreInfoDto;
import com.harmonycloud.dto.application.SnapshotInfoDto;
import com.harmonycloud.dto.log.AppLogDto;
import com.harmonycloud.dto.log.EsSnapshotDto;
import com.harmonycloud.service.application.AppLogService;
import com.harmonycloud.service.application.EsService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Objects;

@RequestMapping("/snapshotrules")
@Controller
public class LogSnapshotRuleController {

    private static Logger LOGGER = LoggerFactory.getLogger(LogSnapshotRuleController.class);

    @Autowired
    private AppLogService appLogService;

    @Autowired
    private EsService esService;
    /**
     * 创建日志备份规则
     * @param appLogDtoIn
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil setLogBackupRule(@ModelAttribute AppLogDto appLogDtoIn){
        if (StringUtils.isAnyBlank(appLogDtoIn.getClusterIds())){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        appLogService.setLogBackupRule(appLogDtoIn);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 更新日志备份规则
     * @param appLogDtoIn
     */
    @ResponseBody
    @RequestMapping(value="/{ruleId}",method = RequestMethod.PUT)
    public ActionReturnUtil updateLogBackupRule(@ModelAttribute AppLogDto appLogDtoIn,
                             @PathVariable(value = "ruleId") Integer ruleId){
        if (Objects.isNull(ruleId) || 0 == ruleId){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        appLogDtoIn.setRuleId(ruleId);
        appLogService.updateLogBackupRule(appLogDtoIn);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 查询日志备份规则
     * @return
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listLogBackupRules(@RequestParam(value = "clusterId", required = false) String clusterId,
                                               @RequestParam(value = "available", required = false) Boolean available){
        List<LogBackupRule> logBackupRules = appLogService.listLogBackupRules(clusterId,available);
        return ActionReturnUtil.returnSuccessWithData(logBackupRules);
    }

    /**
     * 删除日志备份规则
     * @param ruleId
     */
    @ResponseBody
    @RequestMapping(value="/{ruleId}",method = RequestMethod.DELETE)
    public ActionReturnUtil deleteLogBackupRule(@PathVariable(value = "ruleId") Integer ruleId){
        if (Objects.isNull(ruleId) || 0 == ruleId){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        appLogService.deleteLogBackupRule(ruleId);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 停止日志备份规则
     * @param ruleId
     */
    @ResponseBody
    @RequestMapping(value="/{ruleId}/stop",method = RequestMethod.PUT)
    public ActionReturnUtil stopLogBackupRule(@PathVariable(value = "ruleId") Integer ruleId){
        if (Objects.isNull(ruleId) || 0 == ruleId){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        appLogService.stopLogBackupRule(ruleId);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 启动日志备份规则
     * @param ruleId
     */
    @ResponseBody
    @RequestMapping(value="/{ruleId}/start",method = RequestMethod.PUT)
    public ActionReturnUtil startLogBackupRule(@PathVariable(value = "ruleId") Integer ruleId){
        if (Objects.isNull(ruleId) || 0 == ruleId){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        appLogService.startLogBackupRule(ruleId);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 创建快照仓库
     *
     * @param esSnapshotDtoIn 必填：clusterId    选填：maxSnapshotSpeed, maxRestoreSpeed

     * @return
     */
    @ResponseBody
    @RequestMapping(value="repositories",method = RequestMethod.POST)
    public ActionReturnUtil createSnapshotRepository(@ModelAttribute EsSnapshotDto esSnapshotDtoIn){
        if (StringUtils.isAnyBlank(esSnapshotDtoIn.getClusterId())){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        esService.createSnapshotRepository(esSnapshotDtoIn);
        return ActionReturnUtil.returnSuccess();
    }

    /**创建快照
     *
     * @param esSnapshotDtoIn 必填：clusterId, dates     选填：maxSnapshotSpeed, maxRestoreSpeed
     * @return
     */
    @ResponseBody
    @RequestMapping(value="snapshots",method = RequestMethod.POST)
    public ActionReturnUtil createSnapshot(@ModelAttribute EsSnapshotDto esSnapshotDtoIn){
        if (StringUtils.isAnyBlank(esSnapshotDtoIn.getClusterId())){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        esService.createSnapshotWithRepo(esSnapshotDtoIn);
        return ActionReturnUtil.returnSuccess();
    }

    /**查询快照
     *
     * @param clusterId 必填
     * @param snapshotNames 选填
     * @return
     */
    @ResponseBody
    @RequestMapping(value="snapshots",method = RequestMethod.GET)
    public ActionReturnUtil listSnapshots(@RequestParam(value = "clusterId",required = false) String clusterId,
                                           @RequestParam(value = "snapshotNames", required = false) String[] snapshotNames) throws Exception{
        if (StringUtils.isAnyBlank(clusterId)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        List<SnapshotInfoDto> snapshotInfos = esService.listSnapshots(clusterId, snapshotNames);
        return ActionReturnUtil.returnSuccessWithData(snapshotInfos);
    }

    /**删除快照
     *
     * @param clusterId 必填
     * @param snapshotName 必填
     * @return
     */
    @ResponseBody
    @RequestMapping(value="snapshots",method = RequestMethod.DELETE)
    public ActionReturnUtil deleteSnapshot(@RequestParam(value = "clusterId") String clusterId,
                                          @RequestParam(value = "snapshotName") String snapshotName){
        if (StringUtils.isAnyBlank(clusterId, snapshotName)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        esService.deleteSnapshot(clusterId,snapshotName);
        return ActionReturnUtil.returnSuccess();
    }

    /**恢复快照
     *
     * @param esSnapshotDtoIn 必填：clusterId, snapshotName    选填：renamePrefix, renameSuffix, indexNames
     * @return
     */
    @ResponseBody
    @RequestMapping(value="snapshots",method = RequestMethod.PUT)
    public ActionReturnUtil restoreSnapshots(@ModelAttribute EsSnapshotDto esSnapshotDtoIn){
        if (StringUtils.isAnyBlank(esSnapshotDtoIn.getClusterId(), esSnapshotDtoIn.getSnapshotName())){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        RestoreInfoDto restoreInfo = esService.restoreSnapshots(esSnapshotDtoIn);
        return ActionReturnUtil.returnSuccessWithData(restoreInfo);
    }

    /**删除恢复的快照
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/snapshots/restored/{date}",method = RequestMethod.DELETE)
    public ActionReturnUtil deleteRestoreIndex(@PathVariable("date") String date,
                                               @RequestParam(value = "clusterId") String clusterId) throws Exception{
        return ActionReturnUtil.returnSuccessWithData(esService.deleteRestoredIndex(date, clusterId));
    }

}
