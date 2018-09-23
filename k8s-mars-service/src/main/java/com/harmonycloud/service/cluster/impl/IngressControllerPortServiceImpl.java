package com.harmonycloud.service.cluster.impl;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.cluster.IngressControllerPortMapper;
import com.harmonycloud.dao.cluster.bean.IngressControllerPort;
import com.harmonycloud.dao.cluster.bean.IngressControllerPortExample;
import com.harmonycloud.service.cluster.IngressControllerPortService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xc
 * @date 2018/8/6 21:23
 */
@Service
public class IngressControllerPortServiceImpl implements IngressControllerPortService {

    @Autowired
    IngressControllerPortMapper ingressControllerPortMapper;

    @Override
    public void createIngressControllerPort(IngressControllerPort ingressControllerPort) throws MarsRuntimeException {
        ingressControllerPortMapper.insertSelective(ingressControllerPort);
    }

    @Override
    public void deleteIngressControllerPort(String icName, String clusterId) throws MarsRuntimeException {
        IngressControllerPortExample example = new IngressControllerPortExample();
        example.createCriteria().andNameEqualTo(icName).andClusterIdEqualTo(clusterId);
        ingressControllerPortMapper.deleteByExample(example);
    }

    @Override
    public void updateIngressControllerPort(IngressControllerPort ingressControllerPort) throws MarsRuntimeException {
        ingressControllerPortMapper.updateByPrimaryKey(ingressControllerPort);
    }

    @Override
    public List<IngressControllerPort> listIngressControllerPortByClusterId(String clusterId) throws MarsRuntimeException {
        IngressControllerPortExample example = new IngressControllerPortExample();
        example.createCriteria().andClusterIdEqualTo(clusterId);
        return ingressControllerPortMapper.selectByExample(example);
    }

    @Override
    public List<IngressControllerPort> listIngressControllerPortByName(String icName) throws MarsRuntimeException {
        IngressControllerPortExample example = new IngressControllerPortExample();
        example.createCriteria().andNameEqualTo(icName);
        return ingressControllerPortMapper.selectByExample(example);
    }

    @Override
    public IngressControllerPort getIngressControllerPort(String icName, String clusterId) throws MarsRuntimeException {
        IngressControllerPortExample example = new IngressControllerPortExample();
        example.createCriteria().andNameEqualTo(icName).andClusterIdEqualTo(clusterId);
        List<IngressControllerPort> ingressControllerPortList = ingressControllerPortMapper.selectByExample(example);
        IngressControllerPort ingressControllerPort = new IngressControllerPort();
        if (CollectionUtils.isNotEmpty(ingressControllerPortList)) {
            ingressControllerPort = ingressControllerPortList.get(0);
        }
        return ingressControllerPort;
    }

}
