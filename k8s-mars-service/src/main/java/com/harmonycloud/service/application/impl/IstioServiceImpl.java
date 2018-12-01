package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.istio.IstioGlobalConfigureMapper;
import com.harmonycloud.dao.istio.RuleDetailMapper;
import com.harmonycloud.dao.istio.RuleOverviewMapper;
import com.harmonycloud.dao.istio.bean.IstioGlobalConfigure;
import com.harmonycloud.dao.istio.bean.RuleDetail;
import com.harmonycloud.dao.istio.bean.RuleOverview;
import com.harmonycloud.dto.application.istio.*;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.policies.Action;
import com.harmonycloud.k8s.bean.istio.policies.Rule;
import com.harmonycloud.k8s.bean.istio.policies.RuleSpec;
import com.harmonycloud.k8s.bean.istio.policies.ratelimit.*;
import com.harmonycloud.k8s.bean.istio.policies.whitelists.ListChecker;
import com.harmonycloud.k8s.bean.istio.policies.whitelists.ListCheckerSpec;
import com.harmonycloud.k8s.bean.istio.policies.whitelists.ListEntry;
import com.harmonycloud.k8s.bean.istio.policies.whitelists.ListEntrySpec;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.*;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.istio.CircuitBreakerService;
import com.harmonycloud.k8s.service.istio.RateLimitService;
import com.harmonycloud.k8s.service.istio.WhiteListsService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.application.IstioService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.NamespaceService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class IstioServiceImpl implements IstioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IstioServiceImpl.class);

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private WhiteListsService whiteListsService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private CircuitBreakerService circuitBreakerService;

    @Autowired
    private JedisConnectionFactory jedisConnectionFactory;

    @Autowired
    private com.harmonycloud.k8s.service.DeploymentService ds;

    @Autowired
    private com.harmonycloud.k8s.service.NamespaceService ns;
    @Autowired
    private IstioGlobalConfigureMapper istioGlobalConfigureMapper;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private RuleOverviewMapper ruleOverviewMapper;

    @Autowired
    private RuleDetailMapper ruleDetailMapper;

    @Autowired
    private HttpSession session;

    @Override
    public ActionReturnUtil createCircuitBreakerPolicy(String deployName, CircuitBreakDto circuitBreakDto) throws Exception {
        AssertUtil.notNull(circuitBreakDto.getNamespace(), DictEnum.NAMESPACE);
        String namespace = circuitBreakDto.getNamespace();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        String userId = session.getAttribute(CommonConstant.USERID).toString();
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);

        //是否存在只判断数据库
        if (checkPolicyExist(cluster.getId(), namespace, deployName, circuitBreakDto.getRuleName(), circuitBreakDto.getRuleType())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_EXIST);
        }
        String ruleId = UUIDUtil.getUUID();
        insertRuleOverview(circuitBreakDto, ruleId, cluster.getId(), userId);
        RuleDetail ruleDetail = insertRuleDetail(circuitBreakDto, ruleId);

        K8SClientResponse response = circuitBreakerService.getCircuitBreakerPolicy(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, userId);
            LOGGER.error("get DestinationRule error", response.getBody());
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        DestinationRule destinationRule = JsonUtil.jsonToPojo(response.getBody(), DestinationRule.class);
        destinationRule.getSpec().setTrafficPolicy(JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), TrafficPolicy.class));
        //更新CircuitBreaker
        K8SClientResponse updateResponse = circuitBreakerService.updateCircuitBreakerPolicy(namespace, deployName, destinationRule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
            updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, userId);
            UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_SUCCESS);
    }

    @Override
    public ActionReturnUtil updateCircuitBreakerPolicy(String ruleId, CircuitBreakDto circuitBreakDto) throws Exception {
        AssertUtil.notNull(circuitBreakDto.getRuleName(), DictEnum.NAME);
        AssertUtil.notNull(circuitBreakDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(ruleId, DictEnum.RULE_ID);
        String userId = session.getAttribute("userId").toString();
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        if (ruleOverview.getDataStatus() != CommonConstant.DATA_IS_OK) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DATA_STATUS_ERROR);
        }
        RuleDetail ruleDetail = makeCircuitBreakerRuleDetail(circuitBreakDto, ruleId);
        ruleDetailMapper.updateByPrimaryKeySelective(ruleDetail);
        RuleOverview updateRuleOverview = new RuleOverview();
        updateRuleOverview.setRuleId(ruleId);
        updateRuleOverview.setUserId(userId);
        ruleOverviewMapper.updateByPrimaryKeySelective(updateRuleOverview);
        //开关开启状态下更新
        if (checkPolicyStatus(ruleId)) {
            String namespace = circuitBreakDto.getNamespace();
            Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
            //获取集群中DestinationRule
            K8SClientResponse response = circuitBreakerService.getCircuitBreakerPolicy(namespace, circuitBreakDto.getServiceName(), cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userId, 0);
                LOGGER.error("get DestinationRule error", response.getBody());
                return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
            }
            DestinationRule destinationRule = JsonUtil.jsonToPojo(response.getBody(), DestinationRule.class);
            destinationRule.getSpec().setTrafficPolicy(JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), TrafficPolicy.class));
            //更新DestinationRule
            K8SClientResponse updateResponse = circuitBreakerService.updateCircuitBreakerPolicy(namespace, circuitBreakDto.getServiceName(), destinationRule, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                    updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userId, 0);
                UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                LOGGER.error(status.getMessage());
                return ActionReturnUtil.returnSuccessWithData(status.getMessage());
            }
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_UPDATE_SUCCESS);
        } else {
            //开关关闭状态下更新
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_UPDATE_SUCCESS);
        }
    }

    @Override
    public ActionReturnUtil closeCircuitBreakerPolicy(String namespace, String ruleId, String deployName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userId = session.getAttribute("userId").toString();

        K8SClientResponse response = circuitBreakerService.getCircuitBreakerPolicy(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("get DestinationRule error", response.getBody());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        DestinationRule destinationRule = JsonUtil.jsonToPojo(response.getBody(), DestinationRule.class);

        destinationRule.getSpec().setTrafficPolicy(null);

        K8SClientResponse updateResponse = circuitBreakerService.updateCircuitBreakerPolicy(namespace, deployName, destinationRule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
            updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userId, 0);
            UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        updateRuleOverview(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, CommonConstant.DATA_IS_OK, 0, userId);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CLOSE_SUCCESS);
    }

    @Override
    public ActionReturnUtil openCircuitBreakerPolicy(String namespace, String ruleId, String deployName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);

        String userId = session.getAttribute("userId").toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (CollectionUtils.isEmpty(ruleDetails)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        K8SClientResponse response = circuitBreakerService.getCircuitBreakerPolicy(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userId, 0);
            LOGGER.error("get DestinationRule error", response.getBody());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        DestinationRule destinationRule = JsonUtil.jsonToPojo(response.getBody(), DestinationRule.class);
        K8SClientResponse updateResponse = circuitBreakerService.updateCircuitBreakerPolicy(namespace, deployName, destinationRule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
            updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userId, 0);
            UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        updateRuleOverview(ruleId, CommonConstant.ISTIO_SWITCH_OPEN, CommonConstant.DATA_IS_OK, 0, userId);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_OPEN_SUCCESS);
    }

    @Override
    public ActionReturnUtil deleteCircuitBreakerPolicy(String namespace, String ruleId, String deployName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userId = session.getAttribute("userId").toString();

        K8SClientResponse response = circuitBreakerService.getCircuitBreakerPolicy(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("get DestinationRule error", response.getBody());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        DestinationRule destinationRule = JsonUtil.jsonToPojo(response.getBody(), DestinationRule.class);
        if (Objects.nonNull(destinationRule.getSpec().getTrafficPolicy())) {
            destinationRule.getSpec().setTrafficPolicy(null);
            //更新CircuitBreaker
            K8SClientResponse updateResponse = circuitBreakerService.updateCircuitBreakerPolicy(namespace, deployName, destinationRule, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userId, 0);
                UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                throw new MarsRuntimeException(status.getMessage());
            }
        }
        ruleOverviewMapper.deleteByPrimaryKey(ruleId);
        ruleDetailMapper.deleteByPrimaryKey(ruleId);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_DELETE_SUCCESS);
    }

    @Override
    public ActionReturnUtil listIstioPolicies(String deployName, String namespace, String ruleType) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        Map<Object, Object> ruleInfo = new HashMap<>();
        ruleInfo.put("ruleClusterId", cluster.getId());
        ruleInfo.put("ruleNs", namespace);
        if (StringUtils.isNotEmpty(deployName)) {
            ruleInfo.put("ruleSvc", deployName);
        }
        if (StringUtils.isNotEmpty(ruleType)) {
            ruleInfo.put("ruleType", ruleType);
        }
        List<RuleOverview> ruleOverviews = ruleOverviewMapper.selectByRuleInfo(ruleInfo);
        return ActionReturnUtil.returnSuccessWithData(ruleOverviews);
    }

    @Override
    public ActionReturnUtil getCircuitBreakerPolicy(String namespace, String ruleId, String deployName) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userId = session.getAttribute("userId").toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (Objects.isNull(ruleDetails) || ruleDetails.size() != 1) {
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
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
            K8SClientResponse response = circuitBreakerService.getCircuitBreakerPolicy(namespace, deployName, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userId, 0);
                LOGGER.error("get DestinationRule error", response.getBody());
            } else {
                DestinationRule destinationRule = JsonUtil.jsonToPojo(response.getBody(), DestinationRule.class);
                if (Objects.isNull(destinationRule.getSpec()) ||
                        Objects.isNull(destinationRule.getSpec().getTrafficPolicy())) {
                    if (ruleOverview.getSwitchStatus() == CommonConstant.ISTIO_SWITCH_OPEN) {
                        updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userId, 1);
                        ruleOverview.setSwitchStatus(CommonConstant.K8S_NO_DATA);
                    }
                } else {
                    if (!trafficPolicyDBDetail.equals(destinationRule.getSpec().getTrafficPolicy())) {
                        updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userId, 1);
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

    /**
     * 只做服务级别的限流，限流针对服务不做版本区分
     * @param deployName
     * @param rateLimitDto
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil createRateLimitPolicy(String deployName, RateLimitDto rateLimitDto) throws Exception {
        AssertUtil.notNull(rateLimitDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        String namespace = rateLimitDto.getNamespace();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        String userId = session.getAttribute(CommonConstant.USERID).toString();
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        List<String> algorithmList = Arrays.asList("FIXED_WINDOW", "ROLLING_WINDOW");
        if (!algorithmList.contains(rateLimitDto.getAlgorithm())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FRONT_DATA_ERROR);
        }
        //是否存在只判断数据库
        if (checkPolicyExist(cluster.getId(), namespace, deployName, rateLimitDto.getRuleName(), rateLimitDto.getRuleType())) {
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_EXIST);
        }
        String ruleId = UUIDUtil.getUUID();
        insertRuleOverview(rateLimitDto, ruleId, cluster.getId(), userId);
        RuleDetail quotaRuleDetail = makeRateLimitQuotaRuleDetail(rateLimitDto, ruleId);
        ruleDetailMapper.insert(quotaRuleDetail);
        RuleDetail redisQuotaRuleDetail = makeRateLimitRedisQuotaRuleDetail(rateLimitDto, ruleId);
        ruleDetailMapper.insert(redisQuotaRuleDetail);
        RuleDetail quotaSpecRuleDetail = makeRateLimitQuotaSpecRuleDetail(rateLimitDto, ruleId);
        ruleDetailMapper.insert(quotaSpecRuleDetail);
        RuleDetail quotaSpecBindingRuleDetail = makeRateLimitQuotaSpecBindingRuleDetail(rateLimitDto, ruleId);
        ruleDetailMapper.insert(quotaSpecBindingRuleDetail);
        RuleDetail ruleRuleDetail = makeRateLimitRuleRuleDetail(rateLimitDto, ruleId);
        ruleDetailMapper.insert(ruleRuleDetail);
        //创建Quota
        QuotaInstance quotaInstance = JsonUtil.jsonToPojo(new String(quotaRuleDetail.getRuleDetailContent()), QuotaInstance.class);
        K8SClientResponse quotaResponse = rateLimitService.createQuota(namespace, quotaInstance, cluster);
        if (!HttpStatusUtil.isSuccessStatus(quotaResponse.getStatus())) {
            LOGGER.error("create QuotaInstance error", quotaResponse.getBody());
            updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, userId);
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_FAILED);
        }
        //创建redisQuota
        RedisQuota redisQuota = JsonUtil.jsonToPojo(new String(redisQuotaRuleDetail.getRuleDetailContent()), RedisQuota.class);
        K8SClientResponse redisQuotaResponse = rateLimitService.createRedisQuota(namespace, redisQuota, cluster);
        if (!HttpStatusUtil.isSuccessStatus(redisQuotaResponse.getStatus())) {
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, rateLimitDto.getRuleName(), cluster, redisQuotaRuleDetail.getRuleDetailOrder() - 1);
            if (res.isEmpty()) {
                updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, userId);
            } else {
                updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userId, Integer.valueOf(res.get("faileNum").toString()));
            }
            LOGGER.error("create ratelimit policy error", redisQuotaResponse.getBody());
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_FAILED);
        }
        //创建QuotaSpec
        QuotaSpec quotaSpec = JsonUtil.jsonToPojo(new String(quotaSpecRuleDetail.getRuleDetailContent()), QuotaSpec.class);
        K8SClientResponse quotaSpecResponse = rateLimitService.createQuotaSpec(namespace, quotaSpec, cluster);
        if (!HttpStatusUtil.isSuccessStatus(quotaSpecResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", quotaSpecResponse.getBody());
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, rateLimitDto.getRuleName(), cluster, quotaSpecRuleDetail.getRuleDetailOrder() - 1);
            if (res.isEmpty()) {
                updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, userId);
            } else {
                updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userId, Integer.valueOf(res.get("faileNum").toString()));
            }
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_FAILED);
        }
        //创建QuotaSpecBinding
        QuotaSpecBinding quotaSpecBinding = JsonUtil.jsonToPojo(new String(quotaSpecBindingRuleDetail.getRuleDetailContent()), QuotaSpecBinding.class);
        K8SClientResponse quotaSpecBindingResponse = rateLimitService.createQuotaSpecBinding(namespace, quotaSpecBinding, cluster);
        if (!HttpStatusUtil.isSuccessStatus(quotaSpecBindingResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", quotaSpecBindingResponse.getBody());
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, rateLimitDto.getRuleName(), cluster, quotaSpecBindingRuleDetail.getRuleDetailOrder() - 1);
            if (res.isEmpty()) {
                updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, userId);
            } else {
                updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userId, Integer.valueOf(res.get("faileNum").toString()));
            }
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_FAILED);
        }
        //创建rule
        Rule rule = JsonUtil.jsonToPojo(new String(ruleRuleDetail.getRuleDetailContent()), Rule.class);
        K8SClientResponse ruleResponse = rateLimitService.createRule(namespace, rule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(ruleResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", ruleResponse.getBody());
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, rateLimitDto.getRuleName(), cluster, ruleRuleDetail.getRuleDetailOrder() - 1);
            if (res.isEmpty()) {
                updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, userId);
            } else {
                updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userId, Integer.valueOf(res.get("faileNum").toString()));
            }
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_FAILED);
        }
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_SUCCESS);
    }

    @Override
    public ActionReturnUtil updateRateLimitPolicy(String ruleId, RateLimitDto rateLimitDto) throws Exception {
        AssertUtil.notNull(rateLimitDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(rateLimitDto.getRuleName(), DictEnum.NAME);
        String namespace = rateLimitDto.getNamespace();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String ruleName = rateLimitDto.getRuleName();
        String userId = session.getAttribute("userId").toString();
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        if (ruleOverview.getDataStatus() != CommonConstant.DATA_IS_OK) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DATA_STATUS_ERROR);
        }
        RuleDetail redisQuotaRuleDetail = makeRateLimitRedisQuotaRuleDetail(rateLimitDto, ruleId);
        ruleDetailMapper.updateByPrimaryKeySelective(redisQuotaRuleDetail);
        if (!ruleOverview.getRuleScope().equals(rateLimitDto.getScope())) {
            RuleOverview updateRuleScope = new RuleOverview();
            updateRuleScope.setRuleId(ruleId);
            updateRuleScope.setRuleScope(rateLimitDto.getScope());
            updateRuleScope.setUserId(userId);
            ruleOverviewMapper.updateByPrimaryKeySelective(updateRuleScope);
        }
        //开关开启状态下更新
        if (checkPolicyStatus(ruleId)) {
            //更新时只更新redisQuota和rule
            RedisQuota redisQuota = JsonUtil.jsonToPojo(new String(redisQuotaRuleDetail.getRuleDetailContent()), RedisQuota.class);
            K8SClientResponse response = rateLimitService.getRateLimitResource(namespace, ruleName, CommonConstant.RATE_LIMIT_REDIS_QUOTA, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                LOGGER.error("get ratelimit policy error", response.getBody());
                updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userId,redisQuotaRuleDetail.getRuleDetailOrder());
                return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
            }
            RedisQuota redisQuotaData = JsonUtil.jsonToPojo(response.getBody(), RedisQuota.class);
            redisQuotaData.getSpec().setQuotas(redisQuota.getSpec().getQuotas());
            K8SClientResponse redisQuotaResponse = rateLimitService.updateRedisQuota(namespace, ruleName, redisQuotaData, cluster);
            if (!HttpStatusUtil.isSuccessStatus(redisQuotaResponse.getStatus())) {
                LOGGER.error("update rateLimit redisquota error", redisQuotaResponse.getBody());
                updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userId, redisQuotaRuleDetail.getRuleDetailOrder());
                return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_UPDATE_FAILED);
            }
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_UPDATE_SUCCESS);
        } else {
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_UPDATE_SUCCESS);
        }
    }

    @Override
    public ActionReturnUtil closeRateLimitPolicy(String namespace, String ruleId, String ruleName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userId = session.getAttribute("userId").toString();
        Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, ruleName, cluster, 5);
        if (res.isEmpty()) {
            updateRuleOverview(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, CommonConstant.DATA_IS_OK, 0, userId);
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CLOSE_SUCCESS);
        } else {
            updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userId, Integer.valueOf((res.get("faileNum") == null?"0":res.get("faileNum")).toString()));
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_CLOSE_FAILED);
        }
    }

    @Override
    public ActionReturnUtil openRateLimitPolicy(String namespace, String ruleId, String ruleName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userId = session.getAttribute("userId").toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (CollectionUtils.isEmpty(ruleDetails) || ruleDetails.size() < 5) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        QuotaInstance quotaInstance = new QuotaInstance();
        RedisQuota redisQuota = new RedisQuota();
        QuotaSpec quotaSpec = new QuotaSpec();
        QuotaSpecBinding quotaSpecBinding = new QuotaSpecBinding();
        Rule rule = new Rule();
        for (RuleDetail ruleDetail : ruleDetails) {
            switch (ruleDetail.getRuleDetailOrder()) {
                case 1:
                    quotaInstance = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), QuotaInstance.class);
                    break;
                case 2:
                    redisQuota = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), RedisQuota.class);
                    break;
                case 3:
                    quotaSpec = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), QuotaSpec.class);
                    break;
                case 4:
                    quotaSpecBinding = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), QuotaSpecBinding.class);
                    break;
                case 5:
                    rule = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), Rule.class);
                    break;
                default:
                    return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
            }
        }
        //创建Quota
        K8SClientResponse quotaResponse = rateLimitService.createQuota(namespace, quotaInstance, cluster);
        if (!HttpStatusUtil.isSuccessStatus(quotaResponse.getStatus())) {
            LOGGER.error("create QuotaInstance error", quotaResponse.getBody());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        //创建redisQuota
        K8SClientResponse redisQuotaResponse = rateLimitService.createRedisQuota(namespace, redisQuota, cluster);
        if (!HttpStatusUtil.isSuccessStatus(redisQuotaResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", redisQuotaResponse.getBody());
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, ruleName, cluster, 1);
            updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userId, Integer.valueOf(res.get("faileNum").toString()));
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        //创建QuotaSpec
        K8SClientResponse quotaSpecResponse = rateLimitService.createQuotaSpec(namespace, quotaSpec, cluster);
        if (!HttpStatusUtil.isSuccessStatus(quotaSpecResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", quotaSpecResponse.getBody());
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, ruleName, cluster, 2);
            updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userId, Integer.valueOf(res.get("faileNum").toString()));
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        //创建QuotaSpecBinding
        K8SClientResponse quotaSpecBindingResponse = rateLimitService.createQuotaSpecBinding(namespace, quotaSpecBinding, cluster);
        if (!HttpStatusUtil.isSuccessStatus(quotaSpecBindingResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", quotaSpecBindingResponse.getBody());
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, ruleName, cluster, 3);
            updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userId, Integer.valueOf(res.get("faileNum").toString()));
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        //创建rule
        K8SClientResponse ruleResponse = rateLimitService.createRule(namespace, rule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(ruleResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", ruleResponse.getBody());
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, ruleName, cluster, 4);
            updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userId, Integer.valueOf(res.get("faileNum").toString()));
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        updateRuleOverview(ruleId, CommonConstant.ISTIO_SWITCH_OPEN, CommonConstant.DATA_IS_OK, 0, userId);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_OPEN_SUCCESS);
    }

    @Override
    public ActionReturnUtil deleteRateLimitPolicy(String namespace, String ruleId, String ruleName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(ruleName, DictEnum.NAME);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userId = session.getAttribute("userId").toString();
        Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, ruleName, cluster, 5);
        if (res.isEmpty()) {
            ruleOverviewMapper.deleteByPrimaryKey(ruleId);
            ruleDetailMapper.deleteByPrimaryKey(ruleId);
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_DELETE_SUCCESS);
        } else {
            updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userId, Integer.valueOf((res.get("faileNum") == null?"0":res.get("faileNum")).toString()));
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_DELETE_FAILED);
        }
    }

    @Override
    public ActionReturnUtil getRateLimitPolicy(String namespace, String ruleId, String ruleName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userId = session.getAttribute("userId").toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (CollectionUtils.isEmpty(ruleDetails) || ruleDetails.size() < 5) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        QuotaInstance quotaInstanceDBDetail = new QuotaInstance();
        RedisQuota redisQuotaDBDetail = new RedisQuota();
        QuotaSpec quotaSpecDBDetail = new QuotaSpec();
        QuotaSpecBinding quotaSpecBindingDBDetail = new QuotaSpecBinding();
        Rule ruleDBDetail = new Rule();
        for (RuleDetail ruleDetail : ruleDetails) {
            switch (ruleDetail.getRuleDetailOrder()) {
                case 1:
                    quotaInstanceDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), QuotaInstance.class);
                    break;
                case 2:
                    redisQuotaDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), RedisQuota.class);
                    break;
                case 3:
                    quotaSpecDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), QuotaSpec.class);
                    break;
                case 4:
                    quotaSpecBindingDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), QuotaSpecBinding.class);
                    break;
                case 5:
                    ruleDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), Rule.class);
                    break;
                default:
                    return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
            }
        }
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        try {
            QuotaInstance quotaInstanceK8sDetail = new QuotaInstance();
            RedisQuota redisQuotaK8sDetail = new RedisQuota();
            QuotaSpec quotaSpecK8sDetail = new QuotaSpec();
            QuotaSpecBinding quotaSpecBindingK8sDetail = new QuotaSpecBinding();
            int flag = 0;
            K8SClientResponse ruleResponse = rateLimitService.getRateLimitResource(namespace, ruleName, CommonConstant.ISTIO_RULE, cluster);
            if (!HttpStatusUtil.isSuccessStatus(ruleResponse.getStatus())) {
                LOGGER.error("get rateLimit resource error", ruleResponse.getBody());
                flag = 5;
            }
            Rule ruleK8sDetail = JsonUtil.jsonToPojo(ruleResponse.getBody(), Rule.class);
            if (flag == 0) {
                K8SClientResponse quotaSpecBindingResponse = rateLimitService.getRateLimitResource(namespace, ruleName, CommonConstant.RATE_LIMIT_QUOTA_SPEC_BINDING, cluster);
                if (!HttpStatusUtil.isSuccessStatus(quotaSpecBindingResponse.getStatus())) {
                    LOGGER.error("get rateLimit resource error", quotaSpecBindingResponse.getBody());
                    flag = 4;
                }
                quotaSpecBindingK8sDetail = JsonUtil.jsonToPojo(quotaSpecBindingResponse.getBody(), QuotaSpecBinding.class);
            }
            if (flag == 0) {
                K8SClientResponse quotaSpecResponse = rateLimitService.getRateLimitResource(namespace, ruleName, CommonConstant.RATE_LIMIT_QUOTA_SPEC, cluster);
                if (!HttpStatusUtil.isSuccessStatus(quotaSpecResponse.getStatus())) {
                    LOGGER.error("get rateLimit resource error", quotaSpecResponse.getBody());
                    flag = 3;
                }
                quotaSpecK8sDetail = JsonUtil.jsonToPojo(quotaSpecResponse.getBody(), QuotaSpec.class);
            }
            if (flag == 0) {
                K8SClientResponse redisQuotaResponse = rateLimitService.getRateLimitResource(namespace, ruleName, CommonConstant.RATE_LIMIT_REDIS_QUOTA, cluster);
                if (!HttpStatusUtil.isSuccessStatus(redisQuotaResponse.getStatus())) {
                    LOGGER.error("get rateLimit resource error", redisQuotaResponse.getBody());
                    flag = 2;
                }
                redisQuotaK8sDetail = JsonUtil.jsonToPojo(redisQuotaResponse.getBody(), RedisQuota.class);
            }
            if (flag == 0) {
                K8SClientResponse quotaInstanceResponse = rateLimitService.getRateLimitResource(namespace, ruleName, CommonConstant.RATE_LIMIT_QUOTA, cluster);
                if (!HttpStatusUtil.isSuccessStatus(quotaInstanceResponse.getStatus())) {
                    LOGGER.error("get rateLimit resource error", quotaInstanceResponse.getBody());
                    flag = 1;
                }
                quotaInstanceK8sDetail = JsonUtil.jsonToPojo(quotaInstanceResponse.getBody(), QuotaInstance.class);
            }
            if (flag != 0) {
                if (ruleOverview.getSwitchStatus() == CommonConstant.ISTIO_SWITCH_OPEN) {
                    updateRuleOverviewDataStatus(ruleId, flag == 5 ? CommonConstant.K8S_NO_DATA : CommonConstant.DATA_NOT_COMPLETE, userId, flag);
                    ruleOverview.setDataStatus(CommonConstant.DATA_NOT_COMPLETE);
                    ruleOverview.setDataErrLoc(flag);
                }
            } else {
                if (!quotaInstanceDBDetail.equals(quotaInstanceK8sDetail)) {
                    flag = 1;
                }
                if (!redisQuotaDBDetail.equals(redisQuotaK8sDetail)) {
                    flag = 2;
                }
                if (!quotaSpecDBDetail.equals(quotaSpecK8sDetail)) {
                    flag = 3;
                }
                if (!quotaSpecBindingDBDetail.equals(quotaSpecBindingK8sDetail)) {
                    flag = 4;
                }
                if (!ruleDBDetail.equals(ruleK8sDetail)) {
                    flag = 5;
                }
                if (flag != 0) {
                    updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userId, flag);
                    ruleOverview.setDataStatus(CommonConstant.DATA_NOT_SAME);
                    ruleOverview.setDataErrLoc(flag);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        RateLimitDto rateLimitDto = new RateLimitDto();
        rateLimitDto.setRuleId(ruleId);
        rateLimitDto.setRuleName(ruleName);
        rateLimitDto.setRuleType(ruleOverview.getRuleType());
        rateLimitDto.setNamespace(ruleOverview.getRuleNs());
        rateLimitDto.setServiceName(ruleOverview.getRuleSvc());
        rateLimitDto.setScope(ruleOverview.getRuleScope());
        rateLimitDto.setSwitchStatus(ruleOverview.getSwitchStatus().toString());
        rateLimitDto.setDataStatus(ruleOverview.getDataStatus().toString());
        rateLimitDto.setCreateTime(ruleOverview.getCreateTime());
        List<RateLimitOverrideDto> overrideDtos = new ArrayList<>();
        List<QuotaOverride> overrides = redisQuotaDBDetail.getSpec().getQuotas().get(0).getOverrides();
        if (!"0".equals(rateLimitDto.getScope())) {
            overrides.forEach(override -> {
                RateLimitOverrideDto overrideDto = new RateLimitOverrideDto();
                overrideDto.setScopeNamespace(override.getDimensions().get("sourceNamespce"));
                if ("2".equals(rateLimitDto.getScope())) {
                    overrideDto.setScopeServiceName(override.getDimensions().get("sourceName"));
                }
                overrideDtos.add(overrideDto);
            });
            rateLimitDto.setOverrides(overrideDtos);
            rateLimitDto.setMaxAmount(overrides.get(0).getMaxAmount().toString());
        } else {
            rateLimitDto.setMaxAmount(redisQuotaDBDetail.getSpec().getQuotas().get(0).getMaxAmount().toString());
        }
        rateLimitDto.setValidDuration(redisQuotaDBDetail.getSpec().getQuotas().get(0).getValidDuration().replaceAll("s", ""));
        rateLimitDto.setAlgorithm(redisQuotaDBDetail.getSpec().getQuotas().get(0).getRateLimitAlgorithm());
        if (redisQuotaDBDetail.getSpec().getQuotas().get(0).getBucketDuration() != null &&
                "ROLLING_WINDOW".equalsIgnoreCase(redisQuotaDBDetail.getSpec().getQuotas().get(0).getRateLimitAlgorithm())) {
            rateLimitDto.setBucketDuration(redisQuotaDBDetail.getSpec().getQuotas().get(0).getBucketDuration().replaceAll("s", ""));
        }
        return ActionReturnUtil.returnSuccessWithData(rateLimitDto);
    }

    private RuleSpec convertRuleSpec(String ruleName, RateLimitDto rateLimitDto, String namespace) {
        Action action = new Action();
        action.setHandler(ruleName + ".redisquota." + namespace); //by ljf
        action.setInstances(Collections.singletonList(ruleName + ".quota." + namespace)); //by ljf
        RuleSpec ruleSpec = new RuleSpec();
        ruleSpec.setActions(Collections.singletonList(action));
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(rateLimitDto.getServiceName())) {
            sb.append("source.labels[\"app\"] == \"").append(rateLimitDto.getServiceName()).append("\" && ");
        }
        if (StringUtils.isNotEmpty(rateLimitDto.getUsername())) {
            sb.append("match(request.headers[\"cookie\"], \"user=").append(rateLimitDto.getUsername()).append("\") == true");
        }
        if (sb.length() > 0 && (sb.lastIndexOf("&&") == sb.length() - CommonConstant.NUM_TWO)) {
            sb.deleteCharAt(sb.length() - CommonConstant.NUM_TWO);
            ruleSpec.setMatch(sb.toString());
        }
        return ruleSpec;
    }

    @Override
    public ActionReturnUtil createWhiteListsPolicy(WhiteListsDto whiteListsDto) throws Exception {
        AssertUtil.notNull(whiteListsDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(whiteListsDto.getRuleName(), DictEnum.NAME); //策略名称
        AssertUtil.notNull(whiteListsDto.getServiceName(), DictEnum.DEPLOYMENT_NAME);
        String namespace = whiteListsDto.getNamespace();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String serviceName = whiteListsDto.getServiceName();
        String ruleName = whiteListsDto.getRuleName();
        String userId = session.getAttribute(CommonConstant.USERID).toString();
        //判断数据库是否存在策略信息
        if (checkPolicyExist(cluster.getId(), namespace, serviceName, whiteListsDto.getRuleName(),whiteListsDto.getRuleType())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_EXIST);
        }
        String ruleId = UUIDUtil.getUUID();
        insertRuleOverview(whiteListsDto, ruleId, cluster.getId(), userId);
        RuleDetail listCheckerRuleDetail = this.makeListCheckerRuleDetail(whiteListsDto, ruleId);
        ruleDetailMapper.insert(listCheckerRuleDetail);
        RuleDetail listEntryRuleDetail = this.makeListEntryDetail(whiteListsDto, ruleId);
        ruleDetailMapper.insert(listEntryRuleDetail);
        RuleDetail ruleRuleDetail = this.makeRuleRuleDetail(whiteListsDto, ruleId);
        ruleDetailMapper.insert(ruleRuleDetail);
        //build  listchecker
        ListChecker listChecker = JsonUtil.jsonToPojo(new String(listCheckerRuleDetail.getRuleDetailContent()), ListChecker.class);
        K8SClientResponse listCheckerResponse = whiteListsService.createListChecker(namespace, listChecker, cluster);
        if (!HttpStatusUtil.isSuccessStatus(listCheckerResponse.getStatus())) {
            //关闭开关
            this.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, userId);
            LOGGER.error("create whiteLists listChecker error", listCheckerResponse.getBody());
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_FAILED);
        }
        //build  listEntry
        ListEntry listEntry = JsonUtil.jsonToPojo(new String(listEntryRuleDetail.getRuleDetailContent()), ListEntry.class);
        K8SClientResponse listEntryResponse = whiteListsService.createListEntry(namespace, listEntry, cluster);
        if (!HttpStatusUtil.isSuccessStatus(listEntryResponse.getStatus())) {
            Map<String, Object> res = whiteListsService.deleteWhiteListsPolicy(namespace, whiteListsDto.getRuleName(), cluster, listEntryRuleDetail.getRuleDetailOrder() - 1);
            if (res.isEmpty()) {
                updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, userId);
            } else {
                updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userId, Integer.valueOf(res.get("faileNum").toString()));
            }
            LOGGER.error("create whiteLists listEntry error", listEntryResponse.getBody());
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_FAILED);
        }
        //build  rule
        Rule rule = JsonUtil.jsonToPojo(new String(ruleRuleDetail.getRuleDetailContent()), Rule.class);
        K8SClientResponse ruleResponse = whiteListsService.createWhiteRule(namespace, rule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(listEntryResponse.getStatus())) {
            Map<String, Object> res = whiteListsService.deleteWhiteListsPolicy(namespace, whiteListsDto.getRuleName(), cluster, ruleRuleDetail.getRuleDetailOrder() - 1);
            if (res.isEmpty()) {
                updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, userId);
            } else {
                updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userId, Integer.valueOf(res.get("faileNum").toString()));
            }
            LOGGER.error("create whiteLists listEntry error", listEntryResponse.getBody());
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_FAILED);
        }
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_SUCCESS);
    }

    @Override
    public ActionReturnUtil updateWhiteListsPolicy(String ruleId, WhiteListsDto whiteListsDto) throws Exception {
        AssertUtil.notNull(whiteListsDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(whiteListsDto.getRuleName(), DictEnum.NAME);
        String namespace = whiteListsDto.getNamespace();
        String serviceName = whiteListsDto.getServiceName();
        String ruleName = whiteListsDto.getRuleName();
        String userId = session.getAttribute("userId").toString();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        //判断该策略是否异常
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        if (ruleOverview.getDataStatus() != CommonConstant.DATA_IS_OK) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DATA_STATUS_ERROR);
        }
        //更新数据库中策略信息
        RuleDetail listCheckerRuleDetail = this.makeListCheckerRuleDetail(whiteListsDto, ruleId);
        ruleDetailMapper.updateByPrimaryKeySelective(listCheckerRuleDetail);
        //开启状态下更新k8s信息
        if (this.checkPolicyStatus(ruleId)) {
            K8SClientResponse response = whiteListsService.getListChecker(namespace, ruleName, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userId,listCheckerRuleDetail.getRuleDetailOrder());
                LOGGER.error("get listChecker error", response.getBody());
                return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
            }
            ListChecker listChecker = JsonUtil.jsonToPojo(response.getBody(), ListChecker.class);
            List<String> overrides = new ArrayList<String>();
            if (CollectionUtils.isNotEmpty(whiteListsDto.getWhiteNameList())) {
                for (WhiteServiceDto whiteService : whiteListsDto.getWhiteNameList()) {
                    overrides.add(whiteService.getNamespace() + "_" + whiteService.getName());
                }
            }
            listChecker.getSpec().setOverrides(overrides);
            K8SClientResponse updateResponse = whiteListsService.updateListChecker(namespace, ruleName, listChecker, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userId,listCheckerRuleDetail.getRuleDetailOrder());
                UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                LOGGER.error(status.getMessage());
                return ActionReturnUtil.returnSuccessWithData(status.getMessage());
            }

        }
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_UPDATE_SUCCESS);
    }

    @Override
    public ActionReturnUtil closeWhiteListsPolicy(String namespace, String ruleId, String ruleName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userId = session.getAttribute("userId").toString();
        Map<String, Object> res = whiteListsService.deleteWhiteListsPolicy(namespace, ruleName, cluster, 3);
        if (res.isEmpty()) {
            updateRuleOverview(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, CommonConstant.DATA_IS_OK, 0, userId);
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CLOSE_SUCCESS);
        } else {
            updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userId, Integer.valueOf((res.get("faileNum") == null?"0":res.get("faileNum")).toString()));
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_CLOSE_FAILED);
        }
    }

    @Override
    public ActionReturnUtil openWhiteListsPolicy(String namespace, String ruleId, String ruleName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userId = session.getAttribute("userId").toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (CollectionUtils.isEmpty(ruleDetails) || ruleDetails.size() < 3) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        ListChecker listChecker = new ListChecker();
        ListEntry   listEntry  = new ListEntry();
        Rule rule = new Rule();
        for (RuleDetail ruleDetail : ruleDetails) {
            switch (ruleDetail.getRuleDetailOrder()) {
                case 1:
                    listChecker = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), ListChecker.class);
                    break;
                case 2:
                    listEntry = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), ListEntry.class);
                    break;
                case 3:
                    rule = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), Rule.class);
                    break;
                default:
                    return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
            }
        }
        //创建listchecker
        K8SClientResponse listCheckerResponse = whiteListsService.createListChecker(namespace, listChecker, cluster);
        if (!HttpStatusUtil.isSuccessStatus(listCheckerResponse.getStatus())) {
            LOGGER.error("create listChecker error", listCheckerResponse.getBody());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        //创建listEntry
        K8SClientResponse listEntryResponse = whiteListsService.createListEntry(namespace, listEntry, cluster);
        if (!HttpStatusUtil.isSuccessStatus(listEntryResponse.getStatus())) {
            LOGGER.error("create listEntry policy error", listEntryResponse.getBody());
            Map<String, Object> res = whiteListsService.deleteWhiteListsPolicy(namespace, ruleName, cluster, 1);
            if (res.isEmpty()) {
                updateRuleOverview(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, CommonConstant.DATA_IS_OK, 0, userId);
            } else {
                updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userId, Integer.valueOf((res.get("faileNum") == null ? "0" : res.get("faileNum")).toString()));
            }
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }

        //创建rule
        K8SClientResponse ruleResponse = rateLimitService.createRule(namespace, rule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(ruleResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", ruleResponse.getBody());
            Map<String, Object> res = whiteListsService.deleteWhiteListsPolicy(namespace, ruleName, cluster, 2);
            if (res.isEmpty()) {
                updateRuleOverview(ruleId, CommonConstant.ISTIO_SWITCH_CLOSE, CommonConstant.DATA_IS_OK, 0, userId);
            } else {
                updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userId, Integer.valueOf((res.get("faileNum") == null ? "0" : res.get("faileNum")).toString()));
            }
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        updateRuleOverview(ruleId, CommonConstant.ISTIO_SWITCH_OPEN, CommonConstant.DATA_IS_OK, 0, userId);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_OPEN_SUCCESS);
    }

    @Override
    public ActionReturnUtil deleteWhiteListPolicy(String namespace, String ruleId, String ruleName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(ruleName, DictEnum.NAME);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userId = session.getAttribute("userId").toString();
        Map<String, Object> res = whiteListsService.deleteWhiteListsPolicy(namespace, ruleName, cluster, 3);
        if (res.isEmpty()) {
            ruleOverviewMapper.deleteByPrimaryKey(ruleId);
            ruleDetailMapper.deleteByPrimaryKey(ruleId);
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_DELETE_SUCCESS);
        } else {
            updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userId, Integer.valueOf((res.get("faileNum") == null?"0":res.get("faileNum")).toString()));
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_DELETE_FAILED);
        }
    }


    @Override
    public ActionReturnUtil getWhiteListPolicy(String namespace, String ruleId, String ruleName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userId = session.getAttribute("userId").toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (CollectionUtils.isEmpty(ruleDetails) || ruleDetails.size() < 3) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        ListChecker listCheckerDBDetail = new ListChecker();
        ListEntry listEntryDBDetail = new ListEntry();
        Rule ruleDBDetail = new Rule();
        for (RuleDetail ruleDetail : ruleDetails) {
            switch (ruleDetail.getRuleDetailOrder()) {
                case 1:
                    listCheckerDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), ListChecker.class);
                    break;
                case 2:
                    listEntryDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), ListEntry.class);
                    break;
                case 3:
                    ruleDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), Rule.class);
                    break;
                default:
                    return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
            }
        }
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        try {
            ListChecker listCheckerK8sDetail = new ListChecker();
            ListEntry listEntryK8sDetail = new ListEntry();
            int flag = 0;
            String  policyName = CommonConstant.WHITE_LISTS_PREFIX + ruleName;
            K8SClientResponse ruleResponse = whiteListsService.getRule(namespace, policyName, cluster);
            if (!HttpStatusUtil.isSuccessStatus(ruleResponse.getStatus())) {
                LOGGER.error("get  rule resource error", ruleResponse.getBody());
                flag = 3;
            }
            Rule ruleK8sDetail = JsonUtil.jsonToPojo(ruleResponse.getBody(), Rule.class);
            if (flag == 0) {
                K8SClientResponse listEntryResponse = whiteListsService.getListEntry(namespace, ruleName, cluster);
                if (!HttpStatusUtil.isSuccessStatus(listEntryResponse.getStatus())) {
                    LOGGER.error("get  listEntry resource error", listEntryResponse.getBody());
                    flag = 2;
                }
                listEntryK8sDetail  = JsonUtil.jsonToPojo(listEntryResponse.getBody(), ListEntry.class);
            }
            if (flag == 0) {
                K8SClientResponse listCheckerResponse = whiteListsService.getListChecker(namespace, ruleName, cluster);
                if (!HttpStatusUtil.isSuccessStatus(listCheckerResponse.getStatus())) {
                    LOGGER.error("get listchecker resource error", listCheckerResponse.getBody());
                    flag = 1;
                }
                listCheckerK8sDetail = JsonUtil.jsonToPojo(listCheckerResponse.getBody(), ListChecker.class);
            }
            if (flag != 0) {
                if (ruleOverview.getSwitchStatus() == CommonConstant.ISTIO_SWITCH_OPEN) {
                    updateRuleOverviewDataStatus(ruleId, flag == 3 ? CommonConstant.K8S_NO_DATA : CommonConstant.DATA_NOT_COMPLETE, userId, flag);
                    ruleOverview.setDataStatus(CommonConstant.DATA_NOT_COMPLETE);
                    ruleOverview.setDataErrLoc(flag);
                }
            } else {
                if (!listCheckerDBDetail.equals(listCheckerK8sDetail)) {
                    flag = 1;
                }
                if (!listEntryDBDetail.equals(listEntryK8sDetail)) {
                    flag = 2;
                }
                if (!ruleDBDetail.equals(ruleK8sDetail)) {
                    flag = 3;
                }
                if (flag != 0) {
                    updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userId, flag);
                    ruleOverview.setDataStatus(CommonConstant.DATA_NOT_SAME);
                    ruleOverview.setDataErrLoc(flag);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        WhiteListsDto whiteListsDto  = new WhiteListsDto();
        whiteListsDto.setRuleId(ruleId);
        whiteListsDto.setRuleName(ruleName);
        whiteListsDto.setRuleType(ruleOverview.getRuleType());
        whiteListsDto.setDataStatus(ruleOverview.getDataStatus().toString());
        whiteListsDto.setSwitchStatus(ruleOverview.getSwitchStatus().toString());
        whiteListsDto.setNamespace(ruleOverview.getRuleNs());
        whiteListsDto.setServiceName(ruleOverview.getRuleSvc());
        //构建服务列表
        List<String> overrides =  listCheckerDBDetail.getSpec().getOverrides();
        List<WhiteServiceDto> whiteMapList = new ArrayList<>();
        for(String override : overrides){
            WhiteServiceDto whiteService = new WhiteServiceDto();
            String[] nsList = override.split("_");
            if(nsList.length != 2){
                continue;
            }
            whiteService.setNamespace(nsList[0]);
            whiteService.setName(nsList[1]);
            whiteMapList.add(whiteService);
        }
        whiteListsDto.setWhiteNameList(whiteMapList);
        whiteListsDto.setCreateTime(ruleOverview.getCreateTime());
        return ActionReturnUtil.returnSuccessWithData(whiteListsDto);
    }

    @Override
    public ActionReturnUtil getClusterIstioPolicySwitch(String clusterId) throws Exception {
        //获取该集群的开关配置信息
        boolean globalSwitchStatus = false;
        IstioGlobalConfigure istioGlobalConfigure = istioGlobalConfigureMapper.getByClusterId(clusterId);
        if (Objects.nonNull(istioGlobalConfigure) && istioGlobalConfigure.getSwitchStatus() == CommonConstant.OPEN_GLOBAL_STATUS) {
            globalSwitchStatus = true;
        }
        Map<String, Object> status = new HashMap<>();
        status.put("globalSwitchStatus", globalSwitchStatus);
        return ActionReturnUtil.returnSuccessWithData(status);
    }

    @Override
    public ActionReturnUtil updateClusterIstioPolicySwitch(boolean status, String clusterId) throws Exception {
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //判断status是开启还是关闭
        if (status) {  //true为开启istio全局配置的操作
            //检测集群里是否安装istio服务
            K8SClientResponse response = ns.getNamespace(CommonConstant.ISTIO_NAMESPACE, null, null, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                LOGGER.error("get istio-system namespace error", response.getBody());
                throw new MarsRuntimeException(ErrorCodeMessage.ISTIO_NAMESPACE_GET_FAILED);
            }
            //查询istio全局开关配置表中是否有该集群的信息
            IstioGlobalConfigure istioGlobalConfigure = istioGlobalConfigureMapper.getByClusterId(clusterId);
            if (Objects.nonNull(istioGlobalConfigure)) {
                //将数据库的全局开关更新为开启
                this.updateGlobalStatus(clusterId, CommonConstant.OPEN_GLOBAL_STATUS);
            } else {
                //将该集群信息插入到mysql的全局配置表里
                this.insertGlobalInfo(cluster, CommonConstant.OPEN_GLOBAL_STATUS);
            }
        } else {//false为关闭istio全局配置的操作
            //获取集群下是否存在分区开启istio自动注入
            Map<String, Object> bodys = new HashMap<>();
            bodys.put("labelSelector", "istio-injection=enabled");
            bodys.put(CommonConstant.KIND, CommonConstant.NAMESPACE);
            K8SClientResponse response = ns.list(null, bodys, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                LOGGER.error("get namespaceList error", response.getBody());
                throw new MarsRuntimeException(response.getBody());
            }
            NamespaceList namespaceList = JsonUtil.jsonToPojo(response.getBody(), NamespaceList.class);
            if (CollectionUtils.isNotEmpty(namespaceList.getItems())) {
                return ActionReturnUtil.returnErrorWithData(CommonConstant.EXISTS_ISTIO_AUTOMATIC);
            }
            //将数据库的全局开关更新为关闭
            this.updateGlobalStatus(clusterId, CommonConstant.CLOSE_GLOBAL_STATUS);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil getNamespaceIstioPolicySwitch(String namespace, String clusterId) throws MarsRuntimeException {
        //获取集群信息
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //获取该集群下指定分区是否开启自动注入
        K8SClientResponse response = ns.getNamespace(namespace, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("get  namespace error", response.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_NOT_FOUND);
        }
        Namespace namespaceDetail = JsonUtil.jsonToPojo(response.getBody(), Namespace.class);
        String istioInjectionValue = "";
        Map<String, Object> labels = namespaceDetail.getMetadata().getLabels();
        if (Objects.nonNull(labels) && Objects.nonNull(labels.get(CommonConstant.ISTIO_INJECTION))) { //防止分区信息无label
            istioInjectionValue = labels.get(CommonConstant.ISTIO_INJECTION).toString();
        }
        boolean istioStatus = CommonConstant.OPEN_ISTIO_AUTOMATIC_INJECTION.equals(istioInjectionValue);
        Map<String, Object> namespaceIstioStatus = new HashMap<>();
        namespaceIstioStatus.put("namespaceIstioStatus", istioStatus);
        return ActionReturnUtil.returnSuccessWithData(namespaceIstioStatus);
    }

    @Override
    public ActionReturnUtil updateNamespaceIstioPolicySwitch(boolean status, String clusterId, String namespaceName) throws Exception {
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //查询该集群里该分区下的信息
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        K8SClientResponse response = ns.getNamespace(namespaceName, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("get namespace error", response.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_GET_FAILED);
        }
        Namespace namespace = JsonUtil.jsonToPojo(response.getBody(), Namespace.class);
        if (status && !getIstioGlobalStatus(clusterId)) {
            return ActionReturnUtil.returnErrorWithData(CommonConstant.ISTIO_GLOBAL_CLOSE);
        }
        boolean isDeployment = this.getNamespaceIstioStatus(namespaceName, cluster);//查询是否有服务 false代表无服务 true代表有服务
        if (isDeployment) {
            return ActionReturnUtil.returnErrorWithData(CommonConstant.EXISTS_SERVICE);
        }
        Map<String, Object> labels = namespace.getMetadata().getLabels();
        if (status) {//判断前端传过来的是开启操作还是关闭操作
            labels.put("istio-injection", "enabled");
        } else {
            labels.put("istio-injection", "disabled");
        }
        namespace.getMetadata().setLabels(labels);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(namespace);
        K8SClientResponse updateResponse = ns.update(headers, bodys, namespaceName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
            LOGGER.error("update istio status failed", updateResponse.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.ISTIO_STATUS_UPDATE_FAILED);
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 查询该分区下是否有服务
     */
    private boolean getNamespaceIstioStatus(String namespaceName, Cluster cluster) throws Exception {
        //定义一个是否有服务标志
        boolean isDeployment = false;
        // 1.1根据namespace查询deployments列表
        K8SClientResponse deploymentResponse = deploymentService.doDeploymentsByNamespace(namespaceName, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(deploymentResponse.getStatus())) {
            LOGGER.error("calling the k8s interface to query the deployment list under namespace failed", deploymentResponse.getBody());
            throw new MarsRuntimeException(deploymentResponse.getBody());
        }
        DeploymentList deploymentList = JsonUtil.jsonToPojo(deploymentResponse.getBody(), DeploymentList.class);
        if (CollectionUtils.isNotEmpty(deploymentList.getItems())) {
            isDeployment = true;
        }
        return isDeployment;
    }

    /**
     * 判断数据库中是否存在该策略
     *
     * @param clusterId
     * @param namespace
     * @param svcName
     * @param ruleName
     * @return
     */
    private boolean checkPolicyExist(String clusterId, String namespace, String svcName, String ruleName, String ruleType){
        Map<Object, Object> ruleInfoMap = new HashMap<>();
        ruleInfoMap.put("ruleClusterId", clusterId);
        ruleInfoMap.put("ruleNs", namespace);
        ruleInfoMap.put("ruleSvc", svcName);
        ruleInfoMap.put("ruleName", ruleName);
        ruleInfoMap.put("ruleType", ruleType);
        List<RuleOverview> ruleOverviews = ruleOverviewMapper.selectByRuleInfo(ruleInfoMap);
        return CollectionUtils.isNotEmpty(ruleOverviews);
    }

    private void insertRuleOverview(BaseIstioPolicyDto policyDto, String ruleId, String clusterId, String userId) {
        RuleOverview ruleOverview = new RuleOverview();
        ruleOverview.setRuleId(ruleId);
        ruleOverview.setRuleName(policyDto.getRuleName());
        ruleOverview.setRuleClusterId(clusterId);
        ruleOverview.setRuleNs(policyDto.getNamespace());
        ruleOverview.setRuleSvc(policyDto.getServiceName());
        ruleOverview.setRuleType(policyDto.getRuleType());
        if (policyDto.getRuleType().equals(CommonConstant.CIRCUIT_BREAKER)) {
            ruleOverview.setRuleSourceNum(1);
        } else if (policyDto.getRuleType().equals(CommonConstant.RATE_LIMIT)) {
            ruleOverview.setRuleSourceNum(5);
        } else if (policyDto.getRuleType().equals(CommonConstant.WHITE_LISTS)) {
            ruleOverview.setRuleSourceNum(3);
        }
        ruleOverview.setRuleScope(policyDto.getScope());//0表示全局，需要考虑
        ruleOverview.setUserId(userId);
        ruleOverviewMapper.insert(ruleOverview);
    }

    private RuleDetail insertRuleDetail(BaseIstioPolicyDto policyDto, String ruleId) {
        RuleDetail ruleDetail = null;
        if (policyDto.getRuleType().equals(CommonConstant.CIRCUIT_BREAKER)) {
            CircuitBreakDto circuitBreakDto = (CircuitBreakDto) policyDto;
            ruleDetail = makeCircuitBreakerRuleDetail(circuitBreakDto, ruleId);
        }
        ruleDetailMapper.insert(ruleDetail);
        return ruleDetail;
    }

    private ObjectMeta makeObjectMeta(BaseIstioPolicyDto policyDto){
        ObjectMeta meta = new ObjectMeta();
        meta.setName(policyDto.getRuleName());
        meta.setNamespace(policyDto.getNamespace());
        Map<String, Object> label = new HashMap<>();
        label.put(CommonConstant.LABEL_KEY_APP, policyDto.getServiceName());
        label.put(CommonConstant.ISTIO_RULE_TYPE, policyDto.getRuleType());
        meta.setLabels(label);
        return meta;
    }

    //Quota
    private RuleDetail makeRateLimitQuotaRuleDetail(RateLimitDto rateLimitDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = makeObjectMeta(rateLimitDto);
        QuotaInstance quotaInstance = new QuotaInstance();
        quotaInstance.setApiVersion(CommonConstant.CONFIG_ISTIO_V1ALPHA2);
        quotaInstance.setKind(CommonConstant.RATE_LIMIT_QUOTA);
        quotaInstance.setMetadata(meta);
        QuotaInstanceSpec quotaInstanceSpec = new QuotaInstanceSpec();
        Map<String, String> dimension = new HashMap<>();
        dimension.put("sourceName", "source.labels[\"app\"] | \"unknown\"");
        dimension.put("sourceNamespace", "source.namespace | \"unknown\"");
        quotaInstanceSpec.setDimensions(dimension);
        quotaInstance.setSpec(quotaInstanceSpec);
        String quotaInstanceStr = JsonUtil.objectToJson(quotaInstance);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(1);
        ruleDetail.setRuleDetailContent(quotaInstanceStr.getBytes());
        return ruleDetail;
    }

    //RedisQuota
    private RuleDetail makeRateLimitRedisQuotaRuleDetail(RateLimitDto rateLimitDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = makeObjectMeta(rateLimitDto);
        RedisQuota redisQuota = new RedisQuota();
        redisQuota.setApiVersion(CommonConstant.CONFIG_ISTIO_V1ALPHA2);
        redisQuota.setKind(CommonConstant.RATE_LIMIT_REDIS_QUOTA);
        RedisQuotaSpecQuota redisQuotaSpecQuota = new RedisQuotaSpecQuota();
        redisQuotaSpecQuota.setName(rateLimitDto.getRuleName() + ".quota." + rateLimitDto.getNamespace());
        List<QuotaOverride> quotaOverrides = new ArrayList<>();
        if (!"0".equals(rateLimitDto.getScope())) {
            List<RateLimitOverrideDto> overrides = rateLimitDto.getOverrides();
            overrides.forEach(override -> {
                QuotaOverride quotaOverride = new QuotaOverride();
                Map<String, String> dimensions = new HashMap<>();
                dimensions.put("sourceNamespce", override.getScopeNamespace());
                if ("2".equals(rateLimitDto.getScope())) {
                    dimensions.put("sourceName", override.getScopeServiceName());
                }
                quotaOverride.setDimensions(dimensions);
                quotaOverride.setMaxAmount(Integer.valueOf(rateLimitDto.getMaxAmount()));
                quotaOverrides.add(quotaOverride);
            });
            redisQuotaSpecQuota.setOverrides(quotaOverrides);
        } else {
            redisQuotaSpecQuota.setMaxAmount(Integer.valueOf(rateLimitDto.getMaxAmount()));
        }
        redisQuotaSpecQuota.setValidDuration(rateLimitDto.getValidDuration() + CommonConstant.SECOND);
        redisQuotaSpecQuota.setRateLimitAlgorithm(rateLimitDto.getAlgorithm());
        if ("ROLLING_WINDOW".equalsIgnoreCase(rateLimitDto.getAlgorithm()) && StringUtils.isNotEmpty(rateLimitDto.getBucketDuration())) {
            redisQuotaSpecQuota.setBucketDuration(rateLimitDto.getBucketDuration() + CommonConstant.SECOND);
        }
        RedisQuotaSpec redisQuotaSpec = new RedisQuotaSpec();
        redisQuotaSpec.setConnectionPoolSize(CommonConstant.PERCENT_HUNDRED);
        redisQuotaSpec.setQuotas(Collections.singletonList(redisQuotaSpecQuota));
        redisQuotaSpec.setRedisServerUrl(jedisConnectionFactory.getHostName() + CommonConstant.COLON + jedisConnectionFactory.getPort());
//        redisQuotaSpec.setRedisServerUrl("10.10.124.199:30380");
        redisQuota.setSpec(redisQuotaSpec);
        redisQuota.setMetadata(meta);
        String redisQuotaStr = JsonUtil.objectToJson(redisQuota);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(2);
        ruleDetail.setRuleDetailContent(redisQuotaStr.getBytes());
        return ruleDetail;
    }

    //QuotaSpec
    private RuleDetail makeRateLimitQuotaSpecRuleDetail(RateLimitDto rateLimitDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = makeObjectMeta(rateLimitDto);
        QuotaSpecRuleQuota ruleQuota = new QuotaSpecRuleQuota();
        ruleQuota.setCharge(CommonConstant.ONENUMSTRING);
        ruleQuota.setQuota(rateLimitDto.getRuleName());
        List<QuotaSpecRuleQuota> quotas = Collections.singletonList(ruleQuota);
        QuotaSpecRule quotaSpecRule = new QuotaSpecRule();
        quotaSpecRule.setQuotas(quotas);
        QuotaSpecSpec quotaSpecSpec = new QuotaSpecSpec();
        quotaSpecSpec.setRules(Collections.singletonList(quotaSpecRule));
        QuotaSpec quotaSpec = new QuotaSpec();
        quotaSpec.setApiVersion(CommonConstant.CONFIG_ISTIO_V1ALPHA2);
        quotaSpec.setKind(CommonConstant.RATE_LIMIT_QUOTA_SPEC);
        quotaSpec.setSpec(quotaSpecSpec);
        meta.setAnnotations(null);
        quotaSpec.setMetadata(meta);
        String quotaSpecStr = JsonUtil.objectToJson(quotaSpec);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(3);
        ruleDetail.setRuleDetailContent(quotaSpecStr.getBytes());
        return ruleDetail;
    }

    //QuotaSpecBinding
    private RuleDetail makeRateLimitQuotaSpecBindingRuleDetail(RateLimitDto rateLimitDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = makeObjectMeta(rateLimitDto);
        QuotaSpecBindingSpec specBindingSpec = new QuotaSpecBindingSpec();
        Map<String, String> quotaSpecMap = new HashMap<>();
        quotaSpecMap.put("name", rateLimitDto.getRuleName());
        quotaSpecMap.put("namespace", rateLimitDto.getNamespace());
        specBindingSpec.setQuotaSpecs(Collections.singletonList(quotaSpecMap));
        Map<String, String> serviceMap = new HashMap<>();
        serviceMap.put("namespace", rateLimitDto.getNamespace());
        serviceMap.put("name", rateLimitDto.getServiceName());
        specBindingSpec.setServices(Collections.singletonList(serviceMap));
        QuotaSpecBinding quotaSpecBinding = new QuotaSpecBinding();
        quotaSpecBinding.setApiVersion(CommonConstant.CONFIG_ISTIO_V1ALPHA2);
        quotaSpecBinding.setKind(CommonConstant.RATE_LIMIT_QUOTA_SPEC_BINDING);
        quotaSpecBinding.setSpec(specBindingSpec);
        quotaSpecBinding.setMetadata(meta);
        String quotaSpecBindingStr = JsonUtil.objectToJson(quotaSpecBinding);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(4);
        ruleDetail.setRuleDetailContent(quotaSpecBindingStr.getBytes());
        return ruleDetail;
    }

    //Rule
    private RuleDetail makeRateLimitRuleRuleDetail(RateLimitDto rateLimitDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = makeObjectMeta(rateLimitDto);
        //为了区别白名单中的
        meta.setName(CommonConstant.RATE_LIMIT_PREFIX + meta.getName());
        Rule rule = new Rule();
        rule.setApiVersion(CommonConstant.CONFIG_ISTIO_V1ALPHA2);
        rule.setKind(CommonConstant.ISTIO_RULE);
        rule.setSpec(this.convertRuleSpec(rateLimitDto.getRuleName(), rateLimitDto, rateLimitDto.getNamespace()));
        rule.setMetadata(meta);
        String ruleStr = JsonUtil.objectToJson(rule);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(5);
        ruleDetail.setRuleDetailContent(ruleStr.getBytes());
        return ruleDetail;
    }

    /**
     * 组装RuleDetail
     * @param circuitBreakDto
     * @param ruleId
     * @return
     */
    private RuleDetail makeCircuitBreakerRuleDetail(CircuitBreakDto circuitBreakDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        TrafficPolicy trafficPolicyData = new TrafficPolicy();
        ConnectionPool connectionPool = new ConnectionPool();
        if (Objects.nonNull(circuitBreakDto.getMaxConnections())) {
            TcpConnection tcpConnection = new TcpConnection();
            tcpConnection.setMaxConnections(circuitBreakDto.getMaxConnections());
            connectionPool.setTcp(tcpConnection);
        }
        if (Objects.nonNull(circuitBreakDto.getHttp1MaxPendingRequests()) ||
                Objects.nonNull(circuitBreakDto.getHttp2MaxRequests()) ||
                Objects.nonNull(circuitBreakDto.getMaxRequestsPerConnection())) {
            HttpConnection httpConnection = new HttpConnection();
            httpConnection.setHttp1MaxPendingRequests(circuitBreakDto.getHttp1MaxPendingRequests());
            httpConnection.setHttp2MaxRequests(circuitBreakDto.getHttp2MaxRequests());
            httpConnection.setMaxRequestsPerConnection(circuitBreakDto.getMaxRequestsPerConnection());
            connectionPool.setHttp(httpConnection);
        }
        trafficPolicyData.setConnectionPool(connectionPool);

        if (Objects.nonNull(circuitBreakDto.getConsecutiveErrors()) &&
                Objects.nonNull(circuitBreakDto.getInterval()) &&
                Objects.nonNull(circuitBreakDto.getBaseEjectionTime())) {
            OutlierDetection outlierDetection = new OutlierDetection();
            outlierDetection.setConsecutiveErrors(circuitBreakDto.getConsecutiveErrors());
            outlierDetection.setInterval(circuitBreakDto.getInterval() + CommonConstant.SECOND);
            outlierDetection.setBaseEjectionTime(circuitBreakDto.getBaseEjectionTime() + CommonConstant.SECOND);
            trafficPolicyData.setOutlierDetection(outlierDetection);
        }
        String trafficPolicyStr = JsonUtil.objectToJson(trafficPolicyData);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(1);
        ruleDetail.setRuleDetailContent(trafficPolicyStr.getBytes());
        return ruleDetail;
    }

    private void updateRuleOverviewDataStatus(String ruleId, int dataStatus, String userId, int err) {
        RuleOverview ruleOverview = new RuleOverview();
        ruleOverview.setRuleId(ruleId);
        ruleOverview.setDataStatus(dataStatus);
        ruleOverview.setUserId(userId);
        ruleOverview.setDataErrLoc(err);
        ruleOverviewMapper.updateDataStatus(ruleOverview);
    }

    private void updateRuleOverview(String ruleId, int istioSwitchStatus, int dataStatus, int err, String userId){
        RuleOverview ruleOverview = new RuleOverview();
        ruleOverview.setRuleId(ruleId);
        ruleOverview.setSwitchStatus(istioSwitchStatus);
        ruleOverview.setDataStatus(dataStatus);
        ruleOverview.setUserId(userId);
        ruleOverview.setDataErrLoc(err);
        ruleOverviewMapper.updateByPrimaryKeySelective(ruleOverview);
    }

    /**
     * @param ruleId
     * @param istioSwitchStatus
     * @param userId
     */
    private void updateRuleOverviewSwitchStatus(String ruleId, int istioSwitchStatus, String userId) {
        RuleOverview ruleOverview = new RuleOverview();
        ruleOverview.setRuleId(ruleId);
        ruleOverview.setSwitchStatus(istioSwitchStatus);
        ruleOverview.setUserId(userId);
        ruleOverviewMapper.updateSwitchStatus(ruleOverview);
    }

    /**
     * 校验策略开关状态以及策略异常状态
     * @param ruleId
     * @return
     */
    private boolean checkPolicyStatus(String ruleId) {
        Map<String, Object> ruleStatus = ruleOverviewMapper.selectRuleStatus(ruleId);
        if (Objects.isNull(ruleStatus)) {
            throw new MarsRuntimeException(ErrorCodeMessage.POLICY_NOT_EXIST);
        }
        int dataStatus = Integer.valueOf(ruleStatus.get("dataStatus").toString());
        if (dataStatus != 0) {
            throw new MarsRuntimeException(ErrorCodeMessage.DATA_STATUS_ERROR);
        }
        int switchStatus = Integer.valueOf(ruleStatus.get("switchStatus").toString());
        if (switchStatus != 0 && switchStatus != 1) {
            throw new MarsRuntimeException(ErrorCodeMessage.DB_DATA_ERROR);
        }
        return switchStatus == CommonConstant.ISTIO_SWITCH_OPEN;
    }

    /**
     * 更新全局状态
     */
    private void updateGlobalStatus(String clusterId, int switchStatus) throws ParseException {
        //获取用户信息
        String userId = session.getAttribute("userId").toString();
        Date date = new Date();// 获得系统时间.
        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        String nowTime = sdf.format(date);
        Date time = sdf.parse(nowTime);
        IstioGlobalConfigure istioGlobalConfigure = new IstioGlobalConfigure();
        istioGlobalConfigure.setClusterId(clusterId);
        istioGlobalConfigure.setSwitchStatus(switchStatus);
        istioGlobalConfigure.setUpdateTime(time);
        if (Objects.nonNull(userId)) {
            istioGlobalConfigure.setOperatorId(Long.parseLong(userId));
        }
        int istioGlobalNumber = istioGlobalConfigureMapper.updateByClusterId(istioGlobalConfigure);
        if (istioGlobalNumber != 1) {
            throw new MarsRuntimeException(ErrorCodeMessage.UPDATE_FAIL);
        }
    }

    /**
     * 插入集群配置信息
     */
    private void insertGlobalInfo(Cluster cluser, int switchStatus) throws ParseException {
        //获取用户信息
        String userId = session.getAttribute("userId").toString();
        Date date = new Date();// 获得系统时间.
        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        String nowTime = sdf.format(date);
        Date time = sdf.parse(nowTime);
        IstioGlobalConfigure istioGlobalConfigure = new IstioGlobalConfigure();
        istioGlobalConfigure.setClusterId(cluser.getId());
        istioGlobalConfigure.setClusterName(cluser.getAliasName());
        istioGlobalConfigure.setSwitchStatus(switchStatus);
        if (Objects.nonNull(userId)) {
            istioGlobalConfigure.setOperatorId(Long.parseLong(userId));
        }
        istioGlobalConfigure.setUpdateTime(time);
        int istioGlobalNumber = istioGlobalConfigureMapper.insert(istioGlobalConfigure);
        if (istioGlobalNumber != 1) {
            throw new MarsRuntimeException(ErrorCodeMessage.QUERY_FAIL);
        }
    }

    /**
     * 获取全局状态开关状态
     */
    public boolean getIstioGlobalStatus(String clusterId) throws Exception {
        //获取该集群的开关配置信息
        IstioGlobalConfigure istioGlobalConfigure = istioGlobalConfigureMapper.getByClusterId(clusterId);
        if (Objects.nonNull(istioGlobalConfigure) && istioGlobalConfigure.getSwitchStatus() == CommonConstant.OPEN_GLOBAL_STATUS) {
            return true;
        }
        return false;
    }

    /**
     * 组装白名单策略里的listChecker
     *
     * @param whiteListsDto
     * @param ruleId
     * @return
     */
    private RuleDetail makeListCheckerRuleDetail(WhiteListsDto whiteListsDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = this.makeObjectMeta(whiteListsDto);
        //build listChecker
        ListChecker listChecker = new ListChecker();
        listChecker.setApiVersion(CommonConstant.CONFIG_ISTIO_V1ALPHA2);
        listChecker.setKind(CommonConstant.WHITE_LISTS_LIST_CHECKER);
        ListCheckerSpec listCheckerSpec = new ListCheckerSpec();
        listChecker.setMetadata(meta);
        List<String> overrides = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(whiteListsDto.getWhiteNameList())) {
            for (WhiteServiceDto whiteService : whiteListsDto.getWhiteNameList()) {
                overrides.add(whiteService.getNamespace() + "_" + whiteService.getName());
            }
        }
        listCheckerSpec.setOverrides(overrides);
        listCheckerSpec.setBlacklist(false);
        listChecker.setSpec(listCheckerSpec);
        String listCheckerStr = JsonUtil.objectToJson(listChecker);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(1);
        ruleDetail.setRuleDetailContent(listCheckerStr.getBytes());
        return ruleDetail;
    }

    /**
     * 组装白名单策略里的listEntry
     *
     * @param whiteListsDto
     * @param ruleId
     * @return
     */
    private RuleDetail makeListEntryDetail(WhiteListsDto whiteListsDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = this.makeObjectMeta(whiteListsDto);
        ListEntry listEntry = new ListEntry();
        listEntry.setApiVersion(CommonConstant.CONFIG_ISTIO_V1ALPHA2);
        listEntry.setKind(CommonConstant.WHITE_LISTS_LIST_ENTRY);
        ListEntrySpec listEntrySpec = new ListEntrySpec();
        listEntry.setMetadata(meta);
        listEntrySpec.setValue("source.namespace + \"_\" + source.labels[\"app\"]");
        listEntry.setSpec(listEntrySpec);
        String listEntrySrc = JsonUtil.objectToJson(listEntry);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(2);
        ruleDetail.setRuleDetailContent(listEntrySrc.getBytes());
        return ruleDetail;
    }

    /**
     * 组装白名单策略里的rule
     *
     * @param whiteListsDto
     * @param ruleId
     * @return
     */
    private RuleDetail makeRuleRuleDetail(WhiteListsDto whiteListsDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = this.makeObjectMeta(whiteListsDto);
        meta.setName(CommonConstant.WHITE_LISTS_PREFIX + whiteListsDto.getRuleName());
        Rule rule = new Rule();
        rule.setApiVersion(CommonConstant.CONFIG_ISTIO_V1ALPHA2);
        rule.setKind(CommonConstant.ISTIO_RULE);
        rule.setMetadata(meta);
        RuleSpec ruleSpec = new RuleSpec();
        Action action = new Action();
        List<Action> actions = new ArrayList<>();
        if (Objects.nonNull(whiteListsDto.getRuleName()) && Objects.nonNull(whiteListsDto.getNamespace())) {
            action.setHandler(whiteListsDto.getRuleName() + ".listchecker");
        }
        List<String> instances = new ArrayList<>();
        if (Objects.nonNull(whiteListsDto.getRuleName()) && Objects.nonNull(whiteListsDto.getNamespace())) {
            instances.add(whiteListsDto.getRuleName() + ".listentry");
        }
        action.setInstances(instances);
        actions.add(action);
        ruleSpec.setActions(actions);
        ruleSpec.setMatch("destination.labels[\"app\"] == \"" + whiteListsDto.getServiceName() + "\"");
        rule.setSpec(ruleSpec);
        String ruleSrc = JsonUtil.objectToJson(rule);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(3);
        ruleDetail.setRuleDetailContent(ruleSrc.getBytes());
        return ruleDetail;
    }

}
