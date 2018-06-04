package com.harmonycloud.common.util;



import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anson on 17/5/28.
 */
public class HttpJenkinsClientUtil {
    private static int TIMEOUT = 6000000;

    private static CloseableHttpClient getHttpClient() throws Exception {
//		return HttpClients.createDefault();
        return HttpSslClientUtil.createHttpsClient();
    }


    public static ActionReturnUtil httpGetRequest(String url, Map<String, Object> headers,
                                                  Map<String, Object> params, boolean getResponseHeader){
        HttpClientResponse httpClientResponse = new HttpClientResponse();
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        URIBuilder ub = new URIBuilder();
        String jenkinsUrl = JenkinsClient.getUrl();
        ub.setPath(jenkinsUrl + url);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                .build();

        if (params != null) {
            ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
            ub.setParameters(pairs);
        }

       try {
            HttpGet httpGet = new HttpGet(ub.build());


            httpGet.setConfig(requestConfig);
            if (headers == null) {
                headers = new HashMap<String, Object>();
            }
            headers.put("Authorization", "Basic " + JenkinsClient.getApiToken());

            for (Map.Entry<String, Object> param : headers.entrySet()) {
                httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
            }

            httpClient = getHttpClient();
            response = httpClient.execute(httpGet);
            Integer statusCode = response.getStatusLine().getStatusCode();
            httpClientResponse.setStatus(statusCode);
            HttpEntity entity = response.getEntity();
           if(getResponseHeader){
               Map returnMap = new HashMap<>();
               returnMap.put("header", response.getAllHeaders());
               if (entity != null) {
                   returnMap.put("body", EntityUtils.toString(entity));
               }
               if (HttpStatusUtil.isSuccessStatus(statusCode)) {
                   return ActionReturnUtil.returnSuccessWithData(returnMap);
               }
           }
            else if (entity != null) {
                String result = EntityUtils.toString(entity);
                httpClientResponse.setBody(result);
                if (HttpStatusUtil.isSuccessStatus(statusCode)) {
                    return ActionReturnUtil.returnSuccessWithData(result);
                } else {
                    return ActionReturnUtil.returnErrorWithData(result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
           //释放连接
           try {
               if (httpClient != null){
                   response.close();
               }
               if (httpClient != null){
                   httpClient.close();
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
        }
        return ActionReturnUtil.returnError();
    }

    public static ActionReturnUtil httpPostRequest(String url, Map<String, Object> headers,
                                                  Map<String, Object> params, String body, Integer expectStatusCode){
        HttpClientResponse httpClientResponse = new HttpClientResponse();
        CloseableHttpClient httpClient = null;
        URIBuilder ub = new URIBuilder();
        String jenkinsUrl = JenkinsClient.getUrl();
        ub.setPath(jenkinsUrl + url);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                .build();

        try {
            HttpPost httpPost = new HttpPost(ub.build());
            httpPost.setConfig(requestConfig);
            if (headers == null) {
                headers = new HashMap<String, Object>();
            }

            headers.put("Authorization", "Basic " + JenkinsClient.getApiToken());
            for (Map.Entry<String, Object> param : headers.entrySet()) {
                httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
            }
            if (params != null) {
                ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
                httpPost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
            }
            if(body != null) {
                HttpEntity requestEntity = new StringEntity(body,"UTF-8");
                httpPost.setEntity(requestEntity);
            }
            httpClient = getHttpClient();
            CloseableHttpResponse response = httpClient.execute(httpPost);
            Integer statusCode = response.getStatusLine().getStatusCode();
            httpClientResponse.setStatus(statusCode);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                httpClientResponse.setBody(result);
                response.close();
                if(null != expectStatusCode && expectStatusCode.equals(statusCode)){
                    return ActionReturnUtil.returnSuccessWithData(result);
                }
                else if (HttpStatusUtil.isSuccessStatus(statusCode)) {
                    return ActionReturnUtil.returnSuccessWithData(result);
                } else {
                    return ActionReturnUtil.returnErrorWithMap("message", result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (httpClient != null)
                    httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ActionReturnUtil.returnError();
    }

    public static ActionReturnUtil httpDeleteRequest(String url, Map<String, Object> headers,
                                                   Map<String, Object> params){
        HttpClientResponse httpClientResponse = new HttpClientResponse();
        CloseableHttpClient httpClient = null;
        URIBuilder ub = new URIBuilder();
        String jenkinsUrl = JenkinsClient.getUrl();

        ub.setPath(jenkinsUrl + url);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                .build();

        if (params != null) {
            ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
            ub.setParameters(pairs);
        }

        HttpDelete httpDelete = null;
        try {
            httpDelete = new HttpDelete(ub.build());


            httpDelete.setConfig(requestConfig);
            if (headers == null) {
                headers = new HashMap<String, Object>();
            }
            headers.put("Authorization", "Basic " + JenkinsClient.getApiToken());

            for (Map.Entry<String, Object> param : headers.entrySet()) {
                httpDelete.addHeader(param.getKey(), String.valueOf(param.getValue()));
            }

            httpClient = getHttpClient();
            CloseableHttpResponse response = httpClient.execute(httpDelete);
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
        }finally {
            try {
                if (httpClient != null)
                    httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ActionReturnUtil.returnError();
    }

    /**
     * 将map转换为请求中的参数
     *
     * @param params
     * @return
     */
    private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, Object> params) {
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
        }
        return pairs;
    }
}
