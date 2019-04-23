package com.harmonycloud.interceptors;

import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.service.user.UserService;
import com.harmonycloud.service.util.SsoClient;
import com.harmonycloud.filters.UrlWhiteListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.harmonycloud.service.user.auth.AuthManagerCrowd;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;

/**
 * 认证拦截器，
 */
public class AuthInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthInterceptor.class);
    @Value("#{propertiesReader['api.access.allow.origin']}")
    private String allowOrigin;
    @Value("#{propertiesReader['api.url.whitelist']}")
    private String urlWhiteList;
    @Value("#{propertiesReader['sso.exclusion']}")
    private String urlExclusion;


    @Autowired
    private UserService userService;

    @PostConstruct
    public void initWhiteList(){
        if(StringUtils.isBlank(urlWhiteList) || !urlWhiteList.contains("login")){
            urlWhiteList = urlExclusion;
        }
        UrlWhiteListHandler.initUrlPattern(urlWhiteList);
    }

//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        //crsf漏洞 HTTP referer验证
//        /*String referrer = request.getHeader("Referer");
//        StringBuffer stringBuffer = new StringBuffer();
//        stringBuffer.append(request.getScheme()).append("://").append(request.getServerName());
//        if(StringUtils.isNotBlank(referrer) ){
//            if(referrer.lastIndexOf(String.valueOf(stringBuffer)) != 0){//原站点不是第一个位置就是跨域 如http://localhost:8080/  http://lxlocalhost
//                return false; //验证失败
//            }
//        }*/
//        System.out.println("test interceptor!");
//        if(SsoClient.isOpen()){
//            return true;
//        }
//        // 设置跨域访问header信息
//        if(StringUtils.isNotBlank(allowOrigin)) {
//            String origin = allowOrigin;
//            String requestOrigin = request.getHeader("Origin");
//            if (StringUtils.isNotBlank(requestOrigin)
//                    && (allowOrigin.equals("*") || allowOrigin.indexOf(requestOrigin) > -1)) {
//                origin = requestOrigin;
//            }
//            response.setHeader("Access-Control-Allow-Origin", origin);
//            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, PATCH");
//            response.setHeader("Access-Control-Max-Age", "1200");
//            response.setHeader("Access-Control-Allow-Headers", "X-Requested-With,Content-Type");
//            response.setHeader("Access-Control-Allow-Credentials", "true");
//        }
//        String httpMethod = request.getMethod();
//        System.out.println("httpMethod:" + httpMethod);
//        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(httpMethod)) {
//            return true;
//        }
//        // 获取请求的URL
//        String url = request.getRequestURI();
//        System.out.println("url" + url);
//        if(UrlWhiteListHandler.isWhiteUrl(url)){
//            return true;
//        }
//       /* //路径包含openapi的不需要验证是否登陆，oam task定时任务没有用户
//        if(url.indexOf("/openapi/")>-1){
//            return true;
//        }
//        // 判断是否为白名单中的请求地址，是直接返回验证成功
//        for(String whiteUrl : WHITE_URL_LIST){
//            if(url.indexOf(whiteUrl)>-1){
//                return true;
//            }
//        }*/
//        // 获取Session
//        HttpSession session = request.getSession();
//        String username = (String) session.getAttribute("username");
//        System.out.println("username" + username);
//        if (username != null) {
//            return true;
//        }
//
//        // 不符合条件的，返回401 Unauthorized
//        response.setStatus(401);
//        return false;
//    }



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //crsf漏洞 HTTP referer验证
        /*String referrer = request.getHeader("Referer");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(request.getScheme()).append("://").append(request.getServerName());
        if(StringUtils.isNotBlank(referrer) ){
            if(referrer.lastIndexOf(String.valueOf(stringBuffer)) != 0){//原站点不是第一个位置就是跨域 如http://localhost:8080/  http://lxlocalhost
                return false; //验证失败
            }
        }*/
        if(SsoClient.isOpen()){
            return true;
        }
        // 设置跨域访问header信息
        if(StringUtils.isNotBlank(allowOrigin)) {
            String origin = allowOrigin;
            String requestOrigin = request.getHeader("Origin");
            if (StringUtils.isNotBlank(requestOrigin)
                    && (allowOrigin.equals("*") || allowOrigin.indexOf(requestOrigin) > -1)) {
                origin = requestOrigin;
            }
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, PATCH");
            response.setHeader("Access-Control-Max-Age", "1200");
            response.setHeader("Access-Control-Allow-Headers", "X-Requested-With,Content-Type");
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }
        String httpMethod = request.getMethod();
        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(httpMethod)) {
            return true;
        }
        // 获取请求的URL
        String url = request.getRequestURI();
        if(UrlWhiteListHandler.isWhiteUrl(url)){
            return true;
        }
       /* //路径包含openapi的不需要验证是否登陆，oam task定时任务没有用户
        if(url.indexOf("/openapi/")>-1){
            return true;
        }
        // 判断是否为白名单中的请求地址，是直接返回验证成功
        for(String whiteUrl : WHITE_URL_LIST){
            if(url.indexOf(whiteUrl)>-1){
                return true;
            }
        }*/


        // 获取Session
        HttpSession session = request.getSession();
        //获取 Cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("crowd.token_key")) {// 是否是自动登录。。。。//要先get /session看返回码。但实际上应该还要有更多的判断条件，比如这个application能否访问这个用户
                    String token = cookie.getValue();
                    URL crowdUrl = new URL(AuthManagerCrowd.DOMAIN + "session/" + token);
                    HttpURLConnection connection = AuthManagerCrowd.crowdGet(crowdUrl);
                    if (connection.getResponseCode() == 200) {
                        //说明用户已经在登录
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                        String line;
                        String result = "";
                        //读取返回值，直到为空
                        while ((line = in.readLine()) != null) {
                            result = result + line + "\n";
                        }
                        String username = result.substring(result.indexOf("name=\"") + 6, result.indexOf("\"><link"));
                        session.setAttribute("username", username);
                        User user = userService.getUser(username);
                        if (user == null) {
                            user = new User();
                            user.setUsername(username);
                            user.setIsAdmin(0);
                            user.setIsMachine(0);
                        }

                        session.setAttribute("username", user.getUsername());
                        session.setAttribute("isAdmin", user.getIsAdmin());
                        session.setAttribute("isMachine", user.getIsMachine());
                        session.setAttribute("userId", user.getId());
                        return true;
//                        session.setAttribute("language", language);
                    }
                }
            }
        }
//        String username = (String) session.getAttribute("username");
//        System.out.println("username:" + username);
//        if (username != null) {
//            return true;
//        }

        // 不符合条件的，返回401 Unauthorized
        response.setStatus(401);
        return false;
    }
}

