package com.harmonycloud.service.tenant.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.tenant.PrivatePartitionMapper;
import com.harmonycloud.dao.tenant.TenantBindingMapper;
import com.harmonycloud.dao.tenant.bean.PrivatePartition;
import com.harmonycloud.dao.tenant.bean.PrivatePartitionExample;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantBindingExample;
import com.harmonycloud.service.tenant.PrivatePartitionService;

/**
 * Created by zgl on 17-4-5.
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class PrivatePartitionImpl implements PrivatePartitionService {

    @Autowired
    PrivatePartitionMapper privatePartitionMapper;

    @Autowired
    TenantBindingMapper tenantBindingMapper;

    @Override
    public ActionReturnUtil setPrivatePartition(String tenantid, String namespace) throws Exception {
        // 初始化判断
        if (StringUtils.isBlank(tenantid) || StringUtils.isBlank(namespace)) {
            return ActionReturnUtil.returnErrorWithMsg("tenantid or namespace 不能为空");
        }
        // 1 根据tenantid获取tenantname
        TenantBindingExample tenantexample = new TenantBindingExample();
        tenantexample.createCriteria().andTenantIdEqualTo(tenantid);
        List<TenantBinding> tenantlist = tenantBindingMapper.selectByExample(tenantexample);
        if (tenantlist.size() <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("tenantid 传入错误");
        }
        TenantBinding tenantBinding = tenantlist.get(0);
        String tenantname = tenantBinding.getTenantName();
        // 检查namespace是否有效
        boolean contains = tenantBinding.getK8sNamespaces().contains(namespace);
        if (!contains) {
            return ActionReturnUtil.returnErrorWithMsg("tenant ：" + tenantname + " 中不存在namespace ：" + namespace);
        }
        // 2 更新数据库
        PrivatePartition bean = null;
        PrivatePartitionExample example = new PrivatePartitionExample();
        example.createCriteria().andTenantIdEqualTo(tenantid).andNamespaceEqualTo(namespace);
        List<PrivatePartition> list = privatePartitionMapper.selectByExample(example);
        if(list!=null&&list.size()>0){
            bean = list.get(0);
            String label = "HarmonyCloud_TenantName=" + namespace;
            bean.setIsPrivate(1);
            bean.setNamespace(namespace);
            bean.setTenantId(tenantid);
            bean.setTenantName(tenantname);
            bean.setLabel(label);
            privatePartitionMapper.updateByPrimaryKeySelective(bean);
        }else{
            bean = new PrivatePartition();
            String label = "HarmonyCloud_TenantName=" + namespace;
            bean.setIsPrivate(1);
            bean.setNamespace(namespace);
            bean.setTenantId(tenantid);
            bean.setTenantName(tenantname);
            bean.setLabel(label);
            privatePartitionMapper.insertSelective(bean);
        }
        return ActionReturnUtil.returnSuccess();
    }
    @Override
    public ActionReturnUtil setSharePartition(String tenantid, String namespace, boolean config) throws Exception {
        // 初始化判断
        if (StringUtils.isBlank(tenantid) || StringUtils.isBlank(namespace)) {
            return ActionReturnUtil.returnErrorWithMsg("tenantid or namespace 不能为空");
        }
        // 1 根据tenantid获取tenantname
        TenantBindingExample tenantexample = new TenantBindingExample();
        tenantexample.createCriteria().andTenantIdEqualTo(tenantid);
        List<TenantBinding> tenantlist = tenantBindingMapper.selectByExample(tenantexample);
        if (tenantlist.size() <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("tenantid 传入错误");
        }
        TenantBinding tenantBinding = tenantlist.get(0);
        String tenantname = tenantBinding.getTenantName();
        // 检查namespace是否有效
        boolean contains = tenantBinding.getK8sNamespaces().contains(namespace);
        if (!contains) {
            return ActionReturnUtil.returnErrorWithMsg("tenant ：" + tenantname + " 中不存在namespace ：" + namespace);
        }
        // 2 更新数据库
        PrivatePartition bean = new PrivatePartition();
        String label = null;
        if (config) {
            label = "HarmonyCloud_TenantName=" + namespace;
        } else {
            label = "HarmonyCloud_Status=C";
        }

        bean.setIsPrivate(0);
        bean.setNamespace(namespace);
        bean.setTenantId(tenantid);
        bean.setTenantName(tenantname);
        bean.setLabel(label);
        privatePartitionMapper.insertSelective(bean);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil removePrivatePartition(String tenantid, String namespace) throws Exception {

        // 初始化判断
        if (StringUtils.isBlank(tenantid) || StringUtils.isBlank(namespace)) {
            return ActionReturnUtil.returnErrorWithMsg("tenantid or namespace 不能为空");
        }
        // 查看私有分区是否存在
        PrivatePartitionExample example = new PrivatePartitionExample();
        example.createCriteria().andTenantIdEqualTo(tenantid).andNamespaceEqualTo(namespace);
        List<PrivatePartition> list = privatePartitionMapper.selectByExample(example);
        if (list.size() <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("待删除的私有分区不存在或已经删除");
        }
        // 更新数据库
        privatePartitionMapper.deleteByExample(example);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public boolean isPrivatePartition(String tenantid, String namespace) throws Exception {

        // 初始化判断
        if (StringUtils.isBlank(tenantid) || StringUtils.isBlank(namespace)) {
            return false;
        }
        // 查看私有分区是否存在
        PrivatePartitionExample example = new PrivatePartitionExample();
        example.createCriteria().andTenantIdEqualTo(tenantid).andNamespaceEqualTo(namespace).andIsPrivateEqualTo(1);
        List<PrivatePartition> list = privatePartitionMapper.selectByExample(example);
        if (list.size() <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public ActionReturnUtil getPrivatePartitionLabel(String tenantid, String namespace) throws Exception {

        // 初始化判断
        if (StringUtils.isBlank(tenantid) || StringUtils.isBlank(namespace)) {
            return ActionReturnUtil.returnErrorWithMsg("tenantid or namespace 不能为空");
        }
        // 查看私有分区是否存在
        PrivatePartitionExample example = new PrivatePartitionExample();
        example.createCriteria().andTenantIdEqualTo(tenantid).andNamespaceEqualTo(namespace);
        List<PrivatePartition> list = privatePartitionMapper.selectByExample(example);
        if (list.size() <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("私有分区不存在或已经删除");
        }
        String label = list.get(0).getLabel();
        return ActionReturnUtil.returnSuccessWithData(label);
    }

}
