package com.harmonycloud.api.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cicd.DependenceDto;
import com.harmonycloud.dto.cicd.DependenceFileDto;
import com.harmonycloud.service.platform.service.ci.DependenceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author w_kyzhang
 * @Description 依赖管理接口
 * @Date 2017-7-29
 * @Modified
 */

@Api(description = "CICD依赖管理")
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/dependence")
@RestController
public class DependenceController {

    @Autowired
    DependenceService dependenceService;

    /**
     * 获取依赖列表
     * @param projectId 项目id
     * @param clusterId 集群id
     * @param name 依赖名
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "获取依赖列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "projectId", value = "项目id", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "name", value = "名称", paramType = "query", dataType = "String")})
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listDependence(@PathVariable("projectId")String projectId,
                                           @RequestParam(value = "clusterId",required=false ) String clusterId,
                                           @RequestParam(value = "name", required = false) String name) throws Exception {
        List<Map> dependences = dependenceService.listByProjectIdAndClusterId(projectId, clusterId, name);
        Map result = new HashMap();
        result.put("list", dependences);
        result.put("count", dependences.size());
        return ActionReturnUtil.returnSuccessWithData(result);
    }

    /**
     * 新增依赖
     * @param dependenceDto 依赖Dto对象
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "新增依赖")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "项目id", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "dependenceDto", value = "依赖信息", required = true, paramType = "body", dataType = "DependenceDto")
    })
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil addDependency(@PathVariable("projectId")String projectId,
                                          @RequestBody DependenceDto dependenceDto) throws Exception {
        dependenceDto.setProjectId(projectId);
        dependenceService.add(dependenceDto);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 删除依赖
     * @param dependenceName 依赖名
     * @param projectId 项目id
     * @param clusterId 集群id
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "删除依赖")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "项目ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群ID", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "dependenceName", value = "依赖名", required = true, paramType = "path", dataType = "String")})
    @RequestMapping(value = "/{dependenceName}", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteDependency(@PathVariable("projectId")String projectId,
                                             @RequestParam(value = "clusterId", required = false) String clusterId,
                                             @PathVariable("dependenceName")String dependenceName) throws Exception {
        dependenceService.delete(dependenceName, projectId, clusterId);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 上传依赖文件至依赖目录
     * @param file 文件
     * @param dependenceName 依赖名
     * @param projectId 项目id
     * @param clusterId 集群id
     * @param path 上传路径
     * @param isDecompressed 是否解压
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "上传依赖文件至依赖目录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "项目ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群ID", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "dependenceName", value = "依赖名", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "file", value = "文件", paramType = "body", dataType = "MultipartFile"),
            @ApiImplicitParam(name = "path", value = "路径", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "decompressed", value = "是否解压", paramType = "query", dataType = "boolean")})
    @RequestMapping(value = "/{dependenceName}/file", method = RequestMethod.POST)
    public ActionReturnUtil uploadFile(@PathVariable("projectId") String projectId,
                                       @RequestParam(value = "clusterId", required = false) String clusterId,
                                       @PathVariable("dependenceName") String dependenceName,
                                       @RequestParam(value = "file") MultipartFile file,
                                       @RequestParam(value = "path") String path,
                                       @RequestParam(value = "decompressed") boolean isDecompressed) throws Exception {
        DependenceFileDto dependenceFileDto = new DependenceFileDto();
        dependenceFileDto.setFile(file);
        dependenceFileDto.setDependenceName(dependenceName);
        dependenceFileDto.setClusterId(clusterId);
        dependenceFileDto.setProjectId(projectId);
        dependenceFileDto.setPath(path);
        dependenceFileDto.setDecompressed(isDecompressed);
        dependenceService.uploadFile(dependenceFileDto);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 删除path目录
     *
     * @param dependenceName 依赖名
     * @param projectId 项目id
     * @param clusterId 集群id
     * @param path 删除路径
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "删除文件或目录", notes = "根据文件或目录的路径进行删除" )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "项目ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群ID", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "dependenceName", value = "依赖名", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "path", value = "欲删除文件或目录的路径，以“/”开始", paramType = "query", dataType = "String")})
    @RequestMapping(value = "/{dependenceName}/file", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteFile(@PathVariable("projectId") String projectId,
                                       @RequestParam(value = "clusterId", required = false) String clusterId,
                                       @PathVariable("dependenceName") String dependenceName,
                                       @RequestParam(value = "path") String path) throws Exception {
        dependenceService.deleteFile(dependenceName, projectId, clusterId, path);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 获取path下的文件列表
     *
     * @param dependenceName 依赖名
     * @param projectId 项目id
     * @param clusterId 集群id
     * @param path 路径
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "获取path下的文件列表", notes = "获取目标path下的文件及目录" )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "项目ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群ID", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "dependenceName", value = "依赖名", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "path", value = "欲获取文件或目录的路径，以“/”开始", required = true ,paramType = "query", dataType = "String")})
    @RequestMapping(value = "/{dependenceName}/filelist", method = RequestMethod.GET)
    public ActionReturnUtil listFile(@PathVariable("projectId") String projectId,
                                     @RequestParam(value = "clusterId", required = false) String clusterId,
                                     @PathVariable("dependenceName") String dependenceName,
                                     @RequestParam(value = "path") String path) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(dependenceService.listFile(dependenceName, projectId, clusterId, path));
    }

    /**
     * 根据文件或目录的名称关键词查询依赖目录下的文件或目录
     * @param projectId
     * @param clusterId
     * @param dependenceName
     * @param keyWord
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "查询文件或目录", notes = "根据文件或目录的名称关键词查询依赖目录下的文件或目录" )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "项目ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群ID", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "dependenceName", value = "依赖名", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "keyWord", value = "欲查询的关键词", required = true, paramType = "query", dataType = "String")})
    @RequestMapping(value = "/{dependenceName}/file", method = RequestMethod.GET)
    public ActionReturnUtil findDependenceFileByKeyword(@PathVariable("projectId") String projectId,
                                                        @RequestParam(value = "clusterId", required = false) String clusterId,
                                                        @PathVariable("dependenceName") String dependenceName,
                                                        @RequestParam(value = "keyWord") String keyWord) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(dependenceService.findDependenceFileByKeyword(dependenceName, projectId, clusterId , keyWord));

    }

    /**
     * 获取依赖可用的storageclass
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "查询依赖可用存储类", notes = "获取上层集群下的storageClass" )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantId", value = "租户ID", required = false, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "projectId", value = "项目ID", required = false, paramType = "path", dataType = "String")})
    @RequestMapping(value = "/storage", method = RequestMethod.GET)
    public ActionReturnUtil getDependenceStorage() throws Exception {
        return ActionReturnUtil.returnSuccessWithData(dependenceService.listStorageClass());
    }
}
