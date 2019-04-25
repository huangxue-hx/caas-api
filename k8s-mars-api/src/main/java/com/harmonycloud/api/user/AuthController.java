package com.harmonycloud.api.user;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dto.user.CrowdConfigDto;
import com.harmonycloud.dto.user.LdapConfigDto;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.user.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.service.application.SecretService;

@RequestMapping(value = "/users/auth") @Controller public class AuthController {

    public static final int SESSION_TIMEOUT_HOURS = 8;

    @Autowired private AuthService authService;

    @Autowired private HttpSession session;

    @Autowired private UserService userService;

    @Autowired private SecretService secretService;

    @Autowired private SystemConfigService systemConfigService;

    @Autowired private AuthManagerDefault authManagerDefault;

    @Autowired private AuthManager4Ldap authManager4Ldap;

    @Autowired private UserRoleRelationshipService userRoleRelationshipService;
    @Autowired private RolePrivilegeService rolePrivilegeService;
    @Autowired private HttpServletRequest request;
    @Autowired private StringRedisTemplate stringRedisTemplate;

    @Autowired private AuthManagerCrowd authManagerCrowd;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //    @ResponseBody
    //    @RequestMapping(value = "/login",method = RequestMethod.POST)
    //    public ActionReturnUtil Login(@RequestParam(value = "username") final String username, @RequestParam(value = "password") final String password,
    //                                  @RequestParam(value = "language", required=false) final String language) throws Exception {
    //        SystemConfig trialConfig = this.systemConfigService.findByConfigName(CommonConstant.TRIAL_TIME);
    //        System.out.println("Hello!" + username);
    //        System.out.println("Hello!" + password);
    //        if(trialConfig != null) {
    //            int v = Integer.parseInt(trialConfig.getConfigValue());
    //            if (v == 0) {
    //                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.FREE_TRIAL_END);
    //            }
    //        }
    //        LdapConfigDto ldapConfigDto = this.systemConfigService.findLdapConfig();
    //        String res = null;
    //        if(ldapConfigDto != null && ldapConfigDto.getIsOn() != null && ldapConfigDto.getIsOn() == 1
    //                && !CommonConstant.ADMIN.equals(username)) {
    //            res = this.authManager4Ldap.auth(username, password, ldapConfigDto);
    //            System.out.println("authManager4Ldap");
    //        } else {
    //            //一般都是走Default这种情况
    //            res = authManagerDefault.auth(username, password);
    //            System.out.println("authManagerDefault");
    //        }
    //
    //        //如果res不为null，就表示用户名密码正确
    //        if (StringUtils.isNotBlank(res)) {
    //            //userService这部分可能要改，因为我猜测这部分访问了数据库，但实际数据库应该集成在crowd后台
    //            User user = userService.getUser(username);
    //            if (user == null) {
    //                user = new User();
    //                user.setUsername(username);
    //                user.setIsAdmin(0);
    //                user.setIsMachine(0);
    //            }
    //            //userService这部分可能要改，因为我猜测这部分访问了数据库，但实际数据库应该集成在crowd后台
    //            boolean admin = this.userService.isAdmin(username);
    //            session.setAttribute("username", user.getUsername());
    //            session.setAttribute("isAdmin", user.getIsAdmin());
    //            session.setAttribute("isMachine", user.getIsMachine());
    //            session.setAttribute("userId", user.getId());
    //            session.setAttribute("language", language);
    //            //userService这部分可能要改，因为我猜测这部分访问了数据库，但实际数据库应该集成在crowd后台
    //            Boolean hasRole = userRoleRelationshipService.hasRole(username);
    //            //似乎是一个错误处理的代码，不管
    //            if(CommonConstant.PAUSE.equals(user.getPause())){
    //                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USER_DISABLED);
    //            }
    //            if (admin){
    //                session.setAttribute(CommonConstant.ROLEID, CommonConstant.ADMIN_ROLEID);
    //                rolePrivilegeService.switchRole(CommonConstant.ADMIN_ROLEID);
    //            }
    //            if(!(hasRole || admin)){
    //                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USER_NOT_AUTH);
    //            }
    //            //sessionId存放redis统一管理 默认8小时数据销毁
    //            stringRedisTemplate.opsForValue().set("sessionid:sessionid-"+username,session.getId(),SESSION_TIMEOUT_HOURS,TimeUnit.HOURS);
    //            //TODO 后续
    //
    //            Map<String, Object> data = new HashMap<String, Object>();
    //            Map<String, Object> token = authService.generateToken(user);
    //            System.out.println(token.get("token"));
    //            K8SClient.getTokenMap().put(username, token.get("token"));
    //            data.put("username", user.getUsername().toLowerCase());
    //            data.put("isSuperAdmin", user.getIsAdmin());
    //            data.put("token", session.getId());
    //            JsonUtil.objectToJson(data);
    //            System.out.println("test if I can success!");
    //            return ActionReturnUtil.returnSuccessWithData(data);
    //        }
    //        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.AUTH_FAIL);
    //    }

    @ResponseBody @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ActionReturnUtil Login(@RequestParam(value = "username") final String username,
        @RequestParam(value = "password") final String password,
        @RequestParam(value = "language", required = false) final String language, HttpServletResponse response)
        throws Exception {


        SystemConfig trialConfig = this.systemConfigService.findByConfigName(CommonConstant.TRIAL_TIME);
        if (trialConfig != null) {
            int v = Integer.parseInt(trialConfig.getConfigValue());
            if (v == 0) {
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.FREE_TRIAL_END);
            }
        }
        //获取Ldap的配置信息
        LdapConfigDto ldapConfigDto = this.systemConfigService.findLdapConfig();
        //获取crowd的配置信息
        CrowdConfigDto crowdConfigDto = this.systemConfigService.findCrowdConfig();
        String res = null;
        //表示容器云自身的数据库是否储存了账户信息
        boolean cloud = false;
        //表示crowd连接的数据库中是否储存了账户信息
        boolean crowd = false;
        //首先在容器云中验证账号和密码
        if (ldapConfigDto != null && ldapConfigDto.getIsOn() != null && ldapConfigDto.getIsOn() == 1
            && !CommonConstant.ADMIN.equals(username)) {
            res = this.authManager4Ldap.auth(username, password, ldapConfigDto);
        } else {
            res = authManagerDefault.auth(username, password);
        }
        if (StringUtils.isNotBlank(res)) {
            cloud = true;
        }

        String resCrowd = null;
        System.out.println("access" + crowdConfigDto.getIsAccess());
        if (crowdConfigDto != null && crowdConfigDto.getIsAccess() != null && crowdConfigDto.getIsAccess() == 1 && !CommonConstant.ADMIN.equals(username)) {
            //crowd功能开启时,在crowd中验证用户名和密码
            System.out.println("我要验证！！！！！！！！！！");
            resCrowd = authManagerCrowd.auth(username, password);
            if (resCrowd != null) {
                crowd = true;
            }
        }

        //如果res不为null，就表示至少在一方中找到了账户和密码
        if (StringUtils.isNotBlank(res) || StringUtils.isNotBlank(resCrowd)) {
            //            在crowd的数据库中找到了账户信息，但容器云中没有，需要在容器云中新建用户
            if (crowdConfigDto != null && crowdConfigDto.getIsAccess() != null && crowdConfigDto.getIsAccess() == 1 && !CommonConstant.ADMIN.equals(username)) {
                //只有在crowd接入时才进行用户信息的同步
                if (!cloud) {
                    User user = authManagerCrowd.getUser(username, password);
                    if (user != null) {
                        //添加用户
                        userService.addUser(user);
                    } else {
                        return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USER_INFO_LOST);
                    }
                }
            }
            User user = userService.getUser(username);
            if (user == null) {
                user = new User();
                user.setUsername(username);
                user.setIsAdmin(0);
                user.setIsMachine(0);
            }

            if (crowdConfigDto != null && crowdConfigDto.getIsAccess() != null && crowdConfigDto.getIsAccess() == 1 && !CommonConstant.ADMIN.equals(username)) {
                //只有在crowd接入时才进行用户信息的同步
                if (!crowd) {
                    //虽然在容器云的数据库找到了正确的账户信息，但是没有在crowd中找到相关信息，需要同步至crowd数据库
                    String realname = user.getRealName();
                    String email = user.getEmail();
                    String phone = user.getPhone();
                    if (!authManagerCrowd.addUser(username, password, realname, email, phone)) {
                        return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USER_CROWD_CREATE_FAIL);
                    }
                }
            }

            boolean admin = this.userService.isAdmin(username);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("isAdmin", user.getIsAdmin());
            session.setAttribute("isMachine", user.getIsMachine());
            session.setAttribute("userId", user.getId());
            session.setAttribute("language", language);
            Boolean hasRole = userRoleRelationshipService.hasRole(username);
            if (CommonConstant.PAUSE.equals(user.getPause())) {
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USER_DISABLED);
            }
            if (admin) {
                session.setAttribute(CommonConstant.ROLEID, CommonConstant.ADMIN_ROLEID);
                rolePrivilegeService.switchRole(CommonConstant.ADMIN_ROLEID);
            }
            if (!(hasRole || admin)) {
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USER_NOT_AUTH);
            }
            //sessionId存放redis统一管理 默认8小时数据销毁
            stringRedisTemplate.opsForValue()
                .set("sessionid:sessionid-" + username, session.getId(), SESSION_TIMEOUT_HOURS, TimeUnit.HOURS);

            Map<String, Object> data = new HashMap<String, Object>();
            Map<String, Object> token = authService.generateToken(user);
            K8SClient.getTokenMap().put(username, token.get("token"));
            data.put("username", user.getUsername().toLowerCase());
            data.put("isSuperAdmin", user.getIsAdmin());
            data.put("token", session.getId());
            JsonUtil.objectToJson(data);
            if (crowdConfigDto != null && crowdConfigDto.getIsAccess() != null && crowdConfigDto.getIsAccess() == 1 && !CommonConstant.ADMIN.equals(username)) {
                //在crowd接入时，添加cookie
                String crowdToken = authManagerCrowd.getToken(username, password);
                authManagerCrowd.AddCookie(crowdToken, response);
            }
            return ActionReturnUtil.returnSuccessWithData(data);
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.AUTH_FAIL);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST) @ResponseBody public ActionReturnUtil logout()
        throws Exception {
        //获得当前正在登录的用户名
        String username = (String)session.getAttribute("username");
        //移除redis中sessionid
        stringRedisTemplate.delete("sessionid:sessionid-" + session.getAttribute("username"));
        //获取crowd的配置信息
        CrowdConfigDto crowdConfigDto = this.systemConfigService.findCrowdConfig();
        if (crowdConfigDto != null && crowdConfigDto.getIsAccess() != null && crowdConfigDto.getIsAccess() == 1 && !CommonConstant.ADMIN.equals(username)) {
            //在crowd中清除登录信息
            authManagerCrowd.invalidateToken(username);
        }
        //使session失效
        session.invalidate();

        String data = "message" + ":" + "logout successfully!";
        return ActionReturnUtil.returnSuccessWithData(data);
    }

    /**
     * 验证token有效性
     *
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked") @ResponseBody @RequestMapping(value = "/token", method = RequestMethod.POST)
    public Map<String, Object> authToken() throws Exception {
        //        logger.info("start auth token");
        int size = request.getContentLength();
        if (size == 0) {
            logger.error("request is null");
            return null;
        }
        InputStream is = request.getInputStream();
        byte[] reqBodyBytes = readBytes(is, size);
        String res = new String(reqBodyBytes);
        // 将string 转成map
        Map<String, Object> params = JsonUtil.convertJsonToMap(res);
        String token = ((Map<String, Object>)params.get("spec")).get("token").toString();
        Map<String, Object> data = new HashMap<String, Object>();
        User vUser = authService.validateToken(token);
        data.put("apiVersion", params.get("apiVersion").toString());
        data.put("kind", params.get("kind").toString());
        Map<String, Object> status = new HashMap<String, Object>();
        if (vUser != null) {
            status.put("authenticated", true);
            Map<String, Object> userInfo = new HashMap<String, Object>();
            userInfo.put("username", vUser.getUsername());
            userInfo.put("uid", vUser.getId().toString());
            ArrayList<String> group = new ArrayList<String>();
            userInfo.put("groups", group);
            status.put("user", userInfo);
            Map<String, Object> extra = new HashMap<String, Object>();
            status.put("extra", extra);
        } else {
            status.put("authenticated", false);
        }
        data.put("status", status);
        return data;
    }

    @RequestMapping(value = "/getUserName", method = RequestMethod.GET) @ResponseBody
    public ActionReturnUtil getUserName() throws Exception {

        return ActionReturnUtil.returnSuccessWithData(session.getId());
    }

    /**
     * 根据用户名密码获取token
     *
     * @return
     * @throws Exception
     */
    /*
     * @RequestMapping(value = "/getToken", method = RequestMethod.POST)
     *
     * @ResponseBody public ActionReturnUtil getToken(String userName, String
     * password) throws Exception { User user = new User(userName, password);
     * Map<String, Object> token = authService.generateToken(user); if (token !=
     * null) { if(session != null && session.getAttribute("username")){
     *
     * } K8SClient.tokenMap.put(username, token.get("token")); return
     * ActionReturnUtil.returnSuccessWithData(token); } else { return
     * ActionReturnUtil.returnError(); } }
     */
    public static final byte[] readBytes(InputStream is, int contentLen) {
        if (contentLen > 0) {
            int readLen = 0;
            int readLengthThisTime = 0;
            byte[] message = new byte[contentLen];
            try {
                while (readLen != contentLen) {
                    readLengthThisTime = is.read(message, readLen, contentLen - readLen);
                    if (readLengthThisTime == -1) {
                        // Should not happen.
                        break;
                    }
                    readLen += readLengthThisTime;
                }
                return message;
            } catch (IOException e) {
            }
        }
        return new byte[] {};
    }
}
