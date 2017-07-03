package com.harmonycloud.service.tenant.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dao.user.ResourceMapper;
import com.harmonycloud.dao.user.bean.Resource;
import com.harmonycloud.service.tenant.ResourceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by zsl on 16/10/25.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private ResourceMapper resourceMapper;

    @Override
    public List<Resource> findMenusByResourceIds(Set<Long> resourceIds) throws Exception{
        List<Resource> allResources = findAllResources();
        List<Resource> menus = new ArrayList<Resource>();
        for(Resource resource : allResources) {
            if (null != resource) {
                if (resource.isRootNode()) {
                    continue;
                }
                if (CommonConstant.MENU.equals(resource.getType())) {
                    continue;
                }
                if (!checkByResourceIds(resourceIds, resource)) {
                    continue;
                }
                menus.add(resource);
            }
        }
        return menus;
    }

    @Override
    public List<Resource> findAllResources() throws Exception{
        return resourceMapper.findAll();
    }

    private boolean checkByResourceIds(Set<Long> resourceIds, Resource resource) throws Exception{
        for(Long resourceId : resourceIds) {
            if(resource.getId().equals(resourceId)){
                return true;
            }
        }
        return false;
    }
}
