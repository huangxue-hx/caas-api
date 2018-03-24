package com.harmonycloud.common.util;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.whchem.sso.client.entity.User;
import com.whchem.sso.common.utils.HttpClient;
import com.whchem.sso.common.utils.SSOConstants;
import com.whchem.sso.common.utils.SSOUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
@Component
public class SsoClient {
    private static Logger logger = LoggerFactory.getLogger(SsoClient.class);
    private static String login_protocol = "http";

    private static boolean open;

    private static String serverHost;

    public static void setServerHost(String server) {
        serverHost = server;
    }

    public static boolean isOpen() {
        return open;
    }
    @Value("#{propertiesReader['sso.open']}")
    public void setOpen(boolean open) {
        this.open = open;
    }

    /**
     * 通过cookie中的token获取sso用户
     */
    public static User getUserByCookie(HttpServletRequest request) {
        String token = SSOUtil.getCookieValue(request, SSOConstants.SSO_TOKEN);
        return getUserByToken(token, request);
    }

    /**
     * 通过header中的token获取sso用户
     */
    public static User getUserByHeader(HttpServletRequest request) {
        String token = request.getHeader("x-acl-signature");
        return getUserByToken(token, request);
    }
    
    /**
     * 通过token获取sso用户
     */
    private static User getUserByToken(String token, HttpServletRequest request) {
        if(StringUtils.isBlank(token)){
            return null;
        }
        String ssoInfo = com.whchem.sso.client.SSOClient.buildSSOInfo(token);
        Map<String, Object> reqBody = new HashMap();
        reqBody.put("ssoInfo", ssoInfo);
        String apiUrl = "http://" + serverHost + "/api/sso/getLoginUser";
        HttpClient.HttpResult result;
        try {
            result = com.whchem.sso.client.SSOClient.callAPI(apiUrl, reqBody);
        } catch (Exception e) {
            logger.error("获取用户失败,", e);
            throw new MarsRuntimeException(ErrorCodeMessage.HTTP_EXCUTE_FAILED);
        }
        JSONObject content = com.whchem.sso.client.SSOClient.parseResponse(result, "Get user info");
        if (content == null) {
            return null;
        } else {
            String encUser = content.getString("loginUser");
            if (encUser == null) {
                return null;
            } else {
                User user = com.whchem.sso.client.SSOClient.decryptUser(encUser);
                HttpSession session = request.getSession();
                session.setAttribute("username", user.getName());
                return user;
            }
        }
    }

    public static String buildLoginUrl(){
        return login_protocol + "://" + serverHost + "/sso/login?back_url=";
    }

    public static void setRedirectResponse(HttpServletResponse response){
        String loginUrl = buildLoginUrl();
        response.setHeader("Access-Control-Expose-Headers","Status,Location");
        response.setHeader("Status","302");
        response.setHeader("Location",loginUrl);
    }
    public static void dealHeader(HttpSession session){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = attributes.getResponse();
        if(SsoClient.isOpen()) {
            SsoClient.setRedirectResponse(response);
            session.invalidate();
            SsoClient.clearToken(response);
        }else {
            SsoClient.setRedirectResponse(response);
            session.invalidate();
        }
    }
    /**
     ** 清除cookie中的token
     */
    public static void clearToken(HttpServletResponse httpResponse){
        Cookie cookie = new Cookie(SSOConstants.SSO_TOKEN, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        httpResponse.addCookie(cookie);
    }
}
