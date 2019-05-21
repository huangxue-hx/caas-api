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

// import com.harmonycloud.service.user.auth.AuthManagerCrowd;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private AuthManagerCrowd authManagerCrowd;

    @PostConstruct
    public void initWhiteList() {
        if (StringUtils.isBlank(urlWhiteList) || !urlWhiteList.contains("login")) {
            urlWhiteList = urlExclusion;
        }
        UrlWhiteListHandler.initUrlPattern(urlWhiteList);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {

        if (SsoClient.isOpen()) {
            return true;
        }
        // 设置跨域访问header信息
        if (StringUtils.isNotBlank(allowOrigin)) {
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
        if (UrlWhiteListHandler.isWhiteUrl(url)) {
            return true;
        }

        // 获取Session
        HttpSession session = request.getSession();
        CrowdConfigDto crowdConfigDto = this.systemConfigService.findCrowdConfig();
        String username = (String)session.getAttribute("username");
        LOGGER.info("当前用户：{}", username);
        // 容器云平台的admin用户永远不接入crowd进行单点登录
         if (isCrowdOn(crowdConfigDto) && !isAdmin(username)) {
//        if (isCrowdOn(crowdConfigDto)) {
            // 如果crowd接入了系统，则通过获取 Cookie检测登录状态
             LOGGER.info("进入单点登录验证...");
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    LOGGER.info("cookie:{}", cookie.getName());
                    // 判断中crowd中用户是否已经登录了
                    if (isCrowdLogin(cookie, session)) {
                        return true;
                    }
                }
                // 如果crowd中没有用户登录，但发现当前session是admin，也登录
                if (isAdmin(username)) {
                    return true;
                }
            }
        } else {
            // 如果未接入crowd，则通过session检测登录状态
            if (StringUtils.isNotBlank(username)) {
                return true;
            }
        }

        // 不符合条件的，返回401 Unauthorized
        response.setStatus(401);
        return false;
    }

    private boolean isAdmin(String username) {
        if (CommonConstant.ADMIN.equals(username)) {
            return true;
        }
        return false;
    }

    private boolean isCrowdOn(CrowdConfigDto crowdConfigDto) {
        if (crowdConfigDto != null && crowdConfigDto.getIsAccess() != null && crowdConfigDto.getIsAccess() == 1) {
            return true;
        }
        return false;
    }

    private boolean isCrowdLogin(Cookie cookie, HttpSession session) throws Exception {
        if (cookie.getName().equals(authManagerCrowd.getCookieName())) {
            String token = cookie.getValue().replace("\"", "");;
            String username = authManagerCrowd.testLogin(token);
            LOGGER.info("单点token 验证用户:{}", username);
            // crowd中的admin用户不单点登录
            if (StringUtils.isNotBlank(username) && !isAdmin(username)) {
                session.setAttribute("username", username);
                User user = userService.getUser(username);
                //用户不为空且不是黑名单
                if (user != null && !CommonConstant.PAUSE.equals(user.getPause())) {
                    session.setAttribute("username", user.getUsername());
                    session.setAttribute("isAdmin", user.getIsAdmin());
                    session.setAttribute("isMachine", user.getIsMachine());
                    session.setAttribute("userId", user.getId());
                    return true;
                }
            }
        }
        return false;
    }
}
