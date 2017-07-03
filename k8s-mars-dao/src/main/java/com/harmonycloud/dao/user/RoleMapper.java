package com.harmonycloud.dao.user;


import org.springframework.stereotype.Repository;

import com.harmonycloud.dao.user.bean.Role;


/**
 * Created by zsl on 16/10/25.
 */
@Repository
public interface RoleMapper {

    Role findByName(String roleName);
}
