package com.harmonycloud.service.tenant.impl;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.tenant.TenantPrivateNodeMapper;
import com.harmonycloud.dao.tenant.bean.TenantPrivateNode;
import com.harmonycloud.dao.tenant.bean.TenantPrivateNodeExample;
import com.harmonycloud.service.tenant.TenantPrivateNodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.*;

/**
 * Created by zgl on 18-2-9.
 */

@Service
public class TenantPrivateNodeServiceImpl implements TenantPrivateNodeService {

    @Autowired
    private TenantPrivateNodeMapper tenantPrivateNodeMapper;

    private static final Logger logger = LoggerFactory.getLogger(TenantPrivateNodeServiceImpl.class);


    private TenantPrivateNodeExample getExample(){
        return  new TenantPrivateNodeExample();
    }

    /**
     * 根据租户id查询集群下的主机
     *
     * @param tenantId
     * @param clusterId
     * @return
     * @throws Exception
     */
    @Override
    public List<TenantPrivateNode> listTenantPrivateNode(String tenantId, String clusterId) throws Exception {
        TenantPrivateNodeExample example = this.getExample();
        example.createCriteria().andTenantIdEqualTo(tenantId).andClusterIdEqualTo(clusterId);
        List<TenantPrivateNode> tenantPrivateNodes = this.tenantPrivateNodeMapper.selectByExample(example);
        return tenantPrivateNodes;
    }

    /**
     * 根据租户id查询主机
     *
     * @param tenantId
     * @return
     * @throws Exception
     */
    @Override
    public List<TenantPrivateNode> listTenantPrivateNode(String tenantId) throws Exception {
        TenantPrivateNodeExample example = this.getExample();
        example.createCriteria().andTenantIdEqualTo(tenantId);
        List<TenantPrivateNode> tenantPrivateNodes = this.tenantPrivateNodeMapper.selectByExample(example);
        return tenantPrivateNodes;
    }

    /**
     * 根据id查询 TenantPrivateNode
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public TenantPrivateNode getTenantPrivateNode(Integer id) throws Exception {
        TenantPrivateNode tenantPrivateNode = this.tenantPrivateNodeMapper.selectByPrimaryKey(id);
        return tenantPrivateNode;
    }

    /**
     * 根据租户节点名查询集群下的独占主机
     *
     * @param tenantId
     * @param clusterId
     * @param nodeName
     * @return
     * @throws Exception
     */
    @Override
    public TenantPrivateNode getTenantPrivateNode(String tenantId, String clusterId, String nodeName) throws Exception {
        TenantPrivateNodeExample example = this.getExample();
        example.createCriteria().andTenantIdEqualTo(tenantId).andClusterIdEqualTo(clusterId).andNodeNameEqualTo(nodeName);
        List<TenantPrivateNode> privateNodes = this.tenantPrivateNodeMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(privateNodes)){
            return privateNodes.get(0);
        }
        return null;
    }

    /**
     * 创建TenantPrivateNode
     *
     * @param tenantPrivateNode
     * @throws Exception
     */
    @Override
    public void createTenantPrivateNode(TenantPrivateNode tenantPrivateNode) throws Exception {
        String tenantId = tenantPrivateNode.getTenantId();
        String clusterId = tenantPrivateNode.getClusterId();
        String nodeName = tenantPrivateNode.getNodeName();
        TenantPrivateNode privateNode = this.getTenantPrivateNode(tenantId, clusterId, nodeName);
        if (Objects.isNull(privateNode)){
            this.tenantPrivateNodeMapper.insertSelective(tenantPrivateNode);
        }
    }

    /**
     * 更新TenantPrivateNode
     *
     * @param tenantPrivateNode
     * @throws Exception
     */
    @Override
    public void updateTenantPrivateNode(TenantPrivateNode tenantPrivateNode) throws Exception {
        String tenantId = tenantPrivateNode.getTenantId();
        String clusterId = tenantPrivateNode.getClusterId();
        String nodeName = tenantPrivateNode.getNodeName();
        TenantPrivateNode privateNode = this.getTenantPrivateNode(tenantId, clusterId, nodeName);
        if (Objects.isNull(privateNode)){
            throw new MarsRuntimeException(ErrorCodeMessage.TENANT_NODE_NOT_EXIST);
        }
        this.tenantPrivateNodeMapper.updateByPrimaryKeySelective(tenantPrivateNode);
    }

    /**
     * 根据id删除租户下主机
     *
     * @param id
     * @throws Exception
     */
    @Override
    public void deleteTenantPrivateNode(int id) throws Exception {
        this.tenantPrivateNodeMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据租户id删除租户下独占主机
     *
     * @param tenantId
     * @throws Exception
     */
    @Override
    public void deleteTenantPrivateNode(String tenantId) throws Exception {
        TenantPrivateNodeExample example = this.getExample();
        example.createCriteria().andTenantIdEqualTo(tenantId);
        this.tenantPrivateNodeMapper.deleteByExample(example);
    }

    /**
     * 根据集群id,节点名删除集群配额列表
     *
     * @param tenantId
     * @param clusterId
     * @param nodeName
     * @throws Exception
     */
    @Override
    public void deleteTenantPrivateNode(String tenantId, String clusterId, String nodeName) throws Exception {
        TenantPrivateNode tenantPrivateNode = getTenantPrivateNode(tenantId, clusterId, nodeName);
        if (Objects.isNull(tenantPrivateNode)){
            throw new MarsRuntimeException(ErrorCodeMessage.TENANT_NODE_NOT_EXIST);
        }
        this.deleteTenantPrivateNode(tenantPrivateNode.getId());
    }
}
