package com.harmonycloud.service.cluster;

import com.harmonycloud.dao.cluster.bean.NodePortClusterUsage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-14
 * @Modified
 */
public interface NodePortClusterUsageService {

    /**
     * 插入端口使用信息
     * @param portUsage
     */
    boolean insertNodeportUsage(NodePortClusterUsage portUsage) throws Exception;

    /**
     * 根据集群Id查询端口使用记录
     * @param clusterId
     * @return
     * @throws Exception
     */
    List<NodePortClusterUsage> selectPortUsageByClusterId(String clusterId) throws Exception;


    /**
     * 根据集群Id和端口查询某个端口的使用
     * @param clusterId
     * @param port
     * @return
     * @throws Exception
     */
    NodePortClusterUsage selectPortUsageByPort(String clusterId, Integer port) throws Exception;

    /**
     * 删除集群的端口使用
     * @param clusterId
     * @param port
     * @throws Exception
     */
    void deleteNodePortUsage(String clusterId, Integer port) throws Exception;

    /**
     * 更新端口使用状态
     * @param portUsage
     * @throws Exception
     */
    void updateNodePortStatus(NodePortClusterUsage portUsage) throws Exception;

    int deleteByClusterId(String clusterId);
}
