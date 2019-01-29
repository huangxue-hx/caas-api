package com.harmonycloud.dao.cluster;

import com.harmonycloud.dao.cluster.bean.TransferBindDeploy;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferBindDeployMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TransferBindDeploy record);

    int insertSelective(TransferBindDeploy record);

    TransferBindDeploy selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TransferBindDeploy record);

    int updateByPrimaryKey(TransferBindDeploy record);

    Integer queryMaxNun(@Param(value="tenantId")String tanantId,@Param(value="clusterId")String clusterId);

    void saveTransferList(@Param(value="transferBindDeploys") List<TransferBindDeploy> transferBindDeploys);
}