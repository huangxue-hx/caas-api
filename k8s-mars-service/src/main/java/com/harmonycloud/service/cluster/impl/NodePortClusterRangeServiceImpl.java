package com.harmonycloud.service.cluster.impl;

import com.harmonycloud.dao.cluster.NodePortClusterRangeMapper;
import com.harmonycloud.dao.cluster.bean.NodePortClusterRange;
import com.harmonycloud.service.cluster.NodePortClusterRangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-14
 * @Modified
 */
@Service
public class NodePortClusterRangeServiceImpl implements NodePortClusterRangeService{

    @Autowired
    private NodePortClusterRangeMapper portClusterRangeMapper;
    @Override
    public NodePortClusterRange findByClusterId(String clusterId) throws Exception {
        NodePortClusterRange range = portClusterRangeMapper.findByClusterId(clusterId);
        return range;
    }
}
