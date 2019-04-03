package com.harmonycloud.common.util;

import com.offbytwo.jenkins.JenkinsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Base64;

/**
 * Created by anson on 17/5/28.
 */
@Component
public class JenkinsClient {
    private static final Logger logger = LoggerFactory.getLogger(JenkinsClient.class);

    private static String host;

    private static String port;

    private static String username;

    private static String password;

    private static JenkinsServer jenkinsServer;


    public static String getHost() {
        return host;
    }

//    @Value("#{propertiesReader['jenkins.host']}")
    public void setHost(String host) {
        this.host = host;
    }

    public static String getPort() {
        return port;
    }

//    @Value("#{propertiesReader['jenkins.port']}")
    public void setPort(String port) {
        this.port = port;
    }

    public static String getUsername() {
        return username;
    }

//    @Value("#{propertiesReader['jenkins.username']}")
    public void setUsername(String username) {
        this.username = username;
    }

    public static String getPassword() {
        return password;
    }

//    @Value("#{propertiesReader['jenkins.password']}")
    public void setPassword(String password) {
        this.password = password;
    }

    public static String getUrl(){
        return "http://"+host + ":" + port;
    }

    public static JenkinsServer getJenkinsServer() throws Exception {
        if(jenkinsServer == null){
            initJenkinsServer();
        }
        return jenkinsServer;
    }

    public static void setJenkinsServer(JenkinsServer jenkinsServer) {
        JenkinsClient.jenkinsServer = jenkinsServer;
    }

    public static String getApiToken(){
        String src = username + ":" + password;
        return Base64.getEncoder().encodeToString(src.getBytes());
    }

//    @PostConstruct
    public static void initJenkinsServer() throws Exception{
        logger.info("初始化jenkinsServer");
        if(jenkinsServer == null){
            jenkinsServer = new JenkinsServer(new URI("http://"+host + ":" +port), username, password);
        }
    }


}
