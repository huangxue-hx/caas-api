package com.harmonycloud.dao.message;

import com.harmonycloud.dao.message.bean.Message;
import com.harmonycloud.dao.message.bean.MessageExample;
import java.util.List;

public interface MessageMapper {
    int deleteByExample(MessageExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Message record);

    int insertSelective(Message record);

    List<Message> selectByExample(MessageExample example);

    Message selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Message record);

    int updateByPrimaryKey(Message record);
}