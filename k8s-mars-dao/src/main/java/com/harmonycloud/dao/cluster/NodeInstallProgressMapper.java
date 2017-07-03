package com.harmonycloud.dao.cluster;

import com.harmonycloud.dao.cluster.bean.NodeInstallProgress;
import com.harmonycloud.dao.cluster.bean.NodeInstallProgressExample;
import java.util.List;

public interface NodeInstallProgressMapper {
    int deleteByExample(NodeInstallProgressExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(NodeInstallProgress record);

    int insertSelective(NodeInstallProgress record);

    List<NodeInstallProgress> selectByExample(NodeInstallProgressExample example);

    NodeInstallProgress selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(NodeInstallProgress record);

    int updateByPrimaryKey(NodeInstallProgress record);
}