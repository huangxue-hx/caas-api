package com.harmonycloud.service.dataprivilege.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.dataprivilege.DataPrivilegeGroupMemberMapper;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMember;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMemberExample;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.tenant.ProjectService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by chencheng on 18-6-20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DataPrivilegeGroupMemberServiceImpl implements DataPrivilegeGroupMemberService {

    @Autowired
    DataPrivilegeGroupMemberMapper dataPrivilegeGroupMemberMapper;

    @Autowired
    ProjectService projectService;

    private static final Integer MEMBER_TYPE_USER = 0;//用户

    private static final Integer MEMBER_TYPE_GROUP = 1;//组

    private static final Logger logger = LoggerFactory.getLogger(DataPrivilegeGroupMemberServiceImpl.class);


    /**
     * 向组中添加成员
     * @param dataPrivilegeGroupMember
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void addMemberToGroup(DataPrivilegeGroupMember dataPrivilegeGroupMember) throws Exception {

        Integer groupId = dataPrivilegeGroupMember.getGroupId();
        Integer memberId = dataPrivilegeGroupMember.getMemberId();

        if(groupId != null) {

            dataPrivilegeGroupMember.setMemberType(MEMBER_TYPE_USER);
            dataPrivilegeGroupMemberMapper.insert(dataPrivilegeGroupMember);
        }else{
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }


    }

    public void addMemberListToGroup(List<DataPrivilegeGroupMember> dataPrivilegeGroupMemberList) {
        if (CollectionUtils.isNotEmpty(dataPrivilegeGroupMemberList)) {
            dataPrivilegeGroupMemberMapper.insertList(dataPrivilegeGroupMemberList);
        }
    }

    /**
     * 删除组中的成员
     * @param dataPrivilegeGroupMember
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void delMemberFromGroup(DataPrivilegeGroupMember dataPrivilegeGroupMember) throws Exception {

        Integer groupId = dataPrivilegeGroupMember.getGroupId();
        Integer memberId = dataPrivilegeGroupMember.getMemberId();

        if(groupId != null){

            DataPrivilegeGroupMemberExample example = new DataPrivilegeGroupMemberExample();
            example.createCriteria().andGroupIdEqualTo(groupId)
                    .andMemberIdEqualTo(memberId)
                    .andMemberTypeEqualTo(MEMBER_TYPE_USER);

            dataPrivilegeGroupMemberMapper.deleteByExample(example);
        }else{
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
    }

    /**
     * 返回组中的所有成员
     * @param groupId
     * @return
     */
    @Override
    public List<DataPrivilegeGroupMember> listMemberInGroup(Integer groupId) throws Exception {

        if(groupId != null) {

            DataPrivilegeGroupMemberExample example = new DataPrivilegeGroupMemberExample();
            example.createCriteria().andGroupIdEqualTo(groupId)
                    .andMemberTypeEqualTo(MEMBER_TYPE_USER);

            List<DataPrivilegeGroupMember> memberList = dataPrivilegeGroupMemberMapper.selectByExample(example);

            return memberList;
        }else{
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

    }

    @Override
    public void initGroupMember(int groupId, String username, String projectId) throws Exception {
        if(StringUtils.isBlank(projectId) && StringUtils.isNotBlank(username)){
            DataPrivilegeGroupMember dataPrivilegeGroupMember = new DataPrivilegeGroupMember();
            dataPrivilegeGroupMember.setGroupId(groupId);
            dataPrivilegeGroupMember.setMemberType(MEMBER_TYPE_USER);
            dataPrivilegeGroupMember.setUsername(username);
            this.addMemberToGroup(dataPrivilegeGroupMember);
        }else if(StringUtils.isNotBlank(projectId)){
            //项目成员加到权限组中
            Project project = projectService.getProjectByProjectId(projectId);
            List<Map<String,Object>> userRoles = projectService.listProjectUser(project.getTenantId(), projectId);
            Set<String> userSet = new HashSet();
            userRoles.stream().forEach(userRole ->{
                userSet.add((String)userRole.get(CommonConstant.USERNAME));
            });
            List<DataPrivilegeGroupMember> dataPrivilegeGroupMemberList = new ArrayList<>();
            userSet.stream().forEach(user ->{
                if(!user.equalsIgnoreCase(username)) {
                    DataPrivilegeGroupMember dataPrivilegeGroupMember = new DataPrivilegeGroupMember();
                    dataPrivilegeGroupMember.setGroupId(groupId);
                    dataPrivilegeGroupMember.setMemberType(MEMBER_TYPE_USER);
                    dataPrivilegeGroupMember.setUsername(user);
                    dataPrivilegeGroupMemberList.add(dataPrivilegeGroupMember);
                }
            });
            this.addMemberListToGroup(dataPrivilegeGroupMemberList);

        }

    }

    @Override
    public void deleteAllMemberFromGroup(int groupId) throws Exception {
        DataPrivilegeGroupMemberExample example = new DataPrivilegeGroupMemberExample();
        example.createCriteria().andGroupIdEqualTo(groupId);
        dataPrivilegeGroupMemberMapper.deleteByExample(example);
    }
}
