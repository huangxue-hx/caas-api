package com.harmonycloud.service.dataprivilege.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.dataprivilege.DataPrivilegeGroupMemberMapper;
import com.harmonycloud.dao.dataprivilege.bean.*;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.bean.UserRoleRelationship;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMappingService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeStrategyService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.user.UserRoleRelationshipService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chencheng on 18-6-20
 */
@Service
public class DataPrivilegeGroupMemberServiceImpl implements DataPrivilegeGroupMemberService {

    @Autowired
    private DataPrivilegeGroupMemberMapper dataPrivilegeGroupMemberMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private DataPrivilegeGroupService dataPrivilegeGroupService;

    @Autowired
    private DataPrivilegeGroupMappingService dataPrivilegeGroupMappingService;

    @Autowired
    private DataPrivilegeStrategyService dataPrivilegeStrategyService;

    @Autowired
    private UserRoleRelationshipService userRoleRelationshipService;

    @Autowired
    private HttpSession session;


    private static final String RO_GROUP_ID = "roGroupId";
    private static final String RW_GROUP_ID = "rwGroupId";
    private static final String RO_LIST = "roList";
    private static final String RW_LIST = "rwList";

    private static final Logger logger = LoggerFactory.getLogger(DataPrivilegeGroupMemberServiceImpl.class);


    /**
     * 向组中添加成员
     * @param dataPrivilegeGroupMember
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void addMemberToGroup(DataPrivilegeGroupMember dataPrivilegeGroupMember) throws Exception {
        Integer groupId = dataPrivilegeGroupMember.getGroupId();
        if(groupId != null) {

            dataPrivilegeGroupMember.setMemberType(CommonConstant.MEMBER_TYPE_USER);
            dataPrivilegeGroupMemberMapper.insert(dataPrivilegeGroupMember);
        }else{
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }


    }

    /**
     * 数据权限组中加入成员列表
     * @param dataPrivilegeGroupMemberList
     */
    private void addListToGroup(List<DataPrivilegeGroupMember> dataPrivilegeGroupMemberList) {
        if(CollectionUtils.isNotEmpty(dataPrivilegeGroupMemberList)) {
            dataPrivilegeGroupMemberMapper.insertList(dataPrivilegeGroupMemberList);
        }
    }

    /**
     * 数据权限组中加入用户名列表
     * @param groupId
     * @param userList
     */
    private void addUserListToGroup(int groupId, List<String> userList){
        if(CollectionUtils.isNotEmpty(userList)) {
            dataPrivilegeGroupMemberMapper.insertUserList(groupId, CommonConstant.MEMBER_TYPE_USER, userList);
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
        String username = dataPrivilegeGroupMember.getUsername();

        if(groupId != null){
            DataPrivilegeGroupMemberExample example = new DataPrivilegeGroupMemberExample();
            example.createCriteria().andGroupIdEqualTo(groupId)
                    .andUsernameEqualTo(username)
                    .andMemberTypeEqualTo(CommonConstant.MEMBER_TYPE_USER);
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
                    .andMemberTypeEqualTo(CommonConstant.MEMBER_TYPE_USER);

            List<DataPrivilegeGroupMember> memberList = dataPrivilegeGroupMemberMapper.selectByExample(example);

            return memberList;
        }else{
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

    }


    private List<DataPrivilegeGroupMember> listMemberWithRealNameInGroup(Integer groupId) throws Exception {
        if(groupId == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        return dataPrivilegeGroupMemberMapper.selectGroupMemberWithRealName(groupId);
    }

    /**
     * 初始化权限组成员
     * @param groupId
     * @param projectId
     * @param parentGroupId
     * @param privilegeType
     * @throws Exception
     */
    @Override
    public List<String> initGroupMember(int groupId, String projectId, Integer parentGroupId, int privilegeType, List<String> rwUserlist) throws Exception {
        String currentUsername = userService.getCurrentUsername();
        List<String> userList = new ArrayList<>();
        //含有父资源的数据，先获取父资源的数据权限组
        if(parentGroupId != null){
            userList = this.listAllUserInGroup(parentGroupId);
        }
        //获取项目组成员
        if(StringUtils.isNotBlank(projectId)){
            Project project = projectService.getProjectByProjectId(projectId);
            List<Map<String,Object>> userRoles = projectService.listProjectUser(project.getTenantId(), projectId);
            List projectUsers = userRoles.stream().map(userRole->userRole.get(CommonConstant.USERNAME)).collect(Collectors.toList());
            userList.addAll(projectUsers);
        }

        if(CommonConstant.DATA_READWRITE == privilegeType) {
            //读写组加入创建者
            userList.add(currentUsername);
        }else if(CommonConstant.DATA_READONLY == privilegeType){
            //只读组去掉创建者及读写组成员
            userList.removeAll(Arrays.asList(currentUsername));
            userList.removeAll(rwUserlist);
        }
        this.addUserListToGroup(groupId, userList.stream().distinct().collect(Collectors.toList()));

        return userList;

    }


    /**
     * 删除权限组下所有成员
     * @param groupId
     * @throws Exception
     */
    @Override
    public void deleteAllMemberFromGroup(int groupId) throws Exception {
        DataPrivilegeGroupMemberExample example = new DataPrivilegeGroupMemberExample();
        example.createCriteria().andGroupIdEqualTo(groupId);
        dataPrivilegeGroupMemberMapper.deleteByExample(example);
    }



    /**
     * 新增项目成员
     * @param project
     * @param usernameList
     * @throws Exception
     */
    @Override
    public void addNewProjectMemberToGroup(Project project, List<String> usernameList) throws Exception {
        Integer strategy = CommonConstant.DATA_OPEN_STRATEGY;
        String tenantId = project.getTenantId();
        String projectId = project.getProjectId();
        List<DataPrivilegeStrategy> strategyList = dataPrivilegeStrategyService.selectStrategy(tenantId, null, null);

        if(CollectionUtils.isNotEmpty(strategyList)){
            strategy = Integer.valueOf(strategyList.get(0).getStrategy());
        }
        if(strategy == null){
            return;
        }
        //获取项目新增用户列表
        List<String> newProjectUserList = new ArrayList();
        for(String username : usernameList) {
            List<UserRoleRelationship> list = userRoleRelationshipService.getUserRoleRelationshipByUsernameAndProjectId(username, projectId);
            if (CollectionUtils.isEmpty(list)) {
                newProjectUserList.add(username);
            }
        }

        if(CollectionUtils.isEmpty(newProjectUserList)){
            return;
        }
        List<User> userList = userService.getUserByUsernameList(newProjectUserList);
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, user -> user));

        //获取资源与组的关联列表
        DataPrivilegeDto dataPrivilegeDto = new DataPrivilegeDto();
        dataPrivilegeDto.setProjectId(projectId);
        List<DataPrivilegeGroupMapping> list = dataPrivilegeGroupMappingService.getDataPrivilegeGroupMapping(dataPrivilegeDto);
        Map<Integer, DataPrivilegeGroupMapping> dataPrivilegeGroupMappingMap = list.stream().collect(Collectors.toMap(DataPrivilegeGroupMapping::getId, dataPrivilegeGroupMapping->dataPrivilegeGroupMapping));

        for(DataPrivilegeGroupMapping dataPrivilegeGroupMapping : list){
            int creatorId = 0;
            if(dataPrivilegeGroupMapping.getCreatorId() != null){
                creatorId = dataPrivilegeGroupMapping.getCreatorId().intValue();
            }
            Integer parentCreatorId = null;
            Integer parentId = dataPrivilegeGroupMapping.getParentId();
            DataPrivilegeGroupMapping parentMapping;
            if(parentId != null){
                parentMapping = dataPrivilegeGroupMappingMap.get(parentId);
                if (parentMapping != null && parentMapping.getCreatorId() != null) {
                    parentCreatorId = parentMapping.getCreatorId().intValue();
                }
            }
            int groupId = dataPrivilegeGroupMapping.getGroupId();
            switch(strategy){
                //封闭策略，加入用户是资源创建者则加到读写列表
                case CommonConstant.DATA_CLOSED_STRATEGY:
                    if(parentCreatorId != null && parentCreatorId == creatorId){
                        continue;
                    }
                    if(CommonConstant.DATA_READWRITE == dataPrivilegeGroupMapping.getPrivilegeType()) {
                        User creator = userMap.get((long)creatorId);
                        if(creator != null){
                            this.addMemberToPrivilegeGroup(groupId, creatorId, creator.getUsername());
                        }
                    }
                    break;
                //半开放策略，加入用户是创建者则加到读写列表，其他用户加到只读列表
                case CommonConstant.DATA_SEMIOPEN_STRATEGY:
                    if(CommonConstant.DATA_READWRITE == dataPrivilegeGroupMapping.getPrivilegeType()){
                        if(parentCreatorId != null && parentCreatorId == creatorId){
                            continue;
                        }
                        User creator = userMap.get((long)creatorId);
                        if(creator != null){
                            this.addMemberToPrivilegeGroup(groupId, creatorId, creator.getUsername());
                        }
                    }else if(CommonConstant.DATA_READONLY == dataPrivilegeGroupMapping.getPrivilegeType()){
                        List<String> cloneList = new ArrayList<>(newProjectUserList);
                        User creator = userMap.get((long)creatorId);
                        if(creator != null){
                            cloneList.remove(creator.getUsername());
                        }
                        if(parentCreatorId != null && parentCreatorId != creatorId){
                            User parentCreator = userMap.get((long)parentCreatorId);
                            if(parentCreator != null) {
                                cloneList.remove(parentCreator.getUsername());
                            }
                        }
                        this.addUserListToGroup(groupId, cloneList);
                    }
                    break;
                //开发策略，用户加到读写列表
                case CommonConstant.DATA_OPEN_STRATEGY:
                    if(CommonConstant.DATA_READWRITE == dataPrivilegeGroupMapping.getPrivilegeType()){
                        this.addUserListToGroup(groupId, newProjectUserList);
                    }
                    break;
            }

        }
    }

    /**
     * 删除项目中所有数据权限组的对应成员
     * @param projectId
     * @param username
     * @throws Exception
     */
    @Override
    public void deleteProjectMemberFromGroup(String projectId, String username) throws Exception {
        dataPrivilegeGroupMemberMapper.deleteUserInProject(projectId, username);
    }

    /**
     * 删除所有权限组中的某成员
     * @param dataPrivilegeGroupMember
     * @throws Exception
     */
    public void deleteMemberInAllGroup(DataPrivilegeGroupMember dataPrivilegeGroupMember) throws Exception {
        DataPrivilegeGroupMemberExample example = new DataPrivilegeGroupMemberExample();
        example.createCriteria().andMemberIdEqualTo(dataPrivilegeGroupMember.getMemberId()).andMemberTypeEqualTo(dataPrivilegeGroupMember.getMemberType());
        dataPrivilegeGroupMemberMapper.deleteByExample(example);
    }

    /**
     * 数据权限组中增加成员
     * @param groupId
     * @param userId
     * @param username
     * @throws Exception
     */
    @Override
    public void addMemberToPrivilegeGroup(Integer groupId, int userId, String username) throws Exception {
        List<Integer> groupList = dataPrivilegeGroupMappingService.getChildDataMappingGroupWithoutUser(groupId, username);
        List<DataPrivilegeGroupMember> dataPrivilegeGroupMemberList = new ArrayList<>();
        if(!this.existUser(groupId, userId)) {
            groupList.add(groupId);
        }
        groupList.stream().forEach(id ->{
            DataPrivilegeGroupMember dataPrivilegeGroupMember = new DataPrivilegeGroupMember();
            dataPrivilegeGroupMember.setGroupId(id);
            dataPrivilegeGroupMember.setMemberType(CommonConstant.MEMBER_TYPE_USER);
            dataPrivilegeGroupMember.setMemberId(userId);
            dataPrivilegeGroupMember.setUsername(username);
            dataPrivilegeGroupMemberList.add(dataPrivilegeGroupMember);

        });
        try {
            if(CollectionUtils.isNotEmpty(dataPrivilegeGroupMemberList)) {
                this.addListToGroup(dataPrivilegeGroupMemberList);
            }
        }catch (DuplicateKeyException e){
            throw new MarsRuntimeException(ErrorCodeMessage.GROUP_USER_EXIST);
        }
    }


    /**
     * 数据权限组中删除成员
     * @param groupId
     * @param username
     * @throws Exception
     */
    @Override
    public void delMemberFromPrivilegeGroup(int groupId, String username) throws Exception {
        List<Integer> groupList = dataPrivilegeGroupMappingService.getChildDataMappingGroupWithoutUser(groupId, null);
        groupList.add(groupId);
        dataPrivilegeGroupMemberMapper.deleteUserInGroupList(username, groupList);
    }

    /**
     * 列出数据的权限组成员
     * @param dataPrivilegeDto
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> listGroupMemberForData(DataPrivilegeDto dataPrivilegeDto) throws Exception {
        Map<String, Object> resultMap = new HashMap<String,Object>();

        if (StringUtils.isBlank(dataPrivilegeDto.getData())){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        Integer roGroupId = null;//只读权限列表groupId

        Integer rwGroupId = null;//可读写权限列表groupId


        List<DataPrivilegeGroupMapping> mappingList = dataPrivilegeGroupMappingService.listDataPrivilegeGroupMapping(dataPrivilegeDto);

        if(CollectionUtils.isEmpty(mappingList)){
            dataPrivilegeGroupMappingService.initMapping(dataPrivilegeDto);
            mappingList = dataPrivilegeGroupMappingService.listDataPrivilegeGroupMapping(dataPrivilegeDto);
        }
        for (DataPrivilegeGroupMapping mapping : mappingList) {
            if(mapping.getPrivilegeType() == CommonConstant.DATA_READONLY){
                roGroupId = mapping.getGroupId();
            }else if(mapping.getPrivilegeType() == CommonConstant.DATA_READWRITE){
                rwGroupId = mapping.getGroupId();
            }

        }


        //只读组成员
        if(roGroupId != null) {
            resultMap.put(RO_GROUP_ID, roGroupId);
            List<DataPrivilegeGroupMember> roMemberList = this.listMemberWithRealNameInGroup(roGroupId);
            resultMap.put(RO_LIST, roMemberList);
        }
        //读写组成员
        if(rwGroupId != null) {
            resultMap.put(RW_GROUP_ID, rwGroupId);
            List<DataPrivilegeGroupMember> rwMemberList = this.listMemberWithRealNameInGroup(rwGroupId);
            resultMap.put(RW_LIST,rwMemberList);
        }

        return resultMap;
    }

    /**
     * 校验增删数据权限成员
     * @param groupId
     * @param username
     * @param groupType
     * @throws Exception
     */
    @Override
    public void verifyMember(Integer groupId, Integer otherGroupId, String username, boolean isAdd, Integer groupType) throws Exception {
        Integer rwGroupId = null;
        List<String> userList = null;
        String currentUser = (String)session.getAttribute(CommonConstant.USERNAME);
        int currentRoleId = userService.getCurrentRoleId();
        if(currentRoleId > CommonConstant.PM_ROLEID) {
            //判断当前用户是否在资源的读写列表里
            DataPrivilegeGroupMappingExample example = new DataPrivilegeGroupMappingExample();
            example.createCriteria().andGroupIdEqualTo(groupId);
            List<DataPrivilegeGroupMapping> list = dataPrivilegeGroupMappingService.getDataPrivilegeGroupMapping(example);
            if (CollectionUtils.isNotEmpty(list)) {
                DataPrivilegeGroupMapping dataPrivilegeGroupMapping = list.get(0);
                if (dataPrivilegeGroupMapping.getPrivilegeType() == CommonConstant.DATA_READWRITE) {
                    rwGroupId = groupId;
                } else {
                    rwGroupId = otherGroupId;
                }
            }
            userList = this.listAllUserInGroup(rwGroupId);
            if (CollectionUtils.isEmpty(userList) || !userList.contains(currentUser)) {
                throw new MarsRuntimeException(ErrorCodeMessage.GROUP_EDIT_NO_PRIVILEGE);
            }
        }

        if(isAdd){
            //新增用户时校验列表中是否已存在该用户
            if(rwGroupId != groupId){
                userList = this.listAllUserInGroup(groupId);
            }
            if(userList.contains(username)){
                throw new MarsRuntimeException(ErrorCodeMessage.GROUP_USER_EXIST);
            }
            //服务权限不能小于应用
            if(groupType == CommonConstant.DATA_READONLY){
                List<String> parentUserList = dataPrivilegeGroupMemberMapper.selectParentDataGroupUser(otherGroupId);
                if(parentUserList.contains(username)){
                    throw new MarsRuntimeException(ErrorCodeMessage.DATA_PRIVILEGE_UPDATE_ERROR);
                }
            }
        }else{
            //删除用户时校验父资源中是否有此用户
            List<String> parentUserList = dataPrivilegeGroupMemberMapper.selectParentDataGroupUser(groupId);
            List<String> otherParentUserList = dataPrivilegeGroupMemberMapper.selectParentDataGroupUser(otherGroupId);
            if(parentUserList.contains(username) || otherParentUserList.contains(username)){
                throw new MarsRuntimeException(ErrorCodeMessage.DATA_PRIVILEGE_UPDATE_ERROR);
            }
        }
    }



    /**
     * 获取组中所有成员
     * @param groupId
     * @return
     * @throws Exception
     */
    private List<String> listAllUserInGroup(int groupId) throws Exception {
        List<String> userList = new ArrayList<>();
        List<DataPrivilegeGroupMember> list = this.listMemberInGroup(groupId);
        list.stream().forEach(dataPrivilegeGroupMember->{
            if(dataPrivilegeGroupMember.getMemberType() == CommonConstant.MEMBER_TYPE_USER) {
                userList.add(dataPrivilegeGroupMember.getUsername());
            }else if (dataPrivilegeGroupMember.getMemberType() == CommonConstant.MEMBER_TYPE_GROUP) {
                try {
                    userList.addAll(this.listAllUserInGroup(dataPrivilegeGroupMember.getMemberId()));
                } catch (Exception e) {
                    throw new MarsRuntimeException(ErrorCodeMessage.GROUP_QUERY_ERROR);
                }
            }
        });
        return userList;
    }

    @Override
    public void initGroupMemberByStrategy(int strategy, String projectId, Integer roGroupId, Integer rwGroupId, Integer parentRoGroupId, Integer parentRwGroupId) throws Exception{
        List<String> rwUserList;
        switch(strategy){
            case CommonConstant.DATA_CLOSED_STRATEGY:
                rwUserList = initGroupMember(rwGroupId, null, parentRwGroupId, CommonConstant.DATA_READWRITE, null);
                initGroupMember(roGroupId, null, parentRoGroupId, CommonConstant.DATA_READONLY, rwUserList);
                break;
            case CommonConstant.DATA_SEMIOPEN_STRATEGY:
                rwUserList = initGroupMember(rwGroupId, null, parentRwGroupId, CommonConstant.DATA_READWRITE, null);
                initGroupMember(roGroupId, projectId, parentRoGroupId, CommonConstant.DATA_READONLY, rwUserList);
                break;
            case CommonConstant.DATA_OPEN_STRATEGY:
                rwUserList = initGroupMember(rwGroupId, projectId, parentRwGroupId, CommonConstant.DATA_READWRITE, null);
                initGroupMember(roGroupId, null, parentRoGroupId, CommonConstant.DATA_READONLY, rwUserList);
                break;
        }
    }


    private boolean existUser(Integer groupId, int userId) {
        DataPrivilegeGroupMemberExample e = new DataPrivilegeGroupMemberExample();
        e.createCriteria().andGroupIdEqualTo(groupId).andMemberTypeEqualTo(CommonConstant.MEMBER_TYPE_USER).andMemberIdEqualTo(userId);
        List<DataPrivilegeGroupMember> list = dataPrivilegeGroupMemberMapper.selectByExample(e);
        if(CollectionUtils.isNotEmpty(list)){
            return true;
        }else{
            return false;
        }
    }

}
