package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.application.BusinessServiceMapper;
import com.harmonycloud.dao.application.BusinessTemplatesMapper;
import com.harmonycloud.dao.application.ServiceTemplatesMapper;
import com.harmonycloud.dao.application.bean.BusinessTemplates;
import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.dao.network.TopologyMapper;
import com.harmonycloud.dao.network.bean.Topology;
import com.harmonycloud.dto.business.BusinessTemplateDto;
import com.harmonycloud.dto.business.CreateContainerDto;
import com.harmonycloud.dto.business.ServiceTemplateDto;
import com.harmonycloud.dto.business.TopologysDto;
import com.harmonycloud.service.application.BusinessService;
import com.harmonycloud.service.application.BusinessServiceService;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.application.TopologyService;
import com.harmonycloud.service.platform.constant.Constant;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    public ActionReturnUtil getBusinessTemplate(String name, String tag) throws Exception {
        // check params
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(tag)) {
            return ActionReturnUtil.returnErrorWithMsg("name or tag is null");
        }
        // select a application Template
        BusinessTemplates businessTemplates = businessTemplatesMapper.getBusinessTemplatesByNameAndTag(name, tag);
        JSONObject js = new JSONObject();
        if (businessTemplates != null) {
            js.put("name", businessTemplates.getName());
            js.put("desc", businessTemplates.getDetails());
            js.put("tenant", businessTemplates.getTenant());
            JSONArray array = new JSONArray();
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
                List<BusinessTemplates> list = businessTemplatesMapper.listBusinessTemplatesByName(businessTemplatesList.get(i).getName(), tenant);
                array.add(getBusinessTemplates(list));
            }
        } else {
            if (searchKey.equals("name")) {
                // search by name
                List<BusinessTemplates> businessTemplatesList = businessTemplatesMapper.listNameByName("%" + searchValue + "%", tenant);
                for (int i = 0; i < businessTemplatesList.size(); i++) {
                    List<BusinessTemplates> list = businessTemplatesMapper.listBusinessTemplatesByName(businessTemplatesList.get(i).getName(), tenant);
                    array.add(getBusinessTemplates(list));
                }
            } else if (searchKey.equals("image")) {
                // search by image
                List<BusinessTemplates> businessTemplatesList = businessTemplatesMapper.listNameByImage(searchValue, tenant);
                for (int i = 0; i < businessTemplatesList.size(); i++) {
                    List<BusinessTemplates> list = businessTemplatesMapper.listBusinessTemplatesByNameAndImage(businessTemplatesList.get(i).getName(), searchValue, tenant);
                    array.add(getBusinessTemplates(list));
                }
            } else {
                return ActionReturnUtil.returnErrorWithMsg("searchkey error");
            }
        }
        return ActionReturnUtil.returnSuccessWithData(array);
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
        // check name
        BusinessTemplates businessTemplates = businessTemplatesMapper.getBusinessTemplatesByName(businessTemplate.getName());
        if (businessTemplates != null && !businessTemplates.getTenant().equals(businessTemplate.getTenant())) {
            return ActionReturnUtil.returnErrorWithMsg(businessTemplate.getName() + " is existed");
        }
        // add application templates
        businessTemplates = new BusinessTemplates();
        businessTemplates.setName(businessTemplate.getName());
        businessTemplates.setUser(userName);
        businessTemplates.setCreateTime(new Date());
        businessTemplates.setTenant(businessTemplate.getTenant());
        businessTemplates.setDetails(businessTemplate.getDesc());
        businessTemplates.setStatus(Constant.TEMPLATE_STATUS_CREATE);
        double bttag = Constant.TEMPLATE_TAG;
        // application templates version control
        List<BusinessTemplates> btmaxtag = businessTemplatesMapper.listBusinessTempaltesMaxTagByName(businessTemplate.getName());
        if (btmaxtag != null && btmaxtag.size() > 0) {
            bttag = Double.valueOf(btmaxtag.get(0).getTag()) + Constant.TEMPLATE_TAG_INCREMENT;

        }
        businessTemplates.setTag(decimalFormat.format(bttag));
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
            listR = saveServiceTemplates(serviceTemplate, businessTemplatesId, businessTemplate.getTenant(), userName, list);
            //获取每个内部服务镜像
            if (serviceTemplate.getExternal() == null || serviceTemplate.getExternal() == 0) {
                list = (List<String>) listR.get(1);
            }
            ;
            maps.putAll((Map<String, Object>)listR.get(0));
        }
        if (list != null && list.size()>0){
            businessTemplatesMapper.updateImageById(splitString(list), businessTemplatesId);
        }
        // application template imagelist


        // add topology
        if (businessTemplate.getTopologyList() != null) {
            if (!saveTopology(businessTemplate.getTopologyList(), businessTemplatesId)) {
                return ActionReturnUtil.returnErrorWithMsg("add topology error");
            }
        }
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
    	com.harmonycloud.dao.application.bean.BusinessService businessService = new com.harmonycloud.dao.application.bean.BusinessService();
        businessService.setBusinessId(businessTemplatesId);
        businessService.setServiceId(serviceTemplateId);
        businessService.setStatus(status);
        businessService.setIsExternal(external);
        businessServiceMapper.insert(businessService);
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
    private List<Object> saveServiceTemplates(ServiceTemplateDto serviceTemplate, Integer businessTemplatesId, String tenant, String userName, List<String> imageList)
            throws Exception {
        List<Object> result = new ArrayList<Object>();
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
                JSONObject json = new JSONObject();
                json.put(externalservice.getName(), externalservice.getId());
                result.add(json);
            }
            // save businessTemplate-serviceTemplate mapper
            saveBusinessService(businessTemplatesId, externalservice.getId(), Constant.TEMPLATE_STATUS_CREATE, Constant.EXTERNAL_SERVICE);
        } else {
        	//添加版本 
            ActionReturnUtil res = serviceService.saveServiceTemplate(serviceTemplate, userName);
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
    private boolean saveTopology(List<TopologysDto> topologys, Integer businessTemplatesId) throws Exception {
        boolean boo = false;
        if (topologys != null && topologys.size() > 0 && (businessTemplatesId != null && businessTemplatesId != 0)) {
            for (TopologysDto ts : topologys) {
                Topology topology = new Topology();
                topology.setBusinessId(businessTemplatesId);
                topology.setDetails(ts.getDesc());
                if (!StringUtils.isEmpty(ts.getSource().getIsExternal()) && ts.getSource().getIsExternal() == Constant.EXTERNAL_SERVICE) {
                    // external service
                    ServiceTemplates externalservice = serviceTemplatesMapper.getExternalService(ts.getSource().getName());
                    if (externalservice != null) {
                        topology.setSource(externalservice.getId() + "");
                    }
                } else {
                    // service
//                    if (!StringUtils.isEmpty(ts.getSource().getId())) {
//                        // service templates existed
//                        topology.setSource(ts.getSource().getId());
//                    } else {
                        List<ServiceTemplates> maxtag = serviceTemplatesMapper.listServiceMaxTagByName(ts.getSource().getName());
                        if (maxtag.size() > 0) {
                            topology.setSource(maxtag.get(0).getId() + "");
                        }
//                    }
                }
                if (!StringUtils.isEmpty(ts.getTarget().getIsExternal()) && ts.getTarget().getIsExternal() == Constant.EXTERNAL_SERVICE) {
                    // external service
                    ServiceTemplates externalservice = serviceTemplatesMapper.getExternalService(ts.getTarget().getName());
                    if (externalservice != null) {
                        topology.setTarget(externalservice.getId() + "");
                    }
                } else {
                    // service
//                    if (!StringUtils.isEmpty(ts.getTarget().getId())) {
//                        // service templates existed
//                        topology.setTarget(ts.getTarget().getId());
//                    } else {
                        List<ServiceTemplates> maxtag = serviceTemplatesMapper.listServiceMaxTagByName(ts.getTarget().getName());
                        if (maxtag.size() > 0) {
                            topology.setTarget(maxtag.get(0).getId() + "");
                        }
//                    }
                }
                topologyMapper.insert(topology);
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
            JSONArray tagArray = new JSONArray();
            for (int i = 0; i < businessTemplatesList.size(); i++) {
            	JSONObject idAndTag=new JSONObject();
            	idAndTag.put("id", businessTemplatesList.get(i).getId());
            	idAndTag.put("tag", businessTemplatesList.get(i).getTag());
            	tagArray.add(idAndTag);
                json.put("createtime", dateToString(businessTemplatesList.get(i).getCreateTime()));
            }
            json.put("tags", tagArray);
        }
        return json;
    }

    private static String dateToString(Date time){
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd");
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

        for (ServiceTemplateDto serviceTemplate : businessTemplate.getServiceList()) {
            listR = updateServiceTemplates(serviceTemplate, businessTemplatesId, businessTemplate.getTenant(), userName, list);
            //内部服务镜像
            if(serviceTemplate.getExternal() == null && Constant.K8S_SERVICE.equals(serviceTemplate.getExternal())){
            	list = (List<String>) listR.get(1);
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
        //删除已有的业务模板和应用模板的Mapper联系
        businessServiceService.deletebusiness(businessTemplatesId);
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
                JSONObject json = new JSONObject();
                json.put(externalservice.getName(), externalservice.getId());
                result.add(json);
            }
            // save businessTemplate-serviceTemplate mapper
            saveBusinessService(businessTemplatesId, externalservice.getId(), Constant.TEMPLATE_STATUS_CREATE, Constant.EXTERNAL_SERVICE);
        } else {
            // check flag  
            if (serviceTemplate.getFlag() == null || serviceTemplate.getFlag() == SERVICE_SAVE) {
            	//添加版本 
                ActionReturnUtil res = serviceService.saveServiceTemplate(serviceTemplate, userName);
                if(res.isSuccess()){
                	JSONObject json = new JSONObject();
                	json = (JSONObject) res.get("data");
                	result.add(json);
                	// add application - service template
                    saveBusinessService(businessTemplatesId, Integer.parseInt(json.get(serviceTemplate.getName()).toString()), Constant.TEMPLATE_STATUS_CREATE, Constant.K8S_SERVICE);
                }  
            } else if (serviceTemplate.getFlag() == SERVICE_UPDATE) {
                // service template existed update
            	serviceService.updateServiceTemplata(serviceTemplate, userName, serviceTemplate.getTag());
                // save businessTemplates-serviceTemplates mapper
                saveBusinessService(businessTemplatesId, serviceTemplate.getId(), Constant.TEMPLATE_STATUS_CREATE, Constant.K8S_SERVICE);
                JSONObject json = new JSONObject();
                json.put(serviceTemplate.getName(), serviceTemplate.getId());
                result.add(json);
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
                Topology topology = new Topology();
                topology.setBusinessId(businessTemplatesId);
                topology.setDetails(ts.getDesc());
                topology.setSource(map.get(ts.getSource().getName()).toString());
                topology.setTarget(map.get(ts.getTarget().getName()).toString() + "");
                topologyMapper.insert(topology);
            }
            boo = true;
        }
        return boo;
    }
}
