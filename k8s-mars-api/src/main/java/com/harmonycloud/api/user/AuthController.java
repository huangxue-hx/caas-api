package com.harmonycloud.api.user;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dao.tenant.bean.UserTenant;

import com.harmonycloud.dto.user.LdapConfigDto;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.user.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.harmonycloud.service.tenant.UserTenantService;


@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private HttpSession session;

    @Autowired
    private UserService userService;

    
    @Autowired
    UserTenantService userTenantService;
    
    @Autowired
    private SecretService secretService;

    @Autowired
    SystemConfigService systemConfigService;

    @Autowired
    AuthManagerDefault authManagerDefault;

    @Autowired
    AuthManager4Ldap authManager4Ldap;
    /**
     * 用户认证
     * 
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    /*
     * @ResponseBody
     * 
     * @RequestMapping(value="/login")
     * 
     * @Deprecated public ActionReturnUtil Login(@RequestParam(value="username")
     * final String username,@RequestParam(value="password") final String
     * password) throws Exception{ User authUser =
     * authService.AuthUser(username, password); if(authUser != null){
     * session.setAttribute("username", authUser.getUsername());
     * session.setAttribute("password", authUser.getPassword());
     * session.setAttribute("isAdmin", authUser.getIsAdmin());
     * session.setAttribute("isMachine", authUser.getIsMachine());
     * session.setAttribute("userId", authUser.getId()); Map<String, Object>
     * data = new HashMap<String,Object>(); User user =
     * userService.getUser(username); Map<String, Object> token =
     * authService.generateToken(user); K8SClient.tokenMap.put(username,
     * token.get("token")); data.put("username", authUser.getUsername());
     * data.put("isSuperAdmin", authUser.getIsAdmin());
     * JsonUtil.objectToJson(data); return
     * ActionReturnUtil.returnSuccessWithData(data); }else{ return
     * ActionReturnUtil.returnError(); } }
     */

    @ResponseBody
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ActionReturnUtil Login(@RequestParam(value = "username") final String username, @RequestParam(value = "password") final String password) throws Exception {
        SystemConfig trialConfig = this.systemConfigService.findByConfigName(CommonConstant.TRIAL_TIME);
        if(trialConfig != null) {
            int v = Integer.parseInt(trialConfig.getConfigValue());
            if (v == 0) {
                return ActionReturnUtil.returnErrorWithMsg("试用已结束，请联系管理员");
            }
        }
        LdapConfigDto ldapConfigDto = this.systemConfigService.findLdapConfig();
        String res = null;
        if(ldapConfigDto != null && ldapConfigDto.getIsOn() != null && ldapConfigDto.getIsOn() == 1) {
            res = this.authManager4Ldap.auth(username, password, ldapConfigDto);
        } else {
            res = authManagerDefault.auth(username, password);
        }
        if (StringUtils.isNotBlank(res)) {
            User user = userService.getUser(username);
            if (user == null) {
                user = new User();
                user.setUsername(username);
                user.setIsAdmin(0);
                user.setIsMachine(0);
            }
            List<UserTenant> userByUserName = userTenantService.getUserByUserName(username);
            if(CommonConstant.PAUSE.equals(user.getPause())){
                return ActionReturnUtil.returnErrorWithMsg("该用户暂时停止使用，请联系管理员");
            }
            if(user.getIsAdmin()!=1&&(userByUserName==null||userByUserName.size()<=0)){
                return ActionReturnUtil.returnErrorWithMsg("该用户未授权，请联系管理员");
            }
            session.setAttribute("username", user.getUsername());
            session.setAttribute("isAdmin", user.getIsAdmin());
            session.setAttribute("isMachine", user.getIsMachine());
            session.setAttribute("userId", user.getId());
            
            ActionReturnUtil checkedSecret = this.secretService.checkedSecret(username, password);     
            
            Map<String, Object> data = new HashMap<String, Object>();
            Map<String, Object> token = authService.generateToken(user);
            K8SClient.tokenMap.put(username, token.get("token"));
            data.put("username", user.getUsername());
            data.put("isSuperAdmin", user.getIsAdmin());
            data.put("token", session.getId());
            data.put("secrit",checkedSecret);
            JsonUtil.objectToJson(data);
            return ActionReturnUtil.returnSuccessWithData(data);
        }
        return ActionReturnUtil.returnErrorWithMsg("用户名密码不正确，请重新登陆");
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil logout() throws Exception {
        // 清除session
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
    @SuppressWarnings("unchecked")
    @ResponseBody
    @RequestMapping(value = "/validation", method = RequestMethod.POST)
    public Map<String, Object> authToken(HttpServletRequest request) throws Exception {
        int size = request.getContentLength();
        if (size == 0) {
            return null;
        }
        InputStream is = request.getInputStream();
        byte[] reqBodyBytes = readBytes(is, size);
        String res = new String(reqBodyBytes);
        System.out.println(res);
        // 将string 转成map
        Map<String, Object> params = JsonUtil.convertJsonToMap(res);
        String token = ((Map<String, Object>) params.get("spec")).get("token").toString();
        Map<String, Object> data = new HashMap<String, Object>();
        User vUser = authService.validateToken(token);
        data.put("apiVersion", params.get("apiVersion").toString());
        data.put("kind", params.get("kind").toString());
        Map<String, Object> status = new HashMap<String, Object>();
        if (vUser != null) {
            status.put("authenticated", true);
            Map<String, Object> userInfo = new HashMap<String, Object>();
            userInfo.put("username", vUser.getUsername());
            userInfo.put("uid", vUser.getId());
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

    @RequestMapping(value = "/getUserName", method = RequestMethod.GET)
    @ResponseBody
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
        return new byte[]{};
    }
}
