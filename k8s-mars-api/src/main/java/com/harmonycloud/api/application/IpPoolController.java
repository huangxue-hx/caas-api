package com.harmonycloud.api.application;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.tenant.ProjectIpPoolDto;
import com.harmonycloud.service.application.IpPoolService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(value = "IpPoolController", description = "ip资源池接口")
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/ippools")
@RestController
public class IpPoolController {

    @Autowired
    private IpPoolService ipPoolService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * 获取资源池列表
     *
     * @param projectId 项目id
     * @param clusterId 集群id
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil getIpPool(@PathVariable(value = "projectId") String projectId,
                                      @RequestParam(value = "clusterId", required = false) String clusterId) {
        try {
            List<ProjectIpPoolDto> projectIpPoolDtos = ipPoolService.get(projectId, clusterId);
            return ActionReturnUtil.returnSuccessWithData(projectIpPoolDtos);
        } catch (MarsRuntimeException e) {
            return ActionReturnUtil.returnErrorWithData(e.getErrorCode());
        } catch (Exception e) {
            logger.error(ErrorCodeMessage.QUERY_FAIL.getReasonChPhrase(), e);
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.QUERY_FAIL);
        }
    }


    /**
     * 创建ip资源池
     *
     * @param poolDto model
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil createIpPool(@RequestBody ProjectIpPoolDto poolDto) {
        try {
            ipPoolService.create(poolDto);
            return ActionReturnUtil.returnSuccess();
        } catch (MarsRuntimeException e) {
            if (e.getErrorCode() != null) {
                return ActionReturnUtil.returnErrorWithData(e.getErrorCode());
            } else if (StringUtils.isNotBlank(e.getErrorMessage())) {
                return ActionReturnUtil.returnErrorWithMsg(e.getErrorMessage());
            } else {
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CREATE_FAIL);
            }
        } catch (Exception e) {
            logger.error(ErrorCodeMessage.CREATE_FAIL.getReasonChPhrase(), e);
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CREATE_FAIL);
        }
    }


    /**
     * 修改ip资源池
     *
     * @param poolDto model
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ActionReturnUtil updateIpPool(@RequestBody ProjectIpPoolDto poolDto) {
        try {
            ipPoolService.update(poolDto);
            return ActionReturnUtil.returnSuccess();
        } catch (MarsRuntimeException e) {
            if (e.getErrorCode() != null) {
                return ActionReturnUtil.returnErrorWithData(e.getErrorCode());
            } else if (StringUtils.isNotBlank(e.getErrorMessage())) {
                return ActionReturnUtil.returnErrorWithMsg(e.getErrorMessage());
            } else {
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL);
            }
        } catch (Exception e) {
            logger.error(ErrorCodeMessage.UPDATE_FAIL.getReasonChPhrase(), e);
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL);
        }
    }


    /**
     * 删除ip资源池
     *
     * @param projectId 项目id
     * @param name      名称
     * @param clusterId 集群id
     * @return
     */
    @RequestMapping(value = "/{name}", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteIpPool(@PathVariable(value = "projectId") String projectId,
                                         @PathVariable(value = "name") String name,
                                         @RequestParam(value = "clusterId") String clusterId) {
        try {
            ipPoolService.delete(projectId, clusterId, name);
            return ActionReturnUtil.returnSuccess();
        } catch (MarsRuntimeException e) {
            return ActionReturnUtil.returnErrorWithData(e.getErrorCode());
        } catch (Exception e) {
            logger.error(ErrorCodeMessage.DELETE_FAIL.getReasonChPhrase(), e);
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DELETE_FAIL);
        }
    }

    @RequestMapping(value = "/checkcluster", method = RequestMethod.GET)
    public ActionReturnUtil checkCluster(@PathVariable(value = "tenantId") String tenantId,
                                         @PathVariable(value = "projectId") String projectId) {
        try {
            ipPoolService.checkCluster(tenantId, projectId);
            return ActionReturnUtil.returnSuccess();
        } catch (MarsRuntimeException e) {
            return ActionReturnUtil.returnErrorWithData(e.getErrorCode());
        } catch (Exception e) {
            logger.error(ErrorCodeMessage.DELETE_FAIL.getReasonChPhrase(), e);
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DELETE_FAIL);
        }
    }


}
