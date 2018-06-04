package com.harmonycloud.service.user.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.AssertUtil;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.CheckSumBuilder;
import com.harmonycloud.dao.message.MessageMapper;
import com.harmonycloud.dao.message.bean.Message;
import com.harmonycloud.dao.message.bean.MessageExample;
import com.harmonycloud.service.user.MessageService;

@Service
@Transactional(rollbackFor = Exception.class)
public class MessageServiceimpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public List<Message> getAllMessage() throws Exception {
        MessageExample example = new MessageExample();
        List<Message> list = messageMapper.selectByExample(example);
        return list;
    }

    @Override
    public Message getMessageById(Integer id) throws Exception {
        MessageExample example = new MessageExample();
        example.createCriteria().andIdEqualTo(id);
        List<Message> list = messageMapper.selectByExample(example);
        if (list != null && list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public void createMessage(Message message) throws Exception {
        messageMapper.insertSelective(message);
    }

    @Override
    public void updateMessage(Message message) throws Exception {
        messageMapper.updateByPrimaryKeySelective(message);
    }

    @Override
    public void deleteMessage(Integer id) throws Exception {
        messageMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void sendMessage(Integer id, List<String> mobiles, Map<String, String> params) throws Exception {
        AssertUtil.notNull(id);
        AssertUtil.notNull(params);
        AssertUtil.notEmpty(mobiles, DictEnum.PHONE);
        Message message = this.getMessageById(id);
        // 发送验证码的请求路径URL
        String SERVER_URL = message.getServerUrl();
        // 网易云信分配的账号，请替换你在管理后台应用下申请的Appkey
        String APP_KEY = message.getAppKey();
        // 网易云信分配的密钥，请替换你在管理后台应用下申请的appSecret
        String APP_SECRET = message.getAppSecret();
        // 随机数
        String NONCE = message.getNonce();
        // 短信模板ID
        String TEMPLATEID = message.getTemplateid();// 3056657 3056654
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(SERVER_URL);
        String curTime = String.valueOf((new Date()).getTime() / 1000L);
        // 计算CheckSum
        String checkSum = CheckSumBuilder.getCheckSum(APP_SECRET, NONCE, curTime);

        // 设置请求的header
        httpPost.addHeader("AppKey", APP_KEY);
        httpPost.addHeader("Nonce", NONCE);
        httpPost.addHeader("CurTime", curTime);
        httpPost.addHeader("CheckSum", checkSum);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        // 设置请求的的参数，requestBody参数
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        /*
         * 1.如果是模板短信，请注意参数mobile是有s的，详细参数配置请参考“发送模板短信文档” 2.参数格式是jsonArray的格式，例如
         * "['13888888888','13666666666']"
         * 3.params是根据你模板里面有几个参数，那里面的参数也是jsonArray格式
         */
        StringBuilder mobilesJson = new StringBuilder();
        for (String mobile : mobiles) {
            if (mobilesJson == null) {
                mobilesJson.append("['" + mobile + "'");
            } else {
                mobilesJson.append(",'" + mobile + "'");
            }
        }
        String paramsJson = "['" + params.get("name") + "','" + params.get("model") + "','" + params.get("time") + "','" + params.get("content") + "发送告警，请登陆云平台查看详细信息']";
        nvps.add(new BasicNameValuePair("templateid", TEMPLATEID));
        nvps.add(new BasicNameValuePair("mobiles", mobilesJson.toString()));
        nvps.add(new BasicNameValuePair("params", paramsJson));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
        // 执行请求
        HttpResponse response = httpclient.execute(httpPost);
        String res = EntityUtils.toString(response.getEntity(), "utf-8");
        if (!res.contains("200")) {
            throw new MarsRuntimeException(ErrorCodeMessage.MESSAGE_SEND_ERROR ,res,false);
        }
    }

}
