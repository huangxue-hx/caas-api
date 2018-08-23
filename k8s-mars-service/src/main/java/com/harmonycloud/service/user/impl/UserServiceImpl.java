package com.harmonycloud.service.user.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.user.*;
import com.harmonycloud.dao.user.bean.*;
import com.harmonycloud.dto.tenant.TenantDto;
import com.harmonycloud.dto.tenant.show.UserShowDto;
import com.harmonycloud.dto.user.ExcelUtil;
import com.harmonycloud.dto.user.SummaryUserInfo;
import com.harmonycloud.dto.user.UserDetailDto;
import com.harmonycloud.dto.user.UserGroupDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.service.cache.ClusterCacheManager;
import com.harmonycloud.service.platform.bean.harbor.HarborUser;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.harbor.HarborUserService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.RoleService;
import com.harmonycloud.service.user.UserRoleRelationshipService;
import com.harmonycloud.service.user.UserService;
import com.whchem.sso.common.utils.SSOConstants;
import com.whchem.sso.common.utils.SSOUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.FLAG_FALSE;
import static com.harmonycloud.common.Constant.CommonConstant.FLAG_TRUE;
import static com.harmonycloud.service.platform.constant.Constant.DB_BATCH_INSERT_COUNT;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-1-4
 * @Modified
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

    private static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserGroupRelationMapper usergrouprelationMapper;

    @Autowired
    private UserGroupMapper usergroupMapper;

    @Autowired
    private UserMapper userMapperNew;

    @Autowired
    private RoleService roleService;

    @Autowired
    private HarborUserService harborUserService;

    @Autowired
    private AuthUserMapper authUserMapper;

    @Autowired
    TenantService tenantService;
    @Autowired
    HttpSession session;
    @Autowired
    RoleLocalService roleLocalService;
    @Autowired
    UserRoleRelationshipService userRoleRelationshipService;
    @Autowired
    ClusterCacheManager clusterCacheManager;

    public String getCurrentUsername() {
        return (String) session.getAttribute("username");
    }

    /**
     * 从session中获取当前租户id
     *
     * @return
     */
    @Override
    public String getCurrentTenantId() {
        return (String)session.getAttribute(CommonConstant.TENANT_ID);
    }

    /**
     * 从session中获取当前租户别名
     *
     * @return
     */
    @Override
    public String getCurrentTenantAliasName() {
        return (String)session.getAttribute(CommonConstant.TENANT_ALIASNAME);
    }

    /**
     * 从session中获取当前项目别名
     *
     * @return
     */
    @Override
    public String getCurrentProjectAliasName() {
        return (String)session.getAttribute(CommonConstant.PROJECT_ALIASNAME);
    }

    /**
     * 从session中获取当前项目id
     *
     * @return
     */
    @Override
    public String getCurrentProjectId() {
        return (String)session.getAttribute(CommonConstant.PROJECTID);
    }

    /**
     * 从session中获取当前用户集群列表
     *
     * @return
     */
    @Override
    public Map<String, Cluster> getCurrentUserCluster() throws Exception{
        final Integer currentRoleId = this.getCurrentRoleId();
        //设置集群信息
        List<Cluster> clusterList = this.roleLocalService.getClusterListByRoleId(currentRoleId);
        Map<String, Cluster> clusterMap = clusterList.stream().collect(Collectors.toMap(Cluster::getId, cluster -> cluster));
        return clusterMap;
    }

    /**
     * 从session中获取当前用户数据权限列表
     *
     * @return
     */
    @Override
    public List<LocalRolePrivilege> getCurrentUserLocalPrivilegeList() {
        return (List<LocalRolePrivilege>)session.getAttribute(CommonConstant.SESSION_DATA_PRIVILEGE_LIST);
    }


    /**
     * 从session中获取当前数据权限列表
     *
     * @return
     */
    @Override
    public List<LocalRolePrivilege> getCurrentLocalPrivilegeList() {
        return (List<LocalRolePrivilege>)session.getAttribute(CommonConstant.SESSION_DATA_PRIVILEGE_LIST);
    }

    /**
     * 从session中当前角色id
     *
     * @return
     */
    @Override
    public Integer getCurrentRoleId() {
        return (Integer)session.getAttribute(CommonConstant.ROLEID);
    }

    @Override
    public boolean checkCurrentUserIsAdminOrTm() {
        int roleId = this.getCurrentRoleId().intValue();
        if (CommonConstant.ADMIN_ROLEID == roleId || CommonConstant.TM_ROLEID == roleId) {
            return true;
        }
        return false;
    }

    @Override
    public boolean checkCurrentUserIsAdmin() {
        int roleId = this.getCurrentRoleId().intValue();
        if (CommonConstant.ADMIN_ROLEID == roleId) {
            return true;
        }
        return false;
    }

    @Override
    public Map getcurrentUser(HttpServletRequest request, HttpServletResponse response) throws Exception{
        Map<String, Object> res = new HashMap<String, Object>();
        if(SsoClient.isOpen()) {
            //同步用户信息至容器云平台数据库
            User user = syncUser(request);
            if (null == user) {
                SsoClient.setRedirectResponse(response);
                session.invalidate();
                SsoClient.clearToken(response);
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USER_NOT_AUTH_OR_TIMEOUT);
            }
            //用户信息写入session
            request.getSession().setAttribute("userId", user.getId());
            request.getSession().setAttribute("username", user.getUsername());
            request.getSession().setAttribute("isAdmin", user.getIsAdmin());
            String token = SSOUtil.getCookieValue(request, SSOConstants.SSO_TOKEN);
            request.getSession().setAttribute(SSOConstants.SSO_TOKEN, token);

            if (CommonConstant.PAUSE.equals(user.getPause())) {
                SsoClient.setRedirectResponse(response);
                session.invalidate();
                SsoClient.clearToken(response);
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USER_DISABLED);
            }

            //获取用户的租户信息
            List<TenantDto> tenantDtos = tenantService.tenantList();
            if (CommonConstant.IS_NOT_ADMIN == user.getIsAdmin() && org.apache.commons.collections.CollectionUtils.isEmpty(tenantDtos)) {
                SsoClient.setRedirectResponse(response);
                session.invalidate();
                SsoClient.clearToken(response);
                throw new MarsRuntimeException(ErrorCodeMessage.USER_NOT_AUTH);
            }
            List<Role> roleList = null;
            if (!CollectionUtils.isEmpty(tenantDtos) && CommonConstant.IS_NOT_ADMIN == user.getIsAdmin()){
                roleList = this.roleLocalService.getRoleListByUsername(user.getUsername());
//                List<Role> availableRoleList = roleList.stream().filter(role -> role.getAvailable()).collect(Collectors.toList());
                if (roleList.size() <= 0){
                    SsoClient.dealHeader(session);
                    throw new MarsRuntimeException(ErrorCodeMessage.ROLE_DISABLE);
                }
                Map<String,Object> map = new HashMap<>();
                if (!CollectionUtils.isEmpty(roleList)){
                    Map<String, TenantDto> collect = tenantDtos.stream().collect(Collectors.toMap(TenantDto::getTenantId, tenantDto -> tenantDto));
                    tenantDtos.clear();
                    for (Role role:roleList) {
                        List<UserRoleRelationship> userRoleRelationshipList = this.userRoleRelationshipService.getUserRoleRelationshipList(user.getUsername(), role.getId());
                        for (UserRoleRelationship userRoleRelationship : userRoleRelationshipList) {
                            Object object = map.get(userRoleRelationship.getTenantId());
                            if (Objects.isNull(object)){
                                TenantDto tenantDto = collect.get(userRoleRelationship.getTenantId());
                                if (!Objects.isNull(tenantDto)){
                                    tenantDtos.add(tenantDto);
                                    map.put(userRoleRelationship.getTenantId(),tenantDto);
                                }
                            }
                        }
                    }
                }
                if (CollectionUtils.isEmpty(roleList)){
                    //被禁用后该用户在该租户下项目下可用角色为空，处理被禁用的角色
                    session.setAttribute("roleStatus",Boolean.FALSE);
                }else {
                    session.setAttribute("roleStatus",Boolean.TRUE);
                }
            }
            if (CommonConstant.IS_ADMIN == user.getIsAdmin() && roleList == null){
                roleList = new ArrayList<>();
                Role role = roleLocalService.getRoleById(CommonConstant.ADMIN_ROLEID);
                roleList.add(role);
                res.put("roleList", roleList);
                if (!org.springframework.util.CollectionUtils.isEmpty(roleList)){
                    res.put("role", roleList.get(0));
                }
            }
            //返回用户信息
            res.put("userId", user.getId());
            res.put("username", user.getUsername());
            res.put("realName", user.getRealName());
            res.put("isAdmin", CommonConstant.IS_ADMIN == user.getIsAdmin());
            res.put("tenants", tenantDtos);
        }else{
            Object user = session.getAttribute("username");
            if (user == null) {
                throw new K8sAuthException(com.harmonycloud.k8s.constant.Constant.HTTP_401);
            }
            String userName = user.toString();
            User u = this.userMapper.findByUsername(userName);
            String userId = session.getAttribute("userId").toString();
            res.put("username", userName);
            res.put("userId", userId);
            res.put("realName", u.getRealName());
            List<TenantDto> tenantDtos = tenantService.tenantList();
            if (org.springframework.util.CollectionUtils.isEmpty(tenantDtos)){
                List<Role> roleList = this.roleLocalService.getRoleListByUsernameAndTenantIdAndProjectId(userName, null, null);
                res.put("roleList", roleList);
                if (!org.springframework.util.CollectionUtils.isEmpty(roleList)){
                    res.put("role", roleList.get(0));
                }
            }

            res.put("tenants", tenantDtos);

        }
        return res;
    }

    /**
     * 生成随机密码
     */
    public String generatePassWord() {
        String newPass = new String();
        String Base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        //System.out.println(Base.length());
        Random random = new Random();
        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{7,12}$";
        boolean matche = false;
        while (!matche) {
            newPass = "";
            for (int i = 0; i < 7; i++) {
                int number = random.nextInt(Base.length());
                newPass += Base.charAt(number);
                matche = newPass.matches(regex);
            }
        }
        return newPass;
    }

    /**
     * 向用户发送提示邮箱
     */
    public ActionReturnUtil sendResetPwdEmail(String userName, String newPassWord) throws Exception {
        User userEmail = userMapper.findByUsername(userName);
        String email = userEmail.getEmail();

        MimeMessage mimeMessage = MailUtil.getJavaMailSender().createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("k8sdev@harmonycloud.cn");
            helper.setTo(email);
            helper.setSubject("密码重置通知");
            Map dataModel = new HashMap<>();
            Date date = new Date();
            dataModel.put("time", date);
            dataModel.put("userName", userName);
            dataModel.put("newPassWord", newPassWord);

            //设置图标
            ClassLoader classLoader = MailUtil.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("icon-info.png");
            byte[] bytes = MailUtil.stream2byte(inputStream);
            helper.addInline("icon-info", new ByteArrayResource(bytes), "image/png");
            inputStream = classLoader.getResourceAsStream("icon-status.png");
            bytes = MailUtil.stream2byte(inputStream);
            helper.addInline("icon-status", new ByteArrayResource(bytes), "image/png");
            helper.setText(TemplateUtil.generate("passWordRest.ftl", dataModel), true);
        } catch (Exception e) {
            throw e;
        }
        try {
            MailUtil.sendMimeMessage(mimeMessage);
            return ActionReturnUtil.returnSuccessWithMsg("邮件通知发送成功！");
        } catch (Exception e) {
            return ActionReturnUtil.returnSuccessWithMsg("邮件发送失败！");
        }
    }

    /**
     * 判断是否为管理员
     *
     * @param userName
     * @return
     */
    @Deprecated
    public ActionReturnUtil isSystemAdmin(String userName) {
        User user = userMapper.findByUsername(userName);
        String isAdmin = "1";
        String notAdmin = "0";
        if (user.getIsAdmin() == 1) {
            return ActionReturnUtil.returnSuccessWithMap("isSystemAdmin", isAdmin);
        } else {
            return ActionReturnUtil.returnSuccessWithMap("isSystemAdmin", notAdmin);
        }
    }

    /**
     * 判断是否为管理员
     *
     * @param userName
     * @return
     */
    public boolean isAdmin(String userName) {
        User user = userMapper.findByUsername(userName);
        if (Objects.isNull(user)){
            throw new MarsRuntimeException(ErrorCodeMessage.USER_NOT_EXIST,userName,true);
        }
        if (user.getIsAdmin() == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 新增用户,保存用户信息到数据库，将用户加入项目时，再创建harbor用户
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
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.PASSWORD_FORMAT_ERROR);
        }
        // 用户名非重
        if (this.checkUserName(user.getUsername())) {
            return ActionReturnUtil.returnErrorWithData(DictEnum.USERNAME.phrase(),ErrorCodeMessage.EXIST);
        }
        // 邮箱非重
        if (this.checkEmail(user.getEmail())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.USER_EMAIL_DUPLICATE);
        }
        // 真实用户名判断，过滤特殊符号
        boolean matchrealname = user.getRealName().matches(regex1);
        if (!matchrealname) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.USER_REAL_NAME_ERROR);
        }
        // 密码md5加密
        String MD5password = StringUtil.convertToMD5(user.getPassword());
        user.setPassword(MD5password);
        user.setCreateTime(new Date());
        user.setPause(CommonConstant.NORMAL);
        if (user.getIsAdmin() == null) {
            user.setIsAdmin(Constant.NON_ADMIN_ACCOUNT);
        }
        if (user.getIsMachine() == null) {
            user.setIsMachine(Constant.NON_MACHINE_ACCOUNT);
        }
        userMapper.insert(user);
        return ActionReturnUtil.returnSuccess();

    }

    /**
     * 修改k8s中电话号码
     *
     * @param phone
     * @param userName
     * @return
     */
    public ActionReturnUtil changePhone(String userName, String phone) throws Exception {
        if (StringUtils.isBlank(userName)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.USERNAME_BLANK);
        }
        if (StringUtils.isBlank(phone)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USERNAME_PHONE_BLANK);
        }
        Date date = new Date();// 获得系统时间.
        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        String nowTime = sdf.format(date);
        Date time = sdf.parse(nowTime);
        User user = new User();
        user.setPhone(phone);
        user.setUsername(userName);
        user.setUpdateTime(time);
        userMapper.updateUserByUsername(user);
        return ActionReturnUtil.returnSuccess();

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
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USER_REAL_NAME_ERROR);
        }
        if (StringUtils.isEmpty(userName)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USERNAME_BLANK);
        }
        if (StringUtils.isEmpty(realName)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USER_REAL_NAME_BLANK);
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
        userMapper.updateUserByUsername(user);
        // 修改harbor
        try {
            HarborUser harborUser = new HarborUser();
            harborUser.setUsername(userName);
            harborUser.setRealName(realName);
            harborUserService.updateUserByName(harborUser);
            return ActionReturnUtil.returnSuccess();
        } catch (MarsRuntimeException e) {
            //如果更新用户授权的所有harbor全部失败，则回滚
            if (ErrorCodeMessage.USER_HARBOR_UPDATE_FAIL.name().equalsIgnoreCase(e.getErrorName())) {
                user.setRealName(oldRealName);
                userMapper.updateUserByUsername(user);
            }
            throw e;
        }
    }

    /**
     * 修改k8s和harbor中的邮箱地址
     *
     * @param email
     * @param userName
     * @return
     */
    public ActionReturnUtil changeEmail(String userName, String email) throws Exception {
        String regex = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
        boolean matches = email.matches(regex);
        if (!matches) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USER_EMAIL_FORMAT_ERROR);
        }
        if (StringUtils.isEmpty(userName)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USERNAME_BLANK);
        }
        if (StringUtils.isEmpty(email)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USER_EMAIL_BLANK);
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
        userMapper.updateUserByUsername(user);
        // 修改harbor
        try {
            HarborUser harborUser = new HarborUser();
            harborUser.setUsername(userName);
            harborUser.setEmail(email);
            harborUserService.updateUserByName(harborUser);
            return ActionReturnUtil.returnSuccess();
        } catch (MarsRuntimeException e) {
            //如果更新用户授权的所有harbor全部失败，则回滚
            if (ErrorCodeMessage.USER_HARBOR_UPDATE_FAIL.name().equalsIgnoreCase(e.getErrorName())) {
                user.setEmail(oldEmail);
                userMapper.updateUserByUsername(user);
            }
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
        AssertUtil.notBlank(newPassword, DictEnum.PASSWORD_NEW);

        if (newPassword.equals(oldPassword)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PASSWORD_NEW_END_EQ);
        }
        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{7,12}$";
        boolean matches = newPassword.matches(regex);
        if (!matches) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PASSWORD_FORMAT_ERROR);
        }
        // 判断旧密码的正确性
        String MD5oldPassword = StringUtil.convertToMD5(oldPassword);
        User userDb = userMapper.findByUsername(userName);
        if (!userDb.getPassword().equals(MD5oldPassword)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PASSWORD_OLD_ERROR);
        }
        // 更新k8s用户密码
        String MD5newPassword = StringUtil.convertToMD5(newPassword);
        userMapper.updatePassword(userName, MD5newPassword);
        //更新harbor用户密码
        harborUserService.updatePassword(userName, oldPassword, newPassword);
        return ActionReturnUtil.returnSuccess();

    }

    /**
     * 重置用户密码
     *
     * @param userName
     * @return
     */
    public ActionReturnUtil resetUserPwd(String userName) throws Exception {
        AssertUtil.notBlank(userName, DictEnum.USERNAME);
        String newPassWord = generatePassWord();
        // 更新k8s用户密码
        String MD5newPassword = StringUtil.convertToMD5(newPassWord);
        userMapper.updatePassword(userName, MD5newPassword);
        sendResetPwdEmail(userName, newPassWord);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 刪除用户
     *
     * @param userName
     * @return
     */
    @Transactional
    public ActionReturnUtil deleteUser(String userName) throws Exception {
        AssertUtil.notBlank(userName, DictEnum.USERNAME);
        User userDb = userMapper.findByUsername(userName);
        if (!Objects.isNull(userDb)){
            UserGroupRelationExample example =new UserGroupRelationExample();
            example.createCriteria().andUseridEqualTo(userDb.getId());
            usergrouprelationMapper.deleteByExample(example);//删除用户组关联关系 user_group_relation
            userRoleRelationshipService.deleteByUserName(userName);//删除user_role_relationship表中关联数据
            userMapper.deleteUserByName(userName);
        }else {
            throw new MarsRuntimeException(ErrorCodeMessage.USER_NOT_EXIST);
        }
        return ActionReturnUtil.returnSuccess();
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
            if (!CommonConstant.NORMAL.equals(status) && user.getIsAdmin() != 0){
                throw new MarsRuntimeException(ErrorCodeMessage.ADMIN_NOT_ENBALE);
            }else {
                user.setPause(status);
                user.setId(user.getId());
                user.setUpdateTime(new Date());
                userMapperNew.updateByPrimaryKeySelective(user);
                //更新redis中用户的状态
                if (CommonConstant.NORMAL.equals(status)){
                    Boolean userStatus = clusterCacheManager.getUserStatus(username);
                    if (userStatus){
                        clusterCacheManager.updateUserStatus(username,Boolean.FALSE);
                    }
                }else {
                    clusterCacheManager.updateUserStatus(username,Boolean.TRUE);
                }
                return user;
            }
        }
        return null;
    }

    /**
     * 更改用户isadmin状态
     *
     * @param username
     * @param isadmin
     * @return
     * @throws Exception
     */
    public User updateUserToAdmin(String username, Integer isadmin) throws Exception {
        if (username != null) {
            User user = this.getUser(username);
            if (user == null) {
                throw new MarsRuntimeException(ErrorCodeMessage.NOT_FOUND, DictEnum.USERNAME.phrase()+username,true);
            }
            if (user.getIsAdmin() == isadmin) {
                throw new MarsRuntimeException(ErrorCodeMessage.USER_STATUS_CHANGED);
            }
            user.setIsAdmin(isadmin);
            user.setId(user.getId());
            user.setUpdateTime(new Date());
            userMapperNew.updateByPrimaryKeySelective(user);
            if (0 == isadmin) {
                clusterCacheManager.updateRolePrivilegeStatus(CommonConstant.ADMIN_ROLEID,username,Boolean.TRUE);
            } else {
                clusterCacheManager.updateRolePrivilegeStatus(CommonConstant.ADMIN_ROLEID,username,Boolean.FALSE);
            }
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
        example.createCriteria().andTokenCreateBetween(leftDate, date).andTokenCreateIsNotNull().andIsAdminEqualTo(Boolean.FALSE).andIsMachineEqualTo(Boolean.FALSE);
        List<User> activeList = this.userMapperNew.selectByExample(example);
        List<User> search_users_groupname = this.searchUsersGroupname(department);
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
        example.createCriteria().andTokenCreateIsNull().andIsAdminEqualTo(Boolean.FALSE).andIsMachineEqualTo(Boolean.FALSE);
        List<User> unActiveList = this.userMapperNew.selectByExample(example);
        UserExample example1 = new UserExample();
        example1.createCriteria().andTokenCreateNotBetween(leftDate, date).andTokenCreateIsNotNull().andIsAdminEqualTo(Boolean.FALSE).andIsMachineEqualTo(Boolean.FALSE);
        List<User> normalList1 = this.userMapperNew.selectByExample(example1);
        if (normalList1 != null && normalList1.size() > 0) {
            for (User user : normalList1) {
                unActiveList.add(user);
            }
        }
        List<User> search_users_groupname = this.searchUsersGroupname(department);
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
        List<User> search_users_groupname = this.searchUsersGroupname(department);
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
//        //添加组名信息
//        for (int i = 0; i < pausedList.size(); i++) {
//            User u = pausedList.get(i);
//            u.setGroupName(userMapper.selectGroupNameByUserID(u.getId()));
//        }
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
        for (int i = 0; i < normalList.size(); i++) {
            User user = normalList.get(i);
//            normalList.get(i).setGroupName(userMapper.selectGroupNameByUserID(user.getId()));
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
        example.createCriteria().andTokenCreateBetween(leftDate, date).andTokenCreateIsNotNull().andIsAdminEqualTo(Boolean.FALSE).andIsMachineEqualTo(Boolean.FALSE);
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
        example.createCriteria().andTokenCreateIsNull().andIsAdminEqualTo(Boolean.FALSE).andIsMachineEqualTo(Boolean.FALSE);
        List<User> normalList = this.userMapperNew.selectByExample(example);
        UserExample example1 = new UserExample();
        example1.createCriteria().andTokenCreateNotBetween(leftDate, date).andTokenCreateIsNotNull().andIsAdminEqualTo(Boolean.FALSE).andIsMachineEqualTo(Boolean.FALSE);
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
        for (int i = 0; i < unauthorizedUserList.size(); i++) {
            User user = unauthorizedUserList.get(i);
//            user.setGroupName(userMapper.selectGroupNameByUserID(user.getId()));
            user.setIsAuthorize(0);
        }
        return unauthorizedUserList;
    }

    public List<User> getAdminUserList() throws Exception {
        UserExample example = new UserExample();
        example.createCriteria().andIsAdminEqualTo(Boolean.TRUE);
        List<User> adminUserList = userMapperNew.selectByExample(example);
//        for (int i = 0; i < adminUserList.size(); i++) {
//            User u = adminUserList.get(i);
//            u.setGroupName(userMapper.selectGroupNameByUserID(u.getId()));
//        }
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
        su.setUserSum(allUserNormalList.size() + allUserPausedList.size() + adminUserList.size() + unauthorizedUserList.size());
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
    public ActionReturnUtil listUsers(Boolean isAdmin, Boolean isMachine, Boolean isCommon, Boolean all) throws Exception {

        // 查询k8s用户
        List<UserShowDto> userNameList = new ArrayList<UserShowDto>();
        List<User> users = null ;
        if(all != null && all){
            users = userMapper.listAllUsers();
        }else{
            users = userMapper.listUser(isAdmin, isMachine, isCommon);
        }

        for (User user : users) {
            UserGroupRelationExample ugr = new UserGroupRelationExample();
            ugr.createCriteria().andUseridEqualTo(user.getId());
            UserShowDto u = new UserShowDto();
//            List<UserGroupRelation> userGroupRelations = usergrouprelationMapper.selectByExample(ugr);
//
//            if (!CollectionUtils.isEmpty(userGroupRelations)) {
//                int groupid = userGroupRelations.get(0).getGroupid();
//                String groupname = usergroupMapper.selectByPrimaryKey(groupid).getGroupname();
//                u.setGroupName(groupname);
//            }
            u.setId(user.getId());
            u.setIsAdmin(user.getIsAdmin() == FLAG_TRUE);
            u.setIsMachine(user.getIsMachine() == FLAG_TRUE);
            u.setName(user.getUsername());
            u.setNikeName(user.getReal_name());
            u.setRealName(user.getRealName());
            u.setEmail(user.getEmail());
            u.setComment(user.getComment());
            u.setPause(user.getPause());
            u.setPhone(user.getPhone());
            User user1 = userMapper.findAthorizeByUsername(user.getUsername());
            if (user1 != null) {
                u.setIsAuthorize(1);
            } else {
                u.setIsAuthorize(0);
            }

            Date createTime = user.getCreateTime();
            String date = DateUtil.DateToString(createTime, DateStyle.YYYY_MM_DD_HH_MM_SS);
            u.setCreateTime(date);
            if (user.getUpdate_time() == null) {
                u.setUpdateTime("");
            } else {
                Date updateTime = user.getUpdate_time();
                u.setUpdateTime(DateUtil.DateToString(updateTime, DateStyle.YYYY_MM_DD_HH_MM_SS));
            }
            userNameList.add(u);

        }
        return ActionReturnUtil.returnSuccessWithDataAndCount(userNameList, userNameList.size());
    }

    /**
     * 用户列表
     *
     * @throws Exception
     */
    public ActionReturnUtil listCommonUsers() throws Exception {
        // 查询k8s用户
        List<UserShowDto> userNameList = new ArrayList<UserShowDto>();
        List<User> users = userMapper.listAllUsers();
        for (User user : users) {
            if (Constant.ADMIN_ACCOUNT == user.getIsAdmin()) {
                continue;
            }
            UserGroupRelationExample ugr = new UserGroupRelationExample();
            ugr.createCriteria().andUseridEqualTo(user.getId());
            UserShowDto u = new UserShowDto();
//            List<UserGroupRelation> userGroupRelations = usergrouprelationMapper.selectByExample(ugr);
//            if (!CollectionUtils.isEmpty(userGroupRelations)) {
//                int groupid = userGroupRelations.get(0).getGroupid();
//                String groupname = usergroupMapper.selectByPrimaryKey(groupid).getGroupname();
//                u.setGroupName(groupname);
//            }
            u.setIsAdmin(user.getIsAdmin() == 1);
            u.setName(user.getUsername());
            u.setNikeName(user.getRealName());
            u.setEmail(user.getEmail());
            u.setComment(user.getComment());
            u.setPause(user.getPause());
            u.setPhone(user.getPhone());
            User user1 = userMapper.findAthorizeByUsername(user.getUsername());
            if (user1 != null) {
                u.setIsAuthorize(Constant.USER_AUTHORIZED);
            } else {
                u.setIsAuthorize(Constant.USER_NOT_AUTHORIZE);
            }

            Date createTime = user.getCreateTime();
            String date = DateUtil.DateToString(createTime, DateStyle.YYYY_MM_DD_HH_MM_SS);
            u.setCreateTime(date);
            if (user.getUpdateTime() == null) {
                u.setUpdateTime("");
            } else {
                Date updateTime = user.getUpdateTime();
                u.setUpdateTime(DateUtil.DateToString(updateTime, DateStyle.YYYY_MM_DD_HH_MM_SS));
            }
            userNameList.add(u);

        }
        return ActionReturnUtil.returnSuccessWithDataAndCount(userNameList, userNameList.size());
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
            harborUserService.updatePassword(userName, ldapUser.getPassword(), password);
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
     * @param
     * @return ActionReturnUtil
     */
    public ActionReturnUtil createGroup(UserGroup usergroup) throws Exception {
        AssertUtil.notNull(usergroup, DictEnum.USER_GROUP);
        AssertUtil.notNull(usergroup.getGroupname(), DictEnum.NAME);
        UserGroup groupName = this.getGroupByGroupName(usergroup.getGroupname());
        if (!Objects.isNull(groupName)){
            throw new MarsRuntimeException(ErrorCodeMessage.USER_GROUP_EXIST);
        }
        // 在user_group表中增加数据
        usergroupMapper.insert(usergroup);
        UserGroupExample ugexample = new UserGroupExample();
        ugexample.createCriteria().andGroupnameEqualTo(usergroup.getGroupname());
        // 获取用户群组id，群组名唯一
        int groupid = usergroupMapper.selectByExample(ugexample).get(0).getId();
        // 在user_group_relation表中增加数据
        List<User> ls = usergroup.getUsers();
        if (!CollectionUtils.isEmpty(ls)){
            List<UserGroupRelation> ints = new ArrayList<UserGroupRelation>();
            for (int i = 0; i < ls.size(); i++) {
                String username = ls.get(i).getUsername();
                UserExample example = new UserExample();
                example.createCriteria().andUsernameEqualTo(username);
                UserGroupRelation ugr = new UserGroupRelation();
                ugr.setGroupid(groupid);
                // 用户名唯一
                ugr.setUserid(userMapperNew.selectByExample(example).get(0).getId());
                ints.add(ugr);
            }
            // 在user_ group_relation表中增加数据
            usergrouprelationMapper.addUserGroupRelation(ints);
        }
        return ActionReturnUtil.returnSuccess();
    }
    public UserGroupRelation getGroup(Long userId,Integer groupId) throws Exception {
        UserGroupRelationExample example = new UserGroupRelationExample();
        example.createCriteria().andUseridEqualTo(userId).andGroupidEqualTo(groupId);
        List<UserGroupRelation> userGroupRelations = usergrouprelationMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(userGroupRelations)){
            return userGroupRelations.get(0);
        }
        return null;
    }
    /**
     * 删除用户群组
     *
     * @param groupnames
     * @return ActionReturnUtil
     */
    public ActionReturnUtil delete_group(List<String> groupnames) throws Exception {
        // 删除user_group表中的相关数据同时删除user_group_relation表中的关联数据
        try {
            UserGroupExample delexample = new UserGroupExample();
            UserGroupExample.Criteria criteria = delexample.createCriteria();
            criteria.andGroupnameIn(groupnames);
            usergroupMapper.deleteByExample(delexample);
            return ActionReturnUtil.returnSuccess();
        } catch (Exception e) {
            return ActionReturnUtil.returnErrorWithData(e);
        }
    }

    /**
     * 根据组id删除组内成员和组
     * @param groupid
     * @return
     * @throws Exception
     */
    public ActionReturnUtil deleteGroupbyId(int groupid) throws Exception {
        // 删除user_group表中的相关数据同时删除user_group_relation表中的关联数据
        try {
            UserGroup userGroup = usergroupMapper.selectByPrimaryKey(groupid);
            if (Objects.isNull(userGroup)){
                throw new MarsRuntimeException(ErrorCodeMessage.USER_GROUP_NOT_EXIST);
            }
            String groupname = userGroup.getGroupname();
            TenantBinding tenantBinding = this.tenantService.getTenantBytenantName(groupname);
            if (!Objects.isNull(tenantBinding)){
                throw new MarsRuntimeException(ErrorCodeMessage.USER_GROUP_BIND_TENANT);
            }
            usergroupMapper.deleteByPrimaryKey(userGroup.getId());
            UserGroupRelationExample ugrexample = new UserGroupRelationExample();
            ugrexample.createCriteria().andGroupidEqualTo(groupid);
            usergrouprelationMapper.deleteByExample(ugrexample);
            return ActionReturnUtil.returnSuccess();
        } catch (Exception e) {
            String errorMessage = null;
            if (e instanceof MarsRuntimeException){
                errorMessage = ((MarsRuntimeException) e).getErrorMessage();
            }else {
                errorMessage = e.getMessage();
            }
            throw new MarsRuntimeException(ErrorCodeMessage.USER_GROUP_DELETE_FAIL,errorMessage,Boolean.TRUE);
        }
    }

    /**
     * 修改用户群组信息
     *
     * @param usergroupdto
     * @return ActionReturnUtil
     */
    public ActionReturnUtil updateGroup(UserGroupDto usergroupdto) throws Exception {
        AssertUtil.notNull(usergroupdto);
        // 获取修改参数
        List<String> addusers = usergroupdto.getAddusers();
        List<String> delusers = usergroupdto.getDelusers();
        String updategroupname = usergroupdto.getUpdategroupname();
        String userGroupDescribe = usergroupdto.getUpdatedescribe();
        Integer groupId = usergroupdto.getUsergroup().getId();
        //获取用户组
        UserGroup groupByGroup = this.getGroupByGroupId(groupId);
        String oldGroupname = groupByGroup.getGroupname();
        String oldUserGroupDescribe = groupByGroup.getUserGroupDescribe();
        if (Objects.isNull(groupByGroup)){
            throw new MarsRuntimeException(ErrorCodeMessage.USER_GROUP_NOT_EXIST);
        }
        TenantBinding tenantBinding = this.tenantService.getTenantBytenantName(oldGroupname);
        if (StringUtils.isNotBlank(updategroupname)){
            if (Objects.isNull(tenantBinding)){
                groupByGroup.setGroupname(updategroupname);
            }
        }
        if (StringUtils.isNotBlank(userGroupDescribe)){
            groupByGroup.setUserGroupDescribe(userGroupDescribe);
        }
        if (!oldGroupname.equals(updategroupname)
                || !(Objects.isNull(oldGroupname)&&Objects.isNull(userGroupDescribe))
                || oldUserGroupDescribe.equals(userGroupDescribe)){
            if (!Objects.isNull(tenantBinding) &&!oldGroupname.equals(updategroupname)){
                throw new MarsRuntimeException(ErrorCodeMessage.USER_GROUP_BIND_TENANT);
            }
            usergroupMapper.updateByPrimaryKeySelective(groupByGroup);
        }
        if (!CollectionUtils.isEmpty(addusers)){
            // 增加群组用户
            List<UserGroupRelation> ugrs = new ArrayList<UserGroupRelation>();
            int groupid = usergroupdto.getUsergroup().getId();
            for (String userName : addusers) {
                User user = this.getUser(userName);
                if(!Objects.isNull(user)){
                    Long userId = user.getId();
                    UserGroupRelation group = this.getGroup(userId, groupid);
                    if (Objects.isNull(group)){
                        UserGroupRelation ugr = new UserGroupRelation();
                        ugr.setGroupid(groupid);
                        ugr.setUserid(userId);
                        ugrs.add(ugr);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(ugrs)){
                usergrouprelationMapper.addUserGroupRelation(ugrs);
            }
        }
        if (!CollectionUtils.isEmpty(delusers)){
            // 删除群组用户
            int groupid = usergroupdto.getUsergroup().getId();
            for (String userName : delusers) {
                User user = this.getUser(userName);
                Long userId = user.getId();
                UserGroupRelation group = this.getGroup(userId, groupid);
                if (!Objects.isNull(group)){
                    if (!Objects.isNull(tenantBinding)){
                        String tenantId = tenantBinding.getTenantId();
                        List<UserRoleRelationship> userRoles = this.userRoleRelationshipService.
                                getUserRoleRelationshipList(userName, tenantId);
                        if (!CollectionUtils.isEmpty(userRoles)){
                            throw new MarsRuntimeException(ErrorCodeMessage.USER_BIND_TENANT,userName,Boolean.TRUE);
                        }
                    }
                    UserGroupRelationExample ugrexample = new UserGroupRelationExample();
                    ugrexample.createCriteria().andGroupidEqualTo(groupid).andUseridEqualTo(userId);
                    usergrouprelationMapper.deleteByExample(ugrexample);
                }
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
     * 根据组名获取用户组
     *
     * @param groupName
     * @return
     * @throws Exception
     */
    @Override
    public UserGroup getGroupByGroupName(String groupName) throws Exception {
        UserGroupExample example = new UserGroupExample();
        example.createCriteria().andGroupnameEqualTo(groupName);
        // 群组名唯一，所以只有一个
        List<UserGroup> ug = usergroupMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(ug)){
            return null;
        }
        return ug.get(0);
    }
    public UserGroup getGroupByGroupId(Integer groupId) throws Exception {
        UserGroup userGroup = usergroupMapper.selectByPrimaryKey(groupId);
        return userGroup;
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
            uexample.createCriteria().andIdEqualTo(userid.intValue());
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

    public ActionReturnUtil searchGroupByUsername(String username) throws Exception {
        AssertUtil.notBlank(username, DictEnum.USERNAME);
        //根据传入的username模糊查询用户，获取list<uuid>
        List<User> users = userMapperNew.selectLikeUsername(username);
        //判断是否存在user用户
        if (users.size() == 0 || null == users) {
            return ActionReturnUtil.returnSuccessWithData(null);
        }
        //定义反馈的群组信息
        List<UserGroup> ls = new ArrayList<UserGroup>();
        boolean flag;
        //遍历List<user>
        for (int i = 0; i < users.size(); i++) {
            flag = false;
            //根据传入的user获取所属群组信息
            Long uuid = users.get(i).getId();
            UserGroupRelationExample ugr = new UserGroupRelationExample();
            ugr.createCriteria().andUseridEqualTo(uuid);
            List<UserGroupRelation> usergrouprelations = usergrouprelationMapper.selectByExample(ugr);
            //如果用户没有分配群组，则跳过进入下一个用户
            if (usergrouprelations.size() == 0) {
                break;
            }
            //一人只属于一群组，直接get(0)
            UserGroupRelation usergrouprelation = usergrouprelationMapper.selectByExample(ugr).get(0);
            //此时获取当前用户所属群组
            int groupid = usergrouprelation.getGroupid();
            UserGroup usergroup = usergroupMapper.selectByPrimaryKey(groupid);
            //遍历反馈的信息，如果用户群组一样，那么在当前群组里面插入到List<User>的属性中，如果没有找到，那么整个插入新增的一条数据
            for (int j = 0; j < ls.size(); j++) {
                //判断群组是否已存在list中，在则添加到当前群组的List<User>属性中，并结束内层for循环。
                if (usergroup.getGroupname() == ls.get(j).getGroupname()) {

                    ls.get(j).getUsers().add(users.get(i));
                    flag = true;
                    break;
                }
            }
            if (flag) {
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
        Long userid = userMapperNew.selectByExample(uexample).get(0).getId();
        UserGroupRelationExample ugrexample = new UserGroupRelationExample();
        ugrexample.createCriteria().andUseridEqualTo(userid);
        int groupid = usergrouprelationMapper.selectByExample(ugrexample).get(0).getGroupid();
        UserGroup usergroup = usergroupMapper.selectByPrimaryKey(groupid);
        return usergroup;
    }

    /**
     * 根据群组名获取用户详情
     *
     * @param
     * @return List<UserGroup>
     */
    public List<User> searchUsersGroupname(String groupname) throws Exception {
        List<User> list = usergrouprelationMapper.selectUserListByGroupName(groupname);
        return list;
    }

    /**
     * 群组展示用户
     *
     * @return List<UserShowDto>
     */
    public ActionReturnUtil listUserswithoutgroup() throws Exception {

        List<UserShowDto> userNameList = new ArrayList<UserShowDto>();
        // 查询平台用户列表
        List<User> users = userMapper.listAllUsers();
        for (User user : users) {
            UserShowDto u = new UserShowDto();
            u.setIsAdmin(user.getIsAdmin() == 1);
            u.setName(user.getUsername());
            u.setNikeName(user.getRealName());
            u.setEmail(user.getEmail());
            u.setComment(user.getComment());
            u.setPause(user.getPause());
            Date createTime = user.getCreateTime();
            String date = DateUtil.DateToString(createTime, DateStyle.YYYY_MM_DD_HH_MM_SS);
            u.setCreateTime(date);
            if (user.getUpdateTime() == null) {
                u.setUpdateTime("");
            } else {
                Date updateTime = user.getUpdateTime();
                u.setUpdateTime(DateUtil.DateToString(updateTime, DateStyle.YYYY_MM_DD_HH_MM_SS));
            }
            userNameList.add(u);
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
            LOGGER.error("用户信息文件导出失败", e);
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
        List<List<Object>> listob = ExcelUtil.getUserListByExcel(in, file.getOriginalFilename());
        AssertUtil.notEmpty(listob, DictEnum.FILE);
        String userRealNameRegex = "^[\u4E00-\u9FA5A-Za-z0-9]+$";
        // 遍历listob数据，把数据放到List中
        try {
            List<User> users = new ArrayList<>();
            for (int i = 0; i < listob.size(); i++) {
                List<Object> ob = listob.get(i);
                User user = new User();
                if (ob.size() <= 5 && (ob.get(0) == null || ob.get(0).toString().trim().isEmpty()) || (ob.get(1) == null || ob.get(1).toString().trim().isEmpty())
                        || (ob.get(2) == null || ob.get(2).toString().trim().isEmpty()) || (ob.get(3) == null || ob.get(3).toString().trim().isEmpty())
                        || (ob.get(4) == null || ob.get(4).toString().trim().isEmpty())) {
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.FORMAT_ERROR,
                            DictEnum.LINE.phrase()+ (i + 1), true);
                }
                if ((ob.get(0) == null || ob.get(0).toString().trim().isEmpty()) && (ob.get(1) == null || ob.get(1).toString().trim().isEmpty())
                        && (ob.get(2) == null || ob.get(2).toString().trim().isEmpty()) && (ob.get(3) == null || ob.get(3).toString().trim().isEmpty())
                        && (ob.get(4) == null || ob.get(4).toString().trim().isEmpty())) {
                    //如果是第一行则直接返回
                    if (i == 0) {
                        return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NOT_BLANK, DictEnum.FILE.phrase(),true);
                    } else {
                        // 跳过空行
                        break;
                    }
                }
                // 通过遍历获取每一列用户数据
                if (ob.get(0).toString().trim().isEmpty() || ob.get(0) == null) {
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NOT_BLANK,
                            DictEnum.LINE.phrase()+ (i + 1) + DictEnum.USERNAME.phrase(), true);
                } else if (ob.get(0) != null && !ob.get(0).toString().trim().isEmpty()) {
                    User userUsable = userMapper.findByUsername(String.valueOf(ob.get(0)));
                    if (userUsable != null) {
                        return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USERNAME_DUPLICATE,
                                DictEnum.LINE.phrase()+ (i + 1), true);
                    } else {
                        user.setUsername(String.valueOf(ob.get(0)).trim());
                    }
                }

                if (ob.get(1) == null || ob.get(1).toString().trim().isEmpty()) {
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NOT_BLANK,
                            DictEnum.LINE.phrase()+ (i + 1) + DictEnum.PASSWORD.phrase(), true);
                } else if (ob.get(1) != null && !ob.get(1).toString().trim().isEmpty()) {
                    String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{7,12}$";
                    String passWord = String.valueOf(ob.get(1)).trim();
                    boolean matches = passWord.matches(regex);
                    if (!matches) {
                        return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PASSWORD_FORMAT_ERROR,
                                DictEnum.LINE.phrase()+ (i + 1) , true);
                    } else {
                        user.setPassword(String.valueOf(ob.get(1)).trim());
                    }
                }


                if (ob.get(2) == null || ob.get(2).toString().trim().isEmpty()) {
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NOT_BLANK,
                            DictEnum.LINE.phrase()+ (i + 1)+ DictEnum.EMAIL.phrase(), true);
                } else if (ob.get(2) != null && !ob.get(2).toString().trim().isEmpty()) {
                    String regex = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
                    boolean matches = String.valueOf(ob.get(2)).trim().matches(regex);
                    if (!matches) {
                        return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.FORMAT_ERROR,
                                DictEnum.LINE.phrase()+ (i + 1)+ DictEnum.EMAIL.phrase(), true);
                    } else {
                        User userUserableEmail = userMapper.findUserByEmail(ob.get(2).toString().trim());
                        if (userUserableEmail != null) {
                            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.EXIST,
                                    DictEnum.LINE.phrase()+ (i + 1)+ DictEnum.EMAIL.phrase(), true);
                        }
                        user.setEmail(String.valueOf(ob.get(2)).trim());
                    }
                }

                if (ob.get(3) == null || ob.get(3).toString().trim().isEmpty()) {
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NOT_BLANK,
                            DictEnum.LINE.phrase()+ (i + 1)+ DictEnum.REAL_NAME.phrase(), true);
                } else {
                    // 真实用户名判断，过滤特殊符号
                    boolean matchRealname = String.valueOf(ob.get(3)).trim().matches(userRealNameRegex);
                    if (!matchRealname) {
                        return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.FORMAT_ERROR,
                                DictEnum.LINE.phrase()+ (i + 1)+ DictEnum.REAL_NAME.phrase(), true);
                    }
                    user.setRealName(String.valueOf(ob.get(3)).trim());
                }

                if (ob.get(4) == null || ob.get(4).toString().trim().isEmpty()) {
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NOT_BLANK,
                            DictEnum.LINE.phrase()+ (i + 1)+ DictEnum.PHONE.phrase(), true);
                } else if (ob.get(4) != null && !ob.get(4).toString().trim().isEmpty()) {
                    String str = String.valueOf(ob.get(4)).trim();
                    if (str.length() != 11) {
                        return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.FORMAT_ERROR,
                                DictEnum.LINE.phrase()+ (i + 1)+ DictEnum.PHONE.phrase(), true);
                    } else {
                        user.setPhone(String.valueOf(ob.get(4)).trim());
                    }

                }
                if (ob.size() > 5 && ob.get(5) != null && !ob.get(5).toString().trim().isEmpty()) {
                    user.setComment(String.valueOf(ob.get(5)).trim());
                }
                user.setPassword(StringUtil.convertToMD5(user.getPassword()));
                user.setCreateTime(new Date());
                user.setPause(CommonConstant.NORMAL);
                user.setIsAdmin(Constant.NON_ADMIN_ACCOUNT);
                user.setIsAdmin(Constant.NON_MACHINE_ACCOUNT);
                users.add(user);
            }
            if (users.size() > 0) {
                users.stream().forEach(user -> user.setIsMachine(Constant.NON_MACHINE_ACCOUNT));
                //批量插入，每次插入DB_BATCH_INSERT_COUNT条数据，计算分多少次插入数据库
                int count = users.size() % DB_BATCH_INSERT_COUNT == 0 ? users.size() / DB_BATCH_INSERT_COUNT : users.size() / DB_BATCH_INSERT_COUNT + 1;
                for (int i = 0; i < count; i++) {
                    if (i == count - 1) {
                        userMapper.batchInsert(users.subList(i * DB_BATCH_INSERT_COUNT, users.size() ));
                        continue;
                    }
                    userMapper.batchInsert(users.subList(i * DB_BATCH_INSERT_COUNT, (i + 1) * DB_BATCH_INSERT_COUNT));
                }
            }
        } catch (Exception e) {
            throw new MarsRuntimeException(e.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
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
     * 获取机器账号的token
     *
     * @return
     * @throws MarsRuntimeException
     */
    public String getMachineToken() throws MarsRuntimeException {
        List<User> machineUsers = userMapper.listMachineUsers();
        if (CollectionUtils.isEmpty(machineUsers)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_TOKEN_ERROR);
        }
        return machineUsers.get(0).getToken();
    }

    /**
     * 描述：同步用户信息
     */
    public User syncUser(HttpServletRequest request) throws Exception{
        //根据cookie中的token获取用户信息
        com.whchem.sso.client.entity.User ssoUser = SsoClient.getUserByCookie(request);
        if (null == ssoUser) {
            return null;
        }
        //查询容器云平台是否存在该用户
        User user = userMapper.findByUsername(ssoUser.getName());
        if (null == user) {
            //不存在，新增用户
            user = new User();
            user.setUsername(ssoUser.getName());
            user.setRealName(ssoUser.getDisplayName());
            user.setEmail(ssoUser.getEmail());
            user.setCreateTime(DateUtil.getCurrentUtcTime());
            user.setPause(CommonConstant.NORMAL);
            user.setIsAdmin(0);
            userMapper.insert(user);
        } else {
            //存在，更新用户信息
            User updateUser = new User();
            if (null != ssoUser.getDisplayName() && !ssoUser.getDisplayName().equals(user.getRealName())) {
                updateUser.setRealName(ssoUser.getDisplayName());
            }
            if (null != ssoUser.getEmail() && !ssoUser.getEmail().equals(user.getEmail())) {
                updateUser.setEmail(ssoUser.getEmail());
            }
            if (null != updateUser.getRealName() || null != updateUser.getEmail()) {
                updateUser.setId(user.getId());
                updateUser.setUsername(user.getUsername());
                updateUser.setUpdateTime(DateUtil.getCurrentUtcTime());
                userMapper.updateByPrimaryKeySelective(updateUser);
            }
        }
        return user;
    }


    /**
     * 不做用户名及邮箱校验（持续交互平台同步用户使用）
     *
     * @param user
     * @throws Exception
     */
    @Override
    public void insertUser(User user) throws Exception {
        if(user.getIsMachine() == null){
            user.setIsMachine(FLAG_FALSE);
        }
        if(user.getIsAdmin() == null){
            user.setIsAdmin(FLAG_FALSE);
        }
        userMapper.insert(user);
    }
    /**
     * 更新用户，不做用户名及邮箱校验 （持续交互平台同步用户使用）
     * @param user
     * @throws Exception
     */
    public void updateUser(User user) throws Exception {
        userMapper.updateByPrimaryKeySelective(user);
    }
}
