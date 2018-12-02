package com.harmonycloud.filters;


import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.MicroServiceCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.service.util.SsoClient;
//import com.whchem.sso.client.SSOClient;
//import com.whchem.sso.client.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.harmonycloud.common.Constant.CommonConstant.FLAG_TRUE;

/**
 * SSO filter
 * @author
 * created at 2017/12/14
 */
@Component
public class SsoFilter implements Filter {
    private static String exclusion;

    private List<Pattern> exclusions = new ArrayList();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static String getExclusion() {
        return exclusion;
    }
    @Value("#{propertiesReader['api.url.whitelist']}")
    public void setExclusion(String exclusion) {
        this.exclusion = exclusion;
    }



    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        chain.doFilter(request, response);
        /*HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpSession session = httpRequest.getSession();
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        String reqUri = httpRequest.getRequestURI();
        String httpMethod = httpRequest.getMethod();

        //检查请求中的x-acl-signature，取到该请求则为微服务请求，并获取用户信息
        User user = SsoClient.getUserByHeader(httpRequest);
        //OPTIONS请求，微服务请求（user不为空），白名单请求不需要验证cookie
        if (this.isExcluded(reqUri) || HttpMethod.OPTIONS.name().equalsIgnoreCase(httpMethod) || user != null) {
            chain.doFilter(request, response);
        } else {
            //其他系统（oam）登录平台，判断如果已经登录，且账号是机器账号，验证通过
            if(session.getAttribute(CommonConstant.USERNAME) != null){
                Integer isMachine = (Integer)session.getAttribute("isMachine");
                if(isMachine != null && FLAG_TRUE == isMachine){
                    chain.doFilter(request, response);
                }
            }
            User ssoUser = null;
            try {
                ssoUser = SSOClient.getLoginUser(httpRequest, httpResponse);
            }catch (Exception e){
                logger.error("sso查询用户异常",e);
                SsoClient.redirectLogin(session, httpRequest, httpResponse);
            }
            if(ssoUser != null){
                //response中放入user信息，供前端判断用户是否切换过
                httpResponse.setHeader("Access-Control-Expose-Headers","user");
                httpResponse.setHeader("user",ssoUser.getName());
                if(session.getAttribute(CommonConstant.USERNAME) == null){
                    session.setAttribute(CommonConstant.USERNAME, ssoUser.getName());
                }else {
                    String username = (String)session.getAttribute(CommonConstant.USERNAME);
                    //用户名与当前session的不一致，在其他平台切换过用户，移除session角色，重新获取角色权限
                    if(!username.equals(ssoUser.getName())){
                        session.removeAttribute(CommonConstant.ROLEID);
                        session.setAttribute(CommonConstant.USERNAME, ssoUser.getName());
                    }
                }
            }else  if (!response.isCommitted()) {
                SsoClient.redirectLogin(session, httpRequest, httpResponse);
                //给未通过用户认证请求返回错误信息，主要给微服务使用
                httpResponse.setHeader("Content-Type","application/json; charset=UTF-8");
                String result = JsonUtil.convertToJson(ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.USER_NOT_EXIST,"",null));
                ServletOutputStream os = httpResponse.getOutputStream();
                os.write(result.getBytes("UTF-8"));
                httpResponse.flushBuffer();
            }
            if (!response.isCommitted()) {
                chain.doFilter(request, response);
            }
        }*/
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        if(StringUtils.isBlank(exclusion)){
            return;
        }

        String[] inputs = exclusion.split(",");
        int var10 = inputs.length;

        for(int i = 0; i < var10; ++i) {
            String input = inputs[i];
            Pattern pattern = this.regexCompile(input.trim());
            if (pattern != null) {
                this.exclusions.add(pattern);
            }
        }

    }

    private Pattern regexCompile(String input) {
        if (input != null && !input.isEmpty()) {
            String regex = input.replace("*", "(.*)").replace("?", "(.{1})");
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
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

}
