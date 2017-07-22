package com.harmonycloud.service.cluster.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.ClusterMapper;
import com.harmonycloud.dao.cluster.NodeInstallProgressMapper;
import com.harmonycloud.dao.cluster.bean.NodeInstallProgress;
import com.harmonycloud.dao.cluster.bean.NodeInstallProgressExample;
import com.harmonycloud.service.cluster.NodeInstallProgressService;
@Service
@Transactional(rollbackFor = Exception.class)
public class NodeInstallProgressServiceImpl implements NodeInstallProgressService{
    @Autowired
    private NodeInstallProgressMapper nodeInstallProgressMapper;
    @Override
    public ActionReturnUtil addNodeInLineInfo(NodeInstallProgress nodeInstallProgress) throws Exception {
        nodeInstallProgressMapper.insertSelective(nodeInstallProgress);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil updateNodeInLineInfo(NodeInstallProgress nodeInstallProgress) throws Exception {
        nodeInstallProgressMapper.updateByPrimaryKeySelective(nodeInstallProgress);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil deleteNodeInLineInfo(String nodeIp) throws Exception {
        NodeInstallProgressExample example = new NodeInstallProgressExample();
        example.createCriteria().andNameEqualTo(nodeIp);
        nodeInstallProgressMapper.deleteByExample(example);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public NodeInstallProgress getNodeInLineInfoByNodeIp(String nodeIp) throws Exception {
        NodeInstallProgressExample example = new NodeInstallProgressExample();
        example.createCriteria().andNameEqualTo(nodeIp);
        List<NodeInstallProgress> list = nodeInstallProgressMapper.selectByExample(example);
        if(list!=null&&list.size()==1){
            return list.get(0);
        }
        return null;
    }
    
    @Override
    public List<NodeInstallProgress> getNodeInLineInfoByInstallStatusAndClusterId(String installStatus, String clusterId) throws Exception {
        NodeInstallProgressExample example = new NodeInstallProgressExample();
        example.createCriteria().andInstallStatusEqualTo(installStatus).andClusterIdEqualTo(Integer.parseInt(clusterId));
        List<NodeInstallProgress> list = nodeInstallProgressMapper.selectByExample(example);
        return list;
    }
    @Override
    public String getOnLineErrorStatus() throws Exception {
        NodeInstallProgressExample example = new NodeInstallProgressExample();
        example.createCriteria().andErrorMsgIsNotNull().andInstallStatusEqualTo("error");
        List<NodeInstallProgress> list = nodeInstallProgressMapper.selectByExample(example);
        String returnStr = null;
        if(list!=null&&list.size()>0){
            NodeInstallProgress progress = list.get(0);
            returnStr = progress.getErrorMsg();
            progress.setInstallStatus("errorRead");
            nodeInstallProgressMapper.updateByPrimaryKeySelective(progress);
        }
        return returnStr;
    }

    @Override
    public void cancelAddNode(Integer id) throws Exception {
        NodeInstallProgress installProgress = nodeInstallProgressMapper.selectByPrimaryKey(id);
        installProgress.setInstallStatus("cancel");
        nodeInstallProgressMapper.updateByPrimaryKey(installProgress);
    }
    
}
