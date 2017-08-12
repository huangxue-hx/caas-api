package com.harmonycloud.service.user;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.util.*;
import com.harmonycloud.k8s.bean.MailRecord;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.application.bean.HarborUser;
import com.harmonycloud.dao.tenant.bean.UserTenant;
import com.harmonycloud.dao.user.AuthUserMapper;
import com.harmonycloud.dao.user.HarborUserMapper;
import com.harmonycloud.dao.user.UserGroupMapper;
import com.harmonycloud.dao.user.UserGroupRelationMapper;
import com.harmonycloud.dao.user.UserMapper;
import com.harmonycloud.dao.user.bean.AuthUser;
import com.harmonycloud.dao.user.bean.AuthUserExample;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.bean.UserExample;
import com.harmonycloud.dao.user.bean.UserGroup;
import com.harmonycloud.dao.user.bean.UserGroupExample;
import com.harmonycloud.dao.user.bean.UserGroupExample.Criteria;
import com.harmonycloud.dao.user.bean.UserGroupRelation;
import com.harmonycloud.dao.user.bean.UserGroupRelationExample;
import com.harmonycloud.dao.user.customs.CustomUserMapper;
import com.harmonycloud.dto.tenant.show.UserShowDto;
import com.harmonycloud.dto.user.ExcelUtil;
import com.harmonycloud.dto.user.SummaryUserInfo;
import com.harmonycloud.dto.user.UserDetailDto;
import com.harmonycloud.dto.user.UserGroupDto;
import com.harmonycloud.k8s.service.RoleBindingService;
import com.harmonycloud.service.tenant.UserTenantService;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

    @Autowired
    private CustomUserMapper userMapper;

    @Autowired
    private UserGroupRelationMapper usergrouprelationMapper;

    @Autowired
    private UserGroupMapper usergroupMapper;

    @Autowired
    private UserMapper userMapperNew;

    @Autowired
    private HarborUserMapper harboruserMapper;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleBindingService roleBindingService;

    @Autowired
    private AuthUserMapper authUserMapper;

    @Autowired
    UserTenantService userTenantService;

    @Value("#{propertiesReader['webhook.host']}")
    private String webhook;
    @Value("#{propertiesReader['image.host']}")
    private String harborIP;
    @Value("#{propertiesReader['image.port']}")
    private String harborPort;
    @Value("#{propertiesReader['image.username']}")
    private String harborUser;
    @Value("#{propertiesReader['image.password']}")
    private String harborPassword;
    @Value("#{propertiesReader['image.timeout']}")
    private String harborTimeout;
    @Autowired
    private HarborUtil harborUtil;
    private UserService userService;


    /**
     * 向用户发送提示邮箱
     *
     */
    public void sendEmail(String email,String userName) throws Exception{
        MimeMessage mimeMessage = MailUtil.getJavaMailSender().createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("k8sdev@harmonycloud.cn");
            helper.setTo(email);
            helper.setSubject("密码重置通知");
            Map dataModel = new HashMap<>();
            Date date = new Date();
            dataModel.put("time",date);
            dataModel.put("userName",userName);

            //设置图标
            ClassLoader classLoader = MailUtil.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("icon-info.png");
            byte[] bytes = MailUtil.stream2byte(inputStream);
            helper.addInline("icon-info", new ByteArrayResource(bytes), "image/png");
            inputStream = classLoader.getResourceAsStream("icon-status.png");
            bytes = MailUtil.stream2byte(inputStream);
            helper.addInline("icon-status", new ByteArrayResource(bytes), "image/png");
            //helper.setText("测试一下");
            helper.setText(TemplateUtil.generate("passWordRest.ftl",dataModel), true);
            //System.out.println(TemplateUtil.generate("passWordRest.ftl",dataModel));
        }catch(Exception e){
        }
        MailUtil.sendMimeMessage(mimeMessage);
    }

    /**
     * 向k8s和harbor中新增用户
     * 
     * @param user
     * @return
     */
    public ActionReturnUtil addUser(User user) throws Exception {
        // 密码匹配
        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{7,12}$";
        String regex1 = "^[\u4E00-\u9FA5A-Za-z0-9]+$";
        boolean matches = user.getPassword().matches(regex);
        if (!matches) {
            return ActionReturnUtil.returnErrorWithMsg("密码格式为7-12位数字和字母的组合！");
        }
        // 用户名非重
        if (this.checkUserName(user.getUsername())) {
            return ActionReturnUtil.returnErrorWithMsg("用户名已存在！");
        }
        // 邮箱非重
        if (this.checkEmail(user.getEmail())) {
            return ActionReturnUtil.returnErrorWithMsg("邮箱已存在！");
        }
        // 真实用户名判断，过滤特殊符号
        boolean matchrealname = user.getRealName().matches(regex1);
        if (!matchrealname) {
            return ActionReturnUtil.returnErrorWithMsg("真实姓名格式不正确！");
        }
        HarborUser harbor = new HarborUser();
        harbor.setUsername(user.getUsername());
        harbor.setPassword(user.getPassword());

        try {
            // 向harbor新增用户
            String addUrl = "http://" + harborIP + ":" + harborPort + "/api/users";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("username", user.getUsername());
            params.put("password", user.getPassword());
            params.put("realname", user.getRealName());
            params.put("comment", user.getComment());
            params.put("email", user.getEmail());
            String cookie = harborUtil.checkCookieTimeout();
            Map<String, Object> header = new HashMap<String, Object>();
            header.put("Cookie", cookie);
            header.put("Content-type", "application/json");
            String harborUId = null;
            try {
                CloseableHttpResponse response = HttpClientUtil.doBodyPost(addUrl, params, header);
                if (HttpStatusUtil.isSuccessStatus(response.getStatusLine().getStatusCode())) {
                    // 密码md5加密
                    // 获取harbor用户uuid作为user id
                    Header[] headers = response.getHeaders("Location");
                    if (headers.length > 0) {
                        Header location = headers[0];
                        harborUId = location.getValue().substring(location.getValue().lastIndexOf("/") + 1);
                        String MD5password = StringUtil.convertToMD5(user.getPassword());
                        user.setPassword(MD5password);
                        user.setId(Long.valueOf(harborUId));
                        user.setUuid(Long.valueOf(harborUId));
                        user.setCreateTime(new Date());
                        user.setPause(CommonConstant.NORMAL);
                        userMapper.addUser(user);
                        HarborUser harborUser2 = harboruserMapper.findByUsername(user.getUsername());
                        if (harborUser2 == null) {
                            harboruserMapper.addUser(harbor);
                        } else {
                            harboruserMapper.updatePassword(harbor.getUsername(), harbor.getPassword());
                        }
                        return ActionReturnUtil.returnSuccess();
                    } else {
                        return ActionReturnUtil.returnErrorWithMsg("创建失败！");
                    }
                } else {
                    return ActionReturnUtil.returnErrorWithMsg("创建harbor失败！");
                }
            } catch (Exception e) {
                if (e instanceof SQLException) {
                    // 删除harbor用户
                    String deleteUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + harborUId;
                    String dlCookie = harborUtil.checkCookieTimeout();
                    Map<String, Object> headers = new HashMap<String, Object>();
                    headers.put("Cookie", dlCookie);
                    HttpClientResponse deleteRes = HttpClientUtil.doDelete(deleteUrl, null, headers);
                    if (!HttpStatusUtil.isSuccessStatus(deleteRes.getStatus())) {
                        return ActionReturnUtil.returnErrorWithMsg(deleteRes.getBody());
                    }
                }
                throw e;
            }
        } catch (Exception e) {
            throw e;
        }

    }
    /**
     * 修改k8s中电话号码
     *
     * @param phone
     * @param userName
     * @return
     */
    public ActionReturnUtil changePhone(String userName, String phone) throws Exception {
        if (StringUtils.isEmpty(userName)) {
            return ActionReturnUtil.returnErrorWithMsg("用户名不能为空!");
        }
        if (StringUtils.isEmpty(phone)) {
            return ActionReturnUtil.returnErrorWithMsg("电话号码不能为空!");
        }
        Date date = new Date();// 获得系统时间.
        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        String nowTime = sdf.format(date);
        Date time = sdf.parse(nowTime);
        User user = new User();
        user.setPhone(phone);
        user.setUsername(userName);
        user.setUpdateTime(time);
        String oldPhone = userMapper.findByUsername(userName).getPhone();
        userMapper.updateUser(user);
        // 修改harbor
        try {
            String userPath = "http://" + harborIP + ":" + harborPort + "/api/users?username=" + userName;
            String cookie = harborUtil.checkCookieTimeout();
            Map<String, Object> header = new HashMap<String, Object>();
            header.put("Cookie", cookie);
            HttpClientResponse httpClientResponse = HttpClientUtil.doGet(userPath, null, header);
            List<Map<String, Object>> result = JsonUtil.JsonToMapList(httpClientResponse.getBody());
            if (result != null && result.size() > 0) {
                Map<String, Object> harboruser = result.get(0);
                String userId = harboruser.get("user_id").toString();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("phone", phone);
                params.put("email", harboruser.get("email").toString());
                params.put("comment", harboruser.get("comment").toString());
                String updateUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + userId;
                String dlCookie = harborUtil.checkCookieTimeout();
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Cookie", dlCookie);
                HttpClientResponse putRes = HttpClientUtil.doPut(updateUrl, params, headers);
                // 根据返回code判断状态
                if (HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
                    return ActionReturnUtil.returnSuccess();
                } else {
                    // 回滚数据库操作
                    user.setRealName(oldPhone);
                    userMapper.updateUser(user);
                    return ActionReturnUtil.returnErrorWithMsg(putRes.getBody());
                }
            } else {
                return ActionReturnUtil.returnError();
            }
        } catch (Exception e) {
            user.setRealName(oldPhone);
            userMapper.updateUser(user);
            // 如果是harbor请求异常需要回滚数据库
            throw e;
        }
    }

    /**
     * 修改k8s中的真实姓名
     * 
     * @param realName
     * @param userName
     * @return
     */
    public ActionReturnUtil changeRealName(String userName, String realName) throws Exception {
        // 真实用户名判断，过滤特殊符号
        String regex = "^[\u4E00-\u9FA5A-Za-z0-9]+$";
        boolean matchrealname = realName.matches(regex);
        if (!matchrealname) {
            return ActionReturnUtil.returnErrorWithMsg("真实姓名格式不正确！");
        }
        if (StringUtils.isEmpty(userName)) {
            return ActionReturnUtil.returnErrorWithMsg("用户名不能为空!");
        }
        if (StringUtils.isEmpty(realName)) {
            return ActionReturnUtil.returnErrorWithMsg("真实姓名不能为空!");
        }
        Date date = new Date();// 获得系统时间.
        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        String nowTime = sdf.format(date);
        Date time = sdf.parse(nowTime);
        User user = new User();
        user.setRealName(realName);
        user.setUsername(userName);
        user.setUpdateTime(time);
        String oldRealName = userMapper.findByUsername(userName).getRealName();
        userMapper.updateUser(user);
        // 修改harbor
        try {
            String userPath = "http://" + harborIP + ":" + harborPort + "/api/users?username=" + userName;
            String cookie = harborUtil.checkCookieTimeout();
            Map<String, Object> header = new HashMap<String, Object>();
            header.put("Cookie", cookie);
            HttpClientResponse httpClientResponse = HttpClientUtil.doGet(userPath, null, header);
            List<Map<String, Object>> result = JsonUtil.JsonToMapList(httpClientResponse.getBody());
            if (result != null && result.size() > 0) {
                Map<String, Object> harboruser = result.get(0);
                String userId = harboruser.get("user_id").toString();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("realname", realName);
                params.put("email", harboruser.get("email").toString());
                params.put("comment", harboruser.get("comment").toString());
                String updateUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + userId;
                String dlCookie = harborUtil.checkCookieTimeout();
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Cookie", dlCookie);
                HttpClientResponse putRes = HttpClientUtil.doPut(updateUrl, params, headers);
                // 根据返回code判断状态
                if (HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
                    return ActionReturnUtil.returnSuccess();
                } else {
                    // 回滚数据库操作
                    user.setRealName(oldRealName);
                    userMapper.updateUser(user);
                    return ActionReturnUtil.returnErrorWithMsg(putRes.getBody());
                }
            } else {
                return ActionReturnUtil.returnError();
            }
        } catch (Exception e) {
            user.setRealName(oldRealName);
            userMapper.updateUser(user);
            // 如果是harbor请求异常需要回滚数据库
            throw e;
        }
    }

    /**
     * 修改k8s和harbor中的邮箱地址
     * 
     * @param realName
     * @param userName
     * @return
     */
    public ActionReturnUtil changeEmail(String userName, String email) throws Exception {
        String regex = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
        boolean matches = email.matches(regex);
        if (!matches) {
            return ActionReturnUtil.returnErrorWithMsg("邮箱格式不对！");
        }
        if (StringUtils.isEmpty(userName)) {
            return ActionReturnUtil.returnErrorWithMsg("用户名不能为空!");
        }
        if (StringUtils.isEmpty(email)) {
            return ActionReturnUtil.returnErrorWithMsg("邮箱不能为空！");
        }
        Date date = new Date();// 获得系统时间.
        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        String nowTime = sdf.format(date);
        Date time = sdf.parse(nowTime);
        User user = new User();
        user.setEmail(email);
        user.setUsername(userName);
        user.setUpdateTime(time);
        String oldEmail = userMapper.findByUsername(userName).getEmail();
        userMapper.updateUser(user);
        // 修改harbor
        try {
            String userPath = "http://" + harborIP + ":" + harborPort + "/api/users?username=" + userName;
            String cookie = harborUtil.checkCookieTimeout();
            Map<String, Object> header = new HashMap<String, Object>();
            header.put("Cookie", cookie);
            HttpClientResponse httpClientResponse = HttpClientUtil.doGet(userPath, null, header);
            List<Map<String, Object>> result = JsonUtil.JsonToMapList(httpClientResponse.getBody());
            if (result != null && result.size() > 0) {
                Map<String, Object> harboruser = result.get(0);
                String userId = harboruser.get("user_id").toString();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("realname", harboruser.get("realname").toString());
                params.put("email", email);
                params.put("comment", harboruser.get("comment").toString());
                String updateUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + userId;
                String dlCookie = harborUtil.checkCookieTimeout();
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Cookie", dlCookie);
                HttpClientResponse putRes = HttpClientUtil.doPut(updateUrl, params, headers);
                // 根据返回code判断状态
                if (HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
                    return ActionReturnUtil.returnSuccess();
                } else {
                    // 回滚数据库操作
                    user.setRealName(oldEmail);
                    userMapper.updateUser(user);
                    return ActionReturnUtil.returnErrorWithMsg(putRes.getBody());
                }
            } else {
                return ActionReturnUtil.returnError();
            }
        } catch (Exception e) {
            user.setRealName(oldEmail);
            userMapper.updateUser(user);
            // 如果是harbor请求异常需要回滚数据库
            throw e;
        }

    }

    /**
     * 修改密码
     * 
     * @param userName
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public ActionReturnUtil changePwd(String userName, String oldPassword, String newPassword) throws Exception {
        if (StringUtils.isEmpty(newPassword)) {
            return ActionReturnUtil.returnErrorWithMsg("新密码不能为空!");
        }
        if (newPassword.equals(oldPassword)) {
            return ActionReturnUtil.returnErrorWithMsg("新密码不能和原始密码相同!");
        }
        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{7,12}$";
        boolean matches = newPassword.matches(regex);
        if (!matches) {
            return ActionReturnUtil.returnErrorWithMsg("密码必须是7-12位数字和字母的组合！");
        }
        try {
            // 判断旧密码的正确性
            String MD5oldPassword = StringUtil.convertToMD5(oldPassword);
            User userDb = userMapper.findByUsername(userName);
            if (!userDb.getPassword().equals(MD5oldPassword)) {
                return ActionReturnUtil.returnErrorWithMsg("原始密码不正确！");
            }
            // 更新k8s用户密码
            String MD5newPassword = StringUtil.convertToMD5(newPassword);
            userMapper.updatePassword(userName, MD5newPassword);
            harboruserMapper.updatePassword(userName, newPassword);
            // 更新harbor账户密码
            // 根据用户名查询用户id
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("old_password", oldPassword);
                params.put("new_password", newPassword);
                String userId = userDb.getId().toString();
                String updateUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + userId + "/password";
                String dlCookie = harborUtil.checkCookieTimeout();
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Cookie", dlCookie);
                HttpClientResponse putRes = HttpClientUtil.doPut(updateUrl, params, headers);
                // 根据返回code判断状态
                if (HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
                    return ActionReturnUtil.returnSuccess();
                } else {
                    // 回滚数据库操作
                    userMapper.updatePassword(userName, MD5oldPassword);
                    harboruserMapper.updatePassword(userName, oldPassword);
                    return ActionReturnUtil.returnErrorWithMsg(putRes.getBody());
                }
            } catch (Exception e) {
                // 如果是harbor请求异常需要回滚数据库
                userMapper.updatePassword(userName, MD5oldPassword);
                harboruserMapper.updatePassword(userName, oldPassword);
                throw e;
            }
        } catch (Exception e) {
            throw e;
        }

    }

    /**
     * 重置用户密码
     * 
     * @param userName
     * @return
     */
    public ActionReturnUtil userReset(String userName, String newPassword) throws Exception {
        if (StringUtils.isEmpty(userName)) {
            return ActionReturnUtil.returnErrorWithMsg("用户名不能为空!");
        }
        if (StringUtils.isEmpty(newPassword)) {
            return ActionReturnUtil.returnErrorWithMsg("新密码不能为空!");
        }
        try {
            // 查询旧密码
            /*
             * String MD5oldPassword =
             * userMapper.findByUsername(userName).getPassword(); String
             * oldPassword = StringUtil.convertToMD5(MD5oldPassword);
             */
            User userEmail = userMapper.findByUsername(userName);
            String MD5oldPassword = userEmail.getPassword();
            HarborUser harbor = harboruserMapper.findByUsername(userName);
            String oldPassword = harbor.getPassword();
            System.out.println(userEmail.getEmail());
            if (newPassword.equals(oldPassword)) {
                this.sendEmail(userEmail.getEmail(),userName);
                return ActionReturnUtil.returnSuccess();
            }
            // 更新k8s用户密码
            // 更新harbor账户密码
            String MD5newPassword = StringUtil.convertToMD5(newPassword);
            userMapper.updatePassword(userName, MD5newPassword);
            harboruserMapper.updatePassword(userName, newPassword);
            this.sendEmail(userEmail.getEmail(),userName);


            // 根据用户名查询用户id
            try {
                String userPath = "http://" + harborIP + ":" + harborPort + "/api/users?username=" + userName;
                String cookie = harborUtil.checkCookieTimeout();
                Map<String, Object> header = new HashMap<String, Object>();
                header.put("Cookie", cookie);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("old_password", oldPassword);
                params.put("new_password", newPassword);
                HttpClientResponse httpClientResponse = HttpClientUtil.doGet(userPath, null, header);
                List<Map<String, Object>> result = JsonUtil.JsonToMapList(httpClientResponse.getBody());
                if (result != null && result.size() > 0) {
                    Map<String, Object> user = result.get(0);
                    String userId = user.get("user_id").toString();
                    String updateUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + userId + "/password";
                    String dlCookie = harborUtil.checkCookieTimeout();
                    Map<String, Object> headers = new HashMap<String, Object>();
                    headers.put("Cookie", dlCookie);
                    HttpClientResponse putRes = HttpClientUtil.doPut(updateUrl, params, headers);
                    // 根据返回code判断状态
                    if (HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
                        return ActionReturnUtil.returnSuccess();
                    } else {
                        // 回滚数据库操作
                        userMapper.updatePassword(userName, MD5oldPassword);
                        harboruserMapper.updatePassword(userName, oldPassword);
                        return ActionReturnUtil.returnErrorWithMsg(putRes.getBody());
                    }
                } else {
                    return ActionReturnUtil.returnError();
                }
            } catch (Exception e) {
                // 如果是harbor请求异常需要回滚数据库
                userMapper.updatePassword(userName, MD5oldPassword);
                harboruserMapper.updatePassword(userName, oldPassword);
                throw e;
            }
        } catch (Exception e) {
            throw e;
        }

    }

    /**
     * 重置admin密码
     * 
     * @param userName
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public ActionReturnUtil adminReset(String userName, String oldPassword, String newPassword) throws Exception {
        if (StringUtils.isEmpty(userName)) {
            return ActionReturnUtil.returnErrorWithMsg("用户名不能为空!");
        }
        if (StringUtils.isEmpty(newPassword)) {
            return ActionReturnUtil.returnErrorWithMsg("新密码不能为空!");
        }
        if (newPassword.equals(oldPassword)) {
            return ActionReturnUtil.returnErrorWithMsg("新密码和原始密码不能相同!");
        }
        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,12}$";
        boolean matches = newPassword.matches(regex);
        if (!matches) {
            return ActionReturnUtil.returnErrorWithMsg("密码必须是7-12位字母和数字的组合！");
        }

        try {
            // 判断旧密码的正确性
            String MD5oldPassword = StringUtil.convertToMD5(oldPassword);
            User userDb = userMapper.findByUsername(userName);
            if (!userDb.getPassword().equals(MD5oldPassword)) {
                return ActionReturnUtil.returnErrorWithMsg("原始密码不正确！");
            }
            // 更新k8s用户密码
            String MD5newPassword = StringUtil.convertToMD5(newPassword);
            userMapper.updatePassword(userName, MD5newPassword);
            harboruserMapper.updatePassword(userName, newPassword);
            return ActionReturnUtil.returnSuccess();
        } catch (Exception e) {
            throw e;
        }

    }

    /**
     * 刪除用户
     * 
     * @param userName
     * @return
     */
    public ActionReturnUtil deleteUser(String userName) throws Exception {
        if (StringUtils.isEmpty(userName)) {
            return ActionReturnUtil.returnErrorWithMsg("用户名不能为空!");
        }
        try {
            // 先查询该用户是否有绑定信息,如果有则不能删除
            // String label = "nephele_user_" + userName + "=" + userName;
            // K8SClientResponse response =
            // roleBindingService.getRolebindingListbyLabelSelector(label);
            // List<RoleBinding> items = null;
            // if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            // RoleBindingList roleBindingList =
            // K8SClient.converToBean(response, RoleBindingList.class);
            // items = roleBindingList.getItems();
            // }
            List<UserTenant> list = userTenantService.getUserByUserName(userName);
            if (list == null || list.size() == 0) {

                // 删除数据库中k8s账户
                User userDb = userMapper.findByUsername(userName);
                HarborUser harbor = harboruserMapper.findByUsername(userName);
                // 删除harbor账户
                // 先查询用户Id
                try {
                    String userId = userDb.getId().toString();
                    String deleteUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + userId;
                    String dlCookie = harborUtil.checkCookieTimeout();
                    Map<String, Object> headers = new HashMap<String, Object>();
                    headers.put("Cookie", dlCookie);
                    HttpClientResponse deleteRes = HttpClientUtil.doDelete(deleteUrl, null, headers);
                    // 根据返回code判断状态
                    if (HttpStatusUtil.isSuccessStatus(deleteRes.getStatus())) {
                        userMapper.deleteUserByName(userName);
                        harboruserMapper.deleteUserByName(userName);
                        return ActionReturnUtil.returnSuccess();
                    } else {
                        return ActionReturnUtil.returnErrorWithMsg(deleteRes.getBody());
                    }
                } catch (Exception e) {
                    // 回滚数据库
                    userMapper.addUser(userDb);
                    HarborUser harborUser2 = harboruserMapper.findByUsername(userDb.getUsername());
                    if (harborUser2 == null) {
                        harboruserMapper.addUser(harbor);
                    } else {
                        harboruserMapper.updatePassword(harbor.getUsername(), harbor.getPassword());
                    }
                    throw e;
                }
            } else {
                return ActionReturnUtil.returnErrorWithMsg("请删除" + userName + "用户绑定信息");
            }
        } catch (Exception e) {
            throw e;
        }

    }

    /**
     * 根据用户名获取用户明细
     * 
     * @param username
     * @return
     */
    public List<UserDetailDto> userDetail(String username) throws Exception {
        List<UserDetailDto> listRolebindings = roleService.userDetail(username);
        return listRolebindings;
    }

    /**
     * 根据用户名查询用户
     * 
     * @param username
     * @return
     */
    public User getUser(String username) throws Exception {
        if (username != null) {
            User user = userMapper.findByUsername(username);
            return user;
        }
        return null;
    }
    /**
     * 更新用户状态
     * 
     * @param username
     * @return
     * @throws Exception
     */
    public User updateUserStatus(String username, String status) throws Exception {
        if (username != null) {
            User user = this.getUser(username);
            user.setPause(status);
            user.setUuid(user.getId());
            user.setUpdateTime(new Date());
            int updateByPrimaryKeySelective = this.userMapperNew.updateByPrimaryKeySelective(user);
            return user;
        }
        return null;
    }
    /**
     * 更改用户isadmin状态
     * 
     * @param username
     * @param status
     * @return
     * @throws Exception
     */
    public User updateUserToAdmin(String username, Integer isadmin) throws Exception {
        if (username != null) {
            User user = this.getUser(username);
            if (user == null) {
                throw new MarsRuntimeException("用户" + username + "不存在！");
            }
            if (user.getIsAdmin() == isadmin) {
                if (isadmin == 0) {
                    throw new MarsRuntimeException("用户" + username + "已经是普通用户！");
                } else {
                    throw new MarsRuntimeException("用户" + username + "已经是管理员！");
                }
            }
            user.setIsadmin(isadmin);
            user.setUuid(user.getId());
            user.setUpdateTime(new Date());
            int updateByPrimaryKeySelective = this.userMapperNew.updateByPrimaryKeySelective(user);
            return user;
        }
        return null;
    }
    /**
     * 获取部门被pause的用户
     * 
     * @return
     */
    public List<User> getUserPausedListByDepartmnet(String department) throws Exception {
        List<User> pausedList = this.userMapper.getUserPausedListByDepartmnet(department);
        return pausedList;
    }
    /**
     * 获取部门normal的用户
     * 
     * @return
     */
    public List<User> getUserNormalListByDepartmnet(String department) throws Exception {
        List<User> normalList = this.userMapper.getUserNormalListByDepartmnet(department);
        return normalList;
    }
    /**
     * 获取部门一定时间段的活跃用户
     * 
     * @return
     */
    public List<User> getActiveUserListByDepartmnet(Integer domain, String department) throws Exception {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -domain);
        Date leftDate = calendar.getTime();
        UserExample example = new UserExample();
        example.createCriteria().andTokenCreateBetween(leftDate, date).andTokenCreateIsNotNull().andIsadminEqualTo(false).andIsmachineEqualTo(false);
        List<User> activeList = this.userMapperNew.selectByExample(example);
        List<User> search_users_groupname = this.search_users_groupname(department);
        Map<String, String> groupUser = new HashMap<>();
        List<User> result = new ArrayList<>();
        if (search_users_groupname != null && search_users_groupname.size() > 0) {
            for (User user : search_users_groupname) {
                groupUser.put(user.getUsername(), user.getUsername());
            }
            for (User user : activeList) {
                if (groupUser.get(user.getUsername()) != null) {
                    result.add(user);
                }
            }
        }
        return result;
    }
    /**
     * 获取部门一定时间段的不活跃用户
     * 
     * @param domain
     * @return
     * @throws Exception
     */
    public List<User> getUnActiveUserListByDepartmnet(Integer domain, String department) throws Exception {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -domain);
        Date leftDate = calendar.getTime();
        UserExample example = new UserExample();
        example.createCriteria().andTokenCreateIsNull().andIsadminEqualTo(false).andIsmachineEqualTo(false);
        List<User> unActiveList = this.userMapperNew.selectByExample(example);
        UserExample example1 = new UserExample();
        example1.createCriteria().andTokenCreateNotBetween(leftDate, date).andTokenCreateIsNotNull().andIsadminEqualTo(false).andIsmachineEqualTo(false);
        List<User> normalList1 = this.userMapperNew.selectByExample(example1);
        if (normalList1 != null && normalList1.size() > 0) {
            for (User user : normalList1) {
                unActiveList.add(user);
            }
        }
        List<User> search_users_groupname = this.search_users_groupname(department);
        Map<String, String> groupUser = new HashMap<>();
        List<User> result = new ArrayList<>();
        if (search_users_groupname != null && search_users_groupname.size() > 0) {
            for (User user : search_users_groupname) {
                groupUser.put(user.getUsername(), user.getUsername());
            }
            for (User user : unActiveList) {
                if (groupUser.get(user.getUsername()) != null) {
                    result.add(user);
                }
            }
        }
        return result;
    }
    /**
     * 获取部门未授权用户列表
     * 
     * @return
     * @throws Exception
     */
    public List<User> getUnauthorizedUserListByDepartmnet(String department) throws Exception {
        List<User> unauthorizedUserList = userMapper.getUnauthorizedUserList();
        List<User> search_users_groupname = this.search_users_groupname(department);
        Map<String, String> groupUser = new HashMap<>();
        List<User> result = new ArrayList<>();
        if (search_users_groupname != null && search_users_groupname.size() > 0) {
            for (User user : search_users_groupname) {
                groupUser.put(user.getUsername(), user.getUsername());
            }
            for (User user : unauthorizedUserList) {
                if (groupUser.get(user.getUsername()) != null) {
                    result.add(user);
                }
            }
        }
        return result;
    }
    /**
     * 获取所有被pause的用户
     * 
     * @return
     */
    public List<User> getAllUserPausedList() throws Exception {
        List<User> pausedList = this.userMapper.getAllUserPausedList();
        //添加组名信息
        for(int i=0;i<pausedList.size();i++){
            User u = pausedList.get(i);
            u.setGroupName(userMapper.selectGroupNameByUserID(u.getUuid()));
        }
        return pausedList;
    }
    /**
     * 获取所有normal的用户
     * 
     * @return
     */
    public List<User> getAllUserNormalList() throws Exception {
        List<User> normalList = this.userMapper.getAllUserNormalList();
        //添加组名
        for(int i =0;i<normalList.size();i++){
            User user = normalList.get(i);
            normalList.get(i).setGroupName(userMapper.selectGroupNameByUserID(user.getUuid()));
            normalList.get(i).setIsAuthorize(1);
        }
        return normalList;
    }
    /**
     * 获取所有部门一定时间段的活跃用户
     * 
     * @return
     */
    public List<User> getActiveUserList(Integer domain) throws Exception {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -domain);
        Date leftDate = calendar.getTime();
        UserExample example = new UserExample();
        example.createCriteria().andTokenCreateBetween(leftDate, date).andTokenCreateIsNotNull().andIsadminEqualTo(false).andIsmachineEqualTo(false);
        List<User> normalList = this.userMapperNew.selectByExample(example);
        return normalList;
    }
    /**
     * 获取所有部门一定时间段的不活跃用户
     * 
     * @param domain
     * @return
     * @throws Exception
     */
    public List<User> getUnActiveUserList(Integer domain) throws Exception {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -domain);
        Date leftDate = calendar.getTime();
        UserExample example = new UserExample();
        example.createCriteria().andTokenCreateIsNull().andIsadminEqualTo(false).andIsmachineEqualTo(false);
        List<User> normalList = this.userMapperNew.selectByExample(example);
        UserExample example1 = new UserExample();
        example1.createCriteria().andTokenCreateNotBetween(leftDate, date).andTokenCreateIsNotNull().andIsadminEqualTo(false).andIsmachineEqualTo(false);
        List<User> normalList1 = this.userMapperNew.selectByExample(example1);
        if (normalList1 != null && normalList1.size() > 0) {
            for (User user : normalList1) {
                normalList.add(user);
            }
        }
        return normalList;
    }
    /**
     * 获取未授权用户列表
     * 
     * @return
     * @throws Exception
     */
    public List<User> getUnauthorizedUserList() throws Exception {
        List<User> unauthorizedUserList = userMapper.getUnauthorizedUserList();
        //增加群组信息
        for(int i=0;i<unauthorizedUserList.size();i++){
            User user = unauthorizedUserList.get(i);
            user.setGroupName(userMapper.selectGroupNameByUserID(user.getUuid()));
            user.setIsAuthorize(0);
        }
        return unauthorizedUserList;
    }
    public List<User> getAdminUserList() throws Exception {
        UserExample example = new UserExample();
        example.createCriteria().andIsadminEqualTo(true);
        List<User> adminUserList = userMapperNew.selectByExample(example);
        for(int i=0;i<adminUserList.size();i++){
            User u = adminUserList.get(i);
            u.setGroupName(userMapper.selectGroupNameByUserID(u.getUuid()));
        }
        return adminUserList;
    }
    /**
     * 获取用户总览
     * 
     * @return
     * @throws Exception
     */
    public SummaryUserInfo getAllSummary(Integer domain) throws Exception {
        SummaryUserInfo su = new SummaryUserInfo();
        // 活跃用户
        List<User> activeUserList = this.getActiveUserList(domain);
        su.setActiveSum(activeUserList.size());
        su.setActiveUserList(activeUserList);
        // 不活跃用户
        List<User> unActiveUserList = this.getUnActiveUserList(domain);
        su.setUnActiveUserList(unActiveUserList);
        su.setUnActiveSum(unActiveUserList.size());
        // 正常用户
        List<User> allUserNormalList = this.getAllUserNormalList();
        su.setNormalUserList(allUserNormalList);
        su.setUserNormalSum(allUserNormalList.size());
        // 未授权用户
        List<User> unauthorizedUserList = this.getUnauthorizedUserList();
        su.setUnauthorizedUserList(unauthorizedUserList);
        su.setUnauthorizedUserSum(unauthorizedUserList.size());
        // 被阻止用户
        List<User> allUserPausedList = this.getAllUserPausedList();
        su.setPausedUserList(allUserPausedList);
        su.setUserPausedSum(allUserPausedList.size());
        List<User> adminUserList = this.getAdminUserList();
        // 管理员用户
        su.setAdminList(adminUserList);
        su.setAdminSum(adminUserList.size());
        su.setUserSum(allUserNormalList.size() + allUserPausedList.size()+adminUserList.size()+unauthorizedUserList.size());
        return su;
    }
    public SummaryUserInfo getSummaryByDepartmnet(Integer domain, String department) throws Exception {
        SummaryUserInfo su = new SummaryUserInfo();
        // 活跃用户
        List<User> activeUserList = this.getActiveUserListByDepartmnet(domain, department);
        su.setActiveSum(activeUserList.size());
        su.setActiveUserList(activeUserList);
        // 不活跃用户
        List<User> unActiveUserList = this.getUnActiveUserListByDepartmnet(domain, department);
        su.setUnActiveUserList(unActiveUserList);
        su.setUnActiveSum(unActiveUserList.size());
        // 正常用户
        List<User> allUserNormalList = this.getUserNormalListByDepartmnet(department);
        su.setNormalUserList(allUserNormalList);
        su.setUserNormalSum(allUserNormalList.size());
        // 未授权用户
        List<User> unauthorizedUserList = this.getUnauthorizedUserListByDepartmnet(department);
        su.setUnauthorizedUserList(unauthorizedUserList);
        su.setUnauthorizedUserSum(unauthorizedUserList.size());
        // 被阻止用户
        List<User> allUserPausedList = this.getUserPausedListByDepartmnet(department);
        su.setPausedUserList(allUserPausedList);
        su.setUserPausedSum(allUserPausedList.size());
        List<User> adminUserList = this.getAdminUserList();
        // 管理员用户
        su.setAdminList(adminUserList);
        su.setAdminSum(adminUserList.size());
        su.setUserSum(allUserNormalList.size() + allUserPausedList.size());
        return su;
    }
    /**
     * 获取该用户所有权限
     * 
     * @param username
     */
    public void getAuthByUser(String username) throws Exception {

    }

    /**
     * 查询该租户下所有的用户
     * 
     * @param tenantname
     * @return
     */
    /*
     * public List<User> listUsersByTenant(String tenantname){ String lable =
     * "labelSelector=nephele_tenant_+"+tenantname+"="+tenantname;
     * K8SClientResponse response =
     * roleBindingService.getRolebindingListbyLabelSelector(lable);
     * RoleBindingList roleBindingList = K8SClient.converToBean(response,
     * RoleBindingList.class);
     * 
     * return null; }
     */

    /**
     * 获取所有机器账号
     * 
     * @return
     */
    public ActionReturnUtil listMachineUsers() throws Exception {

        List<User> listMachineUsers = userMapper.listMachineUsers();
        List<Map<String, Object>> list = new ArrayList<>();
        for (User user : listMachineUsers) {
            Map<String, Object> map = new HashMap<>();
            map.put("createTime", user.getCreateTime());
            map.put("userName", user.getUsername());
            list.add(map);
        }
        return ActionReturnUtil.returnSuccessWithData(list);
    }

    /**
     * 获取所有管理员
     * 
     * @return
     */

    public ActionReturnUtil listAdmin() throws Exception {
        List<User> listadmin = userMapper.listAdmin();
        List<Map<String, Object>> list = new ArrayList<>();
        for (User user : listadmin) {
            Map<String, Object> map = new HashMap<>();
            map.put("createTime", user.getCreateTime());
            map.put("userName", user.getUsername());
            map.put("email", user.getEmail());
            map.put("realName", user.getRealName());
            list.add(map);
        }
        return ActionReturnUtil.returnSuccessWithData(list);
    }

    /**
     * 用户列表
     * 
     * @throws Exception
     */
    public ActionReturnUtil listUsers() throws Exception {

        // 查询harbor用户
        String cookie = harborUtil.checkCookieTimeout();
        Map<String, Object> header = new HashMap<String, Object>();
        List<Map<String, Object>> result = null;
        header.put("Cookie", cookie);
        String userPath = "http://" + harborIP + ":" + harborPort + "/api/users";
        HttpClientResponse httpClientResponse = HttpClientUtil.doGet(userPath, null, header);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(httpClientResponse.getBody())) {
            result = JsonUtil.JsonToMapList(httpClientResponse.getBody());
        }
        // 查询k8s用户
        List<UserShowDto> userNameList = new ArrayList<UserShowDto>();
        List<User> users = userMapper.listUsers();
        // 取k8s和harbor用户交集
        for (Map<String, Object> harborMap : result) {
            String harbor = (String) harborMap.get("username");
            for (User user : users) {
                String k8s = user.getUsername();
                if (harbor.equals(k8s)) {
                    UserGroupRelationExample ugr = new UserGroupRelationExample();
                    ugr.createCriteria().andUseridEqualTo(user.getId());
                    UserShowDto u = new UserShowDto();
                    if(null != usergrouprelationMapper.selectByExample(ugr) && usergrouprelationMapper.selectByExample(ugr).size()>0){
                        int groupid = usergrouprelationMapper.selectByExample(ugr).get(0).getGroupid();
                        String groupname = usergroupMapper.selectByPrimaryKey(groupid).getGroupname();
                        u.setGroupName(groupname);
                    }
                    u.setIsTm(user.getIsAdmin() == 1);
                    u.setName(user.getUsername());
                    u.setNikeName(user.getRealName());
                    u.setEmail(user.getEmail());
                    u.setComment(user.getComment());
                    u.setPause(user.getPause());
                    u.setPhone(user.getPhone());
                    User user1 = userMapper.findAthorizeByUsername(user.getUsername());
                    if(user1!=null){
                        u.setIsAuthorize(1);
                    }else{
                        u.setIsAuthorize(0);
                    }

                    Date createTime = user.getCreateTime();
                    String date = DateUtil.DateToString(createTime, DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z);
                    u.setCreateTime(date);
                    if (user.getUpdateTime() == null) {
                        u.setUpdateTime("");
                    } else {
                        Date updateTime = user.getUpdateTime();
                        u.setUpdateTime(DateUtil.DateToString(updateTime, DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z));
                    }
                    userNameList.add(u);
                    break;
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithDataAndCount(userNameList,userNameList.size());
    }

    public String getPassword(String userName) {
        AuthUserExample example = new AuthUserExample();
        example.createCriteria().andNameEqualTo(userName);
        List<AuthUser> authUsers = this.authUserMapper.selectByExample(example);
        if (authUsers == null || authUsers.size() <= 0) {
            return null;
        }
        return authUsers.get(0).getPassword();
    }

    public void addLdapUser(String userName, String password, String harborId) {
        AuthUser user = new AuthUser();
        user.setName(userName);
        user.setHarborId(harborId);
        user.setPassword(password);
        authUserMapper.insertSelective(user);
    }

    public void updateLdapUser(String userName, String password) throws Exception {
        AuthUserExample example = new AuthUserExample();
        example.createCriteria().andNameEqualTo(userName);
        List<AuthUser> authUsers = authUserMapper.selectByExample(example);
        if (authUsers != null && authUsers.size() >= 0) {
            AuthUser ldapUser = authUsers.get(0);
            ldapUser.setPassword(password);
            authUserMapper.updateByPrimaryKey(ldapUser);
            // 更新harbor账户密码
            String userPath = "http://" + harborIP + ":" + harborPort + "/api/users?username=" + userName;
            String cookie = harborUtil.checkCookieTimeout();
            Map<String, Object> header = new HashMap<String, Object>();
            header.put("Cookie", cookie);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("old_password", ldapUser.getPassword());
            params.put("new_password", password);
            HttpClientResponse httpClientResponse = HttpClientUtil.doGet(userPath, null, header);
            List<Map<String, Object>> result = JsonUtil.JsonToMapList(httpClientResponse.getBody());
            if (result != null && result.size() > 0) {
                Map<String, Object> user = result.get(0);
                String userId = user.get("user_id").toString();
                String updateUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + userId + "/password";
                String dlCookie = harborUtil.checkCookieTimeout();
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Cookie", dlCookie);
                HttpClientUtil.doPut(updateUrl, params, headers);
            }
        }
    }

    /**
     * 检查用户名是否已存在,存在返回true,不存在返回false
     * 
     * @param userName
     * @return
     */
    private Boolean checkUserName(String userName) throws Exception {
        User user = userMapper.findByUsername(userName);
        return user != null;
    }

    /**
     * 检查邮箱是否已存在,存在返回true,不存在返回false
     * 
     * @param email
     * @return
     */
    private Boolean checkEmail(String email) throws Exception {
        User user = userMapper.findUserByEmail(email);
        return user != null;
    }

    /**
     * 创建用户群组
     * 
     * @param UserGroup
     * @return ActionReturnUtil
     */
    public ActionReturnUtil create_group(UserGroup usergroup) throws Exception {
        if (usergroup == null) {
            return ActionReturnUtil.returnErrorWithData("创建群组对象不能为空！");
        }
        if (usergroup.getGroupname() == "" || usergroup.getGroupname() == null) {
            return ActionReturnUtil.returnErrorWithData("群组名不能为空！");
        }
        // 在user_group表中增加数据
        usergroupMapper.insert(usergroup);
        UserGroupExample ugexample = new UserGroupExample();
        ugexample.createCriteria().andGroupnameEqualTo(usergroup.getGroupname());
        // 获取用户群组id，群组名唯一
        int groupid = usergroupMapper.selectByExample(ugexample).get(0).getId();
        // 在user_group_relation表中增加数据
        List<User> ls = usergroup.getUsers();
        List<UserGroupRelation> ints = new ArrayList<UserGroupRelation>();
        for (int i = 0; i < ls.size(); i++) {
            String username = ls.get(i).getUsername();
            UserExample example = new UserExample();
            example.createCriteria().andUsernameEqualTo(username);
            UserGroupRelation ugr = new UserGroupRelation();
            ugr.setGroupid(groupid);
            // 用户名唯一
            ugr.setUserid(userMapperNew.selectByExample(example).get(0).getUuid());
            ints.add(ugr);
        }
        // 在user_ group_relation表中增加数据
        usergrouprelationMapper.addUserGroupRelation(ints);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 删除用户群组
     * 
     * @param List<String>
     *            groupnames
     * @return ActionReturnUtil
     */
    public ActionReturnUtil delete_group(List<String> groupnames) throws Exception {
        // 删除user_group表中的相关数据同时删除user_group_relation表中的关联数据
        try {
            UserGroupExample delexample = new UserGroupExample();
            Criteria criteria = delexample.createCriteria();
            criteria.andGroupnameIn(groupnames);
            usergroupMapper.deleteByExample(delexample);
            return ActionReturnUtil.returnSuccess();
        } catch (Exception e) {
            return ActionReturnUtil.returnErrorWithData(e);
        }
    }

    /**
     * 删除用户群组
     * 
     * @param List<String>
     *            groupnames
     * @return ActionReturnUtil
     */
    public ActionReturnUtil delete_groupbyid(int groupid) throws Exception {
        // 删除user_group表中的相关数据同时删除user_group_relation表中的关联数据
        try {
            UserGroupExample delexample = new UserGroupExample();
            delexample.createCriteria().andIdEqualTo(groupid);
            usergroupMapper.deleteByExample(delexample);
            UserGroupRelationExample ugrexample = new UserGroupRelationExample();
            ugrexample.createCriteria().andGroupidEqualTo(groupid);
            usergrouprelationMapper.deleteByExample(ugrexample);
            return ActionReturnUtil.returnSuccess();
        } catch (Exception e) {
            return ActionReturnUtil.returnErrorWithData(e);
        }
    }

    /**
     * 修改用户群组信息
     * 
     * @param UserGroupDto
     *            usergroupdto
     * @return ActionReturnUtil
     */
    public ActionReturnUtil updateGroup(UserGroupDto usergroupdto) throws Exception {
        if (usergroupdto == null) {
            return ActionReturnUtil.returnErrorWithMsg("修改参数不能为空！");
        }
        // 判断修改了那些内容：群组名、描述、增、删用户
        // 只修改群组名称
        if (usergroupdto.getUpdategroupname() != null && usergroupdto.getUpdatedescribe() == null && usergroupdto.getAddusers() == null && usergroupdto.getDelusers() == null) {
            UserGroup usergroup = new UserGroup();
            usergroup.setGroupname(usergroupdto.getUpdategroupname());
            usergroup.setUserGroupDescribe(usergroupdto.getUsergroup().getUserGroupDescribe());
            usergroup.setId(usergroupdto.getUsergroup().getId());
            usergroupMapper.updateByPrimaryKeySelective(usergroup);
        } else if (usergroupdto.getUpdategroupname() == null && usergroupdto.getUpdatedescribe() != null && usergroupdto.getAddusers() == null
                && usergroupdto.getDelusers() == null) {
            // 只修改群组描述
            UserGroup usergroup = new UserGroup();
            usergroup.setGroupname(usergroupdto.getUsergroup().getGroupname());
            usergroup.setUserGroupDescribe(usergroupdto.getUpdatedescribe());
            usergroup.setId(usergroupdto.getUsergroup().getId());
            usergroupMapper.updateByPrimaryKeySelective(usergroup);
        } else if (usergroupdto.getUpdategroupname() == null && usergroupdto.getUpdatedescribe() == null && usergroupdto.getAddusers() != null
                && usergroupdto.getDelusers() == null) {
            // 只增加用户
            List<UserGroupRelation> ugrs = new ArrayList<UserGroupRelation>();
            List<String> users = usergroupdto.getAddusers();
            int groupid = usergroupdto.getUsergroup().getId();
            for (int i = 0; i < users.size(); i++) {
                UserGroupRelation ugr = new UserGroupRelation();
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(users.get(i));
                User user = userMapperNew.selectByExample(example).get(0);
                ugr.setGroupid(groupid);
                ugr.setUserid(user.getUuid());
                ugrs.add(ugr);
            }
            usergrouprelationMapper.addUserGroupRelation(ugrs);
        } else if (usergroupdto.getUpdategroupname() == null && usergroupdto.getUpdatedescribe() == null && usergroupdto.getAddusers() == null
                && usergroupdto.getDelusers() != null) {
            // 只删除用户
            List<String> users = usergroupdto.getDelusers();
            int groupid = usergroupdto.getUsergroup().getId();
            for (int i = 0; i < users.size(); i++) {
                UserGroupRelationExample ugrexample = new UserGroupRelationExample();
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(users.get(i));
                User user = userMapperNew.selectByExample(example).get(0);
                Long userid = user.getUuid();
                ugrexample.createCriteria().andGroupidEqualTo(groupid).andUseridEqualTo(userid);
                usergrouprelationMapper.deleteByExample(ugrexample);
            }
        } else if (usergroupdto.getUpdategroupname() != null && usergroupdto.getUpdatedescribe() != null && usergroupdto.getAddusers() == null
                && usergroupdto.getDelusers() == null) {
            // 修改群组名和描述
            UserGroup usergroup = new UserGroup();
            usergroup.setGroupname(usergroupdto.getUpdategroupname());
            usergroup.setUserGroupDescribe(usergroupdto.getUpdatedescribe());
            usergroup.setId(usergroupdto.getUsergroup().getId());
            usergroupMapper.updateByPrimaryKeySelective(usergroup);
        } else if (usergroupdto.getUpdategroupname() == null && usergroupdto.getUpdatedescribe() == null && usergroupdto.getAddusers() != null
                && usergroupdto.getDelusers() != null) {
            // 增、删用户
            List<UserGroupRelation> ugrs = new ArrayList<UserGroupRelation>();
            List<String> addusers = usergroupdto.getAddusers();
            int groupid = usergroupdto.getUsergroup().getId();
            for (int i = 0; i < addusers.size(); i++) {
                UserGroupRelation ugr = new UserGroupRelation();
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(addusers.get(i));
                User user = userMapperNew.selectByExample(example).get(0);
                ugr.setGroupid(groupid);
                ugr.setUserid(user.getUuid());
                ugrs.add(ugr);
            }
            usergrouprelationMapper.addUserGroupRelation(ugrs);
            List<String> delusers = usergroupdto.getDelusers();
            for (int i = 0; i < delusers.size(); i++) {
                UserGroupRelationExample ugrexample = new UserGroupRelationExample();
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(delusers.get(i));
                User user = userMapperNew.selectByExample(example).get(0);
                Long userid = user.getUuid();
                ugrexample.createCriteria().andGroupidEqualTo(groupid).andUseridEqualTo(userid);
                usergrouprelationMapper.deleteByExample(ugrexample);
            }
        } else if (usergroupdto.getUpdategroupname() != null && usergroupdto.getUpdatedescribe() == null && usergroupdto.getAddusers() != null
                && usergroupdto.getDelusers() == null) {
            // 修改了群组名、增用户
            UserGroup usergroup = new UserGroup();
            usergroup.setGroupname(usergroupdto.getUpdategroupname());
            usergroup.setUserGroupDescribe(usergroupdto.getUsergroup().getUserGroupDescribe());
            usergroup.setId(usergroupdto.getUsergroup().getId());
            usergroupMapper.updateByPrimaryKeySelective(usergroup);
            List<UserGroupRelation> ugrs = new ArrayList<UserGroupRelation>();
            List<String> addusers = usergroupdto.getAddusers();
            int groupid = usergroupdto.getUsergroup().getId();
            for (int i = 0; i < addusers.size(); i++) {
                UserGroupRelation ugr = new UserGroupRelation();
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(addusers.get(i));
                User user = userMapperNew.selectByExample(example).get(0);
                ugr.setGroupid(groupid);
                ugr.setUserid(user.getUuid());
                ugrs.add(ugr);
            }
            usergrouprelationMapper.addUserGroupRelation(ugrs);
        } else if (usergroupdto.getUpdategroupname() == null && usergroupdto.getUpdatedescribe() != null && usergroupdto.getAddusers() != null
                && usergroupdto.getDelusers() == null) {
            // 修改了群描述、增用户
            UserGroup usergroup = new UserGroup();
            usergroup.setGroupname(usergroupdto.getUsergroup().getGroupname());
            usergroup.setUserGroupDescribe(usergroupdto.getUpdatedescribe());
            usergroup.setId(usergroupdto.getUsergroup().getId());
            usergroupMapper.updateByPrimaryKeySelective(usergroup);
            List<UserGroupRelation> ugrs = new ArrayList<UserGroupRelation>();
            List<String> addusers = usergroupdto.getAddusers();
            int groupid = usergroupdto.getUsergroup().getId();
            for (int i = 0; i < addusers.size(); i++) {
                UserGroupRelation ugr = new UserGroupRelation();
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(addusers.get(i));
                User user = userMapperNew.selectByExample(example).get(0);
                ugr.setGroupid(groupid);
                ugr.setUserid(user.getUuid());
                ugrs.add(ugr);
            }
            usergrouprelationMapper.addUserGroupRelation(ugrs);
        } else if (usergroupdto.getUpdategroupname() != null && usergroupdto.getUpdatedescribe() == null && usergroupdto.getAddusers() == null
                && usergroupdto.getDelusers() != null) {
            // 修改了群组名、删除用户
            UserGroup usergroup = new UserGroup();
            usergroup.setGroupname(usergroupdto.getUpdategroupname());
            usergroup.setUserGroupDescribe(usergroupdto.getUsergroup().getUserGroupDescribe());
            usergroup.setId(usergroupdto.getUsergroup().getId());
            usergroupMapper.updateByPrimaryKeySelective(usergroup);
            List<String> users = usergroupdto.getDelusers();
            int groupid = usergroupdto.getUsergroup().getId();
            for (int i = 0; i < users.size(); i++) {
                UserGroupRelationExample ugrexample = new UserGroupRelationExample();
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(users.get(i));
                User user = userMapperNew.selectByExample(example).get(0);
                Long userid = user.getUuid();
                ugrexample.createCriteria().andGroupidEqualTo(groupid).andUseridEqualTo(userid);
                usergrouprelationMapper.deleteByExample(ugrexample);
            }
        } else if (usergroupdto.getUpdategroupname() == null && usergroupdto.getUpdatedescribe() != null && usergroupdto.getAddusers() == null
                && usergroupdto.getDelusers() != null) {
            // 修改了群描述、删除用户
            UserGroup usergroup = new UserGroup();
            usergroup.setGroupname(usergroupdto.getUsergroup().getGroupname());
            usergroup.setUserGroupDescribe(usergroupdto.getUpdatedescribe());
            usergroup.setId(usergroupdto.getUsergroup().getId());
            usergroupMapper.updateByPrimaryKeySelective(usergroup);
            List<String> users = usergroupdto.getDelusers();
            int groupid = usergroupdto.getUsergroup().getId();
            for (int i = 0; i < users.size(); i++) {
                UserGroupRelationExample ugrexample = new UserGroupRelationExample();
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(users.get(i));
                User user = userMapperNew.selectByExample(example).get(0);
                Long userid = user.getUuid();
                ugrexample.createCriteria().andGroupidEqualTo(groupid).andUseridEqualTo(userid);
                usergrouprelationMapper.deleteByExample(ugrexample);
            }
        } else if (usergroupdto.getUpdategroupname() != null && usergroupdto.getUpdatedescribe() != null && usergroupdto.getAddusers() != null
                && usergroupdto.getDelusers() == null) {
            // 修改了群描述、群组名、增加用户
            UserGroup usergroup = new UserGroup();
            usergroup.setGroupname(usergroupdto.getUpdategroupname());
            usergroup.setUserGroupDescribe(usergroupdto.getUpdatedescribe());
            usergroup.setId(usergroupdto.getUsergroup().getId());
            usergroupMapper.updateByPrimaryKeySelective(usergroup);
            List<UserGroupRelation> ugrs = new ArrayList<UserGroupRelation>();
            List<String> addusers = usergroupdto.getAddusers();
            int groupid = usergroupdto.getUsergroup().getId();
            for (int i = 0; i < addusers.size(); i++) {
                UserGroupRelation ugr = new UserGroupRelation();
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(addusers.get(i));
                User user = userMapperNew.selectByExample(example).get(0);
                ugr.setGroupid(groupid);
                ugr.setUserid(user.getUuid());
                ugrs.add(ugr);
            }
            usergrouprelationMapper.addUserGroupRelation(ugrs);
        } else if (usergroupdto.getUpdategroupname() != null && usergroupdto.getUpdatedescribe() != null && usergroupdto.getAddusers() == null
                && usergroupdto.getDelusers() != null) {
            // 修改了群描述、群组名、删除用户
            UserGroup usergroup = new UserGroup();
            usergroup.setGroupname(usergroupdto.getUpdategroupname());
            usergroup.setUserGroupDescribe(usergroupdto.getUpdatedescribe());
            usergroup.setId(usergroupdto.getUsergroup().getId());
            usergroupMapper.updateByPrimaryKeySelective(usergroup);
            List<String> users = usergroupdto.getDelusers();
            int groupid = usergroupdto.getUsergroup().getId();
            for (int i = 0; i < users.size(); i++) {
                UserGroupRelationExample ugrexample = new UserGroupRelationExample();
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(users.get(i));
                User user = userMapperNew.selectByExample(example).get(0);
                Long userid = user.getUuid();
                ugrexample.createCriteria().andGroupidEqualTo(groupid).andUseridEqualTo(userid);
                usergrouprelationMapper.deleteByExample(ugrexample);
            }
        } else if (usergroupdto.getUpdategroupname() != null && usergroupdto.getUpdatedescribe() != null && usergroupdto.getAddusers() != null
                && usergroupdto.getDelusers() != null) {
            // 修改了群描述、群组名、增加用户、删除用户
            UserGroup usergroup = new UserGroup();
            usergroup.setGroupname(usergroupdto.getUpdategroupname());
            usergroup.setUserGroupDescribe(usergroupdto.getUpdatedescribe());
            usergroup.setId(usergroupdto.getUsergroup().getId());
            usergroupMapper.updateByPrimaryKeySelective(usergroup);
            List<UserGroupRelation> ugrs = new ArrayList<UserGroupRelation>();
            List<String> addusers = usergroupdto.getAddusers();
            int groupid = usergroupdto.getUsergroup().getId();
            for (int i = 0; i < addusers.size(); i++) {
                UserGroupRelation ugr = new UserGroupRelation();
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(addusers.get(i));
                User user = userMapperNew.selectByExample(example).get(0);
                ugr.setGroupid(groupid);
                ugr.setUserid(user.getUuid());
                ugrs.add(ugr);
            }
            usergrouprelationMapper.addUserGroupRelation(ugrs);
            List<String> delusers = usergroupdto.getDelusers();
            for (int i = 0; i < delusers.size(); i++) {
                UserGroupRelationExample ugrexample = new UserGroupRelationExample();
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(delusers.get(i));
                User user = userMapperNew.selectByExample(example).get(0);
                Long userid = user.getUuid();
                ugrexample.createCriteria().andGroupidEqualTo(groupid).andUseridEqualTo(userid);
                usergrouprelationMapper.deleteByExample(ugrexample);
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 查询所有群组信息
     * 
     * @param
     * @return List<UserGroup>
     */
    public List<UserGroup> get_groups() throws Exception {
        UserGroupExample ugexample = new UserGroupExample();
        List<UserGroup> ugs = usergroupMapper.selectByExample(ugexample);
        for (int i = 0; i < ugs.size(); i++) {
            int groupid = ugs.get(i).getId();
            UserGroupRelationExample ugrexample = new UserGroupRelationExample();
            ugrexample.createCriteria().andGroupidEqualTo(groupid);
            ugs.get(i).setUserNumber(usergrouprelationMapper.selectByExample(ugrexample).size());
        }
        return ugs;
    }

    /**
     * 查询群组是否重名
     * 
     * @param groupname
     * @return boolean
     */
    public boolean issame(String groupname) throws Exception {
        UserGroupExample example = new UserGroupExample();
        example.createCriteria().andGroupnameEqualTo(groupname);
        // 群组名唯一，所以只有一个
        List<UserGroup> ug = usergroupMapper.selectByExample(example);
        if (ug == null || ug.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 根据群组id,获取用户详情
     * 
     * @param groupid
     * @return list<User>
     */
    public List<User> searchuserbygroupid(int groupid) throws Exception {
        UserGroupRelationExample ugrexample = new UserGroupRelationExample();
        ugrexample.createCriteria().andGroupidEqualTo(groupid);
        List<UserGroupRelation> ls = usergrouprelationMapper.selectByExample(ugrexample);
        List<User> users = new ArrayList<User>();
        for (int i = 0; i < ls.size(); i++) {
            User user = new User();
            Long userid = ls.get(i).getUserid();
            UserExample uexample = new UserExample();
            uexample.createCriteria().andUuidEqualTo(userid);
            user = userMapperNew.selectByExample(uexample).get(0);
            users.add(user);
        }
        return users;
    }

    /**
     * 根据用户名获取群组详情
     *
     * @param username
     * @return ActionReturnUtil
     */

    public ActionReturnUtil searchGroupByUsername(String username) throws Exception{
        if(username == null){
            return ActionReturnUtil.returnErrorWithData("用户名不能为空!");
        }
        //根据传入的username模糊查询用户，获取list<uuid>
        List<User> users = userMapperNew.selectLikeUsername(username);
        //判断是否存在user用户
        if(users.size() == 0 || null == users){
            return ActionReturnUtil.returnSuccessWithData(null);
        }
        //定义反馈的群组信息
        List<UserGroup> ls = new ArrayList<UserGroup>();
        boolean flag;
        //遍历List<user>
        for(int i=0;i<users.size();i++){
            flag = false;
            //根据传入的user获取所属群组信息
            Long uuid = users.get(i).getUuid();
            UserGroupRelationExample ugr = new UserGroupRelationExample();
            ugr.createCriteria().andUseridEqualTo(uuid);
            List<UserGroupRelation> usergrouprelations = usergrouprelationMapper.selectByExample(ugr);
            //如果用户没有分配群组，则跳过进入下一个用户
            if(usergrouprelations.size() == 0){
                break;
            }
            //一人只属于一群组，直接get(0)
            UserGroupRelation usergrouprelation = usergrouprelationMapper.selectByExample(ugr).get(0);
            //此时获取当前用户所属群组
            int groupid = usergrouprelation.getGroupid();
            UserGroup usergroup = usergroupMapper.selectByPrimaryKey(groupid);
            //遍历反馈的信息，如果用户群组一样，那么在当前群组里面插入到List<User>的属性中，如果没有找到，那么整个插入新增的一条数据
            for(int j=0;j<ls.size();j++){
                //判断群组是否已存在list中，在则添加到当前群组的List<User>属性中，并结束内层for循环。
                if(usergroup.getGroupname() == ls.get(j).getGroupname()){

                    ls.get(j).getUsers().add(users.get(i));
                    flag = true;
                    break;
                }
            }
            if(flag){
                continue;
            }
            //如果当前list中无此群组信息，则加入
            List<User> listusers = new ArrayList<User>();
            listusers.add(users.get(i));
            usergroup.setUsers(listusers);
            ls.add(usergroup);
        }
        return ActionReturnUtil.returnSuccessWithData(ls);
    }

    /**
     * 根据用户名获取群组详情
     * 
     * @param username
     * @return List<UserGroup>
     */
    public UserGroup search_group_username(String username) throws Exception {
        UserExample uexample = new UserExample();
        uexample.createCriteria().andUsernameEqualTo(username);
        Long userid = userMapperNew.selectByExample(uexample).get(0).getUuid();
        UserGroupRelationExample ugrexample = new UserGroupRelationExample();
        ugrexample.createCriteria().andUseridEqualTo(userid);
        int groupid = usergrouprelationMapper.selectByExample(ugrexample).get(0).getGroupid();
        UserGroup usergroup = usergroupMapper.selectByPrimaryKey(groupid);
        return usergroup;
    }

    /**
     * 根据群组名获取用户详情
     * 
     * @param username
     * @return List<UserGroup>
     */
    public List<User> search_users_groupname(String groupname) throws Exception {
        UserGroupExample ugexample = new UserGroupExample();
        ugexample.createCriteria().andGroupnameEqualTo(groupname);
        int groupid = usergroupMapper.selectByExample(ugexample).get(0).getId();
        UserGroupRelationExample ugrexample = new UserGroupRelationExample();
        ugrexample.createCriteria().andGroupidEqualTo(groupid);
        List<UserGroupRelation> ls = usergrouprelationMapper.selectByExample(ugrexample);
        List<User> users = new ArrayList<User>();
        for (int i = 0; i < ls.size(); i++) {
            User user = new User();
            UserExample example = new UserExample();
            example.createCriteria().andUuidEqualTo(ls.get(i).getUserid());
            user = userMapperNew.selectByExample(example).get(0);
            if(userMapper.findAthorizeByUsername(user.getUsername())!=null){
                user.setIsAuthorize(1);
            }else {
                user.setIsAuthorize(0);
            }
            users.add(user);
        }
        return users;
    }

    /**
     * 群组展示用户
     * 
     * @return List<UserShowDto>
     */
    public ActionReturnUtil listUserswithoutgroup() throws Exception {
        // 查询harbor用户
        String cookie = harborUtil.checkCookieTimeout();
        Map<String, Object> header = new HashMap<String, Object>();
        List<Map<String, Object>> result = null;
        header.put("Cookie", cookie);
        String userPath = "http://" + harborIP + ":" + harborPort + "/api/users";
        HttpClientResponse httpClientResponse = HttpClientUtil.doGet(userPath, null, header);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(httpClientResponse.getBody())) {
            result = JsonUtil.JsonToMapList(httpClientResponse.getBody());
        }
        // 查询k8s用户
        List<UserShowDto> userNameList = new ArrayList<UserShowDto>();
        List<User> users = userMapper.listUsers();
        // 取k8s和harbor用户交集
        for (Map<String, Object> harborMap : result) {
            String harbor = (String) harborMap.get("username");
            for (User user : users) {
                String k8s = user.getUsername();
                if (harbor.equals(k8s)) {
                    UserShowDto u = new UserShowDto();
                    u.setIsTm(user.getIsAdmin() == 1);
                    u.setName(user.getUsername());
                    u.setNikeName(user.getRealName());
                    u.setEmail(user.getEmail());
                    u.setComment(user.getComment());
                    u.setPause(user.getPause());
                    Date createTime = user.getCreateTime();
                    String date = DateUtil.DateToString(createTime, DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z);
                    u.setCreateTime(date);
                    if (user.getUpdateTime() == null) {
                        u.setUpdateTime("");
                    } else {
                        Date updateTime = user.getUpdateTime();
                        u.setUpdateTime(DateUtil.DateToString(updateTime, DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z));
                    }
                    userNameList.add(u);
                    break;
                }
            }
        }
        // 用户群组所占用的用户
        UserGroupRelationExample ugrexample = new UserGroupRelationExample();
        List<UserGroupRelation> ugr = usergrouprelationMapper.selectByExample(ugrexample);
        List<Integer> ls = new ArrayList<Integer>();
        List<String> username = new ArrayList<String>();
        for (int i = 0; i < ugr.size(); i++) {
            Long uuid = ugr.get(i).getUserid();
            ls.add(uuid.intValue());
            UserExample example = new UserExample();
            example.createCriteria().andUuidEqualTo(uuid);
            username.add(userMapperNew.selectByExample(example).get(0).getUsername());
        }
        // 展示用户剔除已加入群组用户,查询在userNameList对应的下标，并remove
        for (int j = 0; j < username.size(); j++) {
            for (int g = 0; g < userNameList.size(); g++) {
                if (username.get(j).equals(userNameList.get(g).getName())) {
                    userNameList.remove(g);
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(userNameList);
    }

    /**
     * 文件导出
     * 
     * @param
     * @return void
     */
    public ActionReturnUtil fileexport(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            String[] title = {"平台账号", "密码", "用户邮箱", "姓名", "手机号", "备注"};
            HttpServletResponse response = null;
            // 创建Excel工作簿
            HSSFWorkbook workbook = new HSSFWorkbook();
            // 创建一个工作表sheet
            HSSFSheet sheet = workbook.createSheet();
            // 创建第一行
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = null;
            // 插入第一行数据id,name,sex
            for (int i = 0; i < title.length; i++) {
                cell = row.createCell(i);
                cell.setCellValue(title[i]);
            }
            response = resp;
            // response.reset();
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=users.xls");
            response.setContentType("application/msexcel"); // 设置生成的文件类型
            OutputStream output = response.getOutputStream();
            workbook.write(output);
            workbook.close();
            output.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 用户批量上传
     * 
     * @param
     * @return void
     */
    public ActionReturnUtil userBulkUpload(InputStream in, MultipartFile file) throws Exception {
        // 定义title用来判断excel表的数据title头是否正确
        String[] title = {"平台账号", "密码", "用户邮箱", "姓名", "手机号", "备注"};
        List<String> list = new ArrayList<String>();
        List<List<Object>> listob = ExcelUtil.getUserListByExcel(in, file.getOriginalFilename());
        if (listob == null || listob.size()<=0) {
            return ActionReturnUtil.returnErrorWithMsg("文件不能为空!");
        }
        // 遍历listob数据，把数据放到List中
        try {
            for (int i = 0; i < listob.size(); i++) {
                List<Object> ob = listob.get(i);
                User user = new User();
                if(ob.size()<=5&&(ob.get(0) == null || ob.get(0).toString().trim().isEmpty()) || (ob.get(1) == null || ob.get(1).toString().trim().isEmpty())
                        || (ob.get(2) == null || ob.get(2).toString().trim().isEmpty()) || (ob.get(3) == null || ob.get(3).toString().trim().isEmpty())
                        || (ob.get(4) == null || ob.get(4).toString().trim().isEmpty())){
                    return ActionReturnUtil.returnErrorWithMsg("第"+(i+1)+"行，缺少数据项");
                }
                if ((ob.get(0) == null || ob.get(0).toString().trim().isEmpty()) && (ob.get(1) == null || ob.get(1).toString().trim().isEmpty())
                        && (ob.get(2) == null || ob.get(2).toString().trim().isEmpty()) && (ob.get(3) == null || ob.get(3).toString().trim().isEmpty())
                        && (ob.get(4) == null || ob.get(4).toString().trim().isEmpty())) {
                    //如果是第一行则直接返回
                    if(i==0){
                        return ActionReturnUtil.returnErrorWithMsg("文件不能为空！");
                    }else {
                        // 跳过空行
                        break;
                    }
                }
                // 通过遍历获取每一列用户数据
                if(ob.get(0).toString().trim().isEmpty()||ob.get(0) == null){
                    return ActionReturnUtil.returnErrorWithMsg("第" + (i + 1) + "行,用户名不能为空");
                }else if(ob.get(0) != null && !ob.get(0).toString().trim().isEmpty()){
                    User userUsable = userMapper.findByUsername(String.valueOf(ob.get(0)));
                    if(userUsable!=null){
                        return ActionReturnUtil.returnErrorWithMsg("第" + (i + 1) + "行,用户名已存在");
                    }else{
                        user.setUsername(String.valueOf(ob.get(0)).trim());
                    }
                }

                if (ob.get(1) == null || ob.get(1).toString().trim().isEmpty()) {
                    return ActionReturnUtil.returnErrorWithMsg("第" + (i + 1) + "行,密码不能为空");
                } else if(ob.get(1) != null && !ob.get(1).toString().trim().isEmpty()){
                    String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{7,12}$";
                    String passWord = String.valueOf(ob.get(1)).trim();
                    boolean matches = passWord.matches(regex);
                    if (!matches) {
                        return ActionReturnUtil.returnErrorWithMsg("第" + (i + 1) + "行，密码必须是7-12位数字和字母的组合！");
                    }else {
                        user.setPassword(String.valueOf(ob.get(1)).trim());
                    }
                }


                if(ob.get(2) == null || ob.get(2).toString().trim().isEmpty()){
                    return ActionReturnUtil.returnErrorWithMsg("第" + (i + 1) + "行,用户邮箱不能为空");
                }
                else if (ob.get(2) != null && !ob.get(2).toString().trim().isEmpty()) {
                    String regex = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
                    boolean matches = String.valueOf(ob.get(2)).trim().matches(regex);
                    if (!matches) {
                        return ActionReturnUtil.returnErrorWithMsg("邮箱格式错误！");
                    }else {
                        User userUserableEmail = userMapper.findUserByEmail(ob.get(2).toString().trim());
                        if(userUserableEmail!=null){
                            return ActionReturnUtil.returnErrorWithMsg("第"+(i+1)+"行，用户邮箱已经注册！");
                        }
                        user.setEmail(String.valueOf(ob.get(2)).trim());
                    }
                }

                if (ob.get(3) == null || ob.get(3).toString().trim().isEmpty()) {
                    System.out.println(ob.get(3).toString().trim().isEmpty());
                    return ActionReturnUtil.returnErrorWithMsg("第" + (i + 1) + "行,真实姓名不能为空");
                } else {
                    user.setRealName(String.valueOf(ob.get(3)).trim());
                }

                if (ob.get(4) == null || ob.get(4).toString().trim().isEmpty()) {
                    return ActionReturnUtil.returnErrorWithMsg("第" + (i + 1) + "行,手机号不能为空");
                } else if (ob.get(4) != null && !ob.get(4).toString().trim().isEmpty()){
                    String str = String.valueOf(ob.get(4)).trim();
                    if (str.length()!=11){
                        return ActionReturnUtil.returnErrorWithMsg("第" + (i + 1) + "行,手机号格式错误");
                    }else{
                        user.setPhone(String.valueOf(ob.get(4)).trim());
                    }

                }
                if (ob.size() > 5 && ob.get(5) != null && !ob.get(5).toString().trim().isEmpty()) {
                    user.setComment(String.valueOf(ob.get(5)).trim());
                }

                System.out.println(user.getUsername()+"---"+user.getPassword()+"------"+user.getEmail()+"-----"+user.getPhone()+"====="+user.getComment());
                list.add(String.valueOf(ob.get(0)).trim());
                // 为了避免用户提交产生错误数据，所以一个个插入,
                excelAddUser(user, i + 1, list);
            }
        } catch (Exception e) {
            this.rollBackHarborUser(list);
            throw new MarsRuntimeException(e.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }
    private void rollBackHarborUser(List<String> list) throws Exception {
        for (int i = 0; i <= list.size() - 1; i++) {
            // 获取uuid
            UserExample example = new UserExample();
            example.createCriteria().andUsernameEqualTo(list.get(i));
            Long uuid = userMapperNew.selectByExample(example).get(0).getUuid();
            // 删除harbor用户
            String deleteUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + uuid;
            String dlCookie = harborUtil.checkCookieTimeout();
            Map<String, Object> headers = new HashMap<String, Object>();
            headers.put("Cookie", dlCookie);
            HttpClientUtil.doDelete(deleteUrl, null, headers);
        }
    }
    /**
     * 向k8s和harbor中新增用户
     * 
     * @param user
     * @param rowNumber
     * @return
     */
    public ActionReturnUtil excelAddUser(User user, int rowNumber, List<String> list) throws Exception {
        // 密码匹配
        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{7,12}$";
        String regex1 = "^[\u4E00-\u9FA5A-Za-z0-9]+$";
        boolean matches = user.getPassword().matches(regex);
        if (!matches) {
            for (int i = 0; i <= list.size() - 1; i++) {
                // 获取uuid
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(list.get(i));
                Long uuid = userMapperNew.selectByExample(example).get(0).getUuid();
                // 删除harbor用户
                String deleteUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + uuid;
                String dlCookie = harborUtil.checkCookieTimeout();
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Cookie", dlCookie);
                HttpClientUtil.doDelete(deleteUrl, null, headers);
            }
            return ActionReturnUtil.returnErrorWithMsg("第" + rowNumber + "行用户信息插入发生错误，" + "密码必须是数字+字母组合且长度不能小于7！");
        }
        // 用户名非重
        if (this.checkUserName(user.getUsername())) {
            for (int i = 0; i <= list.size() - 1; i++) {
                // 获取uuid
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(list.get(i));
                Long uuid = userMapperNew.selectByExample(example).get(0).getUuid();
                // 删除harbor用户
                String deleteUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + uuid;
                String dlCookie = harborUtil.checkCookieTimeout();
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Cookie", dlCookie);
                HttpClientUtil.doDelete(deleteUrl, null, headers);
            }
            return ActionReturnUtil.returnErrorWithMsg("第" + rowNumber + "行用户信息插入发生错误，" + "用户名重复！");
        }
        // 邮箱非重
        if (this.checkEmail(user.getEmail())) {
            for (int i = 0; i <= list.size() - 1; i++) {
                // 获取uuid
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(list.get(i));
                Long uuid = userMapperNew.selectByExample(example).get(0).getUuid();
                // 删除harbor用户
                String deleteUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + uuid;
                String dlCookie = harborUtil.checkCookieTimeout();
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Cookie", dlCookie);
                HttpClientUtil.doDelete(deleteUrl, null, headers);
            }
            return ActionReturnUtil.returnErrorWithMsg("第" + rowNumber + "行用户信息插入发生错误，" + "邮箱重复！");
        }
        // 真实用户名判断，过滤特殊符号
        boolean matchrealname = user.getRealName().matches(regex1);
        if (!matchrealname) {
            for (int i = 0; i <= list.size() - 1; i++) {
                // 获取uuid
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(list.get(i));
                Long uuid = userMapperNew.selectByExample(example).get(0).getUuid();
                // 删除harbor用户
                String deleteUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + uuid;
                String dlCookie = harborUtil.checkCookieTimeout();
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Cookie", dlCookie);
                HttpClientUtil.doDelete(deleteUrl, null, headers);
            }
            return ActionReturnUtil.returnErrorWithMsg("第" + rowNumber + "行用户信息插入发生错误，" + "真实姓名不符合要求！");
        }
        HarborUser harbor = new HarborUser();
        harbor.setUsername(user.getUsername());
        harbor.setPassword(user.getPassword());

        // 向harbor新增用户
        String addUrl = "http://" + harborIP + ":" + harborPort + "/api/users";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("username", user.getUsername());
        params.put("password", user.getPassword());
        params.put("realname", user.getRealName());
        params.put("comment", user.getComment());
        params.put("email", user.getEmail());
        String cookie = harborUtil.checkCookieTimeout();
        Map<String, Object> header = new HashMap<String, Object>();
        header.put("Cookie", cookie);
        header.put("Content-type", "application/json");
        String harborUId = null;

        CloseableHttpResponse response = HttpClientUtil.doBodyPost(addUrl, params, header);
        if (HttpStatusUtil.isSuccessStatus(response.getStatusLine().getStatusCode())) {
            // 密码md5加密
            // 获取harbor用户uuid作为user id
            Header[] headers = response.getHeaders("Location");
            if (headers.length > 0) {
                Header location = headers[0];
                harborUId = location.getValue().substring(location.getValue().lastIndexOf("/") + 1);
                String MD5password = StringUtil.convertToMD5(user.getPassword());
                user.setPassword(MD5password);
                user.setId(Long.valueOf(harborUId));
                user.setCreateTime(new Date());
                user.setPause(CommonConstant.NORMAL);
                userMapper.addUser(user);
                HarborUser harborUser2 = harboruserMapper.findByUsername(user.getUsername());
                if (harborUser2 == null) {
                    harboruserMapper.addUser(harbor);
                } else {
                    harboruserMapper.updatePassword(harbor.getUsername(), harbor.getPassword());
                }
                return ActionReturnUtil.returnSuccess();
            } else {
                for (int i = 0; i <= list.size() - 1; i++) {
                    // 获取uuid
                    UserExample example = new UserExample();
                    example.createCriteria().andUsernameEqualTo(list.get(i));
                    Long uuid = userMapperNew.selectByExample(example).get(0).getUuid();
                    // 删除harbor用户
                    String deleteUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + uuid;
                    String dlCookie = harborUtil.checkCookieTimeout();
                    Map<String, Object> head = new HashMap<String, Object>();
                    head.put("Cookie", dlCookie);
                    HttpClientUtil.doDelete(deleteUrl, null, head);
                }
                return ActionReturnUtil.returnErrorWithMsg("第" + rowNumber + "行用户信息插入发生错误,Create failed");
            }
        } else {
            for (int i = 0; i <= list.size() - 1; i++) {
                // 获取uuid
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(list.get(i));
                Long uuid = userMapperNew.selectByExample(example).get(0).getUuid();
                // 删除harbor用户
                String deleteUrl = "http://" + harborIP + ":" + harborPort + "/api/users/" + uuid;
                String dlCookie = harborUtil.checkCookieTimeout();
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.put("Cookie", dlCookie);
                HttpClientUtil.doDelete(deleteUrl, null, headers);
            }
            return ActionReturnUtil.returnErrorWithMsg("第" + rowNumber + "行用户信息插入发生错误,harbor Create failed");
        }
    }

    /**
     * 描述：根据文件后缀，自适应上传文件的版本
     */
    public static Workbook getWorkbook(InputStream inStr, String fileName) throws Exception {
        Workbook wb = null;
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if ("xls".equals(fileType)) {
            wb = new HSSFWorkbook(inStr); // 2003-
        } else if ("xlsx".equals(fileType)) {
            // wb = new XSSFWorkbook(in); //2007+
        } else {
            throw new Exception("当前文件非excel，请确认后重新上传！");
        }
        return wb;
    }

    /**
     * 描述：对表格中数值进行格式化
     */
    public static Object getCellValue(Cell cell) {
        Object value = null;
        DecimalFormat df = new DecimalFormat("0"); // 格式化字符类型的数字
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING :
                value = cell.getRichStringCellValue().getString();
                break;
            case Cell.CELL_TYPE_NUMERIC :
                if ("General".equals(cell.getCellStyle().getDataFormatString())) {
                    value = df.format(cell.getNumericCellValue());
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN :
                value = cell.getBooleanCellValue();
                break;
            case Cell.CELL_TYPE_BLANK :
                value = "";
                break;
            default :
                break;
        }
        return value;
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getHarborIP() {
        return harborIP;
    }

    public void setHarborIP(String harborIP) {
        this.harborIP = harborIP;
    }

    public String getHarborPort() {
        return harborPort;
    }

    public void setHarborPort(String harborPort) {
        this.harborPort = harborPort;
    }

    public String getHarborUser() {
        return harborUser;
    }

    public void setHarborUser(String harborUser) {
        this.harborUser = harborUser;
    }

    public String getHarborPassword() {
        return harborPassword;
    }

    public void setHarborPassword(String harborPassword) {
        this.harborPassword = harborPassword;
    }

    public String getHarborTimeout() {
        return harborTimeout;
    }

    public void setHarborTimeout(String harborTimeout) {
        this.harborTimeout = harborTimeout;
    }
}
