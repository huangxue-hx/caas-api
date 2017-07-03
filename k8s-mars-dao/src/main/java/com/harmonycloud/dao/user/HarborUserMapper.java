package com.harmonycloud.dao.user;

import org.apache.ibatis.annotations.Param;

import com.harmonycloud.dao.application.bean.HarborUser;

public interface HarborUserMapper {

	 int deleteByPrimaryKey(String username);
	 
		/**
		 * 根据用户名查找用户
		 * @param username
		 * @return
		 */
	 HarborUser findByUsername(String username);

	   /**
	     * 新增用户
	     * @param user
	     */
		void addUser(HarborUser harbor);
		
		/**
		 * 根据用户名删除用户
		 * @param username
		 */
		void deleteUserByName(String username);
		
		void updatePassword(@Param("username") String username,@Param("password") String password);
}
