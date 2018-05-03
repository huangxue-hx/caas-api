package com.harmonycloud.service.tenant.impl;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dao.tenant.NamespaceLocalMapper;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.NamespaceLocalExample;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.tenant.*;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.COMMA;


/**
 * Created by zgl on 17-12-7.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class NamespaceLocalServiceImpl implements NamespaceLocalService {


    @Autowired
    private NamespaceLocalMapper namespaceLocalMapper;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private RoleLocalService roleLocalService;
    @Autowired
    private UserService userService;
    @Autowired
    private HarborProjectService harborProjectService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * 创建namespace
     *
     * @param namespaceLocal
     * @return
     */
    @Override
    public void createNamespace(NamespaceLocal namespaceLocal) throws Exception {
        //设置namespaces id
        namespaceLocal.setNamespaceId(this.getid());
        String clusterId = namespaceLocal.getClusterId();
        Cluster cluster = this.clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)){
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND,clusterId,Boolean.TRUE);
        }
        namespaceLocal.setClusterName(cluster.getName());
        namespaceLocal.setClusterAliasName(cluster.getAliasName());
        //设置数据库
        namespaceLocalMapper.insertSelective(namespaceLocal);
    }
    private String getid() {
        // 通过uuid生成token
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        // 去掉"-"符号
        String id = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);
        return id;
    }

    /**
     * 根据id删除分区
     *
     * @param id
     * @throws Exception
     */
    @Override
    public void deleteNamespaceById(Integer id) throws Exception {
        NamespaceLocal namespaceLocal = this.namespaceLocalMapper.selectByPrimaryKey(id);
        if (!Objects.isNull(namespaceLocal)){
            this.namespaceLocalMapper.deleteByPrimaryKey(id);
        }
    }

    /**
     * 删除namespace
     *
     * @param namespaceLocal
     * @return
     */
    @Override
    public void deleteNamespace(NamespaceLocal namespaceLocal) throws Exception {
        //删除分区
        this.namespaceLocalMapper.deleteByPrimaryKey(namespaceLocal.getId());
    }

    @Override
    public NamespaceLocal getNamespaceByTenantIdAndName(String tenantId, String namespace) throws Exception {
        //查询要删除的分区
        NamespaceLocalExample example = new NamespaceLocalExample();
        example.createCriteria().andTenantIdEqualTo(tenantId).andNamespaceNameEqualTo(namespace);
        List<NamespaceLocal> namespaceLocals = namespaceLocalMapper.selectByExample(example);
        //不存在提示返回
        if (CollectionUtils.isEmpty(namespaceLocals)){
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_NOT_FOUND);
        }
        return namespaceLocals.get(0);
    }

    /**
     * 查询namespace列表
     *
     * @param tenantId
     * @return
     */
    @Override
    public List<NamespaceLocal> getNamespaceListByTenantId(String tenantId) throws Exception {
        NamespaceLocalExample example = this.getExample();
        //获取当前角色id
        Integer roleId = userService.getCurrentRoleId();
        //获取当前角色的作用域
        List<Cluster> clusterList = this.roleLocalService.getClusterListByRoleId(roleId);
        if (CollectionUtils.isEmpty(clusterList)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_HAVE_DISABLE_CLUSTER);
        }
        List<String> clusterIds = clusterList.stream().map(Cluster::getId).collect(Collectors.toList());
        //根据作用域筛选分区列表
        example.createCriteria().andTenantIdEqualTo(tenantId).andClusterIdIn(clusterIds);
        List<NamespaceLocal> namespaceLocals = this.namespaceLocalMapper.selectByExample(example);
        return namespaceLocals;
    }

    /**
     * 根据tenantId查询namespace列表(查询所有集群的分区)
     *
     * @param tenantId
     * @return
     */
    @Override
    public List<NamespaceLocal> getAllNamespaceListByTenantId(String tenantId) throws Exception {
        NamespaceLocalExample example = this.getExample();
        List<String> disableClusterIds = clusterService.listDisableClusterIds();
        Integer currentRoleId = this.userService.getCurrentRoleId();
        Role role = this.roleLocalService.getRoleById(currentRoleId);
        if (Objects.isNull(role)){
            return null;
        }
        String clusterIds = role.getClusterIds();
        NamespaceLocalExample.Criteria criteria = example.createCriteria();
        List<String> clusterIdList = null;
        if (StringUtils.isNotBlank(clusterIds)){
            clusterIdList = Arrays.stream(clusterIds.split(COMMA)).collect(Collectors.toList());
        } else {
            List<Cluster> clusterList = clusterService.listCluster();
            clusterIdList = clusterList.stream().map(cluster -> cluster.getId()).collect(Collectors.toList());
        }
        //过滤角色作用域
        if (!CollectionUtils.isEmpty(clusterIdList)){
            criteria.andClusterIdIn(clusterIdList);
        }
        //过滤不可用集群的分区
        if(!CollectionUtils.isEmpty(disableClusterIds)){
            criteria.andTenantIdEqualTo(tenantId).andClusterIdNotIn(disableClusterIds);
        }else{
            criteria.andTenantIdEqualTo(tenantId);
        }
        List<NamespaceLocal> namespaceLocals = this.namespaceLocalMapper.selectByExample(example);
        return namespaceLocals;
    }

    @Override
    public List<NamespaceLocal> getAllNamespaceListByTenantIdWithMSF(String tenantId) throws Exception {
        NamespaceLocalExample example = this.getExample();
        List<String> disableClusterIds = clusterService.listDisableClusterIds();
        NamespaceLocalExample.Criteria criteria = example.createCriteria();
        //过滤不可用集群的分区
        if(!CollectionUtils.isEmpty(disableClusterIds)){
            criteria.andTenantIdEqualTo(tenantId).andClusterIdNotIn(disableClusterIds);
        }else{
            criteria.andTenantIdEqualTo(tenantId);
        }
        List<NamespaceLocal> namespaceLocals = this.namespaceLocalMapper.selectByExample(example);
        return namespaceLocals;
    }

    @Override
    public List<NamespaceLocal> getAllPublicNamespaceListByTenantId(String tenantId) throws Exception {
        NamespaceLocalExample example = this.getExample();
        List<String> disableClusterIds = clusterService.listDisableClusterIds();
        //过滤不可用集群的分区
        if(!CollectionUtils.isEmpty(disableClusterIds)){
            example.createCriteria().andTenantIdEqualTo(tenantId).andIsPrivateEqualTo(Boolean.FALSE).andClusterIdNotIn(disableClusterIds);
        }else{
            example.createCriteria().andTenantIdEqualTo(tenantId).andIsPrivateEqualTo(Boolean.FALSE);
        }
        List<NamespaceLocal> namespaceLocals = this.namespaceLocalMapper.selectByExample(example);
        return namespaceLocals;
    }
    /**
     * 根据tenantId,clusterId查询namespace列表
     * @param tenantId
     * @param clusterIds
     * @return
     * @throws Exception
     */
    public List<NamespaceLocal> getNamespaceListByTenantIdAndClusterId(String tenantId, List<String> clusterIds) throws Exception{
        Map<String,Cluster> userClusters = userService.getCurrentUserCluster();
        List<String> clusters = clusterIds.stream().filter(clusterId -> userClusters.get(clusterId) != null).collect(Collectors.toList());
        NamespaceLocalExample example = this.getExample();
        example.createCriteria().andTenantIdEqualTo(tenantId).andClusterIdIn(clusters);
        List<NamespaceLocal> namespaceLocals = this.namespaceLocalMapper.selectByExample(example);
        //添加结果集群返回值
        if (!CollectionUtils.isEmpty(namespaceLocals)){
            for (NamespaceLocal namespaceLocal:namespaceLocals) {
                String currentClusterId = namespaceLocal.getClusterId();
                Cluster cluster = clusterService.findClusterById(currentClusterId);
                namespaceLocal.setClusterAliasName(cluster.getAliasName());
            }
        }
        return namespaceLocals;
    }

    @Override
    public List<NamespaceLocal> getNamespaceListByRepositoryId(String tenantId, Integer repositoryId) throws Exception {
        AssertUtil.notBlank(tenantId, DictEnum.TENANT_ID);
        AssertUtil.notNull(repositoryId, DictEnum.TENANT_ID);
        ImageRepository imageRepository = harborProjectService.findRepositoryById(repositoryId);
        if(imageRepository == null){
            throw new MarsRuntimeException(ErrorCodeMessage.NOT_FOUND,DictEnum.REPOSITORY.phrase());
        }
        List<String> clusterIds = new ArrayList<>();
        if(imageRepository.isPublic()) {
            HarborServer harborServer = clusterService.findHarborByHost(imageRepository.getHarborHost());
            String[] clusterIdArray = harborServer.getReferredClusterIds().split(COMMA);
            clusterIds.addAll( Arrays.asList(clusterIdArray));
        }else{
            clusterIds.add(imageRepository.getClusterId());
        }
        return this.getNamespaceListByTenantIdAndClusterId(tenantId,clusterIds);
    }

    /**
     * 根据namespace name查询namespace name
     *
     * @param namespaceName
     * @return
     */
    @Override
    public NamespaceLocal getNamespaceByName(String namespaceName) throws Exception {
        NamespaceLocalExample example = this.getExample();
        example.createCriteria().andNamespaceNameEqualTo(namespaceName);
        List<NamespaceLocal> namespaceLocals = this.namespaceLocalMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(namespaceLocals)){
            return namespaceLocals.get(0);
        }
        return null;
    }

    /**
     * 根据namespace aliasName查询namespace
     *
     * @param aliasName
     * @return
     * @throws Exception
     */
    @Override
    public NamespaceLocal getNamespaceByAliasName(String aliasName) throws Exception {
        NamespaceLocalExample example = this.getExample();
        example.createCriteria().andAliasNameEqualTo(aliasName);
        List<NamespaceLocal> namespaceLocals = this.namespaceLocalMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(namespaceLocals)){
            return namespaceLocals.get(0);
        }
        return null;
    }

    /**
     * 根据namespace name 获取集群
     *
     * @param namespaceName
     * @return
     * @throws Exception
     */
    @Override
    public Cluster getClusterByNamespaceName(String namespaceName) throws Exception {
        NamespaceLocal namespace = this.getNamespaceByName(namespaceName);
        if (Objects.isNull(namespace)){
            return null;
        }
        Cluster cluster = clusterService.findClusterById(namespace.getClusterId());
        if (Objects.isNull(cluster)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_HAVE_DISABLE_CLUSTER);
        }
        return cluster;
    }

    /**
     * 根据clusterId查询namespace列表
     *
     * @param clusterId
     * @return
     */
    @Override
    public List<NamespaceLocal> getNamespaceListByClusterId(String clusterId) throws Exception {
        NamespaceLocalExample example = this.getExample();
        example.createCriteria().andClusterIdEqualTo(clusterId);
        List<NamespaceLocal> namespaceLocals = this.namespaceLocalMapper.selectByExample(example);
        return namespaceLocals;
    }
    @Override
    public List<NamespaceLocal> getPublicNamespaceListByClusterId(String clusterId) throws Exception {
        NamespaceLocalExample example = this.getExample();
        example.createCriteria().andClusterIdEqualTo(clusterId).andIsPrivateEqualTo(Boolean.FALSE);
        List<NamespaceLocal> namespaceLocals = this.namespaceLocalMapper.selectByExample(example);
        return namespaceLocals;
    }
    @Override
    public int deleteByClusterId(String clusterId){
        return namespaceLocalMapper.deleteByClusterId(clusterId);
    }

    /**
     * 根据namespace id查询namespace name
     *
     * @param namespaceId
     * @return
     */
    @Override
    public NamespaceLocal getNamespaceByNamespaceId(String namespaceId) throws Exception {
        NamespaceLocalExample example = this.getExample();
        example.createCriteria().andNamespaceIdEqualTo(namespaceId);
        List<NamespaceLocal> namespaceLocals = this.namespaceLocalMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(namespaceLocals)){
            return namespaceLocals.get(0);
        }
        return null;
    }
    private  NamespaceLocalExample getExample(){
        return  new NamespaceLocalExample();
    }
}
