package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.bean.UserExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    int deleteByExample(UserExample example);

    int deleteByPrimaryKey(Long uuid);

    int insert(User record);

    int batchInsert(List<User> records);

    int insertSelective(User record);

    List<User> selectByExample(UserExample example);

    User selectByPrimaryKey(Long uuid);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    List<User> selectLikeUsername(String username);

    /**
     * 根据用户名查找用户
     * @param username
     * @return
     */
    User findByUsername(String username);

    /**
     * 根据email查找用户
     * @param email
     * @return
     */
    User findUserByEmail(String email);

    /**
     * 根据token查询用户
     * @param token
     * @return
     */
    User findUserByToken(String token);

    /**
     * 查询用户列表
     * @return
     */
    List<User> listAllUsers();


    /**
     * 修改密码
     * @param username
     */

    void updatePassword(@Param("username") String username, @Param("password") String password);

    /**
     * 更新用户
     * @param user
     */
    void updateUserByUsername(User user);

    /**
     * 根据用户名删除用户
     * @param username
     */
    void deleteUserByName(String username);

    /**
     * 获取所有机器账号
     * @return
     */
    List<User> listMachineUsers();

    /**
     * 获取所有普通账号
     * @return
     */
    List<User>  listCommonUsers();

    /**
     * 获取所有admin
     * @return
     */
    List<User> listAdmin();

    List<User> listUser(@Param("isAdmin")Boolean isAdmin, @Param("isMachine")Boolean isMachine,
                        @Param("isCommon")Boolean isCommon, @Param("userIds")List userIds);
    /**
     * 获取所有被pause的用户
     * @return
     */
    List<User> getAllUserPausedList();
    /**
     * 获取部门被pause的用户
     * @param department
     * @return
     */
    List<User> getUserPausedListByDepartmnet(String department);
    /**
     * 获取所有normal的用户
     * @return
     */
    List<User> getAllUserNormalList();
    /**
     * 获取部门被normal的用户
     * @param department
     * @return
     */
    List<User> getUserNormalListByDepartmnet(String department);
    /**
     * 获取一定时间段的活跃用户
     * @return
     */
    List<User> getActiveUserList(User user);
    /**
     * 获取所有未授权的用户
     * @return
     */
    List<User> getUnauthorizedUserList();

    /**
     * 获取所有租户
     * @return
     */
    List<String> listTenant();

    //根据用户ID查询用户所属GroupName
    String selectGroupNameByUserID(Long userid);

    //判断用户是否授权
    User findAthorizeByUsername(String username);

    List<User> listUserByProjectId(String projectId);
}