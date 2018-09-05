package com.harmonycloud.service.platform.service.harbor;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.k8s.bean.RoleBinding;
import com.harmonycloud.service.platform.bean.ProjectUserBinding;
import com.harmonycloud.service.platform.bean.TenantHarborDetail;
import com.harmonycloud.service.platform.bean.UserProjectBiding;
import com.harmonycloud.service.platform.bean.harbor.HarborRole;
import com.harmonycloud.service.platform.bean.harbor.HarborUser;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.service.platform.bean.harbor.HarborUserBinding;

import java.util.List;
import java.util.Set;

/**
 * Created by zhangkui on 2017/12/18.
 * harbor用户管理相关接口
 */
public interface HarborUserService {


    /**
     * 在集群对应的harbor创建用户
     * @param harborServer harbor服务器相关信息
     * @param user 用户信息
     * @return 是否成功
     * @throws Exception
     */
    Integer createUser(HarborServer harborServer, User user) throws Exception;

    /**
     * 登录harbor
     * @param harborServer
     * @param user
     * @return
     * @throws Exception
     */
    void harborUserLogin(HarborServer harborServer, User user) throws Exception;

    /**
     * 根据harbor用户名获取集群对应harbor的用户信息
     * @param harborServer
     * @param userName
     * @return harbor用户
     * @throws Exception
     */
    HarborUser getUserByName(HarborServer harborServer, String userName) throws Exception;

    /**
     * 根据harbor用户名更新harbor的用户信息
     * @param harborUser harbor用户信息
     * @return 是否成功
     * @throws Exception
     */
    boolean updateUserByName(HarborUser harborUser) throws Exception;

    /**
     * 根据harbor用户id更新harbor的用户信息
     * @param harborServer harbor服务器相关信息
     * @param harborUser harbor用户信息
     * @return 是否成功
     * @throws Exception
     */
    boolean updateUserById(HarborServer harborServer, HarborUser harborUser) throws Exception;

    /**
     * 更改harbor用户密码
     * @param userName
     * @param oldPassword
     * @param newPassword
     * @return
     * @throws Exception
     */
    boolean updatePassword(String userName, String oldPassword, String newPassword) throws Exception;

    /**
     * 根据harbor用户名删除harbor的用户
     * @param harborUserName harbor用户名
     * @return 是否成功
     * @throws Exception
     */
    boolean deleteUserByName(String harborUserName) throws Exception;

    /**
     * 根据harbor用户名删除harbor的用户
     * @param harborServer harbor服务器相关信息
     * @param harborUserName harbor用户名
     * @return 是否成功
     * @throws Exception
     */
    boolean deleteUserByName(HarborServer harborServer, String harborUserName) throws Exception;

    /**
     * 根据harbor用户id删除harbor的用户
     * @param harborServer harbor服务器相关信息
     * @param harborUserId harbor用户id
     * @return 是否成功
     * @throws Exception
     */
    boolean deleteUserById(HarborServer harborServer, Integer harborUserId) throws Exception;

    /**
     * 根据repositoryId获取repository下的成员列表
     *
     * @param harborProjectId harborProjectId
     * @return
     * @throws Exception
     */
    List<HarborUser> usersOfProject(String harborHost, Integer harborProjectId) throws Exception;

    /**
     * 根据username 查询出在该project的user 权限详情
     * @param harborHost
     * @param harborProjectId
     * @param username
     * @return
     * @throws Exception
     */
    List<HarborUser> usersOfProjectByUsername(String harborHost, Integer harborProjectId, String username) throws Exception;

    /**
     * 创建repository下的role
     *
     * @param harborProjectId  harborProjectId
     * @param harborRole role bean
     * @return
     * @throws Exception
     */
    ActionReturnUtil createRole(String harborHost, Integer harborProjectId, HarborRole harborRole) throws Exception;

    /**
     * 更新repository下的role
     *
     * @param harborProjectId  harborProjectId
     * @param userId     repositoryId
     * @param harborRole role bean
     * @return
     * @throws Exception
     */
    ActionReturnUtil updateRole(String harborHost, Integer harborProjectId, Integer userId, HarborRole harborRole) throws Exception;

    /**
     * 删除repository下的role
     *
     * @param repositoryId repositoryId
     * @param userId    userId
     * @return
     * @throws Exception
     */
    ActionReturnUtil deleteRole(String harborHost, Integer repositoryId, Integer userId) throws Exception;

    /**
     * 将人员加入某个项目时，同时在harbor上授权项目对应的镜像仓库权限给此用户,反之移出项目时，取消授权
     * @param projectId 项目id
     * @param isAuthorize 是否授权，还是取消授权
     * @param isPm 是否项目管理员
     * @return
     */
    ActionReturnUtil authUserHarborAccess(String username,String projectId, Boolean isAuthorize, Boolean isPm)throws  Exception;

    /**
     * 将多个镜像仓库访问权限授权给某个用户
     * @param projectUserBinding
     * @return
     * @throws Exception
     */
    public ActionReturnUtil bindingUserProjects(ProjectUserBinding projectUserBinding)throws Exception;

    /**
     * 取消某个用户对一些镜像仓库的访问权限
     * @param projectUserBinding
     * @return
     * @throws Exception
     */
    public ActionReturnUtil unBindingUserProjects(ProjectUserBinding projectUserBinding)throws Exception;

    /**
     * 授权多个用户对某个镜像仓库的访问权限
     * @param userProjectBinding
     * @return
     * @throws Exception
     */
    public ActionReturnUtil bindingProjectUsers(UserProjectBiding userProjectBinding)throws Exception;

    /**
     * 获取用户可以操作的harbor服务列表
     * @param username 用户名
     * @return 是否成功
     * @throws Exception
     */
    Set<HarborServer> getUserAvailableHarbor(String username) throws Exception;

    /**
     * 获取当前登录用户可以操作的harbor服务列表
     * @return 是否成功
     * @throws Exception
     */
    Set<HarborServer> getCurrentUserAvailableHarbor() throws Exception;


}
