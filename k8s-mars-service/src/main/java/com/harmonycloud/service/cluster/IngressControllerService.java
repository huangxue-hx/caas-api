package com.harmonycloud.service.cluster;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cluster.IngressConfigMap;
import com.harmonycloud.dto.cluster.IngressControllerDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;

import java.io.IOException;
import java.util.List;

/**
 * @author xc
 * @date 2018/7/31 17:06
 */
public interface IngressControllerService {

    /**
     * 查询所有的IngressController
     * @param clusterId 集群Id
     * @return ActionReturnUtil
     */
    ActionReturnUtil listIngressController(String clusterId) throws Exception;

    /**
     * 查询IngressController信息
     * @param clusterId 集群Id
     * @return ActionReturnUtil
     */
    IngressControllerDto getIngressController(String icName, String clusterId);

    /**
     * 查询某个集群的所有负载均衡器，返回结果不包括已分配的租户和选择的负载均衡节点列表
     * @param clusterId
     * @return
     */
    List<IngressControllerDto> listIngressControllerBrief(String clusterId);

    /**
     * 获取负载均衡器的http端口范围
     * @return
     */
    ActionReturnUtil getIngressControllerPortRange(String clusterId) throws MarsRuntimeException, IOException;

    /**
     * 创建IngressController
     */
    ActionReturnUtil createIngressController(IngressControllerDto ingressControllerDto, IngressConfigMap ingressConfigMap)
            throws MarsRuntimeException, IOException;

    /**
     * 删除IngressController
     * @param icName IngressController名称
     * @return ActionReturnUtil
     */
    ActionReturnUtil deleteIngressController(String icName, String clusterId) throws Exception;

    /**
     * 更新IngressController
     */
    ActionReturnUtil updateIngressController(IngressControllerDto ingressControllerDto, IngressConfigMap ingressConfigMap) throws Exception;

    /**
     * 分配IngressController到指定租户下
     * @param icName IngressController名称
     * @param tenantId 指定租户的Id
     * @return ActionReturnUtil
     */
    ActionReturnUtil assignIngressController(String icName, String tenantId, String clusterId) throws Exception;

    Boolean checkIcUsedStatus(String icName, Cluster cluster) throws MarsRuntimeException;

}
