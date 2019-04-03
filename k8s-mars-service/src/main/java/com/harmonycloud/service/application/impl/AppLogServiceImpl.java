package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.application.LogBackupRuleMapper;
import com.harmonycloud.dao.application.bean.LogBackupRule;
import com.harmonycloud.dao.application.bean.LogBackupRuleExample;
import com.harmonycloud.dto.application.SnapshotInfoDto;
import com.harmonycloud.dto.log.AppLogDto;
import com.harmonycloud.dto.log.EsSnapshotDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.application.AppLogService;
import com.harmonycloud.service.application.EsService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.user.RoleLocalService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.cluster.metadata.RepositoryMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class AppLogServiceImpl implements AppLogService {

    private static Logger LOGGER = LoggerFactory.getLogger(AppLogServiceImpl.class);
    private static final int TIME_OFFSET_HOUT = -1;

    @Autowired
    private LogBackupRuleMapper logBackupRuleMapper;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private EsService esService;

    @Autowired
    private RoleLocalService roleLocalService;

    /**
     * 创建日志备份规则
     * @param appLogDtoIn
     */
    @Override
    public void setLogBackupRule(AppLogDto appLogDtoIn) {
        if (StringUtils.isAnyBlank(appLogDtoIn.getClusterIds())){
            appLogDtoIn.setClusterIds(CommonConstant.RULE_ALL);
        }
        List<String> clusterIds = checkCluster(appLogDtoIn.getClusterIds());
        for (String clusterId : clusterIds) {

            LogBackupRule logBackupRuleIn = new LogBackupRule();
            logBackupRuleIn.setClusterId(clusterId);
            logBackupRuleIn.setBackupDir(appLogDtoIn.getBackupDir());
            logBackupRuleIn.setDaysBefore(appLogDtoIn.getDateBefore());
            logBackupRuleIn.setDaysDuration(appLogDtoIn.getDateDuration());
            logBackupRuleIn.setMaxRestoreSpeed(appLogDtoIn.getMaxRestoreSpeed());
            logBackupRuleIn.setMaxSnapshotSpeed(appLogDtoIn.getMaxSnapshotSpeed());
            logBackupRuleIn.setCreateTime(new Date());
            logBackupRuleIn.setAvailable(null == appLogDtoIn.getAvailable() ? false: appLogDtoIn.getAvailable());
            List<LogBackupRule> logBackupRules = listLogBackupRules(clusterId, null);
            //防止重复，另一方案：引入优先级，多个生效时使用优先级最高的
            if (!CollectionUtils.isEmpty(logBackupRules)){
                logBackupRuleIn.setId(logBackupRules.get(0).getId());
                logBackupRuleMapper.updateByPrimaryKeySelective(logBackupRuleIn);
            }else {
                logBackupRuleMapper.insert(logBackupRuleIn);
            }

        }
    }

    /**
     * 检查cluster等信息
     *
     * @param clusterIds
     * @return
     */
    private List<String> checkCluster(String clusterIds){
        List<String> result = new ArrayList<>();
        if (!clusterIds.contains(CommonConstant.COMMA)){
            result.add(clusterIds);
            return result;
        }
        String[] clusterIdArray = clusterIds.split(CommonConstant.COMMA);
        for (String clusterId : clusterIdArray) {
            if (StringUtils.isAnyBlank(clusterId)
                    || StringUtils.equals(CommonConstant.RULE_ALL, clusterId)) {
                continue;
            }
            Cluster cluster = clusterService.findClusterById(clusterId);
            if (null == cluster){
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }
            result.add(clusterId);
        }
        return result;
    }

    /**
     * 更新日志备份规则
     * @param appLogDtoIn
     */
    @Override
    public void updateLogBackupRule(AppLogDto appLogDtoIn) {
        List<String> clusterIds = null;
        if (!StringUtils.isAnyBlank(appLogDtoIn.getClusterIds())){
            clusterIds = checkCluster(appLogDtoIn.getClusterIds());
        }

        LogBackupRule logBackupRuleIn = new LogBackupRule();
        logBackupRuleIn.setId(appLogDtoIn.getRuleId());
        if (!CollectionUtils.isEmpty(clusterIds)){
            logBackupRuleIn.setClusterId(clusterIds.get(0));
        }
        logBackupRuleIn.setDaysBefore(appLogDtoIn.getDateBefore());
        logBackupRuleIn.setDaysDuration(appLogDtoIn.getDateDuration());
        logBackupRuleIn.setMaxRestoreSpeed(appLogDtoIn.getMaxRestoreSpeed());
        logBackupRuleIn.setMaxSnapshotSpeed(appLogDtoIn.getMaxSnapshotSpeed());
        logBackupRuleIn.setUpdateTime(DateUtil.getCurrentUtcTime());
        logBackupRuleMapper.updateByPrimaryKeySelective(logBackupRuleIn);

    }

    /**
     * 查询日志备份规则
     * @param clusterIds
     * @return
     */
    @Override
    public List<LogBackupRule> listLogBackupRules(String clusterIds, Boolean available) {
        List<String> clusterIdsChecked = new ArrayList<>();
        if (StringUtils.isNotBlank(clusterIds)){
            clusterIdsChecked = checkCluster(clusterIds);
        } else {
            try {
                clusterIdsChecked.addAll(roleLocalService.listCurrentUserRoleClusterIds());
            }catch (Exception e){
                LOGGER.error("获取当前角色的集群列表失败,",e);
                return Collections.emptyList();
            }
        }

        LogBackupRuleExample condition = new LogBackupRuleExample();
        LogBackupRuleExample.Criteria criteria = condition.createCriteria();
        if (!CollectionUtils.isEmpty(clusterIdsChecked)){
            criteria.andClusterIdIn(clusterIdsChecked);
        }
        if (null != available){
            criteria.andAvailableEqualTo(available);
        }
        List<LogBackupRule> logBackupRules = logBackupRuleMapper.selectByExample(condition);
        if (!CollectionUtils.isEmpty(logBackupRules)){
            for (LogBackupRule logBackupRule: logBackupRules) {
                if (CommonConstant.RULE_ALL.equals(logBackupRule.getClusterId())){
                    continue;
                }
                Cluster cluster = clusterService.findClusterById(logBackupRule.getClusterId());
                if (null != cluster){
                    logBackupRule.setClusterName(cluster.getName());
                    logBackupRule.setClusterAliasName(cluster.getAliasName());
                }

            }
        }
        return logBackupRules;
    }

    /**
     * 删除日志备份规则
     * @param ruleId
     */
    @Override
    public void deleteLogBackupRule(Integer ruleId) {
        checkRuleExist(ruleId);
        logBackupRuleMapper.deleteByPrimaryKey(ruleId);
    }

    private void checkRuleExist(Integer ruleId){
        LogBackupRule logBackupRule = logBackupRuleMapper.selectByPrimaryKey(ruleId);
        if (null == logBackupRule){
            throw new MarsRuntimeException(ErrorCodeMessage.APP_LOG_RULE_NOT_EXIST);

        }
    }

    /**
     * 停止日志备份规则
     * @param ruleId
     */
    @Override
    public void stopLogBackupRule(Integer ruleId) {
        changeRuleRunStatus(ruleId, false);
    }

    /**
     * 修改规则运行状态
     *
     * @param ruleId
     * @param enable
     */
    private void changeRuleRunStatus(Integer ruleId, Boolean enable){
        checkRuleExist(ruleId);
        LogBackupRule condition = new LogBackupRule();
        condition.setId(ruleId);
        condition.setAvailable(enable);
        condition.setUpdateTime(DateUtil.getCurrentUtcTime());
        logBackupRuleMapper.updateByPrimaryKeySelective(condition);
    }

    /**
     * 启动日志备份规则
     * @param ruleId
     */
    @Override
    public void startLogBackupRule(Integer ruleId) {
        changeRuleRunStatus(ruleId, true);

    }

    /**
     * 备份应用日志
     *
     * @throws Exception
     */
    public void backupAppLog() throws Exception {
        LogBackupRule commonRule = getCommonRule();
        List<Cluster> clusters = clusterService.listCluster();
        // 确定当前时间，防止重复获取时得到不同的值
        final Date currentDate = new Date();
        final Date nullDate = DateUtil.LongToDate(CommonConstant.ORIGINAL_DATE_MILL_SECOND);
        for (Cluster cluster : clusters) {
            try {
                List<LogBackupRule> logBackupRules = listLogBackupRules(cluster.getId(), true);
                LogBackupRule currentRule = null;
                // 当前cluster规则不存在，则尝试使用通用规则
                if (CollectionUtils.isEmpty(logBackupRules)) {
                    if (null == commonRule) {
                        continue;
                    } else {
                        currentRule = commonRule;
                    }
                } else {
                    // 目前一个cluster只有一个规则，没有优先级
                    currentRule = logBackupRules.get(0);
                }
                List<String> existIndexes = esService.getIndexes(cluster.getId());
                if(CollectionUtils.isEmpty(existIndexes)){
                    continue;
                }
                Date lastSnapshotDate = null;
                SnapshotInfoDto snapshotInfoDto = esService.getLastSnapshot(cluster.getId());
                if (null != snapshotInfoDto) {
                    lastSnapshotDate = DateUtil.LongToDate(snapshotInfoDto.getStartTime());
                }
                // 在duration天数之前做过备份，则现在不需要做,
                // 往前一小时避免不同服务器时间偏差, 同时需要考虑创建日志快照比定时任务开始运行要晚一点
                Date date = DateUtil.addHour(DateUtil.addDay(lastSnapshotDate, currentRule.getDaysDuration()), TIME_OFFSET_HOUT);
                if (null != lastSnapshotDate
                        && date.after(currentDate)) {
                    continue;
                }
                // 计算哪些时间的索引要备份
                Date indexStartDate = null;
                if (null != snapshotInfoDto) {
                    // 从快照索引中推算最大的时间，以便作为开始时间使用
                    indexStartDate = snapshotInfoDto.getIndices().stream().map(indexName -> {
                        if (!indexName.contains(esService.getLogIndexPrefix())) {
                            return nullDate;
                        }
                        indexName = indexName.replace(esService.getLogIndexPrefix(), CommonConstant.EMPTYSTRING).trim();
                        Date tempDate = DateUtil.StringToDate(indexName, CommonConstant.ES_INDEX_LOGSTASH_DATE_FORMAT);
                        return null == tempDate ? nullDate : tempDate;
                    }).filter(indexDate -> null != indexDate)
                            .max(Date::compareTo).get();
                }
                // 如果没有快照或查不到，或者有快照但是未从索引中推算出时间，则默认从daysBefore + daysDuration天前开始
                if (null == indexStartDate || 0 == nullDate.compareTo(indexStartDate)) {
                    indexStartDate = DateUtil.addDay(currentDate, -(currentRule.getDaysDuration() + currentRule.getDaysBefore()));
                } else {
                    // 如果在快照中找到了符合格式的索引，原来的索引是已经备份的，需要从加一天开始
                    indexStartDate = DateUtil.addDay(indexStartDate, CommonConstant.ES_INDEX_START_DATE_ADD_DAY_ONE);
                }
                Date indexEndDate = DateUtil.addDay(currentDate, -currentRule.getDaysBefore());
                if (indexEndDate.before(indexStartDate)) {
                    continue;
                }
                List<String> indexDates = new ArrayList<>();
                Date tempDate = indexEndDate;
                while (tempDate.after(indexStartDate)) {
                    String index = esService.getLogIndexPrefix()
                            + DateUtil.DateToString(tempDate, CommonConstant.ES_INDEX_LOGSTASH_DATE_FORMAT);
                    if (existIndexes.contains(index)) {
                        indexDates.add(index);
                    }
                    tempDate = DateUtil.addDay(tempDate, -CommonConstant.ES_INDEX_START_DATE_ADD_DAY_ONE);
                }
                // 如果生成的索引列表是空的，则不备份，否则将把ES中的索引都备份了
                if (CollectionUtils.isEmpty(indexDates)) {
                    continue;
                }
                // 检查仓库，不存在则创建
                List<RepositoryMetaData> repositoryMetaDatas = esService.listSnapshotRepositories(cluster);
                if (CollectionUtils.isEmpty(repositoryMetaDatas)) {
                    EsSnapshotDto esSnapshotDto = new EsSnapshotDto();
                    esSnapshotDto.setClusterId(cluster.getId());
                    esSnapshotDto.setBackupDir(currentRule.getBackupDir());
                    esSnapshotDto.setMaxSnapshotSpeed(esSnapshotDto.getMaxSnapshotSpeed());
                    esSnapshotDto.setMaxRestoreSpeed(esSnapshotDto.getMaxRestoreSpeed());
                    esService.createSnapshotRepository(esSnapshotDto);
                }
                String snapshotDateSuffix = DateUtil.DateToString(currentDate, CommonConstant.ES_INDEX_LOGSTASH_DATE_FORMAT);

                esService.createSnapshot(cluster.getId(),
                        CommonConstant.ES_SNAPSHOT_CREATE_AUTO_PREFIX + snapshotDateSuffix,
                        indexDates.toArray(new String[indexDates.size()]));
                //更新此次备份的时间
                LogBackupRule updateRule = new LogBackupRule();
                updateRule.setId(currentRule.getId());
                updateRule.setLastBackupTime(new Date());
                logBackupRuleMapper.updateByPrimaryKeySelective(updateRule);
            } catch (Exception e){
                LOGGER.error("日志自动备份失败，clusterId：{}",cluster.getId(),e);
            }
        }
    }

    @Override
    public int deleteBackupRuleByClusterId(String clusterId){
        return logBackupRuleMapper.deleteByClusterId(clusterId);
    }

    /**
     * 获取通用规则（如果存在）
     *
     * @return
     */
    private LogBackupRule getCommonRule(){
        List<LogBackupRule> logBackupRules = listLogBackupRules(CommonConstant.RULE_ALL, true);
        return CollectionUtils.isEmpty(logBackupRules)?null:logBackupRules.get(0);
    }

}
