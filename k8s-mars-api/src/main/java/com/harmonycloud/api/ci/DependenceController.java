package com.harmonycloud.api.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cicd.DependenceDto;
import com.harmonycloud.service.platform.service.ci.DependenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by anson on 17/7/29.
 */

@RestController
@RequestMapping("/cicd/dependence")
public class DependenceController {

    @Autowired
    DependenceService dependenceService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ActionReturnUtil getAllList(@RequestParam(value = "tenantId", required = true)String tenantId, @RequestParam(value = "name", required = false) String name) throws Exception {
        return dependenceService.listByTenantId(tenantId, name);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil addDependency(@RequestBody DependenceDto dependenceDto) throws Exception{
            return dependenceService.add(dependenceDto);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public ActionReturnUtil deleteDependency(@RequestParam(value = "name", required = true)String name) throws Exception {
        return dependenceService.delete(name);
    }
}
