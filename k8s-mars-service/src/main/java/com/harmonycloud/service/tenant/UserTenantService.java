package com.harmonycloud.service.tenant;

import java.util.List;

import com.harmonycloud.dao.tenant.bean.UserTenant;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dto.tenant.show.UserShowDto;


public interface UserTenantService {
    /**
     * 根据tenantid获取所有usertenant
     * 
     * @param tenantid
     * @return
     * @throws Exception
     */
    public List<UserTenant> getUserByTenantid(String tenantid) throws Exception;
    /**
     * 获取所有usertenant
     * 
     * @param userName
     * @return
     * @throws Exception
     */
    public List<UserTenant> getAllUser() throws Exception;
    /**
     * 根据username获取所有usertenant
     * 
     * @return
     * @throws Exception
     */
    public List<UserTenant> getUserByUserName(String userName) throws Exception;
    /**
     * 根据tenantid获取TM的usertenant
     * 
     * @param tenantid
     * @return
     * @throws Exception
     */
    public List<UserTenant> getTMByTenantid(String tenantid) throws Exception;
    /**
     * 创建tenant下面的usertenant
     * 
     * @param tenantid
     * @param username
     * @param isTm
     * @return
     * @throws Exception
     */
    public void setUserByTenantid(String tenantid, List<String> username, boolean isTm) throws Exception;
    /**
     * 根据tenantid删除usertenant
     * 
     * @param tenantid
     * @return
     * @throws Exception
     */
    public void deleteByTenantid(String tenantid) throws Exception;
    /**
     * 根据租户id查询改租户下面的用户详情列表
     * @param tenantid
     * @throws Exception
     */
    public List<UserShowDto> getUserDetailsListByTenantid(String tenantid) throws Exception;
    /**
     * 根据tenantid和username删除usertenant
     * @param tenantid
     * @param userName
     * @throws Exception
     */
    public void deleteByTenantidAndUserName(String tenantid,String userName) throws Exception;
    /**
     * 获取每个tenant的users数量
     * @return
     * @throws Exception
     */
    public List<UserTenant> getTenantCount() throws Exception;
    /**
     * 根据username获取tenant的users数量
     * @return
     * @throws Exception
     */
    public List<UserTenant> getTenantCount(String username) throws Exception;
    /**
     * 根据username,tenantid获取tenant的user
     * @param userName
     * @param tenantid
     * @return
     * @throws Exception
     */
    public UserTenant getUserByUserNameAndTenantid(String userName,String tenantid) throws Exception;
    /**
     * 根据用户名和tenantid查询role
     * 
     * @param username，tenantid
     * @return
     */
    public String findRoleByName(String username,String tenantid) throws Exception;

}
