package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.Trigger;
import com.harmonycloud.dao.ci.bean.TriggerExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TriggerMapper {
    long countByExample(TriggerExample example);

    int deleteByExample(TriggerExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Trigger record);

    int insertSelective(Trigger record);

    List<Trigger> selectByExample(TriggerExample example);

    Trigger selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Trigger record, @Param("example") TriggerExample example);

    int updateByExample(@Param("record") Trigger record, @Param("example") TriggerExample example);

    int updateByPrimaryKeySelective(Trigger record);

    int updateByPrimaryKey(Trigger record);

    Trigger selectByJobId(Integer jobId);
}