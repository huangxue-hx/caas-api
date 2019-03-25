package com.harmonycloud.dao.dataprivilege;

import com.harmonycloud.dao.dataprivilege.bean.DataResourceUrl;
import com.harmonycloud.dao.dataprivilege.bean.DataResourceUrlExample;
import java.util.List;

public interface DataResourceUrlMapper {
    int deleteByExample(DataResourceUrlExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(DataResourceUrl record);

    int insertSelective(DataResourceUrl record);

    List<DataResourceUrl> selectByExample(DataResourceUrlExample example);

    DataResourceUrl selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DataResourceUrl record);

    int updateByPrimaryKey(DataResourceUrl record);
}