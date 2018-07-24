package com.harmonycloud.filters;


import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.MicroServiceCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.SsoClient;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dao.user.bean.UrlDic;
import com.harmonycloud.dto.tenant.TenantDto;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.RolePrivilegeService;
import com.harmonycloud.service.user.UrlDicService;
import com.whchem.sso.client.SSOClient;
import com.whchem.sso.client.entity.User;
import com.whchem.sso.common.utils.SSOConstants;
import com.whchem.sso.common.utils.SSOUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SSO filter
 * @author
 * created at 2017/12/14
 */
@Component
public class SsoFilter implements Filter {
    private static String allowOrigin;
    private static String exclusion;
    private static String logoutUri;
    private static String absolute_url;
    private static String relative_url;
    private static String serverHost;
    private static boolean isOpen;
    private static String appKey;
    private static String appSecret;
    private List<Pattern> exclusions = new ArrayList();
    private static String login_protocol = "http";
    private static String  logout_protocol = "http";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static RoleLocalService roleLocalService;

    private static TenantService tenantService;

    private static RolePrivilegeService rolePrivilegeService;

    public static String getAllowOrigin() {
        return allowOrigin;
    }
    @Value("#{propertiesReader['api.access.allow.origin']}")
    public void setAllowOrigin(String allowOrigin) {
        this.allowOrigin = allowOrigin;
    }

    public static String getExclusion() {
        return exclusion;
    }
    @Value("#{propertiesReader['api.url.whitelist']}")
    public void setExclusion(String exclusion) {
        this.exclusion = exclusion;
    }

    public static String getLogoutUri() {
        return logoutUri;
    }
    @Value("#{propertiesReader['sso.logout.url']}")
    public void setLogoutUri(String logoutUri) {
        this.logoutUri = logoutUri;
    }

    public static String getAbsolute_url() {
        return absolute_url;
    }
    @Value("#{propertiesReader['sso.absolute.url']}")
    public void setAbsolute_url(String absolute_url) {
        this.absolute_url = absolute_url;
    }

    public static String getRelative_url() {
        return relative_url;
    }
    @Value("#{propertiesReader['sso.relative.url']}")
    public void setRelative_url(String relative_url) {
        this.relative_url = relative_url;
    }

    public static String getServerHost() {
        return serverHost;
    }

    @Value("#{propertiesReader['sso.server.host']}")
    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public static boolean isOpen() {
        return isOpen;
    }

    @Value("#{propertiesReader['sso.open']}")
    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public static String getAppKey() {
        return appKey;
    }

    @Value("#{propertiesReader['sso.app.key']}")
    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public static String getAppSecret() {
        return appSecret;
    }

    @Value("#{propertiesReader['sso.app.secret']}")
    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        if(!isOpen){
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpSession session = httpRequest.getSession();
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        String reqUri = httpRequest.getRequestURI();
        String httpMethod = httpRequest.getMethod();
        //检查外部平台调用接口时的token
        User user = SsoClient.getUserByHeader(httpRequest);

        //检查跨域请求的origin是否在允许范围内
        if(StringUtils.isNotBlank(allowOrigin)) {
            String origin = allowOrigin;
            String requestOrigin = httpRequest.getHeader("Origin");
            if (StringUtils.isNotBlank(requestOrigin)
                    && (allowOrigin.equals("*") || allowOrigin.indexOf(requestOrigin) > -1 || user != null)) {
                origin = requestOrigin;
            }
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
            httpResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, PATCH");
            httpResponse.setHeader("Access-Control-Max-Age", "1200");
            httpResponse.setHeader("Access-Control-Allow-Headers", "X-Requested-With,Content-Type");
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        }

        //OPTIONS请求，外部平台调用，白名单请求不需要验证cookie
        if (this.isExcluded(reqUri) || HttpMethod.OPTIONS.name().equalsIgnoreCase(httpMethod) || user != null) {
            chain.doFilter(request, response);
        } else {
            //登出请求
            if (reqUri.contains(this.logoutUri)) {
                String logoutUrl = buildLogoutUrl();
                httpResponse.setHeader("Access-Control-Expose-Headers","Status,Location");
                httpResponse.setHeader("Status","302");
                httpResponse.setHeader("Location",logoutUrl);

                SsoClient.clearToken(httpResponse);
                chain.doFilter(request, response);
                session = httpRequest.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
            } else {
                //其他请求
                String url = buildLoginUrl();
                String token = SSOUtil.getCookieValue(httpRequest, "crowd.token_key");
                if (token == null || token.isEmpty() ) {
                    if (!response.isCommitted()) {
                        //response Header中写入status和重定向地址
                        httpResponse.setHeader("Access-Control-Expose-Headers","Status,Location");
                        httpResponse.setHeader("Status","302");
                        if (!url.startsWith("http") && !url.startsWith("https")) {
                            StringBuilder fullUrl = new StringBuilder();
                            String requestScheme = httpRequest.getScheme();
                            String headerScheme = httpRequest.getHeader("X-Client-Scheme");
                            if (!"https".equalsIgnoreCase(requestScheme) && !"https".equalsIgnoreCase(headerScheme)) {
                                fullUrl.append("http");
                            } else {
                                fullUrl.append("https");
                            }

                            fullUrl.append(url.startsWith("//") ? ":" : "://").append(url);
                            httpResponse.setHeader("Location", fullUrl.toString());
                        } else {
                            httpResponse.setHeader("Location", url);

                        }
                        httpResponse.setHeader("Content-Type","application/json; charset=UTF-8");
                        String result = JsonUtil.convertToJson(ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.USER_NOT_EXIST,"",null));
                        ServletOutputStream os = httpResponse.getOutputStream();
                        os.write(result.getBytes("UTF-8"));
                        httpResponse.flushBuffer();
                    }
                }else{
                    httpResponse.setHeader("Access-Control-Expose-Headers","user");
                    if(StringUtils.isNotBlank((String)session.getAttribute(CommonConstant.USERNAME))){
                        httpResponse.setHeader("user", (String)session.getAttribute(CommonConstant.USERNAME));
                    }
                    if(session.getAttribute(CommonConstant.ROLEID) == null || (StringUtils.isNotBlank((String)session.getAttribute(SSOConstants.SSO_TOKEN)) && !token.equals(session.getAttribute(SSOConstants.SSO_TOKEN)))){
                        session.removeAttribute(CommonConstant.ROLEID);
                        session.setAttribute(SSOConstants.SSO_TOKEN, token);
                        com.whchem.sso.client.entity.User ssoUser = SsoClient.getUserByCookie(httpRequest);
                        if(ssoUser != null){
                            httpResponse.setHeader("user",ssoUser.getName());
                        }
                    }
                }
            }
            if (!response.isCommitted()) {
                chain.doFilter(request, response);
            }
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("sso open:{}",isOpen);
        logger.info("sso filter serverHost:{}",serverHost);
        if (serverHost == null) {
            throw new ServletException("Missing init parameter SERVER_HOST");
        } else {
            SSOClient.setServerHost(serverHost);
            SsoClient.setServerHost(serverHost);
            if (this.absolute_url == null && this.relative_url == null) {
                throw new ServletException("Missing init parameter LOGIN_BACK_URL");
            } else {
                if (this.absolute_url != null) {
                    SSOClient.setBackUrl(this.absolute_url);
                } else {
                    SSOClient.setBackUrl(this.relative_url);
                }

                if (appKey == null) {
                    throw new ServletException("Missing init parameter APP_KEY");
                } else {
                    SSOClient.setAppKey(appKey);
                    if (appSecret == null) {
                        throw new ServletException("Missing init parameter APP_SECRET");
                    } else {
                        logger.info("sso filter init......");
                        SSOClient.setAppSecret(appSecret);
                        String exclusionStr = exclusion;
                        if (exclusionStr != null && !exclusionStr.isEmpty()) {
                            String[] inputs = exclusionStr.split(",");
                            int var10 = inputs.length;

                            for(int i = 0; i < var10; ++i) {
                                String input = inputs[i];
                                Pattern pattern = this.regexCompile(input.trim());
                                if (pattern != null) {
                                    this.exclusions.add(pattern);
                                }
                            }
                        }

                    }
                }
            }
            logger.info("sso filter init done.");
        }
    }


    private Pattern regexCompile(String input) {
        if (input != null && !input.isEmpty()) {
            String regex = input.replace("*", "(.*)").replace("?", "(.{1})");
            return Pattern.compile(regex, 2);
        } else {
            return null;
        }
    }

    private boolean isExcluded(String uri) {
        String reqUri = uri;
        if (uri.contains(";")) {
            String[] split = uri.split(";");
            reqUri = split[0];
        }

        Iterator iterator = this.exclusions.iterator();

        Matcher matcher;
        do {
            if (!iterator.hasNext()) {
                return false;
            }

            Pattern exclusion = (Pattern)iterator.next();
            matcher = exclusion.matcher(reqUri);
        } while(!matcher.matches());

        return true;
    }

    /**
     ** 清除cookie中的token
     */
    private void clearToken(HttpServletResponse httpResponse){
        Cookie cookie = new Cookie("crowd.token_key", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        httpResponse.addCookie(cookie);
    }

    public static String buildLoginUrl(){
        return login_protocol + "://" + serverHost + "/sso/login?back_url=";
    }

    private String buildLogoutUrl(){
        return logout_protocol + "://" + serverHost + "/sso/logout?back_url=";
    }

}
