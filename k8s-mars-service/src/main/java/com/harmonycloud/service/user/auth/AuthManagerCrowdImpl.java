package com.harmonycloud.service.user.auth;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.user.CrowdConfigDto;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.user.AuthManagerCrowd;
import com.harmonycloud.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Base64;

@Service
public class AuthManagerCrowdImpl implements AuthManagerCrowd {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private UserService userService;

    @Value("#{propertiesReader['crowd.cookie.domain']}")
    private String cookieDomain;

    @Value("#{propertiesReader['crowd.cookie.name']}")
    private String cookieName;

    @Value("#{propertiesReader['crowd.api.url']}")
    private String apiUrl;

    @Value("#{propertiesReader['ip']}")
    private String ip;

    // 测试能否连通crowd服务器
    public boolean testCrowd(CrowdConfigDto crowdConfigDto) throws Exception {
        URL url = new URL(crowdConfigDto.getAddress());
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.connect();
        if (connection.getResponseCode() != 200) {
            return false;
        }
        return true;
    }

    // 获得crowd的域名
    private String getAddress() {
        CrowdConfigDto crowdConfigDto = systemConfigService.findCrowdConfig();
        String domain = crowdConfigDto.getAddress().trim();
        if (domain.endsWith("/")) {
            return domain + apiUrl;
        } else {
            return domain + "/" + apiUrl;
        }

    }

    private String getServerIp() throws Exception {
//        InetAddress addr = InetAddress.getLocalHost();
//        return addr.getHostAddress();
         return ip;
    }

    // 进行http基本认证
    private HttpURLConnection authentication(HttpURLConnection connection) throws Exception {
        // http基本认证
        CrowdConfigDto crowdConfigDto = systemConfigService.findCrowdConfig();
        String username = crowdConfigDto.getUsername().trim();
        String password = crowdConfigDto.getPassword().trim();
        String base64encodedString = Base64.getEncoder().encodeToString((username + ":" + password).getBytes("utf-8"));
        connection.setRequestProperty("Authorization", "Basic " + base64encodedString);
        return connection;
    }

    public HttpURLConnection crowdPost(URL url, String contenttype, String data) throws Exception {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", contenttype);
        connection.setRequestProperty("Charset", "UTF-8");
        // http基本认证

        connection = authentication(connection);
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());

        out.write(new String(data.getBytes("UTF-8")));
        out.flush();
        out.close();
        return connection;
    }

    public HttpURLConnection crowdGet(URL url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("connection", "Keep-Alive");
        // http基本认证
        connection = authentication(connection);
        connection.connect();
        return connection;
    }

    public HttpURLConnection crowdDelete(URL url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("connection", "Keep-Alive");
        // http基本认证

        connection = authentication(connection);
        connection.connect();
        return connection;
    }

    public String getMessageBody(HttpURLConnection connection) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuffer result = new StringBuffer();
        for (line = br.readLine(); line != null; line = br.readLine()) {
            result.append(line);
        }
        return result.toString();
    }

    @Override
    public String auth(String username, String password) throws Exception {
        URL url = new URL(getAddress() + "session");
//        String jsonData = "{\"username\":\"" + username + "\",\"password\":\"" + password
//            + "\",\"validation-factors\": {\"validationFactors\": [{\"name\":\"remote_address\",\"value\":\""
//            + getServerIp() + "\"}]}}";
        String jsonData = "{\"username\":\"" + username + "\",\"password\":\"" + password
            + "\",\"validation-factors\": {\"validationFactors\": []}}";
        HttpURLConnection connection = this.crowdPost(url, "application/json", jsonData);
        if (connection.getResponseCode() == 201) {
            User user = getUser(username, password);
            if (user != null) {
                // 在容器云平台添加用户
                userService.addUser(user);
            }
            return username;
        } else {
            // 打日志
            logger.error("验证出错，crowd返回" + connection.getResponseCode());
            return null;
        }
    }

    public boolean addUser(String username, String password, String realname, String email, String phone)
        throws Exception {

        URL crowdurl = new URL(getAddress() + "user");
        String xmlData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><user name=\"" + username
            + "\" expand=\"attributes\"><first-name>" + realname + "</first-name><last-name>" + realname
            + "</last-name><email>" + email + "</email><active>true</active><attributes><link href=\"" + getAddress()
            + "user/attribute?username=" + username
            + "\" rel=\"self\"/></attributes><password><link rel=\"edit\" href=\"" + getAddress()
            + "/user/password?username=" + username + "\"/><value>" + password + "</value></password></user>";
        // 创建用户
        HttpURLConnection httpURLConnection = this.crowdPost(crowdurl, "application/xml", xmlData);
        if (httpURLConnection.getResponseCode() == 201) {
            // 添加phone属性
            URL phoneurl = new URL(getAddress() + "user/attribute?username=" + username);
            String phoneJson = "{\"attributes\": [{\"name\": \"phone\",\"values\": [\"" + phone + "\"]}]}";
            HttpURLConnection phonecon = this.crowdPost(phoneurl, "application/json", phoneJson);
            if (phonecon.getResponseCode() != 204) {
                logger.error("添加用户出错，crowd返回" + phonecon.getResponseCode());
                return false;
            } else {
                return true;
            }
        } else {
            logger.error("添加用户出错，crowd返回" + httpURLConnection.getResponseCode());
            return false;
        }
    }

    public User getUser(String username, String password) throws Exception {
        URL crowdurl = new URL(getAddress() + "user?username=" + username);
        HttpURLConnection urlConnection = this.crowdGet(crowdurl);
        if (urlConnection.getResponseCode() != 200) {
            logger.error("获取信息出错，crowd返回" + urlConnection.getResponseCode());
            return null;
        }
        String messageBody = this.getMessageBody(urlConnection);
        String email = messageBody.substring(messageBody.indexOf("<email>") + "<email>".length(),
            messageBody.lastIndexOf("</email>"));
        // 获取phone值
        URL phoneurl = new URL(getAddress() + "user/attribute?username=" + username);
        HttpURLConnection urlPhoneConnection = this.crowdGet(phoneurl);
        if (urlPhoneConnection.getResponseCode() != 200) {
            logger.error("获取信息出错，crowd返回" + urlPhoneConnection.getResponseCode());
            return null;
        }
        messageBody = this.getMessageBody(urlPhoneConnection);
        String phone = "";
        if (messageBody.indexOf("<attribute name=\"phone\">") != -1) {
            // 有相应的phone属性
            messageBody = messageBody
                .substring(messageBody.indexOf("<attribute name=\"phone\">") + "<attribute name=\"phone\">".length());
            phone = messageBody.substring(messageBody.indexOf("<value>") + "<value>".length(),
                messageBody.indexOf("</value>"));
        } else {
            logger.error("crowd中用户的信息不全，无法添加用户");
            // 找不到crowd中的phone属性
            return null;
        }
        // 组装用户数据
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

    public String getToken(String username, String password) throws Exception {
        URL url = new URL(getAddress() + "session");
        String jsonData = "{\"username\":\"" + username + "\",\"password\":\"" + password
            + "\",\"validation-factors\": {\"validationFactors\": [{\"name\":\"remote_address\",\"value\":\"" + getServerIp() + "\"}]}}";
        HttpURLConnection connection = this.crowdPost(url, "application/json", jsonData);
        if (connection.getResponseCode() == 201) {
            String messageBody = this.getMessageBody(connection);
            return messageBody.substring(messageBody.indexOf("<token>") + "<token>".length(),
                messageBody.lastIndexOf("</token>"));
        } else {
            logger.error("获取token信息出错，crowd返回" + connection.getResponseCode());
            return null;
        }
    }

    public String getToken(String username) throws Exception {
        URL url = new URL(getAddress() + "session?validate-password=false");
        String jsonData = "{\"username\":\"" + username
            + "\",\"validation-factors\": {\"validationFactors\": [{\"name\":\"remote_address\",\"value\":\""
            + getServerIp() + "\"}]}}";
        HttpURLConnection connection = this.crowdPost(url, "application/json", jsonData);
        if (connection.getResponseCode() == 201) {
            String messageBody = this.getMessageBody(connection);
            return messageBody.substring(messageBody.indexOf("<token>") + "<token>".length(),
                messageBody.lastIndexOf("</token>"));
        } else {
            logger.error("获取token信息出错，crowd返回" + connection.getResponseCode());
            return null;
        }
    }

    public void invalidateToken(String username) throws Exception {
        // 在crowd中清除登录信息
        URL url = new URL(getAddress() + "session?username=" + username);

        HttpURLConnection connection = this.crowdDelete(url);
        if (connection.getResponseCode() != 204) {
            logger.error("删除用户登录信息出错，crowd返回" + connection.getResponseCode());
        }
    }

    public void addCookie(String crowdToken, HttpServletResponse response) throws Exception {
        // 将crowd中token的值存入token
        Cookie cookie = new Cookie(cookieName, crowdToken);
        cookie.setPath("/"); // 如果路径为/则为整个tomcat目录有用
        cookie.setDomain(cookieDomain); // 设置对所有*.harmonycloud.com为后缀的域名
        response.addCookie(cookie);
    }

    // 用于检测用户是否登录
    public String testLogin(String crowdToken) throws Exception {
        URL crowdUrl = new URL(getAddress() + "session/" + crowdToken);
        HttpURLConnection connection = this.crowdGet(crowdUrl);
        if (connection.getResponseCode() == 200) {
            // 说明用户已经在登录
            String result = getMessageBody(connection);
            String username =
                result.substring(result.indexOf("name=\"") + "name=\"".length(), result.indexOf("\"><link"));
            return username;
        } else {
            return null;
        }
    }

    public String getCookieName() {
        return cookieName;
    }

}