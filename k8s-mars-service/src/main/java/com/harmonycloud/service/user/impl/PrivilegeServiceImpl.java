package com.harmonycloud.service.user.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.user.PrivilegeMapper;
import com.harmonycloud.dao.user.bean.Privilege;
import com.harmonycloud.dao.user.bean.PrivilegeExample;
import com.harmonycloud.dto.user.PrivilegeDto;
import com.harmonycloud.service.user.PrivilegeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by zgl on 2017/8/10.
 */
@Service
public class PrivilegeServiceImpl implements PrivilegeService {

    @Autowired
    private PrivilegeMapper privilegeMapper;

    /**
     * 添加默认权限
     *
     * @param privilege
     * @throws Exception
     */
    @Override
    public void createPrivilege(Privilege privilege) throws Exception {
        this.privilegeMapper.insertSelective(privilege);
    }

    /**
     * 添加默认权限（面向页面）
     *
     * @param privilegeDto
     * @throws Exception
     */
    @Override
    public void createPrivilege(PrivilegeDto privilegeDto) throws Exception {
        String module = privilegeDto.getModule();
        String resource = privilegeDto.getResource();
        String privilege = privilegeDto.getPrivilege();
        //空值判断
        if (StringUtils.isAnyBlank(module,resource,privilege)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //检查添加的基础权限操作否存在
        Privilege privilegeObj = this.getPrivilegeByModuleAndResourceAndPrivilege(module, resource, privilege);
        if (!Objects.isNull(privilegeObj)){
            throw new MarsRuntimeException(ErrorCodeMessage.PRIVILEGE_EXIST);
        }
        //组装数据
        Privilege privilegeNew = new Privilege();
        privilegeNew.setModule(module);
        privilegeNew.setModuleName(privilegeDto.getModuleName());
        privilegeNew.setResource(resource);
        privilegeNew.setResourceName(privilegeDto.getResourceName());
        privilegeNew.setRemark(privilegeDto.getRemark());
        privilegeNew.setRemarkName(privilegeDto.getRemarkName());
        privilegeNew.setPrivilege(privilege);
        privilegeNew.setPrivilegeName(privilegeDto.getPrivilegeName());
        privilegeNew.setCreateTime(DateUtil.getCurrentUtcTime());
        privilegeNew.setStatus(Boolean.TRUE);
        //向数据库添加数据
        this.createPrivilege(privilegeNew);
    }

    /**
     * 修改默认权限信息状态 （面向页面）
     *
     * @param privilegeDto
     * @throws Exception
     */
    @Override
    public void updatePrivilege(PrivilegeDto privilegeDto) throws Exception {
        String module = privilegeDto.getModule();
        String resource = privilegeDto.getResource();
        String privilege = privilegeDto.getPrivilege();
        //空值判断
        if (StringUtils.isAnyBlank(module,resource,privilege)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //检查更新的基础权限操作是否存在
        Privilege privilegeObj = this.getPrivilegeByModuleAndResourceAndPrivilege(module, resource, privilege);
        if (Objects.isNull(privilegeObj)){
            throw new MarsRuntimeException(ErrorCodeMessage.PRIVILEGE_NOT_EXIST);
        }
        //设置更新状态
        privilegeObj.setStatus(privilegeDto.getStatus());
        //更新数据库
        this.updatePrivilege(privilegeObj);
    }

    /**
     * 修改默认权限信息
     *
     * @param privilege
     * @throws Exception
     */
    @Override
    public void updatePrivilege(Privilege privilege) throws Exception {
        this.privilegeMapper.updateByPrimaryKeySelective(privilege);
    }

    /**
     * 根据id删除权限
     *
     * @param id
     * @throws Exception
     */
    @Override
    public void deletePrivilegeById(Integer id) throws Exception {
        //空值判断
        if (Objects.isNull(id)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //检查要删除的是否存在，如果存在删除
        Privilege privilegeById = this.getPrivilegeById(id);
        if (!Objects.isNull(privilegeById)){
            this.privilegeMapper.deleteByPrimaryKey(id);
        }
    }

    /**
     * 查询所有的基础权限
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<Privilege> listAllPrivilege() throws Exception {
        PrivilegeExample example = this.getExample();
        example.createCriteria().andStatusEqualTo(Boolean.TRUE);
        List<Privilege> privileges = this.privilegeMapper.selectByExample(example);
        return privileges;
    }

    /**
     * 根据id列表对应的权限列表
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<Privilege> listPrivilegeByIds(ArrayList<Integer> ids) throws Exception {
        PrivilegeExample example = this.getExample();
        example.createCriteria().andStatusEqualTo(Boolean.TRUE).andIdIn(ids);
        List<Privilege> privileges = this.privilegeMapper.selectByExample(example);
        return privileges;
    }

    /**
     * 根据id列表和模块列表查询对应的权限列表
     *
     * @param ids
     * @param modules
     * @return
     * @throws Exception
     */
    @Override
    public List<Privilege> listPrivilegeByIds(Integer roleId,ArrayList<Integer> ids, ArrayList<String> modules) throws Exception {
        PrivilegeExample example = this.getExample();
        if (roleId == CommonConstant.ADMIN_ROLEID){
            example.createCriteria().andStatusEqualTo(Boolean.TRUE).andIdIn(ids);
        }else {
            example.createCriteria().andStatusEqualTo(Boolean.TRUE).andIdIn(ids).andModuleNotIn(modules);
        }
        List<Privilege> privileges = this.privilegeMapper.selectByExample(example);
        return privileges;
    }

    /**
     * 查询模块组列表
     *
     * @throws Exception
     */
    @Override
    public List<Privilege> listModuleGroup() throws Exception {
        PrivilegeExample example = this.getExample();
        example.setGroupByClause(CommonConstant.MODULE);
        List<Privilege> privileges = this.privilegeMapper.selectByExample(example);
        return privileges;
    }

    /**
     * 根据模块名查询资源组列表
     *
     * @param module
     * @throws Exception
     */
    @Override
    public List<Privilege> listResourceGroupByModule(String module) throws Exception {
        PrivilegeExample example = this.getExample();
        example.setGroupByClause(CommonConstant.RESOURCE);
        example.createCriteria().andModuleEqualTo(module);
        List<Privilege> privileges = this.privilegeMapper.selectByExample(example);
        return privileges;
    }

    /**
     * 根据资源名查询资源权限列表
     *
     * @param resource
     * @throws Exception
     */
    @Override
    public List<Privilege> listPrivilegeByResource(String resource) throws Exception {
        PrivilegeExample example = this.getExample();
        example.createCriteria().andResourceEqualTo(resource);
        List<Privilege> privileges = this.privilegeMapper.selectByExample(example);
        return privileges;
    }

    /**
     * 根据id查询权限
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public Privilege getPrivilegeById(Integer id) throws Exception {
        //空值判断
        if (Objects.isNull(id)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //数据库查询
        Privilege privilege = this.privilegeMapper.selectByPrimaryKey(id);
        return privilege;
    }

    /**
     * 根据模块，资源，权限操作查询权限
     *
     * @param module
     * @param resource
     * @param privilege
     * @return
     * @throws Exception
     */
    @Override
    public Privilege getPrivilegeByModuleAndResourceAndPrivilege(String module,
                                                                 String resource,
                                                                 String privilege) throws Exception {
        PrivilegeExample example = this.getExample();
        example.createCriteria().andModuleEqualTo(module).andResourceEqualTo(resource).andPrivilegeEqualTo(privilege);
        List<Privilege> privileges = this.privilegeMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(privileges)){
            return null;
        }
        return privileges.get(0);
    }

    private PrivilegeExample getExample(){
        return new PrivilegeExample();
    }
}
