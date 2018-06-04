package com.harmonycloud.service.user;

import com.harmonycloud.dao.user.bean.ResourceMenu;
import java.util.List;

public interface ResourceMenuService {
    /**
     * 获取系统基础菜单列表
     * @return
     * @throws Exception
     */
    public List<ResourceMenu> getResourceMenu() throws Exception;

    /**
     * 根据id获取资源菜单
     * @param id
     * @return
     * @throws Exception
     */
    public ResourceMenu getResourceMenuById(Integer id) throws Exception;

    /**
     * 根据id列表获取资源菜单
     * @param ids
     * @return
     * @throws Exception
     */
    public List<ResourceMenu> getResourceMenuByIds(List<Integer> ids) throws Exception;

    /**
     * 根据parentid获取资源菜单列表
     * @param parentId
     * @return
     * @throws Exception
     */
    public List<ResourceMenu> getResourceMenuListByParentId(Integer parentId) throws Exception;

    /**
     * 根据module获取资源菜单列表
     * @param module
     * @return
     * @throws Exception
     */
    public List<ResourceMenu> getResourceMenuListByModule(String module) throws Exception;

}
