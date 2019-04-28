package com.harmonycloud.interceptors;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.user.CrowdConfigDto;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.user.AuthManagerCrowd;
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

//import com.harmonycloud.service.user.auth.AuthManagerCrowd;

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
    @Value("#{propertiesReader['api.access.allow.origin']}") private String allowOrigin;
    @Value("#{propertiesReader['api.url.whitelist']}") private String urlWhiteList;
    @Value("#{propertiesReader['sso.exclusion']}") private String urlExclusion;

    @Autowired private UserService userService;

    @Autowired private SystemConfigService systemConfigService;

    @Autowired private AuthManagerCrowd authManagerCrowd;

    @PostConstruct public void initWhiteList() {
        if (StringUtils.isBlank(urlWhiteList) || !urlWhiteList.contains("login")) {
            urlWhiteList = urlExclusion;
        }
        UrlWhiteListHandler.initUrlPattern(urlWhiteList);
    }

    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        //crsf漏洞 HTTP referer验证
        /*String referrer = request.getHeader("Referer");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(request.getScheme()).append("://").append(request.getServerName());
        if(StringUtils.isNotBlank(referrer) ){
            if(referrer.lastIndexOf(String.valueOf(stringBuffer)) != 0){//原站点不是第一个位置就是跨域 如http://localhost:8080/  http://lxlocalhost
                return false; //验证失败
            }
        }*/
        if (SsoClient.isOpen()) {
            return true;
        }
        // 设置跨域访问header信息
        if (StringUtils.isNotBlank(allowOrigin)) {
            String origin = allowOrigin;
            String requestOrigin = request.getHeader("Origin");
            if (StringUtils.isNotBlank(requestOrigin) && (allowOrigin.equals("*")
                || allowOrigin.indexOf(requestOrigin) > -1)) {
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
        if (UrlWhiteListHandler.isWhiteUrl(url)) {
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
        CrowdConfigDto crowdConfigDto = this.systemConfigService.findCrowdConfig();
        String username = (String)session.getAttribute("username");
        //容器云平台的admin用户永远不接入crowd进行单点登录
        if (username == null || username != null && !CommonConstant.ADMIN.equals(username)) {
            if (crowdConfigDto != null && crowdConfigDto.getIsAccess() != null && crowdConfigDto.getIsAccess() == 1
                && !CommonConstant.ADMIN.equals(username)) {
                //如果crowd接入了系统，则通过获取 Cookie检测登录状态
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals(authManagerCrowd.getCookieName())) {
                            String token = cookie.getValue();
                            String name = authManagerCrowd.testLogin(token);
                            if (name != null && !name.equals(CommonConstant.ADMIN)) {
                                session.setAttribute("username", name);
                                User user = userService.getUser(name);
                                if (user != null) {
                                    session.setAttribute("username", user.getUsername());
                                    session.setAttribute("isAdmin", user.getIsAdmin());
                                    session.setAttribute("isMachine", user.getIsMachine());
                                    session.setAttribute("userId", user.getId());
                                    return true;
                                }
                            }
                        }
                    }
                }
            } else {
                //如果未接入crowd，则通过session检测登录状态
                if (username != null) {
                    return true;
                }
            }

        } else {
            //如果未接入crowd，则通过session检测登录状态
            if (username != null) {
                return true;
            }
        }

        // 不符合条件的，返回401 Unauthorized
        response.setStatus(401);
        return false;
    }
}

