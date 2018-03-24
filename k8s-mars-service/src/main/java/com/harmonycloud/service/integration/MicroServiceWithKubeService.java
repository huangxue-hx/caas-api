package com.harmonycloud.service.integration;

import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.bean.Service;
import com.harmonycloud.service.platform.bean.microservice.MsfDeployment;

import java.util.List;
import java.util.Map;

/**
 * @Author jiangmi
 * @Description  微服务与kubernetes交互
 * @Date created in 2017-12-12
 * @Modified
 */
public interface MicroServiceWithKubeService {

    /**
     * 创建configmap
     * @param namespaceName
     * @param kubeDep
     * @param cluster
     * @throws Exception
     */
    public void createConfigmap(String namespaceName, Deployment kubeDep, Cluster cluster) throws Exception;

    /**
     * 创建微服务组件的Deployment
     * @param deployment
     * @param service
     * @param namespace
     * @param cluster
     * @throws Exception
     */
    public void createApp(Deployment deployment, Service service, String namespace, Cluster cluster) throws Exception;

    /**
     * 更新系统统一暴露（nginx）的configmap
     * @param cluster
     * @param namespace
     * @param serviceName
     * @param deployment
     * @throws Exception
     */
    public void updateSystemExposeConfigMap(Cluster cluster, String namespace, String serviceName, MsfDeployment deployment, String consulPort) throws Exception;

    /**
     * 生成微服务组件kong的环境变量值
     * @return
     * @throws Exception
     */
    public String generateKongEnvValue(String namespace) throws Exception;

    /**
     * 创建http ingress
     * @param dep
     * @param namespace
     * @throws Exception
     */
    public void createHttpIngress(MsfDeployment dep, String namespace, Cluster cluster) throws Exception;

    /**
     * 获取微服务对外访问信息
     * @param depName
     * @param namespace
     * @param cluster
     * @param service
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> getExternalInfo(String depName, String namespace, Cluster cluster, com.harmonycloud.k8s.bean.Service service) throws Exception;

    /**
     * 删除微服务组件对应的Deployment
     * @param namespace
     * @param depName
     * @param cluster
     * @param serviceName
     * @return
     * @throws Exception
     */
    public boolean deleteMsfDeployment(String namespace, String depName, Cluster cluster, String serviceName, String consulPort) throws Exception;

    public boolean deleteTcpUdpRule(String type, Cluster cluster, String namespace, String serviceName) throws Exception;

    String getConsulExposePort(List<MsfDeployment> msfDeployments, Cluster cluster, String namespace) throws Exception;
}
