package com.harmonycloud.service.test.tenant;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dto.tenant.TenantDto;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;

import javax.servlet.http.HttpSession;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by chencheng on 18-6-19
 */

public class TenantServiceTest extends BaseTest{

    @Autowired
    TenantService tenantService;

    @Autowired
    HttpSession session;

    @BeforeMethod
    void initData(){
        session.setAttribute(CommonConstant.ROLEID, CommonConstant.ADMIN_ROLEID);
    }

    /**
     * 创建租户
     * @throws Exception
     */
    @Test
    public void testCreateTenant() throws Exception{
        TenantDto tenantDto = new TenantDto();
        tenantDto.setAliasName("测试租户");
        tenantDto.setTenantName("testtenant");
        tenantDto.setStrategy(CommonConstant.DATA_CLOSED_STRATEGY);
        tenantService.createTenant(tenantDto);
        List<TenantDto> list= tenantService.tenantList();
        for(TenantDto tenant:list){
            if("testtenant".equalsIgnoreCase(tenant.getTenantName())){
                assertEquals(tenant.getStrategy().intValue(), CommonConstant.DATA_CLOSED_STRATEGY);
            }
        }
    }


    /**
     * 修改租户策略
     * @throws Exception
     */
    @Test
    public void testUpdateTenantStrategy() throws Exception{
        tenantService.updateTenantStrategy(tenantId,CommonConstant.DATA_OPEN_STRATEGY);
        TenantDto tenantDto = tenantService.getTenantDetail(tenantId);
        assertEquals(tenantDto.getStrategy().intValue(), CommonConstant.DATA_OPEN_STRATEGY);

        tenantService.updateTenantStrategy(tenantId,CommonConstant.DATA_SEMIOPEN_STRATEGY);
        tenantDto = tenantService.getTenantDetail(tenantId);
        assertEquals(tenantDto.getStrategy().intValue(), CommonConstant.DATA_SEMIOPEN_STRATEGY);

        tenantService.updateTenantStrategy(tenantId,CommonConstant.DATA_CLOSED_STRATEGY);
        tenantDto = tenantService.getTenantDetail(tenantId);
        assertEquals(tenantDto.getStrategy().intValue(), CommonConstant.DATA_CLOSED_STRATEGY);

    }

}