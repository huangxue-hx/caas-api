package com.harmonycloud.service.platform.serviceImpl.k8s;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.istio.IstioGlobalConfigureMapper;
import com.harmonycloud.dao.istio.bean.IstioGlobalConfigure;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dto.application.istio.ServiceEntryDto;
import com.harmonycloud.dto.cluster.ClusterDto;
import com.harmonycloud.dto.cluster.IstioClusterDto;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.ServicePort;
import com.harmonycloud.k8s.bean.ServiceSpec;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.policies.Port;
import com.harmonycloud.k8s.bean.istio.policies.ServiceEntry;
import com.harmonycloud.k8s.bean.istio.policies.ServiceEntryList;
import com.harmonycloud.k8s.bean.istio.policies.ServiceEntrySpec;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.ServicesService;
import com.harmonycloud.k8s.service.istio.ServiceEntryServices;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.ServiceEntryService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.user.RoleLocalService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.harmonycloud.service.platform.constant.Constant.LABEL_PROJECT_ID;

@Service
public class ServiceEntryImpl implements ServiceEntryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEntryImpl.class);

    @Autowired
    private ServicesService sService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private ServiceEntryServices serviceEntryServices;

    @Autowired
    private RoleLocalService roleLocalService;

    @Autowired
    private IstioGlobalConfigureMapper istioGlobalConfigureMapper;




    /**
     * 创建服务入口
     */
    @Override
    public ActionReturnUtil createServiceEntry(ServiceEntryDto serviceEntryDto) throws Exception {
        if (serviceEntryDto == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
        }
        AssertUtil.notNull(serviceEntryDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(serviceEntryDto.getClusterId(), DictEnum.CLUSTER_ID);
        //获取集群
        Cluster cluster = clusterService.findClusterById(serviceEntryDto.getClusterId());
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        // 获取serviceEntry
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("labelSelector", LABEL_PROJECT_ID + "=" + serviceEntryDto.getProjectId());
        //判断serviceEntry是否存在
        K8SClientResponse response = serviceEntryServices.getServiceEntry(serviceEntryDto.getNamespace(), bodys, cluster, serviceEntryDto.getName());
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            if (Objects.nonNull(sta)) {
                return ActionReturnUtil.returnErrorWithMsg(response.getBody());
            }
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_ENTRY_DUPLICATE);
        }
        // 创建svcEntry包含了创建所需的所有所需参数
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("Content-Type", "application/json");
        ServiceEntry serviceEntry = this.makeServiceEntry(serviceEntryDto);
        Map<String, Object> body = new HashMap<String, Object>();
        body = CollectionUtil.transBean2Map(serviceEntry);
        K8SClientResponse serviceEntryResponse = serviceEntryServices.createServiceEntry(serviceEntryDto.getNamespace(), head, body, HTTPMethod.POST, cluster);
        if (!HttpStatusUtil.isSuccessStatus(serviceEntryResponse.getStatus())) {
            UnversionedStatus sta = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil createInsServiceEntry(ServiceEntryDto serviceEntryDto) throws Exception {
        if (serviceEntryDto == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
        }
        AssertUtil.notNull(serviceEntryDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(serviceEntryDto.getClusterId(), DictEnum.CLUSTER_ID);
        //查找是否存在service
        Cluster cluster = clusterService.findClusterById(serviceEntryDto.getClusterId());
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        // 获取serviceEntry
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("labelSelector", LABEL_PROJECT_ID + "=" + serviceEntryDto.getProjectId());
        //判断service是否存在
        K8SClientResponse response = serviceEntryServices.getService(serviceEntryDto.getNamespace(), bodys, cluster, serviceEntryDto.getHosts());
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DEPLOYMENT_NAME_DUPLICATE);
        }
        //创建服务
        com.harmonycloud.k8s.bean.Service service = this.makeService(serviceEntryDto, cluster);
        Map<String, Object> bodyInfo = new HashMap<String, Object>();
        bodyInfo = CollectionUtil.transBean2Map(service);
        Map<String, Object> heads = new HashMap<String, Object>();
        heads.put("Content-Type", "application/json");
        K8SClientResponse serviceResponse = sService.doServiceByNamespace(serviceEntryDto.getNamespace(), heads, bodyInfo, HTTPMethod.POST, cluster);
        if (!HttpStatusUtil.isSuccessStatus(serviceResponse.getStatus())) {
            UnversionedStatus sta = JsonUtil.jsonToPojo(serviceResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        com.harmonycloud.k8s.bean.Service serviceInfo = JsonUtil.jsonToPojo(serviceResponse.getBody(), com.harmonycloud.k8s.bean.Service.class);
        //获取clusterIp
        String clusterIp = serviceInfo.getSpec().getClusterIP();
        //创建内部服务入口
        ServiceEntry serviceEntry = this.makeExtServiceEntry(serviceEntryDto, clusterIp);
        Map<String, Object> body = new HashMap<String, Object>();
        body = CollectionUtil.transBean2Map(serviceEntry);
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("Content-Type", "application/json");
        K8SClientResponse serviceEntryResponse = serviceEntryServices.createServiceEntry(serviceEntryDto.getNamespace(), head, body, HTTPMethod.POST, cluster);
        if (!HttpStatusUtil.isSuccessStatus(serviceEntryResponse.getStatus())) {
            //删除创建成功的service
            K8SClientResponse svcResponse = serviceEntryServices.deleteExtService(serviceEntryDto.getHosts(), serviceEntryDto.getNamespace(), cluster);
            if (!HttpStatusUtil.isSuccessStatus(svcResponse.getStatus())) {
                UnversionedStatus stas = JsonUtil.jsonToPojo(svcResponse.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithMsg(stas.getMessage());
            }
            UnversionedStatus sta = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil updateServiceEntry(ServiceEntryDto serviceEntryDto) throws Exception {
        if (StringUtils.isBlank(serviceEntryDto.getClusterId())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER, "clusterId", true);
        }
        AssertUtil.notNull(serviceEntryDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(serviceEntryDto.getClusterId(), DictEnum.CLUSTER_ID);
        AssertUtil.notNull(serviceEntryDto.getName(), DictEnum.NAME);
        //获取集群信息
        Cluster cluster = clusterService.findClusterById(serviceEntryDto.getClusterId());
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //查询
        K8SClientResponse serviceEntryResponse = serviceEntryServices.getServiceEntry(serviceEntryDto.getNamespace(), null, cluster, serviceEntryDto.getName());
        if (!HttpStatusUtil.isSuccessStatus(serviceEntryResponse.getStatus())) {
            UnversionedStatus sta = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), UnversionedStatus.class);
            if (Objects.isNull(sta)) {
                return ActionReturnUtil.returnErrorWithMsg(serviceEntryResponse.getBody());
            }
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_ENTRY_FAILED);
        }
        ServiceEntry serviceEntryInfo = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), ServiceEntry.class);
        String resourceVersion = null;
        if (Objects.nonNull(serviceEntryInfo)) {
            resourceVersion = serviceEntryInfo.getMetadata().getResourceVersion();
        }
        //修改serviceEntry
        ServiceEntry serviceEntry = this.makeServiceEntry(serviceEntryDto);
        if (StringUtils.isNotBlank(resourceVersion)) {
            serviceEntry.getMetadata().setResourceVersion(resourceVersion);
        }
        Map<String, Object> body = new HashMap<String, Object>();
        body = CollectionUtil.transBean2Map(serviceEntry);
        K8SClientResponse response = serviceEntryServices.updateServiceEntry(serviceEntryDto.getNamespace(), serviceEntryDto.getName(), body, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil updateInsServiceEntry(ServiceEntryDto serviceEntryDto) throws Exception {
        if (StringUtils.isBlank(serviceEntryDto.getClusterId())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER, "clusterId", true);
        }
        AssertUtil.notNull(serviceEntryDto.getNamespace(), DictEnum.NAMESPACE);
        AssertUtil.notNull(serviceEntryDto.getClusterId(), DictEnum.CLUSTER_ID);
        AssertUtil.notNull(serviceEntryDto.getName(), DictEnum.NAME);
        //获取集群信息
        Cluster cluster = clusterService.findClusterById(serviceEntryDto.getClusterId());
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //查询原来serviceEntry
        K8SClientResponse serviceEntryResponse = serviceEntryServices.getServiceEntry(serviceEntryDto.getNamespace(), null, cluster, serviceEntryDto.getName());
        if (!HttpStatusUtil.isSuccessStatus(serviceEntryResponse.getStatus())) {
            UnversionedStatus sta = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), UnversionedStatus.class);
            if (Objects.isNull(sta)) {
                return ActionReturnUtil.returnErrorWithMsg(serviceEntryResponse.getBody());
            }
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_ENTRY_FAILED);
        }
        ServiceEntry serviceEntryInfo = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), ServiceEntry.class);
        //修改erviceEntry的时候用到
        String resourceVersion = Objects.nonNull(serviceEntryInfo) ? serviceEntryInfo.getMetadata().getResourceVersion() : null;
        //获取clusterIp
        List<String> addressList = serviceEntryInfo.getSpec().getAddresses();
        String clusterIp = CollectionUtils.isEmpty(addressList) ? null : addressList.get(0);
        ServiceEntry serviceEntry = this.makeExtServiceEntry(serviceEntryDto, clusterIp);
        if (StringUtils.isNotBlank(resourceVersion)) {
            serviceEntry.getMetadata().setResourceVersion(resourceVersion);
        }
        List<String> hosts = serviceEntryInfo.getSpec().getHosts();
        if (!CollectionUtils.isEmpty(hosts)) {
            String host = hosts.get(0);
            String name = host.substring(0, host.indexOf("."));
            if (name.equals(serviceEntryDto.getHosts())) {        //判断域名是否修改
                //查询原来的service是否存在
                K8SClientResponse oldServiceResponse = serviceEntryServices.getService(serviceEntryDto.getNamespace(), null, cluster, serviceEntryDto.getHosts());
                if (!HttpStatusUtil.isSuccessStatus(oldServiceResponse.getStatus())) {
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DEPLOYMENT_GET_FAILURE);
                }
                com.harmonycloud.k8s.bean.Service oldService = JsonUtil.jsonToPojo(oldServiceResponse.getBody(), com.harmonycloud.k8s.bean.Service.class);
                //修改service
                com.harmonycloud.k8s.bean.Service service = this.makeService(serviceEntryDto, cluster);
               if(Objects.nonNull(service)){
                   service.getMetadata().setResourceVersion(oldService.getMetadata().getResourceVersion());
                   service.getSpec().setClusterIP(oldService.getSpec().getClusterIP());
               }
                Map<String, Object> body = new HashMap<String, Object>();
                body = CollectionUtil.transBean2Map(service);
                K8SClientResponse response = serviceEntryServices.updateService(serviceEntryDto.getNamespace(), serviceEntryDto.getHosts(), body, cluster);
                if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                    UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                    return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
                }
                //修改serviceentry
                serviceEntry.getMetadata().setResourceVersion(resourceVersion);
                Map<String, Object> bodys = new HashMap<String, Object>();
                bodys = CollectionUtil.transBean2Map(serviceEntry);
                K8SClientResponse responses = serviceEntryServices.updateServiceEntry(serviceEntryDto.getNamespace(), serviceEntryDto.getName(), bodys, cluster);
                if (!HttpStatusUtil.isSuccessStatus(responses.getStatus())) {
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL);
                }
                return ActionReturnUtil.returnSuccess();
            }
        }
        //判断service是否存在
        K8SClientResponse oldServiceResponse = serviceEntryServices.getService(serviceEntryDto.getNamespace(), null, cluster, serviceEntryDto.getHosts());
        if (HttpStatusUtil.isSuccessStatus(oldServiceResponse.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DEPLOYMENT_NAME_DUPLICATE);
        }
        //创建服务
        com.harmonycloud.k8s.bean.Service service = this.makeService(serviceEntryDto, cluster);
        Map<String, Object> bodyInfo = new HashMap<String, Object>();
        bodyInfo = CollectionUtil.transBean2Map(service);
        Map<String, Object> heads = new HashMap<String, Object>();
        heads.put("Content-Type", "application/json");
        K8SClientResponse response = sService.doServiceByNamespace(serviceEntryDto.getNamespace(), heads, bodyInfo, HTTPMethod.POST, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL);
        }
        //获取当前创建成功的服务
        K8SClientResponse newServiceResponse = serviceEntryServices.getService(serviceEntryDto.getNamespace(), null, cluster, serviceEntryDto.getHosts());
        if (!HttpStatusUtil.isSuccessStatus(newServiceResponse.getStatus())) {
            //删除新的service
            K8SClientResponse svcResponse = serviceEntryServices.deleteExtService(serviceEntryDto.getHosts(), serviceEntryDto.getNamespace(), cluster);
            if (!HttpStatusUtil.isSuccessStatus(svcResponse.getStatus())) {
                UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
            }
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL);
        }
        com.harmonycloud.k8s.bean.Service serviceInfo = JsonUtil.jsonToPojo(newServiceResponse.getBody(), com.harmonycloud.k8s.bean.Service.class);
        String serviceClusterIp = serviceInfo.getSpec().getClusterIP();

        //修改serviceEntry
        serviceEntry.getMetadata().setResourceVersion(resourceVersion);
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys = CollectionUtil.transBean2Map(serviceEntry);
        K8SClientResponse responses = serviceEntryServices.updateServiceEntry(serviceEntryDto.getNamespace(), serviceEntryDto.getName(), bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(responses.getStatus())) {
            //删除创建成功的service
            K8SClientResponse svcResponse = serviceEntryServices.deleteExtService(serviceEntryDto.getHosts(), serviceEntryDto.getNamespace(), cluster);
            if (!HttpStatusUtil.isSuccessStatus(svcResponse.getStatus())) {
                UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
            }
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL);
        }

        //删除旧的service
        String host = hosts.get(0);
        String name = host.substring(0, host.indexOf("."));
        K8SClientResponse svcResponse = serviceEntryServices.deleteExtService(name, serviceEntryDto.getNamespace(), cluster);
        if (!HttpStatusUtil.isSuccessStatus(svcResponse.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_DELETE,name,false);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil deleteExtServiceEntry(String clusterId, String serviceEntryName, String namespace, String serviceEntryType) throws Exception {
        if (StringUtils.isBlank(serviceEntryName) || StringUtils.isBlank(serviceEntryType)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
        }

        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Integer.valueOf(serviceEntryType) == 1) {
            //删除service
            K8SURL url = new K8SURL();
            url.setResource(Resource.SERVICE).setNamespace(namespace).setSubpath(serviceEntryName);
            K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
            }
        }
        //删除serviceEntry
        K8SClientResponse serviceEntryResponse = serviceEntryServices.deleteServiceEntry(serviceEntryName, namespace, cluster);
        if (!HttpStatusUtil.isSuccessStatus(serviceEntryResponse.getStatus())) {
            UnversionedStatus sta = JsonUtil.jsonToPojo(serviceEntryResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 获取外部服务tenantName
     */
    @Override
    public ActionReturnUtil listExtServiceEntry(String clusterId, String projectId) throws Exception {
        if (StringUtils.isBlank(projectId)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
        }
        List<Cluster> clusters = new ArrayList<>();
        if (StringUtils.isBlank(clusterId)) {
            List<Cluster> clusterList = roleLocalService.listCurrentUserRoleCluster();
            //筛选istio开关被开启的集群
            if (!CollectionUtils.isEmpty(clusterList)) {
                for (Cluster clusterItem : clusterList) {
                    IstioGlobalConfigure istioGlobalConfigure = istioGlobalConfigureMapper.getByClusterId(clusterItem.getId());
                    if (Objects.nonNull(istioGlobalConfigure) && istioGlobalConfigure.getSwitchStatus() == CommonConstant.OPEN_GLOBAL_STATUS) {
                        Cluster cluster = new Cluster();
                        BeanUtils.copyProperties(clusterItem, cluster);
                        clusters.add(cluster);
                    }
                }
            }
        } else {
            clusters.add(clusterService.findClusterById(clusterId));
        }
        List<ServiceEntryDto> serviceEntryDtoList = new ArrayList<>();
        // 获取serviceList
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("labelSelector", LABEL_PROJECT_ID + "=" + projectId);
        for (Cluster cluster : clusters) {
            K8SClientResponse response = serviceEntryServices.getServiceEntry(null, null, bodys, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                if (Objects.isNull(sta)) {
                    return ActionReturnUtil.returnErrorWithMsg(response.getBody());
                }
                return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
            }
            ServiceEntryList serviceEntryList = JsonUtil.jsonToPojo(response.getBody(), ServiceEntryList.class);
            // item为实际service list的metadata，spec，status
            List<ServiceEntry> serviceEntries = serviceEntryList.getItems();
            if (serviceEntries != null && !serviceEntries.isEmpty()) {
                for (int i = 0; i < serviceEntries.size(); i++) {
                    ServiceEntryDto serviceEntryDto = new ServiceEntryDto();
                    serviceEntryDto.setClusterId(cluster.getId());
                    serviceEntryDto.setClusterName(cluster.getAliasName());
                    ServiceEntry svcEntry = serviceEntries.get(i);
                    serviceEntryDto.setName(svcEntry.getMetadata().getName());
                    serviceEntryDto.setPort(svcEntry.getSpec().getPorts().get(0).toString());
                    serviceEntryDto.setHosts(svcEntry.getSpec().getHosts().get(0).toString());
                    List<Map<String, Object>> addressList = svcEntry.getSpec().getEndpoints();
                    List<String> ipList = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(addressList)) {
                        for (Map<String, Object> addressItem : addressList) {
                            ipList.add(addressItem.get("address").toString());
                        }
                        serviceEntryDto.setIpList(ipList);
                        serviceEntryDto.setServiceEntryType(CommonConstant.INTERNAL_SERVICE_ENTRY);
                    } else {
                        serviceEntryDto.setServiceEntryType(CommonConstant.EXTERNAL_SERVICE_ENTRY);
                    }
                    serviceEntryDto.setCreateTime(svcEntry.getMetadata().getCreationTimestamp());
                    serviceEntryDto.setNamespace(svcEntry.getMetadata().getNamespace());
                    serviceEntryDtoList.add(serviceEntryDto);
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(serviceEntryDtoList);
    }

    @Override
    public ActionReturnUtil getServiceEntry(String clusterId, String serviceEntryName, String namespace) throws Exception {
        AssertUtil.notBlank(serviceEntryName, DictEnum.NAME);
        // 获取serviceList
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //获取分区
        NamespaceLocal namespaceDetail = namespaceLocalService.getNamespaceByNameAndClusterId(namespace,clusterId);
        if (Objects.isNull(namespaceDetail)) {
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_NOT_FOUND);
        }
        K8SClientResponse response = serviceEntryServices.getServiceEntryByName(namespace, null, null, HTTPMethod.GET, serviceEntryName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        ServiceEntry serviceEntry = JsonUtil.jsonToPojo(response.getBody(), ServiceEntry.class);
        ServiceEntryDto serviceEntryDto = new ServiceEntryDto();
        if (Objects.nonNull(serviceEntry)) {
            serviceEntryDto.setNamespace(serviceEntry.getMetadata().getNamespace());
            serviceEntryDto.setNamespaceName(namespaceDetail.getAliasName());
            serviceEntryDto.setClusterName(cluster.getAliasName());
            serviceEntryDto.setName(serviceEntry.getMetadata().getName());
            //获取服务域名
            List<String> hosts = serviceEntry.getSpec().getHosts();
            if (!CollectionUtils.isEmpty(hosts)) {
                String host = hosts.get(0);
                String domainName = host.substring(0, host.indexOf("."));
                serviceEntryDto.setHosts(domainName);
            }
            List<Map<String, Object>> addressList = serviceEntry.getSpec().getEndpoints();
            List<String> ipList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(addressList)) {
                for (Map<String, Object> addressItem : addressList) {
                    ipList.add(addressItem.get("address").toString());
                }
                serviceEntryDto.setIpList(ipList);
                serviceEntryDto.setServiceEntryType(CommonConstant.INTERNAL_SERVICE_ENTRY);
            } else {
                serviceEntryDto.setServiceEntryType(CommonConstant.EXTERNAL_SERVICE_ENTRY);
            }
            List<Port> portlist = serviceEntry.getSpec().getPorts();
            if (!CollectionUtils.isEmpty(portlist)) {
                serviceEntryDto.setProtocol(portlist.get(0).getProtocol());
                serviceEntryDto.setPort(portlist.get(0).getNumber().toString());
            }
        }
        return ActionReturnUtil.returnSuccessWithData(serviceEntryDto);
    }

    @Override
    public ActionReturnUtil listIstioOpenCluster() throws Exception {
        List<Cluster> clusters = roleLocalService.listCurrentUserRoleCluster();
        List<IstioClusterDto> istioOepnClusterList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(clusters)) {
            for (Cluster clusterItem : clusters) {
                IstioGlobalConfigure istioGlobalConfigure = istioGlobalConfigureMapper.getByClusterId(clusterItem.getId());
                if (Objects.nonNull(istioGlobalConfigure) && istioGlobalConfigure.getSwitchStatus() == CommonConstant.OPEN_GLOBAL_STATUS) {
                    IstioClusterDto clusterDto = new IstioClusterDto();
                    clusterDto.setClusterId(clusterItem.getId());
                    clusterDto.setClusterName(clusterItem.getAliasName());
                    istioOepnClusterList.add(clusterDto);
                }
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("istioOepnClusterList", istioOepnClusterList);
        return ActionReturnUtil.returnSuccessWithData(istioOepnClusterList);
    }

    /**
     * 组装服务
     *
     * @param serviceEntryDto
     * @param cluster
     * @return
     * @throws Exception
     */
    private com.harmonycloud.k8s.bean.Service makeService(ServiceEntryDto serviceEntryDto, Cluster cluster) throws Exception {

        // 创建service包含了创建所需的所有所需参数
        com.harmonycloud.k8s.bean.Service service = new com.harmonycloud.k8s.bean.Service();
        // “metedata”，为了防止与内部service重名，所以在name前加outsvc，namespace默认配置文件获取，labels若没有传入，则定义other，labels作用一是租户、二是区分类别
        ObjectMeta meta = new ObjectMeta();
        meta.setName(serviceEntryDto.getHosts());
        Map<String, Object> labels = new HashMap<String, Object>();
        if (serviceEntryDto.getLabels() != null) {
            labels = serviceEntryDto.getLabels();
        }
        labels.put(LABEL_PROJECT_ID, serviceEntryDto.getProjectId());
        meta.setNamespace(serviceEntryDto.getNamespace());
        meta.setLabels(labels);
        // 增加spec
        ServiceSpec serviceSpec = new ServiceSpec();
        List<ServicePort> ports = new ArrayList<ServicePort>();
        ServicePort servicePort = new ServicePort();
        if (Objects.nonNull(serviceEntryDto.getProtocol())) {
            servicePort.setName(serviceEntryDto.getProtocol().toLowerCase() + "-" + serviceEntryDto.getName());
        }
        servicePort.setPort(Integer.valueOf(serviceEntryDto.getPort()));
        servicePort.setTargetPort(Integer.valueOf(serviceEntryDto.getPort()));
        servicePort.setProtocol("TCP");
        ports.add(servicePort);
        serviceSpec.setPorts(ports);

        service.setMetadata(meta);
        service.setSpec(serviceSpec);
        return service;
    }

    /**
     * 组装 内部服务入口
     *
     * @param serviceEntryDto
     * @return
     */
    private ServiceEntry makeExtServiceEntry(ServiceEntryDto serviceEntryDto, String clusterIp) {
        ObjectMeta meta = new ObjectMeta();
        meta.setName(serviceEntryDto.getName());
        Map<String, Object> labels = new HashMap<String, Object>();
        if (serviceEntryDto.getLabels() != null) {
            labels = serviceEntryDto.getLabels();
        }
        labels.put(LABEL_PROJECT_ID, serviceEntryDto.getProjectId());
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
        Port port = new Port();
        port.setNumber(Integer.parseInt(serviceEntryDto.getPort()));
        port.setName(serviceEntryDto.getProtocol().toLowerCase() + "-" + serviceEntryDto.getName());
        port.setProtocol(serviceEntryDto.getProtocol());
        ports.add(port);

        serviceEntrySpec.setPorts(ports);
        serviceEntrySpec.setResolution("STATIC");
        List<Map<String, Object>> endpoints = new ArrayList<>();
        if (Objects.nonNull(serviceEntryDto.getIpList())) {
            for (String ipItem : serviceEntryDto.getIpList()) {
                Map<String, Object> addresses = new HashMap<>();
                addresses.put("address", ipItem);
                endpoints.add(addresses);
            }
        }
        serviceEntrySpec.setEndpoints(endpoints);
        serviceEntry.setSpec(serviceEntrySpec);
        return serviceEntry;

    }

    /**
     * 组装外部服务入口的serviceEntry
     *
     * @param serviceEntryDto
     * @return
     */
    private ServiceEntry makeServiceEntry(ServiceEntryDto serviceEntryDto) {
        ObjectMeta meta = new ObjectMeta();
        meta.setName(serviceEntryDto.getName());
        Map<String, Object> labels = new HashMap<String, Object>();
        if (serviceEntryDto.getLabels() != null) {
            labels = serviceEntryDto.getLabels();
        }
        labels.put(LABEL_PROJECT_ID, serviceEntryDto.getProjectId());
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
        hosts.add(serviceEntryDto.getHosts());
        serviceEntrySpec.setHosts(hosts);
        serviceEntrySpec.setLocation(CommonConstant.MESH_EXTERNAL);
        List<Port> ports = new ArrayList<>();
        Port port = new Port();
        port.setNumber(Integer.parseInt(serviceEntryDto.getPort()));
        if (Objects.nonNull(serviceEntryDto.getProtocol())) {
            port.setName(serviceEntryDto.getProtocol().toLowerCase() + "-" + serviceEntryDto.getName());
        }
        port.setProtocol(serviceEntryDto.getProtocol());
        ports.add(port);
        serviceEntrySpec.setPorts(ports);
        serviceEntrySpec.setResolution("DNS");
        serviceEntry.setSpec(serviceEntrySpec);
        return serviceEntry;
    }


}
