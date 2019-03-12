package com.harmonycloud.api.monitor;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.service.MonitorCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/deploys")
public class MonitorCenterController {

    @Autowired
    private MonitorCenterService monitorCenterService;

    @ResponseBody
    @RequestMapping(value="/monitor", method= RequestMethod.GET)
    public ActionReturnUtil getProjectMonit(@RequestParam(value="rangeType") String rangeType,
                                            @RequestParam(value="namespace") String namespace,
                                            @PathVariable(value="projectId") String projectId,
                                            @PathVariable(value="tenantId") String tenantId) throws Exception {
        return monitorCenterService.getProjectMonit(tenantId, projectId, namespace, rangeType);
    }
}
