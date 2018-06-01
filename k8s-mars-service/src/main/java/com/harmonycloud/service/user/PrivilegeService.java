package com.harmonycloud.service.user;

import com.harmonycloud.dao.user.bean.Privilege;
import com.harmonycloud.dto.user.PrivilegeDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zgl on 2017/12/28.
 */
public interface PrivilegeService {
    /**
     * 添加默认权限
     * @param privilege
     * @throws Exception
     */
    public void createPrivilege(Privilege privilege) throws Exception;
    /**
     * 添加默认权限（面向页面）
     * @param privilegeDto
     * @throws Exception
     */
    public void createPrivilege(PrivilegeDto privilegeDto) throws Exception;
    /**
     * 修改默认权限信息 （面向页面）
     * @param privilegeDto
     * @throws Exception
     */
    public void updatePrivilege(PrivilegeDto privilegeDto) throws Exception;

    /**
     * 修改默认权限信息
     * @param privilege
     * @throws Exception
     */
    public void updatePrivilege(Privilege privilege) throws Exception;

    /**
     * 根据id删除权限
     * @param id
     * @throws Exception
     */
    public void deletePrivilegeById(Integer id) throws Exception;

    /**
     * 查询所有的基础权限
     * @return
     * @throws Exception
     */
    public List<Privilege> listAllPrivilege() throws Exception;

    /**
     * 根据id列表对应的权限列表
     * @return
     * @throws Exception
     */
    public List<Privilege> listPrivilegeByIds(ArrayList<Integer> ids) throws Exception;

    /**
     * 根据id列表和模块列表查询对应的权限列表
     * @param ids
     * @param modules
     * @return
     * @throws Exception
     */
    public List<Privilege> listPrivilegeByIds(Integer roleId,ArrayList<Integer> ids,ArrayList<String> modules) throws Exception;
    /**
     * 查询模块组列表
     * @throws Exception
     */
    public List<Privilege> listModuleGroup() throws Exception;

    /**
     * 根据模块名查询资源组列表
     * @param module
     * @throws Exception
     */
    public List<Privilege> listResourceGroupByModule(String module) throws Exception;

    /**
     * 根据资源名查询资源权限列表
     * @param resource
     * @throws Exception
     */
    public List<Privilege> listPrivilegeByResource(String resource) throws Exception;

    /**
     * 根据id查询权限
     * @param id
     * @return
     * @throws Exception
     */
    public Privilege getPrivilegeById(Integer id) throws Exception;

    /**
     * 根据模块，资源，权限操作查询权限
     * @param module
     * @param resource
     * @param privilege
     * @return
     * @throws Exception
     */
    public Privilege getPrivilegeByModuleAndResourceAndPrivilege(String module,String resource,String privilege) throws Exception;
}
