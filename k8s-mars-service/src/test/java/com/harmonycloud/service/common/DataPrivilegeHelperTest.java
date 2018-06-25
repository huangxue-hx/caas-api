package com.harmonycloud.service.common;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dto.application.ApplicationDto;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMappingService;
import com.harmonycloud.service.test.JUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chencheng on 18-6-25
 */
@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
//@Transactional
@WebAppConfiguration
public class DataPrivilegeHelperTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DataPrivilegeHelper dataPrivilegeHelper;

    @Autowired
    DataPrivilegeGroupMappingService dataPrivilegeGroupMappingService;

    @Autowired
    HttpSession session;

    @Test
    public void filter() throws Exception {

        session.setAttribute(CommonConstant.USERNAME,"dd");

        ApplicationDto dto = new ApplicationDto();
        dto.setName("test-cc");
        //dto.setDataResourceType(DataResourceTypeEnum.APPLICATION.getCode());
        dto.setNamespace("anson-dev1");
        List<ApplicationDto> list = new ArrayList<>();
        list.add(dto);
        List<ApplicationDto> filter = dataPrivilegeHelper.filter(list);
        logger.info("过滤之后大小："+filter.size());
        for (ApplicationDto mapping:filter) {
            System.out.println(mapping.getName());
        }


    }
}