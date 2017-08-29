package com.harmonycloud.service.tenant.impl;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HarborUtil;
import com.harmonycloud.common.util.HttpClientResponse;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.TenantUtils;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.network.NetworkCalicoMapper;
import com.harmonycloud.dao.network.bean.NamespceBindSubnet;
import com.harmonycloud.dao.network.bean.NetworkCalico;
import com.harmonycloud.dao.network.bean.NetworkTopology;
import com.harmonycloud.dao.tenant.HarborProjectTenantMapper;
import com.harmonycloud.dao.tenant.TenantBindingMapper;
import com.harmonycloud.dao.tenant.bean.HarborProjectTenant;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantBindingExample;
import com.harmonycloud.dao.tenant.bean.UserTenant;
import com.harmonycloud.dao.tenant.bean.UserTenantExample;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.customs.CustomUserMapper;
import com.harmonycloud.dto.tenant.CreateNetwork;
import com.harmonycloud.dto.tenant.HarborProjectDto;
import com.harmonycloud.dto.tenant.NamespaceUserDto;
import com.harmonycloud.dto.tenant.NetworkTenant;
import com.harmonycloud.dto.tenant.TenantDto;
import com.harmonycloud.dto.tenant.show.UserShowDto;
import com.harmonycloud.k8s.bean.Namespace;
import com.harmonycloud.k8s.bean.NamespaceList;
import com.harmonycloud.k8s.bean.PersistentVolume;
import com.harmonycloud.k8s.bean.PersistentVolumeList;
import com.harmonycloud.k8s.bean.ResourceQuota;
import com.harmonycloud.k8s.bean.ResourceQuotaList;
import com.harmonycloud.k8s.bean.ResourceQuotaSpec;
import com.harmonycloud.k8s.bean.ResourceQuotaStatus;
import com.harmonycloud.k8s.bean.RoleBinding;
import com.harmonycloud.k8s.bean.RoleBindingList;
import com.harmonycloud.k8s.bean.Subjects;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.service.NetworkPolicyService;
import com.harmonycloud.k8s.service.PersistentvolumeService;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.service.RoleBindingService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.ApplicationService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.service.ConfigCenterService;
import com.harmonycloud.service.platform.service.ExternalService;
import com.harmonycloud.service.tenant.HarborProjectTenantService;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.NetworkService;
import com.harmonycloud.service.tenant.PersistentVolumeService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.tenant.UserTenantService;
import com.harmonycloud.service.user.UserService;

import net.sf.json.JSONArray;

/**
 * Created by andy on 17-1-9.
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class TenantServiceImpl implements TenantService {

    // @Autowired
    // UserTenantMapper userTenantMapper ;
    @Autowired
    UserTenantService userTenantService;
    @Autowired
    TenantBindingMapper tenantBindingMapper;
    @Autowired
    CustomUserMapper userMapper;
    @Autowired
    NamespaceService namespaceService;
    @Autowired
    com.harmonycloud.k8s.service.NamespaceService namespaceService1;
    @Autowired
    PersistentvolumeService persistentvolumeService;
    @Autowired
    RoleBindingService roleBindingService;
    @Autowired
    NetworkCalicoMapper networkCalicoMapper;
    @Autowired
    HarborUtil harborUtil;
    @Autowired
    NetworkService networkService;
    @Autowired
    PersistentVolumeService persistentVolumeService;
    @Autowired
    NetworkPolicyService networkPolicyService;
    @Autowired
    ClusterService clusterService;
    @Autowired
    ExternalService externalService;
    @Autowired
    ApplicationService applicationService;
    @Autowired
    ConfigCenterService configCenterService;
    @Autowired
    UserService userService;
    @Autowired
    private HttpSession session;
    @Autowired 
    HarborProjectTenantService harborProjectTenantService;
    @Autowired
    HarborProjectTenantMapper harborProjectTenantMapper;
    

    public static final String TENANTNAME = "tenantName";
    public static final String TENANTID = "tenantId";
    @Value("#{propertiesReader['image.url']}")
    private String harborUrl;
    @Value("#{propertiesReader['network.networkFlag']}")
    private String networkFlag;

    private static final Logger logger = LoggerFactory.getLogger(TenantServiceImpl.class);

    public ActionReturnUtil getTenantlistWithoutUsername(Integer clusterId) throws Exception {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        List<TenantBinding> listTenantBinding;
        TenantBindingExample exampleuser = new TenantBindingExample();
        if (clusterId != null) {
            exampleuser.createCriteria().andClusterIdEqualTo(clusterId);
        }
        listTenantBinding = tenantBindingMapper.selectByExample(exampleuser);
        if (listTenantBinding != null && listTenantBinding.isEmpty()) {
            return ActionReturnUtil.returnSuccessWithData(data);
        }

        List<HarborProjectTenant> byTenantIdPublic = harborProjectTenantMapper.getByTenantIdPublic(1);
        for (TenantBinding tenantBinding : listTenantBinding) {
            List<UserTenant> list = new ArrayList<UserTenant>();
            Map<String, Object> map = new HashMap<String, Object>();
            Date createTime = tenantBinding.getCreateTime();
            SimpleDateFormat adf = new SimpleDateFormat(CommonConstant.UTCTIME);
            String date = adf.format(createTime);
            // 查询harbor信息
            List<HarborProjectTenant> harborProjectList = harborProjectTenantService.getSimplProjectList(tenantBinding.getTenantId());
            list = userTenantService.getUserByTenantid(tenantBinding.getTenantId());
            map.put(CommonConstant.NAME, tenantBinding.getTenantName());
            map.put(CommonConstant.TIME, date);
            map.put(CommonConstant.TMUSER, tenantBinding.getTmUsernameList());
            map.put(CommonConstant.ANNOTATION, tenantBinding.getAnnotation());
            map.put(CommonConstant.TENANTID, tenantBinding.getTenantId());
            map.put(CommonConstant.NAMESPACES, tenantBinding.getK8sNamespaceList());
            map.put(CommonConstant.HARBORPROJECTS, harborProjectList);
            map.put(CommonConstant.K8SPVS, tenantBinding.getK8sPvList());
            map.put(CommonConstant.NAMESPACENUM, tenantBinding.getK8sNamespaceList().size());
            map.put(CommonConstant.HARBORPUBLICPERJECTNUM, byTenantIdPublic.size());
            map.put(CommonConstant.TENANTUSERNUM, list.size());
            data.add(map);
        }
        return ActionReturnUtil.returnSuccessWithData(data);

    }
    @Override
    public ActionReturnUtil tenantList(String username, Integer clusterId) throws Exception {
        if (StringUtils.isEmpty(username) || CommonConstant.UNDEFINED.equals(username)) {
            return this.getTenantlistWithoutUsername(clusterId);
        }
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        List<UserTenant> list = new ArrayList<UserTenant>();
        list = userTenantService.getTenantCount(username);
        for (UserTenant usertenant : list) {
            TenantBindingExample exampleuser = new TenantBindingExample();
            exampleuser.createCriteria().andTenantIdEqualTo(usertenant.getTenantid());
            List<TenantBinding> listTenantBinding = tenantBindingMapper.selectByExample(exampleuser);
            Map<String, Object> map = new HashMap<String, Object>();
            if (listTenantBinding.size() == 1 && listTenantBinding.get(0).getClusterId() == clusterId) {
                Date createTime = listTenantBinding.get(0).getCreateTime();
                SimpleDateFormat adf = new SimpleDateFormat(CommonConstant.UTCTIME);
                String date = adf.format(createTime);
                List<HarborProjectDto> harborProjectList = new ArrayList<HarborProjectDto>();
                // 查询harbor信息
                ActionReturnUtil projectList = harborProjectTenantService.getProjectList(usertenant.getTenantid(),2,true);
                if ((Boolean) projectList.get(CommonConstant.SUCCESS) == true) {
                    harborProjectList= (List<HarborProjectDto>)projectList.get(CommonConstant.DATA);
                }
                List<UserTenant> userByTenantid = userTenantService.getUserByTenantid(usertenant.getTenantid());
                map.put(CommonConstant.NAME, listTenantBinding.get(0).getTenantName());
                map.put(CommonConstant.TIME, date);
                map.put(CommonConstant.TMUSER, listTenantBinding.get(0).getTmUsernameList());
                map.put(CommonConstant.ANNOTATION, listTenantBinding.get(0).getAnnotation());
                map.put(CommonConstant.TENANTID, listTenantBinding.get(0).getTenantId());
                map.put(CommonConstant.NAMESPACES, listTenantBinding.get(0).getK8sNamespaceList());
                map.put(CommonConstant.HARBORPROJECTS,harborProjectList);
                map.put(CommonConstant.K8SPVS, listTenantBinding.get(0).getK8sPvList());
                map.put(CommonConstant.NAMESPACENUM, listTenantBinding.get(0).getK8sNamespaceList().size());
                map.put(CommonConstant.HARBORPUBLICPERJECTNUM, "1");
                map.put(CommonConstant.TENANTUSERNUM, userByTenantid.size());
                data.add(map);
            }
        }
        // TODO
        // 去重
        // List<Map<String, Object>> newData = new ArrayList<Map<String,
        // Object>>();
        // for (int i = 0; i < data.size(); i++) {
        // Map<String, Object> oldMap = data.get(i);
        // if (newData.size() > 0) {
        // boolean isContain = false;
        // for (int j = 0; j < newData.size(); j++) {
        // Map<String, Object> newMap = newData.get(j);
        // if
        // (newMap.get(CommonConstant.TENANTID).equals(oldMap.get(CommonConstant.TENANTID)))
        // {
        // for (String key : oldMap.keySet()) {
        // newMap.put(key, oldMap.get(key));
        // }
        // isContain = true;
        // break;
        // }
        // }
        // if (!isContain) {
        // newData.add(oldMap);
        // }
        // } else {
        // newData.add(oldMap);
        // }
        // }
        return ActionReturnUtil.returnSuccessWithData(data);
    }

    @Override
    public ActionReturnUtil tenantAlllist() throws Exception {
        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andIdIsNotNull();
        List<TenantBinding> listTenantBinding = tenantBindingMapper.selectByExample(example);
        Map<String, Object> tenant = new HashMap<String, Object>();
        for (TenantBinding tenantBinding : listTenantBinding) {
            tenant.put(tenantBinding.getTenantId(), tenantBinding);
        }
        return ActionReturnUtil.returnSuccessWithData(tenant);
    }

    @Override
    public ActionReturnUtil tenantdetail(String tenantid) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(tenantid)) {
            return ActionReturnUtil.returnErrorWithMsg("租户id不能为空");
        }
        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andTenantIdEqualTo(tenantid);
        List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
        if (list == null || list.size() <= 0 || list.get(0) == null) {
            return ActionReturnUtil.returnErrorWithMsg("tenantid错误");
        }
        TenantBinding tenantBinding = list.get(0);
        Map<String, Object> map = new HashMap<String, Object>();
        List<Object> namespaceData = (List<Object>) namespaceService.getNamespaceListByTenantid(tenantid).get("data");
        
        Boolean isTm = this.isTm(tenantid);
        // harborData.add(harborProjectList);
        // 查询user信息
        List<UserShowDto> userDetailsList = userTenantService.getUserDetailsListByTenantid(tenantid);
        HashMap<String, Object> tenant = new HashMap<>();
        tenant.put(CommonConstant.TENANTID, tenantid);
        tenant.put(CommonConstant.TENANTNAME, tenantBinding.getTenantName());
        tenant.put(CommonConstant.ADMIN, tenantBinding.getTmUsernameList());
        tenant.put(CommonConstant.TM, isTm);
        map.put(CommonConstant.NAMESPACEDATA, namespaceData);

        map.put(CommonConstant.USERDATA, userDetailsList);
        map.put(CommonConstant.TENANT, tenant);
        return ActionReturnUtil.returnSuccessWithData(map);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ActionReturnUtil tenantcreate(String name, String annotation, String userStr, Integer cluster) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(name)) {
            return ActionReturnUtil.returnErrorWithMsg("租户名字不能为空");
        }
        if (CommonConstant.NETWORK_C.equals(networkFlag)) {
            return this.createTenant(name, annotation, userStr, cluster);
        }
        return ActionReturnUtil.returnError();
    }

    @Override
    public ActionReturnUtil tenantdetailByName(String tenantName) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(tenantName)) {
            return ActionReturnUtil.returnErrorWithMsg("租户名不能为空");
        }
        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andTenantNameEqualTo(tenantName);
        List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
        if (list == null || list.size() <= 0 || list.get(0) == null) {
            return ActionReturnUtil.returnErrorWithMsg("tenant name错误");
        }
        TenantBinding tenantBinding = list.get(0);
        String date = DateUtil.DateToString(tenantBinding.getCreateTime(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(CommonConstant.NAME, tenantBinding.getTenantName());
        map.put(CommonConstant.TIME, date);
        map.put(CommonConstant.TENANTID, tenantBinding.getTenantId());
        map.put(CommonConstant.ANNOTATION, tenantBinding.getAnnotation());
        map.put(CommonConstant.USER, getTmNameList(tenantBinding.getTenantId()));
        map.put(CommonConstant.HARBORPROJECTS, tenantBinding.getHarborProjectList());
        map.put(CommonConstant.K8SPVS, tenantBinding.getK8sPvs());
        map.put(CommonConstant.K8SNAMESPACES, tenantBinding.getK8sNamespaceList());
        map.put(CommonConstant.CREATETIME, tenantBinding.getCreateTime());

        return ActionReturnUtil.returnSuccessWithData(map);
    }

    /**
     * 获取租户信息
     */
    private List<UserShowDto> getTmNameList(String tenantid) throws Exception {
        if (!StringUtils.isEmpty(tenantid)) {
            List<UserShowDto> list = userTenantService.getUserDetailsListByTenantid(tenantid);
            return list;
        }
        return Collections.emptyList();
    }

    @Override
    public ActionReturnUtil getNamespaceUserList(String tenantname, String namespace) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(tenantname) || StringUtils.isEmpty(namespace)) {
            return ActionReturnUtil.returnErrorWithMsg("租户名不能为空");
        }
        // 查询namespace下rolebinding列表
        String label = new StringBuffer().append("nephele_tenant_").append(tenantname).append(CommonConstant.EQUALITY_SIGN).append(tenantname).toString();
        K8SClientResponse k8SClientResponse = roleBindingService.getRolebindingInNamespacebyLabelSelector(namespace, label);

        if (HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            if (StringUtils.isEmpty(k8SClientResponse.getBody()))
                return null;
            RoleBindingList roleBindingList = JsonUtil.jsonToPojo(k8SClientResponse.getBody(), RoleBindingList.class);

            List<NamespaceUserDto> userList = new ArrayList<>();
            for (RoleBinding roleBinding : roleBindingList.getItems()) {
                // 去除harbor的用户
                String roleName = roleBinding.getRoleRef().getName();
                if (!StringUtils.isEmpty(roleName) && roleName.startsWith(CommonConstant.HARBOR) == CommonConstant.FALSE) {

                    for (Subjects subjects : roleBinding.getSubjects()) {
                        // 组装用户信息
                        NamespaceUserDto user = new NamespaceUserDto();
                        user.setName(subjects.getName());
                        user.setRole(roleBinding.getRoleRef().getName());
                        user.setRoleBindingName(roleBinding.getMetadata().getName());
                        user.setTime(roleBinding.getMetadata().getCreationTimestamp());

                        userList.add(user);
                    }

                }
            }
            return ActionReturnUtil.returnSuccessWithData(userList);
        }
        return null;
    }

    @Override
    public ActionReturnUtil getSmplTenantDetail(String tenantid) throws Exception {

        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andTenantIdEqualTo(tenantid);
        List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
        if (list == null || list.size() <= 0 || list.get(0) == null) {
            return ActionReturnUtil.returnErrorWithMsg("tenantid错误");
        }
        TenantBinding tenantBinding = list.get(0);
        List<Object> data = new ArrayList<>();
        String date = DateUtil.DateToString(tenantBinding.getCreateTime(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z);
        Map<String, Object> map = new HashMap<>();
        map.put(CommonConstant.NAME, tenantBinding.getTenantName());
        map.put(CommonConstant.TIME, date);
        map.put(CommonConstant.TENANTID, tenantBinding.getTenantId());
        map.put(CommonConstant.ANNOTATION, tenantBinding.getAnnotation());
        map.put(CommonConstant.HARBORPROJECTS, tenantBinding.getHarborProjectList());
        map.put(CommonConstant.K8SPVS, tenantBinding.getK8sPvs());
        map.put(CommonConstant.K8SNAMESPACES, tenantBinding.getK8sNamespaces());
        map.put(CommonConstant.CREATETIME, tenantBinding.getCreateTime());

        data.add(map);
        return ActionReturnUtil.returnSuccessWithData(data);
    }

    @SuppressWarnings(CommonConstant.UNCHECKED)
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ActionReturnUtil tenantdelete(String tenantid) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(tenantid)) {
            return ActionReturnUtil.returnErrorWithMsg("租户id不能为空");
        }
        TenantBinding tenantBinding = this.getTenantByTenantid(tenantid);
        // 调用namespace接口查询是否有namespace
        ActionReturnUtil namespaceResult = isExitNamespace(tenantBinding);
        if ((Boolean) namespaceResult.get(CommonConstant.SUCCESS) == true) {
            if ((Boolean) namespaceResult.get(CommonConstant.DATA) == true) {
                return ActionReturnUtil.returnErrorWithMsg("请先删除分区！");
            }
        } else {
            return namespaceResult;
        }
        // 删除网络
        ActionReturnUtil networkDeleteByTenantid = networkService.networkDeleteByTenantid(tenantid);
        if ((Boolean) networkDeleteByTenantid.get(CommonConstant.SUCCESS) == false) {
            return networkDeleteByTenantid;
        }
        // 删除pv
        ActionReturnUtil pvResult = persistentVolumeService.deletePVBytenantid(tenantid);
        if ((Boolean) pvResult.get(CommonConstant.SUCCESS) == false) {
            return pvResult;
        }
        //删除租户配置文件
        ActionReturnUtil deleteConfigsByTenant = configCenterService.deleteConfigsByTenant(tenantBinding.getTenantName());
        if ((Boolean) deleteConfigsByTenant.get(CommonConstant.SUCCESS) == false) {
            return deleteConfigsByTenant;
        }
        // TODO
        // 删除私有harbor镜像库
        harborProjectTenantService.clearTenantProject(tenantid);
        //如果有外部服务则删除外部服务
        externalService.deleteOutServicebytenant(tenantBinding.getTenantName(),tenantBinding.getTenantId());
        //如果有应用模板删除应用模板
        String[] tenantN = {tenantBinding.getTenantName()};
        applicationService.deleteTemplateByTenant(tenantN);
        // 删除tenant信息
        tenantBindingMapper.deleteByTenantid(tenantid);
        // 删除租户与用户的关联
        userTenantService.deleteByTenantid(tenantid);

        return ActionReturnUtil.returnSuccess();
    }

    private ActionReturnUtil deletePv(PersistentVolume pv) throws Exception {
        // TODO Auto-generated method stub
        Map<String, Object> bodys = new HashMap<>();
        // 根据lable查询pv
        K8SURL url = new K8SURL();
        url.setResource(com.harmonycloud.k8s.constant.Resource.PERSISTENTVOLUME);
        K8SClientResponse k8SClientResponse = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, bodys, null);
        if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            logger.error("调用k8s接口删除pv失败", k8SClientResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg("k8s接口删除pv失败!  errMsg: " + k8SClientResponse.getBody());
        }
        return ActionReturnUtil.returnSuccessWithData("删除成功");
    }

    private ActionReturnUtil isExitHarbor(TenantBinding tenantBinding) throws Exception {

        try {

            if (StringUtils.isEmpty(tenantBinding.getHarborProjects())) {
                return ActionReturnUtil.returnSuccessWithData(false);
            }

            String[] projectIds = tenantBinding.getHarborProjects().split(CommonConstant.COMMA);

            for (String projectId : projectIds) {
                // 根据projectId查询harbor库是否存在harbor
                String harborSearchUrl = harborUrl + CommonConstant.HARBOR_PROJECT + CommonConstant.SLASH + projectId;

                String cookie = harborUtil.checkCookieTimeout();
                Map<String, Object> header = new HashMap<>();
                header.put(CommonConstant.COOKIE, cookie);
                header.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
                HttpClientResponse response = HttpClientUtil.doGet(harborSearchUrl, null, header);
                if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                    if (StringUtils.isEmpty(response.getBody()))
                        return ActionReturnUtil.returnSuccessWithData(CommonConstant.FALSE);

                    Map<String, Object> result = JsonUtil.convertJsonToMap(response.getBody());
                    if (StringUtils.isEmpty(result.get(CommonConstant.PROJECT_ID))) {
                        return ActionReturnUtil.returnSuccessWithData(false);
                    }
                    String project_id = (String) result.get(CommonConstant.PROJECT_ID);
                    if (projectId.equals(project_id)) {
                        return ActionReturnUtil.returnSuccessWithData(CommonConstant.TRUE);
                    }
                } else {
                    logger.error("根据projectId查询harbor失败, projectId=" + projectId);
                    return ActionReturnUtil.returnError();
                }
            }

        } catch (Exception e) {
            logger.error("根据projectId查询harbor库是否存在harbor失败", e.getMessage());
            return ActionReturnUtil.returnError();
        }
        return ActionReturnUtil.returnError();
    }

    /**
     * 租户是否包含存在的namespace
     *
     * @param tenantBinding
     * @return
     */
    private ActionReturnUtil isExitNamespace(TenantBinding tenantBinding) throws Exception {

        if (tenantBinding == null) {
            return ActionReturnUtil.returnErrorWithMsg("tenantid错误");
        }
        String k8sNamespaces = tenantBinding.getK8sNamespaces();
        // 租户未绑定namespace，返回true
        if (StringUtils.isEmpty(k8sNamespaces)) {
            return ActionReturnUtil.returnSuccessWithData(false);
        }
        // 查询k8s内namespace列表
        ActionReturnUtil simpleNamespaceListByTenant = namespaceService.getSimpleNamespaceListByTenant(tenantBinding.getTenantId());
        if ((Boolean) simpleNamespaceListByTenant.get(CommonConstant.SUCCESS) == false) {
            return simpleNamespaceListByTenant;
        }
        NamespaceList list = (NamespaceList) simpleNamespaceListByTenant.get("data");
        if (list.getItems() != null && list.getItems().isEmpty()) {
            return ActionReturnUtil.returnSuccessWithData(CommonConstant.FALSE);
        }
        for (Namespace namespace : list.getItems()) {
            // 租户如有绑定的namespace，返回true,否则返回false
            if (k8sNamespaces.indexOf(namespace.getMetadata().getName()) >= 0) {
                return ActionReturnUtil.returnSuccessWithData(CommonConstant.TRUE);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(CommonConstant.FALSE);
    }

    @SuppressWarnings(CommonConstant.UNCHECKED)
    private String generateTmUsers(String userStr) throws Exception {
        List<String> userList = JSONArray.fromObject(userStr);
        StringBuffer nameBuff = new StringBuffer();
        for (int i = 0; i < userList.size(); i++) {
            if (i == 0) {
                nameBuff.append(userList.get(i));
            } else {
                nameBuff.append(CommonConstant.COMMA).append(userList.get(i));
            }
        }

        return nameBuff.toString();
    }
    @Transactional
    private ActionReturnUtil createTenant(String name, String annotation, String userStr, Integer cluster) throws Exception {
        TenantBindingExample exsit = new TenantBindingExample();
        exsit.createCriteria().andTenantNameEqualTo(name);
        List<TenantBinding> listtenant = tenantBindingMapper.selectByExample(exsit);
        if (listtenant.size() > 0) {
            return ActionReturnUtil.returnErrorWithMsg("租户 " + name + "已经存在请重新输入");
        }
        // 生成tenantid
        String tenantid = this.getid();

        // 创建网络
        CreateNetwork network = new CreateNetwork();
        network.setNetworkname(name + "_network");
        NetworkTenant tenant = new NetworkTenant();
        tenant.setTenantname(name);
        tenant.setTenantid(tenantid);
        network.setTenant(tenant);
        ActionReturnUtil networkCreate = networkService.networkCreate(network);
        if ((Boolean) networkCreate.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
            return networkCreate;
        }

        // 组装tmUsers
        String user = generateTmUsers(userStr);
        Date date = TenantUtils.getUtctime();
        TenantBinding record = new TenantBinding();
        record.setAnnotation(annotation);
        record.setTenantId(tenantid);
        record.setTenantName(name);
        record.setCreateTime(date);
        record.setTmUsernames(user);
        record.setClusterId(cluster);
        tenantBindingMapper.insertSelective(record);

        // 拆分
        ActionReturnUtil addusertodb = addusertodb(tenantid, user, CommonConstant.TRUE,"tm");
        if ((Boolean) addusertodb.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
            logger.error("租户向数据库插入用户失败,tenantId=" + tenantid);
            return addusertodb;
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(CommonConstant.NAME, name);
        map.put(CommonConstant.USER, user);
        map.put(CommonConstant.TENANTID, tenantid);
        map.put(CommonConstant.ANNOTATION, annotation);
        return ActionReturnUtil.returnSuccessWithData(map);
    }

    @Override
    public ActionReturnUtil listTenantByClusterId(Integer clusterId) throws Exception {
        if (clusterId == null) {
            return ActionReturnUtil.returnErrorWithMsg("cluserId 不能为空！");
        }
        return this.getTenantlistWithoutUsername(clusterId);
    }
    @Override
    public ActionReturnUtil listTenantUsers(String tenantid) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(tenantid)) {
            return ActionReturnUtil.returnErrorWithMsg("租户id不能为空");
        }
        List<UserTenant> list = userTenantService.getUserByTenantid(tenantid);
        return ActionReturnUtil.returnSuccessWithData(list);
    }
    @Override
    public Cluster getClusterByTenantid(String tenantid) throws Exception {

        TenantBinding ten = this.getTenantByTenantid(tenantid);
        if (ten == null) {
            return null;
        }
        Cluster cluster = clusterService.findClusterById(ten.getClusterId().toString());
        return cluster;
    }
    @Override
    public ActionReturnUtil addTenantUser(String tenantid, String username, String role) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(tenantid) || StringUtils.isEmpty(username) || StringUtils.isEmpty(role)) {
            return ActionReturnUtil.returnErrorWithMsg("租户id，用户名，用户角色不能为空");
        }
        // 检查用户是否已经存在
        UserTenant userT = this.userTenantService.getUserByUserNameAndTenantid(username, tenantid);
        if (userT != null) {
            return ActionReturnUtil.returnErrorWithMsg("租户中user:" + username + "已经存在");
        }
        String label = "nephele_tenantid=" + tenantid;
        TenantBinding tenantByTenantid = this.getTenantByTenantid(tenantid);
        Object currentUser = session.getAttribute("username");
        if(currentUser == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        //添加用户binding到harbor的镜像仓库
        ActionReturnUtil addProjctsToUser = harborProjectTenantService.addProjectsToUser(username,tenantid);
        if ((Boolean) addProjctsToUser.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
            logger.error("添加用户："+username+"到harbor的镜像仓库失败，" + addProjctsToUser);
            return addProjctsToUser;
        }
        String currentUserName = currentUser.toString();
        if(!this.isAdmin(tenantid, currentUserName)){
            return ActionReturnUtil.returnErrorWithMsg("用户:" + username + "不为管理员或者租户管理员，不能添加用户操作");
        }
        Cluster cluster = this.getClusterByTenantid(tenantid);
        NamespaceList namespaceList = this.namespaceService1.getNamespacesListbyLabelSelector(label, cluster);
        List<Namespace> items = namespaceList.getItems();
        if (items.size() >= 1) {
            String[] user = username.split(CommonConstant.COMMA);
            for (Namespace namespace : items) {
                String name = namespace.getMetadata().getName();
                for (String u : user) {
                    K8SClientResponse response = roleBindingService.addUserToRoleBinding(name, CommonConstant.DEV, u, cluster);
                    if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                        logger.info("namespace: " + name + "赋权失败！", response.getBody());
                        return ActionReturnUtil.returnErrorWithMsg("namespace: " + name + "赋权失败！错误消息：" + response.getBody());
                    }
                }
            }
        } else if (tenantByTenantid.getK8sNamespaceList().size() > 0) {
            return ActionReturnUtil.returnErrorWithMsg("tenant：" + tenantByTenantid.getTenantName() + " 数据库中分区与k8s中的分区数据不一致");
        }
        // 向数据库中同步数据
        ActionReturnUtil addusertodb = this.addusertodb(tenantid, username,
                CommonConstant.TM.equalsIgnoreCase(role) == CommonConstant.TRUE ? CommonConstant.TRUE : CommonConstant.FALSE,role);
        if ((Boolean) addusertodb.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
            logger.error("租户向数据库插入用户失败,tenantid=" + tenantid);
            return addusertodb;
        }
        return ActionReturnUtil.returnSuccessWithData("添加成功");
    }

    public ActionReturnUtil addusertodb(String tenantid, String username, boolean isTm,String role) throws Exception {
        String[] user = username.split(CommonConstant.COMMA);
        List<String> userlist = new ArrayList<String>();
        for (String string : user) {
            userlist.add(string);
        }
        try {
            userTenantService.setUserByTenantid(tenantid, userlist, isTm,role);
        } catch (Exception e) {
            return ActionReturnUtil.returnErrorWithMsg("插入用户失败：" + e.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }
    public ActionReturnUtil deleteusertodb(String tenantid, String username) throws Exception {
        try {
            userTenantService.deleteByTenantidAndUserName(tenantid, username);
        } catch (Exception e) {
            return ActionReturnUtil.returnErrorWithMsg("插入用户失败：" + e.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }
    @Override
    public ActionReturnUtil listTenantBytenantName(String tenantName) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(tenantName)) {
            return ActionReturnUtil.returnErrorWithMsg("租户名不能为空");
        }
        TenantBindingExample exsit = new TenantBindingExample();
        exsit.createCriteria().andTenantNameEqualTo(tenantName);
        List<TenantBinding> listtenant = tenantBindingMapper.selectByExample(exsit);
        return ActionReturnUtil.returnSuccessWithData(listtenant);
    }
    public String getid() {
        // 通过uuid生成token
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        // 去掉"-"符号
        String id = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);
        return id;
    }

    @Override
    public TenantBinding getTenantByTenantid(String tenantid) {
        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andTenantIdEqualTo(tenantid);
        // 根据tenantid查询租户绑定信息
        List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
        if (list == null || list.size() <= 0 || list.get(0) == null) {
            return null;
        }
        return list.get(0);

    }

    @Override
	public Boolean isTm(String tenantid) throws Exception {
    	Object currentUser = session.getAttribute("username");
        if(currentUser == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        String currentUserName = currentUser.toString();
        List<UserTenant> tmByTenantid = userTenantService.getTMByTenantid(tenantid);
        if(tmByTenantid!=null&&tmByTenantid.size()>0){
            for (UserTenant userTenant : tmByTenantid) {
                if(currentUserName.equals(userTenant.getUsername())){
                    return true;
                }
            }
        }
        return false;
	}
	@Override
    public ActionReturnUtil listTenantTm(String tenantid) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(tenantid)) {
            return ActionReturnUtil.returnErrorWithMsg("租户id不能为空");
        }
        List<UserTenant> list = userTenantService.getTMByTenantid(tenantid);
        return ActionReturnUtil.returnSuccessWithData(list);
    }

    @Override
    public ActionReturnUtil removeTenantUser(String tenantid, String username) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(tenantid) || StringUtils.isEmpty(username)) {
            return ActionReturnUtil.returnErrorWithMsg("租户id，用户名不能为空");
        }
        String label = "nephele_tenantid=" + tenantid;
        TenantBinding tenantByTenantid = this.getTenantByTenantid(tenantid);
        Cluster cluster = this.getClusterByTenantid(tenantid);
        Object currentUser = session.getAttribute("username");
        if(currentUser == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        String currentUserName = currentUser.toString();
        if(!this.isAdmin(tenantid, currentUserName)){
            return ActionReturnUtil.returnErrorWithMsg("用户:" + username + "不为管理员或者租户管理员，不能删除用户操作");
        }
        NamespaceList namespaceList = this.namespaceService1.getNamespacesListbyLabelSelector(label, cluster);
        List<Namespace> items = namespaceList.getItems();
        if (items.size() >= 1) {
            for (Namespace namespace : items) {
                String name = namespace.getMetadata().getName();
                K8SClientResponse response = roleBindingService.deleteUserFormRoleBinding(name, CommonConstant.DEV, username, cluster);
                if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                    logger.error("namespace: " + name + "更新rolebinding失败！" + response.getBody());
                    return ActionReturnUtil.returnErrorWithMsg("namespace: " + name + "更新rolebinding失败！错误消息：" + response.getBody());
                }
            }
        } else if (tenantByTenantid.getK8sNamespaceList().size() > 0) {
            return ActionReturnUtil.returnErrorWithMsg("tenant：" + tenantByTenantid.getTenantName() + " 数据库中分区与k8s中的分区数据不一致");
        }
      //删除用户binding到harbor的镜像仓库
        ActionReturnUtil deleteProjctsToUser = harborProjectTenantService.deleteUserFromProjects(username,tenantid);
        if ((Boolean) deleteProjctsToUser.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
            logger.error("从harbor的镜像仓库失败删除用户："+username+"失败，" + deleteProjctsToUser);
            return deleteProjctsToUser;
        }
        // 向数据库中同步数据
        ActionReturnUtil deleteusertodb = this.deleteusertodb(tenantid, username);
        if ((Boolean) deleteusertodb.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
            logger.error("租户向数据库删除用户失败,tenantid=" + tenantid);
            return deleteusertodb;
        }
        return ActionReturnUtil.returnSuccessWithData("删除成功");
    }

    @Override
    public ActionReturnUtil addTrustmember(String tenantid, String trustTenantid) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(tenantid) || StringUtils.isEmpty(trustTenantid)) {
            return ActionReturnUtil.returnErrorWithMsg("租户id，信任租户id不能为空");
        }
        List<NetworkCalico> net1 = networkService.getnetworkbyTenantid(tenantid);
        if (net1.size() != 1) {
            return ActionReturnUtil.returnErrorWithMsg("租户的网络数量冲突");
        }
        List<NetworkCalico> net2 = networkService.getnetworkbyTenantid(trustTenantid);
        if (net2.size() != 1) {
            return ActionReturnUtil.returnErrorWithMsg("信任租户的网络数量冲突");
        }
        // 获取集群
        Cluster cluster = this.getClusterByTenantid(tenantid);
        // 获取租户对应的网络
        NetworkCalico networkfrom = net2.get(0);
        NetworkCalico networkto = net1.get(0);
        String networknamefrom = networkfrom.getNetworkname();
        String networknameto = networkto.getNetworkname();
        String networkidfrom = networkfrom.getNetworkid();
        String networkidto = networkto.getNetworkid();
        // 检查是否拓扑关系存在
        NetworkTopology topology2 = networkService.getTopologybyNetworkidfromAndNetworkidto(networkidfrom, networkidto, networknamefrom, networknameto);
        if (topology2 != null) {
            return ActionReturnUtil.returnErrorWithMsg("network " + networknamefrom + " to " + networknameto + " Topology was existed!");
        }
        Date date = TenantUtils.getUtctime();
        NetworkTopology topology = new NetworkTopology();
        topology.setCreatetime(date);
        topology.setNetId(networkidfrom);
        topology.setNetName(networknamefrom);
        topology.setTopology(networknamefrom + "_" + networknameto);
        topology.setDestinationid(networkidto);
        topology.setDestinationname(networknameto);
        networkService.createNetworkTopology(topology);
        String topology3 = topology.getTopology();
        String[] networkNames = topology3.split(CommonConstant.UNDER_LINE);
        // 查询from 和 to 对应的 ns并返回
        List<NamespceBindSubnet> listfrom = networkService.getsubnetbynetworkid(networkidfrom);
        if(listfrom!=null&&!listfrom.isEmpty()){
            for (NamespceBindSubnet namespceBindSubnet : listfrom) {
                ActionReturnUtil updateNamespaceForTopology = namespaceService.updateNamespaceForTopology(networkNames, namespceBindSubnet.getNamespace(), cluster);
                if ((Boolean) updateNamespaceForTopology.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
                    logger.error("配置信任白名单信息到namespace失败,tenantid=" + tenantid + "信任租户id：" + trustTenantid + "错误信息：" + updateNamespaceForTopology.get(CommonConstant.ERRMSG));
                    return updateNamespaceForTopology;
                }
            }
        }

        List<NamespceBindSubnet> listto = networkService.getsubnetbynetworkid(networkidto);
        if(listto!=null&&!listto.isEmpty()){
            for (NamespceBindSubnet namespceBindSubnet : listto) {
                ActionReturnUtil createNetworkPolicy = namespaceService.createNetworkPolicy(namespceBindSubnet.getNamespace(), networkto.getNetworkname(), 1, networknamefrom,
                        networknameto, cluster);
                if ((Boolean) createNetworkPolicy.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
                    logger.error("创建信任白名单的网络规则失败,tenantid=" + tenantid + "信任租户id：" + trustTenantid + "错误信息：" + createNetworkPolicy.get(CommonConstant.ERRMSG));
                    return createNetworkPolicy;
                }
            } 
        }

        return ActionReturnUtil.returnSuccessWithData("配置信任白名单成功");
    }

    @Override
    public ActionReturnUtil removeTrustmember(String tenantid, String trustTenantid) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(tenantid) || StringUtils.isEmpty(trustTenantid)) {
            return ActionReturnUtil.returnErrorWithMsg("租户id，信任租户id不能为空");
        }
        List<NetworkCalico> net1 = networkService.getnetworkbyTenantid(tenantid);
        if (net1.size() != 1) {
            return ActionReturnUtil.returnErrorWithMsg("租户的网络数量冲突");
        }
        List<NetworkCalico> net2 = networkService.getnetworkbyTenantid(trustTenantid);
        if (net2.size() != 1) {
            return ActionReturnUtil.returnErrorWithMsg("信任租户的网络数量冲突");
        }
        // 获取集群
        Cluster cluster = this.getClusterByTenantid(tenantid);
        // 获取租户对应的网络
        NetworkCalico networkfrom = net2.get(0);
        NetworkCalico networkto = net1.get(0);
        String networknamefrom = networkfrom.getNetworkname();
        String networknameto = networkto.getNetworkname();
        String networkidfrom = networkfrom.getNetworkid();
        String networkidto = networkto.getNetworkid();
        // 检查是否拓扑关系存在
        NetworkTopology topology2 = networkService.getTopologybyNetworkidfromAndNetworkidto(networkidfrom, networkidto, networknamefrom, networknameto);
        if (topology2 == null) {
            return ActionReturnUtil.returnErrorWithMsg("network " + networknamefrom + " to " + networknameto + " 不在信任白名单中！");
        }
        String topology3 = topology2.getTopology();
        String[] networkNames = topology3.split(CommonConstant.UNDER_LINE);
        // 查询from 和 to 对应的 ns并返回
        List<NamespceBindSubnet> listfrom = networkService.getsubnetbynetworkid(networkidfrom);
        for (NamespceBindSubnet namespceBindSubnet : listfrom) {
            ActionReturnUtil updateNamespaceForTopology = namespaceService.removeNamespaceForTopology(networkNames, namespceBindSubnet.getNamespace(), cluster);
            if ((Boolean) updateNamespaceForTopology.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
                logger.error("移除信任白名单信息同步到namespace失败,tenantid=" + tenantid + "信任租户id：" + trustTenantid + "错误信息：" + updateNamespaceForTopology.get(CommonConstant.ERRMSG));
                return updateNamespaceForTopology;
            }
        }

        List<NamespceBindSubnet> listto = networkService.getsubnetbynetworkid(networkidto);
        for (NamespceBindSubnet namespceBindSubnet : listto) {
            ActionReturnUtil createNetworkPolicy = namespaceService.removeNetworkPolicy(namespceBindSubnet.getNamespace(), networknamefrom, networknameto, cluster);
            if ((Boolean) createNetworkPolicy.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
                logger.error("移除信任白名单的网络规则失败,tenantid=" + tenantid + "信任租户id：" + trustTenantid + "错误信息：" + createNetworkPolicy.get(CommonConstant.ERRMSG));
                return createNetworkPolicy;
            }
        }
        // 删除拓扑关系
        networkService.deletetopologybyId(topology2.getId());

        return ActionReturnUtil.returnSuccessWithData("移除信任白名单成功");
    }

    @Override
    public List<TenantDto> listTrustmember(String tenantid) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(tenantid)) {
            throw new MarsRuntimeException("租户id不能为空");
        }
        // 检查租户是否存在
        TenantBinding tenantByTenantid = this.getTenantByTenantid(tenantid);
        if (tenantByTenantid == null) {
            throw new MarsRuntimeException("租户id：" + tenantid + " 所对应的租户不存在!");
        }
        List<NetworkCalico> getnetworkbyTenantid = networkService.getnetworkbyTenantid(tenantid);
        if (getnetworkbyTenantid.size() != 1) {
            throw new MarsRuntimeException("tenantid:的网络数量有误，请检查");
        }
        // 获取信任白名单拓扑关系
        List<NetworkTopology> trustNetworkTopologyList = networkService.getTrustNetworkTopologyList(getnetworkbyTenantid.get(0).getNetworkid());
        List<TenantDto> list = new ArrayList<TenantDto>();
        for (NetworkTopology networkTopology : trustNetworkTopologyList) {
            NetworkCalico getnetworkbyNetworkid = networkService.getnetworkbyNetworkid(networkTopology.getNetId());
            // 组装白名单tenant
            if(getnetworkbyNetworkid != null){
            	TenantDto tenant = new TenantDto();
                tenant.setName(getnetworkbyNetworkid.getTenantname());
                tenant.setTenantId(getnetworkbyNetworkid.getTenantid());
                list.add(tenant);
            }
        }
        return list;
    }

    @Override
    public List<TenantBinding> listAvailableTrustmemberTenantList(String tenantid) throws Exception {
        Cluster cluster = this.getClusterByTenantid(tenantid);
        TenantBindingExample example = new TenantBindingExample ();
        example.createCriteria().andClusterIdEqualTo(Integer.valueOf(cluster.getId().toString()));
        List<TenantBinding> list = this.tenantBindingMapper.selectByExample(example);
        List<TenantDto> listTrustmember = this.listTrustmember(tenantid);
        List<TenantBinding> result = new ArrayList<>();
        if(list!=null&&list.size()>0){
            for (TenantBinding tenantBinding : list) {
               if(listTrustmember!=null&&listTrustmember.size()>0){
                   boolean flag = false;
                   for (TenantDto tenantDto : listTrustmember) {
                       if(tenantBinding.getTenantId().equals(tenantDto.getTenantId())){
                           flag = true;
                           break;
                       }
                   }
                   if(!flag){
                       result.add(tenantBinding);
                   }
               }else{
                   result = list;
               }
            }
        }
        return result;
    }
    @Override
    public Map<String, Object> listTenantQuota(String tenantid) throws Exception {
        // 初始化判断1
        if (StringUtils.isEmpty(tenantid)) {
            throw new MarsRuntimeException("租户id不能为空");
        }
        // 获取租户信息
        ActionReturnUtil smplTenantDetail = this.getSmplTenantDetail(tenantid);
        if ((Boolean) smplTenantDetail.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
            throw new MarsRuntimeException("错误信息：" + smplTenantDetail.get(CommonConstant.ERRMSG));
        }
        List<Object> data = (List<Object>) smplTenantDetail.get(CommonConstant.DATA);
        if (data.size() != 1) {
            throw new MarsRuntimeException("租户信息有误");
        }
        Map<String, Object> tenant = (Map<String, Object>) data.get(0);
        // 获取租户分区列表
        ActionReturnUtil namespaceListByTenantid = namespaceService.getNamespaceListByTenantid(tenantid);
        if ((Boolean) namespaceListByTenantid.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
            throw new MarsRuntimeException("错误信息：" + namespaceListByTenantid.get(CommonConstant.ERRMSG));
        }
        List<Map<String, Object>> namespaceData = (List<Map<String, Object>>) namespaceListByTenantid.get(CommonConstant.DATA);
        double limitMen = 0;
        double useMen = 0;
        double limitCpu = 0;
        double useCpu = 0;
        for (Map<String, Object> map2 : namespaceData) {
            // 处理内存
            String usedtype = map2.get(CommonConstant.USEDTYPE).toString();
            List<String> memory = (List<String>) map2.get(CommonConstant.MEMORY);
            String str = null;
            switch (usedtype) {
                case CommonConstant.MB :
                    str= memory.size() == 2 ? memory.get(1) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    useMen = useMen + Double.parseDouble(str);
                    break;
                case CommonConstant.GB :
                    str= memory.size() == 2 ? memory.get(1) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    useMen = useMen + Double.parseDouble(str) * 1024;
                    break;
                case CommonConstant.TB :
                    str= memory.size() == 2 ? memory.get(1) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    useMen = useMen + Double.parseDouble(str) * 1024 * 1024;
                    break;
                case CommonConstant.PB :
                    str= memory.size() == 2 ? memory.get(1) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    useMen = useMen + Double.parseDouble(str) * 1024 * 1024 * 1024;
                    break;
            }
            String hardtype = map2.get(CommonConstant.HARDTYPE).toString();
            switch (hardtype) {
                case CommonConstant.MB :
                    str= memory.size() == 2 ? memory.get(0) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    limitMen = limitMen + Double.parseDouble(str);
                    break;
                case CommonConstant.GB :
                    str= memory.size() == 2 ? memory.get(0) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    limitMen = limitMen + Double.parseDouble(str) * 1024;
                    break;
                case CommonConstant.TB :
                    str= memory.size() == 2 ? memory.get(0) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    limitMen = limitMen + Double.parseDouble(str) * 1024 * 1024;
                    break;
                case CommonConstant.PB :
                    str= memory.size() == 2 ? memory.get(0) : CommonConstant.ZERONUM;
                    if(str.contains(",")){
                        str = str.replaceAll(",", "");
                    }
                    limitMen = limitMen + Double.parseDouble(str) * 1024 * 1024 * 1024;
                    break;
            }
            // 处理cpu
            List<String> cpu = (List<String>) map2.get(CommonConstant.CPU);
            limitCpu = limitCpu + Double.parseDouble(cpu.size() == 2 ? cpu.get(0) : CommonConstant.ZERONUM);
            useCpu = useCpu + Double.parseDouble(cpu.size() == 2 ? cpu.get(1) : CommonConstant.ZERONUM);
        }
        int hardnum = 1;
        int usednum = 1;
        while (limitMen >= 1024) {
            limitMen = limitMen / 1024;
            hardnum = hardnum + 1;
        }
        while (useMen >= 1024) {
            useMen = useMen / 1024;
            usednum = usednum + 1;
        }
        if (usednum == 1 && useMen == 0) {
            usednum = hardnum;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        switch (hardnum) {
            case 1 :
                map.put(CommonConstant.HARDTYPE, CommonConstant.MB);
                break;
            case 2 :
                map.put(CommonConstant.HARDTYPE, CommonConstant.GB);
                break;
            case 3 :
                map.put(CommonConstant.HARDTYPE, CommonConstant.TB);
                break;
            case 4 :
                map.put(CommonConstant.HARDTYPE, CommonConstant.PB);
                break;
        }
        switch (usednum) {
            case 1 :
                map.put(CommonConstant.USEDTYPE, CommonConstant.MB);
                break;
            case 2 :
                map.put(CommonConstant.USEDTYPE, CommonConstant.GB);
                break;
            case 3 :
                map.put(CommonConstant.USEDTYPE, CommonConstant.TB);
                break;
            case 4 :
                map.put(CommonConstant.USEDTYPE, CommonConstant.PB);
                break;
        }
        // 保留两位小数 四舍五入
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.UP);
        List<Object> cpu = new LinkedList<>();
        List<Object> memory = new LinkedList<>();
        cpu.add(limitCpu % 1.0 == 0 ? (long) limitCpu : limitCpu);
        cpu.add(useCpu % 1.0 == 0 ? (long) useCpu : useCpu);
        memory.add(limitMen % 1.0 == 0 ? (long) limitMen : nf.format(limitMen));
        memory.add(useMen % 1.0 == 0 ? (long) useMen : nf.format(useMen));
        map.put(CommonConstant.MEMORY, memory);
        map.put(CommonConstant.CPU, cpu);
        map.put(CommonConstant.TENANTNAME, tenant.get(CommonConstant.NAME));
        map.put(CommonConstant.TENANTID, tenant.get(CommonConstant.TENANTID));
        return map;
    }

    @Override
    public List<String> findByTenantName(String tenantName) throws Exception {

        List<String> lists = tenantBindingMapper.selectByTenanantName(tenantName);

        return lists;
    }

    @Override
    public ActionReturnUtil listTenantsByUserName(String userName, boolean isAdmin) throws Exception {
        List tenants = new ArrayList();
        if (isAdmin) {
            tenants = tenantBindingMapper.selectAllTenantNames();
        } else {
            tenants = tenantBindingMapper.selectTenantsByUserName(userName);
        }
        // Map<String,Object> tenantlist = new HashMap<>();
        return ActionReturnUtil.returnSuccessWithData(tenants);
    }

    @Override
    public ActionReturnUtil listTenantsByUserNameForAudit(String userName, boolean isAdmin) throws Exception {
        List tenants = new ArrayList();
        if (isAdmin) {
            tenants = tenantBindingMapper.selectAllTenantNames();
        } else {
            tenants = tenantBindingMapper.selectTenantsByUserName(userName);
        }

        if (tenants != null && tenants.size() > 0) {
            TenantBinding tb = new TenantBinding();
            tb.setTenantName("all");
            tenants.add(0, tb);
        }

        return ActionReturnUtil.returnSuccessWithData(tenants);
    }
    @Override
    public Map getTenantQuotaByClusterId(String clusterId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        double totalCpu = 0;
        double totalMemory = 0;
        ActionReturnUtil tenantList = this.tenantList(null, Integer.valueOf(clusterId));
        List<Map<String, Object>> list = (List<Map<String, Object>>) tenantList.get(CommonConstant.DATA);
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map2 : list) {
                Map<String, Object> tenantQuota = this.listTenantQuota(map2.get(CommonConstant.TENANTID).toString());
                List<Object> memory = (List<Object>) tenantQuota.get(CommonConstant.MEMORY);
                List<Object> cpu = (List<Object>) tenantQuota.get(CommonConstant.CPU);
                String hardType = tenantQuota.get(CommonConstant.HARDTYPE).toString();
                switch (hardType) {
                    case CommonConstant.MB :
                        totalMemory = totalMemory + (Double.valueOf(memory.size() == 2 ? memory.get(0).toString() : CommonConstant.ZERONUM));
                        break;
                    case CommonConstant.GB :
                        totalMemory = totalMemory + (Double.valueOf(memory.size() == 2 ? memory.get(0).toString() : CommonConstant.ZERONUM) * 1024);
                        break;
                    case CommonConstant.TB :
                        totalMemory = totalMemory + (Double.valueOf(memory.size() == 2 ? memory.get(0).toString() : CommonConstant.ZERONUM) * 1024 * 1024);
                        break;
                    case CommonConstant.PB :
                        totalMemory = totalMemory + (Double.valueOf(memory.size() == 2 ? memory.get(0).toString() : CommonConstant.ZERONUM) * 1024 * 1024 * 1024);
                        break;
                }
                // 处理cpu
                totalCpu = totalCpu + (cpu.size() == 2 ? Double.valueOf( cpu.get(0).toString()) : Double.valueOf(CommonConstant.ZERONUM));
            }
        }
        result.put(CommonConstant.MEMORY, totalMemory);
        result.put(CommonConstant.CPU, totalCpu);
        result.put(CommonConstant.USEDTYPE, CommonConstant.MB);
        return result;
    }
    @Override
    public boolean isAdmin(String tenantid, String username) throws Exception {
        if(org.apache.commons.lang3.StringUtils.isBlank(tenantid)||org.apache.commons.lang3.StringUtils.isBlank(username)){
            throw new MarsRuntimeException("username or tenantid 不能为空");
        }
        User user = userService.getUser(username);
        if(user==null){
            throw new MarsRuntimeException("用户不存在");
        }
        if(user.getIsAdmin()==1){
            return true;
        }
        List<UserTenant> tmByTenantid = userTenantService.getTMByTenantid(tenantid);
        if(tmByTenantid!=null&&tmByTenantid.size()>0){
            for (UserTenant userTenant : tmByTenantid) {
                if(username.equals(userTenant.getUsername())){
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public List<TenantBinding> testTime(Integer domain) throws Exception {
        Date date=new Date();  
        Calendar calendar = Calendar.getInstance();  
        calendar.setTime(date);  
        calendar.add(Calendar.DAY_OF_MONTH, -domain);  
        Date leftDate = calendar.getTime();
        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andCreateTimeBetween(leftDate, date).andCreateTimeIsNotNull();
        List<TenantBinding> listTenantBinding = tenantBindingMapper.selectByExample(example);
        return listTenantBinding;
    }
    
}
