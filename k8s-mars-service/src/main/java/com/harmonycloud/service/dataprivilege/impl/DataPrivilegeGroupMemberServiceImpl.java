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
@Transactional(rollbackFor = Exception.class)
public class DataPrivilegeGroupMemberServiceImpl implements DataPrivilegeGroupMemberService {

    @Autowired
    DataPrivilegeGroupMemberMapper dataPrivilegeGroupMemberMapper;

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Autowired
    DataPrivilegeGroupService dataPrivilegeGroupService;

    @Autowired
    DataPrivilegeGroupMappingService dataPrivilegeGroupMappingService;

    @Autowired
    DataPrivilegeStrategyService dataPrivilegeStrategyService;

    @Autowired
    UserRoleRelationshipService userRoleRelationshipService;

    @Autowired
    HttpSession session;

    private static final Integer MEMBER_TYPE_USER = 0;//用户

    private static final Integer MEMBER_TYPE_GROUP = 1;//组

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

            dataPrivilegeGroupMember.setMemberType(MEMBER_TYPE_USER);
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
            dataPrivilegeGroupMemberMapper.insertUserList(groupId, MEMBER_TYPE_USER, userList);
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
    public void initGroupMember(int groupId, String projectId, Integer parentGroupId, int privilegeType) throws Exception {
        String currentUsername = userService.getCurrentUsername();
        Long currentUserId = (Long) session.getAttribute(CommonConstant.USERID);
        List<String> userList = new ArrayList<>();
        //含有父资源的数据，先拷贝父资源的数据权限组
        if(parentGroupId != null){
            dataPrivilegeGroupMemberMapper.copyGroupMember(parentGroupId, groupId);
            userList = this.listAllUserInGroup(groupId);
        }
        if(StringUtils.isNotBlank(projectId)){
            //项目成员加到权限组中
            Project project = projectService.getProjectByProjectId(projectId);
            List<Map<String,Object>> userRoles = projectService.listProjectUser(project.getTenantId(), projectId);
            Set<String> userSet = new HashSet();
            userRoles.stream().forEach(userRole -> userSet.add((String) userRole.get(CommonConstant.USERNAME)));
            userSet.addAll(userList);
            if(CommonConstant.DATA_READONLY == privilegeType){
                userSet.remove(currentUsername);
            }else if(CommonConstant.DATA_READWRITE == privilegeType){
                userSet.add(currentUsername);
            }
            this.addUserListToGroup(groupId, new ArrayList<>(userSet));
        }else if(CommonConstant.DATA_READWRITE == privilegeType){
            //创建者加到权限组中
            DataPrivilegeGroupMember dataPrivilegeGroupMember = new DataPrivilegeGroupMember();
            dataPrivilegeGroupMember.setGroupId(groupId);
            dataPrivilegeGroupMember.setMemberType(MEMBER_TYPE_USER);
            dataPrivilegeGroupMember.setMemberId(currentUserId.intValue());
            dataPrivilegeGroupMember.setUsername(currentUsername);
            this.addMemberToGroup(dataPrivilegeGroupMember);
        }

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
        Integer strategy = null;
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
            int creatorId = dataPrivilegeGroupMapping.getCreatorId().intValue();
            Integer parentCreatorId = null;
            Integer parentId = dataPrivilegeGroupMapping.getParentId();
            DataPrivilegeGroupMapping parentMapping;
            if(parentId != null){
                parentMapping = dataPrivilegeGroupMappingMap.get(parentId);
                parentCreatorId = parentMapping.getCreatorId().intValue();
            }
            int groupId = dataPrivilegeGroupMapping.getGroupId();
            switch(strategy){
                //封闭策略，加入用户是资源创建者则加到读写列表
                case CommonConstant.DATA_CLOSED_STRATEGY:
                    if(parentCreatorId != null && parentCreatorId == creatorId){
                        continue;
                    }
                    if(CommonConstant.DATA_READWRITE == dataPrivilegeGroupMapping.getPrivilegeType()){
                        User creator = userMap.get(creatorId);
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
                        User creator = userMap.get(creatorId);
                        if(creator != null){
                            this.addMemberToPrivilegeGroup(groupId, creatorId, creator.getUsername());
                        }
                    }else if(CommonConstant.DATA_READONLY == dataPrivilegeGroupMapping.getPrivilegeType()){
                        List<String> cloneList = new ArrayList<>(newProjectUserList);
                        User creator = userMap.get(creatorId);
                        if(creator != null){
                            cloneList.remove(creator.getUsername());
                        }
                        if(parentCreatorId != null && parentCreatorId != creatorId){
                            User parentCreator = userMap.get(parentCreatorId);
                            cloneList.remove(parentCreator.getUsername());
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
        groupList.add(groupId);
        groupList.stream().forEach(id ->{
            DataPrivilegeGroupMember dataPrivilegeGroupMember = new DataPrivilegeGroupMember();
            dataPrivilegeGroupMember.setGroupId(id);
            dataPrivilegeGroupMember.setMemberType(MEMBER_TYPE_USER);
            dataPrivilegeGroupMember.setMemberId(userId);
            dataPrivilegeGroupMember.setUsername(username);
            dataPrivilegeGroupMemberList.add(dataPrivilegeGroupMember);

        });
        this.addListToGroup(dataPrivilegeGroupMemberList);
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
            resultMap.put(RO_GROUP_ID, null);
            resultMap.put(RW_GROUP_ID, null);
            resultMap.put(RO_LIST, Collections.EMPTY_LIST);
            resultMap.put(RW_LIST, Collections.EMPTY_LIST);
            return resultMap;
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
     * @throws Exception
     */
    @Override
    public void verifyMember(Integer groupId, Integer otherGroupId, String username, boolean isAdd) throws Exception {
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
        }else{
            //删除用户时校验父资源中是否有此用户
            List<String> parentUserList = dataPrivilegeGroupMemberMapper.selectParentDataGroupUser(groupId);
            if(parentUserList.contains(username)){
                throw new MarsRuntimeException(ErrorCodeMessage.PARENT_GROUP_USER_DELETE_FIRST);
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
            if(dataPrivilegeGroupMember.getMemberType() == MEMBER_TYPE_USER) {
                userList.add(dataPrivilegeGroupMember.getUsername());
            }else if (dataPrivilegeGroupMember.getMemberType() == MEMBER_TYPE_GROUP) {
                try {
                    userList.addAll(this.listAllUserInGroup(dataPrivilegeGroupMember.getMemberId()));
                } catch (Exception e) {
                    throw new MarsRuntimeException();
                }
            }
        });
        return userList;
    }

}
