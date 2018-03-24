package com.harmonycloud.api.application;

import com.harmonycloud.dto.application.PersistentVolumeDto;
import com.harmonycloud.service.application.PersistentVolumeService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.PvDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.harmonycloud.common.util.ActionReturnUtil;
import java.util.List;


@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/pvs")
public class PersistentVolumeController {

    @Autowired
    PersistentVolumeService persistentVolumeService;
    @Autowired
    ClusterService clusterService;
    
    /**
     * 在指定集群上创建pv
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createPv(@PathVariable("tenantId") String tenantId,
                                             @PathVariable("projectId") String projectId,
                                             @RequestBody @Validated PvDto pvDto) throws Exception {
        PersistentVolumeDto volume = new PersistentVolumeDto();
        volume.setVolumeName(pvDto.getName());
        volume.setProjectId(projectId);
        volume.setBindOne(pvDto.getIsBindOne());
        volume.setReadOnly(pvDto.getIsReadonly());
        volume.setCapacity(pvDto.getCapacity());
        volume.setType(pvDto.getType());
        return persistentVolumeService.createPv(volume,clusterService.findClusterById(pvDto.getClusterId()));
    }

    /**
     * 根据项目id查询用户可以操作的所有集群上的该项目的pv资源
     * @param projectId
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listPv(@PathVariable("tenantId") String tenantId,
                                   @PathVariable("projectId") String projectId,
                                   @RequestParam(value = "clusterId",required = false) String clusterId,
                                   @RequestParam(value = "isBind",required = false) Boolean isBind) throws Exception {
        List<PvDto> pvs = persistentVolumeService.listPv(projectId, clusterId, isBind);
        return ActionReturnUtil.returnSuccessWithData(pvs);
    }

    /**
     * 根据名称修改pv
     * @param pvDto 存储信息
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updatePv(@PathVariable("tenantId") String tenantId,
                                                   @PathVariable("projectId") String projectId,
                                                   @RequestBody @Validated PvDto pvDto) throws Exception {
        return persistentVolumeService.updatePv(pvDto);
    }

    /**
     * 根据name删除k8s集群的pv
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{pvName:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deletePv(@PathVariable("tenantId") String tenantId,
                                             @PathVariable("projectId") String projectId,
                                             @PathVariable("pvName") String pvName,
                                             @RequestParam(value = "clusterId") String clusterId) throws Exception {
        return persistentVolumeService.deletePv(pvName, clusterId);

    }

    /**
     * 根据name查询pv
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{pvName}", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getPv(@PathVariable("tenantId") String tenantId,
                                          @PathVariable("projectId") String projectId,
                                          @PathVariable("pvName") String pvName,
                                          @RequestParam(value = "clusterId") String clusterId) throws Exception {
        return persistentVolumeService.getPv(pvName, clusterId);
    }
    
    /**
     * 根据name回收pv数据
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{pvName}/recycle", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil recyclePv(@PathVariable("tenantId") String tenantId,
                                            @PathVariable("projectId") String projectId,
                                            @PathVariable("pvName") String pvName,
                                            @RequestParam(value = "clusterId") String clusterId) throws Exception {
        return persistentVolumeService.recyclePv(pvName, clusterId);
    }
}
