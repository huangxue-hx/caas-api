package com.harmonycloud.service.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.tenant.TenantBindingMapper;
import com.harmonycloud.dao.user.RoleMapper;
import com.harmonycloud.dao.user.bean.InitClusterRole;
import com.harmonycloud.dao.user.bean.InitClusterRoleEnum;
import com.harmonycloud.dao.user.bean.RoleResource;
import com.harmonycloud.dto.user.ClusterRoleDetailDto;
import com.harmonycloud.dto.user.ClusterRoleDto;
import com.harmonycloud.dto.user.UserDetailDto;
import com.harmonycloud.k8s.bean.ClusterRole;
import com.harmonycloud.k8s.bean.ClusterRoleList;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.PolicyRule;
import com.harmonycloud.k8s.bean.RoleBinding;
import com.harmonycloud.k8s.bean.RoleBindingList;
import com.harmonycloud.k8s.bean.Subjects;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.ClusterRoleAnnotations;
import com.harmonycloud.k8s.service.APIResourceService;
import com.harmonycloud.k8s.service.ClusterRoleService;
import com.harmonycloud.k8s.service.RoleBindingService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.tenant.TenantService;



/**
 * 角色业余层
 * 
 * @author yj
 * @date 2017年1月6日
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RoleService {

    @Autowired
    private RoleBindingService roleBindingService;
    @Autowired
    private ClusterRoleService clusterRoleService;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantBindingMapper tenantBindingMapper;

    @Autowired
    private APIResourceService apiResourceService;

    public static String ROLE_DEV_RB = "dev-rb";
    public static String ROLE_PM_RB = "pm-rb";
    public static String ROLE_TEST_RB = "test-rb";
    public static String ROLE_TM_RB = "tm-rb";

    /**
     * 获取所有clusterRoles
     */
    public ActionReturnUtil listClusterRoles() throws Exception {
        K8SClientResponse response = clusterRoleService.listClusterRoles();
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            ClusterRoleList clusterRoleList = K8SClient.converToBean(response, ClusterRoleList.class);
            List<ClusterRoleDto> roleDtos = new LinkedList<>();
            List<ClusterRole> items = clusterRoleList.getItems();
            for (ClusterRole clusterRole : items) {
                // 排除harbor和非资源的权限
                if (clusterRole.getMetadata().getName().startsWith("harbor") == false && clusterRole.getMetadata().getName().startsWith("nonResource") == false) {
                    ClusterRoleDto roleDto = new ClusterRoleDto();
                    roleDto.setName(clusterRole.getMetadata().getName());
                    roleDto.setType("ClusterRole");
                    if (clusterRole.getMetadata().getAnnotations() != null) {
                        roleDto.setIndex(String.valueOf(clusterRole.getMetadata().getAnnotations().get("roleIndex")));
                    }
                    roleDto.setTime(clusterRole.getMetadata().getCreationTimestamp());
                    roleDtos.add(roleDto);
                }
            }
            return ActionReturnUtil.returnSuccessWithData(roleDtos);
        }
        return ActionReturnUtil.returnErrorWithMsg(response.getBody());
    }

    /**
     * 获取ClusterRole明细信息
     * 
     * @param roleName
     *            eg:dev
     * @return
     */
    public ActionReturnUtil getClusterRoleDetail(String roleName) throws Exception {

        // 如果为admin,则

        List<String> operations = new ArrayList<>();
        operations.add("get");
        operations.add("list");
        operations.add("create");
        operations.add("update");
        operations.add("patch");
        operations.add("watch");
        operations.add("proxy");
        operations.add("redirect");
        operations.add("delete");
        operations.add("deletecollection");
        K8SClientResponse response = clusterRoleService.getSpecifiedClusterRoles(roleName);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            ClusterRole clusterRole = K8SClient.converToBean(response, ClusterRole.class);
            ClusterRoleDetailDto clusterRoleDetailDto = new ClusterRoleDetailDto();
            clusterRoleDetailDto.setName(clusterRole.getMetadata().getName());
            clusterRoleDetailDto.setIndex(Integer.valueOf(clusterRole.getMetadata().getAnnotations().get(ClusterRoleAnnotations.roleIndex.toString()).toString()));
            clusterRoleDetailDto.setTime(clusterRole.getMetadata().getCreationTimestamp());
            clusterRoleDetailDto.setType(clusterRole.getKind());
            // 设置非资源性操作
            List<PolicyRule> rules = clusterRole.getRules();
            List<RoleResource> resources = new LinkedList<RoleResource>();
            for (PolicyRule rule : rules) {
                if (rule.getResources() != null) {
                    for (String str : rule.getResources()) {
                        // 处理返回为*的结果
                        if (str.equals("*")) {
                            List<String> listAPIResourceNames = apiResourceService.listAPIResourceNames();
                            for (String name : listAPIResourceNames) {
                                RoleResource res = new RoleResource();
                                res.setName(name);
                                res.setApiGroups(rule.getApiGroups());
                                if (rule.getVerbs().get(0).equals("*")) {
                                    res.setOperations(operations);
                                } else {
                                    res.setOperations(rule.getVerbs());
                                }
                                resources.add(res);
                            }
                        } else {
                            RoleResource res = new RoleResource();
                            res.setName(str);
                            res.setApiGroups(rule.getApiGroups());
                            if (rule.getVerbs().equals("*")) {
                                res.setOperations(operations);
                            } else {
                                res.setOperations(rule.getVerbs());
                            }
                            resources.add(res);
                        }

                    }
                    List<String> imagePperations = new ArrayList<>();
                    imagePperations.add("read");
                    imagePperations.add("write");
                    com.harmonycloud.dao.user.bean.RoleResource resource = new com.harmonycloud.dao.user.bean.RoleResource();
                    resource.setName("image");
                    resource.setOperations(imagePperations);
                    resources.add(resource);
                }
            }
            clusterRoleDetailDto.setResource(resources);
            return ActionReturnUtil.returnSuccessWithData(clusterRoleDetailDto);
        }
        return ActionReturnUtil.returnErrorWithMsg(response.getBody());
    }
//TODO 由于使用统一权限控制现在暂时不需要 代码暂时保留，后面如果需要与k8s进一步整合，可能会使用,建议后期调试完成后删除
//    /**
//     * 绑定该用户下该tenantname下该namespace的角色
//     *
//     * @param tenantname
//     * @param tenantid
//     * @param namespace
//     * @param role
//     *            绑定的角色
//     * @param username
//     *            绑定的用户
//     */
//    public ActionReturnUtil rolebinding(String tenantname, String tenantid, String namespace, String role, String username) throws Exception {
//        Cluster cluster = tenantService.getClusterByTenantid(tenantid);
//        K8SClientResponse response = roleBindingService.addUserToRoleBinding(namespace, role, username,cluster,tenantid);
//        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
//            return ActionReturnUtil.returnSuccessWithData(K8SClient.converToBean(response, RoleBinding.class));
//        } else {
//            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
//        }
//    }

//    /**
//     * 绑定tm到该tenant下的所有namespace
//     *
//     * @param tenantid
//     * @param username
//     * @return
//     */
//    public void rolebindingTM(String tenantid, String username) throws Exception {
//        String tmUsernames = null;
//        Cluster cluster = tenantService.getClusterByTenantid(tenantid);
//        TenantBindingExample example = new TenantBindingExample();
//        example.createCriteria().andTenantIdEqualTo(tenantid);
//        List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
//        if (list != null && list.size() > 0) {
//            tmUsernames = list.get(0).getTmUsernames();
//        }
//        // 如果tmUsernames中没有username则插入数据库
//        Boolean flag = false;
//        if (org.apache.commons.lang3.StringUtils.isNotBlank(tmUsernames)) {
//            String[] names = tmUsernames.split(",");
//            for (String name : names) {
//                if (name.equals(username)) {
//                    flag = true;
//                }
//            }
//        }
//        if (!flag) {
//            tmUsernames = tmUsernames == null ? username : tmUsernames + "," + username;
//            TenantBinding tenantBinding = new TenantBinding();
//            tenantBinding.setTenantId(tenantid);
//            tenantBinding.setTmUsernames(tmUsernames);
//            tenantBindingMapper.updateBytenantIdSelective(tenantBinding);
//        }
//
//        // 该tenant下的所有namespace
//        String lable = "nephele_tenantid_" + tenantid + "=" + tenantid;
//        K8SClientResponse response = roleBindingService.getRolebindingListbyLabelSelector(lable);
//        RoleBindingList roleBindingList = K8SClient.converToBean(response, RoleBindingList.class);
//        List<RoleBinding> items = roleBindingList.getItems();
//        for (RoleBinding item : items) {
//            if (item.getMetadata().getName().equals(RoleBindingService.ROLE_TM_RB)) {
//                // 增加用户到该namespace下的subjects中
//                roleBindingService.addUserToRoleBinding(item.getMetadata().getNamespace(), RoleBindingService.ROLE_TM_RB, username,cluster,tenantid);
//            }
//        }
//    }

//    /**
//     * 解绑tm在该tenant下的角色
//     *
//     * @param tenantid
//     * @param username
//     * @return
//     */
//    public void roleUnbindingTM(String tenantid, String username) throws Exception {
//        // 该tenant下的所有namespace
//        String lable = "nephele_tenantid_" + tenantid + "=" + tenantid;
//        Cluster cluster = this.tenantService.getClusterByTenantid(tenantid);
//        K8SClientResponse response = roleBindingService.getRolebindingListbyLabelSelector(lable);
//        RoleBindingList roleBindingList = K8SClient.converToBean(response, RoleBindingList.class);
//        List<RoleBinding> items = roleBindingList.getItems();
//        for (RoleBinding item : items) {
//            if (item.getMetadata().getName().equals(RoleBindingService.ROLE_TM_RB)) {
//                // 删除用户从该namespace下的subjects中
//                roleBindingService.deleteUserFormRoleBinding(item.getMetadata().getNamespace(), RoleBindingService.ROLE_TM_RB, username,cluster);
//            }
//        }
//        String tmUsernames = null;
//        TenantBindingExample example = new TenantBindingExample();
//        example.createCriteria().andTenantIdEqualTo(tenantid);
//        List<TenantBinding> list = tenantBindingMapper.selectByExample(example);
//        if (list != null && list.size() > 0) {
//            tmUsernames = list.get(0).getTmUsernames();
//        }
//        // 如果tmUsernames中有username则删除该数据
//        String delUsers = "";
//        if (org.apache.commons.lang3.StringUtils.isNotBlank(tmUsernames)) {
//            String[] names = tmUsernames.split(",");
//            List<String> asList = Arrays.asList(names);
//            for (String name : asList) {
//                if (!name.equals(username)) {
//                    delUsers = delUsers + name + ",";
//                }
//            }
//            if (delUsers.length() > 0) {
//                delUsers = delUsers.substring(0, delUsers.length() - 1);
//            }
//            TenantBinding tenantBinding = new TenantBinding();
//            tenantBinding.setTenantId(tenantid);
//            tenantBinding.setTmUsernames(delUsers);
//            tenantBindingMapper.updateBytenantIdSelective(tenantBinding);
//        }
//    }
//
//    /**
//     * 解绑该用户下该tenantname下该namespace的角色
//     *
//     * @param tenantname
//     * @param tenantid
//     * @param namespace
//     * @param role
//     * @param username
//     */
//    public ActionReturnUtil roleUnbind(String tenantname, String tenantid, String namespace, String role, String username) throws Exception {
//        Cluster cluster = this.tenantService.getClusterByTenantid(tenantid);
//        K8SClientResponse response = roleBindingService.deleteUserFormRoleBinding(namespace, role, username,cluster);
//        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
//            return ActionReturnUtil.returnSuccessWithData(K8SClient.converToBean(response, RoleBinding.class));
//        } else {
//            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
//        }
//    }

    /**
     * 根据用户名查询角色及所在租户
     * 
     * @param username
     */
    public List<UserDetailDto> userDetail(String username) throws Exception {
        List<UserDetailDto> detailDtos = new ArrayList<>();
        String lable = "nephele_user_" + username + "=" + username;
        K8SClientResponse response = roleBindingService.getRolebindingListbyLabelSelector(lable);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            RoleBindingList roleBindingList = K8SClient.converToBean(response, RoleBindingList.class);
            List<RoleBinding> roleBindingItems = roleBindingList.getItems();
            for (RoleBinding item : roleBindingItems) {
                String regex_harbor = "harbor.*";
                // 查找非harbor角色
                if (item.getMetadata().getName().matches(regex_harbor) == false) {
                    UserDetailDto userDetailDto = new UserDetailDto();
                    userDetailDto.setName(item.getMetadata().getName());
                    userDetailDto.setHarborProjectId("");
                    userDetailDto.setImageVerb("");
                    userDetailDto.setNamespace(item.getMetadata().getNamespace());
                    userDetailDto.setRole(item.getRoleRef().getName());
                    Map<String, Object> labels = item.getMetadata().getLabels();
                    // 遍历label,找出tenantid和tenantname
                    for (Entry<String, Object> entry : labels.entrySet()) {
                        String key = entry.getKey();
                        String regex_tenantid = "^nephele_tenantid.*";
                        String regex_tenantname = "^nephele_tenant.*";
                        if (key.matches(regex_tenantid)) {
                            userDetailDto.setTenantId(String.valueOf(item.getMetadata().getLabels().get(key)));
                        }
                        if (key.matches(regex_tenantname) && key.matches(regex_tenantid) == false) {
                            userDetailDto.setTenantName(String.valueOf(item.getMetadata().getLabels().get(key)));
                        }
                    }
                    userDetailDto.setTime(item.getMetadata().getCreationTimestamp());
                    userDetailDto.setType(item.getRoleRef().getKind());
                    List<Subjects> subjects = item.getSubjects();
                    for (Subjects subject : subjects) {
                        List<String> users = new ArrayList<>();
                        List<String> userIds = new ArrayList<>();
                        users.add(subject.getName());
                        userIds.add("");
                        userDetailDto.setUserIds(userIds);
                        userDetailDto.setUses(users);
                    }
                    detailDtos.add(userDetailDto);
                }
            }
            return detailDtos;
        } else {
            return null;
        }
    }

    /**
     * 查询该namespace下的rolebinding的明细
     * 
     * @param namespace
     * @param
     * @return
     */
    public ActionReturnUtil getRoleBinding(String namespace, String roleBindingName) throws Exception {
        K8SClientResponse response = roleBindingService.getSpecifiedRolebindings(namespace, roleBindingName,null);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnSuccessWithData(response.getBody());
        }
        return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
    }

    /**
     * 根据用户名获取rolebinding
     * 
     * @param userName
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getRoleBindingByUser(String userName) throws Exception {
        String labels = "nephele_user_" + userName + "=" + userName;
        K8SClientResponse response = roleBindingService.getRolebindingListbyLabelSelector(labels);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
        RoleBindingList rbList = JsonUtil.jsonToPojo(response.getBody(), RoleBindingList.class);
        List<RoleBinding> roleBindings = rbList.getItems();
        List<Map<String, Object>> tenants = new ArrayList<Map<String, Object>>();
        if (roleBindings != null && roleBindings.size() > 0) {
            for (RoleBinding rBinding : roleBindings) {
                /*
                 * Map<String, Object> annotations =
                 * rBinding.getMetadata().getAnnotations(); if (annotations !=
                 * null) { if
                 * (StringUtils.isEmpty(annotations.get("project").toString()))
                 * { continue; } }
                 */
                Map<String, Object> tenant = new HashMap<String, Object>();
                Map<String, Object> labelMap = rBinding.getMetadata().getLabels();

                // 获取所有的key
                if (labelMap != null) {
                    for (Map.Entry<String, Object> m : labelMap.entrySet()) {
                        if (m.getKey().indexOf("nephele_tenant_") > -1) {
                            tenant.put("name", m.getKey().substring("nephele_tenant_".length()));
                        }
                        if (m.getKey().indexOf("nephele_tenantid_") > -1) {
                            tenant.put("tenantid", m.getKey().substring("nephele_tenantid_".length()));
                        }
                    }
                }
                tenants.add(tenant);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(CollectionUtil.rmDuplicate(tenants));
    }

    /**
     * 根据用户名获取rolebindingList
     * 
     * @param userName
     * @return RoleBindingList
     * @throws Exception
     */
    public RoleBindingList getRoleBindingByUsername(String userName) throws Exception {
        String labels = "nephele_user_" + userName + "=" + userName;
        K8SClientResponse response = roleBindingService.getRolebindingListbyLabelSelector(labels);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return K8SClient.converToBean(response, RoleBindingList.class);
        }
        return null;
    }

    /**
     * 根据tenantId和userName查询project
     * 
     * @param userName
     * @param tenantId
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getRoleBindingWithNamespace(String userName, String tenantId) throws Exception {
        String labels = "nephele_user_" + userName + "=" + userName;
        K8SClientResponse response = roleBindingService.getRolebindingListbyLabelSelector(labels);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
        RoleBindingList rbList = JsonUtil.jsonToPojo(response.getBody(), RoleBindingList.class);
        List<RoleBinding> roleBindings = rbList.getItems();
        List<String> namespaces = new ArrayList<String>();
        List<Map<String, Object>> res = new ArrayList<>();
        if (roleBindings != null && roleBindings.size() > 0) {
            for (RoleBinding rBinding : roleBindings) {
                /*
                 * Map<String, Object> annotations =
                 * rBinding.getMetadata().getAnnotations(); if (annotations !=
                 * null) { if (annotations.get("project") != null) { continue; }
                 * }
                 */
                Map<String, Object> labelMap = rBinding.getMetadata().getLabels();

                // 获取所有的key
                if (labelMap != null) {
                    for (Map.Entry<String, Object> m : labelMap.entrySet()) {
                        if (m.getKey().indexOf("nephele_tenantid_") > -1) {
                            if (m.getKey().substring("nephele_tenantid_".length()).equals(tenantId)) {
                                namespaces.add(rBinding.getMetadata().getNamespace());
                            }
                        }
                    }
                }
            }
        }

        // 去重
        for (int i = 0; i < namespaces.size() - 1; i++) {
            for (int j = namespaces.size() - 1; j > i; j--) {
                if (namespaces.get(j).equals(namespaces.get(i))) {
                    namespaces.remove(j);
                }
            }
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("k8sNamespaces", namespaces);
        res.add(map);
        return ActionReturnUtil.returnSuccessWithData(res);
    }

    /**
     * 初始化ClusterRole
     * 
     * @return
     */
    public ActionReturnUtil initClusterRole() throws Exception {
        try {
            for (InitClusterRoleEnum clusterRoleName : InitClusterRoleEnum.values()) {
                InitClusterRole initClusterRole = JsonUtil.jsonToPojo(clusterRoleName.getJson(), InitClusterRole.class);
                this.createClusterRole(initClusterRole);
            }
            return ActionReturnUtil.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return ActionReturnUtil.returnError();
        }

    }

    /**
     * 创建clusterRole,如果已存在或者创建失败返回false 创建成功返回true
     * 
     * @param clusterRole
     * @return
     */
    public boolean createClusterRole(InitClusterRole clusterRole) throws Exception {

        // 查询是否已经初始化,若未初始化,则初始化
        K8SClientResponse specifiedClusterRoles = clusterRoleService.getSpecifiedClusterRoles(clusterRole.getName());
        if (specifiedClusterRoles.getStatus() == 404) {
            // 集群中无该角色,初始化该角色
            ClusterRole createClusterRole = new ClusterRole();
            // 设置metadata
            ObjectMeta metadata = new ObjectMeta();
            metadata.setName(clusterRole.getName());
            Map<String, Object> labels = new HashMap<>();
            labels.put("nephele_roleName", clusterRole.getName());
            if (clusterRole.getName().contains("harbor")) {
                labels.put("nephele_image", "image");
            }
            metadata.setLabels(labels);
            Map<String, Object> annotations = new HashMap<>();
            annotations.put("roleIndex", clusterRole.getIndex());
            metadata.setAnnotations(annotations);
            // 设置rules
            List<PolicyRule> rules = new ArrayList<>();
            List<RoleResource> resources = clusterRole.getResource();
            for (RoleResource resource : resources) {
                PolicyRule rule = new PolicyRule();
                List<String> apiGroups = new ArrayList<>();
                apiGroups.add("*");
                List<String> resourceNames = new ArrayList<>();
                resourceNames.add(resource.getName());

                rule.setVerbs(resource.getOperations());
                rule.setResources(resourceNames);
                rule.setApiGroups(apiGroups);
                rules.add(rule);
            }
            createClusterRole.setMetadata(metadata);
            createClusterRole.setRules(rules);
            K8SClientResponse response = clusterRoleService.createClusterRole(createClusterRole);
            if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除clusterrole
     */
    public ActionReturnUtil deleteClusterrole(String name) throws Exception {
        K8SClientResponse deleteClusterRole = this.clusterRoleService.deleteClusterRole(name);
        if (HttpStatusUtil.isSuccessStatus(deleteClusterRole.getStatus())) {
            return ActionReturnUtil.returnSuccess();
        } else {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DELETE_FAIL);
        }
    }

}
