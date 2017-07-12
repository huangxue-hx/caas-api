package com.harmonycloud.service.tenant;

import java.util.List;
import java.util.Map;

/**
 * Created by andy on 17-1-10.
 */

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dto.tenant.TenantDto;

import org.springframework.web.bind.annotation.RequestParam;


/**
 * 租户业务接口
 */
public interface TenantService {

    /**
     * 根据用户名查询租户列表
     * @param username
     * @return
     */
    public ActionReturnUtil tenantList(String username ,Integer clusterId) throws Exception;
    /**
     * 根据clusterid查询租户列表
     * @param username
     * @return
     */
    public ActionReturnUtil listTenantByClusterId(Integer clusterid) throws Exception;
    /**
     * 查询所有租户列表
     * @return
     */
    public ActionReturnUtil tenantAlllist() throws Exception;

    /**
     * 根据id查询租户详情
     * @param tenantid
     * @return
     */
    public ActionReturnUtil tenantdetail(String tenantid) throws Exception;

    /**
     * 根据租户名称查询租户详情
     * @param tenantName
     * @return
     */
    public ActionReturnUtil tenantdetailByName(String tenantName) throws Exception;

    /**
     * 创建租户
     * @param name
     * @param annotation
     * @param user
     * @return
     */
    public ActionReturnUtil tenantcreate(String name, String annotation, String user,Integer cluster) throws Exception;

    /**
     * 根据id删除租户信息
     * @param tenantid
     * @return
     */
    public ActionReturnUtil tenantdelete(String tenantid) throws Exception;

    /**
     * 查询namespace详情
     * @param tenantid
     * @return
     */
    public ActionReturnUtil getSmplTenantDetail(String tenantid) throws Exception;

    /**
     * 查询namespace下用户列表
     * @param tenantname
     * @param namespace
     * @return
     */
    public ActionReturnUtil getNamespaceUserList(String tenantname, String namespace) throws Exception;

    /**
     * 根据租户id查询租户
     * @param tenantid
     * @return
     */
    public TenantBinding getTenantByTenantid(String tenantid) throws Exception;
    
    /**
     * 查询tenant下所有用户
     * @param tenantid
     * @return
     */
    public ActionReturnUtil listTenantUsers(String tenantid) throws Exception;
    /**
     * 查询tenant下所有Tm用户
     * @param tenantid
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listTenantTm(String tenantid) throws Exception;
    /**
     * 根据租户name查询租户
     * @param tenantName
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listTenantBytenantName(String tenantName) throws Exception;
    /**
     * 向租户下添加user
     * @param tenantid
     * @param username
     * @param role
     * @return
     * @throws Exception
     */
    public ActionReturnUtil addTenantUser(String tenantid, String username,String role) throws Exception;
    /**
     * 向租户下移除user
     * @param tenantid
     * @param username
     * @return
     * @throws Exception
     */
    public ActionReturnUtil removeTenantUser(String tenantid, String username) throws Exception;
    /**
     * 添加信任白名单
     * @param tenantid
     * @param trustTenantid
     * @return
     * @throws Exception
     */
    public ActionReturnUtil addTrustmember(String tenantid, String trustTenantid) throws Exception;
    /**
     * 移除信任白名单
     * @param tenantid
     * @param trustTenantid
     * @return
     * @throws Exception
     */
    public ActionReturnUtil removeTrustmember(String tenantid, String trustTenantid) throws Exception;
    /**
     * 获取信任白名单列表
     * @param tenantid
     * @return
     */
    public List<TenantDto> listTrustmember(String tenantid)  throws Exception;
    /**
     * 查询可添加信任白名单租户列表
     * @param tenantid
     * @return
     * @throws Exception
     */
    public List<TenantBinding> listAvailableTrustmemberTenantList(String tenantid)  throws Exception;
    /**
     * 列出tenan下面的资源使用情况
     * @param tenantid
     * @return
     * @throws Exception
     */
    public Map<String, Object> listTenantQuota(String tenantid)  throws Exception;

//    ActionReturnUtil listTenantUsers(String tenantname) throws Exception;


    /**
     * 根据租户名获取该租户下所有的用户
     * @param tenantName
     * @return
     * @throws Exception
     */
    public List<String> findByTenantName(String tenantName)throws Exception;

    /**
     * 根据用户名称获取该用户所属的所有租户
     * @param userName
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listTenantsByUserName(String userName, boolean isAdmin) throws Exception ;
    /**
     * 根据tenantid获取集群
     * @param tenantid
     * @return
     * @throws Exception
     */
    public Cluster getClusterByTenantid(String tenantid) throws Exception;

    /**
     * 生成租户下拉列表专用
     * @param userName
     * @param isAdmin
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listTenantsByUserNameForAudit(String userName, boolean isAdmin) throws Exception ;
    /**
     * 根据clusterid获取已使用配额
     * @param clusterId
     * @return
     * @throws Exception
     */
    public Map getTenantQuotaByClusterId(String clusterId) throws Exception;
    /**
     * 根据用户名查询是否是admin或者租户管理员
     * @param tenantid
     * @param username
     * @return
     * @throws Exception
     */
    public boolean isAdmin(String tenantid,String username) throws Exception;
    public List<TenantBinding> testTime(Integer domain) throws Exception;

}
