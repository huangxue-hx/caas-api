package com.harmonycloud.service.application;

import com.harmonycloud.dao.application.bean.LogBackupRule;
import com.harmonycloud.dto.log.AppLogDto;

import java.util.List;

/**
 * 应用日志接口
 * 目前主要为备份业务
 */
public interface AppLogService {

    /**
     * 创建日志备份规则
     * @param appLogDtoIn
     */
    void setLogBackupRule(AppLogDto appLogDtoIn);

    /**
     * 更新日志备份规则
     * @param appLogDtoIn
     */
    void updateLogBackupRule(AppLogDto appLogDtoIn);

    /**
     * 查询日志备份规则
     * @return
     */
    List<LogBackupRule> listLogBackupRules(String clusterIds, Boolean available);

    /**
     * 删除日志备份规则
     * @param ruleId
     */
    void deleteLogBackupRule(Integer ruleId);

    /**
     * 停止日志备份规则
     * @param ruleId
     */
    void stopLogBackupRule(Integer ruleId);

    /**
     * 启动日志备份规则
     * @param ruleId
     */
    void startLogBackupRule(Integer ruleId);

    /**
     * 备份应用日志
     *
     * @throws Exception
     */
    void backupAppLog() throws Exception;

    int deleteBackupRuleByClusterId(String clusterId);

}
