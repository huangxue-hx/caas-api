package com.harmonycloud.service.user.impl;

import com.harmonycloud.dao.user.ResourceMenuMapper;
import com.harmonycloud.dao.user.bean.ResourceMenu;
import com.harmonycloud.dao.user.bean.ResourceMenuExample;
import com.harmonycloud.service.user.ResourceMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ResourceMenuServiceImpl implements ResourceMenuService{
    @Autowired
    private ResourceMenuMapper resourceMenuMapper;

    /**
     * 获取系统基础菜单列表
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<ResourceMenu> getResourceMenu() throws Exception {
        ResourceMenuExample example = this.getExample();
        example.createCriteria().andAvailableEqualTo(Boolean.TRUE);
        List<ResourceMenu> resourceMenus = this.resourceMenuMapper.selectByExample(example);
        return resourceMenus;
    }

    /**
     * 根据id获取资源菜单
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public ResourceMenu getResourceMenuById(Integer id) throws Exception {
        return this.resourceMenuMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据id列表获取资源菜单
     *
     * @param ids
     * @return
     * @throws Exception
     */
    @Override
    public List<ResourceMenu> getResourceMenuByIds(List<Integer> ids) throws Exception {
        ResourceMenuExample example = this.getExample();
        example.createCriteria().andAvailableEqualTo(Boolean.TRUE).andIdIn(ids);
        List<ResourceMenu> resourceMenus = this.resourceMenuMapper.selectByExample(example);
        return resourceMenus;
    }

    /**
     * 根据parentid获取资源菜单列表
     *
     * @param parentId
     * @return
     * @throws Exception
     */
    @Override
    public List<ResourceMenu> getResourceMenuListByParentId(Integer parentId) throws Exception {
        ResourceMenuExample example = this.getExample();
        example.createCriteria().andParentRmidEqualTo(parentId).andAvailableEqualTo(Boolean.TRUE);
        List<ResourceMenu> resourceMenus = this.resourceMenuMapper.selectByExample(example);
        return resourceMenus;
    }

    /**
     * 根据module获取资源菜单列表
     *
     * @param module
     * @return
     * @throws Exception
     */
    @Override
    public List<ResourceMenu> getResourceMenuListByModule(String module) throws Exception {
        ResourceMenuExample example = this.getExample();
        example.createCriteria().andModuleEqualTo(module);
        List<ResourceMenu> resourceMenus = this.resourceMenuMapper.selectByExample(example);
        return resourceMenus;
    }

    private ResourceMenuExample getExample(){
        return new ResourceMenuExample();
    }
}
