package com.harmonycloud.api.cluster;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.dto.cluster.IngressControllerDto;
import com.harmonycloud.service.cluster.IngressControllerService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * @author xc
 * @date 2018/7/31 19:39
 */
@RestController
@RequestMapping(value = "/clusters/{clusterId}/ingresscontrollers")
public class IngressController {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IngressControllerService ingressControllerService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listIngressController(@PathVariable(value = "clusterId") String clusterId) throws Exception {
        return ingressControllerService.listIngressController(clusterId);
    }

    @RequestMapping(value = "/portrange", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getIngressControllerPortRange(@PathVariable(value = "clusterId") String clusterId) throws Exception {
        return ingressControllerService.getIngressControllerPortRange(clusterId);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createIngressController(@PathVariable(value = "clusterId") String clusterId,
                                                    @RequestParam(value = "icName") String icName,
                                                    @RequestParam(value = "icPort") int icPort,
                                                    @RequestParam(value = "icAliasName",required = false) String icAliasName,
                                                    @RequestParam(value = "icNodeNames", required = false) String icNodeNames) throws Exception {
        IngressControllerDto ingressControllerDto = new IngressControllerDto();
        ingressControllerDto.setClusterId(clusterId);
        ingressControllerDto.setIcName(icName);
        ingressControllerDto.setIcAliasName(icAliasName);
        ingressControllerDto.setHttpPort(icPort);
        if(StringUtils.isNotBlank(icNodeNames)){
            ingressControllerDto.setIcNodeNames(StringUtil.splitAsList(icNodeNames,","));
        }else{
            ingressControllerDto.setIcNodeNames(Collections.emptyList());
        }
        return ingressControllerService.createIngressController(ingressControllerDto);
    }

    @RequestMapping(value = "/{icName}", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteIngressController(@PathVariable(value = "clusterId") String clusterId,
                                                    @PathVariable(value = "icName") String icName) throws Exception {
        return ingressControllerService.deleteIngressController(icName, clusterId);
    }

    @RequestMapping(value = "/{icName}", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateIngressController(@PathVariable(value = "clusterId") String clusterId,
                                                    @PathVariable(value = "icName") String icName,
                                                    @RequestParam(value = "icPort") int icPort,
                                                    @RequestParam(value = "icAliasName") String icAliasName,
                                                    @RequestParam(value = "icNodeNames", required = false) String icNodeNames) throws Exception {
        IngressControllerDto ingressControllerDto = new IngressControllerDto();
        ingressControllerDto.setClusterId(clusterId);
        ingressControllerDto.setIcName(icName);
        ingressControllerDto.setIcAliasName(icAliasName);
        ingressControllerDto.setHttpPort(icPort);
        if(StringUtils.isNotBlank(icNodeNames)){
            ingressControllerDto.setIcNodeNames(StringUtil.splitAsList(icNodeNames,","));
        }else{
            ingressControllerDto.setIcNodeNames(Collections.emptyList());
        }
        return ingressControllerService.updateIngressController(ingressControllerDto);
    }

    @RequestMapping(value = "/{icName}", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil assignIngressController(@PathVariable(value = "clusterId") String clusterId,
                                                    @PathVariable(value = "icName") String icName,
                                                    @RequestParam(value = "tenantId",required = false) String tenantId) throws  Exception {
        return ingressControllerService.assignIngressController(icName, tenantId, clusterId);
    }

}
