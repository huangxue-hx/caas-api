package com.harmonycloud.service.tenant.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.TenantUtils;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.tenant.UserTenantMapper;
import com.harmonycloud.dao.tenant.bean.UserTenant;
import com.harmonycloud.dao.tenant.bean.UserTenantExample;
import com.harmonycloud.dao.tenant.customs.CustomUserTenantMapper;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.customs.CustomUserMapper;
import com.harmonycloud.dto.tenant.show.UserShowDto;
import com.harmonycloud.service.tenant.UserTenantService;


@Service
@Transactional(rollbackFor = Exception.class)
public class UserTenantServiceImpl implements UserTenantService {
    @Autowired
    private UserTenantMapper userTenantMapper;
    @Autowired
    private CustomUserTenantMapper customUserTenantMapper;
    @Autowired
    private CustomUserMapper userMapper;

    @Override
    public List<UserTenant> getUserByTenantid(String tenantid) throws Exception {

        UserTenantExample example = new UserTenantExample();
        example.createCriteria().andTenantidEqualTo(tenantid);
        List<UserTenant> list = userTenantMapper.selectByExample(example);
        return list;
    }

    @Override
    public List<UserTenant> getTMByTenantid(String tenantid) throws Exception {
        UserTenantExample example = new UserTenantExample();
        example.createCriteria().andTenantidEqualTo(tenantid).andIstmEqualTo(1);
        List<UserTenant> list = userTenantMapper.selectByExample(example);
        return list;
    }

    @Override
    public void setUserByTenantid(String tenantid, List<String> username, boolean isTm,String role) throws Exception {
        for (String user : username) {
            UserTenant record = new UserTenant();
            record.setIstm(isTm == true ? 1 : 0);
            record.setTenantid(tenantid);
            Date date = TenantUtils.getUtctime();
            record.setCreateTime(date);
            record.setUsername(user);
            record.setRole(role);
            userTenantMapper.insertSelective(record);
        }

    }

    @Override
    public List<UserTenant> getUserByUserName(String userName) throws Exception {
        UserTenantExample example = new UserTenantExample();
        example.createCriteria().andUsernameEqualTo(userName);
        List<UserTenant> list = userTenantMapper.selectByExample(example);
        return list;
    }
    @Override
    public UserTenant getUserByUserNameAndTenantid(String userName,String tenantid) throws Exception {
        UserTenantExample example = new UserTenantExample();
        example.createCriteria().andUsernameEqualTo(userName).andTenantidEqualTo(tenantid);
        List<UserTenant> list = userTenantMapper.selectByExample(example);
        if(list.size()!=1){
            return null;
        }
        return list.get(0);
    }
    @Override
    public List<UserTenant> getAllUser() throws Exception {
        UserTenantExample example = new UserTenantExample();
        List<UserTenant> list = userTenantMapper.selectByExample(example);
        return list;
    }

    @Override
    public void deleteByTenantid(String tenantid) throws Exception {
        UserTenantExample example = new UserTenantExample();
        example.createCriteria().andTenantidEqualTo(tenantid);
        userTenantMapper.deleteByExample(example);
    }

    @Override
    public List<UserShowDto> getUserDetailsListByTenantid(String tenantid) throws Exception {
        String groupName = "happy";
        List<UserTenant> userByTenantid = this.getUserByTenantid(tenantid);
        List<UserShowDto> uList = new ArrayList<UserShowDto>();
        if (userByTenantid.size() > 0) {
            for (UserTenant userTenant : userByTenantid) {
                String username = userTenant.getUsername();
                User user = userMapper.findByUsername(username);
                System.out.println(user.getUuid());
                groupName = userMapper.selectGroupNameByUserID(user.getId());
                if(user!=null){
                    UserShowDto u = new UserShowDto();
                    u.setIsTm(userTenant.getIstm() == 1);
                    u.setId(user.getId());
                    u.setName(user.getUsername());
                    u.setNikeName(user.getRealName());
                    u.setEmail(user.getEmail());
                    u.setPhone(user.getPhone());
                    u.setComment(user.getComment());
                    u.setGroupName(groupName);
                    Date createTime = user.getCreateTime();
                    String date = DateUtil.DateToString(createTime, DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z);
                    u.setCreateTime(date);
                    uList.add(u);
                }
            }
        }
        return uList;
    }

    @Override
    public void deleteByTenantidAndUserName(String tenantid, String userName) throws Exception {

        UserTenantExample example = new UserTenantExample();
        example.createCriteria().andTenantidEqualTo(tenantid)
                .andUsernameEqualTo(userName);
        userTenantMapper.deleteByExample(example);
    }

    @Override
    public List<UserTenant> getTenantCount() throws Exception {
        List<UserTenant> tenantCount = customUserTenantMapper.getTenantCount();
        return tenantCount;
    }

    @Override
    public List<UserTenant> getTenantCount(String username) throws Exception {
        List<UserTenant> tenantCount = customUserTenantMapper.getTenantCountByUsername(username);
        return tenantCount;
    }

    @Override
    public String findRoleByName(String username, String tenantid) throws Exception {
        UserTenantExample example = new UserTenantExample();
        example.createCriteria().andUsernameEqualTo(username).andTenantidEqualTo(tenantid);
        List<UserTenant> selectByExample = userTenantMapper.selectByExample(example);
        if(selectByExample==null || selectByExample.size() != 1){
            throw new MarsRuntimeException("用户："+username+"不在租户id："+tenantid+"里面请检查！");
        }
        return selectByExample.get(0).getRole();
    }
    
}
