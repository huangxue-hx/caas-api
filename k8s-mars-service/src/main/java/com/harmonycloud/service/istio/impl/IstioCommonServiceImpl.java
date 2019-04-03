package com.harmonycloud.service.istio.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.istio.IstioGlobalConfigureMapper;
import com.harmonycloud.dao.istio.RuleDetailMapper;
import com.harmonycloud.dao.istio.RuleOverviewMapper;
import com.harmonycloud.dao.istio.bean.IstioGlobalConfigure;
import com.harmonycloud.dao.istio.bean.RuleOverview;
import com.harmonycloud.dto.cluster.IstioClusterDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.DestinationRule;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.Subset;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.ReplicasetsService;
import com.harmonycloud.k8s.service.istio.DestinationRuleService;
import com.harmonycloud.k8s.service.istio.IstioPolicyService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.application.VersionControlService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.istio.IstioCommonService;
import com.harmonycloud.service.istio.util.IstioPolicyUtil;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.user.RoleLocalService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * create by weg on 18-12-27.
 */
@Service
public class IstioCommonServiceImpl implements IstioCommonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IstioCommonServiceImpl.class);

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private DestinationRuleService destinationRuleService;

    @Autowired
    private com.harmonycloud.k8s.service.NamespaceService ns;

    @Autowired
    private IstioGlobalConfigureMapper istioGlobalConfigureMapper;

    @Autowired
    private RuleOverviewMapper ruleOverviewMapper;

    @Autowired
    private RuleDetailMapper ruleDetailMapper;

    @Autowired
    private HttpSession session;

    @Autowired
    private VersionControlService versionControlService;

    @Autowired
    private RoleLocalService roleLocalService;

    @Autowired
    private IstioPolicyService istioPolicyService;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private ReplicasetsService rsService;

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
        String userName = session.getAttribute(CommonConstant.USERNAME).toString();
        //判断status是开启还是关闭
        if (status) {  //true为开启istio全局配置的操作
            //检测集群里是否安装istio服务
            K8SClientResponse response = ns.getNamespace(CommonConstant.ISTIO_NAMESPACE, null, null, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                LOGGER.error("get istio-system namespace error", response.getBody());
                throw new MarsRuntimeException(ErrorCodeMessage.ISTIO_SERVICE_GET_FAILED);
            }
            //查询istio全局开关配置表中是否有该集群的信息
            IstioGlobalConfigure istioGlobalConfigure = istioGlobalConfigureMapper.getByClusterId(clusterId);
            if (Objects.nonNull(istioGlobalConfigure)) {
                //将数据库的全局开关更新为开启
                IstioPolicyUtil.updateGlobalStatus(clusterId, CommonConstant.OPEN_GLOBAL_STATUS, userName, istioGlobalConfigureMapper);
            } else {
                //将该集群信息插入到mysql的全局配置表里
                IstioPolicyUtil.insertGlobalInfo(cluster, CommonConstant.OPEN_GLOBAL_STATUS, userName, istioGlobalConfigureMapper);
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
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.EXISTS_ISTIO_AUTOMATIC);
            }
            //将数据库的全局开关更新为关闭
            IstioPolicyUtil.updateGlobalStatus(clusterId, CommonConstant.CLOSE_GLOBAL_STATUS, userName, istioGlobalConfigureMapper);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil getNamespaceIstioPolicySwitch(String namespace, String clusterId) throws Exception {
        //获取集群信息
        Cluster  cluster = new Cluster();
        if(StringUtils.isNotBlank(clusterId)) {
            cluster = clusterService.findClusterById(clusterId);
        } else {
            cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
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
    public boolean isIstioEnabled(String namespace) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);

        K8SClientResponse response = ns.getNamespace(namespace, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("get  namespace error", response.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_NOT_FOUND);
        }
        Namespace namespaceDetail = JsonUtil.jsonToPojo(response.getBody(), Namespace.class);
        String istioInjectionValue = "";
        Map<String, Object> labels = namespaceDetail.getMetadata().getLabels();
        if (Objects.nonNull(labels) && Objects.nonNull(labels.get(CommonConstant.ISTIO_INJECTION))) {
            istioInjectionValue = labels.get(CommonConstant.ISTIO_INJECTION).toString();
        }
        return CommonConstant.OPEN_ISTIO_AUTOMATIC_INJECTION.equals(istioInjectionValue);
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
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.ISTIO_GLOBAL_CLOSE);
        }
        boolean isDeployment = IstioPolicyUtil.getNamespaceIstioStatus(namespaceName, cluster, deploymentService);//查询是否有服务 false代表无服务 true代表有服务
        if (isDeployment) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.EXISTS_SERVICE);
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

    @Override
    public ActionReturnUtil getDesServiceVersion(String deployName, String namespace) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);

        K8SClientResponse response = destinationRuleService.getDestinationRule(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        DestinationRule destinationRule = JsonUtil.jsonToPojo(response.getBody(), DestinationRule.class);
        List<Subset> subsets = destinationRule.getSpec().getSubsets();
        List<String> versions = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(subsets)) {
            subsets.forEach(subset -> versions.add(subset.getName()));
        }
        return ActionReturnUtil.returnSuccessWithData(versions.stream().distinct().collect(Collectors.toList()));
    }

    @Override
    public ActionReturnUtil getSourceServiceVersion(String deployName, String namespace, String serviceType) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        Map<String, Object> body = new HashMap<>();
        body.put(CommonConstant.LABELSELECTOR, "app=" + deployName);
        K8SClientResponse rsRes = rsService.doRsByNamespace(namespace, headers, body, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(rsRes.getBody());
        }
        ReplicaSetList rSetList = JsonUtil.jsonToPojo(rsRes.getBody(), ReplicaSetList.class);
        List<String> versions = new ArrayList<>();
        for (ReplicaSet rs : rSetList.getItems()) {
            Integer replicas =  rs.getSpec().getReplicas();
            if (Objects.nonNull(replicas) && replicas >= 1) {
                versions.add(rs.getSpec().getTemplate().getMetadata().getLabels().get(com.harmonycloud.service.platform.constant.Constant.TYPE_DEPLOY_VERSION).toString());
            }
        }
        return ActionReturnUtil.returnSuccessWithData(versions.stream().distinct().collect(Collectors.toList()));
    }

    @Override
    public ActionReturnUtil listIstioCluster() throws Exception {
        List<Cluster> clusters = roleLocalService.listCurrentUserRoleCluster();
        List<IstioClusterDto> istioOepnClusterList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(clusters)) {
            for (Cluster clusterItem : clusters) {
                IstioGlobalConfigure istioGlobalConfigure = istioGlobalConfigureMapper.getByClusterId(clusterItem.getId());
                if (Objects.nonNull(istioGlobalConfigure) && istioGlobalConfigure.getSwitchStatus() == CommonConstant.OPEN_GLOBAL_STATUS) {
                    IstioClusterDto clusterDto = new IstioClusterDto();
                    clusterDto.setClusterId(clusterItem.getId());
                    clusterDto.setClusterName(clusterItem.getAliasName());
                    clusterDto.setDataCenterName(clusterItem.getDataCenterName());
                    clusterDto.setDataCenter(clusterItem.getDataCenter());
                    istioOepnClusterList.add(clusterDto);
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(istioOepnClusterList);
    }

    @Override
    public ActionReturnUtil deleteIstioPolicy(String namespace, String serviceName, String clusterId) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        //查询集群信息
        Cluster cluster = clusterService.findClusterById(clusterId);
        Map<Object, Object> ruleInfoMap = new HashMap<>();
        ruleInfoMap.put("ruleNs", namespace);
        if (StringUtils.isNotBlank(serviceName)) {
            ruleInfoMap.put("ruleSvc", serviceName);
            //删除k8s里的资源
            List<String> resources = Arrays.asList(
                    Resource.DESTINATIONRULES,
                    Resource.VIRTUALSERVICE,
                    Resource.QUOTA,
                    Resource.REDISQUOTA,
                    Resource.QUOTASPEC,
                    Resource.QUOTASPECBINDING,
                    Resource.LISTCHECKER,
                    Resource.LISTENTRY,
                    Resource.RULE
            );
            Map<String, Object> bodys = new HashMap<>();
            bodys.put(CommonConstant.LABELSELECTOR, "app=" + serviceName);
            resources.forEach(resource -> istioPolicyService.deleteIstioPolicyResource(namespace, bodys, resource, cluster));
        }
        //根据namespace 和 service查询策略列表
        List<RuleOverview> ruleOverviewList = ruleOverviewMapper.selectByRuleInfo(ruleInfoMap);
        if (CollectionUtils.isNotEmpty(ruleOverviewList)) {
            for (RuleOverview ruleOverviewItem : ruleOverviewList) {
                ruleDetailMapper.deleteByPrimaryKey(ruleOverviewItem.getRuleId());//删除detail里面相关策略
            }
        }
        //删除概览表的策略信息
        ruleOverviewMapper.deleteIstioPolicy(ruleInfoMap);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil createDestinationRule(String deployName, String namespace, String version) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        //创建时默认为v1
        DestinationRule destinationRule = IstioPolicyUtil.makeDestinationRule(deployName, namespace, Collections.singletonList(version));
        K8SClientResponse response = destinationRuleService.createDestinationRule(namespace, destinationRule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("create DestinationRule error", response.getBody());
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil updateDestinationRule(String deployName, String namespace, String version, boolean isBlueGreen) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);

        boolean isPause = false;
        if (!isBlueGreen) {
            ActionReturnUtil updateStatus = versionControlService.getUpdateStatus(namespace, deployName, "");
            isPause = (boolean) ((Map) updateStatus.getData()).get("pause");
        }

        K8SClientResponse response = destinationRuleService.getDestinationRule(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
            LOGGER.error("get DestinationRule fail", response.getBody());
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        DestinationRule destinationRule = JsonUtil.jsonToPojo(response.getBody(), DestinationRule.class);
        if (Objects.nonNull(destinationRule) &&
                Objects.nonNull(destinationRule.getSpec()) &&
                CollectionUtils.isNotEmpty(destinationRule.getSpec().getSubsets())) {
            Subset subset = new Subset();
            subset.setName(version);
            Map<String, String> subsetLabels = new HashMap<>();
            subsetLabels.put("version", version);
            subset.setLabels(subsetLabels);
            if (isPause) {
                destinationRule.getSpec().getSubsets().add(subset);
            } else {
                destinationRule.getSpec().setSubsets(Collections.singletonList(subset));
            }
            //更新DestinationRule
            K8SClientResponse updateResponse = destinationRuleService.updateDestinationRule(namespace, deployName, destinationRule, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
                LOGGER.error("update DestinationRule fail", response.getBody());
                UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
                throw new MarsRuntimeException(status.getMessage());
            }
        } else {
            Map<String, Object> headers = new HashMap<>();
            headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
            Map<String, Object> body = new HashMap<>();
            body.put(CommonConstant.LABELSELECTOR, "app=" + deployName);
            K8SClientResponse rsRes = rsService.doRsByNamespace(namespace, headers, body, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
                return ActionReturnUtil.returnErrorWithData(rsRes.getBody());
            }
            ReplicaSetList rSetList = JsonUtil.jsonToPojo(rsRes.getBody(), ReplicaSetList.class);
            List<String> versions = new ArrayList<>();
            for (ReplicaSet rs : rSetList.getItems()) {
                Integer replicas = rs.getSpec().getReplicas();
                if (Objects.nonNull(replicas) && replicas >= 1) {
                    versions.add(rs.getSpec().getTemplate().getMetadata().getLabels().get(com.harmonycloud.service.platform.constant.Constant.TYPE_DEPLOY_VERSION).toString());
                }
            }
            destinationRule = IstioPolicyUtil.makeDestinationRule(deployName, namespace, Collections.singletonList(version));
            K8SClientResponse responseCreateDR = destinationRuleService.createDestinationRule(namespace, destinationRule, cluster);
            if (!HttpStatusUtil.isSuccessStatus(responseCreateDR.getStatus())) {
                LOGGER.error("create DestinationRule error", responseCreateDR.getBody());
                UnversionedStatus status = JsonUtil.jsonToPojo(responseCreateDR.getBody(), UnversionedStatus.class);
                throw new MarsRuntimeException(status.getMessage());
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil deleteDestinationRule(String deployName, String namespace) throws Exception {
        AssertUtil.notNull(namespace, DictEnum.NAMESPACE);
        AssertUtil.notNull(deployName, DictEnum.NAME);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        K8SClientResponse response = destinationRuleService.deleteDestinationRule(namespace, deployName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
            LOGGER.error("delete DestinationRule error", response.getBody());
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 获取全局状态开关状态
     */
    @Override
    public boolean getIstioGlobalStatus(String clusterId) throws Exception {
        //获取该集群的开关配置信息
        IstioGlobalConfigure istioGlobalConfigure = istioGlobalConfigureMapper.getByClusterId(clusterId);
        return Objects.nonNull(istioGlobalConfigure) &&
                istioGlobalConfigure.getSwitchStatus() == CommonConstant.OPEN_GLOBAL_STATUS;
    }
}
