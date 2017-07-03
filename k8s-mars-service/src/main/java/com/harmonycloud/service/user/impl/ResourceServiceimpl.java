package com.harmonycloud.service.user.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.user.ResourceMapper;
import com.harmonycloud.dao.user.bean.Resource;
import com.harmonycloud.dao.user.bean.ResourceExample;
import com.harmonycloud.k8s.service.APIResourceService;
import com.harmonycloud.service.user.ResourceService;

@Service
@Transactional(rollbackFor = Exception.class)
public class ResourceServiceimpl implements ResourceService{
    @Autowired
    private ResourceMapper resourceMapper;
    
    @Autowired
    private APIResourceService aPIResourceService;
    
    @Override
    public List<Resource> findMenusByResourceIds(Set<Long> resourceIds) throws Exception {
        List<Resource> allResources = resourceMapper.findAll();
        List<Resource> menus = new ArrayList<Resource>();
        for (Resource resource : allResources) {
            if (null != resource) {
                if (resource.isRootNode()) {
                    continue;
                }
                if (!CommonConstant.MENU.equals(resource.getType())) {
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
    public boolean checkByResourceIds(Set<Long> resourceIds, Resource resource) throws Exception {
        for (Long resourceId : resourceIds) {
            if (resource.getId().equals(resourceId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取所有api资源列表
     * @return APIResourceList
     */
    @Override
    public ActionReturnUtil listAPIResource() throws Exception{
        List<String> operations = new ArrayList<>();
        operations.add("get");
        operations.add("list");
        operations.add("create");
        operations.add("update");
        operations.add("patch");
        operations.add("watch");
        operations.add("proxy");
        operations.add("redirect");
        operations.add("delete");
        operations.add("deletecollection");
        List<String> imagePperations = new ArrayList<>();
        imagePperations.add("read");
        imagePperations.add("write");
        List<com.harmonycloud.k8s.bean.Resource> response = aPIResourceService.listAPIResource();
        List<com.harmonycloud.dao.user.bean.RoleResource> resources = new ArrayList<>();
        for (com.harmonycloud.k8s.bean.Resource resource : response) {
            com.harmonycloud.dao.user.bean.RoleResource res = new com.harmonycloud.dao.user.bean.RoleResource();
            res.setName(resource.getName());
            res.setOperations(operations);
            resources.add(res);
        }
        com.harmonycloud.dao.user.bean.RoleResource resource = new com.harmonycloud.dao.user.bean.RoleResource();
        resource.setName("image");
        resource.setOperations(imagePperations);
        resources.add(resource);
        return ActionReturnUtil.returnSuccessWithData(resources);
    }
    
    /**
     * 获取非资源性api列表
     * @return
     */
    @Override
    public ActionReturnUtil listNoneResource() throws Exception {
        List<String> operations = new ArrayList<>();
        operations.add("get");
        operations.add("post");
        operations.add("put");
        operations.add("delete");
        com.harmonycloud.dao.user.bean.RoleResource resource = new com.harmonycloud.dao.user.bean.RoleResource();
        resource.setName("/api");
        resource.setOperations(operations);
        List<com.harmonycloud.dao.user.bean.RoleResource> resources = new ArrayList<>();
        resources.add(resource);
        resource.setName("/apis");
        resources.add(resource);
        return ActionReturnUtil.returnSuccessWithData(resources);
    }
    @Override
    public List<Map<String, Object>> listAdminMenu() throws Exception {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        ResourceExample example = new ResourceExample ();
        example.createCriteria().andIdBetween(2, 999);
        List<Resource> list = this.resourceMapper.selectByExample(example);
        if(list==null||list.size()<=0){
            return null;
        }
        for (Resource resource : list) {
            if(StringUtils.isEmpty(resource.getParentIds())){
                Map<String, Object> map = new HashMap<>();
                map.put(CommonConstant.ID,  resource.getId());
                map.put(CommonConstant.NAME,  resource.getName());
                map.put(CommonConstant.URL,  resource.getUrl());
                map.put(CommonConstant.ICONNAME,  resource.getIconName());
                ResourceExample example1 = new ResourceExample ();
                example1.createCriteria().andParentIdEqualTo(resource.getId());
                List<Resource> list2 = this.resourceMapper.selectByExample(example1);
                
                if(list2!=null&&list2.size()>0){
                    map.put(CommonConstant.SUBMENU,list2);
                }
                result.add(map);
            }
        }
        return result;
    }
    @Override
    public List<Map<String, Object>> listDevMenu() throws Exception {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        ResourceExample example = new ResourceExample ();
        example.createCriteria().andIdBetween(500, 2000);
        List<Resource> list = this.resourceMapper.selectByExample(example);
        if(list==null||list.size()<=0){
            return null;
        }
        for (Resource resource : list) {
            if(StringUtils.isEmpty(resource.getParentIds())){
                Map<String, Object> map = new HashMap<>();
                map.put(CommonConstant.ID,  resource.getId());
                map.put(CommonConstant.NAME,  resource.getName());
                map.put(CommonConstant.URL,  resource.getUrl());
                map.put(CommonConstant.ICONNAME,  resource.getIconName());
                ResourceExample example1 = new ResourceExample ();
                example1.createCriteria().andParentIdEqualTo(resource.getId());
                List<Resource> list2 = this.resourceMapper.selectByExample(example1);
                
                if(list2!=null&&list2.size()>0){
                    map.put(CommonConstant.SUBMENU,list2);
                }
                result.add(map);
            }
        }
        return result;
    }
    
}
