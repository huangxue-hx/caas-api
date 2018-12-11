package com.harmonycloud.service.test.cluster;

import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cluster.IngressControllerDto;
import com.harmonycloud.service.cluster.IngressControllerService;
import com.harmonycloud.service.platform.bean.NodeDto;
import com.harmonycloud.service.platform.service.NodeService;
import com.harmonycloud.service.test.BaseTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * @author xc
 * @date 2018/8/1 10:45
 */
@Rollback(false)
public class IngressControllerServiceTest extends BaseTest {

    private Logger LOGGER = LoggerFactory.getLogger(IngressControllerServiceTest.class);

    private String ingressControllerName = "nginx-ingress-controller-unittest";
    private int icPort = 98;
    private List<String> icNodeNames = new ArrayList<>();

    @Autowired
    private IngressControllerService ingressControllerService;
    @Autowired
    private NodeService nodeService;

    @Test
    public void createIngressController() throws Exception {
        //如果已经存在，先删除
        ActionReturnUtil response = ingressControllerService.listIngressController(devClusterId);
        assertTrue(response.isSuccess());
        List<IngressControllerDto> ingressControllerDtoList = (List)response.getData();
        assertTrue(ingressControllerDtoList.size() > 0);
        for(IngressControllerDto ingressControllerDto : ingressControllerDtoList){
            if(ingressControllerDto.getIcName().equals(ingressControllerName)){
                ingressControllerService.deleteIngressController(ingressControllerName,devClusterId);
            }
        }
        //获取闲置节点
        ActionReturnUtil nodeResponse = nodeService.listNode(devClusterId);
        List<NodeDto> nodeDtoList = (List)nodeResponse.getData();
        for(NodeDto nodeDto : nodeDtoList){
            if(nodeDto.getNodeShareStatus().equals(DictEnum.NODE_IDLE.phrase())){
                icNodeNames.add(nodeDto.getName());
            }
        }
        IngressControllerDto ic = new IngressControllerDto();
        ic.setClusterId(devClusterId);
        ic.setIcName(ingressControllerName);
        ic.setHttpPort(icPort);
        ic.setIcNodeNames(icNodeNames);
        ic.setIcAliasName(TEST_ALISA_NAME);
        ic.setExternalHttpPort(9000);
        ic.setExternalHttpsPort(9001);
        assertTrue(ingressControllerService.createIngressController(ic).isSuccess());
        response = ingressControllerService.listIngressController(devClusterId);
        assertTrue(response.isSuccess());
        ingressControllerDtoList = (List)response.getData();
        assertTrue(ingressControllerDtoList.size() > 1);
        boolean created = false;
        for(IngressControllerDto ingressControllerDto : ingressControllerDtoList){
            if(ingressControllerDto.getIcName().equals(ingressControllerName)){
                created = true;
                assertTrue(ingressControllerDto.getHttpPort() == icPort);
            }
        }
        assertTrue(created);
    }

    @Test
    public void testListIngressController() throws Exception {
        ActionReturnUtil response = ingressControllerService.listIngressController(devClusterId);
        assertTrue(response.isSuccess());
        List<IngressControllerDto> ingressControllerDtoList = (List)response.getData();
        assertTrue(ingressControllerDtoList.size() > 0);
        for(IngressControllerDto ingressControllerDto : ingressControllerDtoList){
            assertNotNull(ingressControllerDto.getClusterAliasName());
            assertNotNull(ingressControllerDto.getClusterId());
            assertNotNull(ingressControllerDto.getIcPort());
            assertNotNull(ingressControllerDto.getIcName());
            assertTrue(ingressControllerDto.getHttpPort()>0);
        }
    }

    @Test
    public void testAssignIngressController() throws Exception{
        ActionReturnUtil response = ingressControllerService
                .assignIngressController(ingressControllerName,tenantId,devClusterId);
        assertTrue(response.isSuccess());
        response = ingressControllerService.listIngressController(devClusterId);
        List<IngressControllerDto> ingressControllerDtoList = (List)response.getData();
        assertTrue(ingressControllerDtoList.size() > 1);
        boolean assign = false;
        for(IngressControllerDto ingressControllerDto : ingressControllerDtoList){
            if(ingressControllerDto.getIcName().equals(ingressControllerName)){
                assertTrue(ingressControllerDto.getTenantInfo().size() > 0);
                List<Map<String, String>> tenants = ingressControllerDto.getTenantInfo();
                for(Map<String, String> tenant : tenants){
                    if(tenant.get("tenantId").equals(tenantId)){
                        assign = true;
                    }
                }
            }
        }
        assertTrue(assign);
    }

    @Test
    public void testUpdateIngressController() throws Exception{
        IngressControllerDto ic = new IngressControllerDto();
        ic.setClusterId(devClusterId);
        ic.setIcName(ingressControllerName);
        ic.setHttpPort(icPort+1);
        ic.setIcNodeNames(icNodeNames);
        ic.setIcAliasName(TEST_ALISA_NAME);
        ActionReturnUtil response = ingressControllerService.updateIngressController(ic);
        assertTrue(response.isSuccess());
        response = ingressControllerService.listIngressController(devClusterId);
        List<IngressControllerDto> ingressControllerDtoList = (List)response.getData();
        assertTrue(ingressControllerDtoList.size() > 1);
        boolean exist = false;
        for(IngressControllerDto ingressControllerDto : ingressControllerDtoList){
            if(ingressControllerDto.getIcName().equals(ingressControllerName)){
                exist = true;
                assertTrue(ingressControllerDto.getHttpPort() == icPort+1);
            }
        }
        assertTrue(exist);
    }

    @Test
    public void testDeleteIngressControllerTest() throws Exception{
        ActionReturnUtil response = ingressControllerService.listIngressController(devClusterId);
        assertTrue(response.isSuccess());
        List<IngressControllerDto> ingressControllerDtoList = (List)response.getData();
        assertTrue(ingressControllerDtoList.size() > 0);
        for(IngressControllerDto ingressControllerDto : ingressControllerDtoList){
            if(ingressControllerDto.getIcName().equals(ingressControllerName)){
                ingressControllerService.deleteIngressController(ingressControllerName,devClusterId);
            }
        }
    }

}
