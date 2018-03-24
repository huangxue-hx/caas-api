package com.harmonycloud.service.tenant;
import com.harmonycloud.dao.tenant.bean.TenantPrivateNode;
import java.util.List;

/**
 * Created by zgl on 18-2-8.
 */


/**
 * 租户独占主机业务接口
 */
public interface TenantPrivateNodeService {

    /**
     * 根据租户id查询集群下的主机
     * @return
     * @throws Exception
     */
    public List<TenantPrivateNode> listTenantPrivateNode(String tenantId, String clusterId) throws Exception;

    /**
     * 根据租户id查询主机
     * @param tenantId
     * @return
     * @throws Exception
     */
    public List<TenantPrivateNode> listTenantPrivateNode(String tenantId) throws Exception;
    /**
     * 根据id查询 TenantPrivateNode
     * @param id
     * @return
     * @throws Exception
     */
    public TenantPrivateNode getTenantPrivateNode(Integer id) throws Exception;

    /**
     * 根据租户节点名查询集群下的独占主机
     * @param tenantId
     * @param clusterId
     * @param nodeName
     * @return
     * @throws Exception
     */
    public TenantPrivateNode getTenantPrivateNode(String tenantId,String clusterId,String nodeName) throws Exception;

    /**
     * 创建TenantPrivateNode
     * @param tenantPrivateNode
     * @throws Exception
     */
    public void createTenantPrivateNode(TenantPrivateNode tenantPrivateNode) throws Exception;

    /**
     * 更新TenantPrivateNode
     * @param tenantPrivateNode
     * @throws Exception
     */
    public void updateTenantPrivateNode(TenantPrivateNode tenantPrivateNode) throws Exception;
    /**
     * 根据id删除租户下主机
     * @param id
     * @throws Exception
     */
    public void deleteTenantPrivateNode(int id) throws Exception;
    /**
     * 根据租户id删除租户下独占主机
     * @param tenantId
     * @throws Exception
     */
    public void deleteTenantPrivateNode(String tenantId) throws Exception;

    /**
     * 根据集群id,节点名删除集群配额列表
     * @param clusterId
     * @throws Exception
     */
    public void deleteTenantPrivateNode(String tenantId,String clusterId,String nodeName) throws Exception;

}
