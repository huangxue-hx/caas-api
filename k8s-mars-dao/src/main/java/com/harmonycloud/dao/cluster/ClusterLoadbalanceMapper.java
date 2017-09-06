package com.harmonycloud.dao.cluster;

import com.harmonycloud.dao.cluster.bean.ClusterLoadbalance;
import com.harmonycloud.dao.cluster.bean.ClusterLoadbalanceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ClusterLoadbalanceMapper {
    int countByExample(ClusterLoadbalanceExample example);

    int deleteByExample(ClusterLoadbalanceExample example);

    int deleteByPrimaryKey(Integer lbId);

    int insert(ClusterLoadbalance record);

    int insertSelective(ClusterLoadbalance record);

    List<ClusterLoadbalance> selectByExample(ClusterLoadbalanceExample example);

    ClusterLoadbalance selectByPrimaryKey(Integer lbId);

    int updateByExampleSelective(@Param("record") ClusterLoadbalance record, @Param("example") ClusterLoadbalanceExample example);

    int updateByExample(@Param("record") ClusterLoadbalance record, @Param("example") ClusterLoadbalanceExample example);

    int updateByPrimaryKeySelective(ClusterLoadbalance record);

    int updateByPrimaryKey(ClusterLoadbalance record);
}