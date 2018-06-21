package com.harmonycloud.service.dataprivilege.impl;

import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMember;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.test.JUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by chencheng on 18-6-20
 */
@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
//@Transactional
@WebAppConfiguration
public class DataPrivilegeGroupMemberServiceImplTest {

    @Autowired
    DataPrivilegeGroupMemberService dataPrivilegeGroupMemberService;

    /**
     * 向组中添加用户
     */
    @Test
    public void addMemberToGroup() throws Exception {
        DataPrivilegeGroupMember dataPrivilegeGroupMember = new DataPrivilegeGroupMember();
        dataPrivilegeGroupMember.setMemberId(99);
        dataPrivilegeGroupMember.setGroupId(88);
        dataPrivilegeGroupMemberService.addMemberToGroup(dataPrivilegeGroupMember);
        DataPrivilegeGroupMember dataPrivilegeGroupMember2 = new DataPrivilegeGroupMember();
        dataPrivilegeGroupMember2.setMemberId(100);
        dataPrivilegeGroupMember2.setGroupId(88);
        dataPrivilegeGroupMemberService.addMemberToGroup(dataPrivilegeGroupMember2);
    }

    /**
     * 删除组中的用户
     */
    @Test
    public void delMemberFromGroup() throws Exception {
        DataPrivilegeGroupMember dataPrivilegeGroupMember = new DataPrivilegeGroupMember();
        dataPrivilegeGroupMember.setMemberId(99);
        dataPrivilegeGroupMember.setGroupId(88);
        dataPrivilegeGroupMemberService.delMemberFromGroup(dataPrivilegeGroupMember);
    }

    /**
     * 返回组中的用户
     */
    @Test
    public void listMemberInGroup() throws Exception {
        Integer groupId = 88;
        List<DataPrivilegeGroupMember> members = dataPrivilegeGroupMemberService.listMemberInGroup(groupId);
        assertEquals(2, members.size());

    }
}