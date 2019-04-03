package com.harmonycloud.service.istio.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.istio.RuleDetailMapper;
import com.harmonycloud.dao.istio.RuleOverviewMapper;
import com.harmonycloud.dao.istio.bean.RuleDetail;
import com.harmonycloud.dao.istio.bean.RuleOverview;
import com.harmonycloud.dto.application.istio.WhiteListsDto;
import com.harmonycloud.dto.application.istio.WhiteServiceDto;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.policies.Rule;
import com.harmonycloud.k8s.bean.istio.policies.whitelists.ListChecker;
import com.harmonycloud.k8s.bean.istio.policies.whitelists.ListEntry;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.service.istio.WhiteListsService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.istio.IstioWhiteListsService;
import com.harmonycloud.service.istio.util.IstioPolicyUtil;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.*;

import static com.harmonycloud.service.istio.util.IstioPolicyUtil.DEFAULT_OVERRIDE;

/**
 * create by weg on 18-12-27.
 */
@Service
public class IstioWhiteListsServiceImpl implements IstioWhiteListsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IstioWhiteListsServiceImpl.class);

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private WhiteListsService whiteListsService;

    @Autowired
    private RuleOverviewMapper ruleOverviewMapper;

    @Autowired
    private RuleDetailMapper ruleDetailMapper;

    @Autowired
    private HttpSession session;

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
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        //判断数据库是否存在策略信息
        if (IstioPolicyUtil.checkPolicyExist(cluster.getId(), namespace, serviceName, whiteListsDto.getRuleName(),whiteListsDto.getRuleType(), ruleOverviewMapper)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_EXIST);
        }
        String ruleId = UUIDUtil.getUUID();
        IstioPolicyUtil.insertRuleOverview(whiteListsDto, ruleId, cluster.getId(), userName, ruleOverviewMapper);
        RuleDetail listCheckerRuleDetail = IstioPolicyUtil.makeListCheckerRuleDetail(whiteListsDto, ruleId);
        listCheckerRuleDetail.setCreateTime(new Date());
        ruleDetailMapper.insert(listCheckerRuleDetail);
        RuleDetail listEntryRuleDetail = IstioPolicyUtil.makeListEntryDetail(whiteListsDto, ruleId);
        listEntryRuleDetail.setCreateTime(new Date());
        ruleDetailMapper.insert(listEntryRuleDetail);
        RuleDetail ruleRuleDetail = IstioPolicyUtil.makeRuleRuleDetail(whiteListsDto, ruleId);
        ruleRuleDetail.setCreateTime(new Date());
        ruleDetailMapper.insert(ruleRuleDetail);
        //build  listchecker
        ListChecker listChecker = JsonUtil.jsonToPojo(new String(listCheckerRuleDetail.getRuleDetailContent()), ListChecker.class);
        K8SClientResponse listCheckerResponse = whiteListsService.createListChecker(namespace, listChecker, cluster);
        if (!HttpStatusUtil.isSuccessStatus(listCheckerResponse.getStatus())) {
            //关闭开关
            IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
            LOGGER.error("create whiteLists listChecker error", listCheckerResponse.getBody());
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_FAILED);
        }
        //build  listEntry
        ListEntry listEntry = JsonUtil.jsonToPojo(new String(listEntryRuleDetail.getRuleDetailContent()), ListEntry.class);
        K8SClientResponse listEntryResponse = whiteListsService.createListEntry(namespace, listEntry, cluster);
        if (!HttpStatusUtil.isSuccessStatus(listEntryResponse.getStatus())) {
            Map<String, Object> res = whiteListsService.deleteWhiteListsPolicy(namespace, whiteListsDto.getServiceName(), cluster, CommonConstant.WHITE_LISTS_LISTCHECKER_ORDER);
            if (res.isEmpty()) {
                IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
            } else {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userName, Integer.valueOf(res.get("faileNum").toString()), ruleOverviewMapper);
            }
            LOGGER.error("create whiteLists listEntry error", listEntryResponse.getBody());
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CREATE_FAILED);
        }
        //build  rule
        Rule rule = JsonUtil.jsonToPojo(new String(ruleRuleDetail.getRuleDetailContent()), Rule.class);
        K8SClientResponse ruleResponse = whiteListsService.createWhiteRule(namespace, rule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(ruleResponse.getStatus())) {
            Map<String, Object> res = whiteListsService.deleteWhiteListsPolicy(namespace, whiteListsDto.getServiceName(), cluster, CommonConstant.WHITE_LISTS_LISTENTRY_ORDER);
            if (res.isEmpty()) {
                IstioPolicyUtil.updateRuleOverviewSwitchStatus(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, userName, ruleOverviewMapper);
            } else {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userName, Integer.valueOf(res.get("faileNum").toString()), ruleOverviewMapper);
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
        String deployName = whiteListsDto.getServiceName();
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        RuleOverview ruleOverview = ruleOverviewMapper.selectByRuleId(ruleId);
        if (Objects.isNull(ruleOverview)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        //更新数据库中策略信息
        RuleDetail listCheckerRuleDetail = IstioPolicyUtil.makeListCheckerRuleDetail(whiteListsDto, ruleId);
        listCheckerRuleDetail.setUpdateTime(new Date());
        ruleDetailMapper.updateByPrimaryKeySelective(listCheckerRuleDetail);
        //开启状态下更新k8s信息
        if (IstioPolicyUtil.checkPolicyStatus(ruleId, ruleOverviewMapper)) {
            K8SClientResponse response = whiteListsService.getListChecker(namespace, deployName, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.K8S_NO_DATA, userName, listCheckerRuleDetail.getRuleDetailOrder(), ruleOverviewMapper);
                LOGGER.error("get listChecker error", response.getBody());
                return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
            }
            ListChecker listChecker = JsonUtil.jsonToPojo(response.getBody(), ListChecker.class);
            List<String> overrides = new ArrayList<>();
            overrides.add(DEFAULT_OVERRIDE);
            if (CollectionUtils.isNotEmpty(whiteListsDto.getWhiteNameList())) {
                for (WhiteServiceDto whiteService : whiteListsDto.getWhiteNameList()) {
                    overrides.add(whiteService.getNamespace() + "_" + whiteService.getName());
                }
            }
            listChecker.getSpec().setOverrides(overrides);
            K8SClientResponse updateResponse = whiteListsService.updateListChecker(namespace, deployName, listChecker, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userName, listCheckerRuleDetail.getRuleDetailOrder(), ruleOverviewMapper);
                UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                LOGGER.error(status.getMessage());
                return ActionReturnUtil.returnSuccessWithData(status.getMessage());
            }
        }
        IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_IS_OK, userName, CommonConstant.DATA_IS_OK, ruleOverviewMapper);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_UPDATE_SUCCESS);
    }

    @Override
    public ActionReturnUtil closeWhiteListsPolicy(String namespace, String ruleId, String deployName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        Map<String, Object> res = whiteListsService.deleteWhiteListsPolicy(namespace, deployName, cluster, 3);
        if (res.isEmpty()) {
            IstioPolicyUtil.updateRuleOverview(ruleId, CommonConstant.ISTIO_POLICY_CLOSE, CommonConstant.DATA_IS_OK, 0, userName, ruleOverviewMapper);
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_CLOSE_SUCCESS);
        } else {
            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userName,
                    Integer.valueOf((res.get("faileNum") == null ? "0" : res.get("faileNum")).toString()), ruleOverviewMapper);
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_CLOSE_FAILED);
        }
    }

    @Override
    public ActionReturnUtil openWhiteListsPolicy(String namespace, String ruleId, String deployName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (CollectionUtils.isEmpty(ruleDetails) || ruleDetails.size() < CommonConstant.WHITE_LISTS_RESOURCE_COUNT) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        ListChecker listChecker = new ListChecker();
        ListEntry   listEntry  = new ListEntry();
        Rule rule = new Rule();
        for (RuleDetail ruleDetail : ruleDetails) {
            if (ruleDetail.getRuleDetailOrder() == CommonConstant.WHITE_LISTS_LISTCHECKER_ORDER) {
                listChecker = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), ListChecker.class);
            } else if (ruleDetail.getRuleDetailOrder() == CommonConstant.WHITE_LISTS_LISTENTRY_ORDER) {
                listEntry = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), ListEntry.class);
            } else if (ruleDetail.getRuleDetailOrder() == CommonConstant.WHITE_LISTS_RULE_ORDER) {
                rule = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), Rule.class);
            }
        }
        //创建listchecker
        K8SClientResponse listCheckerResponse = whiteListsService.createListChecker(namespace, listChecker, cluster);
        if (Constant.HTTP_409 == listCheckerResponse.getStatus()) {
            Map<String, Object> resMap = whiteListsService.deleteWhiteListsPolicy(namespace, deployName, cluster, CommonConstant.WHITE_LISTS_RESOURCE_COUNT);
            if (resMap.isEmpty()) {
                listCheckerResponse = whiteListsService.createListChecker(namespace, listChecker, cluster);
            } else {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
            }
        }
        if (!HttpStatusUtil.isSuccessStatus(listCheckerResponse.getStatus())) {
            LOGGER.error("create listChecker error", listCheckerResponse.getBody());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        //创建listEntry
        K8SClientResponse listEntryResponse = whiteListsService.createListEntry(namespace, listEntry, cluster);
        if (!HttpStatusUtil.isSuccessStatus(listEntryResponse.getStatus())) {
            LOGGER.error("create listEntry policy error", listEntryResponse.getBody());
            Map<String, Object> res = whiteListsService.deleteWhiteListsPolicy(namespace, deployName, cluster, CommonConstant.WHITE_LISTS_LISTCHECKER_ORDER);
            if (!res.isEmpty()) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userName, Integer.valueOf(res.get("faileNum").toString()), ruleOverviewMapper);
            }
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }

        //创建rule
        K8SClientResponse ruleResponse = whiteListsService.createWhiteRule(namespace, rule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(ruleResponse.getStatus())) {
            LOGGER.error("create whitelists policy error", ruleResponse.getBody());
            Map<String, Object> res = whiteListsService.deleteWhiteListsPolicy(namespace, deployName, cluster, CommonConstant.WHITE_LISTS_LISTENTRY_ORDER);
            if (!res.isEmpty()) {
                IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_COMPLETE, userName, Integer.valueOf(res.get("faileNum").toString()), ruleOverviewMapper);
            }
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_OPEN_FAILED);
        }
        IstioPolicyUtil.updateRuleOverview(ruleId, CommonConstant.ISTIO_POLICY_OPEN, CommonConstant.DATA_IS_OK, 0, userName, ruleOverviewMapper);
        return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_OPEN_SUCCESS);
    }

    @Override
    public ActionReturnUtil deleteWhiteListsPolicy(String namespace, String ruleId, String deployName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        Map<String, Object> res = whiteListsService.deleteWhiteListsPolicy(namespace, deployName, cluster, CommonConstant.WHITE_LISTS_RESOURCE_COUNT);
        if (res.isEmpty()) {
            ruleOverviewMapper.deleteByPrimaryKey(ruleId);
            ruleDetailMapper.deleteByPrimaryKey(ruleId);
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_DELETE_SUCCESS);
        } else {
            IstioPolicyUtil.updateRuleOverviewDataStatus(ruleId, CommonConstant.DATA_NOT_SAME, userName,
                    Integer.valueOf((res.get("faileNum") == null ? "0" : res.get("faileNum")).toString()), ruleOverviewMapper);
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_DELETE_FAILED);
        }
    }

    @Override
    public ActionReturnUtil getWhiteListsPolicy(String namespace, String ruleId, String deployName) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        List<RuleDetail> ruleDetails = ruleDetailMapper.selectByPrimaryKey(ruleId);
        if (CollectionUtils.isEmpty(ruleDetails) || ruleDetails.size() < 3) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        ListChecker listCheckerDBDetail = new ListChecker();
        ListEntry listEntryDBDetail = new ListEntry();
        Rule ruleDBDetail = new Rule();
        for (RuleDetail ruleDetail : ruleDetails) {
            if (ruleDetail.getRuleDetailOrder() == CommonConstant.WHITE_LISTS_LISTCHECKER_ORDER) {
                listCheckerDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), ListChecker.class);
            } else if (ruleDetail.getRuleDetailOrder() == CommonConstant.WHITE_LISTS_LISTENTRY_ORDER) {
                listEntryDBDetail = JsonUtil.jsonToPojo(new String(ruleDetail.getRuleDetailContent()), ListEntry.class);
            } else if (ruleDetail.getRuleDetailOrder() == CommonConstant.WHITE_LISTS_RULE_ORDER) {
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
                        listCheckerDBDetail,
                        listEntryDBDetail,
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
        WhiteListsDto whiteListsDto  = new WhiteListsDto();
        whiteListsDto.setRuleId(ruleId);
        whiteListsDto.setRuleName(ruleOverview.getRuleName());
        whiteListsDto.setRuleType(ruleOverview.getRuleType());
        whiteListsDto.setDataStatus(ruleOverview.getDataStatus().toString());
        whiteListsDto.setSwitchStatus(ruleOverview.getSwitchStatus().toString());
        whiteListsDto.setNamespace(ruleOverview.getRuleNs());
        whiteListsDto.setServiceName(ruleOverview.getRuleSvc());
        //构建服务列表
        List<String> overrides = listCheckerDBDetail.getSpec().getOverrides();
        List<WhiteServiceDto> whiteMapList = new ArrayList<>();
        for (String override : overrides) {
            if (override.equalsIgnoreCase(DEFAULT_OVERRIDE)) {
                continue;
            }
            WhiteServiceDto whiteService = new WhiteServiceDto();
            String[] nsList = override.split("_");
            if (nsList.length != 2) {
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

    /**
     * 开关关闭状态下校验数据
     */
    private void checkClosePolicy(String namespace, String deployName, Cluster cluster, Integer[] flag) {
        K8SClientResponse ruleResponse = whiteListsService.getRule(namespace, CommonConstant.WHITE_LISTS_PREFIX + deployName, cluster);
        if (HttpStatusUtil.isSuccessStatus(ruleResponse.getStatus())) {
            LOGGER.info("get whitelists resource(rule) success");
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.WHITE_LISTS_RULE_ORDER;
            throw new RuntimeException("whitelists switch error(rule)");
        }
        K8SClientResponse listEntryResponse = whiteListsService.getListEntry(namespace, deployName, cluster);
        if (HttpStatusUtil.isSuccessStatus(listEntryResponse.getStatus())) {
            LOGGER.info("get whitelists resource(listentry) success");
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.WHITE_LISTS_LISTENTRY_ORDER;
            throw new RuntimeException("whitelists switch error(listentry)");
        }
        K8SClientResponse listCheckerResponse = whiteListsService.getListChecker(namespace, deployName, cluster);
        if (HttpStatusUtil.isSuccessStatus(listCheckerResponse.getStatus())) {
            LOGGER.info("get whitelists resource(listchecker) success");
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.WHITE_LISTS_LISTCHECKER_ORDER;
            throw new RuntimeException("whitelists switch error(listchecker) ");
        }
    }

    /**
     * 开关开启状态下校验数据
     */
    private void checkOpenPolicy(String namespace, String deployName, Cluster cluster, List<Object> resourceDBDetail, Integer[] flag) {
        //Rule
        K8SClientResponse ruleResponse = whiteListsService.getRule(namespace, CommonConstant.WHITE_LISTS_PREFIX + deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(ruleResponse.getStatus())) {
            LOGGER.info("get whitelists resource(rule) error ", ruleResponse.getBody());
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.WHITE_LISTS_RULE_ORDER;
            throw new RuntimeException("whitelists switch error(rule) " + ruleResponse.getBody());
        }
        Rule ruleK8sDetail = JsonUtil.jsonToPojo(ruleResponse.getBody(), Rule.class);
        Rule ruleDBDetail = (Rule) resourceDBDetail.get(2);
        if (!ruleDBDetail.equals(ruleK8sDetail)) {
            flag[0] = CommonConstant.DATA_NOT_SAME;
            flag[1] = CommonConstant.WHITE_LISTS_RULE_ORDER;
            throw new RuntimeException("whitelists rule not same");
        }
        //ListEntry
        K8SClientResponse listEntryResponse = whiteListsService.getListEntry(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(listEntryResponse.getStatus())) {
            LOGGER.info("get whitelists resource(listentry) error ", listEntryResponse.getBody());
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.WHITE_LISTS_LISTENTRY_ORDER;
            throw new RuntimeException("get whitelists resource(listentry) " + listEntryResponse.getBody());
        }
        ListEntry listEntryK8sDetail  = JsonUtil.jsonToPojo(listEntryResponse.getBody(), ListEntry.class);
        ListEntry listEntryDBDetail = (ListEntry) resourceDBDetail.get(1);
        if (!listEntryDBDetail.equals(listEntryK8sDetail)) {
            flag[0] = CommonConstant.DATA_NOT_SAME;
            flag[1] = CommonConstant.WHITE_LISTS_LISTENTRY_ORDER;
            throw new RuntimeException("whitelists listentry not same");
        }
        //ListChecker
        K8SClientResponse listCheckerResponse = whiteListsService.getListChecker(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(listCheckerResponse.getStatus())) {
            LOGGER.info("get whitelists resource(listchecker) error ", listCheckerResponse.getBody());
            flag[0] = CommonConstant.SWITCH_STATUS_ERROR;
            flag[1] = CommonConstant.WHITE_LISTS_LISTCHECKER_ORDER;
            throw new RuntimeException("get whitelists resource(listchecker) " + listCheckerResponse.getBody());
        }
        ListChecker listCheckerK8sDetail = JsonUtil.jsonToPojo(listCheckerResponse.getBody(), ListChecker.class);
        ListChecker listCheckerDBDetail = (ListChecker) resourceDBDetail.get(0);
        if (!listCheckerDBDetail.equals(listCheckerK8sDetail)) {
            flag[0] = CommonConstant.DATA_NOT_SAME;
            flag[1] = CommonConstant.WHITE_LISTS_LISTCHECKER_ORDER;
            throw new RuntimeException("whitelists listchecker not same");
        }
    }
}
