package com.harmonycloud.service.test.cluster;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.cluster.bean.IngressControllerPort;
import com.harmonycloud.service.cluster.IngressControllerPortService;
import com.harmonycloud.service.cluster.IngressControllerService;
import com.harmonycloud.service.test.BaseTest;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static org.junit.Assert.assertTrue;


/**
 * @author xc
 * @date 2018/8/1 10:45
 */
public class IngressControllerServiceTest extends BaseTest {

    private Logger LOGGER = LoggerFactory.getLogger(IngressControllerServiceTest.class);

    @Autowired
    IngressControllerService ingressControllerService;

    @Autowired
    IngressControllerPortService ingressControllerPortService;

    private static String clusterId = "cluster-top--dev";

    @Test
    public void listIngressController() throws Exception {
        assertTrue(ingressControllerService.listIngressController(clusterId).isSuccess());
    }

    @Test
    public void createIngressController() throws MarsRuntimeException, IOException {
        assertTrue(ingressControllerService.createIngressController(clusterId, "ingress-controller-a", 81).isSuccess());
    }

    @Test
    public void deleteIngressController() throws Exception{
        ingressControllerService.deleteIngressController("ingress-controller-a","cluster-top--dev");
    }

    @Test
    public void udpIngressController() throws Exception{
       ingressControllerService.updateIngressController("ingress-controller-a",82,"cluster-top--dev");
    }
    @Test
    public void asginIngressController() throws Exception{
        ingressControllerService.assignIngressController("ingress-controller-a","a6f8f4633b0941fe932d79f04bbd2b95","cluster-top--dev");
    }

}
