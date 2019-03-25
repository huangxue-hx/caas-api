package com.harmonycloud.service.test.tenant;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dto.tenant.TenantDto;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.test.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * @author xc
 * @date 2018/7/3 14:32
 */
public class TenantServiceTest extends BaseTest {

    private Logger LOGGER = LoggerFactory.getLogger(TenantServiceTest.class);

    @Autowired
    private TenantService tenantService;

    private static TenantDto tenantDto;

    @Autowired
    private HttpSession session;

    @BeforeClass
    public void createTenantData() {
        session.setAttribute(CommonConstant.ROLEID, CommonConstant.ADMIN_ROLEID);
        tenantDto.setTenantName("test-xc");
        tenantDto.setAliasName("test-xc");
        List<String> tmList = new ArrayList<>();
        tmList.add("xuchao");
        tenantDto.setTmList(tmList);
    }

    @Test
    public void createTenant() throws Exception {
//        assertTrue(tenantService.createTenant(tenantDto));
    }

    @Test
    public void createTenantQuota() {

    }

    @Test
    public void updateTenantQuota() {

    }

    @Test
    public void deleteTenantQuota() {

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
