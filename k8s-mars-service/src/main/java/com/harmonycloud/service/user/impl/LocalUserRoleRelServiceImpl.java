package com.harmonycloud.service.user.impl;

import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.dao.user.LocalUserRoleRelMapper;
import com.harmonycloud.dao.user.bean.LocalUserRoleRel;
import com.harmonycloud.dao.user.bean.LocalUserRoleRelExample;
import com.harmonycloud.service.user.LocalUserRoleRelService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 用户角色关系Service
 */
@Service
public class LocalUserRoleRelServiceImpl implements LocalUserRoleRelService {

    @Autowired
    private LocalUserRoleRelMapper localUserRoleRelMapper;

    /**
     * 用户是否存在局部角色
     *
     * @param projectId
     * @param userName
     * @return
     */
    public boolean hasLocalRole(String projectId, String userName){
        LocalUserRoleRelExample example = new LocalUserRoleRelExample();
        LocalUserRoleRelExample.Criteria criteria = example.createCriteria().andUserNameEqualTo(userName).andHasLocalRoleEqualTo(true);
        if (!StringUtils.isEmpty(projectId)){
            criteria.andProjectIdEqualTo(projectId);
        }
        List<LocalUserRoleRel> rels = listLocalUserRoleRels(example);
        return !CollectionUtils.isEmpty(rels);
    }

    /**
     * 创建用户局部角色关系
     *
     * @param localRoleRel
     * @return
     */
    public int insert(LocalUserRoleRel localRoleRel){
        localRoleRel.setAvailable(true);
        return localUserRoleRelMapper.insertSelective(localRoleRel);
    }

    /**
     * 创建用户局部角色关系
     *
     * @param localRoleRels
     * @return
     */
    @Override
    public int insert(List<LocalUserRoleRel> localRoleRels){
        return localUserRoleRelMapper.batchInsert(localRoleRels);
    }

    /**
     * 带条件查询用户局部角色关系
     *
     * @param locarRoleRel
     * @return
     */
    public List<LocalUserRoleRel> listLocalUserRoleRels(LocalUserRoleRelExample locarRoleRel){
        locarRoleRel.getOredCriteria().get(0).andAvailableEqualTo(true);
        return localUserRoleRelMapper.selectByExample(locarRoleRel);
    }

    /**
     * 更新用户局部角色关系
     *
     * @param locarRoleRel
     * @return
     */
    public int update(LocalUserRoleRel locarRoleRel){
        return localUserRoleRelMapper.updateByPrimaryKeySelective(locarRoleRel);
    }

    /**
     * 删除用户局部角色关系
     *
     * @param id
     * @return
     */
    public int delete(Integer id){
        return localUserRoleRelMapper.deleteByPrimaryKey(id);
    }


    @Override
    public int delete(String projectId, Integer localRoleId) {
        AssertUtil.notBlank(projectId, DictEnum.PROJECT_ID);
        AssertUtil.notNull(localRoleId, DictEnum.LOCAL_ROLE_ID);
        LocalUserRoleRelExample example = new LocalUserRoleRelExample();
        LocalUserRoleRelExample.Criteria criteria = example.createCriteria().andProjectIdEqualTo(projectId).andLocalRoleIdEqualTo(localRoleId);
        return localUserRoleRelMapper.deleteByExample(example);
    }


}
