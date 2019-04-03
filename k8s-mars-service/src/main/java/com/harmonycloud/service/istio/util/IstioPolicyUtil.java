package com.harmonycloud.service.istio.util;

import com.google.common.collect.ImmutableMap;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.istio.IstioGlobalConfigureMapper;
import com.harmonycloud.dao.istio.RuleDetailMapper;
import com.harmonycloud.dao.istio.RuleOverviewMapper;
import com.harmonycloud.dao.istio.bean.IstioGlobalConfigure;
import com.harmonycloud.dao.istio.bean.RuleDetail;
import com.harmonycloud.dao.istio.bean.RuleOverview;
import com.harmonycloud.dto.application.istio.*;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.policies.*;
import com.harmonycloud.k8s.bean.istio.policies.ratelimit.*;
import com.harmonycloud.k8s.bean.istio.policies.whitelists.ListChecker;
import com.harmonycloud.k8s.bean.istio.policies.whitelists.ListCheckerSpec;
import com.harmonycloud.k8s.bean.istio.policies.whitelists.ListEntry;
import com.harmonycloud.k8s.bean.istio.policies.whitelists.ListEntrySpec;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.*;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.istio.DestinationRuleService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.harmonycloud.service.platform.constant.Constant.LABEL_PROJECT_ID;

public class IstioPolicyUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(IstioPolicyUtil.class);

    public static final String DEFAULT_OVERRIDE = "default_unknown";

    public static final Map<String, Integer> typeMap = ImmutableMap.of(
            CommonConstant.TIMEOUT_RETRY, 4,
            CommonConstant.FAULT_INJECTION, 2,
            CommonConstant.TRAFFIC_SHIFTING, 1
    );

    /**
     * 判断数据库中是否存在该策略
     */
    public static boolean checkPolicyExist(String clusterId, String namespace, String svcName, String ruleName, String ruleType, RuleOverviewMapper ruleOverviewMapper) {
        Map<Object, Object> ruleInfoMap = new HashMap<>();
        ruleInfoMap.put("ruleClusterId", clusterId);
        ruleInfoMap.put("ruleNs", namespace);
        ruleInfoMap.put("ruleSvc", svcName);
//        ruleInfoMap.put("ruleName", ruleName);
        ruleInfoMap.put("ruleType", ruleType);
        List<RuleOverview> ruleOverviews = ruleOverviewMapper.selectByRuleInfo(ruleInfoMap);
        return CollectionUtils.isNotEmpty(ruleOverviews);
    }

    /**
     * 插入RuleOverview信息
     */
    public static void insertRuleOverview(BaseIstioPolicyDto policyDto, String ruleId, String clusterId, String userName, RuleOverviewMapper ruleOverviewMapper) {
        RuleOverview ruleOverview = new RuleOverview();
        ruleOverview.setRuleId(ruleId);
        ruleOverview.setRuleName(policyDto.getRuleName());
        ruleOverview.setRuleClusterId(clusterId);
        ruleOverview.setRuleNs(policyDto.getNamespace());
        ruleOverview.setRuleSvc(policyDto.getServiceName());
        ruleOverview.setRuleType(policyDto.getRuleType());
        if (policyDto.getRuleType().equals(CommonConstant.CIRCUIT_BREAKER)) {
            ruleOverview.setRuleSourceNum(CommonConstant.CIRCUIT_BREAKER_RESOURCE_COUNT);
        } else if (policyDto.getRuleType().equals(CommonConstant.RATE_LIMIT)) {
            ruleOverview.setRuleSourceNum(CommonConstant.RATE_LIMIT_RESOURCE_COUNT);
        } else if (policyDto.getRuleType().equals(CommonConstant.WHITE_LISTS)) {
            ruleOverview.setRuleSourceNum(CommonConstant.WHITE_LISTS_RESOURCE_COUNT);
        } else if (policyDto.getRuleType().equals(CommonConstant.TRAFFIC_SHIFTING)) {
            ruleOverview.setRuleSourceNum(CommonConstant.TRAFFIC_SHIFTING_RESOURCE_COUNT);
        } else if (policyDto.getRuleType().equals(CommonConstant.FAULT_INJECTION)) {
            ruleOverview.setRuleSourceNum(CommonConstant.FAULT_INJECTION_RESOURCE_COUNT);
        } else if (policyDto.getRuleType().equals(CommonConstant.TIMEOUT_RETRY)) {
            ruleOverview.setRuleSourceNum(CommonConstant.TIMEOUT_RETRY_RESOURCE_COUNT);
        }
        ruleOverview.setSwitchStatus(CommonConstant.ISTIO_POLICY_OPEN);
        ruleOverview.setRuleScope(policyDto.getScope());//0表示全局，需要考虑
        ruleOverview.setUserName(userName);
        ruleOverview.setCreateTime(new Date());
        ruleOverviewMapper.insert(ruleOverview);
    }

    /**
     * 插入RuleDetail信息
     */
    public static RuleDetail insertRuleDetail(BaseIstioPolicyDto policyDto, String ruleId, RuleDetailMapper ruleDetailMapper) {
        RuleDetail ruleDetail = null;
        if (policyDto.getRuleType().equals(CommonConstant.CIRCUIT_BREAKER)) {
            CircuitBreakDto circuitBreakDto = (CircuitBreakDto) policyDto;
            ruleDetail = makeCircuitBreakerRuleDetail(circuitBreakDto, ruleId);
        } else if (policyDto.getRuleType().equals(CommonConstant.TRAFFIC_SHIFTING)) {
            TrafficShiftingDto trafficShiftingDto = (TrafficShiftingDto) policyDto;
            ruleDetail = makeTrafficShiftingRuleDetail(trafficShiftingDto, ruleId);
        } else if (policyDto.getRuleType().equals(CommonConstant.FAULT_INJECTION)) {
            FaultInjectionDto faultInjectionDto = (FaultInjectionDto) policyDto;
            ruleDetail = makeFaultInjectionRuleDetail(faultInjectionDto, ruleId);
        }
        ruleDetail.setCreateTime(new Date());
        ruleDetailMapper.insert(ruleDetail);
        return ruleDetail;
    }

    /**
     * 组装CircuitBreaker RuleDetail
     */
    public static RuleDetail makeCircuitBreakerRuleDetail(CircuitBreakDto circuitBreakDto, String ruleId) {
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
            outlierDetection.setMaxEjectionPercent(circuitBreakDto.getMaxEjectionPercent());
            trafficPolicyData.setOutlierDetection(outlierDetection);
        }
        String trafficPolicyStr = JsonUtil.objectToJson(trafficPolicyData);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(1);
        ruleDetail.setRuleDetailContent(trafficPolicyStr.getBytes());
        return ruleDetail;
    }

    /**
     * 组装TrafficShifting RuleDetail
     */
    //hosts: name
    public static RuleDetail makeTrafficShiftingRuleDetail(TrafficShiftingDto trafficShiftingDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        List<TrafficShiftingDesServiceDto> desServiceList = trafficShiftingDto.getDesServices();
        List<TrafficShiftingMatchDto> matchList = trafficShiftingDto.getMatches();
        List<HTTPRoute> http = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(desServiceList)) {
            HTTPRoute httpRoute = new HTTPRoute();
            List<DestinationWeight> route = new ArrayList<>();
            desServiceList.forEach(desService -> {
                DestinationWeight destinationWeight = new DestinationWeight();
                Destination destination = new Destination();
                destination.setHost(trafficShiftingDto.getServiceName());
                destination.setSubset(desService.getSubset());
                destinationWeight.setDestination(destination);
                if (Objects.nonNull(desService.getWeight())) {
                    destinationWeight.setWeight(Integer.valueOf(desService.getWeight()));
                }
                route.add(destinationWeight);
            });
            httpRoute.setRoute(route);
            http.add(httpRoute);
        } else {
            HTTPRoute httpRoute = new HTTPRoute();
            List<DestinationWeight> route = new ArrayList<>();
            DestinationWeight destinationWeight = new DestinationWeight();
            Destination destination = new Destination();
            destination.setHost(trafficShiftingDto.getServiceName());
            destinationWeight.setDestination(destination);
            route.add(destinationWeight);
            httpRoute.setRoute(route);
            http.add(httpRoute);
        }
        if (CollectionUtils.isNotEmpty(matchList)) {
            matchList.forEach(matchDto -> {
                HTTPRoute httpRoute = new HTTPRoute();
                DestinationWeight destinationWeight = new DestinationWeight();
                Destination destination = new Destination();
                destination.setHost(trafficShiftingDto.getServiceName());
                destination.setSubset(matchDto.getSubset());
                destinationWeight.setDestination(destination);

                HTTPMatchRequest matchRequest = new HTTPMatchRequest();
                if (CollectionUtils.isNotEmpty(matchDto.getHeaders())) {
                    Map<String, StringMatch> headers = new HashMap<>();
                    matchDto.getHeaders().forEach(header -> {
                        String[] headerKeyV = header.split("=");
                        StringMatch stringMatch = new StringMatch();
                        stringMatch.setExact(headerKeyV[1]);
                        headers.put(headerKeyV[0], stringMatch);
                    });
                    matchRequest.setHeaders(headers);
                }
                Map<String, String> sourceLabels = new HashMap<>();
                if (StringUtils.isNotBlank(matchDto.getSourceName())) {
                    sourceLabels.put("app", matchDto.getSourceName());
                }
                if (StringUtils.isNotBlank(matchDto.getSourceVersion())) {
                    sourceLabels.put("version", matchDto.getSourceVersion());
                }
                if (!sourceLabels.isEmpty()) {
                    matchRequest.setSourceLabels(sourceLabels);
                }
                httpRoute.setRoute(Collections.singletonList(destinationWeight));
                httpRoute.setMatch(Collections.singletonList(matchRequest));
                http.add(httpRoute);
            });
        }
        //default 必须放到包含match的元素后面
        Collections.sort(http, (httpRouteNext, httpRouteCur) -> {
            if (Objects.isNull(httpRouteCur.getMatch())) {
                return -1;
            }
            return 0;
        });
        String httpRouteStr = JsonUtil.convertToJsonNonNull(http);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(1);
        ruleDetail.setRuleDetailContent(httpRouteStr.getBytes());
        return ruleDetail;
    }

    /**
     * 组装FaultInjection RuleDetail
     */
    public static RuleDetail makeFaultInjectionRuleDetail(FaultInjectionDto faultInjectionDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        HTTPFaultInjection httpFaultInjection = new HTTPFaultInjection();
        if (StringUtils.isNotBlank(faultInjectionDto.getFixedDelay())) {
            Delay delay = new Delay();
            delay.setFixedDelay(faultInjectionDto.getFixedDelay().trim() + CommonConstant.SECOND);
            delay.setPercent(Integer.valueOf(faultInjectionDto.getDelayPercent().trim()));
            httpFaultInjection.setDelay(delay);
        }
        if (StringUtils.isNotBlank(faultInjectionDto.getHttpStatus())) {
            Abort abort = new Abort();
            abort.setHttpStatus(Integer.valueOf(faultInjectionDto.getHttpStatus().trim()));
            abort.setPercent(Integer.valueOf(faultInjectionDto.getCodePercent().trim()));
            httpFaultInjection.setAbort(abort);
        }
        String httpFaultInjectionStr = JsonUtil.convertToJsonNonNull(httpFaultInjection);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(1);
        ruleDetail.setRuleDetailContent(httpFaultInjectionStr.getBytes());
        return ruleDetail;
    }

    /**
     * 更新策略开关状态、策略状态
     */
    public static void updateRuleOverview(String ruleId, int istioSwitchStatus, int dataStatus, int err, String userName, RuleOverviewMapper ruleOverviewMapper) {
        RuleOverview ruleOverview = new RuleOverview();
        ruleOverview.setRuleId(ruleId);
        ruleOverview.setSwitchStatus(istioSwitchStatus);
        ruleOverview.setDataStatus(dataStatus);
        ruleOverview.setUserName(userName);
        ruleOverview.setDataErrLoc(err);
        ruleOverview.setUpdateTime(new Date());
        ruleOverviewMapper.updateByPrimaryKeySelective(ruleOverview);
    }

    /**
     * 更新策略开关状态
     */
    public static void updateRuleOverviewSwitchStatus(String ruleId, int istioSwitchStatus, String userName, RuleOverviewMapper ruleOverviewMapper) {
        RuleOverview ruleOverview = new RuleOverview();
        ruleOverview.setRuleId(ruleId);
        ruleOverview.setSwitchStatus(istioSwitchStatus);
        ruleOverview.setUserName(userName);
        ruleOverview.setUpdateTime(new Date());
        ruleOverviewMapper.updateSwitchStatus(ruleOverview);
    }

    /**
     * 校验策略开关状态以及策略异常状态
     */
    public static boolean checkPolicyStatus(String ruleId, RuleOverviewMapper ruleOverviewMapper) {
        Map<String, Object> ruleStatus = ruleOverviewMapper.selectRuleStatus(ruleId);
        if (Objects.isNull(ruleStatus)) {
            throw new MarsRuntimeException(ErrorCodeMessage.POLICY_NOT_EXIST);
        }
        int switchStatus = Integer.valueOf(ruleStatus.get("switchStatus").toString());
        if (switchStatus != 0 && switchStatus != 1) {
            throw new MarsRuntimeException(ErrorCodeMessage.DB_DATA_ERROR);
        }
        return switchStatus == CommonConstant.ISTIO_POLICY_OPEN;
    }

    /**
     * 更新策略策略状态
     */
    public static void updateRuleOverviewDataStatus(String ruleId, int dataStatus, String userName, int err, RuleOverviewMapper ruleOverviewMapper) {
        RuleOverview ruleOverview = new RuleOverview();
        ruleOverview.setRuleId(ruleId);
        ruleOverview.setDataStatus(dataStatus);
        ruleOverview.setUserName(userName);
        ruleOverview.setDataErrLoc(err);
        ruleOverview.setUpdateTime(new Date());
        ruleOverviewMapper.updateDataStatus(ruleOverview);
    }

    public static ObjectMeta makeObjectMeta(String ruleName, String namespace, String ruleType, String serviceName) {
        ObjectMeta meta = new ObjectMeta();
        meta.setName(serviceName);
        meta.setNamespace(namespace);
        Map<String, Object> label = new HashMap<>();
        label.put(CommonConstant.LABEL_KEY_APP, serviceName);
        label.put(CommonConstant.ISTIO_RULE_TYPE, ruleType);
        label.put(CommonConstant.ISTIO_RULE_NAME, ruleName);
        meta.setLabels(label);
        return meta;
    }

    //Quota
    public static RuleDetail makeRateLimitQuotaRuleDetail(RateLimitDto rateLimitDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = makeObjectMeta(rateLimitDto.getRuleName(), rateLimitDto.getNamespace(), rateLimitDto.getRuleType(), rateLimitDto.getServiceName());
        QuotaInstance quotaInstance = new QuotaInstance();
        quotaInstance.setApiVersion(CommonConstant.CONFIG_ISTIO_V1ALPHA2);
        quotaInstance.setKind(CommonConstant.RATE_LIMIT_QUOTA);
        quotaInstance.setMetadata(meta);
        QuotaInstanceSpec quotaInstanceSpec = new QuotaInstanceSpec();
        Map<String, String> dimension = new HashMap<>();
        if (CollectionUtils.isNotEmpty(rateLimitDto.getOverrides())) {
            rateLimitDto.getOverrides().forEach(override -> {
                if (CollectionUtils.isNotEmpty(override.getHeaders())) {
                    override.getHeaders().forEach(header -> {
                        String key = String.format("request.headers[\"%s\"] | \"unknown\"", header.split("=")[0]);
                        dimension.put(header.split("=")[0], key);
                    });
                }
            });
        }
        dimension.put("sourceName", "source.labels[\"app\"] | \"unknown\"");
        quotaInstanceSpec.setDimensions(dimension);
        quotaInstance.setSpec(quotaInstanceSpec);
        String quotaInstanceStr = JsonUtil.objectToJson(quotaInstance);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(1);
        ruleDetail.setRuleDetailContent(quotaInstanceStr.getBytes());
        return ruleDetail;
    }

    //RedisQuota
    public static RuleDetail makeRateLimitRedisQuotaRuleDetail(RateLimitDto rateLimitDto, String ruleId, String istioRedisAddress) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = makeObjectMeta(rateLimitDto.getRuleName(), rateLimitDto.getNamespace(), rateLimitDto.getRuleType(), rateLimitDto.getServiceName());
        RedisQuota redisQuota = new RedisQuota();
        redisQuota.setApiVersion(CommonConstant.CONFIG_ISTIO_V1ALPHA2);
        redisQuota.setKind(CommonConstant.RATE_LIMIT_REDIS_QUOTA);
        RedisQuotaSpecQuota redisQuotaSpecQuota = new RedisQuotaSpecQuota();
        redisQuotaSpecQuota.setName(rateLimitDto.getServiceName() + ".quota." + rateLimitDto.getNamespace());
        redisQuotaSpecQuota.setMaxAmount(Integer.valueOf(rateLimitDto.getMaxAmount()));
        List<RateLimitOverrideDto> overrides = rateLimitDto.getOverrides();
        if (CollectionUtils.isNotEmpty(overrides)) {
            List<QuotaOverride> quotaOverrides = new ArrayList<>();
            overrides.forEach(override -> {
                QuotaOverride quotaOverride = new QuotaOverride();
                Map<String, String> dimensions = new HashMap<>();
                if (StringUtils.isNotBlank(override.getScopeServiceName())) {
                    dimensions.put("sourceName", override.getScopeServiceName());
                }
                if (CollectionUtils.isNotEmpty(override.getHeaders())) {
                    override.getHeaders().forEach(header -> dimensions.put(header.split("=")[0], header.split("=")[1]));
                }
                quotaOverride.setDimensions(dimensions);
                quotaOverride.setMaxAmount(Integer.valueOf(override.getMaxAmount()));
                quotaOverrides.add(quotaOverride);
            });
            redisQuotaSpecQuota.setOverrides(quotaOverrides);
        }
        redisQuotaSpecQuota.setValidDuration(rateLimitDto.getValidDuration() + CommonConstant.SECOND);
        redisQuotaSpecQuota.setRateLimitAlgorithm(rateLimitDto.getAlgorithm());
        if (CommonConstant.RATE_LIMIT_ALGORITHM_ROLLING_WINDOW.equalsIgnoreCase(rateLimitDto.getAlgorithm()) && StringUtils.isNotBlank(rateLimitDto.getBucketDuration())) {
            redisQuotaSpecQuota.setBucketDuration(rateLimitDto.getBucketDuration() + CommonConstant.SECOND);
        }
        RedisQuotaSpec redisQuotaSpec = new RedisQuotaSpec();
        redisQuotaSpec.setConnectionPoolSize(CommonConstant.PERCENT_HUNDRED);
        redisQuotaSpec.setQuotas(Collections.singletonList(redisQuotaSpecQuota));
        redisQuotaSpec.setRedisServerUrl(istioRedisAddress);
        redisQuota.setSpec(redisQuotaSpec);
        redisQuota.setMetadata(meta);
        String redisQuotaStr = JsonUtil.objectToJson(redisQuota);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(2);
        ruleDetail.setRuleDetailContent(redisQuotaStr.getBytes());
        return ruleDetail;
    }

    //QuotaSpec
    public static RuleDetail makeRateLimitQuotaSpecRuleDetail(RateLimitDto rateLimitDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = makeObjectMeta(rateLimitDto.getRuleName(), rateLimitDto.getNamespace(), rateLimitDto.getRuleType(), rateLimitDto.getServiceName());
        QuotaSpecRuleQuota ruleQuota = new QuotaSpecRuleQuota();
        ruleQuota.setCharge(CommonConstant.QUOTASPEC_CHARGE_ONE);
        ruleQuota.setQuota(rateLimitDto.getServiceName());
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
    public static RuleDetail makeRateLimitQuotaSpecBindingRuleDetail(RateLimitDto rateLimitDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = makeObjectMeta(rateLimitDto.getRuleName(), rateLimitDto.getNamespace(), rateLimitDto.getRuleType(), rateLimitDto.getServiceName());
        QuotaSpecBindingSpec specBindingSpec = new QuotaSpecBindingSpec();
        Map<String, String> quotaSpecMap = new HashMap<>();
        quotaSpecMap.put("name", rateLimitDto.getServiceName());
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
    public static RuleDetail makeRateLimitRuleRuleDetail(RateLimitDto rateLimitDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = makeObjectMeta(rateLimitDto.getRuleName(), rateLimitDto.getNamespace(), rateLimitDto.getRuleType(), rateLimitDto.getServiceName());
        //为了区别白名单中的
        meta.setName(CommonConstant.RATE_LIMIT_PREFIX + meta.getName());
        Rule rule = new Rule();
        rule.setApiVersion(CommonConstant.CONFIG_ISTIO_V1ALPHA2);
        rule.setKind(CommonConstant.ISTIO_RULE);
        Action action = new Action();
        action.setHandler(rateLimitDto.getServiceName() + ".redisquota." + rateLimitDto.getNamespace());
        action.setInstances(Collections.singletonList(rateLimitDto.getServiceName() + ".quota." + rateLimitDto.getNamespace()));
        RuleSpec ruleSpec = new RuleSpec();
        ruleSpec.setActions(Collections.singletonList(action));
        rule.setSpec(ruleSpec);
        rule.setMetadata(meta);
        String ruleStr = JsonUtil.objectToJson(rule);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(5);
        ruleDetail.setRuleDetailContent(ruleStr.getBytes());
        return ruleDetail;
    }

    /**
     * 组装白名单策略里的listChecker
     */
    public static RuleDetail makeListCheckerRuleDetail(WhiteListsDto whiteListsDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = makeObjectMeta(whiteListsDto.getRuleName(), whiteListsDto.getNamespace(), whiteListsDto.getRuleType(), whiteListsDto.getServiceName());
        //build listChecker
        ListChecker listChecker = new ListChecker();
        listChecker.setApiVersion(CommonConstant.CONFIG_ISTIO_V1ALPHA2);
        listChecker.setKind(CommonConstant.WHITE_LISTS_LIST_CHECKER);
        ListCheckerSpec listCheckerSpec = new ListCheckerSpec();
        listChecker.setMetadata(meta);
        List<String> overrides = new ArrayList<>();
        overrides.add(DEFAULT_OVERRIDE);
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
     */
    public static RuleDetail makeListEntryDetail(WhiteListsDto whiteListsDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = makeObjectMeta(whiteListsDto.getRuleName(), whiteListsDto.getNamespace(), whiteListsDto.getRuleType(), whiteListsDto.getServiceName());
        ListEntry listEntry = new ListEntry();
        listEntry.setApiVersion(CommonConstant.CONFIG_ISTIO_V1ALPHA2);
        listEntry.setKind(CommonConstant.WHITE_LISTS_LIST_ENTRY);
        ListEntrySpec listEntrySpec = new ListEntrySpec();
        listEntry.setMetadata(meta);
        listEntrySpec.setValue("source.namespace + \"_\" + ( source.labels[\"app\"] | \"unknown\" )");
        listEntry.setSpec(listEntrySpec);
        String listEntrySrc = JsonUtil.objectToJson(listEntry);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(2);
        ruleDetail.setRuleDetailContent(listEntrySrc.getBytes());
        return ruleDetail;
    }

    /**
     * 组装白名单策略里的rule
     */
    public static RuleDetail makeRuleRuleDetail(WhiteListsDto whiteListsDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        ObjectMeta meta = makeObjectMeta(whiteListsDto.getRuleName(), whiteListsDto.getNamespace(), whiteListsDto.getRuleType(), whiteListsDto.getServiceName());
        meta.setName(CommonConstant.WHITE_LISTS_PREFIX + meta.getName());
        Rule rule = new Rule();
        rule.setApiVersion(CommonConstant.CONFIG_ISTIO_V1ALPHA2);
        rule.setKind(CommonConstant.ISTIO_RULE);
        rule.setMetadata(meta);
        RuleSpec ruleSpec = new RuleSpec();
        Action action = new Action();
        List<Action> actions = new ArrayList<>();
        action.setHandler(whiteListsDto.getServiceName() + ".listchecker");
        List<String> instances = new ArrayList<>();
        instances.add(whiteListsDto.getServiceName() + ".listentry");
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

    /**
     * 集群中存在virtualService对象时，创建智能路由策略
     */
    public static void makeUpdateVirtualService(VirtualService virtualService, List<HTTPRoute> httpDetail) {
        HTTPFaultInjection fault = null;
        String timeout = "";
        HTTPRetry retries = null;
        if (CollectionUtils.isNotEmpty(virtualService.getSpec().getHttp())) {
            HTTPRoute httpRoute = virtualService.getSpec().getHttp().get(0);
            if (Objects.nonNull(httpRoute.getFault())) {
                fault = httpRoute.getFault();
            }
            if (StringUtils.isNotBlank(httpRoute.getTimeout())) {
                timeout = httpRoute.getTimeout();
            }
            if (Objects.nonNull(httpRoute.getRetries())) {
                retries = httpRoute.getRetries();
            }
        }
        for (HTTPRoute httpRoute : httpDetail) {
            if (Objects.nonNull(fault)) {
                httpRoute.setFault(fault);
            }
            if (Objects.nonNull(retries)) {
                httpRoute.setRetries(retries);
            }
            if (StringUtils.isNotBlank(timeout)) {
                httpRoute.setTimeout(timeout);
            }
        }
        virtualService.getSpec().setHttp(httpDetail);
    }

    public static VirtualService makeVirtualService(List<HTTPRoute> httpDetail, String name, String namespace, String ruleType) {
        VirtualService virtualService = new VirtualService();
        ObjectMeta meta = makeObjectMeta(name, namespace, ruleType, name);
        virtualService.setMetadata(meta);
        virtualService.setApiVersion(CommonConstant.NETWORKING_ISTIO_V1ALPHA3);
        virtualService.setKind(CommonConstant.ISTIO_VIRTUALSERVICE);
        VirtualServiceSpec spec = new VirtualServiceSpec();
        spec.setHosts(Collections.singletonList(name));
        spec.setHttp(httpDetail);
        virtualService.setSpec(spec);
        return virtualService;
    }

    public static VirtualService makeVirtualService(HTTPFaultInjection faultInjection, String name, String namespace, String ruleType, String host) {
        VirtualService virtualService = new VirtualService();
        ObjectMeta meta = makeObjectMeta(name, namespace, ruleType, name);
        virtualService.setMetadata(meta);
        virtualService.setApiVersion(CommonConstant.NETWORKING_ISTIO_V1ALPHA3);
        virtualService.setKind(CommonConstant.ISTIO_VIRTUALSERVICE);
        VirtualServiceSpec spec = new VirtualServiceSpec();
        spec.setHosts(Collections.singletonList(StringUtils.isNotBlank(host) ? host : name));
        HTTPRoute httpRoute = new HTTPRoute();
        List<DestinationWeight> route = new ArrayList<>();
        Destination destination = new Destination();
        destination.setHost(StringUtils.isNotBlank(host) ? host : name);
        DestinationWeight destinationWeight = new DestinationWeight();
        destinationWeight.setDestination(destination);
        route.add(destinationWeight);
        httpRoute.setRoute(route);
        httpRoute.setFault(faultInjection);
        spec.setHttp(Collections.singletonList(httpRoute));
        virtualService.setSpec(spec);
        return virtualService;
    }

    public static VirtualService makeVirtualService(String timeout, HTTPRetry retry, String name, String namespace, String ruleType, String host) {
        VirtualService virtualService = new VirtualService();
        ObjectMeta meta = makeObjectMeta(name, namespace, ruleType, name);
        virtualService.setMetadata(meta);
        virtualService.setApiVersion(CommonConstant.NETWORKING_ISTIO_V1ALPHA3);
        virtualService.setKind(CommonConstant.ISTIO_VIRTUALSERVICE);
        VirtualServiceSpec spec = new VirtualServiceSpec();
        spec.setHosts(Collections.singletonList(StringUtils.isNotBlank(host) ? host : name));
        HTTPRoute httpRoute = new HTTPRoute();
        httpRoute.setTimeout(timeout);
        httpRoute.setRetries(retry);
        List<DestinationWeight> route = new ArrayList<>();
        Destination destination = new Destination();
        destination.setHost(StringUtils.isNotBlank(host) ? host : name);
        DestinationWeight destinationWeight = new DestinationWeight();
        destinationWeight.setDestination(destination);
        route.add(destinationWeight);
        httpRoute.setRoute(route);
        spec.setHttp(Collections.singletonList(httpRoute));
        virtualService.setSpec(spec);
        return virtualService;
    }

    /**
     * 判断该对象中含有策略
     * timeoutRetry,fault,trafficshifting 111
     */
    public static int checkVirtualServicePolicyType(VirtualService virtualService) {
        char[] typeFlag = new char[]{'0', '0', '0'};
        List<HTTPRoute> http = virtualService.getSpec().getHttp();
        HTTPRoute httpRouteFirst = http.get(0);
        if (StringUtils.isNotBlank(httpRouteFirst.getTimeout()) ||
                (Objects.nonNull(httpRouteFirst.getRetries()) &&
                        StringUtils.isNotBlank(httpRouteFirst.getRetries().getPerTryTimeout()) &&
                        Objects.nonNull(httpRouteFirst.getRetries().getAttempts()))) {
            typeFlag[0] = '1';
        }
        if (Objects.nonNull(httpRouteFirst.getFault())) {
            typeFlag[1] = '1';
        }
        for (int i = 0; i < http.size(); i++) {
            HTTPRoute httpRoute = http.get(i);
            if (CollectionUtils.isNotEmpty(httpRoute.getMatch()) || httpRoute.getRoute().size() > 1) {
                typeFlag[2] = '1';
                break;
            }
        }
        return Integer.parseInt(String.copyValueOf(typeFlag), 2);
    }

    /**
     * 在virtualService包含其他策略的情况下删除k8s中智能路由策略
     */
    public static void deleteK8sTrafficShiftingPolicy(VirtualService virtualService) {
        HTTPRoute httpRouteDefault = new HTTPRoute();
        HTTPRoute httpRoute = virtualService.getSpec().getHttp().get(0);
        if (Objects.nonNull(httpRoute.getFault())) {
            httpRouteDefault.setFault(httpRoute.getFault());
        }
        if (Objects.nonNull(httpRoute.getRetries())) {
            httpRouteDefault.setRetries(httpRoute.getRetries());
        }
        if (StringUtils.isNotBlank(httpRoute.getTimeout())) {
            httpRouteDefault.setTimeout(httpRoute.getTimeout());
        }
        DestinationWeight destinationWeight = new DestinationWeight();
        Destination destination = new Destination();
        destination.setHost(httpRoute.getRoute().get(0).getDestination().getHost());
        destinationWeight.setDestination(destination);
        httpRouteDefault.setRoute(Collections.singletonList(destinationWeight));
        List<HTTPRoute> httpDefault = Collections.singletonList(httpRouteDefault);
        virtualService.getSpec().setHttp(httpDefault);
    }

    public static boolean checkVirtualServiceVersion(String namespace, String deployName, Cluster cluster, List<HTTPRoute> httpDetail, DestinationRuleService destinationRuleService) {
        K8SClientResponse destinationRuleResponse = destinationRuleService.getDestinationRule(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(destinationRuleResponse.getStatus())) {
            LOGGER.error("get DestinationRule error", destinationRuleResponse.getBody());
            return false;
        }
        DestinationRule destinationRule = JsonUtil.jsonToPojo(destinationRuleResponse.getBody(), DestinationRule.class);
        Set<String> subsetSet = new HashSet<>();
        if (Objects.isNull(destinationRule.getSpec()) ||
                CollectionUtils.isEmpty(destinationRule.getSpec().getSubsets())) {
            return false;
        } else {
            List<Subset> subsets = destinationRule.getSpec().getSubsets();
            subsets.forEach(subset -> subsetSet.add(subset.getName()));
        }
        if (CollectionUtils.isNotEmpty(httpDetail)) {
            for (HTTPRoute httpRoute : httpDetail) {
                List<DestinationWeight> route = httpRoute.getRoute();
                for (DestinationWeight weight : route) {
                    if (StringUtils.isNotBlank(weight.getDestination().getSubset()) &&
                            !subsetSet.contains(weight.getDestination().getSubset())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean equalsList(List<HTTPRoute> httpDBDetail, List<HTTPRoute> httpK8sDetail) {
        if (CollectionUtils.isEmpty(httpDBDetail) || CollectionUtils.isEmpty(httpK8sDetail)) {
            return false;
        }
        if (httpDBDetail.size() != httpK8sDetail.size()) {
            return false;
        }
        for (int i = 0; i < httpDBDetail.size(); i++) {
            HTTPRoute httpRouteDBDetail = httpDBDetail.get(i);
            HTTPRoute httpRouteK8sDetail = httpK8sDetail.get(i);
            httpRouteDBDetail.setRetries(null);
            httpRouteDBDetail.setTimeout(null);
            httpRouteDBDetail.setFault(null);
            httpRouteK8sDetail.setRetries(null);
            httpRouteK8sDetail.setTimeout(null);
            httpRouteK8sDetail.setFault(null);
            if (!httpRouteDBDetail.equals(httpRouteK8sDetail)) {
                return false;
            }
        }
        return true;
    }

    public static RuleDetail makeTimeoutRuleDetail(TimeoutRetryDto timeoutRetryDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        String timeout = "";
        if (StringUtils.isNotBlank(timeoutRetryDto.getTimeout())) {
            timeout = timeoutRetryDto.getTimeout() + CommonConstant.SECOND;
        }
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(CommonConstant.TIMEOUT_RETRY_TIMEOUT_ORDER);
        ruleDetail.setRuleDetailContent(timeout.getBytes());
        return ruleDetail;
    }

    public static RuleDetail makeRetryRuleDetail(TimeoutRetryDto timeoutRetryDto, String ruleId) {
        RuleDetail ruleDetail = new RuleDetail();
        HTTPRetry httpRetry = new HTTPRetry();
        if (StringUtils.isNotBlank(timeoutRetryDto.getPerTryTimeout())) {
            httpRetry.setAttempts(Integer.valueOf(timeoutRetryDto.getAttempts()));
            httpRetry.setPerTryTimeout(timeoutRetryDto.getPerTryTimeout() + CommonConstant.SECOND);
        }
        String httpRetryStr = JsonUtil.convertToJsonNonNull(httpRetry);
        ruleDetail.setRuleId(ruleId);
        ruleDetail.setRuleDetailOrder(CommonConstant.TIMEOUT_RETRY_RETRY_ORDER);
        ruleDetail.setRuleDetailContent(httpRetryStr.getBytes());
        return ruleDetail;
    }

    public static void createServiceEntryDR(String deployName, String namespace,String host,Cluster cluster, DestinationRuleService destinationRuleService) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        DestinationRule destinationRule = makeServiceEntryDR(deployName, namespace, host);
        K8SClientResponse response = destinationRuleService.createDestinationRule(namespace, destinationRule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("create DestinationRule error", response.getBody());
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
    }

    /**
     * 创建serviceEntry的destinationRule
     * @param host
     * @param namespace
     * @return
     */
    private static DestinationRule makeServiceEntryDR(String deployName, String namespace, String host) {
        ObjectMeta meta = new ObjectMeta();
        meta.setName(deployName);
        meta.setNamespace(namespace);
        Map<String, Object> label = new HashMap<>();
        label.put(CommonConstant.LABEL_KEY_APP, deployName);
        meta.setLabels(label);
        DestinationRule destinationRule = new DestinationRule();
        destinationRule.setApiVersion(CommonConstant.NETWORKING_ISTIO_V1ALPHA3);
        destinationRule.setKind(CommonConstant.DESTINATION_RULE);
        destinationRule.setMetadata(meta);
        DestinationRuleSpec spec = new DestinationRuleSpec();
        spec.setHost(host);
        destinationRule.setSpec(spec);
        return destinationRule;
    }

    /**
     * 组装外部服务入口的serviceEntry
     *
     * @param serviceEntryDto
     * @return
     */
    public static ServiceEntry makeExternalServiceEntry(ServiceEntryDto serviceEntryDto) {
        ObjectMeta meta = new ObjectMeta();
        Map<String, Object> labels = new HashMap<>();
        if (serviceEntryDto.getLabels() != null) {
            labels = serviceEntryDto.getLabels();
        }
        meta.setName(serviceEntryDto.getName());
        meta.setNamespace(CommonConstant.ISTIO_NAMESPACE);
        meta.setLabels(labels);
        // 增加spec
        ServiceEntry serviceEntry = new ServiceEntry();
        serviceEntry.setApiVersion(CommonConstant.NETWORKING_ISTIO_V1ALPHA3);
        serviceEntry.setKind(CommonConstant.SERVICE_ENTRY);
        serviceEntry.setMetadata(meta);

        ServiceEntrySpec serviceEntrySpec = new ServiceEntrySpec();
        List<String> hosts = new ArrayList<>();
        hosts.add(serviceEntryDto.getHosts());
        serviceEntrySpec.setHosts(hosts);
        serviceEntrySpec.setLocation(CommonConstant.MESH_EXTERNAL);
        List<Port> ports = new ArrayList<>();
        List<ServiceEntryPortDto>  portList = serviceEntryDto.getPortList();
        if(Objects.nonNull(serviceEntryDto.getPortList())){
            for(ServiceEntryPortDto portItem : portList){
                Port port = new Port();
                port.setNumber(portItem.getNumber());
                port.setName(portItem.getProtocol().toLowerCase() + "-" + portItem.getNumber());
                port.setProtocol(portItem.getProtocol());
                ports.add(port);
            }
        }
        serviceEntrySpec.setPorts(ports);
        serviceEntrySpec.setResolution(CommonConstant.RESOLUTION_NONE);
        serviceEntry.setSpec(serviceEntrySpec);
        return serviceEntry;
    }

    /**
     * 组装服务
     *
     * @param serviceEntryDto serviceEntryDto
     * @return 返回值
     * @throws Exception 异常信息
     */
    public static com.harmonycloud.k8s.bean.Service makeService(ServiceEntryDto serviceEntryDto, String projectId) throws Exception {
        // 创建service包含了创建所需的所有所需参数
        com.harmonycloud.k8s.bean.Service service = new com.harmonycloud.k8s.bean.Service();
        ObjectMeta meta = new ObjectMeta();
        meta.setName(serviceEntryDto.getHosts());
        Map<String, Object> labels = new HashMap<>();
        if (serviceEntryDto.getLabels() != null) {
            labels = serviceEntryDto.getLabels();
        }
        labels.put(LABEL_PROJECT_ID, projectId + CommonConstant.PROJECT_ID_SERVICEENTRY);
        meta.setNamespace(serviceEntryDto.getNamespace());
        meta.setLabels(labels);
        // 增加spec
        ServiceSpec serviceSpec = new ServiceSpec();
        List<ServicePort> ports = new ArrayList<>();
        List<ServiceEntryPortDto> portList = serviceEntryDto.getPortList();
        if(CollectionUtils.isNotEmpty(portList)){
            ServicePort servicePort = new ServicePort();
            if(Objects.nonNull(portList.get(0).getProtocol())){
               servicePort.setName(portList.get(0).getProtocol().toLowerCase() + "-" + serviceEntryDto.getName());
            }
            if(Objects.nonNull(portList.get(0).getNumber())){
                servicePort.setPort(Integer.valueOf(portList.get(0).getNumber()));
            }
            servicePort.setProtocol(CommonConstant.PROTOCOL_TCP);
            ports.add(servicePort);
        }
        serviceSpec.setPorts(ports);
        service.setMetadata(meta);
        service.setSpec(serviceSpec);
        return service;
    }

    /**
     * 组装 内部服务入口
     * @param serviceEntryDto serviceEntryDto
     * @return 返回值
     */
    public static ServiceEntry makeInternalServiceEntry(ServiceEntryDto serviceEntryDto, String clusterIp, String projectId) {
        ObjectMeta meta = new ObjectMeta();
        Map<String, Object> labels = new HashMap<>();
        if (serviceEntryDto.getLabels() != null) {
            labels = serviceEntryDto.getLabels();
        }
        labels.put(LABEL_PROJECT_ID, projectId + CommonConstant.PROJECT_ID_SERVICEENTRY);
        meta.setName(serviceEntryDto.getName());
        meta.setNamespace(serviceEntryDto.getNamespace());
        meta.setLabels(labels);
        // 增加spec
        ServiceEntry serviceEntry = new ServiceEntry();
        serviceEntry.setApiVersion(CommonConstant.NETWORKING_ISTIO_V1ALPHA3);
        serviceEntry.setKind(CommonConstant.SERVICE_ENTRY);
        serviceEntry.setMetadata(meta);

        ServiceEntrySpec serviceEntrySpec = new ServiceEntrySpec();
        List<String> hosts = new ArrayList<>();
        String host = serviceEntryDto.getHosts() + "." + serviceEntryDto.getNamespace() + ".svc.cluster.local";
        hosts.add(host);
        serviceEntrySpec.setHosts(hosts);
        List<String> address = new ArrayList<>();
        address.add(clusterIp);
        serviceEntrySpec.setAddresses(address);
        serviceEntrySpec.setLocation(CommonConstant.MESH_INTERNAL);
        List<Port> ports = new ArrayList<>();
        List<ServiceEntryPortDto> portList = serviceEntryDto.getPortList();
        if(CollectionUtils.isNotEmpty(portList)){
            Port port = new Port();
            if(Objects.nonNull(portList.get(0).getNumber())){
                port.setNumber(portList.get(0).getNumber());
            }
            if(Objects.nonNull(portList.get(0).getProtocol())){
                port.setName(portList.get(0).getProtocol().toLowerCase() + "-" + serviceEntryDto.getName());
                port.setProtocol(portList.get(0).getProtocol());
            }
            ports.add(port);
        }
        serviceEntrySpec.setPorts(ports);
        serviceEntrySpec.setResolution(CommonConstant.RESOLUTION_STATIC);
        List<Endpoint> endpoints = new ArrayList<>();
        if (Objects.nonNull(serviceEntryDto.getIpList())) {
            for (String ipItem : serviceEntryDto.getIpList()) {
                Endpoint endpoint = new Endpoint();
                endpoint.setAddress(ipItem);
                endpoints.add(endpoint);
            }
        }
        serviceEntrySpec.setEndpoints(endpoints);
        serviceEntry.setSpec(serviceEntrySpec);
        return serviceEntry;
    }

    /**
     * 组装部分外部服务入口操作
     */
    public static ServiceEntry makePartExternalServiceEntry(ServiceEntry serviceEntry, ServiceEntryDto serviceEntryDto) {
        ServiceEntrySpec serviceEntrySpec = serviceEntry.getSpec();
        //修改hosts
        List<String> hosts = new ArrayList<>();
        hosts.add(serviceEntryDto.getHosts());
        serviceEntrySpec.setHosts(hosts);
        //修改ports
        List<Port> ports = new ArrayList<>();
        List<ServiceEntryPortDto>  portList = serviceEntryDto.getPortList();
        if(Objects.nonNull(serviceEntryDto.getPortList())){
            for(ServiceEntryPortDto portItem : portList){
                Port port = new Port();
                port.setNumber(portItem.getNumber());
                port.setName(portItem.getProtocol().toLowerCase() + "-" + portItem.getNumber());
                port.setProtocol(portItem.getProtocol());
                ports.add(port);
            }
        }
        serviceEntrySpec.setPorts(ports);
        serviceEntry.setSpec(serviceEntrySpec);
        return serviceEntry;
    }

    /**
     * 组装部分内部服务入口操作
     * @param serviceEntry  原来的serviceEntry
     */
    public static ServiceEntry makePartInternalServiceEntry(ServiceEntry serviceEntry, String clusterIp, ServiceEntryDto serviceEntryDto) {
        ServiceEntrySpec serviceEntrySpec = serviceEntry.getSpec();
        //修改hosts
        List<String> hosts = new ArrayList<>();
        String host = serviceEntryDto.getHosts() + "." + serviceEntryDto.getNamespace() + ".svc.cluster.local";
        hosts.add(host);
        serviceEntrySpec.setHosts(hosts);
        //修改address
        List<String> address = new ArrayList<>();
        address.add(clusterIp);
        serviceEntrySpec.setAddresses(address);
        //修改ports
        List<Port> ports = new ArrayList<>();
        List<ServiceEntryPortDto> portList = serviceEntryDto.getPortList();
        if(CollectionUtils.isNotEmpty(portList)){
            Port port = new Port();
            if(Objects.nonNull(portList.get(0).getNumber())){
                port.setNumber(portList.get(0).getNumber());
            }
            if(Objects.nonNull(portList.get(0).getProtocol())){
                port.setName(portList.get(0).getProtocol().toLowerCase() + "-" + serviceEntryDto.getName());
                port.setProtocol(portList.get(0).getProtocol());
            }
            ports.add(port);
        }
        serviceEntrySpec.setPorts(ports);
        //修改endpoints
        List<Endpoint> endpoints = new ArrayList<>();
        if (Objects.nonNull(serviceEntryDto.getIpList())) {
            for (String ipItem : serviceEntryDto.getIpList()) {
                Endpoint endpoint = new Endpoint();
                endpoint.setAddress(ipItem);
                endpoints.add(endpoint);
            }
        }
        serviceEntrySpec.setEndpoints(endpoints);
        serviceEntry.setSpec(serviceEntrySpec);
        return serviceEntry;
    }

    /**
     * 组装部分服务
     */
    public static com.harmonycloud.k8s.bean.Service makePartService(com.harmonycloud.k8s.bean.Service service, ServiceEntryDto serviceEntryDto) throws Exception {
        ObjectMeta meta = service.getMetadata();
        meta.setName(serviceEntryDto.getHosts());
        // 增加spec
        ServiceSpec serviceSpec = service.getSpec();
        List<ServicePort> ports = new ArrayList<>();
        List<ServiceEntryPortDto> portList = serviceEntryDto.getPortList();
        if(CollectionUtils.isNotEmpty(portList)){
            ServicePort servicePort = new ServicePort();
            if(Objects.nonNull(portList.get(0).getProtocol())){
                servicePort.setName(portList.get(0).getProtocol().toLowerCase() + "-" + serviceEntryDto.getName());
            }
            if(Objects.nonNull(portList.get(0).getNumber())){
                servicePort.setPort(Integer.valueOf(portList.get(0).getNumber()));
            }
            servicePort.setProtocol(CommonConstant.PROTOCOL_TCP);
            ports.add(servicePort);
        }
        serviceSpec.setPorts(ports);
        service.setMetadata(meta);
        service.setSpec(serviceSpec);
        return service;
    }


    public static DestinationRule makeDestinationRule(String deployName, String namespace, List<String> versions) {
        DestinationRule destinationRule = new DestinationRule();
        destinationRule.setApiVersion(CommonConstant.NETWORKING_ISTIO_V1ALPHA3);
        destinationRule.setKind(CommonConstant.DESTINATION_RULE);
        ObjectMeta metadata = makeObjectMeta(deployName, namespace, CommonConstant.DESTINATION_RULE, deployName);
        destinationRule.setMetadata(metadata);
        DestinationRuleSpec spec = new DestinationRuleSpec();
        spec.setHost(deployName);
        List<Subset> subsets = new ArrayList<>();
        versions.forEach(version -> {
            Subset subset = new Subset();
            subset.setName(version);
            Map<String, String> subsetLabels = new HashMap<>();
            subsetLabels.put("version", version);
            subset.setLabels(subsetLabels);
            subsets.add(subset);
        });
        spec.setSubsets(subsets);
        destinationRule.setSpec(spec);
        return destinationRule;
    }

    /**
     * 查询该分区下是否有服务
     */
    public static boolean getNamespaceIstioStatus(String namespaceName, Cluster cluster, DeploymentService deploymentService) throws Exception {
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
     * 更新全局状态
     */
    public static void updateGlobalStatus(String clusterId, int switchStatus, String userName, IstioGlobalConfigureMapper istioGlobalConfigureMapper) throws ParseException {
        Date date = new Date();// 获得系统时间.
        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        String nowTime = sdf.format(date);
        Date time = sdf.parse(nowTime);
        IstioGlobalConfigure istioGlobalConfigure = new IstioGlobalConfigure();
        istioGlobalConfigure.setClusterId(clusterId);
        istioGlobalConfigure.setSwitchStatus(switchStatus);
        istioGlobalConfigure.setUpdateTime(time);
        if (Objects.nonNull(userName)) {
            istioGlobalConfigure.setUserName(userName);
        }
        int istioGlobalNumber = istioGlobalConfigureMapper.updateByClusterId(istioGlobalConfigure);
        if (istioGlobalNumber != 1) {
            throw new MarsRuntimeException(ErrorCodeMessage.UPDATE_FAIL);
        }
    }

    /**
     * 插入集群配置信息
     */
    public static void insertGlobalInfo(Cluster cluster, int switchStatus, String userName, IstioGlobalConfigureMapper istioGlobalConfigureMapper) throws ParseException {
        IstioGlobalConfigure istioGlobalConfigure = new IstioGlobalConfigure();
        istioGlobalConfigure.setClusterId(cluster.getId());
        istioGlobalConfigure.setClusterName(cluster.getAliasName());
        istioGlobalConfigure.setSwitchStatus(switchStatus);
        if (Objects.nonNull(userName)) {
            istioGlobalConfigure.setUserName(userName);
        }
        istioGlobalConfigure.setCreateTime(new Date());
        istioGlobalConfigure.setUpdateTime(istioGlobalConfigure.getCreateTime());
        int istioGlobalNumber = istioGlobalConfigureMapper.insert(istioGlobalConfigure);
        if (istioGlobalNumber != 1) {
            throw new MarsRuntimeException(ErrorCodeMessage.QUERY_FAIL);
        }
    }
}
