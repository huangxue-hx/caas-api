package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.application.ServiceTemplatesMapper;
import com.harmonycloud.dao.application.bean.ApplicationTemplates;
import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.dto.application.ApplicationTemplateDto;
import com.harmonycloud.dto.application.CreateContainerDto;
import com.harmonycloud.dto.application.DeploymentDetailDto;
import com.harmonycloud.dto.application.ServiceTemplateDto;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.service.ReplicasetsService;
import com.harmonycloud.service.application.ApplicationService;
import com.harmonycloud.service.application.ApplicationTemplateService;
import com.harmonycloud.service.application.ApplicationWithServiceService;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.application.Util.TemplateToYamlUtil;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.harmonycloud.common.Constant.CommonConstant.BLANKSTRING;

/**
 * Created by root on 3/29/17.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApplicationServiceImpl implements ApplicationService {
    private static Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    @Autowired
    private ApplicationTemplateService appTemplateService;

    @Autowired
    private ApplicationWithServiceService appWithServiceService;

    @Autowired
    private ServiceTemplatesMapper serviceTemplatesMapper;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private ReplicasetsService rsService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private HttpSession session;

    @Autowired
    private UserService userService;
    @Autowired
    private RoleLocalService roleLocalService;

    @Autowired
    private ClusterService clusterService;

    /**
     * remove appTemplate on 17/04/07.
     *
     * @param name
     * @return ActionReturnUtil
     */
    @Override
    public ActionReturnUtil deleteApplicationTemplate(String name, String projectId, String clusterId) throws Exception {
        AssertUtil.notBlank(name, DictEnum.NAME);
        // delete application_template
        appTemplateService.deleteApplicationTemplate(name, projectId, clusterId);
        // delete application__service
        appWithServiceService.deleteApplicationService(name, projectId, clusterId);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * get AppTemplate on 17/05/04.
     *
     * @param name
     * @param tag
     * @return {@link ActionReturnUtil}
     */
    @Override
    public ActionReturnUtil getApplicationTemplate(String name, String tag, String clusterId, String projectId) throws Exception {
        // check params
        if (StringUtils.isBlank(name) || StringUtils.isBlank(projectId) || (!"all".equals(projectId) && StringUtils.isBlank(clusterId))) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        // select a application Template
        String newClusterId = "all".equals(projectId)? null : clusterId;
        ApplicationTemplates applicationTemplates = appTemplateService.getApplicationTemplatesByNameAndTag(name, tag, newClusterId, projectId);
        JSONObject js = new JSONObject();
        if (applicationTemplates != null) {
            js.put("name", applicationTemplates.getName());
            js.put("desc", (applicationTemplates.getDetails() != null) ? applicationTemplates.getDetails() : "");
            js.put("id", applicationTemplates.getId());
            js.put("image", applicationTemplates.getImageList());
            js.put("tag", applicationTemplates.getTag());
            js.put("tenant", applicationTemplates.getTenant());
            js.put("clusterId", applicationTemplates.getClusterId());
            js.put("user", applicationTemplates.getUser());
            js.put("realName", userService.getUser(applicationTemplates.getUser()).getRealName());
            js.put("createTime", dateToString(applicationTemplates.getCreateTime()));
            JSONArray array = new JSONArray();
            List<Object> deploymentListToyaml = new ArrayList<>();
            // select service Template
            List<com.harmonycloud.dao.application.bean.ApplicationService> applicationServiceList = appWithServiceService.listApplicationServiceByAppTemplatesId(applicationTemplates.getId());
            if (applicationServiceList != null) {
                for (com.harmonycloud.dao.application.bean.ApplicationService applicationService : applicationServiceList) {
                    ServiceTemplates serviceTemplates = serviceTemplatesMapper.getServiceTemplatesByID(applicationService.getServiceId());
                    if (serviceTemplates != null) {
                        JSONObject json = new JSONObject();
                        json.put("id", serviceTemplates.getId());
                        json.put("name", serviceTemplates.getName());
                        if (serviceTemplates.getTag() != null) {
                            json.put("tag", serviceTemplates.getTag());
                        } else {
                            json.put("tag", "");
                        }
                        json.put("isExternal", serviceTemplates.getFlag());
                        if (serviceTemplates.getNodeSelector() != null) {
                            json.put("nodeSelector", serviceTemplates.getNodeSelector());
                        } else {
                            json.put("nodeSelector", "");
                        }

                        json.put("deployment", (serviceTemplates.getDeploymentContent() != null) ? serviceTemplates.getDeploymentContent().toString().replace("null", "\"\"") : "");
                        json.put("ingress", (serviceTemplates.getIngressContent() != null) ? serviceTemplates.getIngressContent().toString().replace("null", "\"\"") : "");
                        json.put("imageList", (serviceTemplates.getImageList() != null) ? serviceTemplates.getImageList() : "");
                        json.put("user", (serviceTemplates.getUser() != null) ? serviceTemplates.getUser() : "");
                        json.put("tenant", (serviceTemplates.getTenant() != null) ? serviceTemplates.getTenant() : "");
                        json.put("details", (serviceTemplates.getDetails() != null) ? serviceTemplates.getDetails() : "");
                        array.add(json);
                        if (StringUtils.isNotBlank(serviceTemplates.getDeploymentContent())) {
                            String dep = json.getJSONArray("deployment").getJSONObject(0).toString().replaceAll(":\"\",", ":" + null + ",").replaceAll(":\"\"", ":" + null + "");
                            DeploymentDetailDto deployment = JsonUtil.jsonToPojo(dep, DeploymentDetailDto.class);

                            Deployment deploymentToYaml = TemplateToYamlUtil.templateToDeployment(deployment);
                            com.harmonycloud.k8s.bean.Service serviceYaml = TemplateToYamlUtil.templateToService(deployment);

                            deploymentListToyaml.add(serviceYaml);

                            deploymentListToyaml.add(deploymentToYaml);
                        }
                    }
                }
            }
            js.put("servicelist", array);
            Yaml yaml = new Yaml();
            if (deploymentListToyaml != null) {
                String yamlC = convertYaml(yaml.dumpAsMap(deploymentListToyaml));
                js.put("yaml", yamlC);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(js);
    }

    /**
     * get application template by name or image and projectId service on 17/04/07.
     *
     * @param searchKey   关键字搜索key
     * @param searchValue 关键字搜索value
     * @param projectId   项目Id
     * @param isPublic    是否共有
     * @return ActionReturnUtil
     * @throws Exception
     * @Modified tenant -> projectId
     */
    @Override
    public ActionReturnUtil listApplicationTemplate(String searchKey, String searchValue, boolean isPublic, String projectId, String clusterId) throws Exception {
        //公私模板
        if (isPublic) {
            //公有模板
            return listPublicApplicationTemplate(searchKey, searchValue);
        } else {
            //私有模板
            return listPrivateAppTemplate(searchKey, searchValue, projectId, clusterId);
        }
    }

    /**
     * 公有模板
     */
    private ActionReturnUtil listPublicApplicationTemplate(String searchKey, String searchValue) throws Exception {
        JSONArray array = new JSONArray();
        if (StringUtils.isEmpty(searchValue)) {
            // search value is null
            List<ApplicationTemplates> appTemplatesList = appTemplateService.listPublicTemplate();
            for (int i = 0; i < appTemplatesList.size(); i++) {
                List<ApplicationTemplates> list = appTemplateService.listApplicationTemplatesByName(appTemplatesList.get(i).getName(), appTemplatesList.get(i).getTenant(), true, appTemplatesList.get(i).getProjectId());
                array.add(getApplicationTemplates(list));
            }
        } else {
            if (!"name".equals(searchKey) && !"image".equals(searchKey)) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.TEMPLATE_SEARCH_KEY_ERROR);
            }
            if ("name".equals(searchKey)) {
                // search by name
                List<ApplicationTemplates> appTemplatesList = appTemplateService.listPublicNameByName("%" + searchValue + "%");
                for (int i = 0; i < appTemplatesList.size(); i++) {
                    List<ApplicationTemplates> list = appTemplateService.listApplicationTemplatesByName(appTemplatesList.get(i).getName(), appTemplatesList.get(i).getTenant(), true, appTemplatesList.get(i).getProjectId());
                    array.add(getApplicationTemplates(list));
                }
            }
            if ("image".equals(searchKey)) {
                // search by image
                List<ApplicationTemplates> appTemplatesList = appTemplateService.listPublicNameByImage(searchValue);
                for (int i = 0; i < appTemplatesList.size(); i++) {
                    List<ApplicationTemplates> list = appTemplateService.listApplicationTemplatesByNameAndImage(appTemplatesList.get(i).getName(), searchValue, appTemplatesList.get(i).getTenant(), appTemplatesList.get(i).getProjectId());
                    array.add(getApplicationTemplates(list));
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(array);
    }

    /**
     * 私有应用模板查询
     *
     * @param searchKey   关键字查询key
     * @param searchValue 关键字查询value
     * @param projectId   项目Id
     * @return ActionReturnUtil
     * @throws Exception
     * @Modified tenant -> projectId(从租户角度变成项目)
     */
    private ActionReturnUtil listPrivateAppTemplate(String searchKey, String searchValue, String projectId, String clusterId) throws Exception {
        JSONArray array = new JSONArray();
        Set<String> clusterIdList = new HashSet<>();
        if (StringUtils.isEmpty(clusterId)) {
            //获取当前角色的集群
            clusterIdList = roleLocalService.listCurrentUserRoleClusterIds();
        } else {
            clusterIdList.add(clusterId);
        }
        for (String cId : clusterIdList) {
            if (StringUtils.isEmpty(searchValue)) {
                List<ApplicationTemplates> appTemplatesList = appTemplateService.listNameByProjectId(projectId, cId);
                for (int i = 0; i < appTemplatesList.size(); i++) {
                    List<ApplicationTemplates> list = appTemplateService.listApplicationTemplatesByName(appTemplatesList.get(i).getName(), appTemplatesList.get(i).getClusterId(), false, projectId);
                    array.add(getApplicationTemplates(list));
                }
            } else {
                List<ApplicationTemplates> appTemplatesList = appTemplateService.listNameByName("%" + searchValue + "%", cId, projectId);
                for (int i = 0; i < appTemplatesList.size(); i++) {
                    List<ApplicationTemplates> list = appTemplateService.listApplicationTemplatesByName(appTemplatesList.get(i).getName(), appTemplatesList.get(i).getClusterId(), false, projectId);
                    array.add(getApplicationTemplates(list));
                }
                // search by image
                List<ApplicationTemplates> appTemplatesImageList = appTemplateService.listNameByImage(searchValue, cId, projectId);
                for (int i = 0; i < appTemplatesImageList.size(); i++) {
                    List<ApplicationTemplates> list = appTemplateService.listApplicationTemplatesByNameAndImage(appTemplatesImageList.get(i).getName(), searchValue, cId, projectId);
                    array.add(getApplicationTemplates(list));
                }
                //对数组进行去重
                if (array != null && !array.isEmpty()) {
                    array = uniqueArray(array);
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(array);
    }

    @Override
    public ActionReturnUtil getApplicationTemplateYaml(ApplicationTemplateDto appTemplate) throws Exception {
        List<Object> deploymentListToyaml = new ArrayList<>();
        if (appTemplate != null && appTemplate.getServiceList() != null) {
            for (ServiceTemplateDto svcOne : appTemplate.getServiceList()) {
                if (svcOne.getDeploymentDetail() != null) {
//                    // set nodeselector
//                    if (svcOne.getDeploymentDetail() != null) {
//                        if(svcOne.getDeploymentDetail().getNodeSelector() !=null  && !"".equals(svcOne.getDeploymentDetail().getNodeSelector())) {
//                            svcOne.getDeploymentDetail().setNodeSelector(Constant.NODESELECTOR_LABELS_PRE+svcOne.getDeploymentDetail().getNodeSelector());
//                        }else {
//                            String tenantid = (String) session.getAttribute("tenantId");
//                            ActionReturnUtil l = namespaceService.getPrivatePartitionLabel(tenantid, svcOne.getDeploymentDetail().getNamespace());
//                            if(!l.isSuccess()) {
//                                return l;
//                            }
//                            String lal = (String) l.get("data");
//                            svcOne.getDeploymentDetail().setNodeSelector(lal);
//                        }
//                    }

                    Deployment deploymentToYaml = TemplateToYamlUtil.templateToDeployment(svcOne.getDeploymentDetail());
                    com.harmonycloud.k8s.bean.Service serviceYaml = TemplateToYamlUtil.templateToService(svcOne.getDeploymentDetail());
                    deploymentListToyaml.add(serviceYaml);
                    deploymentListToyaml.add(deploymentToYaml);
                }
            }
        }
        Yaml yaml = new Yaml();
        String yamlc = "";
        if (yaml.dumpAsMap(deploymentListToyaml) != null) {
            yamlc = convertYaml(yaml.dumpAsMap(deploymentListToyaml));
        }
        return ActionReturnUtil.returnSuccessWithData(yamlc);
    }

    /**
     * create a appTemplate service implement on 17/04/07.
     *
     * @param appTemplate required
     * @param userName    required
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized ActionReturnUtil saveApplicationTemplate(ApplicationTemplateDto appTemplate, String userName) throws Exception {
        // check value
        if (StringUtils.isEmpty(userName) || appTemplate == null || CollectionUtils.isEmpty(appTemplate.getServiceList()) || StringUtils.isBlank(appTemplate.getProjectId())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        ApplicationTemplates applicationTemplates = new ApplicationTemplates();
        if (Constant.APPLICATION_TEMPLATE_DEPLOYED == appTemplate.getIsDeploy()) {
            applicationTemplates.setStatus(Constant.TEMPLATE_STATUS_DELETE);
        } else {
            double appTag = Constant.TEMPLATE_TAG;
            applicationTemplates.setTag(String.valueOf(appTag));
            applicationTemplates.setStatus(Constant.TEMPLATE_STATUS_CREATE);
        }
        // add application templates
        applicationTemplates.setName(appTemplate.getName());
        applicationTemplates.setUser(userName);
        applicationTemplates.setCreateTime(new Date());
        applicationTemplates.setTenant(appTemplate.getTenant());
        applicationTemplates.setDetails(appTemplate.getDesc());
        applicationTemplates.setPublic(appTemplate.isPublic());
        applicationTemplates.setProjectId(appTemplate.getProjectId());
        applicationTemplates.setClusterId(appTemplate.getClusterId());
        appTemplateService.saveApplicationTemplates(applicationTemplates);
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> maps = new HashMap<String, Object>();
        List<Map<String, Object>> idList = new ArrayList<Map<String, Object>>();
        int appTemplatesId = applicationTemplates.getId();
        map.put(applicationTemplates.getName(), appTemplatesId + "");

        //该变量用来保存应用下的服务用到的所有镜像
        List<String> list = new LinkedList<String>();

        // addSave service templates
        for (ServiceTemplateDto serviceTemplate : appTemplate.getServiceList()) {

            //将list作为参数不断的传递，然后将当前服务所用到的镜像返回给listR, listR中存放的元素0：模板名称和id；1：镜像列表信息
            List<Object> listR = saveServiceTemplates(serviceTemplate, appTemplatesId, appTemplate.getTenant(), userName, list, Constant.TEMPLATE_STATUS_DELETE);
            //获取每个内部服务镜像
            if (serviceTemplate.getExternal() == null || serviceTemplate.getExternal() == 0) {

                //镜像列表信息保存的列表的第二个位置，所以需要get(1)
                if (listR.get(1) != null) {
                    list = (List<String>) listR.get(1);
                }
            }
            maps.putAll((Map<String, Object>) listR.get(0));
        }
        if (list != null && list.size() > 0) {
            appTemplateService.updateImageById(splitString(list), appTemplatesId);
        }

        idList.add(map);
        idList.add(maps);
        return ActionReturnUtil.returnSuccessWithData(idList);
    }

    /**
     * list images on 17/04/07.
     *
     * @param containers
     * @param imageList
     * @return imageList
     */
    private List<String> listImages(List<CreateContainerDto> containers, List<String> imageList) {
        for (CreateContainerDto container : containers) {
            if (!imageList.contains(container.getImg())) {
                imageList.add(container.getImg());
            }
        }
        return imageList;
    }

    /**
     * save appTemplates and serviceTemplates mapper on 17/04/07.
     *
     * @param appTemplatesId
     * @param serviceTemplateId
     * @param status
     * @param external
     */
    private void saveApplicationService(Integer appTemplatesId, Integer serviceTemplateId, Integer status, Integer external) throws Exception {
        List<com.harmonycloud.dao.application.bean.ApplicationService> list = appWithServiceService.selectAppServiceByAppId(appTemplatesId, serviceTemplateId);
        if (list == null || list.size() <= 0) {
            com.harmonycloud.dao.application.bean.ApplicationService applicationService = new com.harmonycloud.dao.application.bean.ApplicationService();
            applicationService.setApplicationId(appTemplatesId);
            applicationService.setServiceId(serviceTemplateId);
            applicationService.setStatus(status);
            applicationService.setIsExternal(external);
            appWithServiceService.insert(applicationService);
        }
    }

    /**
     * save serviceTemplates on 17/04/07.
     *
     * @param serviceTemplate
     * @param appTemplatesId
     * @param tenant
     * @param userName
     * @param imageList
     * @return imageList
     */
    private List<Object> saveServiceTemplates(ServiceTemplateDto serviceTemplate, Integer appTemplatesId, String tenant, String userName, List<String> imageList, int type)
            throws Exception {
        List<Object> result = new ArrayList<Object>();

        //添加删除模板
        ActionReturnUtil res = serviceService.saveServiceTemplate(serviceTemplate, userName, type);
        if (res.isSuccess()) {
            JSONObject json = new JSONObject();
            json = (JSONObject) res.get("data");
            result.add(json);
            // add application - service template
            saveApplicationService(appTemplatesId, Integer.parseInt(json.get(serviceTemplate.getName()).toString()), Constant.TEMPLATE_STATUS_CREATE, Constant.K8S_SERVICE);
        }
        listImages(serviceTemplate.getDeploymentDetail().getContainers(), imageList);
        result.add(imageList);

        return result;
    }

    /**
     * List to String on 17/04/07.
     *
     * @param strList
     * @return sb
     */
    private String splitString(List<String> strList) throws Exception {
        StringBuffer sb = new StringBuffer();
        if (strList != null) {
            for (int i = 0; i < strList.size(); i++) {
                sb.append(strList.get(i)).append(",");
            }
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * get appTemplate overview on 17/05/05.
     *
     * @param appTemplatesList 应用模板列表
     * @return JSONObject
     * @throws Exception
     */
    private JSONObject getApplicationTemplates(List<ApplicationTemplates> appTemplatesList) throws Exception {
        JSONObject json = new JSONObject();
        if (appTemplatesList != null && appTemplatesList.size() > 0) {
            json.put("name", appTemplatesList.get(0).getName());
            if ("all".equals(appTemplatesList.get(0).getTenant())) {
                json.put("public", true);
            } else {
                json.put("public", false);
            }
            JSONArray tagArray = new JSONArray();
            for (int i = 0; i < appTemplatesList.size(); i++) {
                JSONObject idAndTag = new JSONObject();
                idAndTag.put("id", appTemplatesList.get(i).getId());
                idAndTag.put("tag", appTemplatesList.get(i).getTag());
                idAndTag.put("image", appTemplatesList.get(i).getImageList());
                idAndTag.put("user", appTemplatesList.get(i).getUser());
                idAndTag.put("realName", userService.getUser(appTemplatesList.get(i).getUser()).getRealName());
                tagArray.add(idAndTag);
                json.put("createtime", dateToString(appTemplatesList.get(i).getCreateTime()));
            }
            json.put("tags", tagArray);
            json.put("clusterId", appTemplatesList.get(0).getClusterId());
            json.put("clusterName", "all".equals(appTemplatesList.get(0).getProjectId())?null : clusterService.findClusterById(appTemplatesList.get(0).getClusterId()).getAliasName());
        }
        return json;
    }

    private static String dateToString(Date time) {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ctime = formatter.format(time);

        return ctime;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ActionReturnUtil updateApplicationTemplate(ApplicationTemplateDto appTemplate, String userName)
            throws Exception {
        // add application templates
        ApplicationTemplates applicationTemplates = new ApplicationTemplates();
        applicationTemplates.setName(appTemplate.getName());
        applicationTemplates.setUser(userName);
        applicationTemplates.setUpdateTime(new Date());
        applicationTemplates.setTenant(appTemplate.getTenant());
        applicationTemplates.setDetails(appTemplate.getDesc());
        applicationTemplates.setStatus(Constant.TEMPLATE_STATUS_CREATE);
        applicationTemplates.setId(appTemplate.getId());
        applicationTemplates.setTag(appTemplate.getTag());
        applicationTemplates.setClusterId(appTemplate.getClusterId());
        appTemplateService.updateApplicationTemplate(applicationTemplates);
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> maps = new HashMap<String, Object>();
        List<Map<String, Object>> idList = new ArrayList<Map<String, Object>>();
        int appTemplatesId = applicationTemplates.getId();
        map.put(applicationTemplates.getName(), appTemplatesId + "");
        // check service templates
        if (appTemplate.getServiceList().size() <= 0) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_TEMPLATE_NOT_BLANK);
        }
        List<String> list = new LinkedList<String>();
        List<Object> listR = new LinkedList<Object>();
        // addSave service templates

        //删除服务模板
        List<com.harmonycloud.dao.application.bean.ApplicationService> bsList = appWithServiceService.listApplicationServiceByAppTemplatesId(appTemplate.getId());
        if (bsList != null && bsList.size() > 0) {
            for (com.harmonycloud.dao.application.bean.ApplicationService bs : bsList) {
                if (bs.getServiceId() != null) {
                    serviceService.delById(bs.getServiceId());
                }
            }
        }
        //删除已有的业务模板和应用模板的Mapper联系
        appWithServiceService.deleteApplicationServiceByAppTemplateId(appTemplatesId);
        for (ServiceTemplateDto serviceTemplate : appTemplate.getServiceList()) {
            listR = saveServiceTemplates(serviceTemplate, appTemplatesId, appTemplate.getTenant(), userName, list, 1);
            //内部服务镜像
            if (serviceTemplate.getExternal() == null && Constant.K8S_SERVICE.equals(serviceTemplate.getExternal())) {
                if (listR.get(1) != null) {
                    list = (List<String>) listR.get(1);
                }
            }
            //存放应用模板的name和id
            maps.putAll((Map<String, Object>) listR.get(0));
        }
        if (list != null && list.size() > 0) {
            //更新镜像
            appTemplateService.updateImageById(splitString(list), appTemplatesId);
        }
        idList.add(map);
        idList.add(maps);
        return ActionReturnUtil.returnSuccessWithData(idList);
    }

    @Override
    public ActionReturnUtil getApplicationTemplateByName(String name, String clusterId, boolean isPublic, String projectId) throws Exception {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(projectId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //判断tenant的名称是否为all
        String newProjectId = StringUtils.isEmpty(clusterId) ? "all" : projectId;
        List<ApplicationTemplates> list = appTemplateService.listApplicationTemplatesByName(name, clusterId, isPublic, newProjectId);
        JSONObject json = new JSONObject();
        if (list != null && list.size() > 0) {
            JSONArray array = new JSONArray();
            json.put("name", name);
            for (ApplicationTemplates bt : list) {
                JSONObject js = new JSONObject();
                js.put("tag", bt.getTag());
                js.put("id", bt.getId());
                array.add(js);
            }
            json.put("tags", array);
            return ActionReturnUtil.returnSuccessWithData(json);
        } else {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.APPLICATION_TEMPLATE_NOT_EXIST);
        }

    }

    @Override
    public ActionReturnUtil addServiceTemplateByName(ApplicationTemplateDto appTemplate, String userName)
            throws Exception {
        AssertUtil.notNull(appTemplate, DictEnum.APPLICATION_TEMPLATE);
        AssertUtil.notEmpty(appTemplate.getServiceList(), DictEnum.APPLICATION_TEMPLATE);
        //添加更新应用模板
        List<ServiceTemplateDto> list = appTemplate.getServiceList();
        JSONObject json = new JSONObject();

        for (ServiceTemplateDto s : list) {
            ActionReturnUtil serRes = serviceService.saveServiceTemplate(s, userName, 1);
            if (!serRes.isSuccess()) {
                return serRes;
            }
            JSONObject js = (JSONObject) serRes.get("data");
            s.setId(js.getInt(s.getName()));
            json.putAll(js);
            //添加业务模板和应用模板Mapper
            saveApplicationService(appTemplate.getId(), s.getId(), Constant.TEMPLATE_STATUS_CREATE, Constant.K8S_SERVICE);
        }
        return ActionReturnUtil.returnSuccessWithData(json);
    }

    @Override
    public ActionReturnUtil updateServiceTemplateByName(ApplicationTemplateDto appTemplate, String userName)
            throws Exception {
        AssertUtil.notNull(appTemplate, DictEnum.APPLICATION_TEMPLATE);
        AssertUtil.notEmpty(appTemplate.getServiceList(), DictEnum.APPLICATION_TEMPLATE);
        //更新应用模板
        List<ServiceTemplateDto> list = appTemplate.getServiceList();
        for (ServiceTemplateDto s : list) {
            if (s.getFlag() != null && s.getFlag() == 1) {
                serviceService.updateServiceTemplate(s, userName);
            }
            //添加业务模板和应用模板Mapper
            saveApplicationService(appTemplate.getId(), s.getId(), Constant.TEMPLATE_STATUS_CREATE, Constant.K8S_SERVICE);
        }

        return ActionReturnUtil.returnSuccess();
    }

    public String convertYaml(String yaml) {
        ByteArrayInputStream is = new ByteArrayInputStream(yaml.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String line = "";
        try {
            while ((line = br.readLine()) != null) {
                if (line != null && !line.contains("!!") && !line.contains("null")) {
                    if (line.length() > 2) {
                        String lineNew = line.substring(2, line.length());
                        sb.append(lineNew + "\n");
                    } else {
                        sb.append(line + "\n");
                    }
                }
            }
        } catch (IOException e) {
            logger.error("convertYaml failed", e);
        } finally {
            try {
                br.close();
                is.close();
            } catch (IOException e) {
                logger.error("try to close io stream failed", e);
            }
        }
        return sb.toString();
    }

    @Override
    public ActionReturnUtil listServiceTemplatePublic() throws Exception {
        JSONArray array = new JSONArray();
        List<ApplicationTemplates> appTemplatesList = appTemplateService.listPublic();
        for (int i = 0; i < appTemplatesList.size(); i++) {
            List<ApplicationTemplates> list = appTemplateService.listApplicationTemplatesByName(appTemplatesList.get(i).getName(), appTemplatesList.get(i).getClusterId(), false, appTemplatesList.get(i).getProjectId());
            array.add(getApplicationTemplates(list));
        }
        return ActionReturnUtil.returnSuccessWithData(array);
    }

    @Override
    public ActionReturnUtil switchPub(String name, boolean isPublic) throws Exception {
        if (StringUtils.isBlank(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        appTemplateService.updateApplicationTemplatePublic(name, isPublic);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public void deleteTemplatesInProject(String projectId) throws Exception {
        //删除应用模板
        String[] projectIds = new String[]{projectId};
        appTemplateService.deleteByProjectIds(projectIds);
        //删除服务模板
        serviceTemplatesMapper.deleteByProjects(projectIds);
    }

    @Override
    public Map<String, Long> getAppTemplateResource(String name, String tag, String clusterId, String projectId) throws Exception {
        ApplicationTemplates applicationTemplates = appTemplateService.getApplicationTemplatesByNameAndTag(name, tag, clusterId, projectId);
        Integer appTemId = (null != applicationTemplates) ? applicationTemplates.getId() : null;
        if (Objects.isNull(appTemId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.APPLICATION_TEMPLATE_NOT_EXIST);
        }
        //获取应用模板对应的服务信息
        List<com.harmonycloud.dao.application.bean.ApplicationService> applicationServiceList = appWithServiceService.listApplicationServiceByAppTemplatesId(appTemId);
        if (null == applicationServiceList) {
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_TEMPLATE_NOT_EXIST);
        }
        long cpuTotal = 0;          //单位m
        long memoryTotal = 0;       //单位是MB
        for (com.harmonycloud.dao.application.bean.ApplicationService applicationService : applicationServiceList) {
            ServiceTemplates serviceTemplates = serviceTemplatesMapper.getServiceTemplatesByID(applicationService.getServiceId());
            if (null == serviceTemplates) {
                throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_TEMPLATE_NOT_EXIST);
            }
            Map<String, Long> serviceRequireRes = serviceService.getServiceRequireResource(serviceTemplates);
            cpuTotal += serviceRequireRes.get("cpuNeed");
            memoryTotal += serviceRequireRes.get("memoryNeed");
        }
        Map<String, Long> res = new HashMap<>();
        res.put("cpuNeed", cpuTotal);
        res.put("memoryNeed", memoryTotal);
        return res;
    }

    @Override
    public ActionReturnUtil checkAppTemplateName(String name, String projectId, String clusterId) throws Exception {
        List<ApplicationTemplates> appTpls = appTemplateService.listApplicationTemplatesByName(name, clusterId, false, projectId);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(appTpls)) {
            Integer AppTmpId = appTpls.get(0).getId();
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.APPLICATION_TEMPLATE_NAME_DUPLICATE, BLANKSTRING + String.valueOf(AppTmpId));
        }
        return ActionReturnUtil.returnSuccess();
    }

    private JSONArray uniqueArray(JSONArray array) throws Exception {
        JSONArray newArray = new JSONArray();
        JSONObject object = array.getJSONObject(0);
        newArray.add(object);
        for (int i = 1; i < array.size(); i++) {
            JSONObject tempObject = array.getJSONObject(i);
            boolean isRepeat = false;
            for (int j = 0; j<newArray.size(); j++) {
                if(Objects.nonNull(tempObject) && tempObject.size() > 0 && tempObject.getString("name").equals(newArray.getJSONObject(j).getString("name"))
                        && tempObject.getString("clusterId").equals(newArray.getJSONObject(j).getString("clusterId"))) {
                    isRepeat = true;
                    break;
                }
            }
            if (!isRepeat) {
                newArray.add(tempObject);
            }
        }
        return newArray;
    }
}
