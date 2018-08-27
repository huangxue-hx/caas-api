package com.harmonycloud.service.test.platform.service.ci;

import com.harmonycloud.service.platform.service.ci.StageTypeService;
import com.harmonycloud.service.test.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * Created by anson on 18/8/27.
 */
public class StageTypeServiceTest extends BaseTest {

    @Autowired
    StageTypeService stageTypeService;

    @Test
    public void testListStageType() throws Exception {
        assertNotNull(stageTypeService.queryByType("ci"));
        assertNotNull(stageTypeService.queryByType("cd"));
        assertNotNull(stageTypeService.queryByType(null));
    }
}
