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
import com.harmonycloud.dto.application.istio.CircuitBreakDto;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.*;
import com.harmonycloud.k8s.service.istio.DestinationRuleService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.istio.IstioCircuitBreakerService;
import com.harmonycloud.service.istio.util.IstioPolicyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * create by weg on 18-12-27.
 */
@Service
public class IstioCircuitBreakerServiceImpl implements IstioCircuitBreakerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IstioCircuitBreakerServiceImpl.class);

    @Value("#{propertiesReader['istio.redis.address']}")
    private String istioRedisAddress;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private DestinationRuleService destinationRuleService;

    @Autowired
    private com.harmonycloud.k8s.service.NamespaceService ns;

    @Autowired
    private RuleOverviewMapper ruleOverviewMapper;

    @Autowired
    private RuleDetailMapper ruleDetailMapper;

    @Autowired
    private HttpSession session;

    //熔断策略
    @Override
    public ActionReturnUtil createCircuitBreakerPolicy(String deployName, CircuitBreakDto circuitBreakDto) throws Exception {
        AssertUtil.notNull(circuitBreakDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(circuitBreakDto.getClusterId(), DictEnum.CLUSTER_ID);
        String namespace = circuitBreakDto.getNamespace();
        Cluster cluster = clusterService.findClusterById(circuitBreakDto.getClusterId());
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);

        //是否存在只判断数据库
        if (IstioPolicyUtil.checkPolicyExist(cluster.getId(), namespace, deployName, circuitBreakDto.getRuleName(), circuitBreakDto.getRuleType(), ruleOverviewMapper)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_EXIST);
        }
        String ruleId = UUIDUtil.getUUID();
        IstioPolicyUtil.insertRuleOverview(circuitBreakDto, ruleId, cluster.getId(), userName, ruleOverviewMapper);
        RuleDetail ruleDetail = IstioPolicyUtil.insertRuleDetail(circuitBreakDto, ruleId, ruleDetailMapper);

        K8SClientResponse response = destinationRuleService.getDestinationRule(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            IstioPolicyUtil.updateRuleOverview(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, CommonConstant.DATA_IS_ERROR, CommonConstant.DATA_IS_ERROR, userName, ruleOverviewMapper);
            LOGGER.error("get DestinationRule error", response.getBody());
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.DESTINATIONRULE_GET_FAILED);
        }
        DestinationRule destinationRule = JsonUtil.jsonToPojo(response.getBody(), DestinationRule.class);
        destinationRule.getSpec().setTrafficPolicy(JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), TrafficPolicy.class));
        //更新CircuitBreaker
        K8SClientResponse updateResponse = destinationRuleService.updateDestinationRule(namespace, deployName, destinationRule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
            IstioPolicyUtil.updateRuleOverview(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, CommonConstant.DATA_IS_ERROR, CommonConstant.DATA_IS_ERROR, userName, ruleOverviewMapper);
            UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnSuccessWithData(status.getMessage());
        }
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_SUCCESS);
    }

    @Override
    public ActionReturnUtil updateCircuitBreakerPolicy(String ruleId, CircuitBreakDto circuitBreakDto) throws Exception {
        AssertUtil.notNull(circuitBreakDto.getRuleName(), DictEnum.NAME);
        AssertUtil.notNull(circuitBreakDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(circuitBreakDto.getClusterId(), DictEnum.CLUSTER_ID);
        AssertUtil.notNull(ruleId, DictEnum.RULE_ID);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        RuleDetail ruleDetail = IstioPolicyUtil.makeCircuitBreakerRuleDetail(circuitBreakDto, ruleId);
        ruleDetail.setUpdateTime(new Date());
        ruleDetailMapper.updateByPrimaryKeySelective(ruleDetail);
        RuleOverview updateRuleOverview = new RuleOverview();
        updateRuleOverview.setRuleId(ruleId);
        updateRuleOverview.setUserName(userName);
        updateRuleOverview.setUpdateTime(new Date());
        ruleOverviewMapper.updateByPrimaryKeySelective(updateRuleOverview);
        //开关开启状态下更新
        if (IstioPolicyUtil.checkPolicyStatus(ruleId, ruleOverviewMapper)) {
            String namespace = circuitBreakDto.getNamespace();
            Cluster cluster = clusterService.findClusterById(circuitBreakDto.getClusterId());
            //获取集群中DestinationRule
            K8SClientResponse response = destinationRuleService.getDestinationRule(namespace, circuitBreakDto.getServiceName(), cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                LOGGER.error("get DestinationRule error", response.getBody());
                return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.DESTINATIONRULE_GET_FAILED);
            }
            DestinationRule destinationRule = JsonUtil.jsonToPojo(response.getBody(), DestinationRule.class);
            destinationRule.getSpec().setTrafficPolicy(JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), TrafficPolicy.class));
            //更新DestinationRule
            K8SClientResponse updateResponse = destinationRuleService.updateDestinationRule(namespace, circuitBreakDto.getServiceName(), destinationRule, cluster);
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
    public ActionReturnUtil closeCircuitBreakerPolicy(String namespace, String ruleId, String deployName, String clusterId) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(clusterId, DictEnum.CLUSTER_ID);
        Cluster cluster = clusterService.findClusterById(clusterId);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();

        K8SClientResponse response = destinationRuleService.getDestinationRule(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_IS_ERROR, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
            LOGGER.error("get DestinationRule error", response.getBody());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DESTINATIONRULE_GET_FAILED);
        }
        DestinationRule destinationRule = JsonUtil.jsonToPojo(response.getBody(), DestinationRule.class);

        destinationRule.getSpec().setTrafficPolicy(null);

        K8SClientResponse updateResponse = destinationRuleService.updateDestinationRule(namespace, deployName, destinationRule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_IS_ERROR, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
            UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        IstioPolicyUtil.updateRuleOverview(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, CommonConstant.DATA_IS_OK, CommonConstant.DATA_IS_OK, userName, ruleOverviewMapper);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CLOSE_SUCCESS);
    }

    @Override
    public ActionReturnUtil openCircuitBreakerPolicy(String namespace, String ruleId, String deployName, String clusterId) throws Exception {
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
        K8SClientResponse response = destinationRuleService.getDestinationRule(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_IS_ERROR, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
            LOGGER.error("get DestinationRule error", response.getBody());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DESTINATIONRULE_GET_FAILED);
        }
        DestinationRule destinationRule = JsonUtil.jsonToPojo(response.getBody(), DestinationRule.class);
        destinationRule.getSpec().setTrafficPolicy(JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), TrafficPolicy.class));
        K8SClientResponse updateResponse = destinationRuleService.updateDestinationRule(namespace, deployName, destinationRule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_IS_ERROR, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
            UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
            LOGGER.error(status.getMessage());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        IstioPolicyUtil.updateRuleOverview(ruleId, CommonConstant.ISTIO_POLICY_OPEN, CommonConstant.DATA_IS_OK, CommonConstant.DATA_IS_OK, userName, ruleOverviewMapper);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_OPEN_SUCCESS);
    }

    @Override
    public ActionReturnUtil deleteCircuitBreakerPolicy(String namespace, String ruleId, String deployName, String clusterId) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        Cluster cluster = clusterService.findClusterById(clusterId);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();

        K8SClientResponse response = destinationRuleService.getDestinationRule(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_IS_ERROR, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
            LOGGER.error("get DestinationRule error", response.getBody());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DESTINATIONRULE_GET_FAILED);
        }
        DestinationRule destinationRule = JsonUtil.jsonToPojo(response.getBody(), DestinationRule.class);
        if (Objects.nonNull(destinationRule.getSpec().getTrafficPolicy())) {
            destinationRule.getSpec().setTrafficPolicy(null);
            //更新CircuitBreaker
            K8SClientResponse updateResponse = destinationRuleService.updateDestinationRule(namespace, deployName, destinationRule, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_IS_ERROR, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                throw new MarsRuntimeException(status.getMessage());
            }
        }
        ruleOverviewMapper.deleteByPrimaryKey(ruleId);
        ruleDetailMapper.deleteByPrimaryKey(ruleId);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_DELETE_SUCCESS);
    }

    @Override
    public ActionReturnUtil listIstioPolicies(String deployName, String namespace, String ruleType, String clusterId) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = clusterService.findClusterById(clusterId);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        Map<Object, Object> ruleInfo = new HashMap<>();
        ruleInfo.put("ruleClusterId", cluster.getId());
        ruleInfo.put("ruleNs", namespace);
        if (StringUtils.isNotBlank(deployName)) {
            ruleInfo.put("ruleSvc", deployName);
        }
        if (StringUtils.isNotBlank(ruleType)) {
            ruleInfo.put("ruleType", ruleType);
        }
        List<RuleOverview> ruleOverviews = ruleOverviewMapper.selectByRuleInfo(ruleInfo);
        return ActionReturnUtil.returnSuccessWithData(ruleOverviews);
    }

    @Override
    public ActionReturnUtil getCircuitBreakerPolicy(String namespace, String ruleId, String deployName, String clusterId) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (Objects.isNull(ruleDetails) || ruleDetails.size() != 1) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        RuleDetail ruleDetail = ruleDetails.get(0);
        TrafficPolicy trafficPolicyDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), TrafficPolicy.class);
        if (Objects.isNull(trafficPolicyDBDetail)) {
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        try {
            K8SClientResponse response = destinationRuleService.getDestinationRule(namespace, deployName, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                LOGGER.error("get DestinationRule error", response.getBody());
            } else {
                DestinationRule destinationRule = JsonUtil.jsonToPojo(response.getBody(), DestinationRule.class);
                if (Objects.isNull(destinationRule.getSpec()) ||
                        Objects.isNull(destinationRule.getSpec().getTrafficPolicy())) {
                    if (ruleOverview.getSwitchStatus() == CommonConstant.ISTIO_POLICY_OPEN) {
                        IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                        ruleOverview.setSwitchStatus(CommonConstant.K8S_NO_DATA);
                    }
                } else {
                    if (!trafficPolicyDBDetail.equals(destinationRule.getSpec().getTrafficPolicy())) {
                        IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userName, CommonConstant.DATA_IS_ERROR, ruleOverviewMapper);
                        ruleOverview.setDataStatus(CommonConstant.DATA_NOT_SAME);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        CircuitBreakDto circuitBreakDto = new CircuitBreakDto();
        circuitBreakDto.setRuleId(ruleId);
        circuitBreakDto.setRuleName(ruleOverview.getRuleName());
        circuitBreakDto.setRuleType(ruleOverview.getRuleType());
        circuitBreakDto.setNamespace(ruleOverview.getRuleNs());
        circuitBreakDto.setServiceName(ruleOverview.getRuleSvc());
        circuitBreakDto.setSwitchStatus(ruleOverview.getSwitchStatus().toString());
        circuitBreakDto.setDataStatus(ruleOverview.getDataStatus().toString());
        ConnectionPool connectionPool = trafficPolicyDBDetail.getConnectionPool();
        if (Objects.nonNull(connectionPool)) {
            TcpConnection tcpConnection = connectionPool.getTcp();
            if (Objects.nonNull(tcpConnection)) {
                circuitBreakDto.setMaxConnections(tcpConnection.getMaxConnections());
            }
            HttpConnection httpConnection = connectionPool.getHttp();
            if (Objects.nonNull(httpConnection)) {
                circuitBreakDto.setHttp1MaxPendingRequests(httpConnection.getHttp1MaxPendingRequests());
                circuitBreakDto.setHttp2MaxRequests(httpConnection.getHttp2MaxRequests());
                circuitBreakDto.setMaxRequestsPerConnection(httpConnection.getMaxRequestsPerConnection());
            }
        }
        OutlierDetection outlierDetection = trafficPolicyDBDetail.getOutlierDetection();
        if (Objects.nonNull(outlierDetection)) {
            if (outlierDetection.getBaseEjectionTime() != null) {
                circuitBreakDto.setBaseEjectionTime(Integer.valueOf(outlierDetection.getBaseEjectionTime().replace(CommonConstant.SECOND, "")));
            }
            circuitBreakDto.setConsecutiveErrors(outlierDetection.getConsecutiveErrors());
            if (outlierDetection.getInterval() != null) {
                circuitBreakDto.setInterval(Integer.valueOf(outlierDetection.getInterval().replace(CommonConstant.SECOND, "")));
            }
        }
        return ActionReturnUtil.returnSuccessWithData(circuitBreakDto);
    }

}
