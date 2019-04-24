package com.harmonycloud.service.user.auth;

import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.user.bean.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class AuthManagerCrowd {
    public static final String DOMAIN = "http://crowd.harmonycloud.com:8095/crowd/rest/usermanagement/latest/";

    public static final String COOKIE_NAME = "crowd.token_key";

    public static HttpURLConnection crowdPost(URL url, String contenttype, String data) throws Exception{
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", contenttype);
        connection.setRequestProperty("Charset", "UTF-8");
//		http基本认证
        String base64encodedString = Base64.getEncoder().encodeToString("mars:123456".getBytes("utf-8"));
        connection.setRequestProperty("Authorization", "Basic " + base64encodedString);
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());

        out.write(new String(data.getBytes("UTF-8")));
        out.flush();
        out.close();
        return connection;
    }

    public static HttpURLConnection crowdGet(URL url) throws Exception{
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("connection", "Keep-Alive");
        //http基本认证
        String base64encodedString = Base64.getEncoder().encodeToString("mars:123456".getBytes("utf-8"));
        connection.setRequestProperty("Authorization", "Basic " + base64encodedString);
        connection.connect();
        return connection;
    }

    public static HttpURLConnection crowdDelete(URL url) throws Exception{
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("connection", "Keep-Alive");
        //http基本认证
        String base64encodedString = Base64.getEncoder().encodeToString("mars:123456".getBytes("utf-8"));
        connection.setRequestProperty("Authorization", "Basic " + base64encodedString);
        connection.connect();
        return connection;
    }


    public static String getMessageBody(HttpURLConnection connection)throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuffer result = new StringBuffer();
        for (line = br.readLine(); line != null; line = br.readLine()) {
            result.append(line);
        }
        System.out.println(result.toString());
        return result.toString();
    }

    public static String auth(String username, String password) throws Exception {
        URL url = new URL(AuthManagerCrowd.DOMAIN + "session");
        String jsonData = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"validation-factors\": {\"validationFactors\": [{\"name\":\"remote_address\",\"value\":\"10.100.100.247\"}]}}";
//        String jsonData = "{\"username\":\"" + username + "\",\"password\":\""+ password + "\",\"validation-factors\": {\"validationFactors\": [{\"name\":\"remote_address\",\"value\":\"10.100.100.94\"}]}}";
//        String jsonData = "{\"username\":\"" + username + "\",\"password\":\""+ password + "\"}";
        HttpURLConnection connection = AuthManagerCrowd.crowdPost(url, "application/json", jsonData);
        if (connection.getResponseCode() == 201) {
//            crowd = true;
//            String messageBody = AuthManagerCrowd.getMessageBody(connection);
//            crowdToken = messageBody.substring(messageBody.indexOf("<token>") + 7, messageBody.lastIndexOf("</token>"));
            //在crowd服务器中找到了相关信息
//            res = username;
            return username;
        }
        else{
            return null;
        }
    }

    public static boolean addUser(String username, String password, String realname, String email, String phone) throws Exception{

        URL crowdurl = new URL(AuthManagerCrowd.DOMAIN + "user");
        String xmlData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><user name=\"" + username + "\" expand=\"attributes\"><first-name>" + realname + "</first-name><last-name>" + realname + "</last-name><email>" + email + "</email><active>true</active><attributes><link href=\"" + AuthManagerCrowd.DOMAIN + "user/attribute?username=" + username + "\" rel=\"self\"/></attributes><password><link rel=\"edit\" href=\"" + AuthManagerCrowd.DOMAIN + "/user/password?username=" + username + "\"/><value>" + password + "</value></password></user>";
        //创建用户
        HttpURLConnection httpURLConnection = AuthManagerCrowd.crowdPost(crowdurl, "application/xml", xmlData);
        if (httpURLConnection.getResponseCode() == 201) {
            //添加phone属性
            URL phoneurl = new URL(AuthManagerCrowd.DOMAIN + "user/attribute?username=" + username);
            String phoneJson = "{\"attributes\": [{\"name\": \"phone\",\"values\": [\"" + phone + "\"]}]}";
            HttpURLConnection phonecon = AuthManagerCrowd.crowdPost(phoneurl, "application/json", phoneJson);
            if (phonecon.getResponseCode() != 204) {
                return false;
            }
            else{
                return true;
            }
        } else {
            return false;
        }
    }

    public static User getUser(String username, String password)throws Exception{
        URL crowdurl = new URL(AuthManagerCrowd.DOMAIN + "user?username=" + username);
        HttpURLConnection urlConnection = AuthManagerCrowd.crowdGet(crowdurl);
        if (urlConnection.getResponseCode() != 200) {
            return null;
        }
        String messageBody = AuthManagerCrowd.getMessageBody(urlConnection);
        String email = messageBody.substring(messageBody.indexOf("<email>") + 7, messageBody.lastIndexOf("</email>"));
        //获取phone值
        URL phoneurl = new URL(AuthManagerCrowd.DOMAIN + "user/attribute?username=" + username);
        HttpURLConnection urlPhoneConnection = AuthManagerCrowd.crowdGet(phoneurl);
        if (urlPhoneConnection.getResponseCode() != 200) {
            return null;
        }
        messageBody = AuthManagerCrowd.getMessageBody(urlPhoneConnection);
        String phone = "";
        if (messageBody.indexOf("<attribute name=\"phone\">") != -1) {
            //有相应的phone属性
            messageBody = messageBody.substring(messageBody.indexOf("<attribute name=\"phone\">") + "<attribute name=\"phone\">".length());
            phone = messageBody.substring(messageBody.indexOf("<value>") + 7, messageBody.indexOf("</value>"));
        } else {
            //找不到crowd中的phone属性
            return null;
        }
        //组装用户数据
        User user = new User();
        user.setCreate_time(DateUtil.getCurrentUtcTime());
        user.setPhone(phone);
        user.setEmail(email);
        user.setComment("Created by CROWD");
        user.setPassword(password);
        user.setUsername(username);
        user.setRealName(username);
        user.setIsAdmin(0);
        return user;
    }

    public static String getToken(String username, String password) throws Exception{
        URL url = new URL(AuthManagerCrowd.DOMAIN + "session");
        String jsonData = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"validation-factors\": {\"validationFactors\": [{\"name\":\"remote_address\",\"value\":\"10.100.100.247\"}]}}";
        HttpURLConnection connection = AuthManagerCrowd.crowdPost(url, "application/json", jsonData);
        if (connection.getResponseCode() == 201) {
            String messageBody = AuthManagerCrowd.getMessageBody(connection);
            return messageBody.substring(messageBody.indexOf("<token>") + 7, messageBody.lastIndexOf("</token>"));
        }
        else{
            return null;
        }
    }

    public static void invalidateToken(String username) throws Exception{
        //在crowd中清除登录信息
        URL url = new URL(AuthManagerCrowd.DOMAIN + "session?username=" + username);

        HttpURLConnection connection = AuthManagerCrowd.crowdDelete(url);
        connection.getResponseCode();
    }

    public static void AddCookie(String crowdToken, HttpServletResponse response) throws Exception {
        //将crowd中token的值存入token
        Cookie cookie = new Cookie(COOKIE_NAME, crowdToken);
        cookie.setPath("/");                //如果路径为/则为整个tomcat目录有用
        cookie.setDomain("harmonycloud.com");    //设置对所有*.harmonycloud.com为后缀的域名

        response.addCookie(cookie);
    }

    //用于检测用户是否登录
    public static String testLogin(String crowdToken) throws Exception{
        URL crowdUrl = new URL(AuthManagerCrowd.DOMAIN + "session/" + crowdToken);
        HttpURLConnection connection = AuthManagerCrowd.crowdGet(crowdUrl);
        if (connection.getResponseCode() == 200) {
            //说明用户已经在登录
            String result = getMessageBody(connection);
            String username = result.substring(result.indexOf("name=\"") + 6, result.indexOf("\"><link"));
            return username;
        }
        else{
            return null;
        }
    }

}
