package com.harmonycloud.service.cluster;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.cluster.bean.IngressControllerPort;

import java.util.List;

/**
 * Ingress-controller-port存储到数据库
 * @author xc
 * @date 2018/8/6 21:07
 */
public interface IngressControllerPortService {

    void createIngressControllerPort(IngressControllerPort ingressControllerPort) throws MarsRuntimeException;

    void deleteIngressControllerPort(String icName, String clusterId) throws MarsRuntimeException;

    void updateIngressControllerPort(IngressControllerPort ingressControllerPort) throws MarsRuntimeException;

    List<IngressControllerPort> listIngressControllerPortByClusterId(String clusterId) throws MarsRuntimeException;

    List<IngressControllerPort> listIngressControllerPortByName(String icName) throws MarsRuntimeException;

    IngressControllerPort getIngressControllerPort(String icName, String clusterId) throws MarsRuntimeException;
}
