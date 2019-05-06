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
import com.harmonycloud.dto.application.istio.FaultInjectionDto;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.HTTPFaultInjection;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.HTTPRoute;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.VirtualService;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.service.istio.VirtualServiceService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.istio.IstioFaultInjectionService;
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
public class IstioFaultInjectionServiceImpl implements IstioFaultInjectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IstioFaultInjectionServiceImpl.class);

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

    //故障注入策略
    @Override
    public ActionReturnUtil createFaultInjectionPolicy(String deployName, FaultInjectionDto faultInjectionDto) throws Exception {
        AssertUtil.notNull(faultInjectionDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        AssertUtil.notNull(faultInjectionDto.getClusterId(), DictEnum.CLUSTER_ID);
        if (StringUtils.isBlank(faultInjectionDto.getFixedDelay()) != StringUtils.isBlank(faultInjectionDto.getDelayPercent()) ||
                StringUtils.isBlank(faultInjectionDto.getHttpStatus()) != StringUtils.isBlank(faultInjectionDto.getCodePercent())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FRONT_DATA_ERROR);
        }
        if (StringUtils.isBlank(faultInjectionDto.getFixedDelay()) && StringUtils.isBlank(faultInjectionDto.getHttpStatus())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FRONT_DATA_ERROR);
        }
        String namespace = faultInjectionDto.getNamespace();
        Cluster cluster = clusterService.findClusterById(faultInjectionDto.getClusterId());
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);

        //是否存在只判断数据库
        if (IstioPolicyUtil.checkPolicyExist(cluster.getId(),
                namespace,
                deployName,
                faultInjectionDto.getRuleName(),
                faultInjectionDto.getRuleType(),
                ruleOverviewMapper)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_EXIST);
        }

        String ruleId = UUIDUtil.getUUID();
        IstioPolicyUtil.insertRuleOverview(faultInjectionDto, ruleId, cluster.getId(), userName, ruleOverviewMapper);
        RuleDetail ruleDetail = IstioPolicyUtil.insertRuleDetail(faultInjectionDto, ruleId, ruleDetailMapper);

        K8SClientResponse response = virtualServiceService.getVirtualService(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
            IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnSuccessWithData(status.getMessage());
        }
        VirtualService virtualService = JsonUtil.jsonToPojo(response.getBody(), VirtualService.class);
        HTTPFaultInjection fault = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), HTTPFaultInjection.class);
        if (Objects.nonNull(virtualService) &&
                Objects.nonNull(virtualService.getSpec()) &&
                CollectionUtils.isNotEmpty(virtualService.getSpec().getHttp())) {
            virtualService.getSpec().getHttp().forEach(httpRoute -> httpRoute.setFault(fault));
            K8SClientResponse updateResponse = virtualServiceService.updateVirtualService(namespace, deployName, virtualService, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
                UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnSuccessWithData(status.getMessage());
            }
        } else {
            virtualService = IstioPolicyUtil.makeVirtualService(fault, faultInjectionDto.getServiceName(), faultInjectionDto.getNamespace(), faultInjectionDto.getServiceName(), faultInjectionDto.getHost());
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
    public ActionReturnUtil updateFaultInjectionPolicy(String ruleId, FaultInjectionDto faultInjectionDto) throws Exception {
        AssertUtil.notNull(faultInjectionDto.getRuleName(), DictEnum.NAME);
        AssertUtil.notNull(faultInjectionDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(faultInjectionDto.getClusterId(), DictEnum.CLUSTER_ID);
        AssertUtil.notNull(ruleId, DictEnum.RULE_ID);
        if (StringUtils.isBlank(faultInjectionDto.getFixedDelay()) != StringUtils.isBlank(faultInjectionDto.getDelayPercent()) ||
                StringUtils.isBlank(faultInjectionDto.getHttpStatus()) != StringUtils.isBlank(faultInjectionDto.getCodePercent())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FRONT_DATA_ERROR);
        }
        if (StringUtils.isBlank(faultInjectionDto.getFixedDelay()) && StringUtils.isBlank(faultInjectionDto.getHttpStatus())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FRONT_DATA_ERROR);
        }
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        RuleDetail ruleDetail = IstioPolicyUtil.makeFaultInjectionRuleDetail(faultInjectionDto, ruleId);
        ruleDetail.setUpdateTime(new Date());
        ruleDetailMapper.updateByPrimaryKeySelective(ruleDetail);
        RuleOverview updateRuleOverview = new RuleOverview();
        updateRuleOverview.setRuleId(ruleId);
        updateRuleOverview.setUserName(userName);
        updateRuleOverview.setUpdateTime(new Date());
        ruleOverviewMapper.updateByPrimaryKeySelective(updateRuleOverview);
        //开关开启状态下更新
        if (IstioPolicyUtil.checkPolicyStatus(ruleId, ruleOverviewMapper)) {
            String namespace = faultInjectionDto.getNamespace();
            Cluster cluster = clusterService.findClusterById(faultInjectionDto.getClusterId());
            //获取集群中VirtualService
            K8SClientResponse response = virtualServiceService.getVirtualService(namespace, faultInjectionDto.getServiceName(), cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_OK, ruleOverviewMapper);
                LOGGER.error("get VirtualService error", response.getBody());
                return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
            }
            VirtualService virtualService = JsonUtil.jsonToPojo(response.getBody(), VirtualService.class);
            HTTPFaultInjection fault = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), HTTPFaultInjection.class);
            if (CollectionUtils.isNotEmpty(virtualService.getSpec().getHttp())) {
                virtualService.getSpec().getHttp().forEach(httpRoute -> httpRoute.setFault(fault));
            }
            K8SClientResponse updateResponse = virtualServiceService.updateVirtualService(namespace, faultInjectionDto.getServiceName(), virtualService, cluster);
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
    public ActionReturnUtil closeFaultInjectionPolicy(String namespace, String ruleId, String deployName, String clusterId) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
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
            int count = flag | IstioPolicyUtil.typeMap.get(CommonConstant.FAULT_INJECTION);
            if (flag > 0 && count == IstioPolicyUtil.typeMap.get(CommonConstant.FAULT_INJECTION)) {
                //delete k8s
                K8SClientResponse deleteResponse = virtualServiceService.deleteVirtualService(namespace, deployName, cluster);
                if (!HttpStatusUtil.isSuccessStatus(deleteResponse.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
                    IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                    UnversionedStatus status = JsonUtil.jsonToPojo(deleteResponse.getBody(), UnversionedStatus.class);
                    throw new MarsRuntimeException(status.getMessage());
                }
            } else {
                //update k8s
                virtualService.getSpec().getHttp().forEach(httpRoute -> httpRoute.setFault(null));
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
    public ActionReturnUtil openFaultInjectionPolicy(String namespace, String ruleId, String deployName, String clusterId, String host) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(clusterId, DictEnum.CLUSTER_ID);
        Cluster cluster = clusterService.findClusterById(clusterId);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);

        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (CollectionUtils.isEmpty(ruleDetails) || ruleDetails.size() > 1) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        RuleDetail ruleDetail = ruleDetails.get(0);
        K8SClientResponse response = virtualServiceService.getVirtualService(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.info("no VirtualService " + response.getStatus());
        }
        VirtualService virtualService = JsonUtil.jsonToPojo(response.getBody(), VirtualService.class);
        HTTPFaultInjection fault = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), HTTPFaultInjection.class);
        if (Objects.nonNull(virtualService) && Objects.nonNull(virtualService.getSpec())) {
            if (CollectionUtils.isNotEmpty(virtualService.getSpec().getHttp())) {
                virtualService.getSpec().getHttp().forEach(httpRoute -> httpRoute.setFault(fault));
            }
            K8SClientResponse updateResponse = virtualServiceService.updateVirtualService(namespace, deployName, virtualService, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                LOGGER.error(status.getMessage());
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
            }
        } else {
            virtualService = IstioPolicyUtil.makeVirtualService(fault, deployName, namespace, deployName, host);
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
    public ActionReturnUtil deleteFaultInjectionPolicy(String namespace, String ruleId, String deployName, String  clusterId) throws Exception {
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
            int count = flag | IstioPolicyUtil.typeMap.get(CommonConstant.FAULT_INJECTION);
            if (flag > 0 && count == IstioPolicyUtil.typeMap.get(CommonConstant.FAULT_INJECTION)) {
                //delete k8s
                K8SClientResponse deleteResponse = virtualServiceService.deleteVirtualService(namespace, deployName, cluster);
                if (!HttpStatusUtil.isSuccessStatus(deleteResponse.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
                    IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                    UnversionedStatus status = JsonUtil.jsonToPojo(deleteResponse.getBody(), UnversionedStatus.class);
                    throw new MarsRuntimeException(status.getMessage());
                }
            } else {
                //update k8s
                virtualService.getSpec().getHttp().forEach(httpRoute -> httpRoute.setFault(null));
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
    public ActionReturnUtil getFaultInjectionPolicy(String namespace, String ruleId, String deployName,String  clusterId) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (Objects.isNull(ruleDetails) || ruleDetails.size() != 1) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        RuleDetail ruleDetail = ruleDetails.get(0);
        HTTPFaultInjection fault = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), HTTPFaultInjection.class);
        if (Objects.isNull(fault)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
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
                int count = flag | IstioPolicyUtil.typeMap.get(CommonConstant.FAULT_INJECTION);
                if (ruleOverview.getSwitchStatus() == CommonConstant.ISTIO_POLICY_CLOSE) {
                    // "|"运算后结果相等，说明包含该策略
                    if (count == flag) {
                        IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.SWITCH_STATUS_ERROR, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                        ruleOverview.setDataStatus(CommonConstant.SWITCH_STATUS_ERROR);
                        ruleOverview.setDataErrLoc(ruleDetail.getRuleDetailOrder());
                    }
                } else {
                    // "|"运算后结果大于flag，说明资源中不包含该策略
                    if (count > flag) {
                        IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.SWITCH_STATUS_ERROR, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                        ruleOverview.setDataStatus(CommonConstant.SWITCH_STATUS_ERROR);
                        ruleOverview.setDataErrLoc(ruleDetail.getRuleDetailOrder());
                    } else {
                        if (!fault.equals(httpK8sDetail.get(0).getFault())) {
                            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userName, 1, ruleOverviewMapper);
                            ruleOverview.setDataStatus(CommonConstant.DATA_NOT_SAME);
                            ruleOverview.setDataErrLoc(1);
                        }
                    }
                }
            } else if (ruleOverview.getSwitchStatus() == CommonConstant.ISTIO_POLICY_OPEN) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.SWITCH_STATUS_ERROR, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                ruleOverview.setDataStatus(CommonConstant.SWITCH_STATUS_ERROR);
                ruleOverview.setDataErrLoc(ruleDetail.getRuleDetailOrder());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        FaultInjectionDto faultInjectionDto = new FaultInjectionDto();
        faultInjectionDto.setRuleName(ruleOverview.getRuleName());
        faultInjectionDto.setRuleName(ruleOverview.getRuleName());
        faultInjectionDto.setRuleId(ruleOverview.getRuleId());
        faultInjectionDto.setRuleType(ruleOverview.getRuleType());
        faultInjectionDto.setServiceName(ruleOverview.getRuleSvc());
        faultInjectionDto.setNamespace(ruleOverview.getRuleNs());
        faultInjectionDto.setSwitchStatus(ruleOverview.getSwitchStatus().toString());
        faultInjectionDto.setDataStatus(ruleOverview.getDataStatus().toString());
        faultInjectionDto.setCreateTime(ruleOverview.getCreateTime());
        if (Objects.nonNull(fault.getDelay())) {
            faultInjectionDto.setFixedDelay(fault.getDelay().getFixedDelay().replace(CommonConstant.SECOND, ""));
            faultInjectionDto.setDelayPercent(fault.getDelay().getPercent().toString());
        }
        if (Objects.nonNull(fault.getAbort())) {
            faultInjectionDto.setHttpStatus(fault.getAbort().getHttpStatus().toString());
            faultInjectionDto.setCodePercent(fault.getAbort().getPercent().toString());
        }
        return ActionReturnUtil.returnSuccessWithData(faultInjectionDto);
    }
}
