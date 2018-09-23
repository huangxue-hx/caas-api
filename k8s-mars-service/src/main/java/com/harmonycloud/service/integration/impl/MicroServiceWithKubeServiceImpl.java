package com.harmonycloud.service.integration.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dto.application.TcpRuleDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.ClusterDomain;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.ServicesService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.NodePortClusterUsageService;
import com.harmonycloud.service.integration.MicroServiceWithKubeService;
import com.harmonycloud.service.platform.bean.microservice.MsfDeployment;
import com.harmonycloud.service.platform.bean.microservice.MsfDeploymentPort;
import com.harmonycloud.service.platform.bean.microservice.MsfDeploymentVolume;
import com.harmonycloud.service.platform.constant.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author jiangmi
 * @Description 微服务与kubernetes的交互
 * @Date created in 2017-12-12
 * @Modified
 */
@Service
public class MicroServiceWithKubeServiceImpl implements MicroServiceWithKubeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroServiceWithKubeServiceImpl.class);

    @Autowired
    private RouterService routerService;

    @Autowired
    private DeploymentService dpService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private NodePortClusterUsageService portUsageService;

    @Autowired
    private ServicesService sService;

    @Override
    public void createConfigmap(String namespaceName, Deployment kubeDep, Cluster cluster) throws Exception {
        Map<String, Object> annotation = kubeDep.getMetadata().getAnnotations();
        if (annotation != null && annotation.get("springcloud.params/volumes") != null) {
            List<MsfDeploymentVolume> deploymentVolumes = (List<MsfDeploymentVolume>) annotation.get("springcloud.params/volumes");
            for (int i = 0; i < deploymentVolumes.size(); i++) {
                MsfDeploymentVolume dv = deploymentVolumes.get(i);
                String content = downLoadFile(dv.getFile_url());
                String fileName = dv.getFile_url().substring(dv.getFile_url().lastIndexOf("/") + 1);

                //判断是否已存在同名的configmap
                K8SURL url = new K8SURL();
                url.setNamespace(namespaceName).setResource(Resource.CONFIGMAP);
                K8SURL url1 = new K8SURL();
                url1.setNamespace(namespaceName).setResource(Resource.CONFIGMAP).setName(kubeDep.getMetadata().getName() + i);
                K8SClientResponse responses = new K8sMachineClient().exec(url1, HTTPMethod.GET, null, null, cluster);
                Map<String, Object> convertJsonToMap = JsonUtil.convertJsonToMap(responses.getBody());
                String metadata = convertJsonToMap.get(CommonConstant.METADATA).toString();
                if (!CommonConstant.EMPTYMETADATA.equals(metadata)) {
                    throw new Exception("configmap=" + kubeDep.getMetadata().getName() + i + " 已经存在");
                }

                //创建configmap
                Map<String, Object> bodys = new HashMap<String, Object>();
                Map<String, Object> meta = new HashMap<String, Object>();
                meta.put("namespace", namespaceName);
                meta.put("name", kubeDep.getMetadata().getName() + i);
                Map<String, Object> label = new HashMap<String, Object>();
                label.put("app", kubeDep.getMetadata().getName());
                meta.put("labels", label);
                bodys.put("metadata", meta);
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(fileName, content);
                bodys.put("data", data);
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Content-type", "application/json");
                K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
                if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                    UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                    throw new Exception(status.getMessage());
                }
            }
        }
    }

    @Override
    public void createApp(Deployment deployment, com.harmonycloud.k8s.bean.Service service, String namespace, Cluster cluster) throws Exception {
        //创建组件的Deployment
        K8SURL k8surl = new K8SURL();
        k8surl.setNamespace(namespace).setResource(Resource.DEPLOYMENT);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys = CollectionUtil.transBean2Map(deployment);
        K8SClientResponse response = new K8sMachineClient().exec(k8surl, HTTPMethod.POST, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new Exception(status.getMessage());
        }

        //创建组件的Service
        k8surl.setNamespace(namespace).setResource(Resource.SERVICE);
        bodys.clear();
        bodys = CollectionUtil.transBean2Map(service);
        K8SClientResponse sResponse = new K8sMachineClient().exec(k8surl, HTTPMethod.POST, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(sResponse.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(sResponse.getBody(), UnversionedStatus.class);
            throw new Exception(status.getMessage());
        }
    }

    @Override
    public void updateSystemExposeConfigMap(Cluster cluster, String namespace, String serviceName, MsfDeployment depContent, String consulExposePort) throws Exception {
        List<MsfDeploymentPort> msfPorts = depContent.getPorts();

        //根据协议进行分组
        List<TcpRuleDto> tcpRules = new ArrayList<>();
        List<TcpRuleDto> udpRules = new ArrayList<>();
        if (msfPorts != null && msfPorts.size() > 0) {
            for (MsfDeploymentPort pp : msfPorts) {
                TcpRuleDto rule = new TcpRuleDto();
                rule.setPort(pp.getExpose_port());
                rule.setTargetPort(pp.getContainer_port());
                rule.setProtocol(pp.getExternal_type());
                if (Constant.EXTERNAL_PROTOCOL_TCP.equals(pp.getExternal_type())) {
                    tcpRules.add(rule);
                }
                if (Constant.EXTERNAL_PROTOCOL_UDP.equals(pp.getExternal_type()) && StringUtils.isBlank(consulExposePort)) {
                    udpRules.add(rule);
                }
            }
        }
        //routerService.updateSystemExposeConfigmap(cluster, namespace, serviceName, icName, tcpRules, Constant.PROTOCOL_TCP);
        //routerService.updateSystemExposeConfigmap(cluster, namespace, serviceName, icName, udpRules, Constant.PROTOCOL_UDP);
    }

    @Override
    public String generateKongEnvValue(String namespace) throws Exception {
        return clusterService.getEntry(namespace);
    }

    @Override
    public void createHttpIngress(MsfDeployment dep, String namespace, Cluster cluster) throws Exception {
        List<MsfDeploymentPort> msfPorts = dep.getPorts();

        //去除tcp和udp的暴露类型
        Iterator<MsfDeploymentPort> iterator = msfPorts.iterator();
        while (iterator.hasNext()) {
            MsfDeploymentPort onePort = iterator.next();
            String type = onePort.getExternal_type();
            if (!Constant.EXTERNAL_PROTOCOL_HTTP.equals(type)) {
                iterator.remove();
            }
        }
        if (msfPorts != null && msfPorts.size() > 0) {
            //获取四级域名
            String fourDomain = null;
            List<ClusterDomain> domains = clusterService.findDomain(namespace);
            for (ClusterDomain domain : domains) {
                if (Constant.CLUSTER_FOUR_DOMAIN.equals(domain.getName())) {
                    fourDomain = domain.getDomain();
                    break;
                }
            }
            //组装ingress对象
            Ingress ingress = new Ingress();
            ObjectMeta meta = new ObjectMeta();
            meta.setName(dep.getMetadata().getDeployment_name() + CommonConstant.LINE + namespace);
            Map<String, Object> labels = new HashMap<>();
            labels.put(Constant.TYPE_DEPLOYMENT, dep.getMetadata().getDeployment_name());
            meta.setLabels(labels);
            ingress.setSpec(new IngressSpec());

            //一个ingress多个path增加annotation
            Map<String, Object> annotation = new HashMap<>();
            annotation.put("nginx.ingress.kubernetes.io/force-ssl-redirect", "false");
            annotation.put("nginx.ingress.kubernetes.io/rewrite-target", "/");
            annotation.put("nginx.ingress.kubernetes.io/ssl-redirect", "false");
            meta.setAnnotations(annotation);
            ingress.setMetadata(meta);
            List<HTTPIngressPath> path = new ArrayList<HTTPIngressPath>();
            for (MsfDeploymentPort pp : msfPorts) {
                HTTPIngressPath httpIngressPath = new HTTPIngressPath();
                IngressBackend backend = new IngressBackend();
                backend.setServiceName(dep.getSpec().getService_name());
                backend.setServicePort(Integer.valueOf(pp.getService_port()));
                httpIngressPath.setBackend(backend);
                httpIngressPath.setPath(pp.getHttp_path());
                path.add(httpIngressPath);
            }
            IngressRule ingressRule = new IngressRule();
            ingressRule.setHost(dep.getMetadata().getDeployment_name() + CommonConstant.LINE + namespace + CommonConstant.DOT + fourDomain);
            HTTPIngressRuleValue http = new HTTPIngressRuleValue();
            http.setPaths(path);
            ingressRule.setHttp(http);
            ingress.getSpec().setRules(new ArrayList<>());
            ingress.getSpec().getRules().add(ingressRule);
            Map<String, Object> body = CollectionUtil.transBean2Map(ingress);
            K8SURL url = new K8SURL();
            Map<String, Object> head = new HashMap<String, Object>();
            head.put("Content-Type", "application/json");
            url.setNamespace(namespace).setResource(Resource.INGRESS);
            K8SClientResponse k = new K8sMachineClient().exec(url, HTTPMethod.POST, head, body, cluster);
            if (!HttpStatusUtil.isSuccessStatus(k.getStatus())) {
                LOGGER.error("微服务创建ingress失败,{}", new String[]{k.getBody()});
                throw new Exception(dep.getMetadata().getDeployment_name() + " ingress 创建失败");
            }
        }
    }

    @Override
    public List<Map<String, Object>> getExternalInfo(String depName, String namespace, Cluster cluster, com.harmonycloud.k8s.bean.Service service) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        List<ServicePort> servicePorts = service.getSpec().getPorts();
        //获取nginx confimap
        //ConfigMap configMap = routerService.getSystemExposeConfigmap(icName, cluster, Constant.PROTOCOL_TCP);
        //获取ip
        String ip = clusterService.getEntry(namespace);
        //result.addAll(getTcpUdpExternalInfo(configMap, namespace, depName, servicePorts, ip, Constant.PROTOCOL_TCP));
        //configMap = routerService.getSystemExposeConfigmap(icName, cluster, Constant.PROTOCOL_UDP);
        //result.addAll(getTcpUdpExternalInfo(configMap, namespace, depName, servicePorts, ip, Constant.PROTOCOL_UDP));
        //获取ingress
        List<Ingress> list = routerService.listHttpIngress(depName, namespace, cluster);
        if (list != null && list.size() > 0) {
            //获取域名
            List<ClusterDomain> domains = clusterService.findDomain(namespace);
            for (Ingress in : list) {
                List<IngressRule> rules = in.getSpec().getRules();
                if (CollectionUtils.isNotEmpty(rules)) {
                    IngressRule rule = rules.get(0);
                    HTTPIngressRuleValue http = rule.getHttp();
                    List<HTTPIngressPath> paths = http.getPaths();
                    if (CollectionUtils.isNotEmpty(paths)) {
                        //获取四级域名端口
                        Integer port = Constant.LIVENESS_PORT;
                        for (ClusterDomain clusterDomain : domains) {
                            if (Constant.CLUSTER_FOUR_DOMAIN.equals(clusterDomain.getDomain())) {
                                port = clusterDomain.getPort();
                                break;
                            }
                        }
                        for (HTTPIngressPath path : paths) {
                            if (path.getBackend() != null) {
                                Map<String, Object> tmp = new HashMap<>();
                                tmp.put("type", Constant.EXTERNAL_PROTOCOL_HTTP);
                                tmp.put("container_port", String.valueOf(path.getBackend().getServicePort()));

                                if (StringUtils.isNotBlank(path.getPath())) {
                                    if (path.getPath().lastIndexOf("/") != path.getPath().length() - CommonConstant.NUM_ONE) {
                                        path.setPath(path.getPath() + "/");
                                    }
                                }
                                //查找端口名称
                                for (ServicePort sPort : servicePorts) {
                                    if (sPort.getPort().intValue() == path.getBackend().getServicePort().intValue()) {
                                        tmp.put("port_name", sPort.getName());
                                    }
                                }
                                String hostName = StringUtils.isNotBlank(path.getPath()) ? rule.getHost() + CommonConstant.COLON + port + path.getPath() : rule.getHost() + CommonConstant.COLON + port;
                                tmp.put("hostname", hostName);
                                result.add(tmp);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean deleteMsfDeployment(String namespace, String depName, Cluster cluster, String serviceName, String consulPort) throws Exception {
        //删除configmap
        K8SURL cUrl = new K8SURL();
        cUrl.setNamespace(namespace).setResource(Resource.CONFIGMAP);
        Map<String, Object> queryP = new HashMap<>();
        queryP.put("labelSelector", "app=" + depName);
        K8SClientResponse conRes = new K8sMachineClient().exec(cUrl, HTTPMethod.DELETE, null, queryP, cluster);
        if (!HttpStatusUtil.isSuccessStatus(conRes.getStatus()) && conRes.getStatus() != Constant.HTTP_404) {
            LOGGER.error("删除configmap失败,{}", new String[]{conRes.getBody()});
            return false;
        }
        if (StringUtils.isBlank(consulPort) && !deleteTcpUdpRule(Constant.PROTOCOL_UDP, cluster, namespace, serviceName)) {
            return false;
        }

        if (!deleteTcpUdpRule(Constant.PROTOCOL_TCP, cluster, namespace, serviceName)) {
            return false;
        }

        //删除deployment
        K8SClientResponse delRes = dpService.doSpecifyDeployment(namespace, depName, null, null, HTTPMethod.DELETE, cluster);
        if (!HttpStatusUtil.isSuccessStatus(delRes.getStatus()) && delRes.getStatus() != Constant.HTTP_404) {
            LOGGER.error("删除deployment失败,{}", new String[]{delRes.getBody()});
            return false;
        }

        // 删除ingress
        cUrl.setResource(Resource.INGRESS);
        K8SClientResponse ingRes = new K8sMachineClient().exec(cUrl, HTTPMethod.DELETE, null, queryP, cluster);
        if (!HttpStatusUtil.isSuccessStatus(ingRes.getStatus()) && ingRes.getStatus() != Constant.HTTP_404) {
            LOGGER.error("删除Ingress失败,{}", new String[]{ingRes.getBody()});
            return false;
        }

        //删除service
        K8SURL svcUrl = new K8SURL();
        svcUrl.setNamespace(namespace).setResource(Resource.SERVICE);
        svcUrl.setName(serviceName);
        K8SClientResponse serviceRes = new K8sMachineClient().exec(svcUrl, HTTPMethod.DELETE, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(serviceRes.getStatus()) && serviceRes.getStatus() != Constant.HTTP_404) {
            LOGGER.error("删除service失败,{}", new String[]{serviceRes.getBody()});
            return false;
        }
        return true;
    }


    /**
     * 下载文件
     *
     * @param downloadUrl
     * @return
     * @throws Exception
     */
    private String downLoadFile(String downloadUrl) throws Exception {
        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(3 * 1000);
        InputStream inputStream = connection.getInputStream();
        StringBuilder res = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line = null;
        while ((line = in.readLine()) != null) {
            res.append(line);
        }
        in.close();
        return res.toString();
    }

    private List<Map<String, Object>> getTcpUdpExternalInfo(ConfigMap configMap, String namespace, String service, List<ServicePort> servicePorts, String ip, String type) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> data = (Map<String, Object>) configMap.getData();
        if (data != null) {
            for (Map.Entry<String, Object> map : data.entrySet()) {
                String value = map.getValue().toString();
                if (value.indexOf(namespace + "/" + service) > -1) {
                    Map<String, Object> tmp = new HashMap<>();
                    tmp.put("type", type);
                    String prefix = namespace + "/" + service + ":";
                    String port = value.substring(value.indexOf(prefix) + prefix.length());
                    for (ServicePort sPort : servicePorts) {
                        if (sPort.getPort().intValue() == Integer.valueOf(port).intValue()) {
                            tmp.put("port_name", sPort.getName());
                        }
                    }
                    tmp.put("container_port", port);
                    tmp.put("hostname", ip + ":" + map.getKey());
                    result.add(tmp);
                }
            }
        }
        return result;
    }

    @Override
    public boolean deleteTcpUdpRule(String type, Cluster cluster, String namespace, String serviceName) throws Exception {
        //更新系统configmap
        ConfigMap configMap = null;//routerService.getSystemExposeConfigmap(icName, cluster, type);
        if (configMap != null) {
            Map<String, Object> data = (Map<String, Object>) configMap.getData();
            if (data == null) {
                return true;
            }
            Iterator<Map.Entry<String, Object>> it = data.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                String value = entry.getValue().toString();
                if (value.indexOf(namespace + "/" + serviceName) > -1) {
                    //释放tcp和udp对外端口
                    portUsageService.deleteNodePortUsage(cluster.getId(), Integer.valueOf(entry.getKey()));
                    it.remove();
                }
            }
            //更新configmap
            configMap.setData(data);
            Map<String, Object> headers = new HashMap<String, Object>();
            headers.put("Content-type", "application/json");
            Map<String, Object> bodys = new HashMap<String, Object>();
            bodys = CollectionUtil.transBean2Map(configMap);
            K8SURL url = new K8SURL();
            url.setNamespace(CommonConstant.KUBE_SYSTEM).setResource(Resource.CONFIGMAP);
            if (Constant.PROTOCOL_TCP.equals(type)) {
                url.setName(Constant.EXPOSE_CONFIGMAP_NAME_TCP);
            }
            if (Constant.PROTOCOL_UDP.equals(type)) {
                url.setName(Constant.EXPOSE_CONFIGMAP_NAME_UDP);
            }
            K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                LOGGER.error("更新系统对外configmap失败：{}", response.getBody());
                return false;
            }
        }
        return true;
    }

    @Override
    public String getConsulExposePort(List<MsfDeployment> msfDeployments, Cluster cluster, String namespace) throws Exception {
        String nginxPort = null;
        for (MsfDeployment deployment : msfDeployments) {
            String svcName = deployment.getSpec().getService_name();
            //如果是consul组件或者kong组件，则需要先获取consul组件暴露在nginx上的udp端口
            if (svcName.equals(Constant.SPRINGCLOUD_CONSUL) || svcName.equals(Constant.SPRINGCLOUD_KONG)) {
                //获取集群内的consul
                K8SClientResponse sRes = sService.doSepcifyService(namespace, Constant.SPRINGCLOUD_CONSUL, null, null, HTTPMethod.GET, cluster);
                if (!HttpStatusUtil.isSuccessStatus(sRes.getStatus())) {
                    throw new Exception(sRes.getBody());
                }
                com.harmonycloud.k8s.bean.Service svc = JsonUtil.jsonToPojo(sRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
                List<ServicePort> ports = svc.getSpec().getPorts();
                Integer udpPort = ports.stream().filter(sp -> Constant.PROTOCOL_UDP.equals(sp.getProtocol())).collect(Collectors.toList()).get(0).getPort();
                ConfigMap configMap = null;//routerService.getSystemExposeConfigmap(icName, cluster, Constant.PROTOCOL_UDP);
                for (Map.Entry<String, Object> map : ((Map<String, Object>) configMap.getData()).entrySet()) {
                    String value = map.getValue().toString();
                    String newValue = namespace + CommonConstant.SLASH + Constant.SPRINGCLOUD_CONSUL + CommonConstant.COLON + String.valueOf(udpPort);
                    if (value.equals(newValue)) {
                        nginxPort = map.getKey();
                        break;
                    }
                }
            }
        }
        return nginxPort;
    }
}
