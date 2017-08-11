package com.harmonycloud.service.user;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.user.bean.Resource;

public interface ResourceService {
//    public List<Resource> findMenusByResourceIds(Set<Long> resourceIds) throws Exception;
    public boolean checkByResourceIds(Set<Long> resourceIds, Resource resource) throws Exception;
    /**
     * 获取所有api资源列表
     * @return APIResourceList
     */
    public ActionReturnUtil listAPIResource() throws Exception;
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
