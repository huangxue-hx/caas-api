package com.harmonycloud.service.tenant;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.tenant.bean.Tenant;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.bean.UserRoleRelationship;
import com.harmonycloud.dto.cluster.IngressControllerDto;
import com.harmonycloud.dto.tenant.CDPUserDto;
import com.harmonycloud.dto.tenant.ClusterQuotaDto;
import com.harmonycloud.dto.tenant.TenantDto;
import com.harmonycloud.service.platform.bean.NodeDto;

import java.util.List;
import java.util.Map;

/**
 * 租户业务接口
 * Created by andy on 17-1-10.
 */
public interface TenantService {

    /**
     * 查询当前用户（session里面）的租户列表(返回前台租户列表数据)
     * @return
     * @throws Exception
     */
    public List<TenantDto> tenantList() throws Exception;

    List<TenantDto> listTenantBrief() throws Exception;

    /**
     * 切换租户
     * @param tenantId
     * @return
     * @throws Exception
     */
    public Map<String,Object> switchTenant(String tenantId) throws Exception;

    /**
     * 设置当前租户，项目，角色信息
     */
    Map<String,Object> setCurrentTenant(String tenantId, String tenantAliasName, String username, boolean isAdmin) throws Exception;
    /**
     * 根据用户名查询租户列表(返回租户表数据)
     * @return
     * @throws Exception
     */
    public List<TenantBinding> tenantListByUsernameInner(String username) throws Exception;

    /**
     * 根据id查询租户详情
     * @param tenantid
     * @return
     */
    public TenantDto getTenantDetail(String tenantid) throws Exception;

    /**
     * 根据租户名称查询租户详情
     * @param tenantName
     * @return
     */
    public TenantDto getTenantDetailByTenantName(String tenantName) throws Exception;

    /**
     * 创建租户
     * @return
     */
    public String createTenant(TenantDto tenantDto) throws Exception;

    /**
     * 根据id删除租户信息
     * @param tenantid
     * @return
     */
    public void deleteTenantByTenantId(String tenantid) throws Exception;

    /**
     * 修改租户在集群下的配额
     * @param tenantId
     * @param clusterQuota
     * @throws Exception
     */
    public void updateTenant(String tenantId,List<ClusterQuotaDto> clusterQuota) throws Exception;
    /**
     * 修改租户
     * @param tenantDto
     * @throws Exception
     */
    public void updateTenant(TenantDto tenantDto) throws Exception;
    public List<NodeDto> getTenantPrivateNodeList(String tenantid,String namespace,String clusterId) throws Exception;

    /**
     * 根据租户id查询租户
     * @param tenantid
     * @return
     */
    public TenantBinding getTenantByTenantid(String tenantid) throws Exception;

    /**
     * 根据集群id，租户id获取该租户还可以使用的存储资源
     * @param clusterId
     * @return
     * @throws Exception
     */
    public Map<String, Integer> getStorageClassUnused(String tenantId, String clusterId) throws Exception;

    /**
     * 查询tenant下所有Tm用户
     * @param tenantid
     * @return
     * @throws Exception
     */
    public List<UserRoleRelationship> listTenantTm(String tenantid) throws Exception;
    /**
     * 查询是否为tm
     * @param tenantid
     * @return
     * @throws Exception
     */
    public Boolean isTm(String tenantid) throws Exception;
    /**
     * 根据租户name查询租户
     * @param tenantName
     * @return
     * @throws Exception
     */
    public TenantBinding getTenantBytenantName(String tenantName) throws Exception;
    /**
     * 向租户下添加租户管理员
     * @return
     * @throws Exception
     */
    public void createTm(String tenantId, List<String> tmList) throws Exception;

    /**
     * 向租户下移除租户管理员
     * @param tenantid
     * @param username
     * @throws Exception
     */
    public void deleteTm(String tenantid, String username) throws Exception;

    /**
     * 列出tenan下面的资源使用情况
     * @param tenantid
     * @return
     * @throws Exception
     */
    public Map<String, Object> listTenantQuota(String tenantid)  throws Exception;

    /**
     * 根据namespaces列表获取分区资源使用量
     * @param namespaceDataList
     * @return
     * @throws Exception
     */
    public Map<String, Object> getTotalQuotaByNamespaceDataList(List<Map<String, Object>> namespaceDataList) throws Exception;

//    ActionReturnUtil listTenantUsers(String tenantname) throws Exception;


    /**
     * 根据租户名获取该租户下所有的用户
     * @param tenantName
     * @return
     * @throws Exception
     */
    public List<String> listUserByTenantName(String tenantName)throws Exception;

    /**
     * 根据用户名称获取该用户所属的所有租户
     * @param userName
     * @return
     * @throws Exception
     */
    public List<TenantBinding> listTenantsByUserName(String userName) throws Exception ;

    /**
     * 根据租户id获取租户成员
     * @param tenantId
     * @return
     * @throws Exception
     */
    public List<User> listTenantMember(String tenantId) throws Exception ;

    /**
     * 生成租户下拉列表专用
     * @param userName
     * @param isAdmin
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listTenantsByUserNameForAudit(String userName, boolean isAdmin) throws Exception ;
    /**
     * 获取所有租户列表
     * @return
     * @throws Exception
     */
    public List<TenantBinding> listAllTenant() throws Exception;

    /**
     *给新集群的添加租户配额
     * @param clusterId
     * @return
     * @throws Exception
     */
    public void dealQuotaWithNewCluster(String clusterId) throws Exception;

    /**
     * 给要开启的集群恢复租户配额
     * @param clusterId
     * @throws Exception
     */
    public void dealQuotaWithNormalCluster(String clusterId) throws Exception;
    /**
     *给要暂停的集群软删除租户配额
     * @param clusterId
     * @return
     * @throws Exception
     */
    public void dealQuotaWithPauseCluster(String clusterId) throws Exception;
    /**
     *  导入租户项目
     * @param tenantDto
     * @return
     * @throws Exception
     */
    public void importCdsSystem(TenantDto tenantDto) throws Exception;

    /**
     * 导入CDP用户
     * @param cdpUserDto
     * @return
     * @throws Exception
     */
    public void importCdsUserAccount(CDPUserDto cdpUserDto) throws Exception;

    /**
     * 导入CDP项目用户关系
     * @param cdpUserDto
     * @return
     * @throws Exception
     */
    public void importCdsUserRelationship(CDPUserDto cdpUserDto) throws Exception;

    /**
     * 删除CDP项目用户关系
     * @param cdpUserDto
     * @return
     * @throws Exception
     */
    public void removeCdsUserRelationship(CDPUserDto cdpUserDto) throws Exception;

    /**
     * 添加租户成员
     * @param tenantId
     * @param addUsers
     * @param deleteUsers
     * @throws Exception
     */
    public void updateTenantMember(String tenantId, List <String> addUsers, List <String> deleteUsers) throws Exception;
    public List<TenantBinding> testTime(Integer domain) throws Exception;

    /**
     * 修改租户策略
     * @param tenantId
     * @param strategy
     */
    public void updateTenantStrategy(String tenantId,Integer strategy) throws Exception;

    /**
     * 移除租户在集群下的配额
     * @param tenantId
     * @param clusterQuota
     * @throws Exception
     */
    public void removeClusterQuota(String tenantName, String tenantId, ClusterQuotaDto clusterQuota) throws Exception;

    /**
     * 查询某个租户在某个集群下的所有可用负载均衡器
     * 包括全局负载均衡器和分配给租户的自定义负载均衡器
     */
    List<IngressControllerDto> getTenantIngressController(String tenantId, String clusterId) throws Exception;

    List<Tenant> queryTenantByClusterId(String clusterId) throws Exception;
}
