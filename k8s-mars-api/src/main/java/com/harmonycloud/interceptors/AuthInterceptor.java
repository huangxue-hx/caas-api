package com.harmonycloud.interceptors;

import com.harmonycloud.api.user.AuthController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

public class AuthInterceptor extends HandlerInterceptorAdapter {
    private static final List<String> WHITE_URL_LIST = Arrays.asList(new String[]{"login","validation",
            "getToken","namespace/listByTenantid","clusters/list","clusters/getClusterBytenantId","clusters","systemConfig/trialTime"});
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthInterceptor.class);
    @Value("#{propertiesReader['api.access.allow.origin']}")
    private String allowOrigin;
    @Autowired
    AuthController AuthController;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 设置跨域访问header信息
        if(StringUtils.isNotBlank(allowOrigin)) {
            String origin = allowOrigin;
            String requestOrigin = request.getHeader("Origin");
            if (StringUtils.isNotBlank(requestOrigin)
                    && (allowOrigin.equals("*") || allowOrigin.indexOf(requestOrigin) > -1)) {
                origin = requestOrigin;
            }
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
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
        LOGGER.info("url=" + url);
        //路径包含openapi的不需要验证是否登陆，oam task定时任务没有用户
        if(url.indexOf("/openapi/")>-1){
            return true;
        }
        // 判断是否为白名单中的请求地址，是直接返回验证成功
        for(String whiteUrl : WHITE_URL_LIST){
            if(url.indexOf(whiteUrl)>-1){
                return true;
            }
        }
        // 获取Session
        HttpSession session = request.getSession();
        LOGGER.info("sessionId=" + session.getId());
        String username = (String) session.getAttribute("username");
        LOGGER.info("username=" + username);
        if (username != null) {
            return true;
        }

        // 不符合条件的，返回401 Unauthorized
        response.setStatus(401);
        return false;
    }
}

