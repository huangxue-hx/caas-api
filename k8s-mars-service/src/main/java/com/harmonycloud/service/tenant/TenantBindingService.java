package com.harmonycloud.service.tenant;


import java.util.List;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.tenant.bean.TenantBinding;

/**
 * Created by zhangsl on 16/11/7.
 */
public interface TenantBindingService {

    int updateByTenantId(TenantBinding record) throws Exception;

    int updateHarborProjectsByTenantId(String tenantId, List<String> harborProjects) throws Exception;

    /**
     * 添加namespace并设置用户信息
     * @param tenantid
     * @param namespace
     * @param user
     * @param userList
     * @return
     */
    ActionReturnUtil updateTenantBinding(String tenantid, String namespace, String user, List<String> userList) throws Exception;

    /**
     * 删除namespace
     * @param tenantid
     * @param namespace
     * @return
     */
    ActionReturnUtil deleteNamespace(String tenantid, String namespace) throws Exception;
}
