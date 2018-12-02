package com.harmonycloud.service.test.cluster;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cluster.DataCenterDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.application.DataCenterService;
import com.harmonycloud.service.test.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.List;

import static org.junit.Assert.*;


/**
 * Created by lucia on 2018/6/7.
 */
public class DataCenterServiceTest extends BaseTest {

    protected Logger logger= LoggerFactory.getLogger(DataCenterServiceTest.class);

    @Autowired
    private DataCenterService dataCenterService;

    @Test
    public void testGetDataCenter() throws Exception {
        ActionReturnUtil res = dataCenterService.listDataCenter(Boolean.TRUE, null);
        assertTrue(res.isSuccess());
        assertNotNull(res.getData());
        List<DataCenterDto> dataCenterDtoList = (List)res.getData();
        assertTrue(dataCenterDtoList.size() > 0);

        res = dataCenterService.listDataCenter(Boolean.TRUE, Boolean.TRUE);
        assertTrue(res.isSuccess());
        assertNotNull(res.getData());
        List<DataCenterDto> dataCenterDtoList2 = (List)res.getData();
        assertTrue(dataCenterDtoList2.size() > 0);
        for(DataCenterDto centerDto: dataCenterDtoList2){
            List<Cluster>  clusters = centerDto.getClusters();
            if(clusters == null){
                continue;
            }
            for(Cluster cluster: clusters){
                assertTrue(cluster.getIsEnable());
            }
        }

        res = dataCenterService.listDataCenter(Boolean.TRUE, Boolean.FALSE);
        assertTrue(res.isSuccess());
        assertNotNull(res.getData());
        List<DataCenterDto> dataCenterDtoList3 = (List)res.getData();
        assertTrue(dataCenterDtoList3.size() > 0);
        for(DataCenterDto centerDto: dataCenterDtoList3){
            List<Cluster>  clusters = centerDto.getClusters();
            if(clusters == null){
                continue;
            }
            for(Cluster cluster: clusters){
                assertFalse(cluster.getIsEnable());
            }
        }

        res = dataCenterService.listDataCenter(Boolean.FALSE, null);
        assertTrue(res.isSuccess());
        assertNotNull(res.getData());
        dataCenterDtoList = (List)res.getData();
        assertTrue(dataCenterDtoList.size() > 0);
        DataCenterDto dataCenterDto = dataCenterDtoList.get(0);
        assertNull(dataCenterDto.getClusters());

    }

}
