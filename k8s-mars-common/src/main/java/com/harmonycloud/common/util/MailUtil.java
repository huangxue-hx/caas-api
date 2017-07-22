package com.harmonycloud.common.util;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Created by anson on 17/7/20.
 */
public class MailUtil {

    private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);
    private static final String CONNECTION_TIMEOUT = "20000";

    private static String smtp;
    private static String port;
    private static String user;
    private static String password;
    private static String fromEmail;

    private static JavaMailSenderImpl javaMailSender;

    public static JavaMailSenderImpl getJavaMailSender(){
        if(null == javaMailSender){
            initMailSender();
        }
        return javaMailSender;
    }

    public static void sendSimpleMail(SimpleMailMessage simpleMailMessage) throws Exception {
        if(null == javaMailSender){
            initMailSender();
        }
        if(StringUtils.isBlank(simpleMailMessage.getFrom())){
            simpleMailMessage.setFrom(fromEmail);
        }
        javaMailSender.send(simpleMailMessage);
    }

    public static void sendMimeMessage(MimeMessage mimeMessage) throws Exception {
        //if(null == javaMailSender){
            initMailSender();
        //}

        if(mimeMessage.getFrom() == null){
            mimeMessage.setFrom(InternetAddress.parse(fromEmail)[0]);
        }
        javaMailSender.send(mimeMessage);
    }



    public static void initMailSender(){
//        AlertEmailConfig alertEmailConfig = configService.getAlertEmailConfig();
        getEmailConfig();
        if( StringUtils.isBlank(smtp)){
            logger.error("邮箱初始化失败, 无法获取邮箱设置信息");
            if(javaMailSender == null || !testConnection()){
                throw new MessagingException("告警邮箱连接失败， 请在系统设置里设置邮箱服务");
            }
        }
        //如果邮箱发送器已经创建 并且邮箱设置信息没有变更，不需要重新初始化
        if(javaMailSender!= null && !isEmailConfigChanged()){
            return;
        }
//        logger.info("邮箱服务器设置，alertEmailConfig：{}",JSONObject.toJSONString(alertEmailConfig));
//        fromMail = alertEmailConfig.getFromEmail();
        if(StringUtils.isBlank(fromEmail)){
            fromEmail = user;
        }
        javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setUsername(user);
        javaMailSender.setPassword(password);
        javaMailSender.setHost(smtp);
        if(StringUtils.isNotBlank(port)){
            javaMailSender.setPort(Integer.parseInt(port));
        }
        javaMailSender.setDefaultEncoding("UTF-8");
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", smtp);
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.connectiontimeout", CONNECTION_TIMEOUT);
        props.setProperty("mail.smtp.timeout", CONNECTION_TIMEOUT);
        javaMailSender.setJavaMailProperties(props);
        //javaMailSender = javaMailSender;
    }

    private static void getEmailConfig(){
        ActionReturnUtil result = null;
        try {
            result = HttpClientUtil.httpGetRequest("http://10.10.101.143:30097/config/alertemail/query", null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(result != null && result.isSuccess()){
            Map emailConfig = JsonUtil.convertJsonToMap((String)result.get("data"));
            smtp = (String)emailConfig.get("smtp");
            port =  (String)emailConfig.get("port");
            user = (String)emailConfig.get("user");
            password = (String)emailConfig.get("password");
            fromEmail = (String)emailConfig.get("fromEmail");
        }
    }

    private static boolean testConnection(){
        try {
            if(javaMailSender == null){
                initMailSender();
            }
            javaMailSender.testConnection();
            return true;
        }catch (Exception e){
            logger.error("告警邮箱初始化,连接失败,javaMailSender:{}",
                    JSONObject.toJSONString(javaMailSender), e);
            return false;
        }
    }

    private static boolean isEmailConfigChanged(){
        if(javaMailSender == null){
            return true;
        }
        if(!javaMailSender.getHost().equalsIgnoreCase(smtp)){
            return true;
        }
        if(!javaMailSender.getUsername().equalsIgnoreCase(user)){
            return true;
        }
        if(!javaMailSender.getPassword().equalsIgnoreCase(password)){
            return true;
        }
        if(StringUtils.isNotBlank(port)){
            if(Integer.valueOf(port) != javaMailSender.getPort()) {
                return true;
            }
        }else if(javaMailSender.getPort() != -1){
            return true;
        }
        return false;
    }

    public static byte[] stream2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1000];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 1000)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }



    public static void main(String[] args) throws Exception {
        MimeMessage mimeMessage = MailUtil.getJavaMailSender().createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("k8sdev@harmonycloud.cn");
            helper.setTo("kaiyunzhang@harmonycloud.cn");
            Map dataModel = new HashMap<>();
            dataModel.put("jobName","test");
            dataModel.put("status","SUCCESS");
            dataModel.put("time","2017-07-20 11:11:11");
            dataModel.put("startTime","2017-07-20 11:11:11");
            dataModel.put("duration","1m23s");
            helper.setText(TemplateUtil.generate("notification.ftl",dataModel), true);

        }catch(Exception e){

        }
        sendMimeMessage(mimeMessage);
    }

}
