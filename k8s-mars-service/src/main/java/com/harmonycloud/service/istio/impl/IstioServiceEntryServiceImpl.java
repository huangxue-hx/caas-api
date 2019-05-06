package com.harmonycloud.service.istio.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.istio.IstioGlobalConfigureMapper;
import com.harmonycloud.dao.istio.RuleOverviewMapper;
import com.harmonycloud.dao.istio.bean.IstioGlobalConfigure;
import com.harmonycloud.dao.istio.bean.RuleOverview;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dto.application.istio.ServiceEntryDto;
import com.harmonycloud.dto.application.istio.ServiceEntryPortDto;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.policies.Endpoint;
import com.harmonycloud.k8s.bean.istio.policies.Port;
import com.harmonycloud.k8s.bean.istio.policies.ServiceEntry;
import com.harmonycloud.k8s.bean.istio.policies.ServiceEntryList;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.DestinationRule;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.service.ServicesService;
import com.harmonycloud.k8s.service.istio.DestinationRuleService;
import com.harmonycloud.k8s.service.istio.ServiceEntryServices;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.istio.IstioCommonService;
import com.harmonycloud.service.istio.IstioServiceEntryService;
import com.harmonycloud.service.istio.util.IstioPolicyUtil;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.user.RoleLocalService;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.harmonycloud.service.platform.constant.Constant.LABEL_PROJECT_ID;

/**
 * create by weg on 18-12-27.
 */
@Service
public class IstioServiceEntryServiceImpl implements IstioServiceEntryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IstioServiceEntryServiceImpl.class);

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private DestinationRuleService destinationRuleService;

    @Autowired
    private IstioGlobalConfigureMapper istioGlobalConfigureMapper;

    @Autowired
    private ServicesService servicesService;

    @Autowired
    private ServiceEntryServices serviceEntryServices;

    @Autowired
    private RoleLocalService roleLocalService;

    @Autowired
    private IstioCommonService istioCommonService;

    @Autowired
    private RuleOverviewMapper ruleOverviewMapper;

    /**
     * 创建服务入口
     */
    @Override
    public ActionReturnUtil createExternalServiceEntry(ServiceEntryDto serviceEntryDto) throws Exception {
        AssertUtil.notNull(serviceEntryDto.getClusterId(), DictEnum.CLUSTER_ID);
        //获取集群
        Cluster cluster = clusterService.findClusterById(serviceEntryDto.getClusterId());
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        //判断serviceEntry是否存在
        K8SClientResponse response = serviceEntryServices.getServiceEntry(CommonConstant.ISTIO_NAMESPACE, null, cluster, serviceEntryDto.getName());
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_ENTRY_DUPLICATE);
        }
        //判断该集群下的服务域名是否存在
        K8SClientResponse allServiceEntryResponse = serviceEntryServices.getServiceEntry(CommonConstant.ISTIO_NAMESPACE, null, cluster, null);
        if (!HttpStatusUtil.isSuccessStatus(allServiceEntryResponse.getStatus())) {
            LOGGER.error("get external serviceentry error", allServiceEntryResponse.getBody());
            UnversionedStatus sta = JsonUtil.jsonToPojo(allServiceEntryResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        ServiceEntryList serviceEntryList = JsonUtil.jsonToPojo(allServiceEntryResponse.getBody(), ServiceEntryList.class);
        // item为实际service list的metadata，spec，status
        List<ServiceEntry> serviceEntries = serviceEntryList.getItems();
        if(CollectionUtils.isNotEmpty(serviceEntries)){
            for (ServiceEntry serviceEntry : serviceEntries) {
                String hosts = serviceEntry.getSpec().getHosts().get(0).toString();
                if (hosts.equals(serviceEntryDto.getHosts())) {
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_DOMIAN_DUPLICATE);
                }
            }
        }
        // 创建svcEntry包含了创建所需的所有所需参数
        ServiceEntry serviceEntry = IstioPolicyUtil.makeExternalServiceEntry(serviceEntryDto);
        K8SClientResponse serviceEntryResponse = serviceEntryServices.createServiceEntry(CommonConstant.ISTIO_NAMESPACE, serviceEntry, cluster);
        if (!HttpStatusUtil.isSuccessStatus(serviceEntryResponse.getStatus())) {
            LOGGER.error("create external serviceentry error", serviceEntryResponse.getBody());
            UnversionedStatus sta = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        //创建destinationRule
        String host = serviceEntryDto.getHosts();
        IstioPolicyUtil.createServiceEntryDR(serviceEntryDto.getName(), CommonConstant.ISTIO_NAMESPACE, host, cluster, destinationRuleService);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil createInternalServiceEntry(ServiceEntryDto serviceEntryDto, String projectId) throws Exception {
        AssertUtil.notNull(serviceEntryDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(serviceEntryDto.getClusterId(), DictEnum.CLUSTER_ID);
        AssertUtil.notNull(projectId, DictEnum.PROJECT_ID);
        //获取集群
        Cluster cluster = clusterService.findClusterById(serviceEntryDto.getClusterId());
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        //判断service是否存在
        K8SClientResponse response = serviceEntryServices.getService(serviceEntryDto.getNamespace(), null, cluster, serviceEntryDto.getHosts());
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DEPLOYMENT_NAME_DUPLICATE);
        }
        //创建服务
        com.harmonycloud.k8s.bean.Service service = IstioPolicyUtil.makeService(serviceEntryDto, projectId);
        Map<String, Object> bodyInfo = CollectionUtil.transBean2Map(service);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse serviceResponse = servicesService.doServiceByNamespace(serviceEntryDto.getNamespace(), headers, bodyInfo, HTTPMethod.POST, cluster);
        if (!HttpStatusUtil.isSuccessStatus(serviceResponse.getStatus())) {
            LOGGER.error("create service error", serviceResponse.getBody());
            UnversionedStatus sta = JsonUtil.jsonToPojo(serviceResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        com.harmonycloud.k8s.bean.Service serviceInfo = JsonUtil.jsonToPojo(serviceResponse.getBody(), com.harmonycloud.k8s.bean.Service.class);
        //获取clusterIp
        String clusterIp = serviceInfo.getSpec().getClusterIP();
        ServiceEntry serviceEntry = IstioPolicyUtil.makeInternalServiceEntry(serviceEntryDto, clusterIp, projectId);
        //创建内部服务入口
        K8SClientResponse serviceEntryResponse = serviceEntryServices.createServiceEntry(serviceEntryDto.getNamespace(), serviceEntry, cluster);
        if (!HttpStatusUtil.isSuccessStatus(serviceEntryResponse.getStatus())) {
            LOGGER.error("create internal serviceentry error", serviceEntryResponse.getBody());
            //删除创建成功的service
            K8SClientResponse svcResponse = serviceEntryServices.deleteExtService(serviceEntryDto.getHosts(), serviceEntryDto.getNamespace(), cluster);
            if (!HttpStatusUtil.isSuccessStatus(svcResponse.getStatus()) && Constant.HTTP_404 != svcResponse.getStatus()) {
                LOGGER.error("delete service error", svcResponse.getBody());
                UnversionedStatus stas = JsonUtil.jsonToPojo(svcResponse.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithMsg(stas.getMessage());
            }
            UnversionedStatus sta = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        //创建destinationRule
        String host = serviceEntryDto.getHosts() + "." + serviceEntryDto.getNamespace() + ".svc.cluster.local";
        IstioPolicyUtil.createServiceEntryDR(serviceEntryDto.getHosts(), serviceEntryDto.getNamespace(), host, cluster, destinationRuleService);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil updateExternalServiceEntry(ServiceEntryDto serviceEntryDto) throws Exception {
        AssertUtil.notNull(serviceEntryDto.getClusterId(), DictEnum.CLUSTER_ID);
        AssertUtil.notNull(serviceEntryDto.getName(), DictEnum.NAME);
        //获取集群信息
        Cluster cluster = clusterService.findClusterById(serviceEntryDto.getClusterId());
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        //查询
        K8SClientResponse serviceEntryResponse = serviceEntryServices.getServiceEntry(CommonConstant.ISTIO_NAMESPACE, null, cluster, serviceEntryDto.getName());
        if (!HttpStatusUtil.isSuccessStatus(serviceEntryResponse.getStatus())) {
            LOGGER.error("get serviceentry error", serviceEntryResponse.getBody());
            UnversionedStatus sta = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        ServiceEntry serviceEntryInfo = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), ServiceEntry.class);
        //判断该集群下的服务域名是否存在
        K8SClientResponse allServiceEntryResponse = serviceEntryServices.getServiceEntry(CommonConstant.ISTIO_NAMESPACE, null, cluster, null);
        if (!HttpStatusUtil.isSuccessStatus(allServiceEntryResponse.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_ENTRY_DUPLICATE);
        }
        ServiceEntryList serviceEntryList = JsonUtil.jsonToPojo(allServiceEntryResponse.getBody(), ServiceEntryList.class);
        // item为实际service list的metadata，spec，status
        List<ServiceEntry> serviceEntries = serviceEntryList.getItems();
        if(CollectionUtils.isNotEmpty(serviceEntries)){
            for(ServiceEntry serviceEntry : serviceEntries){
                String  hosts = serviceEntry.getSpec().getHosts().get(0).toString();
                String  serviceEntryName = serviceEntry.getMetadata().getName();
                if(hosts.equals(serviceEntryDto.getHosts()) && !serviceEntryName.equals(serviceEntryDto.getName())){
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_DOMIAN_DUPLICATE);
                }
            }
        }
        //组装部分修改的服务
        ServiceEntry serviceEntry = IstioPolicyUtil.makePartExternalServiceEntry(serviceEntryInfo, serviceEntryDto);
        K8SClientResponse response = serviceEntryServices.updateServiceEntry(CommonConstant.ISTIO_NAMESPACE, serviceEntryDto.getName(), serviceEntry, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("update external serviceentry error", response.getBody());
            UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        //获取集群中DestinationRule
        K8SClientResponse destinationRuleResponse = destinationRuleService.getDestinationRule(CommonConstant.ISTIO_NAMESPACE, serviceEntryDto.getName(), cluster);
        if (!HttpStatusUtil.isSuccessStatus(destinationRuleResponse.getStatus())) {
            LOGGER.error("get DestinationRule error", destinationRuleResponse.getBody());
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        DestinationRule destinationRule = JsonUtil.jsonToPojo(destinationRuleResponse.getBody(), DestinationRule.class);
        destinationRule.getSpec().setHost(serviceEntryDto.getHosts());
        //更新DestinationRule
        K8SClientResponse updateResponse = destinationRuleService.updateDestinationRule(CommonConstant.ISTIO_NAMESPACE,  serviceEntryDto.getName(), destinationRule, cluster);
        if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
            LOGGER.error(status.getMessage());
            return ActionReturnUtil.returnSuccessWithData(status.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil updateInternalServiceEntry(ServiceEntryDto serviceEntryDto, String projectId) throws Exception {
        AssertUtil.notNull(serviceEntryDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(serviceEntryDto.getClusterId(), DictEnum.CLUSTER_ID);
        AssertUtil.notNull(serviceEntryDto.getName(), DictEnum.NAME);
        //获取集群信息
        Cluster cluster = clusterService.findClusterById(serviceEntryDto.getClusterId());
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        //查询原来serviceEntry
        K8SClientResponse serviceEntryResponse = serviceEntryServices.getServiceEntry(serviceEntryDto.getNamespace(), null, cluster, serviceEntryDto.getName());
        if (!HttpStatusUtil.isSuccessStatus(serviceEntryResponse.getStatus())) {
            LOGGER.error("get serviceentry error", serviceEntryResponse.getBody());
            UnversionedStatus sta = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        ServiceEntry serviceEntryInfo = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), ServiceEntry.class);
        List<String> hosts = serviceEntryInfo.getSpec().getHosts();
        if (!CollectionUtils.isEmpty(hosts)) {
            String host = hosts.get(0);
            String hostValue = host.substring(0, host.indexOf("."));
            if (hostValue.equals(serviceEntryDto.getHosts())) { //判断域名是否修改
                //查询原来的service是否存在
                K8SClientResponse oldServiceResponse = serviceEntryServices.getService(serviceEntryDto.getNamespace(), null, cluster, hostValue);
                if (!HttpStatusUtil.isSuccessStatus(oldServiceResponse.getStatus())) {
                    LOGGER.error("get service error", oldServiceResponse.getBody());
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DEPLOYMENT_GET_FAILURE);
                }
                com.harmonycloud.k8s.bean.Service oldService = JsonUtil.jsonToPojo(oldServiceResponse.getBody(), com.harmonycloud.k8s.bean.Service.class);
                //修改serviceentry
                ServiceEntry serviceEntry = IstioPolicyUtil.makePartInternalServiceEntry(serviceEntryInfo, oldService.getSpec().getClusterIP(), serviceEntryDto);
                K8SClientResponse updateServiceEntryResponse = serviceEntryServices.updateServiceEntry(serviceEntryDto.getNamespace(), serviceEntryDto.getName(), serviceEntry, cluster);
                if (!HttpStatusUtil.isSuccessStatus(updateServiceEntryResponse.getStatus())) {
                    LOGGER.error("update internal serviceentry  error", updateServiceEntryResponse.getBody());
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL);
                }
                //修改service
                com.harmonycloud.k8s.bean.Service service = IstioPolicyUtil.makePartService(oldService, serviceEntryDto);
                K8SClientResponse response = serviceEntryServices.updateService(serviceEntryDto.getNamespace(), serviceEntryDto.getHosts(), service, cluster);
                if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                    LOGGER.error("update service error", response.getBody());
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_DELETE, hostValue, false);
                }
                return ActionReturnUtil.returnSuccess();
            }
        }
        //判断service是否存在
        K8SClientResponse serviceResponse = serviceEntryServices.getService(serviceEntryDto.getNamespace(), null, cluster, serviceEntryDto.getHosts());
        if (HttpStatusUtil.isSuccessStatus(serviceResponse.getStatus())) {
            LOGGER.error("get service error", serviceResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DEPLOYMENT_NAME_DUPLICATE);
        }
        //创建服务
        com.harmonycloud.k8s.bean.Service service = IstioPolicyUtil.makeService(serviceEntryDto, projectId);
        Map<String, Object> bodyInfo = CollectionUtil.transBean2Map(service);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse response = servicesService.doServiceByNamespace(serviceEntryDto.getNamespace(), headers, bodyInfo, HTTPMethod.POST, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("create service error", response.getBody());
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL);
        }
        //获取当前创建成功的服务
        K8SClientResponse newServiceResponse = serviceEntryServices.getService(serviceEntryDto.getNamespace(), null, cluster, serviceEntryDto.getHosts());
        if (!HttpStatusUtil.isSuccessStatus(newServiceResponse.getStatus())) {
            LOGGER.error("get new service error", newServiceResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL);
        }
        com.harmonycloud.k8s.bean.Service serviceInfo = JsonUtil.jsonToPojo(newServiceResponse.getBody(), com.harmonycloud.k8s.bean.Service.class);
        String clusterIp = serviceInfo.getSpec().getClusterIP();
        //删除新的service
        if (StringUtils.isBlank(clusterIp)) {
            K8SClientResponse svcResponse = serviceEntryServices.deleteExtService(serviceEntryDto.getHosts(), serviceEntryDto.getNamespace(), cluster);
            if (!HttpStatusUtil.isSuccessStatus(svcResponse.getStatus()) && Constant.HTTP_404 != svcResponse.getStatus()) {
                LOGGER.error("delete new service error", svcResponse.getBody());
                UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
            }
        }
        //修改serviceEntry
        ServiceEntry serviceEntry = IstioPolicyUtil.makePartInternalServiceEntry(serviceEntryInfo, clusterIp, serviceEntryDto);
        K8SClientResponse responses = serviceEntryServices.updateServiceEntry(serviceEntryDto.getNamespace(), serviceEntryDto.getName(), serviceEntry, cluster);
        if (!HttpStatusUtil.isSuccessStatus(responses.getStatus())) {
            LOGGER.error("update internal serviceentry error", response.getBody());
            //删除创建成功的service
            K8SClientResponse svcResponse = serviceEntryServices.deleteExtService(serviceEntryDto.getHosts(), serviceEntryDto.getNamespace(), cluster);
            if (!HttpStatusUtil.isSuccessStatus(svcResponse.getStatus()) && Constant.HTTP_404 != svcResponse.getStatus()) {
                UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
            }
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL);
        }
        //删除旧的service
        String host = hosts.get(0);
        String name = host.substring(0, host.indexOf("."));
        K8SClientResponse svcResponse = serviceEntryServices.deleteExtService(name, serviceEntryDto.getNamespace(), cluster);
        if (!HttpStatusUtil.isSuccessStatus(svcResponse.getStatus()) && Constant.HTTP_404 != svcResponse.getStatus()) {
            LOGGER.error("delete service error", svcResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_DELETE, name, false);
        }
        //判断是否存在要创建destinationrule
        K8SClientResponse destinationRuleResponse = destinationRuleService.getDestinationRule(serviceEntryDto.getNamespace(), serviceEntryDto.getHosts(), cluster);
        if (HttpStatusUtil.isSuccessStatus(destinationRuleResponse.getStatus())) {
            LOGGER.error("destinationRule name duplicate", destinationRuleResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DESTINATIONRULE_NAME_DUPLICATE);
        }
        //创建新的destinationRule
        String hostValue = serviceEntryDto.getHosts() + "." + serviceEntryDto.getNamespace() + ".svc.cluster.local";
        IstioPolicyUtil.createServiceEntryDR(serviceEntryDto.getHosts(), serviceEntryDto.getNamespace(), hostValue, cluster, destinationRuleService);
        //删除旧的destinationrule
        String drHost = hosts.get(0);
        String drHostValue = host.substring(0, host.indexOf("."));
        K8SClientResponse deleteDestinationRuleResponse = destinationRuleService.deleteDestinationRule(serviceEntryDto.getNamespace(),drHostValue,cluster);
        if (!HttpStatusUtil.isSuccessStatus(deleteDestinationRuleResponse.getStatus()) && Constant.HTTP_404 != deleteDestinationRuleResponse.getStatus()) {
            LOGGER.error("delete destinationRule error", response.getBody());
            UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return  ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DESTINATIONRULE_DELETE, drHostValue, false);
        }
        //获取原来的是否有创建好的策略
        Map ruleInfo = new HashMap();
        ruleInfo.put(CommonConstant.RULE_CLUSTER_ID, serviceEntryDto.getClusterId());
        ruleInfo.put(CommonConstant.RULE_NS, serviceEntryDto.getNamespace());
        ruleInfo.put(CommonConstant.RULE_SVC, drHostValue);
        List<RuleOverview> ruleOverviews = ruleOverviewMapper.selectByRuleInfo(ruleInfo);
        if(CollectionUtils.isNotEmpty(ruleOverviews)){
            for(RuleOverview  ruleOverview : ruleOverviews){
                ruleOverview.setRuleSvc(serviceEntryDto.getHosts());
                ruleOverview.setUpdateTime(new Date());
                ruleOverviewMapper.updateByPrimaryKey(ruleOverview);
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil deleteInternalServiceEntry(String serviceEntryName, String namespace, String hosts, String clusterId) throws Exception {
        //获取集群信息
        Cluster cluster = clusterService.findClusterById(clusterId);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        if (StringUtils.isBlank(hosts)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        String  host = hosts;
        if(hosts.contains(".")){
            host = host.substring(0, hosts.indexOf("."));
        }
        istioCommonService.deleteIstioPolicy(namespace, host, clusterId);
        //删除serviceEntry
        K8SClientResponse serviceEntryResponse = serviceEntryServices.deleteServiceEntry(serviceEntryName, namespace, cluster);
        if (!HttpStatusUtil.isSuccessStatus(serviceEntryResponse.getStatus()) && Constant.HTTP_404 != serviceEntryResponse.getStatus()) {
            LOGGER.error("delete serviceentry error", serviceEntryResponse.getBody());
            UnversionedStatus sta = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        //删除service
        K8SClientResponse response = serviceEntryServices.deleteExtService(host, namespace, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
            LOGGER.error("delete service error", response.getBody());
            UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil deleteExternalServiceEntry(String serviceEntryName, String clusterId) throws Exception {
        //获取集群信息
        Cluster cluster = clusterService.findClusterById(clusterId);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        //删除对应的策略信息
        istioCommonService.deleteIstioPolicy(CommonConstant.ISTIO_NAMESPACE, serviceEntryName, clusterId);
        //删除serviceEntry
        K8SClientResponse serviceEntryResponse = serviceEntryServices.deleteServiceEntry(serviceEntryName, CommonConstant.ISTIO_NAMESPACE, cluster);
        if (!HttpStatusUtil.isSuccessStatus(serviceEntryResponse.getStatus()) && Constant.HTTP_404 != serviceEntryResponse.getStatus()) {
            LOGGER.error("delete serviceentry error", serviceEntryResponse.getBody());
            UnversionedStatus sta = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 获取外部服务tenantName
     */
    @Override
    public ActionReturnUtil listServiceEntry(String projectId, String clusterId, String serviceEntryType, String namespace, boolean isTenantScope) throws Exception {
        String currentClusterId = clusterId;
        if (StringUtils.isNotBlank(namespace) && !namespace.equals(CommonConstant.ISTIO_NAMESPACE)) {
            Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
            currentClusterId = cluster.getId();
        }

        List<Cluster> clusters = new ArrayList<>();
        if (StringUtils.isBlank(currentClusterId)) {
            List<Cluster> clusterList = roleLocalService.listCurrentUserRoleCluster();
            //筛选istio开关被开启的集群
            if (!CollectionUtils.isEmpty(clusterList)) {
                for (Cluster clusterItem : clusterList) {
                    IstioGlobalConfigure istioGlobalConfigure = istioGlobalConfigureMapper.getByClusterId(clusterItem.getId());
                    if (Objects.nonNull(istioGlobalConfigure) && istioGlobalConfigure.getSwitchStatus() == CommonConstant.OPEN_GLOBAL_STATUS) {
                        clusters.add(clusterItem);
                    }
                }
            }
        } else {
            clusters.add(clusterService.findClusterById(currentClusterId));
        }
        List<ServiceEntryDto> serviceEntryDtoList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(clusters)) {
            for (Cluster cluster : clusters) {
                try {
                    ServiceEntryList serviceEntryList = new ServiceEntryList();
                    // 获取serviceList
                    if (Integer.valueOf(serviceEntryType) == CommonConstant.INTERNAL_SERVICE_ENTRY) { //ip模式下获取serviceentry
                        Map<String, Object> bodys = new HashMap<>();
                        // isTenantScope 用来标记是否在租户级别查询服务入口列表
                        if (StringUtils.isNotEmpty(projectId) && !isTenantScope) {
                            bodys.put("labelSelector", LABEL_PROJECT_ID + "=" + projectId + CommonConstant.PROJECT_ID_SERVICEENTRY);
                        }
                        K8SClientResponse response = serviceEntryServices.getServiceEntry(namespace, null, bodys, HTTPMethod.GET, cluster);
                        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                            LOGGER.error("get serviceentry error", response.getBody());
                            UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
                        }
                        serviceEntryList = JsonUtil.jsonToPojo(response.getBody(), ServiceEntryList.class);
                    } else {
                        K8SClientResponse response = serviceEntryServices.getServiceEntry(CommonConstant.ISTIO_NAMESPACE, null, null, HTTPMethod.GET, cluster);
                        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                            LOGGER.error("get serviceentry error", response.getBody());
                            UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
                        }
                        serviceEntryList = JsonUtil.jsonToPojo(response.getBody(), ServiceEntryList.class);
                    }
                    // item为实际service list的metadata，spec，status
                    List<ServiceEntry> serviceEntries = serviceEntryList.getItems();
                    if (CollectionUtils.isNotEmpty(serviceEntries)) {
                        for (int i = 0; i < serviceEntries.size(); i++) {
                            ServiceEntryDto serviceEntryDto = new ServiceEntryDto();
                            serviceEntryDto.setClusterName(cluster.getAliasName());
                            serviceEntryDto.setClusterId(cluster.getId());
                            ServiceEntry svcEntry = serviceEntries.get(i);
                            serviceEntryDto.setName(svcEntry.getMetadata().getName());
                            String host = svcEntry.getSpec().getHosts().get(0).toString();
                            //获取endpoints
                            List<Endpoint> addressList = svcEntry.getSpec().getEndpoints();
                            List<String> ipList = new ArrayList<>();
                            if (!CollectionUtils.isEmpty(addressList) && svcEntry.getSpec().getLocation().equals(CommonConstant.MESH_INTERNAL)) {
                                for (Endpoint addressItem : addressList) {
                                    ipList.add(addressItem.getAddress());
                                }
                                serviceEntryDto.setIpList(ipList);
                                serviceEntryDto.setServiceEntryType(CommonConstant.INTERNAL_SERVICE_ENTRY);
                                serviceEntryDto.setHosts(host.substring(0, host.indexOf(".")));
                            } else {
                                serviceEntryDto.setHosts(host);
                                serviceEntryDto.setServiceEntryType(CommonConstant.EXTERNAL_SERVICE_ENTRY);
                            }
                            List<Port> portList = svcEntry.getSpec().getPorts();
                            List<ServiceEntryPortDto> portDtoList = new ArrayList<>();
                            portList.forEach(port -> {
                                ServiceEntryPortDto portDto = new ServiceEntryPortDto();
                                portDto.setNumber(port.getNumber());
                                portDto.setProtocol(port.getProtocol());
                                portDtoList.add(portDto);
                            });
                            serviceEntryDto.setPortList(portDtoList);
                            serviceEntryDto.setCreateTime(svcEntry.getMetadata().getCreationTimestamp());
                            serviceEntryDto.setNamespace(svcEntry.getMetadata().getNamespace());
                            if (Integer.valueOf(serviceEntryType) == CommonConstant.INTERNAL_SERVICE_ENTRY) {
                                NamespaceLocal namespacePojo = namespaceLocalService.getNamespaceByName(serviceEntryDto.getNamespace());
                                if (namespacePojo == null) {
                                    throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_NOT_FOUND);
                                }
                                serviceEntryDto.setNamespaceName(namespacePojo.getAliasName());
                            }
                            serviceEntryDtoList.add(serviceEntryDto);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.info("查询服务入口失败， clusterId：{}", cluster.getId(), e);
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(serviceEntryDtoList);
    }

    @Override
    public ActionReturnUtil getServiceEntry(String serviceEntryName, String namespace, String clusterId, String serviceEntryType) throws Exception {
        AssertUtil.notBlank(serviceEntryName, DictEnum.NAME);
        // 获取serviceList
        Cluster cluster = clusterService.findClusterById(clusterId);
        AssertUtil.notNull(cluster, DictEnum.CLUSTER);
        //获取分区
        NamespaceLocal namespaceDetail = new NamespaceLocal();
        if (Integer.valueOf(serviceEntryType) == CommonConstant.INTERNAL_SERVICE_ENTRY) {
            namespaceDetail = namespaceLocalService.getNamespaceByName(namespace);
            if (Objects.isNull(namespaceDetail)) {
                throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_NOT_FOUND);
            }
        }
        K8SClientResponse response = serviceEntryServices.getServiceEntryByName(namespace, null, null, serviceEntryName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("get serviceentry error", response.getBody());
            UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        ServiceEntry serviceEntry = JsonUtil.jsonToPojo(response.getBody(), ServiceEntry.class);
        ServiceEntryDto serviceEntryDto = new ServiceEntryDto();
        if (Objects.nonNull(serviceEntry)) {
            serviceEntryDto.setNamespace(serviceEntry.getMetadata().getNamespace());
            serviceEntryDto.setNamespaceName(namespaceDetail.getAliasName());
            serviceEntryDto.setClusterId(cluster.getId());
            serviceEntryDto.setClusterName(cluster.getAliasName());
            serviceEntryDto.setName(serviceEntry.getMetadata().getName());
            serviceEntryDto.setCreateTime(serviceEntry.getMetadata().getCreationTimestamp());
            List<Endpoint> addressList = serviceEntry.getSpec().getEndpoints();
            List<String> ipList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(addressList) && serviceEntry.getSpec().getLocation().equals(CommonConstant.MESH_INTERNAL)) {
                for (Endpoint addressItem : addressList) {
                    ipList.add(addressItem.getAddress());
                }
                serviceEntryDto.setIpList(ipList);
                serviceEntryDto.setServiceEntryType(CommonConstant.INTERNAL_SERVICE_ENTRY);
            } else {
                serviceEntryDto.setServiceEntryType(CommonConstant.EXTERNAL_SERVICE_ENTRY);
            }
            List<Port> portList = serviceEntry.getSpec().getPorts();
            List<ServiceEntryPortDto> portDtoList = new ArrayList<>();
            portList.forEach(port -> {
                ServiceEntryPortDto portDto = new ServiceEntryPortDto();
                portDto.setNumber(port.getNumber());
                portDto.setProtocol(port.getProtocol());
                portDtoList.add(portDto);
            });
            serviceEntryDto.setPortList(portDtoList);
            //获取服务域名
            List<String> hosts = serviceEntry.getSpec().getHosts();
            if (!CollectionUtils.isEmpty(hosts)) {
                String host = hosts.get(0);
                if(serviceEntryDto.getServiceEntryType() == CommonConstant.INTERNAL_SERVICE_ENTRY){
                    String domainName = host.substring(0, host.indexOf("."));
                    serviceEntryDto.setHosts(domainName);
                }else {
                    serviceEntryDto.setHosts(host);
                }
            }

        }
        return ActionReturnUtil.returnSuccessWithData(serviceEntryDto);
    }
}
