package com.harmonycloud.service.user;

import java.util.List;
import java.util.Map;

import com.harmonycloud.dao.message.bean.Message;

/**
 * 
 * @Title AuthManager.java
 * @author zgl
 * @date 2017年7月7日
 * @Description 
 * @version V1.0
 */
public interface MessageService {
    /**
     * 获取所有的短信配置
     * @return
     * @throws Exception
     */
   public List<Message> getAllMessage() throws Exception;
   /**
    * 根据id获取短信配置
    * @param id
    * @return
    * @throws Exception
    */
   public Message getMessageById(Integer id) throws Exception;
   /**
    * 创建短信配置
    * @param message
    * @throws Exception
    */
   public void createMessage(Message message) throws Exception;
   /**
    * 根据id更新短信
    * @param id
    * @throws Exception
    */
   public void updateMessage(Message message) throws Exception;
   /**
    * 根据id删除短信
    * @param id
    * @throws Exception
    */
   public void deleteMessage(Integer id) throws Exception;
   /**
    * 发送短信
    *
    * @param mobiles 电话号码
    * @param params 模板参数
    * @throws Exception
    */
   public void sendMessage(Integer id,List<String> mobiles,Map<String, String> params) throws Exception;
}
