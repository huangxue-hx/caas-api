package com.harmonycloud.service.istio.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.istio.RuleDetailMapper;
import com.harmonycloud.dao.istio.RuleOverviewMapper;
import com.harmonycloud.dao.istio.bean.RuleDetail;
import com.harmonycloud.dao.istio.bean.RuleOverview;
import com.harmonycloud.dto.application.istio.TimeoutRetryDto;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.HTTPRetry;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.HTTPRoute;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.VirtualService;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.service.istio.VirtualServiceService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.istio.IstioTimeoutRetryService;
import com.harmonycloud.service.istio.util.IstioPolicyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * create by weg on 18-12-27.
 */
@Service
public class IstioTimeoutRetryServiceImpl implements IstioTimeoutRetryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IstioTimeoutRetryServiceImpl.class);

    @Autowired
    private VirtualServiceService virtualServiceService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private RuleOverviewMapper ruleOverviewMapper;

    @Autowired
    private RuleDetailMapper ruleDetailMapper;

    @Autowired
    private HttpSession session;

    @Override
    public ActionReturnUtil createTimeoutRetryPolicy(String deployName, TimeoutRetryDto timeoutRetryDto) throws Exception {
        AssertUtil.notNull(timeoutRetryDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        AssertUtil.notNull(timeoutRetryDto.getClusterId(), DictEnum.CLUSTER_ID);
        if (StringUtils.isBlank(timeoutRetryDto.getAttempts()) != StringUtils.isBlank(timeoutRetryDto.getPerTryTimeout())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FRONT_DATA_ERROR);
        }
        if (StringUtils.isBlank(timeoutRetryDto.getTimeout()) && StringUtils.isBlank(timeoutRetryDto.getAttempts())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FRONT_DATA_ERROR);
        }
        String namespace = timeoutRetryDto.getNamespace();
        Cluster cluster = clusterService.findClusterById(timeoutRetryDto.getClusterId());
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);

        //是否存在只判断数据库
        if (IstioPolicyUtil.checkPolicyExist(cluster.getId(),
                namespace,
                deployName,
                timeoutRetryDto.getRuleName(),
                timeoutRetryDto.getRuleType(),
                ruleOverviewMapper)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_EXIST);
        }

        String ruleId = UUIDUtil.getUUID();
        IstioPolicyUtil.insertRuleOverview(timeoutRetryDto, ruleId, cluster.getId(), userName, ruleOverviewMapper);
        RuleDetail timeoutRuleDetail = IstioPolicyUtil.makeTimeoutRuleDetail(timeoutRetryDto, ruleId);
        timeoutRuleDetail.setCreateTime(new Date());
        ruleDetailMapper.insert(timeoutRuleDetail);
        RuleDetail retryRuleDetail = IstioPolicyUtil.makeRetryRuleDetail(timeoutRetryDto, ruleId);
        retryRuleDetail.setCreateTime(new Date());
        ruleDetailMapper.insert(retryRuleDetail);
        K8SClientResponse response = virtualServiceService.getVirtualService(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
            IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnSuccessWithData(status.getMessage());
        }
        VirtualService virtualService = JsonUtil.jsonToPojo(response.getBody(), VirtualService.class);
        String timeout = StringUtils.isBlank(timeoutRetryDto.getTimeout()) ? null : timeoutRetryDto.getTimeout().trim() + CommonConstant.SECOND;
        HTTPRetry httpRetry = JsonUtil.jsonToPojo(new String(retryRuleDetail.getRuleDetailContent()), HTTPRetry.class);
        if (Objects.isNull(httpRetry.getAttempts()) && StringUtils.isBlank(httpRetry.getPerTryTimeout())) {
            httpRetry = null;
        }
        if (Objects.nonNull(virtualService) && Objects.nonNull(virtualService.getSpec()) && CollectionUtils.isNotEmpty(virtualService.getSpec().getHttp())) {
            for (HTTPRoute httpRoute : virtualService.getSpec().getHttp()) {
                httpRoute.setTimeout(timeout);
                httpRoute.setRetries(httpRetry);
            }
            K8SClientResponse updateResponse = virtualServiceService.updateVirtualService(namespace, deployName, virtualService, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
                UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnSuccessWithData(status.getMessage());
            }
        } else {
            virtualService = IstioPolicyUtil.makeVirtualService(timeout, httpRetry, timeoutRetryDto.getServiceName(), timeoutRetryDto.getNamespace(), timeoutRetryDto.getServiceName(), timeoutRetryDto.getHost());
            K8SClientResponse createResponse = virtualServiceService.createVirtualService(namespace, virtualService, cluster);
            if (!HttpStatusUtil.isSuccessStatus(createResponse.getStatus())) {
                IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
                UnversionedStatus status = JsonUtil.jsonToPojo(createResponse.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnSuccessWithData(status.getMessage());
            }
        }
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_SUCCESS);
    }

    @Override
    public ActionReturnUtil updateTimeoutRetryPolicy(String ruleId, TimeoutRetryDto timeoutRetryDto) throws Exception {
        AssertUtil.notNull(timeoutRetryDto.getRuleName(), DictEnum.NAME);
        AssertUtil.notNull(timeoutRetryDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(ruleId, DictEnum.RULE_ID);
        AssertUtil.notNull(timeoutRetryDto.getClusterId(), DictEnum.CLUSTER_ID);
        if (StringUtils.isBlank(timeoutRetryDto.getAttempts()) != StringUtils.isBlank(timeoutRetryDto.getPerTryTimeout())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FRONT_DATA_ERROR);
        }
        if (StringUtils.isBlank(timeoutRetryDto.getTimeout()) && StringUtils.isBlank(timeoutRetryDto.getAttempts())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FRONT_DATA_ERROR);
        }
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        RuleDetail timeoutRuleDetail = IstioPolicyUtil.makeTimeoutRuleDetail(timeoutRetryDto, ruleId);
        timeoutRuleDetail.setUpdateTime(new Date());
        ruleDetailMapper.updateByPrimaryKeySelective(timeoutRuleDetail);
        RuleDetail retryRuleDetail = IstioPolicyUtil.makeRetryRuleDetail(timeoutRetryDto, ruleId);
        retryRuleDetail.setUpdateTime(new Date());
        ruleDetailMapper.updateByPrimaryKeySelective(retryRuleDetail);
        RuleOverview updateRuleOverview = new RuleOverview();
        updateRuleOverview.setRuleId(ruleId);
        updateRuleOverview.setUserName(userName);
        updateRuleOverview.setUpdateTime(new Date());
        ruleOverviewMapper.updateByPrimaryKeySelective(updateRuleOverview);
        //开关开启状态下更新
        if (IstioPolicyUtil.checkPolicyStatus(ruleId, ruleOverviewMapper)) {
            String namespace = timeoutRetryDto.getNamespace();
            Cluster cluster = clusterService.findClusterById(timeoutRetryDto.getClusterId());
            //获取集群中VirtualService
            K8SClientResponse response = virtualServiceService.getVirtualService(namespace, timeoutRetryDto.getServiceName(), cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_OK, ruleOverviewMapper);
                LOGGER.error("get VirtualService error", response.getBody());
                return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
            }
            VirtualService virtualService = JsonUtil.jsonToPojo(response.getBody(), VirtualService.class);
            String timeout = StringUtils.isBlank(timeoutRetryDto.getTimeout()) ? null : timeoutRetryDto.getTimeout().trim() + CommonConstant.SECOND;
            HTTPRetry httpRetry = JsonUtil.jsonToPojo(new String(retryRuleDetail.getRuleDetailContent()), HTTPRetry.class);
            if (Objects.nonNull(virtualService.getSpec()) && CollectionUtils.isNotEmpty(virtualService.getSpec().getHttp())) {
                virtualService.getSpec().getHttp().forEach(httpRoute -> {
                    httpRoute.setTimeout(timeout);
                    if (StringUtils.isNotEmpty(httpRetry.getPerTryTimeout())) {
                        httpRoute.setRetries(httpRetry);
                    } else {
                        httpRoute.setRetries(null);
                    }
                });
            }
            K8SClientResponse updateResponse = virtualServiceService.updateVirtualService(namespace, timeoutRetryDto.getServiceName(), virtualService, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userName, CommonConstant.DATA_IS_OK, ruleOverviewMapper);
                UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                LOGGER.error(status.getMessage());
                return ActionReturnUtil.returnSuccessWithData(status.getMessage());
            }
            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_IS_OK, userName, CommonConstant.DATA_IS_OK, ruleOverviewMapper);
        }
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_UPDATE_SUCCESS);
    }

    @Override
    public ActionReturnUtil closeTimeoutRetryPolicy(String namespace, String ruleId, String deployName, String clusterId) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = clusterService.findClusterById(clusterId);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();

        K8SClientResponse response = virtualServiceService.getVirtualService(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
            LOGGER.error("get VirtualService error", response.getBody());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        VirtualService virtualService = JsonUtil.jsonToPojo(response.getBody(), VirtualService.class);
        if (Objects.nonNull(virtualService) && Objects.nonNull(virtualService.getSpec()) && CollectionUtils.isNotEmpty(virtualService.getSpec().getHttp())) {
            int flag = IstioPolicyUtil.checkVirtualServicePolicyType(virtualService);
            int count = flag | IstioPolicyUtil.typeMap.get(CommonConstant.TIMEOUT_RETRY);
            if (flag > 0 && count == IstioPolicyUtil.typeMap.get(CommonConstant.TIMEOUT_RETRY)) {
                //delete k8s
                K8SClientResponse deleteResponse = virtualServiceService.deleteVirtualService(namespace, deployName, cluster);
                if (!HttpStatusUtil.isSuccessStatus(deleteResponse.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
                    IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                    UnversionedStatus status = JsonUtil.jsonToPojo(deleteResponse.getBody(), UnversionedStatus.class);
                    throw new MarsRuntimeException(status.getMessage());
                }
            } else {
                //update k8s
                virtualService.getSpec().getHttp().forEach(httpRoute -> {
                    httpRoute.setTimeout(null);
                    httpRoute.setRetries(null);
                });
                K8SClientResponse updateResponse = virtualServiceService.updateVirtualService(namespace, deployName, virtualService, cluster);
                if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                    IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_OK, ruleOverviewMapper);
                    UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                    throw new MarsRuntimeException(status.getMessage());
                }
            }
        }
        IstioPolicyUtil.updateRuleOverview(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, CommonConstant.DATA_IS_OK, CommonConstant.DATA_IS_OK, userName, ruleOverviewMapper);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CLOSE_SUCCESS);
    }

    @Override
    public ActionReturnUtil openTimeoutRetryPolicy(String namespace, String ruleId, String deployName, String clusterId, String host) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(clusterId, DictEnum.CLUSTER_ID);
        Cluster cluster = clusterService.findClusterById(clusterId);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);

        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (CollectionUtils.isEmpty(ruleDetails) || ruleDetails.size() != 2) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        String timeout = "";
        HTTPRetry httpRetry = new HTTPRetry();
        for (RuleDetail ruleDetail : ruleDetails) {
            if (ruleDetail.getRuleDetailOrder() == 1) {
                timeout = new String(ruleDetail.getRuleDetailContent());
            } else if (ruleDetail.getRuleDetailOrder() == 2) {
                httpRetry = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), HTTPRetry.class);
                if (Objects.isNull(httpRetry.getAttempts()) && StringUtils.isBlank(httpRetry.getPerTryTimeout())) {
                    httpRetry = null;
                }
            }
        }
        K8SClientResponse response = virtualServiceService.getVirtualService(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.info("no VirtualService " + response.getStatus());
        }
        VirtualService virtualService = JsonUtil.jsonToPojo(response.getBody(), VirtualService.class);
        if (Objects.nonNull(virtualService) &&
                Objects.nonNull(virtualService.getSpec()) &&
                CollectionUtils.isNotEmpty(virtualService.getSpec().getHttp())) {
            for (HTTPRoute httpRoute : virtualService.getSpec().getHttp()) {
                httpRoute.setTimeout(timeout);
                httpRoute.setRetries(httpRetry);
            }
            K8SClientResponse updateResponse = virtualServiceService.updateVirtualService(namespace, deployName, virtualService, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                LOGGER.error(status.getMessage());
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
            }
        } else {
            virtualService = IstioPolicyUtil.makeVirtualService(timeout, httpRetry, deployName, namespace, deployName, host);
            K8SClientResponse createResponse = virtualServiceService.createVirtualService(namespace, virtualService, cluster);
            if (!HttpStatusUtil.isSuccessStatus(createResponse.getStatus())) {
                UnversionedStatus status = JsonUtil.jsonToPojo(createResponse.getBody(), UnversionedStatus.class);
                LOGGER.error(status.getMessage());
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
            }
        }
        IstioPolicyUtil.updateRuleOverview(ruleId, CommonConstant.ISTIO_POLICY_OPEN, CommonConstant.DATA_IS_OK, CommonConstant.DATA_IS_OK, userName, ruleOverviewMapper);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_OPEN_SUCCESS);
    }

    @Override
    public ActionReturnUtil deleteTimeoutRetryPolicy(String namespace, String ruleId, String deployName, String clusterId) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        AssertUtil.notNull(clusterId, DictEnum.CLUSTER_ID);
        Cluster cluster = clusterService.findClusterById(clusterId);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();

        K8SClientResponse response = virtualServiceService.getVirtualService(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
            LOGGER.error("get VirtualService error", response.getBody());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        VirtualService virtualService = JsonUtil.jsonToPojo(response.getBody(), VirtualService.class);
        if (Objects.nonNull(virtualService) && Objects.nonNull(virtualService.getSpec()) && CollectionUtils.isNotEmpty(virtualService.getSpec().getHttp())) {
            int flag = IstioPolicyUtil.checkVirtualServicePolicyType(virtualService);
            int count = flag | IstioPolicyUtil.typeMap.get(CommonConstant.TIMEOUT_RETRY);
            if (flag > 0 && count == IstioPolicyUtil.typeMap.get(CommonConstant.TIMEOUT_RETRY)) {
                //delete k8s
                K8SClientResponse deleteResponse = virtualServiceService.deleteVirtualService(namespace, deployName, cluster);
                if (!HttpStatusUtil.isSuccessStatus(deleteResponse.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
                    IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                    UnversionedStatus status = JsonUtil.jsonToPojo(deleteResponse.getBody(), UnversionedStatus.class);
                    throw new MarsRuntimeException(status.getMessage());
                }
            } else {
                //update k8s
                virtualService.getSpec().getHttp().forEach(httpRoute -> {
                    httpRoute.setTimeout(null);
                    httpRoute.setRetries(null);
                });
                K8SClientResponse updateResponse = virtualServiceService.updateVirtualService(namespace, deployName, virtualService, cluster);
                if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                    IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_OK, ruleOverviewMapper);
                    UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                    throw new MarsRuntimeException(status.getMessage());
                }
            }
        }
        ruleOverviewMapper.deleteByPrimaryKey(ruleId);
        ruleDetailMapper.deleteByPrimaryKey(ruleId);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_DELETE_SUCCESS);
    }

    @Override
    public ActionReturnUtil getTimeoutRetryPolicy(String namespace, String ruleId, String deployName, String clusterId) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (CollectionUtils.isEmpty(ruleDetails) || ruleDetails.size() < 2) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        //获取数据库中对应的策略中资源的详细信息
        String timeout = "";
        HTTPRetry httpRetry = new HTTPRetry();
        for (RuleDetail ruleDetail : ruleDetails) {
            if (ruleDetail.getRuleDetailOrder() == CommonConstant.RATE_LIMIT_QUOTA_ORDER) {
                timeout = new String(ruleDetail.getRuleDetailContent());
            } else if (ruleDetail.getRuleDetailOrder() == CommonConstant.RATE_LIMIT_REDISQUOTA_ORDER) {
                httpRetry = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), HTTPRetry.class);
            }
        }
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        try {
            K8SClientResponse response = virtualServiceService.getVirtualService(namespace, deployName, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                LOGGER.error("get VirtualService error", response.getBody());
            }
            VirtualService virtualService = JsonUtil.jsonToPojo(response.getBody(), VirtualService.class);
            if (Objects.nonNull(virtualService) &&
                    Objects.nonNull(virtualService.getSpec()) &&
                    CollectionUtils.isNotEmpty(virtualService.getSpec().getHttp())) {
                List<HTTPRoute> httpK8sDetail = virtualService.getSpec().getHttp();

                int flag = IstioPolicyUtil.checkVirtualServicePolicyType(virtualService);
                int count = flag | IstioPolicyUtil.typeMap.get(CommonConstant.TIMEOUT_RETRY);
                if (ruleOverview.getSwitchStatus() == CommonConstant.ISTIO_POLICY_CLOSE) {
                    // "|"运算后结果相等，说明包含该策略
                    if (count == flag) {
                        IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.SWITCH_STATUS_ERROR, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                        ruleOverview.setDataStatus(CommonConstant.SWITCH_STATUS_ERROR);
                        ruleOverview.setDataErrLoc(CommonConstant.DATA_IS_ERROR);
                    }
                } else {
                    // "|"运算后结果大于flag，说明资源中不包含该策略
                    if (count > flag) {
                        IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.SWITCH_STATUS_ERROR, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                        ruleOverview.setDataStatus(CommonConstant.SWITCH_STATUS_ERROR);
                        ruleOverview.setDataErrLoc(CommonConstant.DATA_IS_ERROR);
                    } else {
                        if (StringUtils.isNotEmpty(timeout) && !timeout.equals(httpK8sDetail.get(0).getTimeout())) {
                            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userName, 1, ruleOverviewMapper);
                            ruleOverview.setDataStatus(CommonConstant.DATA_NOT_SAME);
                            ruleOverview.setDataErrLoc(1);
                        }
                        //创建策略时，为了处理HTTPRetry空的问题，直接将对象实例化了，所以此处的空判断需要对HTTPRetry对象的属性进行判断
                        if (Objects.nonNull(httpRetry.getAttempts()) && StringUtils.isNotEmpty(httpRetry.getPerTryTimeout()) && !httpRetry.equals(httpK8sDetail.get(0).getRetries())) {
                            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userName, 1, ruleOverviewMapper);
                            ruleOverview.setDataStatus(CommonConstant.DATA_NOT_SAME);
                            ruleOverview.setDataErrLoc(2);
                        }
                    }
                }
            } else if (ruleOverview.getSwitchStatus() == CommonConstant.ISTIO_POLICY_OPEN) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.SWITCH_STATUS_ERROR, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                ruleOverview.setDataStatus(CommonConstant.SWITCH_STATUS_ERROR);
                ruleOverview.setDataErrLoc(CommonConstant.DATA_IS_ERROR);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        TimeoutRetryDto timeoutRetryDto = new TimeoutRetryDto();
        timeoutRetryDto.setRuleName(ruleOverview.getRuleName());
        timeoutRetryDto.setRuleName(ruleOverview.getRuleName());
        timeoutRetryDto.setRuleId(ruleOverview.getRuleId());
        timeoutRetryDto.setRuleType(ruleOverview.getRuleType());
        timeoutRetryDto.setServiceName(ruleOverview.getRuleSvc());
        timeoutRetryDto.setNamespace(ruleOverview.getRuleNs());
        timeoutRetryDto.setSwitchStatus(ruleOverview.getSwitchStatus().toString());
        timeoutRetryDto.setDataStatus(ruleOverview.getDataStatus().toString());
        timeoutRetryDto.setCreateTime(ruleOverview.getCreateTime());
        if (StringUtils.isNotBlank(timeout)) {
            timeoutRetryDto.setTimeout(timeout.replace(CommonConstant.SECOND, ""));
        }
        if (Objects.nonNull(httpRetry.getAttempts()) && StringUtils.isNotBlank(httpRetry.getPerTryTimeout())) {
            timeoutRetryDto.setAttempts(httpRetry.getAttempts().toString());
            timeoutRetryDto.setPerTryTimeout(httpRetry.getPerTryTimeout().replace(CommonConstant.SECOND, ""));
        }
        return ActionReturnUtil.returnSuccessWithData(timeoutRetryDto);
    }
}
