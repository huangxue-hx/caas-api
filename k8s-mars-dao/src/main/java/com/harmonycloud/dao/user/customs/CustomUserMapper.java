package com.harmonycloud.dao.user.customs;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.harmonycloud.dao.user.bean.User;


@Repository
public interface CustomUserMapper {
	
	/**
	 * 根据用户名查找用户
	 * @param username
	 * @return
	 */
    User findByUsername(String username);
	

    /**
     * 新增用户
     * @param user
     */
	void addUser(User user);
	
	/**
	 * 根据用户名删除用户
	 * @param username
	 */
	void deleteUserByName(String username);
    
	/**
	 * 修改密码
	 * @param username
	 * @param newPassword
	 * @param oldPassword
	 */
	
	void updatePassword(@Param("username") String username,@Param("password") String password);
	
	/**
	 * 根据email查找用户
	 * @param email
	 * @return
	 */
	User findUserByEmail(String email);
	
	/**
	 * 用户列表
	 * @return
	 */
	List<User> listUsers();
	
	/**
	 * 更新用户
	 * @param user
	 */
	void updateUser(User user);
	
	/**
	 * 根据token查询用户
	 * @param token
	 * @return
	 */
	User findUserByToken(String token);
	
	/**
	 * 获取所有机器账号
	 * @return
	 */
	List<User> listMachineUsers();
	
	/**
	 * 获取所有admin
	 * @return
	 */
	List<User> listAdmin();
	/**
	 * 获取所有被pause的用户
	 * @return
	 */
	List<User> getAllUserPausedList();
	/**
	 * 获取部门被pause的用户
	 * @param departmentName
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

}