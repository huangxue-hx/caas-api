package com.harmonycloud.service.test.platform.service.ci;

import com.harmonycloud.dto.cicd.StageDto;
import com.harmonycloud.service.application.JobsService;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.service.ci.StageService;
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
    private StageTypeService stageTypeService;

    @Autowired
     private JobService jobService;


    @Test
    public void testListStageType() throws Exception {
        assertNotNull(stageTypeService.queryByType("ci"));
        assertNotNull(stageTypeService.queryByType("cd"));
        assertNotNull(stageTypeService.queryByType(null));
    }

    @Test
    public void testRunStage() throws Exception {
        jobService.runStage(151,1);

    }


}
