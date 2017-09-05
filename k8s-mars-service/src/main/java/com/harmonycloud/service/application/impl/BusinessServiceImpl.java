package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.application.BusinessServiceMapper;
import com.harmonycloud.dao.application.BusinessTemplatesMapper;
import com.harmonycloud.dao.application.ServiceTemplatesMapper;
import com.harmonycloud.dao.application.bean.BusinessTemplates;
import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.dao.network.TopologyMapper;
import com.harmonycloud.dao.network.bean.Topology;
import com.harmonycloud.dto.business.*;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.service.ReplicasetsService;
import com.harmonycloud.service.application.BusinessService;
import com.harmonycloud.service.application.BusinessServiceService;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.application.TopologyService;
import com.harmonycloud.service.application.Util.TemplateToYamlUtil;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.TenantService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by root on 3/29/17.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BusinessServiceImpl implements BusinessService {

    public static final Integer SERVICE_UPDATE = 1;

    public static final Integer SERVICE_SAVE = 0;

    @Autowired
    private BusinessTemplatesMapper businessTemplatesMapper;

    @Autowired
    private BusinessServiceMapper businessServiceMapper;

    private DecimalFormat decimalFormat = new DecimalFormat("######0.0");

    @Autowired
    private ServiceTemplatesMapper serviceTemplatesMapper;

    @Autowired
    private BusinessServiceService businessServiceService;
    
    @Autowired
    private ServiceService serviceService;

    @Autowired
    private TopologyService topologyService;

    @Autowired
    private TopologyMapper topologyMapper;
    @Autowired
	ReplicasetsService rsService;
    @Autowired
    TenantService tenantService;

    /**
     * remove businessTemplate on 17/04/07.
     * 
     * @param name
     * 
     * @return ActionReturnUtil*/
    @Override
    public ActionReturnUtil deleteBusinessTemplate(String name) throws Exception {
        // check params
        if (StringUtils.isEmpty(name)) {
            return ActionReturnUtil.returnErrorWithMsg("name or tenant is null");
        }
        // delete business_template
        businessTemplatesMapper.deleteBusinessTemplate(name);
        // delete business_service
        businessServiceMapper.deleteBusinessService(name);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * get BusinessTemplate on 17/05/04.
     * @param name
     * 
     * @param tag
     * 
     * @return {@link ActionReturnUtil}*/
    @Override
    public ActionReturnUtil getBusinessTemplate(String name, String tag, String tenant) throws Exception {
        // check params
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(tag)) {
            return ActionReturnUtil.returnErrorWithMsg("name or tag is null");
        }
        // select a application Template
        BusinessTemplates businessTemplates = businessTemplatesMapper.getBusinessTemplatesByNameAndTag(name, tag,tenant);
        JSONObject js = new JSONObject();
        if (businessTemplates != null) {
            js.put("name", businessTemplates.getName());
            js.put("desc", (businessTemplates.getDetails() != null) ? businessTemplates.getDetails() : "");
            js.put("id", businessTemplates.getId());
            js.put("image", businessTemplates.getImageList());
            js.put("tag", businessTemplates.getTag());
            js.put("tenant", businessTemplates.getTenant());
            js.put("user", businessTemplates.getUser());
            js.put("createTime", dateToString(businessTemplates.getCreateTime()));
            JSONArray array = new JSONArray();
            List<Object> deploymentListToyaml = new ArrayList<>();
            // select service Template
            List<com.harmonycloud.dao.application.bean.BusinessService> businessServiceList = businessServiceMapper.listBusinessServiceByBusinessTemplatesId(businessTemplates.getId());
            if (businessServiceList != null) {
                for (com.harmonycloud.dao.application.bean.BusinessService businessService : businessServiceList) {
                    ServiceTemplates serviceTemplates = serviceTemplatesMapper.getServiceTemplatesByID(businessService.getServiceId());
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
                        if (serviceTemplates.getDeploymentContent() != null){
                            String dep=json.getJSONArray("deployment").getJSONObject(0).toString().replaceAll(":\"\",", ":"+null+",").replaceAll(":\"\"", ":"+null+"");
                            DeploymentDetailDto deployment = JsonUtil.jsonToPojo(dep, DeploymentDetailDto.class);

                            Deployment deploymentToYaml =  TemplateToYamlUtil.templateToDeployment(deployment);
                            com.harmonycloud.k8s.bean.Service serviceYaml = TemplateToYamlUtil.templateToService(deployment);

                            deploymentListToyaml.add(serviceYaml);

                            deploymentListToyaml.add(deploymentToYaml);
                        }
                    }
                }
            }
            js.put("servicelist", array);
            // select topology
            List<Topology> topologyList = topologyMapper.listToplogyByBusinessTemplatesId(businessTemplates.getId());
            array = new JSONArray();
            if (topologyList != null) {
                for (Topology topology : topologyList) {
                    JSONObject topologyJson = new JSONObject();
                    topologyJson.put("id", topology.getId());
                    topologyJson.put("source", topology.getSource());
                    topologyJson.put("target", topology.getTarget());
                    array.add(topologyJson);
                }
            }
            js.put("topology", array);
            Yaml yaml = new Yaml();
            if (deploymentListToyaml != null){

                String yamlc = convertYaml(yaml.dumpAsMap(deploymentListToyaml));

                js.put("yaml",yamlc);
            }

//        } else {
//            ActionReturnUtil.returnErrorWithMsg("buiness template is null");
        }
        return ActionReturnUtil.returnSuccessWithData(js);
    }

    /**
     * get application template by name or iamge and tenant service on 17/04/07.
     * 
     * @author gurongyun
     * 
     * @param searchkey
     * 
     * @param searchvalue
     * 
     * @param tenant
     * 
     * @return ActionReturnUtil
     */
    @Override
    public ActionReturnUtil listBusinessTemplateByTenant(String searchKey, String searchValue, String tenant) throws Exception {
        JSONArray array = new JSONArray();
        if (StringUtils.isEmpty(searchValue)) {
            // search value is null
            List<BusinessTemplates> businessTemplatesList = businessTemplatesMapper.listNameByTenant(tenant);
            for (int i = 0; i < businessTemplatesList.size(); i++) {
                List<BusinessTemplates> list = businessTemplatesMapper.listBusinessTemplatesByName(businessTemplatesList.get(i).getName(), businessTemplatesList.get(i).getTenant());
                array.add(getBusinessTemplates(list));
            }
        } else {
            if (searchKey.equals("name")) {
                // search by name
                List<BusinessTemplates> businessTemplatesList = businessTemplatesMapper.listNameByName("%" + searchValue + "%", tenant);
                for (int i = 0; i < businessTemplatesList.size(); i++) {
                    List<BusinessTemplates> list = businessTemplatesMapper.listBusinessTemplatesByName(businessTemplatesList.get(i).getName(), businessTemplatesList.get(i).getTenant());
                    array.add(getBusinessTemplates(list));
                }
            } else if (searchKey.equals("image")) {
                // search by image
                List<BusinessTemplates> businessTemplatesList = businessTemplatesMapper.listNameByImage(searchValue, tenant);
                for (int i = 0; i < businessTemplatesList.size(); i++) {
                    List<BusinessTemplates> list = businessTemplatesMapper.listBusinessTemplatesByNameAndImage(businessTemplatesList.get(i).getName(), searchValue, businessTemplatesList.get(i).getTenant());
                    array.add(getBusinessTemplates(list));
                }
            } else {
                return ActionReturnUtil.returnErrorWithMsg("searchkey error");
            }
        }
        return ActionReturnUtil.returnSuccessWithData(array);
    }

    @Override
    public ActionReturnUtil getBusinessTemplateYaml(BusinessTemplateDto businessTemplate) throws Exception {
        List<Object> deploymentListToyaml = new ArrayList<>();
        if (businessTemplate != null && businessTemplate.getServiceList() != null){
            for(ServiceTemplateDto svcOne : businessTemplate.getServiceList()){
                if (svcOne.getDeploymentDetail() != null){
                    Deployment deploymentToYaml =  TemplateToYamlUtil.templateToDeployment(svcOne.getDeploymentDetail());
                    com.harmonycloud.k8s.bean.Service serviceYaml = TemplateToYamlUtil.templateToService(svcOne.getDeploymentDetail());
                    deploymentListToyaml.add(serviceYaml);
                    deploymentListToyaml.add(deploymentToYaml);
                }
            }
        }
        Yaml yaml = new Yaml();
        String yamlc = "";
        if (yaml.dumpAsMap(deploymentListToyaml) != null){
            yamlc = convertYaml(yaml.dumpAsMap(deploymentListToyaml));
        }
        return ActionReturnUtil.returnSuccessWithData(yamlc);
    }

    /**
     * create a businessTemplate service implement on 17/04/07.
     * 
     * @author gurongyun
     * @param businessTemplate
     *            required
     * @param userName
     *            required
     * @return ActionReturnUtil
     */
    @SuppressWarnings("unchecked")
	@Override
    public synchronized ActionReturnUtil saveBusinessTemplate(BusinessTemplateDto businessTemplate, String userName) throws Exception {
        // check value
        if (StringUtils.isEmpty(userName) || businessTemplate == null) {
            return ActionReturnUtil.returnErrorWithMsg("username or application temolate is null");
        }
        BusinessTemplates businessTemplates = new BusinessTemplates();
        if(businessTemplate.getIsDeploy() == 1){
        	businessTemplates.setStatus(Constant.TEMPLATE_STATUS_DELETE);
        }else{
        	businessTemplates = businessTemplatesMapper.getBusinessTemplatesByName(businessTemplate.getName());
            if (businessTemplates != null && !businessTemplates.getTenant().equals(businessTemplate.getTenant())) {
                return ActionReturnUtil.returnErrorWithMsg(businessTemplate.getName() + " is existed");
            }else{
            	businessTemplates = new BusinessTemplates();
            }
            double bttag = Constant.TEMPLATE_TAG;
            // application templates version control
            List<BusinessTemplates> btmaxtag = businessTemplatesMapper.listBusinessTempaltesMaxTagByName(businessTemplate.getName());
            if (btmaxtag != null && btmaxtag.size() > 0) {
                bttag = Double.valueOf(btmaxtag.get(0).getTag()) + Constant.TEMPLATE_TAG_INCREMENT;

            }
            businessTemplates.setTag(decimalFormat.format(bttag));
            businessTemplates.setStatus(Constant.TEMPLATE_STATUS_CREATE);
        }
        // check name
         
        // add application templates
        businessTemplates.setName(businessTemplate.getName());
        businessTemplates.setUser(userName);
        businessTemplates.setCreateTime(new Date());
        businessTemplates.setTenant(businessTemplate.getTenant());
        businessTemplates.setDetails(businessTemplate.getDesc());
        businessTemplatesMapper.saveBusinessTemplates(businessTemplates);
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> maps = new HashMap<String, Object>();
        List<Map<String, Object>> idList = new ArrayList<Map<String, Object>>();
        int businessTemplatesId = businessTemplates.getId();
        map.put(businessTemplates.getName(),businessTemplatesId+"");
        // check service templates
        if (businessTemplate.getServiceList() != null && businessTemplate.getServiceList().size() <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("service template is null");
        }
        List<String> list = new LinkedList<String>();
        List<Object> listR = new LinkedList<Object>();
        // addSave service templates

        for (ServiceTemplateDto serviceTemplate : businessTemplate.getServiceList()) {
            listR = saveServiceTemplates(serviceTemplate, businessTemplatesId, businessTemplate.getTenant(), userName, list, Constant.TEMPLATE_STATUS_DELETE);
            //获取每个内部服务镜像
            if (serviceTemplate.getExternal() == null || serviceTemplate.getExternal() == 0) {
            	if(listR.get(1) != null){
            		list = (List<String>) listR.get(1);
            	}
            }
            maps.putAll((Map<String, Object>)listR.get(0));
        }
        if (list != null && list.size()>0){
            businessTemplatesMapper.updateImageById(splitString(list), businessTemplatesId);
        }
        // application template imagelist
        // add topology
        //添加拓扑图
  		if(businessTemplate.getTopologyList() != null && businessTemplate.getTopologyList().size() > 0 ){
  			for(TopologysDto topology : businessTemplate.getTopologyList()){
  				if(topology.getSource() != null && topology.getSource().getName() != null && topology.getTarget()!= null && topology.getTarget().getName() != null ){
  					int source = (int) maps.get(topology.getSource().getName());
  					int target = (int) maps.get(topology.getTarget().getName());
  					topology.getSource().setId(source+"");
  					topology.getTarget().setId(target+"");
  				}
  			}
  			saveTopology(businessTemplate.getTopologyList(), businessTemplatesId);
  		}
       /* if (businessTemplate.getTopologyList() != null) {
            if (!saveTopology(businessTemplate.getTopologyList(), businessTemplatesId)) {
                return ActionReturnUtil.returnErrorWithMsg("add topology error");
            }
        }*/
        idList.add(map);
        idList.add(maps);
        return ActionReturnUtil.returnSuccessWithData(idList);
    }

    /**
     * list images on 17/04/07.
     * 
     * @param containers
     * 
     * @param imageList
     * 
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
     * save businessTemplates and serviceTemplates mapper on 17/04/07.
     * 
     * @param businessTemplatesId
     * 
     * @param serviceTemplateId
     * 
     * @param status
     * 
     * @param external
     * 
     */
    private void saveBusinessService(Integer businessTemplatesId, Integer serviceTemplateId, Integer status, Integer external) {
    	List<com.harmonycloud.dao.application.bean.BusinessService> list = businessServiceMapper.selectBusinessServiceByBusinessId(businessTemplatesId, serviceTemplateId);
    	if(list == null || list.size() <= 0){
    		com.harmonycloud.dao.application.bean.BusinessService businessService = new com.harmonycloud.dao.application.bean.BusinessService();
            businessService.setBusinessId(businessTemplatesId);
            businessService.setServiceId(serviceTemplateId);
            businessService.setStatus(status);
            businessService.setIsExternal(external);
            businessServiceMapper.insert(businessService);
    	}
    }

    /**
     * save serviceTemplates on 17/04/07.
     * 
     * @param serviceTemplate
     * 
     * @param businessTemplatesId
     * 
     * @param tenant
     * 
     * @param userName
     * 
     * @param imageList
     * 
     * @return imageList
     */
    private List<Object> saveServiceTemplates(ServiceTemplateDto serviceTemplate, Integer businessTemplatesId, String tenant, String userName, List<String> imageList,int type)
            throws Exception {
        List<Object> result = new ArrayList<Object>();
        if (serviceTemplate.getExternal() != null && serviceTemplate.getExternal() == Constant.EXTERNAL_SERVICE) {
        	ServiceTemplates externalservice = new ServiceTemplates();
        	// external service
            externalservice.setName(serviceTemplate.getName());
            externalservice.setDetails(serviceTemplate.getDesc());
            externalservice.setFlag(Constant.EXTERNAL_SERVICE);
            externalservice.setCreateTime(new Date());
            externalservice.setTenant(tenant);
            externalservice.setStatus(Constant.TEMPLATE_STATUS_DELETE);
            // insert external service
            serviceTemplatesMapper.insert(externalservice);
            JSONObject json = new JSONObject();
            json.put(externalservice.getName(), externalservice.getId());
            result.add(json);
            // save businessTemplate-serviceTemplate mapper
            saveBusinessService(businessTemplatesId, externalservice.getId(), Constant.TEMPLATE_STATUS_CREATE, Constant.EXTERNAL_SERVICE);
        } else {
    		//添加删除模板
            ActionReturnUtil res = serviceService.saveServiceTemplate(serviceTemplate, userName, type);
            if(res.isSuccess()){
            	JSONObject json = new JSONObject();
            	json = (JSONObject) res.get("data");
            	result.add(json);
            	// add application - service template
                saveBusinessService(businessTemplatesId, Integer.parseInt(json.get(serviceTemplate.getName()).toString()), Constant.TEMPLATE_STATUS_CREATE, Constant.K8S_SERVICE);
            }  
            listImages(serviceTemplate.getDeploymentDetail().getContainers(), imageList);
        }
        result.add(imageList);

        return result;
    }

    /**
     * save topology on 17/04/07.
     * 
     * @param topologys
     *            re
     */
    public boolean saveTopology(List<TopologysDto> topologys, Integer businessTemplatesId) throws Exception {
        boolean boo = false;
        if (topologys != null && topologys.size() > 0 && (businessTemplatesId != null && StringUtils.isEmpty(businessTemplatesId))) {
            for (TopologysDto ts : topologys) {
                Topology topology = new Topology();
                topology.setBusinessId(businessTemplatesId);
                topology.setDetails(ts.getDesc());
                if(ts.getSource() != null && ts.getSource().getId() != null && ts.getTarget() != null && ts.getTarget().getId() != null){
                	topology.setSource(ts.getSource().getId() + "");
                    topology.setTarget(ts.getTarget().getId() + "");
                    topologyMapper.insert(topology);
                }
                
            }
            boo = true;
        }
        return boo;
    }

    /**
     * List to String on 17/04/07.
     * 
     * @param strList
     * 
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
     * get businessTemplate overview on 17/05/05.
     * 
     * @param BusinessTemplatesList
     * 
     * @return {@link JSONObject}
     */
    private JSONObject getBusinessTemplates(List<BusinessTemplates> businessTemplatesList) throws Exception {
        JSONObject json = new JSONObject();
        if (businessTemplatesList != null && businessTemplatesList.size() > 0) {
            json.put("name", businessTemplatesList.get(0).getName());
            if("all".equals(businessTemplatesList.get(0).getTenant())){
            	json.put("public",true);
            }else{
            	json.put("public",false);
            }
            JSONArray tagArray = new JSONArray();
            for (int i = 0; i < businessTemplatesList.size(); i++) {
            	JSONObject idAndTag=new JSONObject();
            	idAndTag.put("id", businessTemplatesList.get(i).getId());
            	idAndTag.put("tag", businessTemplatesList.get(i).getTag());
            	idAndTag.put("image", businessTemplatesList.get(i).getImageList());
            	idAndTag.put("user", businessTemplatesList.get(i).getUser());
            	tagArray.add(idAndTag);
                json.put("createtime", dateToString(businessTemplatesList.get(i).getCreateTime()));
            }
            json.put("tags", tagArray);
        }
        return json;
    }

    private static String dateToString(Date time){
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ctime = formatter.format(time);

        return ctime;
    }

	@Override
	public ActionReturnUtil deleteBusinessTemplateByTenant(String[] tenant) throws Exception {
		if(tenant != null && tenant.length > 0){
		    businessTemplatesMapper.deleteByTenant(tenant);
			return ActionReturnUtil.returnSuccess();
		}else{
			return ActionReturnUtil.returnErrorWithMsg("tenant 为空");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ActionReturnUtil updateBusinessTemplate(BusinessTemplateDto businessTemplate, String userName)
			throws Exception {
        // add application templates
        BusinessTemplates businessTemplates = new BusinessTemplates();
        businessTemplates.setName(businessTemplate.getName());
        businessTemplates.setUser(userName);
        businessTemplates.setUpdateTime(new Date());
        businessTemplates.setTenant(businessTemplate.getTenant());
        businessTemplates.setDetails(businessTemplate.getDesc());
        businessTemplates.setStatus(Constant.TEMPLATE_STATUS_CREATE);
        businessTemplates.setId(businessTemplate.getId());
        businessTemplates.setTag(businessTemplate.getTag());
        businessTemplatesMapper.updateBusinessTemplate(businessTemplates);
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> maps = new HashMap<String, Object>();
        List<Map<String, Object>> idList = new ArrayList<Map<String, Object>>();
        int businessTemplatesId = businessTemplates.getId();
        map.put(businessTemplates.getName(),businessTemplatesId+"");
        // check service templates
        if (businessTemplate.getServiceList().size() <= 0) {
            return ActionReturnUtil.returnErrorWithMsg("service template is null");
        }
        List<String> list = new LinkedList<String>();
        List<Object> listR = new LinkedList<Object>();
        // addSave service templates

        //删除应用模板
        List<com.harmonycloud.dao.application.bean.BusinessService>  bsList = businessServiceService.listByBusiness(businessTemplate.getId());
        if(bsList != null && bsList.size() > 0){
        	for(com.harmonycloud.dao.application.bean.BusinessService bs : bsList){
        		if(bs.getServiceId() != null){
        			serviceService.delById(bs.getServiceId());
        		}
        	}
        }
        //删除已有的业务模板和应用模板的Mapper联系
        businessServiceService.deletebusiness(businessTemplatesId);
        for (ServiceTemplateDto serviceTemplate : businessTemplate.getServiceList()) {
            listR = updateServiceTemplates(serviceTemplate, businessTemplatesId, businessTemplate.getTenant(), userName, list);
            //内部服务镜像
            if(serviceTemplate.getExternal() == null && Constant.K8S_SERVICE.equals(serviceTemplate.getExternal())){
            	if(listR.get(1) != null){
            		list = (List<String>) listR.get(1);
            	}
            }
            //存放应用模板的name和id
            maps.putAll((Map<String, Object>)listR.get(0));
        }
        if (list != null && list.size()>0){
        	//更新镜像
            businessTemplatesMapper.updateImageById(splitString(list), businessTemplatesId);
        }
        // add topology
        if (businessTemplate.getTopologyList() != null) {
            if (!updateTopology(businessTemplate.getTopologyList(), businessTemplatesId, maps)) {
                return ActionReturnUtil.returnErrorWithMsg("add topology error");
            }
        }
        idList.add(map);
        idList.add(maps);
        return ActionReturnUtil.returnSuccessWithData(idList);
	}
	/**
     * save serviceTemplates on 17/04/07.
     * 
     * @param serviceTemplate
     * 
     * @param businessTemplatesId
     * 
     * @param tenant
     * 
     * @param userName
     * 
     * @param imageList
     * 
     * @return imageList
     */
	private List<Object> updateServiceTemplates(ServiceTemplateDto serviceTemplate, Integer businessTemplatesId, String tenant, String userName, List<String> imageList)
            throws Exception {
        List<Object> result = new ArrayList<Object>();
        //添加service
        if (serviceTemplate.getExternal() != null && serviceTemplate.getExternal() == Constant.EXTERNAL_SERVICE) {
            // external service
            ServiceTemplates externalservice = serviceTemplatesMapper.getExternalService(serviceTemplate.getName());
            if (externalservice == null) {
                // new external service
                externalservice = new ServiceTemplates();
                externalservice.setName(serviceTemplate.getName());
                externalservice.setDetails(serviceTemplate.getDesc());
                externalservice.setFlag(Constant.EXTERNAL_SERVICE);
                externalservice.setCreateTime(new Date());
                externalservice.setTenant(tenant);
                externalservice.setStatus(Constant.TEMPLATE_STATUS_CREATE);
                // insert external service
                serviceTemplatesMapper.insert(externalservice);
            }
            JSONObject json = new JSONObject();
            json.put(externalservice.getName(), externalservice.getId());
            result.add(json);
            // save businessTemplate-serviceTemplate mapper
            saveBusinessService(businessTemplatesId, externalservice.getId(), Constant.TEMPLATE_STATUS_CREATE, Constant.EXTERNAL_SERVICE);
        } else {
           
        	//添加版本 
            ActionReturnUtil res = serviceService.saveServiceTemplate(serviceTemplate, userName,1);
            if(res.isSuccess()){
            	JSONObject json = new JSONObject();
            	json = (JSONObject) res.get("data");
            	result.add(json);
            	// add application - service template
                saveBusinessService(businessTemplatesId, Integer.parseInt(json.get(serviceTemplate.getName()).toString()), Constant.TEMPLATE_STATUS_CREATE, Constant.K8S_SERVICE);
            }  
            listImages(serviceTemplate.getDeploymentDetail().getContainers(), imageList);
        }
        result.add(imageList);

        return result;
    }

    /**
     * save topology on 17/04/07.
     * 
     * @param topologys
     *            re
     */
    private boolean updateTopology(List<TopologysDto> topologys, Integer businessTemplatesId, Map<String, Object> map) throws Exception {
        boolean boo = false;
        //删除旧的拓扑关系
        topologyService.deleteToplogy(businessTemplatesId);
        if (topologys != null && topologys.size() > 0 && (businessTemplatesId != null && businessTemplatesId != 0)) {
            for (TopologysDto ts : topologys) {
            	if(ts != null && ts.getSource() != null && ts.getSource().getName() != null && ts.getTarget() != null && ts.getTarget().getName() != null){
            		Topology topology = new Topology();
                    topology.setBusinessId(businessTemplatesId);
                    topology.setDetails(ts.getDesc());
                    topology.setSource(map.get(ts.getSource().getName()).toString());
                    topology.setTarget(map.get(ts.getTarget().getName()).toString() + "");
                    topologyMapper.insert(topology);
            	}
            }
            boo = true;
        }
        return boo;
    }

	@Override
	public ActionReturnUtil getBusinessTemplateByName(String name, String tenant) throws Exception {
		if(StringUtils.isEmpty(name)){
			return ActionReturnUtil.returnErrorWithMsg("模板名称为空");
		}
		if(StringUtils.isEmpty(tenant)){
			return ActionReturnUtil.returnErrorWithMsg("租户为空");
		}
		List<BusinessTemplates> list = businessTemplatesMapper.listBusinessTemplatesByName(name, tenant);
		JSONObject json = new JSONObject();
		if(list != null && list.size() > 0){
			JSONArray array = new JSONArray();
			json.put("name", name);
			for(BusinessTemplates bt : list){
				JSONObject js = new JSONObject();
				js.put("tag", bt.getTag());
				js.put("id", bt.getId());
				array.add(js);
			}
			json.put("tags", array);
			return ActionReturnUtil.returnSuccessWithData(json);
		}else{
			return ActionReturnUtil.returnErrorWithMsg("不存在改模板");
		}
		
	}

	@Override
	public ActionReturnUtil addServiceTemplateByName(BusinessTemplateDto businessTemplate, String userName)
			throws Exception {
		if(businessTemplate == null || businessTemplate.getServiceList() == null){
			return ActionReturnUtil.returnErrorWithMsg("业务模板为空");
		}
		//添加更新应用模板
		List<ServiceTemplateDto> list = businessTemplate.getServiceList();
		JSONObject json = new JSONObject();
		
		if(list !=null && list.size() > 0){
			for( ServiceTemplateDto s : list){
				ActionReturnUtil serRes = serviceService.saveServiceTemplate(s, userName,1);
				if(!serRes.isSuccess()){
					return serRes;
				}
				JSONObject js = (JSONObject) serRes.get("data");
				s.setId(js.getInt(s.getName()));
				json.putAll(js);
				//添加业务模板和应用模板Mapper
				saveBusinessService(businessTemplate.getId(), s.getId(), Constant.TEMPLATE_STATUS_CREATE, Constant.K8S_SERVICE);
			}
		}else{
			return ActionReturnUtil.returnErrorWithMsg("应用模板为空");
		}
		//添加拓扑图
		if(businessTemplate.getTopologyList() != null && businessTemplate.getTopologyList().size() > 0 ){
			for(TopologysDto topology : businessTemplate.getTopologyList()){
				if(topology.getSource() != null && topology.getSource().getName() != null && topology.getTarget()!= null && topology.getTarget().getName() != null ){
					int source = json.getInt(topology.getSource().getName());
					int target = json.getInt(topology.getTarget().getName());
					topology.getSource().setId(source+"");
					topology.getTarget().setId(target+"");
				}
			}
			saveTopology(businessTemplate.getTopologyList(), businessTemplate.getBusinessId());
		}
		return ActionReturnUtil.returnSuccessWithData(json);
	}

	@Override
	public ActionReturnUtil updateServiceTemplateByName(BusinessTemplateDto businessTemplate, String userName)
			throws Exception {
		if(businessTemplate == null || businessTemplate.getServiceList() == null){
			return ActionReturnUtil.returnErrorWithMsg("业务模板为空");
		}
		//更新应用模板
		List<ServiceTemplateDto> list = businessTemplate.getServiceList();
		if(list !=null && list.size() > 0){
			for( ServiceTemplateDto s : list){
				if(s.getFlag() !=  null && s.getFlag() == 1){
					serviceService.updateServiceTemplata(s, userName, "");
				}
				//添加业务模板和应用模板Mapper
				saveBusinessService(businessTemplate.getId(), s.getId(), Constant.TEMPLATE_STATUS_CREATE, Constant.K8S_SERVICE);
			}
		}else{
			return ActionReturnUtil.returnErrorWithMsg("应用模板为空");
		}
		return ActionReturnUtil.returnSuccess();
	}

	public String convertYaml(String yaml){

        ByteArrayInputStream is=new ByteArrayInputStream(yaml.getBytes());
        BufferedReader br=new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String line = "";
        try {
            while((line = br.readLine())!=null){
                if (line != null && !line.contains("!!") && !line.contains("null")){
                    if (line.length() > 2){
                        String lineNew = line.substring(2, line.length());
                        sb.append(lineNew + "\n");
                    } else {
                        sb.append(line + "\n");
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally { 
        	try {
				br.close();
				is.close(); 
			} catch (IOException e) {
			} 
        	
        }
        return sb.toString();
    }

	@Override
	public ActionReturnUtil listServiceTemplatePublic() throws Exception {
		JSONArray array = new JSONArray();
		List<BusinessTemplates> businessTemplatesList = businessTemplatesMapper.listPublic();
		for (int i = 0; i < businessTemplatesList.size(); i++) {
            List<BusinessTemplates> list = businessTemplatesMapper.listBusinessTemplatesByName(businessTemplatesList.get(i).getName(), businessTemplatesList.get(i).getTenant());
            array.add(getBusinessTemplates(list));
        }
		return ActionReturnUtil.returnSuccessWithData(array);
	}
}
