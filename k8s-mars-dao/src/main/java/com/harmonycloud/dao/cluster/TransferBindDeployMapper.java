package com.harmonycloud.dao.cluster;

import com.harmonycloud.dao.cluster.bean.TransferBindDeploy;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface TransferBindDeployMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TransferBindDeploy record);

    int insertSelective(TransferBindDeploy record);

    TransferBindDeploy selectByPrimaryKey(Integer id);

    TransferBindDeploy selectUnique(@Param(value="query")TransferBindDeploy query);

    int updateByPrimaryKeySelective(TransferBindDeploy record);

    int updateByPrimaryKey(TransferBindDeploy record);

    Integer queryMaxNun(@Param(value="tenantId")String tenantId,@Param(value="clusterId")String clusterId);

    void saveTransferList(@Param(value="transferBindDeploys") List<TransferBindDeploy> transferBindDeploys);

    List<TransferBindDeploy> queryTransferDeployDetail(@Param(value="tenantId")String tenantId,@Param(value="clusterId")String clusterId);

    List<TransferBindDeploy> listTransferDeploys(@Param(value="transferBackupId")Integer transferBackupId);

    void deleteTransferBindDeploy(@Param(value="clusterId")String clusterId);

    List<TransferBindDeploy> queryErrorBindDeploy(@Param(value="tenantId")String tenantId,@Param(value="clusterId")String clusterId);

    void updateDeploys(@Param(value="updateDeploys")List<TransferBindDeploy> updateDeploys);
}