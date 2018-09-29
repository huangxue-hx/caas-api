package com.harmonycloud.service.user.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.user.ResourceMenuRoleMapper;
import com.harmonycloud.dao.user.bean.ResourceMenu;
import com.harmonycloud.dao.user.bean.ResourceMenuRole;
import com.harmonycloud.dao.user.bean.ResourceMenuRoleExample;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dto.user.MenuDto;
import com.harmonycloud.service.cache.ClusterCacheManager;
import com.harmonycloud.service.user.ResourceMenuRoleService;
import com.harmonycloud.service.user.ResourceMenuService;
import com.harmonycloud.service.user.RoleLocalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ResourceMenuRoleServiceImpl implements ResourceMenuRoleService{
    @Autowired
    ResourceMenuRoleMapper resourceMenuRoleMapper;
    @Autowired
    ResourceMenuService resourceMenuService;
    @Autowired
    RoleLocalService roleLocalService;
    @Autowired
    ClusterCacheManager clusterCacheManager;
    //项目管理员角色id
    public static final Integer TENANTMGR= 3;
    //项目管理员角色id
    public static final Integer MYTENANT= 4;
    //项目管理员角色id
    public static final Integer MYPROJECT= 5;

    /**
     * 根据角色id获取系统菜单列表
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<MenuDto> getResourceMenuByRoleId(Integer roleId) throws Exception {
        //有效性检查
        Role role = this.roleLocalService.getRoleById(roleId);
        if (Objects.isNull(role)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        List<MenuDto> result = new ArrayList<>();
        //角色被停用返回空菜单
        if (!role.getAvailable()){
            return result;
        }
        //获取该角色的ResourceMenuRole列表
        Map<Integer, ResourceMenuRole> resourceMenuIdsMap = this.getResourceMenuIdsByRoleId(roleId);
        Set<Integer> idSet = resourceMenuIdsMap.keySet();
        List<Integer> resourceMenuIds = idSet.stream().collect(Collectors.toList());
        //根据ids获取基本菜单列表
        List<ResourceMenu> resourceMenuList = this.resourceMenuService.getResourceMenuByIds(resourceMenuIds);
        Map<Integer, List<ResourceMenu>> collect = resourceMenuList.stream().collect(Collectors.groupingBy(ResourceMenu::getParentRmid));
        List<ResourceMenu> parentResourceMenus = collect.get(0);
        //一级菜单处理
        for (ResourceMenu resourceMenu : parentResourceMenus) {
            MenuDto menuDto = this.convertMenuDtoObject(resourceMenu,resourceMenuIdsMap);
            List<ResourceMenu> subResourceMenus = collect.get(resourceMenu.getId());
            //如果有二级菜单，二级菜单处理
            if (!CollectionUtils.isEmpty(subResourceMenus)){
                List<MenuDto> subMenu = new ArrayList<>();
                for (ResourceMenu subResourceMenu:subResourceMenus) {
                    MenuDto subMenuDto = this.convertMenuDtoObject(subResourceMenu,resourceMenuIdsMap);
                    subMenu.add(subMenuDto);
                }
                Collections.sort(subMenu);
                menuDto.setSubMenu(subMenu);
                result.add(menuDto);
            }else if (!CommonConstant.APPCENTER.equals(resourceMenu.getModule())
                    &&!CommonConstant.CICD.equals(resourceMenu.getModule())
                    &&!CommonConstant.DELIVERY.equals(resourceMenu.getModule())
                    &&!CommonConstant.LOG.equals(resourceMenu.getModule())
                    &&!CommonConstant.ALARM.equals(resourceMenu.getModule())){
                //如果是应用中心，cicd，交互中心，日志中心，告警中心模块，没有子菜单则不显示一级菜单
                result.add(menuDto);
            }
        }
        //排序
        Collections.sort(result);
        return result;
    }

    @Override
    public List<ResourceMenuRole> listResourceMenuRole(Integer roleId) throws Exception {
        ResourceMenuRoleExample example = this.getExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        List<ResourceMenuRole> resourceMenuRoles = this.resourceMenuRoleMapper.selectByExample(example);
        return resourceMenuRoles;
    }

    /**
     * 把ResourceMenu转换为MenuDto对象
     * @param resourceMenu
     * @return
     * @throws Exception
     */
    private MenuDto convertMenuDtoObject(ResourceMenu resourceMenu,Map<Integer, ResourceMenuRole> resourceMenuIdsMap) throws Exception{
        MenuDto menuDto = new MenuDto();
        Integer id = resourceMenu.getId();
        menuDto.setId(resourceMenu.getId());
        menuDto.setIconName(resourceMenu.getIconName());
        menuDto.setIsparent(resourceMenu.getIsparent());
        menuDto.setUrl(resourceMenu.getUrl());
        menuDto.setName(resourceMenu.getName());
        menuDto.setNameEn(resourceMenu.getNameEn());
        ResourceMenuRole resourceMenuRole = resourceMenuIdsMap.get(id);
        menuDto.setWeight(resourceMenuRole.getWeight());
        menuDto.setModule(resourceMenu.getModule());
        return menuDto;
    }
    /**
     * 根据角色id获取菜单id列表
     *
     * @return
     * @throws Exception
     */
    @Override
    public Map<Integer, ResourceMenuRole> getResourceMenuIdsByRoleId(Integer roleId) throws Exception {
        ResourceMenuRoleExample example = this.getExample();
        //获取该角色的所有可用的菜单可用
        example.createCriteria().andRoleIdEqualTo(roleId).andAvailableEqualTo(Boolean.TRUE);
        List<ResourceMenuRole> resourceMenuRoles = this.resourceMenuRoleMapper.selectByExample(example);
        Map<Integer, ResourceMenuRole> collect = resourceMenuRoles.stream().collect(Collectors.toMap(ResourceMenuRole::getRmid, resourceMenuRole -> resourceMenuRole));
//        List<Integer> ids = resourceMenuRoles.stream().map(ResourceMenuRole::getRmid).collect(Collectors.toList());
        return collect;
    }

    /**
     * 根据角色id删除角色菜单
     *
     * @param roleId
     * @throws Exception
     */
    @Override
    public void deleteResourceMenuRoleByRoleId(Integer roleId) throws Exception {
        ResourceMenuRoleExample example = this.getExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        this.resourceMenuRoleMapper.deleteByExample(example);
    }

    /**
     * 新增角色菜单
     *
     * @param roleId
     * @throws Exception
     */
    @Override
    public void createResourceMenuRole(Integer roleId) throws Exception {
        //获取所有的可用基础菜单项
        List<ResourceMenu> resourceMenus = resourceMenuService.getResourceMenu();
        for (ResourceMenu resourceMenu:resourceMenus) {
            //添加新角色的菜单到资源菜单角色表中
            ResourceMenuRole resourceMenuRole = new ResourceMenuRole();
            //组装参数
            resourceMenuRole.setRmid(resourceMenu.getId());
            resourceMenuRole.setCreateTime(DateUtil.getCurrentUtcTime());
            resourceMenuRole.setRoleId(roleId);
            resourceMenuRole.setWeight(resourceMenu.getWeight());
            resourceMenuRole.setAvailable(Boolean.FALSE);
            this.createResourceMenuRole(resourceMenuRole);
        }
    }

    /**
     * 新增角色菜单
     *
     * @param resourceMenuRole
     * @throws Exception
     */
    @Override
    public void createResourceMenuRoleNative(ResourceMenuRole resourceMenuRole) throws Exception {
        this.resourceMenuRoleMapper.insertSelective(resourceMenuRole);
    }

    public void createResourceMenuRole(ResourceMenuRole resourceMenuRole) throws Exception{
        ResourceMenuRole resourceMenuRoleById = this.getResourceMenuRoleById(resourceMenuRole.getId());
        if (Objects.isNull(resourceMenuRoleById)){
            this.resourceMenuRoleMapper.insertSelective(resourceMenuRole);
        }
    }

    /**
     * 更新角色菜单状态
     *
     * @param roleId
     * @param rmid
     * @param status
     * @throws Exception
     */
    @Override
    public void updateResourceMenuRole(Integer roleId, Integer rmid, Boolean status) throws Exception {
        ResourceMenuRole resourceMenuRole = this.getResourceMenuRole(roleId, rmid);
        Boolean available = resourceMenuRole.getAvailable();
        //状态不相同更新状态
        if (available^status){
            resourceMenuRole.setAvailable(status);
            this.resourceMenuRoleMapper.updateByPrimaryKeySelective(resourceMenuRole);
        }
    }

    /**
     * 根据角色id与rmid获取ResourceMenuRole
     *
     * @param roleId
     * @param rmid
     * @return
     * @throws Exception
     */
    @Override
    public ResourceMenuRole getResourceMenuRole(Integer roleId, Integer rmid) throws Exception {
        ResourceMenuRoleExample example = this.getExample();
        example.createCriteria().andRoleIdEqualTo(roleId).andRmidEqualTo(rmid);
        ResourceMenuRole resourceMenuRole = null;
        List<ResourceMenuRole> resourceMenuRoles = this.resourceMenuRoleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(resourceMenuRoles)){
            //菜单不存在创建
            resourceMenuRole = new ResourceMenuRole();
            resourceMenuRole.setRoleId(roleId);
            resourceMenuRole.setRmid(rmid);
            resourceMenuRole.setAvailable(Boolean.FALSE);
            resourceMenuRole.setWeight(rmid);
            resourceMenuRole.setCreateTime(new Date());
            resourceMenuRole.setUpdateTime(new Date());
            this.createResourceMenuRoleNative(resourceMenuRole);
        }else {
            resourceMenuRole = resourceMenuRoles.get(0);
        }
        return resourceMenuRole;
    }
    /**
     * 获取角色id下租户列表，我的租户，我的项目菜单map
     * @param roleId
     * @return
     * @throws Exception
     */
    @Override
    public Map<Integer, ResourceMenuRole> getResourceTenantMenuRole(Integer roleId) throws Exception {
        ResourceMenuRoleExample example = this.getExample();
        example.createCriteria().andRoleIdEqualTo(roleId).andRmidBetween(TENANTMGR,MYPROJECT);
        List<ResourceMenuRole> resourceMenuRoles = this.resourceMenuRoleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(resourceMenuRoles)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_MENU_NOT_EXIST);
        }
        Map<Integer, ResourceMenuRole> result = resourceMenuRoles.stream().collect(Collectors.toMap(ResourceMenuRole::getRmid, resourceMenuRole -> resourceMenuRole));
        return result;
    }

    @Override
    public ResourceMenuRole getResourceMenuRoleById(Integer id) throws Exception {
        ResourceMenuRole resourceMenuRole = this.resourceMenuRoleMapper.selectByPrimaryKey(id);
        return resourceMenuRole;
    }

    /**
     * 更新角色菜单状态
     *
     * @param resourceMenuRole
     * @throws Exception
     */
    @Override
    public void updateResourceMenuRole(ResourceMenuRole resourceMenuRole) throws Exception {
        this.resourceMenuRoleMapper.updateByPrimaryKeySelective(resourceMenuRole);
    }

    private ResourceMenuRoleExample getExample(){
        return new ResourceMenuRoleExample();
    }
}
