package com.harmonycloud.service.tenant;

import java.util.List;
import java.util.Set;

import com.harmonycloud.dao.user.bean.Resource;


/**
 * Created by zsl on 16/10/25.
 */
public interface ResourceService {

    /**
     * 得到资源对应的菜单
     * @param resourceIds 资源id列表
     * @return
     */
    List<Resource> findMenusByResourceIds(Set<Long> resourceIds) throws Exception;

    List<Resource> findAllResources() throws Exception;
}
