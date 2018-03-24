package com.harmonycloud.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class SimpleWrappedFilter implements Filter {
    private static Logger log = LoggerFactory.getLogger(SimpleWrappedFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * 防止请求流只能获取一次
     *
     * @param request  请求
     * @param response 响应
     * @param chain    拦截器链
     * @throws IOException      IO异常
     * @throws ServletException Servlet异常
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest servletRequest = (HttpServletRequest) request;
            if (("PUT".equals(servletRequest.getMethod()) || "PATCH".equals(servletRequest.getMethod())
                    || "DELETE".equals(servletRequest.getMethod()) || "POST".equals(servletRequest.getMethod())) && isApplicationJsonType(servletRequest)) {
                BodyReaderHttpServletRequestWrapper bodyReaderHttpServletRequestWrapper = new BodyReaderHttpServletRequestWrapper(servletRequest);
                HttpSession session = bodyReaderHttpServletRequestWrapper.getSession();
                session.setAttribute("requestBody", bodyReaderHttpServletRequestWrapper.getBody());
                ServletRequest requestWrapper = bodyReaderHttpServletRequestWrapper;
                chain.doFilter(requestWrapper, response);
            } else {
                chain.doFilter(request, response);
            }
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    /**
     * 判断是否是application/json方式
     * @param request 请求
     * @return boolean
     */
    private boolean isApplicationJsonType(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null) {
            try {
                MediaType mediaType = MediaType.parseMediaType(contentType);
                if (MediaType.APPLICATION_JSON.includes(mediaType)) {
                    return true;
                }
                return false;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    public void destroy() {

    }

}
