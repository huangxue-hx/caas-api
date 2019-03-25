package com.harmonycloud.dao.cluster;

import com.harmonycloud.dao.cluster.bean.TransferClusterBackup;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferClusterBackupMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TransferClusterBackup record);

    int insertSelective(TransferClusterBackup record);

    TransferClusterBackup selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TransferClusterBackup record);

    int updateByPrimaryKey(TransferClusterBackup record);

    List<TransferClusterBackup> queryHistoryBackUp(@Param(value = "clusterId") String clusterId, @Param(value = "tenantId") String tenantId);
}