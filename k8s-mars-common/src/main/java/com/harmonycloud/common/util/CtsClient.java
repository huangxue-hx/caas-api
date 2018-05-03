package com.harmonycloud.common.util;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-1-9
 * @Modified
 */
@Component
public class CtsClient {
    private static final Logger logger = LoggerFactory.getLogger(CtsClient.class);
    public static final String SUCCESS_CODE = "100";
    public static final String FAILURE = "failure";
    public static final String SUCCESS = "success";

    private static String ctsUrl;

    private static String apiUrl;

    private static String userAccount;

    private static String username;

    private static String password;

    public static String getCtsUrl() {
        return ctsUrl;
    }
    @Value("#{propertiesReader['cts.url']}")
    public void setCtsUrl(String ctsUrl) {
        CtsClient.ctsUrl = ctsUrl;
    }

    public static String getApiUrl() {
        return apiUrl;
    }
    @Value("#{propertiesReader['api.url']}")
    public void setApiUrl(String apiUrl) {
        CtsClient.apiUrl = apiUrl;
    }

    public static String getUserAccount() {
        return userAccount;
    }
    @Value("#{propertiesReader['cts.account']}")
    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public static String getUsername() {
        return username;
    }
    @Value("#{propertiesReader['auth.username']}")
    public void setUsername(String username) {
        this.username = username;
    }

    public static String getPassword() {
        return password;
    }
    @Value("#{propertiesReader['auth.password']}")
    public  void setPassword(String password) {
        this.password = password;
    }

    public static HttpClientResponse exec(String url, String method, Map<String, Object> headers, Map<String, Object> bodys) throws Exception{
        HttpClientResponse response = null;

        Map<String, Object> newHeader = headers == null? new HashMap<>(): headers;
        newHeader.put("Authorization", "Basic " + getToken());
        try {
            switch (method) {
                case "POST":
                    response = HttpClientUtil.httpPostJsonRequest(url, newHeader, bodys);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            throw new MarsRuntimeException(ErrorCodeMessage.HTTP_EXCUTE_FAILED);
        }
        return response;
    }

    public static String buildGetSuitsUrl(){
        StringBuilder sb= new StringBuilder(getCtsUrl()).append("/cts/service/getSysSuiteApi.do");
        return sb.toString();
    }

    public static String buildExecuteSuiteUrl(){
        StringBuilder sb = new StringBuilder(getCtsUrl()).append("/cts/service/runSuiteApi.do");
        return sb.toString();
    }

    public static String buildCallBackUrl(Integer stageId, Integer buildNum){
        StringBuilder sb = new StringBuilder(getApiUrl())
                .append("/rest/cicdjobs/stage/")
                .append(stageId)
                .append("/result/")
                .append(buildNum)
                .append("/testcallback");
        return sb.toString();
    }

    public static String getToken(){
        String src = username + ":" + password;
        return Base64.getEncoder().encodeToString(src.getBytes());
    }

    public static void main(String[] args) throws Exception {
        Map param = new HashMap();
        param.put("sysCode","O1802");
        Map header = new HashMap();
        header.put("Authorization", "Basic Y3N6d2RoOlNpbm9wZWMyMDE3IQ==");
        HttpClientResponse response = CtsClient.exec("http://ctsqas.whchem.com/cts/service/getSysSuiteApi.do", "POST",header,param);

        String re = response.getBody();
        //String body = response.getBody();
        //String body="{\"code\":100,\"msg\":\"操作成功\",\"result\":[{\"projectId\":\"projectId01\",\"projectName\":\"CTS测试项目\",\"sysId\":\"sysId01\",\"sysName\":\"sysName\",\"suiteList\":[{\"suiteId\":\"suiteId01\",\"suiteName\":\"冒烟测试\"},{\"suiteId\":\"suiteId03\",\"suiteName\":\"clone测试\"}]}]}";
        Map map = JsonUtil.convertJsonToMap(re);
        List<Map> allSuiteList = new ArrayList<>();
        if(null != map.get("code") && SUCCESS_CODE.equals(String.valueOf(map.get("code")))){
            List<Map> resultMapList = (List<Map>)map.get("result");
            for(Map resultMap:resultMapList){
                List<Map> suiteList = (List<Map>)resultMap.get("suiteList");
                allSuiteList.addAll(suiteList);
            }
        }
    }
}
