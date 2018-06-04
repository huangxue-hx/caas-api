package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.UrlDic;
import com.harmonycloud.dao.user.bean.UrlDicExample;
import java.util.List;

public interface UrlDicMapper {
    int deleteByExample(UrlDicExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(UrlDic record);

    int insertSelective(UrlDic record);

    List<UrlDic> selectByExample(UrlDicExample example);

    UrlDic selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UrlDic record);

    int updateByPrimaryKey(UrlDic record);
}