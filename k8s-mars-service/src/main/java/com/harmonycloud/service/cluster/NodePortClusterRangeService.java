package com.harmonycloud.service.cluster;

import com.harmonycloud.dao.cluster.bean.NodePortClusterRange;
import org.apache.ibatis.annotations.Param;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-14
 * @Modified
 */
public interface NodePortClusterRangeService {

    /**
     * 根据集群Id查询端口可使用范围
     * @param clusterId
     * @return
     * @throws Exception
     */
    NodePortClusterRange findByClusterId(@Param("clusterId") String clusterId) throws Exception;

}
