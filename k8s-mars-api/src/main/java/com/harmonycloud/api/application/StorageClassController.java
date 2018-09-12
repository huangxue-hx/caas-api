package com.harmonycloud.api.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.StorageClassDto;
import com.harmonycloud.service.application.StorageClassService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xc
 * @date 2018/6/19 17:33
 */
@Api(description = "StorageClass存储管理，新增、删除、查询")
@RestController
@RequestMapping(value = "/clusters/{clusterId}/storage")
public class StorageClassController {

    @Autowired
    StorageClassService storageClassService;

    /**
     * 创建StorageClass
     *
     * @param scDto StorageClass相关属性
     * @return ActionReturnUtil
     */
    @ApiOperation(value = "创建StorageClass存储", notes = "在集群内K8S上创建StorageClass及NFS相关插件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群ID", required = true, paramType = "path", dataType = "String")
    })
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createStorageClass(@PathVariable("clusterId") String clusterId,
                                               @RequestBody StorageClassDto scDto) throws Exception {
        scDto.setClusterId(clusterId);
        return storageClassService.createStorageClass(scDto);
    }

    /**
     * 根据名称删除StorageClass
     *
     * @param scName StorageClass名称
     * @param clusterId 所属集群id
     * @return ActionReturnUtil
     */
    @ApiOperation(value = "删除StorageClass存储", notes = "在集群内K8S上删除StorageClass及NFS相关插件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "name", value = "StorageClass名称", required = true, paramType = "query", dataType = "String")
    })
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteStorageClass(@PathVariable("clusterId") String clusterId,
                                               @RequestParam(value = "name") String scName) throws Exception {
        return storageClassService.deleteStorageClass(scName, clusterId);
    }

    /**
     * 根据名称查询StorageClass
     *
     * @param scName StorageClass名称
     * @param clusterId 所属集群id
     * @return ActionReturnUtil
     */
    @ApiOperation(value = "根据名称查询StorageClass存储", notes = "在集群内K8S上根据名称查询StorageClass")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "scName", value = "StorageClass名称", required = true, paramType = "query", dataType = "String")
    })
    @RequestMapping(value = "/{scName}",method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getStorageClass(@PathVariable("clusterId") String clusterId,
                                            @PathVariable("scName") String scName) throws Exception {
        return storageClassService.getStorageClass(scName, clusterId);
    }

    /**
     * 查看某集群下StorageClass列表
     *
     * @param clusterId 所属集群id
     * @return ActionReturnUtil
     */
    @ApiOperation(value = "查询某个集群下StorageClass存储列表", notes = "在集群内K8S上根据集群名称查询该集群下所有StorageClass存储")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "分区", required = false, paramType = "query", dataType = "String")
    })
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listStorageClass(@PathVariable("clusterId") String clusterId,
                                             @RequestParam(value = "namespace", required = false)String namespace) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(storageClassService.listStorageClass(clusterId, namespace));
    }
}
