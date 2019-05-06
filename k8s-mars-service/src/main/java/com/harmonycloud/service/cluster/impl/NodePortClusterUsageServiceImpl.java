package com.harmonycloud.service.cluster.impl;

import com.harmonycloud.dao.cluster.NodePortClusterUsageMapper;
import com.harmonycloud.dao.cluster.bean.NodePortClusterUsage;
import com.harmonycloud.service.cluster.NodePortClusterUsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-14
 * @Modified
 */
@Service
public class NodePortClusterUsageServiceImpl implements NodePortClusterUsageService{

    @Autowired
    private NodePortClusterUsageMapper portUsageMapper;

    @Override
    public boolean insertNodeportUsage(NodePortClusterUsage portUsage) throws Exception {
        portUsageMapper.insertNodeportUsage(portUsage);
        return true;
    }

    @Override
    public List<NodePortClusterUsage> selectPortUsageByClusterId(String clusterId) throws Exception {
        List<NodePortClusterUsage> portClusterUsages = portUsageMapper.selectPortUsageByClusterId(clusterId);
        return portClusterUsages;
    }

    @Override
    public NodePortClusterUsage selectPortUsageByPort(String clusterId, Integer port) throws Exception {
        NodePortClusterUsage portClusterUsage = portUsageMapper.selectPortUsageByPort(clusterId, port);
        return portClusterUsage;
    }

    @Override
    public void deleteNodePortUsage(String clusterId, Integer port) throws Exception {
        portUsageMapper.deleteNodePortUsage(clusterId, port);
    }

    @Override
    public void updateNodePortStatus(NodePortClusterUsage portUsage) throws Exception {
        portUsageMapper.updateNodePortUsage(portUsage);
    }

    @Override
    public int deleteByClusterId(String clusterId){
        return portUsageMapper.deleteByClusterId(clusterId);
    }
}
