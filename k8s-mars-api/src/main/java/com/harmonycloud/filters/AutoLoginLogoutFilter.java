package com.harmonycloud.filters;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.service.user.RolePrivilegeService;
import com.harmonycloud.service.user.UserRoleRelationshipService;
import com.harmonycloud.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static com.harmonycloud.api.user.AuthController.SESSION_TIMEOUT_HOURS;

@WebFilter(filterName = "AutoLoginLogoutFilter")
public class AutoLoginLogoutFilter implements Filter {

    @Autowired
    private UserService userService;

//    @Autowired
//    private HttpSession session;

    @Autowired
    private UserRoleRelationshipService userRoleRelationshipService;

    @Autowired
    private RolePrivilegeService rolePrivilegeService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        System.out.println("???????????????");

        HttpServletRequest requ = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
//        HttpSession session = requ.getSession();
        HttpSession s = requ.getSession();
        //表示用户是否已经在其他应用登出
        boolean flag = true;
        Cookie[] cookies = requ.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("crowd.token_key")) {// 是否是自动登录。。。。//要先get /session看返回码。但实际上应该还要有更多的判断条件，比如这个application能否访问这个用户
                    System.out.println("检测到cookie");
                    String token = cookie.getValue();
                    URL url = new URL("http://crowd.harmonycloud.com:8095/crowd/rest/usermanagement/latest/session/" + token);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
//						connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Charset", "UTF-8");
                    connection.setRequestProperty("connection", "Keep-Alive");
                    //http基本认证
                    String base64encodedString = Base64.getEncoder().encodeToString("mars:123456".getBytes("utf-8"));
                    connection.setRequestProperty("Authorization", "Basic " + base64encodedString);
                    connection.connect();
                    if(connection.getResponseCode() == 200) {
                        //说明用户已经在登录
                        flag = false;
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                        String line;
                        String result = "";
                        //读取返回值，直到为空
                        while ((line = in.readLine()) != null) {
                            result = result + line + "\n";
                        }
                        String username = result.substring(result.indexOf("name=\"") + 6, result.indexOf("\"><link"));
                        System.out.println(username + "OK");
//                        s.setAttribute("user", username);
                        resp.setHeader("Access-Control-Expose-Headers","user");
                        resp.setHeader("user",username.toLowerCase());
                        System.out.println("1weizhi");
//                        if(session.getAttribute(CommonConstant.USERNAME) == null){
                            s.setAttribute(CommonConstant.USERNAME, username.toLowerCase());
                        System.out.println("2weizhi");
//                        stringRedisTemplate.opsForValue().set("sessionid:sessionid-"+username,s.getId(),SESSION_TIMEOUT_HOURS,TimeUnit.HOURS);
                        System.out.println("3weizhi");
//                        try {
//                            User user = userService.getUser(username);
//                            System.out.println("活了");
//                        }catch(Exception e){
//                            System.out.println("test if error");
//                        }
//                        session.setAttribute("isAdmin", user.getIsAdmin());
//                        session.setAttribute("isMachine", user.getIsMachine());
//                        session.setAttribute("userId", user.getId());
//                        }
//                        else {
////                            String username = (String)session.getAttribute(CommonConstant.USERNAME);
//                            //用户名与当前session的不一致，在其他平台切换过用户，移除session角色，重新获取角色权限
//                            if (!username.equalsIgnoreCase(ssoLoginUser.toLowerCase())) {
//                                session.removeAttribute(CommonConstant.ROLEID);
//                                session.setAttribute(CommonConstant.USERNAME, ssoLoginUser.toLowerCase());
//                            }
//                        }
                    }
                    else {
                        //cookie失效了表示用户已登出
                        System.out.println("cookie的值是无效的");
                    }
                }

            }
        }
        if(flag && s.getAttribute("user") != null) {
            s.removeAttribute("user");
            s.invalidate();
            System.out.println("已经移除用户");
        }
        chain.doFilter(request, response);
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
