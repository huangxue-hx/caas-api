package com.harmonycloud.api.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.PersistentVolumeClaimDto;
import com.harmonycloud.service.application.PersistentVolumeClaimService;
import com.harmonycloud.service.cluster.ClusterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author xc
 * @date 2018/7/7 11:32
 */
@Api(description = "PersistentVolumeClaim管理，新增、删除、查询、更新、回收")
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/pvc")
public class PersistentVolumeClaimController {

    @Autowired
    PersistentVolumeClaimService persistentVolumeClaimService;

    @Autowired
    ClusterService clusterService;

    @ApiOperation(value = "创建PersistentVolumeClaim", notes = "在集群内K8S上创建存储卷索取，动态创建存储卷")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantId", value = "租户ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "projectId", value = "项目ID", required = true, paramType = "path", dataType = "String")
    })
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createPersistentVolumeClaim(@PathVariable("tenantId") String tenantId,
                                                        @PathVariable("projectId") String projectId,
                                                        @RequestBody PersistentVolumeClaimDto persistentVolumeClaimDto) throws Exception {
        persistentVolumeClaimDto.setTenantId(tenantId);
        persistentVolumeClaimDto.setProjectId(projectId);
        return persistentVolumeClaimService.createPersistentVolumeClaim(persistentVolumeClaimDto);
    }

    @ApiOperation(value = "获取某个租户下某个项目的所有PersistentVolumeClaim", notes = "从集群内K8S上获取某个租户下某个项目的所有存储卷索取")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantId", value = "租户ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "projectId", value = "项目ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群ID", paramType = "query", dataType = "String")
    })
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listPersistentVolumeClaim(@PathVariable("tenantId") String tenantId,
                                                      @PathVariable("projectId") String projectId,
                                                      @RequestParam(value = "clusterId", required = false) String clusterId) throws Exception {
        return  persistentVolumeClaimService.listPersistentVolumeClaim(projectId, tenantId, clusterId);
    }

    @ApiOperation(value = "根据名称删除PersistentVolumeClaim", notes = "从集群内K8S上根据名称删除存储卷索取")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pvcName", value = "PersistentVolumeClaim名称", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "所属分区", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群ID", required = true, paramType = "query", dataType = "String")
    })
    @RequestMapping(value = "/{pvcName}", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deletePersistentVolumeClaim(@PathVariable("pvcName") String pvcName,
                                                        @RequestParam(value = "namespace") String namespace,
                                                        @RequestParam(value = "clusterId") String clusterId) throws Exception {
        return persistentVolumeClaimService.deletePersistentVolumeClaim(namespace, pvcName, clusterId);
    }

    @ApiOperation(value = "根据名称清空PersistentVolumeClaim对应的PersistentVolume", notes = "从集群内K8S上根据名称清空存储卷")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pvcName", value = "PersistentVolumeClaim名称", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "所属分区", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群ID", required = true, paramType = "query", dataType = "String")
    })
    @RequestMapping(value = "/{pvcName}/recycle", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil recyclePersistentVolumeClaim(@PathVariable("pvcName") String pvcName,
                                                         @RequestParam(value = "namespace") String namespace,
                                                         @RequestParam(value = "clusterId") String clusterId) throws Exception {
        return persistentVolumeClaimService.recyclePersistentVolumeClaim(namespace, pvcName, clusterId);
    }

    @ApiOperation(value = "根据名称查询PersistentVolumeClaim", notes = "从集群内K8S上根据名称查询存储卷索取")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pvcName", value = "PersistentVolumeClaim名称", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "所属分区", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群ID", required = true, paramType = "query", dataType = "String")
    })
    @RequestMapping(value = "/{pvcName}", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getPersistentVolumeClaim(@PathVariable("pvcName") String pvcName,
                                                     @RequestParam(value = "namespace") String namespace,
                                                     @RequestParam(value = "clusterId") String clusterId) throws Exception {
        return persistentVolumeClaimService.getPersistentVolumeClaim(namespace, pvcName, clusterId);
    }

    @ApiOperation(value = "更新PersistentVolumeClaim", notes = "在集群内K8S上更新存储卷索取，只能更新容量和读写权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantId", value = "租户ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "projectId", value = "项目ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "pvcName", value = "PersistentVolumeClaim名称", required = true, paramType = "path", dataType = "String")
    })
    @RequestMapping(value = "/{pvcName}", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updatePersistentVolumeClaim(@PathVariable("tenantId") String tenantId,
                                                        @PathVariable("projectId") String projectId,
                                                        @PathVariable("pvcName") String pvcName,
                                                        @RequestBody PersistentVolumeClaimDto persistentVolumeClaimDto) throws Exception {
        persistentVolumeClaimDto.setTenantId(tenantId);
        persistentVolumeClaimDto.setProjectId(projectId);
        persistentVolumeClaimDto.setName(pvcName);
        return persistentVolumeClaimService.updatePersistentVolumeClaim(persistentVolumeClaimDto);
    }
}
