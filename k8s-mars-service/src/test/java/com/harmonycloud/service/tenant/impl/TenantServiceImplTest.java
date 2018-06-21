package com.harmonycloud.service.tenant.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dto.tenant.TenantDto;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.test.JUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertTrue;

/**
 * Created by chencheng on 18-6-19
 */
@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
//@Transactional
@WebAppConfiguration
public class TenantServiceImplTest {

    @Autowired
    TenantService tenantService;

    @Autowired
    HttpSession session;

    /**
     * 创建租户
     * @throws Exception
     */
    @Test
    public void createTenant() throws Exception{
        TenantDto tenantDto = new TenantDto();
        tenantDto.setAliasName("张三");
        tenantDto.setTenantName("zhangsan");
        tenantDto.setStrategy(CommonConstant.DATA_CLOSED_STRATEGY);
        tenantService.createTenant(tenantDto);

    }

    /**
     * 查询租户详情
     * @throws Exception
     */
    @Test
    public void getTenantDetail() throws Exception{
        session.setAttribute(CommonConstant.ROLEID,1);
        String tenantId = "12e5523162474a02970f034830329c27";
        TenantDto tenantDto = tenantService.getTenantDetail(tenantId);
        assertTrue(tenantDto.getStrategy() == CommonConstant.DATA_CLOSED_STRATEGY);
    }

    /**
     * 修改租户策略
     * @throws Exception
     */
    @Test
    public void updateTenantStrategy() throws Exception{
        String tenantId = "12e5523162474a02970f034830329c27";
        Integer strategy = CommonConstant.DATA_OPEN_STRATEGY;
        tenantService.updateTenantStrategy(tenantId,strategy);
    }

}