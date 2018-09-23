package com.harmonycloud.service.cluster;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;

import java.io.IOException;

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
     * 获取负载均衡器的http端口范围
     * @return
     */
    ActionReturnUtil getIngressControllerPortRange(String clusterId) throws MarsRuntimeException, IOException;

    /**
     * 创建IngressController
     * @param icName IngressController名称
     * @param icPort IngressController暴露端口
     * @return ActionReturnUtil
     */
    ActionReturnUtil createIngressController(String clusterId, String icName, int icPort) throws MarsRuntimeException, IOException;

    /**
     * 删除IngressController
     * @param icName IngressController名称
     * @return ActionReturnUtil
     */
    ActionReturnUtil deleteIngressController(String icName, String clusterId) throws Exception;

    /**
     * 更新IngressController
     * @param icName IngressController名称
     * @param icPort IngressController暴露端口
     * @return ActionReturnUtil
     */
    ActionReturnUtil updateIngressController(String icName, int icPort, String clusterId) throws  Exception;

    /**
     * 分配IngressController到指定租户下
     * @param icName IngressController名称
     * @param tenantId 指定租户的Id
     * @return ActionReturnUtil
     */
    ActionReturnUtil assignIngressController(String icName, String tenantId, String clusterId) throws Exception;

}
