package com.harmonycloud.service.tenant.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.dao.tenant.NamespaceLocalMapper;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.NamespaceLocalExample;
import com.harmonycloud.dao.user.bean.Privilege;
import com.harmonycloud.dto.cluster.ErrorNamespaceDto;
import com.harmonycloud.k8s.bean.Namespace;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.NodeService;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.RolePrivilegeService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.COMMA;


/**
 * Created by zgl on 17-12-7.
 */
@Service
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
    @Autowired
    private RolePrivilegeService rolePrivilegeService;
    @Autowired
    private com.harmonycloud.k8s.service.NamespaceService ns;
    @Autowired
    private NodeService nodeService;

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
        namespaceLocal.setNamespaceId(UUIDUtil.get16UUID());
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
        //获取当前角色的作用域
        List<Cluster> clusterList = this.roleLocalService.listCurrentUserRoleCluster();
        if (CollectionUtils.isEmpty(clusterList)){
            return Collections.emptyList();
        }
        List<String> clusterIds = clusterList.stream().map(Cluster::getId).collect(Collectors.toList());
        //根据作用域筛选分区列表
        example.createCriteria().andTenantIdEqualTo(tenantId).andClusterIdIn(clusterIds);
        List<NamespaceLocal> namespaceLocals = this.namespaceLocalMapper.selectByExample(example);
        //将分区istio状态返回
        if(!CollectionUtils.isEmpty(namespaceLocals)){
            for(NamespaceLocal namespaceLocal:namespaceLocals){
                String currentClusterId = namespaceLocal.getClusterId();
                boolean istioStatus = this.getNamespaceIstioStatus(namespaceLocal.getNamespaceName(), currentClusterId);
                namespaceLocal.setIstioStatus(istioStatus);
                if(namespaceLocal.getIsPrivate()){
//                  List<NodeDto> nodeDtos = nodeService.listNodeByNamespaces(namespaceLocal.getNamespaceName());
                    List<NamespaceLocal> namespaceLocalList= getSimpleNamespaceListByTenantId(namespaceLocal.getTenantId(),namespaceLocal.getClusterId());
                    for(NamespaceLocal namespaceLocalTemp : namespaceLocalList){
                        if(namespaceLocalTemp.getIsGpu()){
                            namespaceLocal.setIsGpu(true);
                            break;
                        }
                    }
                }
            }
        }
        return namespaceLocals;
    }

    /**
     * 查询namespace列表，不返回Istio状态以及私有分区信息
     *
     * @param tenantId
     * @return
     */
    @Override
    public List<NamespaceLocal> getSimpleNamespaceListByTenantId(String tenantId, String clusterId) throws Exception {
        NamespaceLocalExample example = this.getExample();
        if (StringUtils.isBlank(clusterId)) {
            //获取当前角色的作用域
            List<Cluster> clusterList = this.roleLocalService.listCurrentUserRoleCluster();
            if (CollectionUtils.isEmpty(clusterList)){
                return Collections.emptyList();
            }
            List<String> clusterIds = clusterList.stream().map(Cluster::getId).collect(Collectors.toList());
            //根据作用域筛选分区列表
            example.createCriteria().andTenantIdEqualTo(tenantId).andClusterIdIn(clusterIds);
        } else {
            example.createCriteria().andTenantIdEqualTo(tenantId).andClusterIdEqualTo(clusterId);
        }
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
        NamespaceLocalExample.Criteria criteria = example.createCriteria();
        List<String> clusterIdList = roleLocalService.listCurrentUserRoleClusterIds();
        //过滤角色作用域
        if (CollectionUtils.isEmpty(clusterIdList)){
            return Collections.emptyList();
        }
        criteria.andClusterIdIn(clusterIdList);
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
        if (CollectionUtils.isEmpty(clusterIds)) {
            return Collections.emptyList();
        }
        List<String> clusters = null;
        if (clusterIds.size() == 1) {
            clusters = clusterIds;
        } else {
            Map<String, Cluster> userClusters = userService.getCurrentUserCluster();
            clusters = clusterIds.stream().filter(clusterId -> userClusters.get(clusterId) != null).collect(Collectors.toList());
        }
        NamespaceLocalExample example = this.getExample();
        example.createCriteria().andTenantIdEqualTo(tenantId).andClusterIdIn(clusters);
        List<NamespaceLocal> namespaceLocals = this.namespaceLocalMapper.selectByExample(example);
        //添加结果集群返回值
        if (!CollectionUtils.isEmpty(namespaceLocals)){
            for (NamespaceLocal namespaceLocal:namespaceLocals) {
                String currentClusterId = namespaceLocal.getClusterId();
                Cluster cluster = clusterService.findClusterById(currentClusterId);
                namespaceLocal.setClusterAliasName(cluster.getAliasName());
                //添加分区开关字段
                boolean  istioStatus = getNamespaceIstioStatus(namespaceLocal.getNamespaceName(), currentClusterId);
                namespaceLocal.setIstioStatus(istioStatus);
            }
        }
        return namespaceLocals;
    }

    @Override
    public List<NamespaceLocal> listNamespace(String tenantId, String clusterId) throws Exception {
        List<String> clusterIds = new ArrayList<>();
        clusterIds.add(clusterId);
        return this.getNamespaceListByTenantIdAndClusterId(tenantId, clusterIds);
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
    public NamespaceLocal getNamespace(String aliasName, String tenantId) throws Exception {
        NamespaceLocalExample example = this.getExample();
        example.createCriteria().andAliasNameEqualTo(aliasName).andTenantIdEqualTo(tenantId);
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

    /**
     * 更新分区
     *
     * @param namespaceLocal
     * @throws Exception
     */
    @Override
    public void updateNamespace(NamespaceLocal namespaceLocal) throws Exception {
        this.namespaceLocalMapper.updateByPrimaryKeySelective(namespaceLocal);
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

    @Override
    public NamespaceLocal getKubeSystemNamespace() throws Exception {
        NamespaceLocal namespaceLocal = null;
        Integer roleId = userService.getCurrentRoleId();

        Map<String, Object> privilegeMap = rolePrivilegeService.getAvailablePrivilegeByRoleId(roleId);
        if(privilegeMap.get(CommonConstant.APPCENTER) != null){
            List<Privilege> privileges = (List<Privilege>)((Map)privilegeMap.get(CommonConstant.APPCENTER)).get(CommonConstant.DAEMONSET);
            if(privileges != null) {
                Privilege daemonsetPrivilege = privileges.stream().filter(privilege -> privilege.getStatus()).findAny().orElse(null);
                if (daemonsetPrivilege != null) {
                    namespaceLocal = new NamespaceLocal();
                    namespaceLocal.setNamespaceName(CommonConstant.KUBE_SYSTEM);
                    namespaceLocal.setAliasName(CommonConstant.KUBE_SYSTEM);
                }
            }
        }
        return namespaceLocal;
    }

    /**
     *
     * @param name
     * @param clusterId
     * @return
     * @throws Exception
     */
    @Override
    public NamespaceLocal getNamespaceByNameAndClusterId(String name,String  clusterId) throws Exception {
        NamespaceLocalExample example = this.getExample();
        example.createCriteria().andNamespaceNameEqualTo(name).andClusterIdEqualTo(clusterId);
        List<NamespaceLocal> namespaceLocals = this.namespaceLocalMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(namespaceLocals)){
            return namespaceLocals.get(0);
        }
        return null;

    }
    //获取分区开关istio状态
    public boolean  getNamespaceIstioStatus(String namespace, String  clusterId) throws  Exception{
        //获取集群信息
        Cluster  cluster = new Cluster();
        if(StringUtils.isNotBlank(clusterId)) {
            cluster = clusterService.findClusterById(clusterId);
        } else {
            cluster = this.getClusterByNamespaceName(namespace);
        }
        //获取该集群下指定分区是否开启自动注入
        K8SClientResponse response = ns.getNamespace(namespace, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_NOT_FOUND);
        }
        Namespace namespaceDetail = JsonUtil.jsonToPojo(response.getBody(), Namespace.class);
        String istioInjectionValue = "";
        Map<String, Object> labels = namespaceDetail.getMetadata().getLabels();
        if (Objects.nonNull(labels) && Objects.nonNull(labels.get(CommonConstant.ISTIO_INJECTION))) { //防止分区信息无label
            istioInjectionValue = labels.get(CommonConstant.ISTIO_INJECTION).toString();
        }
        boolean istioStatus = CommonConstant.OPEN_ISTIO_AUTOMATIC_INJECTION.equals(istioInjectionValue);
        return  istioStatus;
    }

    @Override
    public ErrorNamespaceDto createTransferNamespace(NamespaceLocal namespaceLocal) {
        ErrorNamespaceDto sussNamespaceDto = new ErrorNamespaceDto();
        //设置namespaces id
        namespaceLocal.setNamespaceId(StringUtil.getId());
        String clusterId = namespaceLocal.getClusterId();
        Cluster cluster = this.clusterService.findClusterById(clusterId);
        namespaceLocal.setClusterName(cluster.getName());
        namespaceLocal.setClusterAliasName(cluster.getAliasName());
        //设置数据库
        namespaceLocalMapper.insertSelective(namespaceLocal);
        sussNamespaceDto.setNamespace(namespaceLocal.getNamespaceName());
        return sussNamespaceDto;
    }

    /**
     * 根据clusterIds获取所有NamespaceLocal
     * @return
     * @throws MarsRuntimeException
     */
    @Override
    public List<NamespaceLocal> getNamespaceByClsterIds(List<String> clusterIds) throws MarsRuntimeException {
        NamespaceLocalExample example = this.getExample();
        example.createCriteria().andClusterIdIn(clusterIds);
        return this.namespaceLocalMapper.selectByExample(example);
    }

}
