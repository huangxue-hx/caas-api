package com.harmonycloud.api.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cicd.DependenceDto;
import com.harmonycloud.dto.cicd.DependenceFileDto;
import com.harmonycloud.service.platform.service.ci.DependenceService;
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
    @RequestMapping(value = "/{dependenceName}", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteDependency(@PathVariable("projectId")String projectId,
                                             @RequestParam(value = "clusterId", required = false) String clusterId,
                                             @PathVariable("dependenceName")String dependenceName) throws Exception {
       dependenceService.delete(dependenceName, projectId, clusterId);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     *
     * @param file 文件
     * @param dependenceName 依赖名
     * @param projectId 项目id
     * @param clusterId 集群id
     * @param path 上传路径
     * @param isDecompressed 是否解压
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{dependenceName}/file",method = RequestMethod.POST)
    public ActionReturnUtil uploadFile(@PathVariable("projectId") String projectId,
                                       @RequestParam(value = "clusterId" ) String clusterId,
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
    @RequestMapping(value = "/{dependenceName}/file",method = RequestMethod.DELETE)
    public ActionReturnUtil deleteFile(@PathVariable("projectId") String projectId,
                                       @RequestParam(value = "clusterId" ) String clusterId,
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
    @RequestMapping(value = "/{dependenceName}/file",method = RequestMethod.GET)
    public ActionReturnUtil listFile(@PathVariable("projectId") String projectId,
                                     @RequestParam(value = "clusterId" ) String clusterId,
                                     @PathVariable("dependenceName") String dependenceName,
                                     @RequestParam(value = "path") String path) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(dependenceService.listFile(dependenceName, projectId, clusterId, path));
    }
}
