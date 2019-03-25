package com.harmonycloud.dao.cluster;

import com.harmonycloud.dao.cluster.bean.TransferStep;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferStepMapper {
    int deleteByPrimaryKey(Integer stepId);

    int insert(TransferStep record);

    int insertSelective(TransferStep record);

    TransferStep selectByPrimaryKey(Integer stepId);

    int updateByPrimaryKeySelective(TransferStep record);

    int updateByPrimaryKey(TransferStep record);
}