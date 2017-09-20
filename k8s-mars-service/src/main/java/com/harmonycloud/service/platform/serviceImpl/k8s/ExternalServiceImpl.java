package com.harmonycloud.service.platform.serviceImpl.k8s;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.external.*;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.ServiceList;
import com.harmonycloud.k8s.bean.ServicePort;
import com.harmonycloud.k8s.bean.ServiceSpec;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.ServicesService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.platform.service.EndpointService;
import com.harmonycloud.service.platform.service.ExternalService;
import com.harmonycloud.service.tenant.TenantService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExternalServiceImpl implements ExternalService {

    @Autowired
    private ServicesService sService;

    @Autowired
    private EndpointService eService;

    @Autowired
    private TenantService tenantService;

    /**
     * 增加外部服务
     */
    @Override
    public ActionReturnUtil svcCreate(ExternalServiceBean externalServiceBean) throws Exception {
        if (externalServiceBean == null) {
            return ActionReturnUtil.returnErrorWithMsg("external service cannot be null!");
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
        meta.setNamespace(Resource.EXTERNALNAMESPACE);
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
        K8SClientResponse response = sService.doServiceByNamespace(Resource.EXTERNALNAMESPACE, head, bodys, HTTPMethod.POST);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
        	UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }

        // 创建endpoint
        // 创建svc service包含了创建所需的所有所需参数
        com.harmonycloud.dto.external.Endpoint endpoint = new com.harmonycloud.dto.external.Endpoint();
        ObjectMeta metadate = new ObjectMeta();
        metadate.setName(meta.getName());
        metadate.setNamespace(Resource.EXTERNALNAMESPACE);
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
        K8SClientResponse eptresponse = eService.doEndpointByNamespace(Resource.EXTERNALNAMESPACE, head, eptbodys, HTTPMethod.POST);
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
    public ActionReturnUtil deleteOutService(String name) throws Exception {
        if (StringUtils.isEmpty(name)) {
            return ActionReturnUtil.returnErrorWithMsg("name cannot be null!");
        }
        K8SURL url = new K8SURL();
        url.setResource(Resource.SERVICE).setNamespace(Resource.EXTERNALNAMESPACE).setSubpath(name);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnSuccess();
        }
        return ActionReturnUtil.returnErrorWithMsg("删除出错");
    }
    /**
     * 根据tenant删除外部服务
     */
    @Override
    public ActionReturnUtil deleteOutServicebytenant(String tenantName, String tenantId) throws Exception {
        if (StringUtils.isEmpty(tenantName) || StringUtils.isEmpty(tenantId)) {
            return ActionReturnUtil.returnErrorWithMsg("tenantName 或者tenantid 不能为空!");
        }
        // 根据tenantid获取cluster
        Cluster cluster = tenantService.getClusterByTenantid(tenantId);
        // 获取serviceList
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys.put("labelSelector", "name" + "=" + tenantName);
        K8SClientResponse response = sService.doServiceByNamespace(Resource.EXTERNALNAMESPACE, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
        	UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
        }
        ServiceList svList = JsonUtil.jsonToPojo(response.getBody(), ServiceList.class);
        // item为实际service list的metadata，spec，status
        List<com.harmonycloud.k8s.bean.Service> services = svList.getItems();
        if (services != null && services.size() > 0) {
            for (int i = 0; i < services.size(); i++) {
                com.harmonycloud.k8s.bean.Service svc = services.get(i);
                String name = svc.getMetadata().getName();
                K8SURL url = new K8SURL();
                url.setResource(Resource.SERVICE).setNamespace(Resource.EXTERNALNAMESPACE).setSubpath(name);
                K8SClientResponse responsed = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null);
                if (!HttpStatusUtil.isSuccessStatus(responsed.getStatus())) {
                	UnversionedStatus sta = JsonUtil.jsonToPojo(responsed.getBody(), UnversionedStatus.class);
                    return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
                }
            }
        }
        return ActionReturnUtil.returnSuccess();
    }
    /**
     * 更新外部服务
     */
    @Override
    public ActionReturnUtil updateOutService(ExternalServiceBean externalServiceBean) throws Exception {
        if (externalServiceBean == null) {
            return ActionReturnUtil.returnErrorWithMsg("external service cannot be null!");
        }
        // 查询
        K8SURL url1 = new K8SURL();
        url1.setNamespace(Resource.EXTERNALNAMESPACE).setResource(Resource.SERVICE).setSubpath(externalServiceBean.getName());
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("Content-Type", "application/json");
        //new K8sMachineClient().exec   
        Cluster cluster = tenantService.getClusterByTenantid(externalServiceBean.getTenantid());
        K8SClientResponse serivceResponse = new K8sMachineClient().exec(url1, HTTPMethod.GET, head, null,cluster);
        com.harmonycloud.k8s.bean.Service service = K8SClient.converToBean(serivceResponse, com.harmonycloud.k8s.bean.Service.class);
        String apiVersion = service.getApiVersion();
        String kind = service.getKind();
        ObjectMeta metadata = service.getMetadata();
        // 更新metadata
        Map<String, Object> labels = externalServiceBean.getLabels();
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
        url.setNamespace(Resource.EXTERNALNAMESPACE).setResource(Resource.SERVICE).setSubpath(externalServiceBean.getName());
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
        meta.setNamespace(Resource.EXTERNALNAMESPACE);

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
        eurl.setNamespace(Resource.EXTERNALNAMESPACE).setResource(Resource.ENDPOINT).setSubpath(externalServiceBean.getName());
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
    public ActionReturnUtil getListOutService(String tenant,String tenantId) throws Exception {
        if (StringUtils.isEmpty(tenant)) {
            return ActionReturnUtil.returnErrorWithMsg("tenant cannot be null!");
        }
        Cluster cluster = tenantService.getClusterByTenantid(tenantId);
        // 获取serviceList
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys.put("labelSelector", "name" + "=" + tenant);
        K8SClientResponse response = sService.doServiceByNamespace(Resource.EXTERNALNAMESPACE, null, bodys, HTTPMethod.GET,cluster);
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
                ExternalSvc externalSvc = new ExternalSvc();
                com.harmonycloud.k8s.bean.Service svc = services.get(i);
                if (svc.getMetadata().getLabels() != null && svc.getMetadata().getLabels().get("name").toString().equals(tenant)) {
                    // zgl 修改
                    externalSvc.setServiceName(svc.getMetadata().getName());
                    externalSvc.setPort(svc.getSpec().getPorts().get(0).getTargetPort());
                    externalSvc.setTime(svc.getMetadata().getCreationTimestamp());
                    externalSvc.setIp(svc.getMetadata().getLabels().get("ip") == null ? "" : svc.getMetadata().getLabels().get("ip").toString());
                    externalSvc.setType(svc.getMetadata().getLabels().get("type") == null ? "" : svc.getMetadata().getLabels().get("type").toString());
                    externalSvc.setName(svc.getMetadata().getLabels().get("name") == null ? "" : svc.getMetadata().getLabels().get("name").toString());
                    externalSvcs.add(externalSvc);
                }

            }
        }
        return ActionReturnUtil.returnSuccessWithData(externalSvcs);
    }

    /**
     * 根据label获取外部服务
     */
    @Override
    public ActionReturnUtil getListOutServiceByLabel(String labels) throws Exception {
        if (StringUtils.isEmpty(labels)) {
            return ActionReturnUtil.returnErrorWithMsg("labels cannot be null!");
        }
        String tenantName = labels.substring(5, labels.indexOf("type="));
        String type = labels.substring(labels.indexOf("type=") + 5);
        if (StringUtils.isEmpty(tenantName)) {
            return ActionReturnUtil.returnErrorWithMsg("tenantName cannot be null!");
        }
        if (StringUtils.isEmpty(type)) {
            return ActionReturnUtil.returnErrorWithMsg("type cannot be null!");
        }
//        Map<String, Object> bodys = new HashMap<String, Object>();
//        bodys.put("labelSelector", "name" + "=" + tenant);
        // 获取serviceList
        K8SClientResponse response = sService.doServiceByNamespace(Resource.EXTERNALNAMESPACE, null, null, HTTPMethod.GET);
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
                Map<String, Object> label = services.get(i).getMetadata().getLabels();
                ExternalSvc externalSvc = new ExternalSvc();
                com.harmonycloud.k8s.bean.Service svc = services.get(i);
                if (svc.getMetadata().getLabels() != null && tenantName.equals(label.get("name")) && type.equals(label.get("type"))) {
                    // zgl 修改
                    externalSvc.setServiceName(svc.getMetadata().getName());
                    externalSvc.setPort(svc.getSpec().getPorts().get(0).getTargetPort());
                    externalSvc.setTime(svc.getMetadata().getCreationTimestamp());
                    externalSvc.setIp(svc.getMetadata().getLabels().get("ip") == null ? "" : svc.getMetadata().getLabels().get("ip").toString());
                    externalSvc.setType(svc.getMetadata().getLabels().get("type") == null ? "" : svc.getMetadata().getLabels().get("type").toString());
                    externalSvc.setName(svc.getMetadata().getLabels().get("name") == null ? "" : svc.getMetadata().getLabels().get("name").toString());
                    externalSvcs.add(externalSvc);
                    // externalSvc.setServiceName(svc.getMetadata().getName());
                    // externalSvc.setIp(svc.getMetadata().getLabels().get("ip").toString());
                    // externalSvc.setPort(svc.getSpec().getPorts().get(0).getTargetPort());
                    // externalSvc.setType(svc.getMetadata().getLabels().get("type").toString());
                    // externalSvc.setTime(svc.getMetadata().getCreationTimestamp());
                    // externalSvc.setDescribe(svc.getMetadata().getLabels().get("describe").toString());
                    // externalSvc.setName(svc.getMetadata().getLabels().get("name").toString());
                    // externalSvcs.add(externalSvc);
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(externalSvcs);
    }

    /**
     * 根据name获取外部服务
     */
    @Override
    public ActionReturnUtil getservicebyname(String name) throws Exception {

        if (StringUtils.isEmpty(name)) {
            return ActionReturnUtil.returnErrorWithMsg("name cannot be null!");
        }
        // 获取serviceList
        K8SClientResponse response = sService.doServiceByNamespace(Resource.EXTERNALNAMESPACE, null, null, HTTPMethod.GET);
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
                    externalSvcs.add(externalSvc);
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(externalSvcs);
    }
}