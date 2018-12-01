package com.harmonycloud.filters;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 跨域过滤器，设置head
 * @author
 * created at 2018/11/22
 */
@Component
public class OriginFilter implements Filter {
    private static String allowOrigin;

    public static String getAllowOrigin() {
        return allowOrigin;
    }
    @Value("#{propertiesReader['api.access.allow.origin']}")
    public void setAllowOrigin(String allowOrigin) {
        this.allowOrigin = allowOrigin;
    }



    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;


        //检查跨域请求的origin是否在允许范围内
        if(StringUtils.isNotBlank(allowOrigin)) {
            String origin = allowOrigin;
            String requestOrigin = httpRequest.getHeader("Origin");
            if (StringUtils.isNotBlank(requestOrigin)
                    && (allowOrigin.equals("*") || allowOrigin.indexOf(requestOrigin) > -1)) {
                origin = requestOrigin;
            }
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
            httpResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, PATCH");
            httpResponse.setHeader("Access-Control-Max-Age", "1200");
            httpResponse.setHeader("Access-Control-Allow-Headers", "X-Requested-With,Content-Type");
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        }
        chain.doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) throws ServletException {

    }
}
