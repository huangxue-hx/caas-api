package com.harmonycloud.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Created by anson on 17/5/28.
 */
@Component
public class JenkinsClient {

    private static String host;

    private static String port;

    private static String username;

    private static String password;

    public static String getHost() {
        return host;
    }

    @Value("#{propertiesReader['jenkins.host']}")
    public void setHost(String host) {
        this.host = host;
    }

    public static String getPort() {
        return port;
    }

    @Value("#{propertiesReader['jenkins.port']}")
    public void setPort(String port) {
        this.port = port;
    }

    public static String getUsername() {
        return username;
    }

    @Value("#{propertiesReader['jenkins.username']}")
    public void setUsername(String username) {
        this.username = username;
    }

    public static String getPassword() {
        return password;
    }

    @Value("#{propertiesReader['jenkins.password']}")
    public void setPassword(String password) {
        this.password = password;
    }

    public static String getUrl(){
        return host + ":" + port;
    }

    public static String getApiToken(){
        String src = username + ":" + password;
        return Base64.getEncoder().encodeToString(src.getBytes());
    }


}
