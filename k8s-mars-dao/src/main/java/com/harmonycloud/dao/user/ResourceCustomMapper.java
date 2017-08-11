package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.ResourceCustom;
import com.harmonycloud.dao.user.bean.ResourceCustomExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ResourceCustomMapper {
    int deleteByExample(ResourceCustomExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ResourceCustom record);

    int insertSelective(ResourceCustom record);

    List<ResourceCustom> selectByExample(ResourceCustomExample example);

    ResourceCustom selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") ResourceCustom record, @Param("example") ResourceCustomExample example);

    int updateByExample(@Param("record") ResourceCustom record, @Param("example") ResourceCustomExample example);

    int updateByPrimaryKeySelective(ResourceCustom record);

    int updateByPrimaryKey(ResourceCustom record);
}