package com.harmonycloud.service.platform.client;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.service.common.HarborHttpsClientUtil;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.harmonycloud.common.Constant.CommonConstant.COOKIE;
import static com.harmonycloud.common.Constant.CommonConstant.CREATETIME;

/**
 * Created by zsl on 2017/1/18.
 */
public class HarborClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HarborClient.class);
    private static final long COOKIE_TIMEOUT_CHECK = 10000L;

    //admin用户登录各个harbor服务器的cookie
    public static Map<String,Map<String, String>> adminCookies = new ConcurrentHashMap<>();

    public static String getHarborUrl(HarborServer harborServer) {
        return harborServer.getHarborProtocol() + "://" + harborServer.getHarborHost()+":"+harborServer.getHarborPort();
    }

    public static Map<String, Object> getAdminCookieHeader(HarborServer harborServer) throws Exception {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(COOKIE, checkHarborAdminCookie(harborServer));
        return headers;
    }

    public static void clearCookie(String harborHost) throws Exception {
        adminCookies.remove(harborHost);
    }

    /**
     * 获取harbor admin 登录的cookie, 先检查harbor登录是否超时，如果超时重新登录获取最新cookie，没有超时获取上次登录的cookie
     *
     * @return
     * @throws Exception
     */
    public static String checkHarborAdminCookie(HarborServer harborServer) throws Exception {
        if(harborServer == null){
            return null;
        }
        Map<String, String> cookieMap = adminCookies.get(harborServer.getHarborHost());
        //不存在该集群对应的admin登录cookie信息，登录并记录cookie
        if(cookieMap == null || cookieMap.get(COOKIE) == null){
            String cookie = loginWithAdmin(harborServer);
            Map<String, String> adminCookie = new HashMap<>();
            adminCookie.put(COOKIE,cookie);
            adminCookie.put(CREATETIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
            adminCookies.put(harborServer.getHarborHost(), adminCookie);
            return cookie;
        }
        String createTime = cookieMap.get(CREATETIME);
        long interval = createTime == null ? 0: new Date().getTime() - Long.valueOf(createTime);
        //上次登录时间距今是否超过设置的harbor连接时间，超过重新登录
        //离超时时间还有10s 重现登录
        if (interval > harborServer.getHarborLoginTimeOut() - COOKIE_TIMEOUT_CHECK) {
            // 重新登陆
            cookieMap.put(COOKIE,  loginWithAdmin(harborServer));
            cookieMap.put(CREATETIME, String.valueOf(new Date(System.currentTimeMillis()).getTime()));
        }
        return cookieMap.get(COOKIE);
    }

    public static String loginWithAdmin(HarborServer harborServer) throws Exception {
        String url = getHarborUrl(harborServer) + "/login";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("principal", harborServer.getHarborAdminAccount());
        params.put("password", harborServer.getHarborAdminPassword());
        CloseableHttpResponse response = HarborHttpsClientUtil.doPostWithLogin(url, params, null);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatusLine().getStatusCode())){
            return null;
        }
        if(response.getHeaders("Set-Cookie") == null){
            return null;
        }
        String cookie = response.getHeaders("Set-Cookie")[0].getValue();
        return cookie.substring(0, cookie.indexOf(";"));
    }

    public static boolean checkHarborStatus(HarborServer harborServer){
        try {
            String cookie = HarborClient.loginWithAdmin(harborServer);
            if(StringUtils.isNotBlank(cookie)){
                return true;
            }
        }catch (Exception e){
            LOGGER.error("harbor 登录失败,harborServer:{}", JSONObject.toJSONString(harborServer),e);
        }
        return false;
    }




}
