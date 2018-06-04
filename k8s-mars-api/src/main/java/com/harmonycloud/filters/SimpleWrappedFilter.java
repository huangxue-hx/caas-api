package com.harmonycloud.filters;

import com.harmonycloud.common.util.HttpClientUtil;
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
                    || "DELETE".equals(servletRequest.getMethod()) || "POST".equals(servletRequest.getMethod())) && HttpClientUtil.isApplicationJsonType(servletRequest)) {
                BodyReaderHttpServletRequestWrapper bodyReaderHttpServletRequestWrapper = new BodyReaderHttpServletRequestWrapper(servletRequest);
                HttpSession session = bodyReaderHttpServletRequestWrapper.getSession();
                session.setAttribute("requestBody", bodyReaderHttpServletRequestWrapper.getBody());
                ServletRequest requestWrapper = bodyReaderHttpServletRequestWrapper;
                chain.doFilter(requestWrapper, response);
            } else {
                HttpSession session = servletRequest.getSession();
                session.setAttribute("requestBody", null);
                chain.doFilter(request, response);
            }
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    public void destroy() {

    }

}
