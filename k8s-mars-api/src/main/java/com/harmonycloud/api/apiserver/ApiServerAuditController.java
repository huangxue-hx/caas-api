package com.harmonycloud.api.apiserver;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dto.apiserver.ApiServerAuditSearchDto;
import com.harmonycloud.service.apiserver.ApiServerAuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author liangli
 */
@Api(value = "ApiServerAuditController", description = "k8s ApiServer 审计日志模块接口")
@Controller
@RequestMapping("/system/api-server")
public class ApiServerAuditController {

    @Autowired
    private ApiServerAuditService apiServerAuditService;

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "根据组合条件查找日志", response = ActionReturnUtil.class, httpMethod = "GET",  notes = "", consumes = "", produces = "")
    @ResponseBody
    @RequestMapping(value = "/auditlogs", method = RequestMethod.GET)
    public ActionReturnUtil getAuditLogsByQuery(@RequestParam(value = "startTime") String startTime,
                                                @RequestParam(value = "endTime") String endTime,
                                                @RequestParam(value = "keyWords", required = false) String keyWords,
                                                @RequestParam(value = "verbName", required = false) String verbName,
                                                @RequestParam(value = "namespace", required = false) String namespace,
                                                @RequestParam(value = "clusterId", required = false) String clusterId,
                                                @RequestParam(value = "pageNum", required = false) Integer pageNum,
                                                @RequestParam(value = "size") Integer size) throws Exception {
        ApiServerAuditSearchDto search = buildApiServerAuditSearch(startTime, endTime, clusterId, keyWords, verbName, namespace, size, pageNum);
        return apiServerAuditService.searchByQuery(search);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "查找apiserver日志数量", response = ActionReturnUtil.class, httpMethod = "GET",  notes = "", consumes = "", produces = "")
    @ResponseBody
    @RequestMapping(value = "/auditlogs/count", method = RequestMethod.GET)
    public ActionReturnUtil getAuditLogsCount(@RequestParam(value = "startTime") String startTime,
                                              @RequestParam(value = "endTime") String endTime,
                                              @RequestParam(value = "keyWords", required = false) String keyWords,
                                              @RequestParam(value = "verbName", required = false) String verbName,
                                              @RequestParam(value = "namespace", required = false) String namespace,
                                              @RequestParam(value = "clusterId", required = false) String clusterId,
                                              @RequestParam(value = "pageNum", required = false) Integer pageNum) throws Exception {

        ApiServerAuditSearchDto search = buildApiServerAuditSearch(startTime, endTime, clusterId, keyWords, verbName, namespace, null, pageNum);
        return apiServerAuditService.getAuditCount(search);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "查找集群apiserver日志包含的分区", response = ActionReturnUtil.class, httpMethod = "GET",  notes = "", consumes = "", produces = "")
    @ResponseBody
    @RequestMapping(value = "/auditlogs/namespaces", method = RequestMethod.GET)
    public ActionReturnUtil getAuditNamespace() throws Exception {
        return apiServerAuditService.getAuditLogsNamespace();
    }

    private ApiServerAuditSearchDto buildApiServerAuditSearch(String startTime, String endTime, String clusterId, String keyWords, String verbName, String namespace, Integer size, Integer pageNum) {
        ApiServerAuditSearchDto search = new ApiServerAuditSearchDto();
        startTime = DateUtil.local2Utc(startTime, DateStyle.YYYY_MM_DD_HH_MM_SS.getValue(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue());
        endTime = DateUtil.local2Utc(endTime, DateStyle.YYYY_MM_DD_HH_MM_SS.getValue(),  DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue());
        search.setStartTime(startTime);
        search.setEndTime(endTime);
        search.setClusterId(clusterId);
        search.setKeyWords(keyWords);
        search.setPageNum(pageNum);
        search.setSize(size);
        search.setVerbName(verbName);
        search.setNamespace(namespace);
        return search;
    }
}
