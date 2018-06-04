package com.harmonycloud.common.util;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.date.DateUtil;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class ApmHttpClient {


    private static String loginUrl = null;
    private static String username = null;
    private static String password = null;
    private static int TIMEOUT = 6000000;

    private static CloseableHttpClient httpClient = null;
    private static boolean login = false;
    private static Date lastLoginTime = null;

    public static String getLoginUrl() {
        return loginUrl;
    }

    public static void setLoginUrl(String loginUrl) {
        ApmHttpClient.loginUrl = loginUrl;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        ApmHttpClient.username = username;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        ApmHttpClient.password = password;
    }

    public static boolean login() throws Exception {

        if(httpClient != null){
            httpClient.close();
        }
        httpClient = HttpSslClientUtil.createHttpsClient();
        HttpPost httpPost = new HttpPost(loginUrl);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userName", username);
        params.put("password", password);
        UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(
                getParam(params), "UTF-8");
        httpPost.setEntity(postEntity);
        try {
            // 执行post请求
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            Integer statusCode = httpResponse.getStatusLine().getStatusCode();
            if (HttpStatusUtil.isSuccessStatus(statusCode)) {
                login = true;
                lastLoginTime = new Date();
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public static ActionReturnUtil httpsGetRequest(String url, Map<String, Object> headers,
                                                   Map<String, Object> params) throws Exception{
        if(!login || isLoginTimeOut()){
            login();
        }
        HttpClientResponse httpClientResponse = new HttpClientResponse();
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);
        if (params != null) {
            ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
            ub.setParameters(pairs);
        }
        try {
            // 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .build();
            HttpGet httpGet = new HttpGet(ub.build());
            httpGet.setConfig(requestConfig);
            if (headers != null) {
                for (Map.Entry<String, Object> param : headers.entrySet()) {
                    httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
                }
            }
            CloseableHttpResponse response = httpClient.execute(httpGet);
            Integer statusCode = response.getStatusLine().getStatusCode();
            httpClientResponse.setStatus(statusCode);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                httpClientResponse.setBody(result);
                response.close();
                if (HttpStatusUtil.isSuccessStatus(statusCode)) {
                    return ActionReturnUtil.returnSuccessWithData(result);
                } else {
                    return ActionReturnUtil.returnErrorWithData(result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.HTTP_EXCUTE_FAILED, e.getMessage(), false);
        }
        return ActionReturnUtil.returnError();
    }

    /**
     * 发起POST请求,如果没有header和params则设置为null
     *
     * @param url
     * @param headers
     *            请求头
     * @param params
     *            设置参数
     * @return
     * @throws Exception
     */
    public static ActionReturnUtil httpsPostRequest(String url, Map<String, Object> headers,
                                                   Map<String, Object> params) throws Exception {
        if(!login || isLoginTimeOut()){
            login();
        }
        HttpClientResponse httpClientResponse = new HttpClientResponse();

        try {

            // 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .build();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            if (headers != null) {
                for (Map.Entry<String, Object> param : headers.entrySet()) {
                    httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
                }
            }
            if (params != null) {
                ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
                httpPost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
            }
            CloseableHttpResponse response = httpClient.execute(httpPost);
            Integer statusCode = response.getStatusLine().getStatusCode();
            httpClientResponse.setStatus(statusCode);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                httpClientResponse.setBody(result);
                response.close();
                if (HttpStatusUtil.isSuccessStatus(statusCode)) {
                    return ActionReturnUtil.returnSuccessWithData(result);
                } else {
                    return ActionReturnUtil.returnErrorWithMsg(result);
                }
            }
        } catch (Exception e) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.HTTP_EXCUTE_FAILED, e.getMessage(), false);
        }
        return ActionReturnUtil.returnError();
    }

    private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, Object> params) {
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
        }
        return pairs;
    }



    public static List<NameValuePair> getParam(Map parameterMap) {
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        Iterator it = parameterMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry parmEntry = (Entry) it.next();
            param.add(new BasicNameValuePair((String) parmEntry.getKey(),
                    (String) parmEntry.getValue()));
        }
        return param;
    }

    private static boolean isLoginTimeOut(){
        if(lastLoginTime == null){
            return true;
        }
        if(DateUtil.addMinute(lastLoginTime,10).before(new Date())){
            return true;
        }
        return false;
    }
}
