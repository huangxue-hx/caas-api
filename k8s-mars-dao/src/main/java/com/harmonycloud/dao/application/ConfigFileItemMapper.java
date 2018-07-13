package com.harmonycloud.dao.application;

import com.harmonycloud.dao.application.bean.ConfigFileItem;
import com.harmonycloud.dao.application.bean.ConfigFileItemExample;
import java.util.List;

public interface ConfigFileItemMapper {
    int deleteByExample(ConfigFileItemExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ConfigFileItem record);

    int insertSelective(ConfigFileItem record);

    List<ConfigFileItem> selectByExampleWithBLOBs(ConfigFileItemExample example);

    List<ConfigFileItem> selectByExample(ConfigFileItemExample example);

    ConfigFileItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ConfigFileItem record);

    int updateByPrimaryKeyWithBLOBs(ConfigFileItem record);

    int updateByPrimaryKey(ConfigFileItem record);

    void deleteConfigFileItem(String ConfigFileId);

    List<ConfigFileItem> getConfigFileItem(String ConfigFileId);
}