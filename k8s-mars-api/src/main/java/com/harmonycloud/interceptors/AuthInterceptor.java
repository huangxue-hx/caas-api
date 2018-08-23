package com.harmonycloud.interceptors;

import com.harmonycloud.api.user.AuthController;
import com.harmonycloud.common.util.DicUtil;
import com.harmonycloud.common.util.SsoClient;
import com.harmonycloud.filters.UrlWhiteListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 认证拦截器，
 */
public class AuthInterceptor extends HandlerInterceptorAdapter {
    private static final List<String> WHITE_URL_LIST = Arrays.asList(new String[]{"users/auth/login","validation","swagger-resources",
            "api-docs","/webjars","getToken","/clusters","system/configs/trialtime","cicd/trigger/webhookTrigger","users/auth/token","/testcallback"});
    private static List<Pattern> whilteList = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthInterceptor.class);
    @Value("#{propertiesReader['api.access.allow.origin']}")
    private String allowOrigin;
    @Value("#{propertiesReader['api.url.whitelist']}")
    private String urlWhiteList;
    @Value("#{propertiesReader['sso.exclusion']}")
    private String urlExclusion;
    @Autowired
    AuthController AuthController;

    @PostConstruct
    public void initWhiteList(){
        if(StringUtils.isBlank(urlWhiteList) || !urlWhiteList.contains("login")){
            urlWhiteList = urlExclusion;
        }
        UrlWhiteListHandler.initUrlPattern(urlWhiteList);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //crsf漏洞 HTTP referer验证
        String referrer = request.getHeader("Referer");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(request.getScheme()).append("://").append(request.getServerName());
        if(StringUtils.isNotBlank(referrer) ){
            if(referrer.lastIndexOf(String.valueOf(stringBuffer)) != 0){//原站点不是第一个位置就是跨域 如http://localhost:8080/  http://lxlocalhost
                return false; //验证失败
            }
        }
        //判断是否开启sso，开启则不执行拦截器的逻辑
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
        String username = (String) session.getAttribute("username");
        if (username != null) {
            return true;
        }

        // 不符合条件的，返回401 Unauthorized
        response.setStatus(401);
        return false;
    }
}

