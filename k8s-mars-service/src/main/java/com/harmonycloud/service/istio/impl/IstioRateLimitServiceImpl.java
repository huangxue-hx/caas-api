package com.harmonycloud.service.istio.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.istio.RuleDetailMapper;
import com.harmonycloud.dao.istio.RuleOverviewMapper;
import com.harmonycloud.dao.istio.bean.RuleDetail;
import com.harmonycloud.dao.istio.bean.RuleOverview;
import com.harmonycloud.dto.application.istio.RateLimitDto;
import com.harmonycloud.dto.application.istio.RateLimitOverrideDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.policies.Rule;
import com.harmonycloud.k8s.bean.istio.policies.ratelimit.*;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.istio.RateLimitService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.istio.IstioRateLimitService;
import com.harmonycloud.service.istio.util.IstioPolicyUtil;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import org.apache.commons.collections.CollectionUtils;
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
public class IstioRateLimitServiceImpl implements IstioRateLimitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IstioRateLimitServiceImpl.class);

    @Value("#{propertiesReader['istio.redis.address']}")
    private String istioRedisAddress;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private RuleOverviewMapper ruleOverviewMapper;

    @Autowired
    private RuleDetailMapper ruleDetailMapper;

    @Autowired
    private HttpSession session;

    /**
     * 只做服务级别的限流，限流针对服务不做版本区分
     */
    @Override
    public ActionReturnUtil createRateLimitPolicy(String deployName, RateLimitDto rateLimitDto) throws Exception {
        AssertUtil.notNull(rateLimitDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        String namespace = rateLimitDto.getNamespace();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        List<String> algorithmList = Arrays.asList(CommonConstant.RATE_LIMIT_ALGORITHM_FIXED_WINDOW, CommonConstant.RATE_LIMIT_ALGORITHM_ROLLING_WINDOW);
        if (!algorithmList.contains(rateLimitDto.getAlgorithm())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FRONT_DATA_ERROR);
        }
        //是否存在只判断数据库
        if (IstioPolicyUtil.checkPolicyExist(cluster.getId(), namespace, deployName, rateLimitDto.getRuleName(), rateLimitDto.getRuleType(), ruleOverviewMapper)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_EXIST);
        }
        String ruleId = UUIDUtil.getUUID();
        IstioPolicyUtil.insertRuleOverview(rateLimitDto, ruleId, cluster.getId(), userName, ruleOverviewMapper);
        //组装quota入库对象
        RuleDetail quotaRuleDetail = IstioPolicyUtil.makeRateLimitQuotaRuleDetail(rateLimitDto, ruleId);
        quotaRuleDetail.setCreateTime(new Date());
        ruleDetailMapper.insert(quotaRuleDetail);
        //组装redisquota入库对象
        RuleDetail redisQuotaRuleDetail = IstioPolicyUtil.makeRateLimitRedisQuotaRuleDetail(rateLimitDto, ruleId, istioRedisAddress);
        redisQuotaRuleDetail.setCreateTime(new Date());
        ruleDetailMapper.insert(redisQuotaRuleDetail);
        //组装quotaspec入库对象
        RuleDetail quotaSpecRuleDetail = IstioPolicyUtil.makeRateLimitQuotaSpecRuleDetail(rateLimitDto, ruleId);
        quotaSpecRuleDetail.setCreateTime(new Date());
        ruleDetailMapper.insert(quotaSpecRuleDetail);
        //组装quotaspecbinding入库对象
        RuleDetail quotaSpecBindingRuleDetail = IstioPolicyUtil.makeRateLimitQuotaSpecBindingRuleDetail(rateLimitDto, ruleId);
        quotaSpecBindingRuleDetail.setCreateTime(new Date());
        ruleDetailMapper.insert(quotaSpecBindingRuleDetail);
        //组装rule入库对象
        RuleDetail ruleRuleDetail = IstioPolicyUtil.makeRateLimitRuleRuleDetail(rateLimitDto, ruleId);
        ruleRuleDetail.setCreateTime(new Date());
        ruleDetailMapper.insert(ruleRuleDetail);
        //创建Quota
        QuotaInstance quotaInstance = JsonUtil.jsonToPojo(new String(quotaRuleDetail.getRuleDetailContent()), QuotaInstance.class);
        K8SClientResponse quotaResponse = rateLimitService.createRateLimitResource(namespace, quotaInstance, cluster, Resource.QUOTA);
        if (!HttpStatusUtil.isSuccessStatus(quotaResponse.getStatus())) {
            LOGGER.error("create QuotaInstance error", quotaResponse.getBody());
            IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_FAILED);
        }
        //创建redisQuota
        RedisQuota redisQuota = JsonUtil.jsonToPojo(new String(redisQuotaRuleDetail.getRuleDetailContent()), RedisQuota.class);
        K8SClientResponse redisQuotaResponse = rateLimitService.createRateLimitResource(namespace, redisQuota, cluster, Resource.REDISQUOTA);
        if (!HttpStatusUtil.isSuccessStatus(redisQuotaResponse.getStatus())) {
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, rateLimitDto.getServiceName(), cluster, redisQuotaRuleDetail.getRuleDetailOrder() - 1);
            if (res.isEmpty()) {
                IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
            } else {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userName, Integer.valueOf(res.get("faileNum").toString()), ruleOverviewMapper);
            }
            LOGGER.error("create ratelimit policy error", redisQuotaResponse.getBody());
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_FAILED);
        }
        //创建QuotaSpec
        QuotaSpec quotaSpec = JsonUtil.jsonToPojo(new String(quotaSpecRuleDetail.getRuleDetailContent()), QuotaSpec.class);
        K8SClientResponse quotaSpecResponse = rateLimitService.createRateLimitResource(namespace, quotaSpec, cluster, Resource.QUOTASPEC);
        if (!HttpStatusUtil.isSuccessStatus(quotaSpecResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", quotaSpecResponse.getBody());
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, rateLimitDto.getServiceName(), cluster, quotaSpecRuleDetail.getRuleDetailOrder() - 1);
            if (res.isEmpty()) {
                IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
            } else {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userName, Integer.valueOf(res.get("faileNum").toString()), ruleOverviewMapper);
            }
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_FAILED);
        }
        //创建QuotaSpecBinding
        QuotaSpecBinding quotaSpecBinding = JsonUtil.jsonToPojo(new String(quotaSpecBindingRuleDetail.getRuleDetailContent()), QuotaSpecBinding.class);
        K8SClientResponse quotaSpecBindingResponse = rateLimitService.createRateLimitResource(namespace, quotaSpecBinding, cluster, Resource.QUOTASPECBINDING);
        if (!HttpStatusUtil.isSuccessStatus(quotaSpecBindingResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", quotaSpecBindingResponse.getBody());
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, rateLimitDto.getServiceName(), cluster, quotaSpecBindingRuleDetail.getRuleDetailOrder() - 1);
            if (res.isEmpty()) {
                IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
            } else {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userName, Integer.valueOf(res.get("faileNum").toString()), ruleOverviewMapper);
            }
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_FAILED);
        }
        //创建rule
        Rule rule = JsonUtil.jsonToPojo(new String(ruleRuleDetail.getRuleDetailContent()), Rule.class);
        K8SClientResponse ruleResponse = rateLimitService.createRateLimitResource(namespace, rule, cluster, Resource.RULE);
        if (!HttpStatusUtil.isSuccessStatus(ruleResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", ruleResponse.getBody());
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, rateLimitDto.getServiceName(), cluster, ruleRuleDetail.getRuleDetailOrder() - 1);
            if (res.isEmpty()) {
                IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
            } else {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userName, Integer.valueOf(res.get("faileNum").toString()), ruleOverviewMapper);
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
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        RuleDetail quotaRuleDetail = IstioPolicyUtil.makeRateLimitQuotaRuleDetail(rateLimitDto, ruleId);
        quotaRuleDetail.setUpdateTime(new Date());
        ruleDetailMapper.updateByPrimaryKeySelective(quotaRuleDetail);
        RuleDetail redisQuotaRuleDetail = IstioPolicyUtil.makeRateLimitRedisQuotaRuleDetail(rateLimitDto, ruleId, istioRedisAddress);
        redisQuotaRuleDetail.setUpdateTime(new Date());
        ruleDetailMapper.updateByPrimaryKeySelective(redisQuotaRuleDetail);
        if (!ruleOverview.getRuleScope().equals(rateLimitDto.getScope())) {
            RuleOverview updateRuleScope = new RuleOverview();
            updateRuleScope.setRuleId(ruleId);
            updateRuleScope.setRuleScope(rateLimitDto.getScope());
            updateRuleScope.setUserName(userName);
            updateRuleScope.setUpdateTime(new Date());
            ruleOverviewMapper.updateByPrimaryKeySelective(updateRuleScope);
        }
        //开关开启状态下更新
        if (IstioPolicyUtil.checkPolicyStatus(ruleId, ruleOverviewMapper)) {
            //更新quota
            QuotaInstance quotaInstance = JsonUtil.jsonToPojo(new String(quotaRuleDetail.getRuleDetailContent()), QuotaInstance.class);
            K8SClientResponse responseQuota = rateLimitService.getRateLimitResource(namespace, rateLimitDto.getServiceName(), CommonConstant.RATE_LIMIT_QUOTA, cluster);
            if (!HttpStatusUtil.isSuccessStatus(responseQuota.getStatus())) {
                LOGGER.error("get ratelimit policy error", responseQuota.getBody());
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, quotaRuleDetail.getRuleDetailOrder(), ruleOverviewMapper);
                return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
            }
            QuotaInstance quotaInstanceData = JsonUtil.jsonToPojo(responseQuota.getBody(), QuotaInstance.class);
            quotaInstanceData.getSpec().setDimensions(quotaInstance.getSpec().getDimensions());
            K8SClientResponse quotaResponse = rateLimitService.updateQuotaInstance(namespace, rateLimitDto.getServiceName(), quotaInstanceData, cluster);
            if (!HttpStatusUtil.isSuccessStatus(quotaResponse.getStatus())) {
                LOGGER.error("update rateLimit quota error", quotaResponse.getBody());
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userName, quotaRuleDetail.getRuleDetailOrder(), ruleOverviewMapper);
                return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_UPDATE_FAILED);
            }
            //更新redisquota
            RedisQuota redisQuota = JsonUtil.jsonToPojo(new String(redisQuotaRuleDetail.getRuleDetailContent()), RedisQuota.class);
            K8SClientResponse response = rateLimitService.getRateLimitResource(namespace, rateLimitDto.getServiceName(), CommonConstant.RATE_LIMIT_REDIS_QUOTA, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                LOGGER.error("get ratelimit policy error", response.getBody());
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, redisQuotaRuleDetail.getRuleDetailOrder(), ruleOverviewMapper);
                return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
            }
            RedisQuota redisQuotaData = JsonUtil.jsonToPojo(response.getBody(), RedisQuota.class);
            redisQuotaData.getSpec().setQuotas(redisQuota.getSpec().getQuotas());
            K8SClientResponse redisQuotaResponse = rateLimitService.updateRedisQuota(namespace, rateLimitDto.getServiceName(), redisQuotaData, cluster);
            if (!HttpStatusUtil.isSuccessStatus(redisQuotaResponse.getStatus())) {
                LOGGER.error("update rateLimit redisquota error", redisQuotaResponse.getBody());
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userName, redisQuotaRuleDetail.getRuleDetailOrder(), ruleOverviewMapper);
                return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_UPDATE_FAILED);
            }
        }
        IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_IS_OK, userName, CommonConstant.DATA_IS_OK, ruleOverviewMapper);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_UPDATE_SUCCESS);
    }

    @Override
    public ActionReturnUtil closeRateLimitPolicy(String namespace, String ruleId, String deployName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, deployName, cluster, 5);
        if (res.isEmpty()) {
            IstioPolicyUtil.updateRuleOverview(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, CommonConstant.DATA_IS_OK, 0, userName, ruleOverviewMapper);
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CLOSE_SUCCESS);
        } else {
            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userName,
                    Integer.valueOf((res.get("faileNum") == null?"0":res.get("faileNum")).toString()), ruleOverviewMapper);
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_CLOSE_FAILED);
        }
    }

    @Override
    public ActionReturnUtil openRateLimitPolicy(String namespace, String ruleId, String deployName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
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
            if (ruleDetail.getRuleDetailOrder() == CommonConstant.RATE_LIMIT_QUOTA_ORDER) {
                quotaInstance = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), QuotaInstance.class);
            } else if (ruleDetail.getRuleDetailOrder() == CommonConstant.RATE_LIMIT_REDISQUOTA_ORDER) {
                redisQuota = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), RedisQuota.class);
            } else if (ruleDetail.getRuleDetailOrder() == CommonConstant.RATE_LIMIT_QUOTASPEC_ORDER) {
                quotaSpec = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), QuotaSpec.class);
            } else if (ruleDetail.getRuleDetailOrder() == CommonConstant.RATE_LIMIT_QUOTASPECBINDING_ORDER) {
                quotaSpecBinding = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), QuotaSpecBinding.class);
            } else if (ruleDetail.getRuleDetailOrder() == CommonConstant.RATE_LIMIT_RULE_ORDER) {
                rule = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), Rule.class);
            }
        }
        //创建Quota
        K8SClientResponse quotaResponse = rateLimitService.createRateLimitResource(namespace, quotaInstance, cluster, Resource.QUOTA);
        if (Constant.HTTP_409 == quotaResponse.getStatus()) {
            Map<String, Object> resMap = rateLimitService.deleteRateLimitPolicy(namespace, deployName, cluster, CommonConstant.RATE_LIMIT_RESOURCE_COUNT);
            if (resMap.isEmpty()) {
                quotaResponse = rateLimitService.createRateLimitResource(namespace, quotaInstance, cluster, Resource.QUOTA);
            } else {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
            }
        }
        if (!HttpStatusUtil.isSuccessStatus(quotaResponse.getStatus())) {
            LOGGER.error("create QuotaInstance error", quotaResponse.getBody());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        //创建redisQuota
        K8SClientResponse redisQuotaResponse = rateLimitService.createRateLimitResource(namespace, redisQuota, cluster, Resource.REDISQUOTA);
        if (!HttpStatusUtil.isSuccessStatus(redisQuotaResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", redisQuotaResponse.getBody());
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, deployName, cluster, CommonConstant.RATE_LIMIT_QUOTA_ORDER);
            if (!res.isEmpty()) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userName, Integer.valueOf(res.get("faileNum").toString()), ruleOverviewMapper);
            }
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        //创建QuotaSpec
        K8SClientResponse quotaSpecResponse = rateLimitService.createRateLimitResource(namespace, quotaSpec, cluster, Resource.QUOTASPEC);
        if (!HttpStatusUtil.isSuccessStatus(quotaSpecResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", quotaSpecResponse.getBody());
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, deployName, cluster, CommonConstant.RATE_LIMIT_REDISQUOTA_ORDER);
            if (!res.isEmpty()) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userName, Integer.valueOf(res.get("faileNum").toString()), ruleOverviewMapper);
            }
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        //创建QuotaSpecBinding
        K8SClientResponse quotaSpecBindingResponse = rateLimitService.createRateLimitResource(namespace, quotaSpecBinding, cluster, Resource.QUOTASPECBINDING);
        if (!HttpStatusUtil.isSuccessStatus(quotaSpecBindingResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", quotaSpecBindingResponse.getBody());
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, deployName, cluster, CommonConstant.RATE_LIMIT_QUOTASPEC_ORDER);
            if (!res.isEmpty()) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userName, Integer.valueOf(res.get("faileNum").toString()), ruleOverviewMapper);
            }
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        //创建rule
        K8SClientResponse ruleResponse = rateLimitService.createRateLimitResource(namespace, rule, cluster, Resource.RULE);
        if (!HttpStatusUtil.isSuccessStatus(ruleResponse.getStatus())) {
            LOGGER.error("create ratelimit policy error", ruleResponse.getBody());
            Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, deployName, cluster, CommonConstant.RATE_LIMIT_QUOTASPECBINDING_ORDER);
            if (!res.isEmpty()) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userName, Integer.valueOf(res.get("faileNum").toString()), ruleOverviewMapper);
            }
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        IstioPolicyUtil.updateRuleOverview(ruleId, CommonConstant.ISTIO_POLICY_OPEN, CommonConstant.DATA_IS_OK, CommonConstant.DATA_IS_OK, userName, ruleOverviewMapper);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_OPEN_SUCCESS);
    }

    @Override
    public ActionReturnUtil deleteRateLimitPolicy(String namespace, String ruleId, String deployName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        Map<String, Object> res = rateLimitService.deleteRateLimitPolicy(namespace, deployName, cluster, 5);
        if (res.isEmpty()) {
            ruleOverviewMapper.deleteByPrimaryKey(ruleId);
            ruleDetailMapper.deleteByPrimaryKey(ruleId);
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_DELETE_SUCCESS);
        } else {
            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userName, Integer.valueOf((res.get("faileNum") == null ? "0" : res.get("faileNum")).toString()), ruleOverviewMapper);
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_DELETE_FAILED);
        }
    }

    @Override
    public ActionReturnUtil getRateLimitPolicy(String namespace, String ruleId, String deployName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (CollectionUtils.isEmpty(ruleDetails) || ruleDetails.size() < 5) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        //获取数据库中对应的策略中资源的详细信息
        QuotaInstance quotaInstanceDBDetail = new QuotaInstance();
        RedisQuota redisQuotaDBDetail = new RedisQuota();
        QuotaSpec quotaSpecDBDetail = new QuotaSpec();
        QuotaSpecBinding quotaSpecBindingDBDetail = new QuotaSpecBinding();
        Rule ruleDBDetail = new Rule();
        for (RuleDetail ruleDetail : ruleDetails) {
            if (ruleDetail.getRuleDetailOrder() == CommonConstant.RATE_LIMIT_QUOTA_ORDER) {
                quotaInstanceDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), QuotaInstance.class);
            } else if (ruleDetail.getRuleDetailOrder() == CommonConstant.RATE_LIMIT_REDISQUOTA_ORDER) {
                redisQuotaDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), RedisQuota.class);
            } else if (ruleDetail.getRuleDetailOrder() == CommonConstant.RATE_LIMIT_QUOTASPEC_ORDER) {
                quotaSpecDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), QuotaSpec.class);
            } else if (ruleDetail.getRuleDetailOrder() == CommonConstant.RATE_LIMIT_QUOTASPECBINDING_ORDER) {
                quotaSpecBindingDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), QuotaSpecBinding.class);
            } else if (ruleDetail.getRuleDetailOrder() == CommonConstant.RATE_LIMIT_RULE_ORDER) {
                ruleDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), Rule.class);
            }
        }
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        Integer[] flag = new Integer[2];
        try {
            //获取集群中策略对应的crd的详细信息
            if (ruleOverview.getSwitchStatus() != CommonConstant.ISTIO_POLICY_OPEN) {
                checkClosePolicy(namespace, deployName, cluster, flag);
            } else {
                List<Object> resourceDBDetails = Arrays.asList(
                        quotaInstanceDBDetail,
                        redisQuotaDBDetail,
                        quotaSpecDBDetail,
                        quotaSpecBindingDBDetail,
                        ruleDBDetail
                );
                checkOpenPolicy(namespace, deployName, cluster, resourceDBDetails, flag);
            }
        } catch (Exception e) {
            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, flag[0], userName, flag[1], ruleOverviewMapper);
            ruleOverview.setDataStatus(flag[0]);
            ruleOverview.setDataErrLoc(flag[1]);
            LOGGER.error(e.getMessage());
        }
        //组装返回数据
        RateLimitDto rateLimitDto = new RateLimitDto();
        rateLimitDto.setRuleId(ruleId);
        rateLimitDto.setRuleName(ruleOverview.getRuleName());
        rateLimitDto.setRuleType(ruleOverview.getRuleType());
        rateLimitDto.setNamespace(ruleOverview.getRuleNs());
        rateLimitDto.setServiceName(ruleOverview.getRuleSvc());
        rateLimitDto.setScope(ruleOverview.getRuleScope());
        rateLimitDto.setSwitchStatus(ruleOverview.getSwitchStatus().toString());
        rateLimitDto.setDataStatus(ruleOverview.getDataStatus().toString());
        rateLimitDto.setCreateTime(ruleOverview.getCreateTime());
        List<RateLimitOverrideDto> overrideDtos = new ArrayList<>();
        List<QuotaOverride> overrides = redisQuotaDBDetail.getSpec().getQuotas().get(0).getOverrides();
        if (CollectionUtils.isNotEmpty(overrides)) {
            overrides.forEach(override -> {
                RateLimitOverrideDto overrideDto = new RateLimitOverrideDto();
                List<String> headers = new ArrayList<>();
                override.getDimensions().keySet().forEach(key -> {
                    if ("sourceName".equals(key)) {
                        overrideDto.setScopeServiceName(override.getDimensions().get(key));
                    } else {
                        headers.add(key + "=" + override.getDimensions().get(key));
                    }
                });
                overrideDto.setHeaders(headers);
                overrideDto.setMaxAmount(override.getMaxAmount().toString());
                overrideDtos.add(overrideDto);
            });
            rateLimitDto.setOverrides(overrideDtos);
        }
        rateLimitDto.setMaxAmount(redisQuotaDBDetail.getSpec().getQuotas().get(0).getMaxAmount().toString());
        rateLimitDto.setValidDuration(redisQuotaDBDetail.getSpec().getQuotas().get(0).getValidDuration().replaceAll(CommonConstant.SECOND, ""));
        rateLimitDto.setAlgorithm(redisQuotaDBDetail.getSpec().getQuotas().get(0).getRateLimitAlgorithm());
        if (redisQuotaDBDetail.getSpec().getQuotas().get(0).getBucketDuration() != null &&
                CommonConstant.RATE_LIMIT_ALGORITHM_ROLLING_WINDOW.equalsIgnoreCase(redisQuotaDBDetail.getSpec().getQuotas().get(0).getRateLimitAlgorithm())) {
            rateLimitDto.setBucketDuration(redisQuotaDBDetail.getSpec().getQuotas().get(0).getBucketDuration().replaceAll(CommonConstant.SECOND, ""));
        }
        return ActionReturnUtil.returnSuccessWithData(rateLimitDto);
    }

    /**
     * 开关关闭状态下校验数据
     */
    private void checkClosePolicy(String namespace, String deployName, Cluster cluster, Integer[] flag) {
        K8SClientResponse ruleResponse = rateLimitService.getRateLimitResource(namespace, deployName, CommonConstant.ISTIO_RULE, cluster);
        if (HttpStatusUtil.isSuccessStatus(ruleResponse.getStatus())) {
            LOGGER.info("get rateLimit resource(rule) success");
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.RATE_LIMIT_RULE_ORDER;
            throw new RuntimeException("rateLimit switch error(rule)");
        }
        K8SClientResponse quotaSpecBindingResponse = rateLimitService.getRateLimitResource(namespace, deployName, CommonConstant.RATE_LIMIT_QUOTA_SPEC_BINDING, cluster);
        if (HttpStatusUtil.isSuccessStatus(quotaSpecBindingResponse.getStatus())) {
            LOGGER.info("get rateLimit resource(quotaspecbinding) success");
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.RATE_LIMIT_QUOTASPECBINDING_ORDER;
            throw new RuntimeException("rateLimit switch error(quotaspecbinding)");
        }
        K8SClientResponse quotaSpecResponse = rateLimitService.getRateLimitResource(namespace, deployName, CommonConstant.RATE_LIMIT_QUOTA_SPEC, cluster);
        if (HttpStatusUtil.isSuccessStatus(quotaSpecResponse.getStatus())) {
            LOGGER.info("get rateLimit resource(quotaspec) success");
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.RATE_LIMIT_QUOTASPEC_ORDER;
            throw new RuntimeException("rateLimit switch error(quotaspec) ");
        }
        K8SClientResponse redisQuotaResponse = rateLimitService.getRateLimitResource(namespace, deployName, CommonConstant.RATE_LIMIT_REDIS_QUOTA, cluster);
        if (HttpStatusUtil.isSuccessStatus(redisQuotaResponse.getStatus())) {
            LOGGER.info("get rateLimit resource(redisquota) success");
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.RATE_LIMIT_REDISQUOTA_ORDER;
            throw new RuntimeException("rateLimit switch error(redisquota)");
        }
        K8SClientResponse quotaInstanceResponse = rateLimitService.getRateLimitResource(namespace, deployName, CommonConstant.RATE_LIMIT_QUOTA, cluster);
        if (HttpStatusUtil.isSuccessStatus(quotaInstanceResponse.getStatus())) {
            LOGGER.info("get rateLimit resource(quota) success");
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.RATE_LIMIT_QUOTA_ORDER;
            throw new RuntimeException("rateLimit switch error(quota)");
        }
    }

    /**
     * 开关开启状态下校验数据
     */
    private void checkOpenPolicy(String namespace, String deployName, Cluster cluster, List<Object> resourceDBDetail, Integer[] flag) {
        //Ruel
        K8SClientResponse ruleResponse = rateLimitService.getRateLimitResource(namespace, deployName, CommonConstant.ISTIO_RULE, cluster);
        if (!HttpStatusUtil.isSuccessStatus(ruleResponse.getStatus())) {
            LOGGER.info("get rateLimit resource(rule) error ", ruleResponse.getBody());
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.RATE_LIMIT_RULE_ORDER;
            throw new RuntimeException("rateLimit switch error(rule) " + ruleResponse.getBody());
        }
        Rule ruleK8sDetail = JsonUtil.jsonToPojo(ruleResponse.getBody(), Rule.class);
        Rule ruleDBDetail = (Rule) resourceDBDetail.get(4);
        if (!ruleDBDetail.equals(ruleK8sDetail)) {
            flag[0] = CommonConstant.DATA_NOT_SAME;
            flag[1] = CommonConstant.RATE_LIMIT_RULE_ORDER;
            throw new RuntimeException("rateLimit rule not same");
        }
        //QuotaSpecBinding
        K8SClientResponse quotaSpecBindingResponse = rateLimitService.getRateLimitResource(namespace, deployName, CommonConstant.RATE_LIMIT_QUOTA_SPEC_BINDING, cluster);
        if (!HttpStatusUtil.isSuccessStatus(quotaSpecBindingResponse.getStatus())) {
            LOGGER.info("get rateLimit resource(quotaspecbinding) error ", quotaSpecBindingResponse.getBody());
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.RATE_LIMIT_QUOTASPECBINDING_ORDER;
            throw new RuntimeException("rateLimit switch error(quotaspecbinding) " + quotaSpecBindingResponse.getBody());
        }
        QuotaSpecBinding quotaSpecBindingK8sDetail = JsonUtil.jsonToPojo(quotaSpecBindingResponse.getBody(), QuotaSpecBinding.class);
        QuotaSpecBinding quotaSpecBindingDBDetail = (QuotaSpecBinding) resourceDBDetail.get(3);
        if (!quotaSpecBindingDBDetail.equals(quotaSpecBindingK8sDetail)) {
            flag[0] = CommonConstant.DATA_NOT_SAME;
            flag[1] = CommonConstant.RATE_LIMIT_QUOTASPECBINDING_ORDER;
            throw new RuntimeException("rateLimit quotaspecbinding not same");
        }
        //QuotaSpec
        K8SClientResponse quotaSpecResponse = rateLimitService.getRateLimitResource(namespace, deployName, CommonConstant.RATE_LIMIT_QUOTA_SPEC, cluster);
        if (!HttpStatusUtil.isSuccessStatus(quotaSpecResponse.getStatus())) {
            LOGGER.info("get rateLimit resource(quotaspec) error ", quotaSpecResponse.getBody());
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.RATE_LIMIT_QUOTASPEC_ORDER;
            throw new RuntimeException("rateLimit switch error(quotaspec) " + quotaSpecResponse.getBody());
        }
        QuotaSpec quotaSpecK8sDetail = JsonUtil.jsonToPojo(quotaSpecResponse.getBody(), QuotaSpec.class);
        QuotaSpec quotaSpecDBDetail = (QuotaSpec) resourceDBDetail.get(2);
        if (!quotaSpecDBDetail.equals(quotaSpecK8sDetail)) {
            flag[0] = CommonConstant.DATA_NOT_SAME;
            flag[1] = CommonConstant.RATE_LIMIT_QUOTASPEC_ORDER;
            throw new RuntimeException("rateLimit quotaspec not same");
        }
        //RedisQuota
        K8SClientResponse redisQuotaResponse = rateLimitService.getRateLimitResource(namespace, deployName, CommonConstant.RATE_LIMIT_REDIS_QUOTA, cluster);
        if (!HttpStatusUtil.isSuccessStatus(redisQuotaResponse.getStatus())) {
            LOGGER.info("get rateLimit resource(redisquota) error ", redisQuotaResponse.getBody());
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.RATE_LIMIT_REDISQUOTA_ORDER;
            throw new RuntimeException("rateLimit switch error(redisquota) " + redisQuotaResponse.getBody());
        }
        RedisQuota redisQuotaK8sDetail = JsonUtil.jsonToPojo(redisQuotaResponse.getBody(), RedisQuota.class);
        RedisQuota redisQuotaDBDetail = (RedisQuota) resourceDBDetail.get(1);
        if (!redisQuotaDBDetail.equals(redisQuotaK8sDetail)) {
            flag[0] = CommonConstant.DATA_NOT_SAME;
            flag[1] = CommonConstant.RATE_LIMIT_REDISQUOTA_ORDER;
            throw new RuntimeException("rateLimit redisquota not same");
        }
        //Quota
        K8SClientResponse quotaInstanceResponse = rateLimitService.getRateLimitResource(namespace, deployName, CommonConstant.RATE_LIMIT_QUOTA, cluster);
        if (!HttpStatusUtil.isSuccessStatus(quotaInstanceResponse.getStatus())) {
            LOGGER.info("get rateLimit resource(quota) error ", quotaInstanceResponse.getBody());
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.RATE_LIMIT_QUOTA_ORDER;
            throw new RuntimeException("rateLimit switch error(quota) " + quotaInstanceResponse.getBody());
        }
        QuotaInstance quotaInstanceK8sDetail = JsonUtil.jsonToPojo(quotaInstanceResponse.getBody(), QuotaInstance.class);
        QuotaInstance quotaInstanceDBDetail = (QuotaInstance) resourceDBDetail.get(0);
        if (!quotaInstanceDBDetail.equals(quotaInstanceK8sDetail)) {
            flag[0] = CommonConstant.DATA_NOT_SAME;
            flag[1] = CommonConstant.RATE_LIMIT_QUOTA_ORDER;
            throw new RuntimeException("rateLimit quota not same");
        }
    }
}
