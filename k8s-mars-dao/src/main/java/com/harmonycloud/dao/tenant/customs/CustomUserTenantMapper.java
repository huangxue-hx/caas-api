package com.harmonycloud.dao.tenant.customs;



import java.util.List;

import org.springframework.stereotype.Repository;

import com.harmonycloud.dao.tenant.bean.HarborProjectTenant;
import com.harmonycloud.dao.tenant.bean.UserTenant;

/**
 * Created by zgl on 17/5/8.
 */
@Repository
public interface CustomUserTenantMapper {

    List<UserTenant> getTenantCount();
    
    List<UserTenant> getTenantCountByUsername(String username);

}
