package com.harmonycloud.dao.cluster;

import com.harmonycloud.dao.cluster.bean.NodePortClusterUsage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-12
 * @Modified
 */
public interface NodePortClusterUsageMapper {

    void insertNodeportUsage(NodePortClusterUsage portUsage);

    List<NodePortClusterUsage> selectPortUsageByClusterId(String clusterId);

    NodePortClusterUsage selectPortUsageByPort(@Param("clusterId") String clusterId, @Param("port") Integer port);

    void deleteNodePortUsage(@Param("clusterId") String clusterId, @Param("port") Integer port);

    void updateNodePortUsage(NodePortClusterUsage portUsage);

    int deleteByClusterId(@Param("clusterId")String clusterId);
}
