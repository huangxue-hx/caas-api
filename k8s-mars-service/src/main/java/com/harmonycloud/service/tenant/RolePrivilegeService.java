package com.harmonycloud.service.tenant;

import java.util.List;
import java.util.Map;

import com.harmonycloud.dao.tenant.bean.RolePrivilege;

/**
 * Created by zgl on 2017/8/10.
 */
public interface RolePrivilegeService {
    /**
     * 根据parentid获取模块列表
     * @return
     * @throws Exception
     */
    public List<RolePrivilege> getModuleByParentId(Integer parentId,String roleName) throws Exception;
    /**
     * 添加rolePrivilege
     * @param rolePrivilege
     * @return
     * @throws Exception
     */
    public void addModule(RolePrivilege rolePrivilege) throws Exception;
    /**
     * 根据id删除rolePrivilege
     * @param id
     * @return
     * @throws Exception
     */
    public void deleteModuleById(Integer id) throws Exception;
    /**
     * 更新rolePrivilege
     * @param rolePrivilege
     * @return
     * @throws Exception
     */
    public void updateModule(RolePrivilege rolePrivilege) throws Exception;
    /**
     * 根据rolename获取权限
     * @param roleName
     * @throws Exception
     */
    public Map<String, Object> getPrivilegeByRole(String roleName) throws Exception;
}
