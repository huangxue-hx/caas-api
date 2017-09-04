package com.harmonycloud.service.user;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.tenant.bean.RolePrivilege;
import com.harmonycloud.dao.user.bean.Resource;

public interface ResourceService {
//    public List<Resource> findMenusByResourceIds(Set<Long> resourceIds) throws Exception;
    public boolean checkByResourceIds(Set<Long> resourceIds, Resource resource) throws Exception;

    /**
     * 根据角色更新resource的权重
     * @param id
     * @param weight
     * @return
     * @throws Exception
     */
    public RolePrivilege updateRoleMenuPrivilegeWight(Integer id, Integer weight) throws Exception;
    /**
     * 获取所有api资源列表
     * @return APIResourceList
     */
    public ActionReturnUtil listAPIResource() throws Exception;

    /**
     * 根据id更新菜单状态
     * @param id
     * @param status
     * @throws Exception
     */
    public void updateRoleMenuResource(Integer id, Boolean status) throws Exception;
    /**
     * 获取非资源性api列表
     * @return
     */
    public ActionReturnUtil listNoneResource() throws Exception;
//    /**
//     * 获取admin角色菜单
//     * @return
//     * @throws Exception
//     */
//    public List<Map<String, Object>> listAdminMenu() throws Exception;
    /**
     * 获取角色菜单
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> listMenuByRole(String roleName) throws Exception;
}
