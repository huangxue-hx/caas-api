package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.Parameter;
import com.harmonycloud.dao.ci.bean.ParameterExample;
import java.util.List;

public interface ParameterMapper {
    int deleteByExample(ParameterExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Parameter record);

    int insertSelective(Parameter record);

    List<Parameter> selectByExample(ParameterExample example);

    Parameter selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Parameter record);

    int updateByPrimaryKey(Parameter record);

    Parameter selectByJobId(Integer jobId);
}