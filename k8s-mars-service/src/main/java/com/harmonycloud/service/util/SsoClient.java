package com.harmonycloud.service.util;


import com.harmonycloud.dao.user.bean.User;
//import com.whchem.sso.client.SSOClient;
//import com.whchem.sso.common.utils.SSOConstants;
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

    /**
     * 通过header中的token获取sso用户
     */
    /*public static com.whchem.sso.client.entity.User getUserByHeader(HttpServletRequest request) {
        String token = request.getHeader("x-acl-signature");
        return SSOClient.getLoginUser(token);
    }*/

    public static void dealHeader(HttpSession session){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        redirectLogin(session, request, response);
    }

    public static void redirectLogin(HttpSession session, HttpServletRequest request, HttpServletResponse response){
        if(SsoClient.isOpen()) {
            session.invalidate();
            if(response != null) {
                clearToken(response);
            }
            /*if(request != null && response != null) {
                try {
                    SSOClient.doLogin(request, response);
                } catch (Exception e) {
                    logger.error("设置跳转单点登录页面错误", e);
                }
            }*/
        }else {
            session.invalidate();
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
        /*com.whchem.sso.client.entity.User ssoUser = SSOClient.getLoginUser(request, response);
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
