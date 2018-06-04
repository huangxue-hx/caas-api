package com.harmonycloud.service.platform.serviceImpl.infrastructure;

import com.harmonycloud.dao.cluster.NodeDrainProgressMapper;
import com.harmonycloud.dao.cluster.bean.NodeDrainProgress;
import com.harmonycloud.service.platform.service.NodeDrainProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by zhangkui on 2018/1/12.
 */
@Service
public class NodeDrainProgressServiceImpl implements NodeDrainProgressService{

    @Autowired
    NodeDrainProgressMapper nodeDrainProgressMapper;

    @Override
    public NodeDrainProgress findByNodeName(String nodeName, String clusterId) {
        return nodeDrainProgressMapper.findByNodeName(nodeName,clusterId);
    }

    @Override
    public void insertDrainProgress(NodeDrainProgress nodeDrainProgress) {
        nodeDrainProgressMapper.insertDrainProgress(nodeDrainProgress);
    }

    @Override
    public void updateDrainProgress(NodeDrainProgress nodeDrainProgress) {
        nodeDrainProgressMapper.updateDrainProgress(nodeDrainProgress);
    }
}
