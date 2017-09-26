package com.harmonycloud.api.harbor;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.integrationService.HarborIntegrationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.harmonycloud.service.platform.bean.HarborProjectQuota;
import com.harmonycloud.service.platform.serviceImpl.harbor.HarborServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by andy on 17-1-19.
 */
@Controller
public class HarborController {

    @Autowired
    private HarborIntegrationService harborIntegrationService;
    @Autowired
    private HarborServiceImpl harborServiceImpl;

    @Value("#{propertiesReader['image.domain']}")
    private String harborDomain;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 镜像脆弱性分析 - project纬度
     *
     * @param name project name
     * @return
     */
    @RequestMapping(value = "/harborProject/security/clairStatistcsOfProject", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil clairStatistcsOfProject(@RequestParam(value = "name", required = true) String name) throws Exception{
        if (StringUtils.isEmpty(name)) {
            return ActionReturnUtil.returnErrorWithMsg("name cannot be null");
        }
        try {
            return harborIntegrationService.clairStatistcsOfProject(name);
        } catch (Exception e) {
        	throw e;
        }
    }

    /**
     * 镜像脆弱性分析 - project纬度
     *
     * @param name project name
     * @return
     */
    @RequestMapping(value = "/harborProject/url", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getHarborUrl() throws Exception{
        try {
            return ActionReturnUtil.returnSuccessWithData(harborDomain);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 镜像脆弱性分析 - user纬度
     *
     * @param name username
     * @return
     */
    @RequestMapping(value = "/harborProject/security/clairStatistcsOfUser", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil clairStatistcsOfUser(@RequestParam(value = "name") String name) throws Exception{

        try {
            return harborIntegrationService.clairStatistcsOfUser(name);
        } catch (Exception e) {
        	throw e;
        }
    }

    /**
     * 查询namespace下harbor用户列表
     * @param tenantname
     * @param namespace
     * @return
     */
    @RequestMapping(value = "/harborProject/namespace/user", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getUserList(@RequestParam(value = "tenantname") String tenantname,
                                        @RequestParam(value = "namespace") String namespace)throws Exception{

        try {
            return harborIntegrationService.getUserList(tenantname, namespace);
        } catch (Exception e) {
        	throw e;
        }
    }

    /**
     * tag detail
     *
     * @param repoName repo name
     * @param tag      tag
     * @return
     */
    //@RequestMapping(value = "/image/repo/tag", method = RequestMethod.GET)

    //@ResponseBody
    @RequestMapping(value = "/image/security/clairStatistcs", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil tagDetail(@RequestParam(value = "repoName", required = true) String repoName, @RequestParam(value = "tag", required = true) String tag) throws Exception{
        if (StringUtils.isEmpty(repoName)) {
            return ActionReturnUtil.returnErrorWithMsg("repo name cannot be null");
        }
        if (StringUtils.isEmpty(tag)) {
            return ActionReturnUtil.returnErrorWithMsg("tag cannot be null");
        }
        try {
            return harborIntegrationService.manifestsOfTag(repoName, tag);
        } catch (Exception e) {
        	throw e;
        }
    }

    /**
     * 获取repo详情
     *
     * @param pid projectId
     * @return
     */
//    @RequestMapping(value = "/image/repo", method = RequestMethod.GET)
//    @ResponseBody
    public ActionReturnUtil getRepoDetail(@RequestParam(value = "pid", required = true) Integer pid) throws Exception{
        if (pid == null || pid < 0) {
            return ActionReturnUtil.returnErrorWithMsg("harbor projectId is invalid");
        }
        try {
            return harborIntegrationService.getRepoDomainDetailByProjectId(pid);
        } catch (Exception e) {
        	throw e;
        }
    }

    /**update projectquota
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/updatequota", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil copyImage(@RequestParam(value = "projectId") String projectID,@ModelAttribute HarborProjectQuota harborProjectQuota)throws Exception{
        try {
            logger.info("升级镜像仓库project配额");
            return this.harborServiceImpl.updateProjectQuota(Integer.valueOf(projectID),harborProjectQuota);
        } catch (Exception e) {
            logger.info("升级镜像仓库project配额失败:projectId="+projectID);
            throw e;
        }
    }
    /**get projectquota
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/quota", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil copyImage(@RequestParam(value = "project_name") String projectName)throws Exception{
        try {
            logger.info("获取镜像仓库project配额");
            return this.harborServiceImpl.getProjectQuota(projectName);
        } catch (Exception e) {
            logger.info("获取镜像仓库project配额失败:projectName="+projectName);
            throw e;
        }
    }

    /**get repoTagList
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/tagList", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getRepoTags(@RequestParam(value = "repository_name") String repositroyName)throws Exception{
        try {
            logger.info("get repositroy's tags");
            return this.harborServiceImpl.getTagsByRepoName(repositroyName);
        } catch (Exception e) {
            logger.info("get repositroy's tags failed:repositroyName="+repositroyName);
            throw e;
        }
    }

    /**get project detail
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/projectInfo", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getProjectInformation(@RequestParam(value = "project_name") String projectName)throws Exception{
        try {
            logger.info("get project's informations");
            return this.harborServiceImpl.getPolicyDetailList(projectName);
        } catch (Exception e) {
            logger.info("get project's informations failed:repositroyName="+projectName);
            throw e;
        }
    }
    /**镜像repository的模糊查询
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/search", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getFuzzySearchResult(@RequestParam(value = "query") String query,@RequestParam(value="tenantID") String tenantID ,@RequestParam(value="isPublic") String isPublic)throws Exception{
        if (StringUtils.isEmpty(query)) {
            return ActionReturnUtil.returnSuccess();
        }
        try {
            logger.info("模糊查询");
            return this.harborServiceImpl.getRepoFuzzySearch(query,tenantID,isPublic);
        } catch (Exception e) {
            logger.info("模糊查询失败:tenantID="+tenantID);
            throw e;
        }
    }

    /**查询指定租户的镜像
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborImage/search", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getImageByTenantID(@RequestParam(value="tenantID") String tenantID)throws Exception{
        try {
            logger.info("查看租户的镜像列表");
            return this.harborServiceImpl.getImageByTenantID(tenantID);
        } catch (Exception e) {
            logger.info("查看租户镜像列表失败:tenantID="+tenantID);
            throw e;
        }
    }

    /**查询满足条件的默认一个镜像
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborImage/search/first", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getFirstImageByTenantID(@RequestParam(value="tenantID") String tenantID,
                                                    @RequestParam(value="projectName", required = false) String projectName,
                                                    @RequestParam(value="repoName", required = false) String repoName)throws Exception{
        try {
            return this.harborServiceImpl.getDefaultImageByTenantID(tenantID, projectName, repoName);
        } catch (Exception e) {
            logger.info("查询租户的第一个镜像失败:tenantID="+tenantID);
            throw e;
        }
    }
    
    /**查询满足条件的默认一个镜像
    *
    * @return
    * @throws Exception
    */
   @RequestMapping(value = "/harborImage/search/repo", method = RequestMethod.GET)
   @ResponseBody
   public ActionReturnUtil getRepoByTenantID(@RequestParam(value="tenantID") String tenantID)throws Exception{
       try {
    	   if(StringUtils.isEmpty(tenantID)){
    		  return ActionReturnUtil.returnErrorWithMsg("租户为空"); 
    	   }
           return this.harborServiceImpl.getRepoByTenantID(tenantID);
       } catch (Exception e) {
           logger.info("查询租户的第一个镜像失败:tenantID="+tenantID);
           throw e;
       }
   }

}


