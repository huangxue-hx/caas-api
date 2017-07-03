package com.harmonycloud.service.tenant.impl;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.tenant.TenantBindingMapper;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantBindingExample;
import com.harmonycloud.service.tenant.TenantBindingService;

/**
 * Created by zhangsl on 16/11/7.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TenantBindingServiceImpl implements TenantBindingService {

    @Autowired
    private TenantBindingMapper tenantBindingMapper;

    public int updateByTenantId(TenantBinding tenantBinding) throws Exception{
        return tenantBindingMapper.updateBytenantIdSelective(tenantBinding);
    }

    public int updateHarborProjectsByTenantId(String tenantId, List<String> harborProjects) throws Exception{
        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andTenantIdEqualTo(tenantId);
        List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
        if(list.size()<=0){
            return 0;
        }
        TenantBinding tenantBinding = list.get(0);
        tenantBinding.setTenantId(tenantId);
        tenantBinding.setHarborProjectList(harborProjects);
        return this.updateByTenantId(tenantBinding);
    }

    @Override
    public ActionReturnUtil updateTenantBinding(String tenantid, String namespace, String user, List<String> userList) throws Exception{

        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andTenantIdEqualTo(tenantid);
        List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
        if(list.size()<=0){
            return ActionReturnUtil.returnErrorWithMsg("tenantId error");
        }
        TenantBinding binding = list.get(0);
        String k8snamespaces = binding.getK8sNamespaces();
        if(!StringUtils.isEmpty(k8snamespaces)){
            String[] split = k8snamespaces.split(",");
            for (String string : split) {
                if(string.equals(namespace)){
                    return ActionReturnUtil.returnSuccess();
                }
            }
        }
        // user used to add
        if(!StringUtils.isEmpty(user)){
            binding.setTmUsernames(user);
        }
        //userList used to set user only
        if(userList != null && StringUtils.isEmpty(namespace) && StringUtils.isEmpty(user)){
            binding.setTmUsernameList(userList);
        }
        binding.setK8sNamespaces(namespace);

        if(tenantBindingMapper.updateBytenantIdSelective(binding)< 0){
            return ActionReturnUtil.returnErrorWithMsg("update failed");
        }

        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil deleteNamespace(String tenantid, String namespace) throws Exception{

        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andTenantIdEqualTo(tenantid);
        List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
        if(list.size()<=0){
            return ActionReturnUtil.returnSuccessWithMsg("tenantId error");
        }
        TenantBinding binding = list.get(0);
        String k8snamespaces = binding.getK8sNamespaces();

        List<String> namespacelists = new ArrayList<String>();
        if(k8snamespaces.contains(namespace)){
            String[] split = k8snamespaces.split(",");
            for (String string : split) {
                if(!string.equals(namespace)){
                    namespacelists.add(string);
                }
            }
        }
        binding.setK8sNamespaceList(namespacelists);

        if(tenantBindingMapper.updateByPrimaryKeySelective(binding) < 0){
            return ActionReturnUtil.returnErrorWithMsg("update failed");
        }
        return ActionReturnUtil.returnSuccess();
    }
}
