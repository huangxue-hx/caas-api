package com.harmonycloud.dao.tenant;



import java.util.List;

import org.springframework.stereotype.Repository;

import com.harmonycloud.dao.tenant.bean.HarborProjectTenant;

/**
 * Created by zhangsl on 16/11/13.
 */
@Repository
public interface HarborProjectTenantMapper {

    int insert(HarborProjectTenant harborProjectTenant);

    int delete(Long harborProjectTenantId);

    HarborProjectTenant getByHarborProjectId(Long harborProjectTenantId);

    List<HarborProjectTenant> list();

    List<HarborProjectTenant> getByTenantId(String tenantId);

    List<HarborProjectTenant> getByTenantIdPrivate(HarborProjectTenant harborProjectTenant);

    List<HarborProjectTenant> getByTenantIdPublic(Integer isPublic);
}
