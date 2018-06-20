package com.harmonycloud.service.dataprivilege.impl;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.dataprivilege.DataPrivilegeGroupMemberMapper;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMember;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMemberExample;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.tenant.impl.TenantServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by chencheng on 18-6-20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DataPrivilegeGroupMemberServiceImpl implements DataPrivilegeGroupMemberService {

    @Autowired
    DataPrivilegeGroupMemberMapper dataPrivilegeGroupMemberMapper;

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

        String groupId = dataPrivilegeGroupMember.getGroupId();
        Integer memberId = dataPrivilegeGroupMember.getMemberId();

        if(StringUtils.isNotBlank(groupId) && StringUtils.isNotBlank(memberId.toString())) {

            dataPrivilegeGroupMember.setMemberType(MEMBER_TYPE_USER);
            dataPrivilegeGroupMemberMapper.insert(dataPrivilegeGroupMember);
        }else{
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }


    }

    /**
     * 删除组中的成员
     * @param dataPrivilegeGroupMember
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void delMemberFromGroup(DataPrivilegeGroupMember dataPrivilegeGroupMember) throws Exception {

        String groupId = dataPrivilegeGroupMember.getGroupId();
        Integer memberId = dataPrivilegeGroupMember.getMemberId();

        if(StringUtils.isNotBlank(groupId) && StringUtils.isNotBlank(memberId.toString())){

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
    public List<DataPrivilegeGroupMember> listMemberInGroup(String groupId) throws Exception {

        if(StringUtils.isNotBlank(groupId)) {

            DataPrivilegeGroupMemberExample example = new DataPrivilegeGroupMemberExample();
            example.createCriteria().andGroupIdEqualTo(groupId)
                    .andMemberTypeEqualTo(MEMBER_TYPE_USER);

            List<DataPrivilegeGroupMember> memberList = dataPrivilegeGroupMemberMapper.selectByExample(example);

            return memberList;
        }else{
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

    }
}
