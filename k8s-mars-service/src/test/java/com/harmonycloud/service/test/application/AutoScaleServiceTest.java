package com.harmonycloud.service.test.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.scale.AutoScaleDto;
import com.harmonycloud.service.application.AutoScaleService;
import com.harmonycloud.service.test.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;



import static org.testng.Assert.*;

/**
 * Create by chenbilong on 2018/6/20.
 */
public class AutoScaleServiceTest extends BaseTest {
    protected Logger logger = LoggerFactory.getLogger(AutoScaleServiceTest.class);

    @Autowired
    private AutoScaleService autoScaleService;

    private AutoScaleDto autoScaleDto = new AutoScaleDto();

    @BeforeMethod
    public void setUp() {
        autoScaleDto.setNamespace("shenzhe-fenqu1");
        autoScaleDto.setDeploymentName("apptest");
        autoScaleDto.setMinPods(1);
        autoScaleDto.setMaxPods(2);
        autoScaleDto.setTargetCpuUsage(20);
        autoScaleDto.setTargetMemoryUsage(20);
    }

    @Test
    public void testCreate() throws Exception {
        ActionReturnUtil result = autoScaleService.create(autoScaleDto);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testUpdate() throws Exception {
        ActionReturnUtil result = autoScaleService.update(autoScaleDto);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testDelete() throws Exception{
        boolean result = autoScaleService.delete(autoScaleDto.getNamespace(), autoScaleDto.getDeploymentName());
        assertTrue(result);
    }

    @Test
    public void testGet() {
    }
}
