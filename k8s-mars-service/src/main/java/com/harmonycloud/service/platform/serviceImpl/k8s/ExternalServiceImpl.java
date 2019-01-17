package com.harmonycloud.service.platform.serviceImpl.k8s;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.application.ExternalTypeMapper;
import com.harmonycloud.dao.application.bean.ExternalTypeBean;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dto.external.*;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.ServicesService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.EndpointService;
import com.harmonycloud.service.platform.service.ExternalService;
import com.harmonycloud.service.user.RoleLocalService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.harmonycloud.service.platform.constant.Constant.LABEL_PROJECT_ID;
import static com.harmonycloud.service.platform.constant.Constant.LABEL_TYPE;

@Service
public class ExternalServiceImpl implements ExternalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalServiceImpl.class);

    @Autowired
    private ServicesService sService;

    @Autowired
    private EndpointService eService;
    @Autowired
    private RoleLocalService roleLocalService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private ExternalTypeMapper externalTypeMapper;

    /**
     * 增加外部服务
     */
    @Override
    public ActionReturnUtil createExtService(ExternalServiceBean externalServiceBean) throws Exception {
        if (externalServiceBean == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
        }
        // 创建svc service包含了创建所需的所有所需参数
        com.harmonycloud.k8s.bean.Service service = new com.harmonycloud.k8s.bean.Service();
        // ObejectMeta
        // “metedata”，为了防止与内部service重名，所以在name前加outsvc，namespace默认配置文件获取，labels若没有传入，则定义other，labels作用一是租户、二是区分类别
        ObjectMeta meta = new ObjectMeta();
        meta.setName(externalServiceBean.getName());
        Map<String, Object> labels = new HashMap<String, Object>();
        if (externalServiceBean.getLabels() != null) {
            labels = externalServiceBean.getLabels();
        }
        labels.put(LABEL_PROJECT_ID, externalServiceBean.getProjectId());
        meta.setNamespace(externalServiceBean.getNamespace());
        meta.setLabels(labels);
        // 增加spec
        ServiceSpec serviceSpec = new ServiceSpec();
        serviceSpec.setType(null);

        List<ServicePort> ports = new ArrayList<ServicePort>();
        ServicePort servicePort = new ServicePort();
        servicePort.setProtocol("TCP");
        servicePort.setPort(Integer.valueOf(externalServiceBean.getTargetPort()));
        servicePort.setTargetPort(Integer.valueOf(externalServiceBean.getTargetPort()));
        ports.add(servicePort);
        serviceSpec.setPorts(ports);

        service.setMetadata(meta);
        service.setSpec(serviceSpec);
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys = CollectionUtil.transBean2Map(service);
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("Content-Type", "application/json");
        Cluster cluster = clusterService.findClusterById(externalServiceBean.getClusterId());
        K8SClientResponse response = sService.doServiceByNamespace(externalServiceBean.getNamespace(), head, bodys, HTTPMethod.POST, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
        	UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }

        // 创建endpoint
        // 创建svc service包含了创建所需的所有所需参数
        com.harmonycloud.dto.external.Endpoint endpoint = new com.harmonycloud.dto.external.Endpoint();
        ObjectMeta metadate = new ObjectMeta();
        metadate.setName(meta.getName());
        metadate.setNamespace(externalServiceBean.getNamespace());
        List<Subsets> sbt = new ArrayList<Subsets>();
        Subsets subset = new Subsets();
        List<EndpointAddress> listEndpointAddress = new ArrayList<EndpointAddress>();
        EndpointAddress endpointAddress = new EndpointAddress();
        endpointAddress.setIp(externalServiceBean.getLabels().get("ip").toString());
        listEndpointAddress.add(endpointAddress);

        List<EndpointPort> listEndpointPort = new ArrayList<EndpointPort>();
        EndpointPort endpointPort = new EndpointPort();
        endpointPort.setPort(Integer.valueOf(externalServiceBean.getTargetPort()));
        listEndpointPort.add(endpointPort);
        subset.setAddresses(listEndpointAddress);
        subset.setPorts(listEndpointPort);
        sbt.add(subset);
        endpoint.setMetadata(metadate);
        endpoint.setSubsets(sbt);
        Map<String, Object> eptbodys = new HashMap<String, Object>();
        eptbodys = CollectionUtil.transBean2Map(endpoint);
        K8SClientResponse eptresponse = eService.doEndpointByNamespace(externalServiceBean.getNamespace(), head, eptbodys, HTTPMethod.POST, cluster);
        if (!HttpStatusUtil.isSuccessStatus(eptresponse.getStatus())) {
        	UnversionedStatus sta = JsonUtil.jsonToPojo(eptresponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        com.harmonycloud.k8s.bean.Service newService = JsonUtil.jsonToPojo(response.getBody(), com.harmonycloud.k8s.bean.Service.class);
        return ActionReturnUtil.returnSuccessWithData(newService);
    }
    /**
     * 删除外部服务
     */
    @Override
    public ActionReturnUtil deleteExtService(String clusterId, String name, String namespace) throws Exception {
        if (StringUtils.isBlank(name)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
        }
        Cluster cluster = clusterService.findClusterById(clusterId);
        K8SURL url = new K8SURL();
        url.setResource(Resource.SERVICE).setNamespace(namespace).setSubpath(name);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null,cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnSuccess();
        }
        return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DELETE_FAIL);
    }
    /**
     * 删除某个项目下的所有外部服务
     */
    @Override
    public ActionReturnUtil deleteExtServiceByProject(String projectId) throws Exception {
        if (StringUtils.isBlank(projectId)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
        }
        List<Cluster> clusters = clusterService.listCluster();
        // 获取serviceList
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("labelSelector", LABEL_PROJECT_ID + "=" + projectId);
        String errorMessage = "";
        for(Cluster cluster: clusters) {
            K8SClientResponse response = sService.doServiceByNamespace(null, null, bodys, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                LOGGER.error("获取外部服务失败:projectId:{},cluster:{},res:{}",
                        new String[]{projectId, cluster.getName(), JSONObject.toJSONString(sta)});
                errorMessage += cluster.getName() + ", ";
                continue;
            }
            ServiceList svList = JsonUtil.jsonToPojo(response.getBody(), ServiceList.class);
            // item为实际service list的metadata，spec，status
            List<com.harmonycloud.k8s.bean.Service> services = svList.getItems();
            if (services != null && services.size() > 0) {
                for (int i = 0; i < services.size(); i++) {
                    com.harmonycloud.k8s.bean.Service svc = services.get(i);
                    String name = svc.getMetadata().getName();
                    K8SClientResponse responsed = this.sService.doSepcifyService(svc.getMetadata().getNamespace(),name,null,null,HTTPMethod.DELETE,cluster);
                    if (!HttpStatusUtil.isSuccessStatus(responsed.getStatus())) {
                        LOGGER.error("删除外部服务失败:projectId:{},cluster:{},service:{},res:{}",
                                new String[]{projectId, cluster.getName(), name, JSONObject.toJSONString(responsed)});
                        errorMessage += cluster.getName() + "." + name + ", ";
                    }
                }
            }
        }
        if(StringUtils.isNotBlank(errorMessage)){
            throw new MarsRuntimeException(ErrorCodeMessage.EXT_SERVICE_DELETE_FAIL,errorMessage,Boolean.TRUE);
        }
        return ActionReturnUtil.returnSuccess();
    }
    /**
     * 更新外部服务
     */
    @Override
    public ActionReturnUtil updateExtService(ExternalServiceBean externalServiceBean) throws Exception {
        if (StringUtils.isBlank(externalServiceBean.getClusterId())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER,"clusterId",true);
        }
        // 查询
        K8SURL url1 = new K8SURL();
        url1.setNamespace(externalServiceBean.getNamespace()).setResource(Resource.SERVICE).setName(externalServiceBean.getName());
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("Content-Type", "application/json");
        Cluster cluster = clusterService.findClusterById(externalServiceBean.getClusterId());
        K8SClientResponse serivceResponse = new K8sMachineClient().exec(url1, HTTPMethod.GET, head, null,cluster);
        com.harmonycloud.k8s.bean.Service service = K8SClient.converToBean(serivceResponse, com.harmonycloud.k8s.bean.Service.class);
        String apiVersion = service.getApiVersion();
        String kind = service.getKind();
        ObjectMeta metadata = service.getMetadata();
        // 更新metadata
        Map<String, Object> labels = externalServiceBean.getLabels();
        labels.put(LABEL_PROJECT_ID, externalServiceBean.getProjectId());
        metadata.setLabels(labels);

        ServiceSpec spec = service.getSpec();
        // 更新spec.ports
        List<ServicePort> ports = new ArrayList<>();

        ServicePort port = new ServicePort();
        port.setPort(Integer.valueOf(externalServiceBean.getTargetPort()));
        port.setTargetPort(Integer.valueOf(externalServiceBean.getTargetPort()));
        port.setProtocol(externalServiceBean.getProtocol());
        ports.add(port);

        spec.setPorts(ports);
        Map<String, Object> body = new HashMap<>();
        body.put("metadata", metadata);
        body.put("spec", spec);
        body.put("kind", kind);
        body.put("apiVersion", apiVersion);
        K8SURL url = new K8SURL();
        url.setNamespace(externalServiceBean.getNamespace()).setResource(Resource.SERVICE).setSubpath(externalServiceBean.getName());
        head.put("Content-Type", "application/json");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, head, body,cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
        	UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        // 更新endpoint
        List<Subsets> sbt = new ArrayList<Subsets>();
        Subsets subset = new Subsets();
        List<EndpointAddress> listEndpointAddress = new ArrayList<EndpointAddress>();
        EndpointAddress endpointAddress = new EndpointAddress();
        endpointAddress.setIp(externalServiceBean.getLabels().get("ip").toString());
        listEndpointAddress.add(endpointAddress);

        ObjectMeta meta = new ObjectMeta();
        meta.setName(externalServiceBean.getName());
        meta.setNamespace(externalServiceBean.getNamespace());

        List<EndpointPort> listEndpointPort = new ArrayList<EndpointPort>();
        EndpointPort endpointPort = new EndpointPort();
        endpointPort.setPort(Integer.valueOf(externalServiceBean.getTargetPort()));
        listEndpointPort.add(endpointPort);
        subset.setAddresses(listEndpointAddress);
        subset.setPorts(listEndpointPort);
        sbt.add(subset);
        Map<String, Object> ebody = new HashMap<>();
        ebody.put("metadata", meta);
        ebody.put("subsets", sbt);
        ebody.put("kind", "Endpoints");
        ebody.put("apiVersion", apiVersion);
        K8SURL eurl = new K8SURL();
        eurl.setNamespace(externalServiceBean.getNamespace()).setResource(Resource.ENDPOINT).setSubpath(externalServiceBean.getName());
        head.put("Content-Type", "application/json");
        K8SClientResponse eresponse = new K8sMachineClient().exec(eurl, HTTPMethod.PUT, head, ebody,cluster);

        if (!HttpStatusUtil.isSuccessStatus(eresponse.getStatus())) {
        	UnversionedStatus sta = JsonUtil.jsonToPojo(eresponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }
    /**
     * 获取外部服务tenantName
     */
    @Override
    public ActionReturnUtil listExtService(String clusterId, String projectId, String serviceType) throws Exception {
        if (StringUtils.isBlank(projectId)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
        }
        List<Cluster> clusters = null;
        if(StringUtils.isBlank(clusterId)){
            clusters = roleLocalService.listCurrentUserRoleCluster();
        }else{
            clusters = new ArrayList<>();
            clusters.add(clusterService.findClusterById(clusterId));
        }
        List<ExternalSvc> externalSvcs = new ArrayList<>();
        // 获取serviceList
        Map<String, Object> bodys = new HashMap<>();
        if(StringUtils.isBlank(serviceType)) {
            bodys.put("labelSelector", LABEL_PROJECT_ID + "=" + projectId);
        }else{
            bodys.put("labelSelector", LABEL_PROJECT_ID + "=" + projectId + "," + LABEL_TYPE + "=" + serviceType);
        }
        for(Cluster cluster : clusters) {
            K8SClientResponse response = sService.doServiceByNamespace(null, null, bodys, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                if (Objects.isNull(sta)){
                    return ActionReturnUtil.returnErrorWithMsg(response.getBody());
                }
                return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
            }
            ServiceList svList = JsonUtil.jsonToPojo(response.getBody(), ServiceList.class);
            // item为实际service list的metadata，spec，status
            List<com.harmonycloud.k8s.bean.Service> services = svList.getItems();
            if (services != null && !services.isEmpty()) {
                for (int i = 0; i < services.size(); i++) {
                    ExternalSvc externalSvc = new ExternalSvc();
                    externalSvc.setClusterName(cluster.getName());
                    externalSvc.setClusterAliasName(cluster.getAliasName());
                    externalSvc.setClusterId(cluster.getId());
                    com.harmonycloud.k8s.bean.Service svc = services.get(i);                     // zgl 修改
                    externalSvc.setServiceName(svc.getMetadata().getName());
                    externalSvc.setPort(svc.getSpec().getPorts().get(0).getTargetPort());
                    externalSvc.setTime(svc.getMetadata().getCreationTimestamp());
                    externalSvc.setIp(svc.getMetadata().getLabels().get("ip") == null ? "" : svc.getMetadata().getLabels().get("ip").toString());
                    externalSvc.setType(svc.getMetadata().getLabels().get("type") == null ? "" : svc.getMetadata().getLabels().get("type").toString());
                    externalSvc.setName(svc.getMetadata().getLabels().get("name") == null ? "" : svc.getMetadata().getLabels().get("name").toString());
                    externalSvc.setNamespace(svc.getMetadata().getNamespace());
                    externalSvcs.add(externalSvc);

                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(externalSvcs);
    }

    /**
     * 根据name获取外部服务
     */
    @Override
    public ActionReturnUtil getExtService(String clusterId, String name,String namespace) throws Exception {
        AssertUtil.notBlank(name, DictEnum.NAME);
        // 获取serviceList
        Cluster cluster = clusterService.findClusterById(clusterId);
        K8SClientResponse response = sService.doServiceByNamespace(namespace, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
        	UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        ServiceList svList = JsonUtil.jsonToPojo(response.getBody(), ServiceList.class);
        // item为实际service list的metadata，spec，status
        List<com.harmonycloud.k8s.bean.Service> services = svList.getItems();
        List<ExternalSvc> externalSvcs = new ArrayList<ExternalSvc>();
        if (services != null && !services.isEmpty()) {
            for (int i = 0; i < services.size(); i++) {
                String serviceName = services.get(i).getMetadata().getName();
                ExternalSvc externalSvc = new ExternalSvc();
                com.harmonycloud.k8s.bean.Service svc = services.get(i);
                if (serviceName.equals(name)) {
                    externalSvc.setServiceName(svc.getMetadata().getName());
                    externalSvc.setIp(svc.getMetadata().getLabels().get("ip").toString());
                    externalSvc.setPort(svc.getSpec().getPorts().get(0).getTargetPort());
                    externalSvc.setType(svc.getMetadata().getLabels().get("type").toString());
                    externalSvc.setTime(svc.getMetadata().getCreationTimestamp());
                    externalSvc.setName(svc.getMetadata().getLabels().get("name").toString());
                    externalSvc.setNamespace(svc.getMetadata().getNamespace());
                    externalSvcs.add(externalSvc);
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(externalSvcs);
    }

    @Override
    public List<ExternalTypeBean> listExtServiceType() throws Exception{
        return externalTypeMapper.list();
    }
}
