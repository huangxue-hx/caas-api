package com.harmonycloud.dao.cluster;

import com.harmonycloud.dao.cluster.bean.TransferCluster;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferClusterMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TransferCluster record);

    int insertSelective(TransferCluster record);

    TransferCluster selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TransferCluster record);

    int updateByPrimaryKey(TransferCluster record);

    TransferCluster queryTransferClusterByParam(@Param(value="tenantId")String tenantId, @Param(value="clusterId")String clusterId);



    void updatePercent(@Param(value="clusterId")String clusterId,@Param(value="tenantId")String tenantId,@Param(value="percent")Double percent);

    void deleteCluster(@Param(value="clusterId")String clusterId);

    List<TransferCluster> queryTransferCluster(@Param(value="clusterId")String clusterId);
}