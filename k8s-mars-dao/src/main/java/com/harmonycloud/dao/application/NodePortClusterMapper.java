package com.harmonycloud.dao.application;

import com.harmonycloud.dao.application.bean.NodePortCluster;
import com.harmonycloud.dao.application.bean.NodePortClusterExample;
import java.util.List;

public interface NodePortClusterMapper {
    int deleteByExample(NodePortClusterExample example);

    int insert(NodePortCluster record);

    int insertSelective(NodePortCluster record);

    List<NodePortCluster> selectByExample(NodePortClusterExample example);
    
    void updateNodePortCluster(NodePortCluster npc);
    
    void updateNodePortClusterbynodeportid(NodePortCluster npc);
}