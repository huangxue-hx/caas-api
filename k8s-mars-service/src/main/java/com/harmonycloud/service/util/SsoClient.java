package com.harmonycloud.service.util;


import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.dao.user.bean.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class SsoClient {
    private static Logger logger = LoggerFactory.getLogger(SsoClient.class);

    private static boolean open;

    public static boolean isOpen() {
        return open;
    }
    @Value("#{propertiesReader['sso.open']}")
    public void setOpen(boolean open) {
        this.open = open;
    }



    public static void dealHeader(HttpSession session){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        redirectLogin(session, request, response);
    }

    public static void redirectLogin(HttpSession session, HttpServletRequest request, HttpServletResponse response){
        session.invalidate();
        if(SsoClient.isOpen()) {
            if(response != null) {
                clearToken(response);
            }
            if(request != null && response != null) {
                try {
                    //SSOClient.doLogin(request, response);
                } catch (Exception e) {
                    logger.error("设置跳转单点登录页面错误", e);
                }
            }
        }
    }

    /**
     ** 清除cookie中的token
     */
    public static void clearToken(HttpServletResponse httpResponse){
        /*Cookie cookie = new Cookie(SSOConstants.SSO_TOKEN, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        httpResponse.addCookie(cookie);*/
    }

    public static User getLoginUser(HttpServletRequest request, HttpServletResponse response){
       /* User ssoUser = SSOClient.getLoginUser(request, response);
        if(ssoUser == null){
            return null;
        }
        User user = new User();
        user.setUsername(ssoUser.getName().toLowerCase());
        user.setRealName(ssoUser.getDisplayName());
        user.setEmail(ssoUser.getEmail());
        user.setPhone(ssoUser.getMobile());
        return user;*/
       return null;
    }
}
