package com.harmonycloud.service.tenant;

import com.harmonycloud.dao.user.bean.Role;

/**
 * Created by zsl on 16/10/25.
 */
public interface RoleService {

    Role findByName(String roleName) throws Exception;
}
