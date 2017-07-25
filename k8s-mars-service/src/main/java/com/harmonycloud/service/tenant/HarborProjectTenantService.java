package com.harmonycloud.service.tenant;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.tenant.bean.HarborProjectTenant;

/**
 * Created by zhangsl on 16/11/13.
 */
@Transactional(rollbackFor = Exception.class)
public interface HarborProjectTenantService {

    HarborProjectTenant getByHarborProjectId(Long harborProjectTenantId) throws Exception;

    List<HarborProjectTenant> harborProjectList();

    int create(String harborProjectId, String tenantId, String name,Integer isPublic) throws Exception;

    int delete(String harborProjectTenantId) throws Exception;

    /**
     * 创建harbor project
     * @param name
     * @param tenantid
     * @return
     */
    ActionReturnUtil createHarborProject(String name, String tenantid,Float quotaSize) throws Exception;

    /**
     * 删除harbor project
     * @param tenantname
     * @param tenantid
     * @param projectid
     * @return
     */
    ActionReturnUtil deleteHarborProject(String tenantname, String tenantid, String projectid) throws Exception;

    /**
     * 查询租户下harbor projectharbor project
     * @param tenantid
     * @return
     */
    ActionReturnUtil getProjectList(String tenantid,Integer isPublic,boolean quota) throws Exception;
    /**
     * 获取简单镜像仓库列表
     * @param tenantid
     * @return
     * @throws Exception
     */
    public List<HarborProjectTenant> getSimplProjectList(String tenantid) throws Exception;

    /**
     * 查询harbor project 详情
     * @param tenantid
     * @param projectid
     * @return
     */
    ActionReturnUtil getProjectDetail(String tenantid, Integer projectid) throws Exception;

    /**
    * 删除租户所有的镜像仓库数据
     * @param tenantid
     * @return
     */
    ActionReturnUtil clearTenantProject(String tenantid)throws  Exception;
    /**
     * 添加租户用户到镜像仓库
      * @param tenantid
      * @return
      */
     ActionReturnUtil addProjctsToUser(String username,String tenantid)throws  Exception;
}
