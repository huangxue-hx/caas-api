package com.harmonycloud.service.user;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.user.bean.LocalRolePrivilege;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.bean.UserGroup;
import com.harmonycloud.dao.user.bean.UserGroupRelation;
import com.harmonycloud.dto.user.SummaryUserInfo;
import com.harmonycloud.dto.user.UserDetailDto;
import com.harmonycloud.dto.user.UserGroupDto;
import com.harmonycloud.dto.user.UserQueryDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface UserService {

    String generatePassWord();

    ActionReturnUtil sendResetPwdEmail(String userName, String newPassword) throws Exception;

    ActionReturnUtil isSystemAdmin(String userName);

    boolean isAdmin(String userName);

    ActionReturnUtil addUser(User user) throws Exception;

    ActionReturnUtil changePhone(String userName, String phone) throws Exception;

    ActionReturnUtil changeRealName(String userName, String realName) throws Exception;

    ActionReturnUtil changeEmail(String userName, String email) throws Exception;

    ActionReturnUtil changePwd(String userName, String oldPassword, String newPassword) throws Exception;

    ActionReturnUtil resetUserPwd(String userName) throws Exception;

    ActionReturnUtil deleteUser(String userName) throws Exception;

    List<UserDetailDto> userDetail(String username) throws Exception;

    User getUser(String username) throws Exception;


    User updateUserStatus(String username, String status) throws Exception;

    User updateUserToAdmin(String username, Integer isadmin) throws Exception;

    List<User> getUserPausedListByDepartmnet(String department) throws Exception;


    List<User> getUserNormalListByDepartmnet(String department) throws Exception;

    List<User> getActiveUserListByDepartmnet(Integer domain, String department) throws Exception;

    List<User> getUnActiveUserListByDepartmnet(Integer domain, String department) throws Exception;

    List<User> getUnauthorizedUserListByDepartmnet(String department) throws Exception;

    List<User> getAllUserPausedList() throws Exception;

    List<User> getAllUserNormalList() throws Exception;

    List<User> getActiveUserList(Integer domain) throws Exception;

    List<User> getUnActiveUserList(Integer domain) throws Exception;

    List<User> getUnauthorizedUserList() throws Exception;

    List<User> getAdminUserList() throws Exception;

    SummaryUserInfo getAllSummary(Integer domain) throws Exception;

    SummaryUserInfo getSummaryByDepartmnet(Integer domain, String department) throws Exception;

    void getAuthByUser(String username) throws Exception;

    ActionReturnUtil listMachineUsers() throws Exception;

    ActionReturnUtil listAdmin() throws Exception;

    ActionReturnUtil listUsers(UserQueryDto userQueryDto) throws Exception;

    ActionReturnUtil listCommonUsers() throws Exception;

    /**
     * 创建用户组
     * @param usergroup
     * @return
     * @throws Exception
     */
    ActionReturnUtil createGroup(UserGroup usergroup) throws Exception;

    /**
     * 查询组用户
     * @param userId
     * @param groupId
     * @return
     * @throws Exception
     */
    public UserGroupRelation getGroup(Long userId, Integer groupId) throws Exception;

    ActionReturnUtil delete_group(List<String> groupnames) throws Exception;

    /**
     * 根据组id删除组内成员和组
     * @param groupid
     * @return
     * @throws Exception
     */
    ActionReturnUtil deleteGroupbyId(int groupid) throws Exception;

    ActionReturnUtil updateGroup(UserGroupDto usergroupdto) throws Exception;

    List<UserGroup> get_groups() throws Exception;

    /**
     * 根据组名获取用户组
     * @param groupName
     * @return
     * @throws Exception
     */
    UserGroup getGroupByGroupName(String groupName) throws Exception;

    boolean issame(String groupname) throws Exception;

    List<User> searchuserbygroupid(int groupid) throws Exception;

    ActionReturnUtil searchGroupByUsername(String username) throws Exception;

    UserGroup search_group_username(String username) throws Exception;

    /**
     * 根据组名获取用户组成员
     * @param groupname
     * @return
     * @throws Exception
     */
    List<User> searchUsersGroupname(String groupname) throws Exception;

    ActionReturnUtil listUserswithoutgroup() throws Exception;

    ActionReturnUtil fileexport(HttpServletRequest req, HttpServletResponse resp) throws Exception;

    ActionReturnUtil userBulkUpload(InputStream in, MultipartFile file) throws Exception;

    String getMachineToken() throws MarsRuntimeException;

    /**
     * 新增用户，不做用户名及邮箱校验 （持续交互平台同步用户使用）
     * @param user
     * @throws Exception
     */
    public void insertUser(User user) throws Exception;

    /**
     * 更新用户，不做用户名及邮箱校验 （持续交互平台同步用户使用）
     * @param user
     * @throws Exception
     */
    public void updateUser(User user) throws Exception;
    /**
     * 从session中获取当前用户名
     * @return
     */
    public String getCurrentUsername();
    /**
     * 从session中获取当前租户id
     * @return
     */
    public String getCurrentTenantId();

    /**
     * 从session中获取当前租户别名
     * @return
     */
    public String getCurrentTenantAliasName();
    /**
     * 从session中获取当前项目id
     * @return
     */
    public String getCurrentProjectId();

    /**
     * 从session中获取当前项目别名
     * @return
     */
    public String getCurrentProjectAliasName();
    /**
     * 从session中获取当前用户集群列表
     * @return
     */
    public Map<String, Cluster> getCurrentUserCluster() throws Exception;

    /**
     * 从session中获取当前用户数据权限列表
     * @return
     */
    public List<LocalRolePrivilege> getCurrentUserLocalPrivilegeList();


    /**
     * 从session中获取当前数据权限列表
     *
     * @return
     */
    public List<LocalRolePrivilege> getCurrentLocalPrivilegeList();

    /**
     * 从session中当前角色id
     * @return
     */
    public Integer getCurrentRoleId();

    /**
     * 获取当前用户是否为管理员和租户管理员
     * @return
     */
    public boolean checkCurrentUserIsAdminOrTm();

    /**
     * 获取当前用户是否为管理员
     * @return
     */
    public boolean checkCurrentUserIsAdmin();

    Map getcurrentUser(HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 根据用户名列表查询用户列表
     * @param usernameList
     * @return
     * @throws Exception
     */
    List<User> getUserByUsernameList(List<String> usernameList) throws Exception;

    /**
     * 查询项目下的用户列表
     * @param projectId
     * @return
     */
    List<User> listUserByProjectId(String projectId);
}
