package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.LocalPrivilege;
import com.harmonycloud.dao.user.bean.LocalPrivilegeExample;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalPrivilegeMapper {
    int deleteByExample(LocalPrivilegeExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(LocalPrivilege record);

    int insertSelective(LocalPrivilege record);

    List<LocalPrivilege> selectByExample(LocalPrivilegeExample example);

    LocalPrivilege selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(LocalPrivilege record);

    int updateByPrimaryKey(LocalPrivilege record);
}