package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cluster.IngressControllerDto;
import com.harmonycloud.k8s.bean.Ingress;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.k8s.bean.ConfigMap;
import com.harmonycloud.service.platform.bean.RouterSvc;

import java.util.List;
import java.util.Map;


/**
 * Created by czm on 2017/1/18.
 */
public interface RouterService {

    public ActionReturnUtil ingCreate(ParsedIngressListDto parsedIngressList) throws Exception;

    public ActionReturnUtil ingUpdate(ParsedIngressListUpdateDto parsedIngressList) throws Exception;

    public ActionReturnUtil ingDelete(String namespace, String name, String depName, String serviceType) throws Exception;

    public ActionReturnUtil svcList(String namespace) throws Exception;

    /**
     * 删除服务时删除tcp和udp规则
     * @param namespace
     * @param name
     * @param icList
     * @param cluster
     * @throws Exception
     */
    void deleteRulesByName(String namespace, String name, List<IngressControllerDto> icList, Cluster cluster) throws Exception;

    public ActionReturnUtil svcUpdate(SvcRouterUpdateDto svcRouterUpdate) throws Exception;

    /**
     * 分配一个未使用的端口
     * @param namespace
     * @return ActionReturnUtil
     * @throws Exception
     */
    public ActionReturnUtil getPort(String namespace) throws Exception;

    /**
     * 手动输入检测端口是否已使用
     * @param port
     * @param namespace
     * @return ActionReturnUtil
     * @throws Exception
     */
    public ActionReturnUtil checkPort(String port,String namespace) throws Exception;

    /**
     * 手动输入时更新端口
     * @param oldPort
     * @param nowPort
     * @param namespace
     * @return
     * @throws Exception
     */
    public ActionReturnUtil updatePort(String oldPort,String nowPort,String namespace) throws Exception;

    public ActionReturnUtil delPort(String port,String tenantId) throws Exception;

    List<RouterSvc> listIngressByName(ParsedIngressListDto parsedIngressListDto) throws Exception;

    public ActionReturnUtil listIngressByName(String namespace, String nameList) throws Exception;

    /**
     * 根据服务名称在指定分区内获取http ingress
     * @param name
     * @param namespace
     * @param cluster
     * @return List<Ingress>
     * @throws Exception
     */
    List<Ingress> listHttpIngress(String name, String namespace, Cluster cluster) throws Exception;

    /**
     * 获取系统暴露configmap
     * @param cluster
     * @return ConfigMap
     * @throws Exception
     */
    public ConfigMap getSystemExposeConfigmap(String icName, Cluster cluster, String protocolType) throws Exception;

    /**
     * 更新系统nginx的configmap
     * @param cluster
     * @param namespace
     * @param service
     * @param ruleDto
     * @return
     * @throws Exception
     */
    public ActionReturnUtil updateSystemExposeConfigmap(Cluster cluster, String namespace, String service, String icName, List<TcpRuleDto> ruleDto, String protocol) throws Exception;

    /**
     * 获取所有的对外访问路由， tcp和udp对外服务通过负载均衡器的tcp和upd的configmap获取，http通过ingress获取
     * @param namespace
     * @param nameList
     * @return ActionReturnUtil
     * @throws Exception
     */
    public ActionReturnUtil listExposedRouterWithIngressAndNginx(String namespace, String nameList, String projectId) throws Exception;

    /**
     * 更新集群内的服务外部路由规则
     * @param svcRouterDto
     * @return
     * @throws Exception
     */
    ActionReturnUtil updateSystemRouteRule(SvcRouterDto svcRouterDto) throws Exception;

    /**
     * 删除集群内的服务外部路由规则
     * @param tcpDeleteDto
     * @return ActionReturnUtil
     * @throws Exception
     */
    ActionReturnUtil deleteSystemRouteRule(TcpDeleteDto tcpDeleteDto, String deployName) throws Exception;

    /**
     * 保存
     * @param cluster
     * @return String
     * @throws Exception
     */
    Integer chooseOnePort(Cluster cluster) throws Exception;

    /**
     * 获取端口范围
     * @param namespace
     * @param cluster
     * @return Map<String, Integer>
     * @throws Exception
     */
    Map<String, Integer> getPortRange(String namespace, Cluster cluster) throws Exception;

    ActionReturnUtil createRuleInDeploy(SvcRouterDto svcRouterDto) throws Exception;

    List<Map<String, Object>> createExternalRule(ServiceTemplateDto svcTemplate, String namespace, String serviceType) throws Exception;

    boolean checkIngressName(Cluster cluster, String name) throws Exception;
}
