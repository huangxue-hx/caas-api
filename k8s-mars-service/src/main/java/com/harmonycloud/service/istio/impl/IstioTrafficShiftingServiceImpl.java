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
import com.harmonycloud.dto.application.istio.TrafficShiftingDesServiceDto;
import com.harmonycloud.dto.application.istio.TrafficShiftingDto;
import com.harmonycloud.dto.application.istio.TrafficShiftingMatchDto;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.*;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.istio.DestinationRuleService;
import com.harmonycloud.k8s.service.istio.VirtualServiceService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.istio.IstioTrafficShiftingService;
import com.harmonycloud.service.istio.util.IstioPolicyUtil;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * create by weg on 18-12-27.
 */
@Service
public class IstioTrafficShiftingServiceImpl implements IstioTrafficShiftingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IstioTrafficShiftingServiceImpl.class);

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private VirtualServiceService virtualServiceService;

    @Autowired
    private DestinationRuleService destinationRuleService;

    @Autowired
    private RuleOverviewMapper ruleOverviewMapper;

    @Autowired
    private RuleDetailMapper ruleDetailMapper;

    @Autowired
    private HttpSession session;

    //智能路由策略
    //TODO 该策略暂时只支持http --weg --20181204
    @Override
    public ActionReturnUtil createTrafficShiftingPolicy(String deployName, TrafficShiftingDto trafficShiftingDto) throws Exception {
        AssertUtil.notNull(trafficShiftingDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        String namespace = trafficShiftingDto.getNamespace();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);

        if (CollectionUtils.isEmpty(trafficShiftingDto.getDesServices()) && CollectionUtils.isEmpty(trafficShiftingDto.getMatches())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FRONT_DATA_ERROR);
        }
        //是否存在只判断数据库
        if (IstioPolicyUtil.checkPolicyExist(cluster.getId(), namespace, deployName, trafficShiftingDto.getRuleName(), trafficShiftingDto.getRuleType(), ruleOverviewMapper)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_EXIST);
        }
        String ruleId = UUIDUtil.getUUID();
        IstioPolicyUtil.insertRuleOverview(trafficShiftingDto, ruleId, cluster.getId(), userName, ruleOverviewMapper);
        RuleDetail ruleDetail = IstioPolicyUtil.insertRuleDetail(trafficShiftingDto, ruleId, ruleDetailMapper);

        K8SClientResponse response = virtualServiceService.getVirtualService(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
            IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnSuccessWithData(status.getMessage());
        }
        VirtualService virtualService = JsonUtil.jsonToPojo(response.getBody(), VirtualService.class);
        List<HTTPRoute> httpDetail = JsonUtil.jsonToListNonNull(new String(ruleDetail.getRuleDetailContent()), HTTPRoute.class);
        if (Objects.nonNull(virtualService) && Objects.nonNull(virtualService.getSpec())) {
            IstioPolicyUtil.makeUpdateVirtualService(virtualService, httpDetail);
            K8SClientResponse updateResponse = virtualServiceService.updateVirtualService(namespace, deployName, virtualService, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
                UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnSuccessWithData(status.getMessage());
            }
        } else {
            virtualService = IstioPolicyUtil.makeVirtualService(httpDetail, trafficShiftingDto.getServiceName(), trafficShiftingDto.getNamespace(), trafficShiftingDto.getServiceName());
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
    public ActionReturnUtil updateTrafficShiftingPolicy(String ruleId, TrafficShiftingDto trafficShiftingDto) throws Exception {
        AssertUtil.notNull(trafficShiftingDto.getRuleName(), DictEnum.NAME);
        AssertUtil.notNull(trafficShiftingDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(ruleId, DictEnum.RULE_ID);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        if (CollectionUtils.isEmpty(trafficShiftingDto.getDesServices()) && CollectionUtils.isEmpty(trafficShiftingDto.getMatches())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FRONT_DATA_ERROR);
        }
        RuleDetail ruleDetail = IstioPolicyUtil.makeTrafficShiftingRuleDetail(trafficShiftingDto, ruleId);
        ruleDetail.setUpdateTime(new Date());
        ruleDetailMapper.updateByPrimaryKeySelective(ruleDetail);
        RuleOverview updateRuleOverview = new RuleOverview();
        updateRuleOverview.setRuleId(ruleId);
        updateRuleOverview.setUserName(userName);
        updateRuleOverview.setUpdateTime(new Date());
        ruleOverviewMapper.updateByPrimaryKeySelective(updateRuleOverview);
        //开关开启状态下更新
        if (IstioPolicyUtil.checkPolicyStatus(ruleId, ruleOverviewMapper)) {
            String namespace = trafficShiftingDto.getNamespace();
            Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
            //获取集群中VirtualService
            K8SClientResponse response = virtualServiceService.getVirtualService(namespace, trafficShiftingDto.getServiceName(), cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                LOGGER.error("get VirtualService error", response.getBody());
                return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
            }
            VirtualService virtualService = JsonUtil.jsonToPojo(response.getBody(), VirtualService.class);
            List<HTTPRoute> httpDetail = JsonUtil.jsonToListNonNull(new String(ruleDetail.getRuleDetailContent()), HTTPRoute.class);
            IstioPolicyUtil.makeUpdateVirtualService(virtualService, httpDetail);
            K8SClientResponse updateResponse = virtualServiceService.updateVirtualService(namespace, trafficShiftingDto.getServiceName(), virtualService, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                LOGGER.error(status.getMessage());
                return ActionReturnUtil.returnSuccessWithData(status.getMessage());
            }
            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_IS_OK, userName, CommonConstant.DATA_IS_OK, ruleOverviewMapper);
        }
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_UPDATE_SUCCESS);
    }

    @Override
    public ActionReturnUtil closeTrafficShiftingPolicy(String namespace, String ruleId, String deployName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
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
            int count = flag | IstioPolicyUtil.typeMap.get(CommonConstant.TRAFFIC_SHIFTING);
            if (flag > 0 && count == IstioPolicyUtil.typeMap.get(CommonConstant.TRAFFIC_SHIFTING)) {
                //delete k8s
                K8SClientResponse deleteResponse = virtualServiceService.deleteVirtualService(namespace, deployName, cluster);
                if (!HttpStatusUtil.isSuccessStatus(deleteResponse.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
                    IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                    UnversionedStatus status = JsonUtil.jsonToPojo(deleteResponse.getBody(), UnversionedStatus.class);
                    throw new MarsRuntimeException(status.getMessage());
                }
            } else {
                //update k8s
                IstioPolicyUtil.deleteK8sTrafficShiftingPolicy(virtualService);
                K8SClientResponse updateResponse = virtualServiceService.updateVirtualService(namespace, deployName, virtualService, cluster);
                if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                    IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                    UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                    throw new MarsRuntimeException(status.getMessage());
                }
            }
        }
        IstioPolicyUtil.updateRuleOverview(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, CommonConstant.DATA_IS_OK, CommonConstant.DATA_IS_OK, userName, ruleOverviewMapper);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CLOSE_SUCCESS);
    }

    @Override
    public ActionReturnUtil openTrafficShiftingPolicy(String namespace, String ruleId, String deployName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (CollectionUtils.isEmpty(ruleDetails) || ruleDetails.size() > 1) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        RuleDetail ruleDetail = ruleDetails.get(0);
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        List<HTTPRoute> httpDetail = JsonUtil.jsonToListNonNull(new String(ruleDetail.getRuleDetailContent()), HTTPRoute.class);
        boolean versionIsOk = IstioPolicyUtil.checkVirtualServiceVersion(namespace, deployName, cluster, httpDetail, destinationRuleService);
        if (!versionIsOk) {
            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_IS_ERROR, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
            ruleOverview.setDataStatus(CommonConstant.DATA_IS_ERROR);
            ruleOverview.setDataErrLoc(1);
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DATA_VERSION_ERROR);
        }
        K8SClientResponse response = virtualServiceService.getVirtualService(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("get VirtualService error", response.getBody());
        }
        VirtualService virtualService = JsonUtil.jsonToPojo(response.getBody(), VirtualService.class);
        if (Objects.nonNull(virtualService) && Objects.nonNull(virtualService.getSpec())) {
            IstioPolicyUtil.makeUpdateVirtualService(virtualService, httpDetail);
            K8SClientResponse updateResponse = virtualServiceService.updateVirtualService(namespace, deployName, virtualService, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                LOGGER.error(status.getMessage());
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
            }
        } else {
            virtualService = IstioPolicyUtil.makeVirtualService(httpDetail, deployName, namespace, deployName);
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
    public ActionReturnUtil deleteTrafficShiftingPolicy(String namespace, String ruleId, String deployName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
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
            int count = flag | IstioPolicyUtil.typeMap.get(CommonConstant.TRAFFIC_SHIFTING);
            if (flag > 0 && count == IstioPolicyUtil.typeMap.get(CommonConstant.TRAFFIC_SHIFTING)) {
                //delete k8s
                K8SClientResponse deleteResponse = virtualServiceService.deleteVirtualService(namespace, deployName, cluster);
                if (!HttpStatusUtil.isSuccessStatus(deleteResponse.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
                    IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                    UnversionedStatus status = JsonUtil.jsonToPojo(deleteResponse.getBody(), UnversionedStatus.class);
                    throw new MarsRuntimeException(status.getMessage());
                }
            } else {
                //update k8s
                IstioPolicyUtil.deleteK8sTrafficShiftingPolicy(virtualService);
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
    public ActionReturnUtil getTrafficShiftingPolicy(String namespace, String ruleId, String deployName) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (Objects.isNull(ruleDetails) || ruleDetails.size() != 1) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        RuleDetail ruleDetail = ruleDetails.get(0);
        List<HTTPRoute> httpDBDetail = JsonUtil.jsonToListNonNull(new String(ruleDetail.getRuleDetailContent()), HTTPRoute.class);
        if (Objects.isNull(httpDBDetail)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        try {
            boolean versionIsOk = IstioPolicyUtil.checkVirtualServiceVersion(namespace, deployName, cluster, httpDBDetail, destinationRuleService);
            VirtualService virtualService = null;
            if (!versionIsOk) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_IS_ERROR, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                ruleOverview.setDataStatus(CommonConstant.DATA_IS_ERROR);
                ruleOverview.setDataErrLoc(1);
            } else {
                K8SClientResponse response = virtualServiceService.getVirtualService(namespace, deployName, cluster);
                if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                    LOGGER.error("get VirtualService error", response.getBody());
                }
                virtualService = JsonUtil.jsonToPojo(response.getBody(), VirtualService.class);
            }
            if (Objects.nonNull(virtualService) && Objects.nonNull(virtualService.getSpec())) {
                List<HTTPRoute> httpK8sDetail = virtualService.getSpec().getHttp();
                int flag = IstioPolicyUtil.checkVirtualServicePolicyType(virtualService);
                int count = flag | IstioPolicyUtil.typeMap.get(CommonConstant.TRAFFIC_SHIFTING);
                if (ruleOverview.getSwitchStatus() != CommonConstant.ISTIO_POLICY_OPEN) {
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
                    } else if (!IstioPolicyUtil.equalsList(httpDBDetail, httpK8sDetail)) {
                        IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userName, 1, ruleOverviewMapper);
                        ruleOverview.setDataStatus(CommonConstant.DATA_NOT_SAME);
                        ruleOverview.setDataErrLoc(1);
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
        TrafficShiftingDto trafficShiftingDto = new TrafficShiftingDto();
        trafficShiftingDto.setRuleName(ruleOverview.getRuleName());
        trafficShiftingDto.setRuleId(ruleOverview.getRuleId());
        trafficShiftingDto.setRuleType(ruleOverview.getRuleType());
        trafficShiftingDto.setServiceName(ruleOverview.getRuleSvc());
        trafficShiftingDto.setNamespace(ruleOverview.getRuleNs());
        trafficShiftingDto.setSwitchStatus(ruleOverview.getSwitchStatus().toString());
        trafficShiftingDto.setDataStatus(ruleOverview.getDataStatus().toString());
        trafficShiftingDto.setCreateTime(ruleOverview.getCreateTime());
        List<TrafficShiftingDesServiceDto> desServices = new ArrayList<>();
        List<TrafficShiftingMatchDto> matches = new ArrayList<>();
        httpDBDetail.forEach(httpRoute -> {
            List<HTTPMatchRequest> match = httpRoute.getMatch();
            if (CollectionUtils.isNotEmpty(match)) {
                TrafficShiftingMatchDto matchDto = new TrafficShiftingMatchDto();
                match.forEach(httpMatchRequest -> {
                    if (Objects.nonNull(httpMatchRequest.getHeaders()) && !httpMatchRequest.getHeaders().isEmpty()) {
                        List<String> headers = new ArrayList<>();
                        Set<String> keySet = httpMatchRequest.getHeaders().keySet();
                        keySet.forEach(key -> {
                            StringMatch stringMatch = httpMatchRequest.getHeaders().get(key);
                            headers.add(key + "=" + stringMatch.getExact());
                        });
                        matchDto.setHeaders(headers);
                    }
                    if (Objects.nonNull(httpMatchRequest.getSourceLabels()) && !httpMatchRequest.getSourceLabels().isEmpty()) {
                        if (StringUtils.isNotBlank(httpMatchRequest.getSourceLabels().get("app"))) {
                            matchDto.setSourceName(httpMatchRequest.getSourceLabels().get("app"));
                        }
                        if (StringUtils.isNotBlank(httpMatchRequest.getSourceLabels().get("version"))) {
                            matchDto.setSourceVersion(httpMatchRequest.getSourceLabels().get("version"));
                        }
                    }
                    matchDto.setSubset(httpRoute.getRoute().get(0).getDestination().getSubset());
                });
                matches.add(matchDto);
            } else {
                List<DestinationWeight> route = httpRoute.getRoute();
                if (CollectionUtils.isNotEmpty(route) && route.size() > 1) {
                    route.forEach(destinationWeight -> {
                        TrafficShiftingDesServiceDto trafficShiftingDesService = new TrafficShiftingDesServiceDto();
                        trafficShiftingDesService.setSubset(destinationWeight.getDestination().getSubset());
                        if (Objects.nonNull(destinationWeight.getWeight())) {
                            trafficShiftingDesService.setWeight(destinationWeight.getWeight().toString());
                        }
                        desServices.add(trafficShiftingDesService);
                    });
                }
            }
        });
        trafficShiftingDto.setMatches(matches);
        trafficShiftingDto.setDesServices(desServices);
        return ActionReturnUtil.returnSuccessWithData(trafficShiftingDto);
    }
}
