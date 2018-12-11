package com.harmonycloud.api.cluster;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cluster.IngressControllerDto;
import com.harmonycloud.service.cluster.IngressControllerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
                                                    @ModelAttribute IngressControllerDto ingressControllerDto) throws Exception {
        ingressControllerDto.setClusterId(clusterId);
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
                                                    @ModelAttribute IngressControllerDto ingressControllerDto) throws Exception {
        ingressControllerDto.setClusterId(clusterId);
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
