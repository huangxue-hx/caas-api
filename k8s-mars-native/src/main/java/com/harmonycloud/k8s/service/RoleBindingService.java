package com.harmonycloud.k8s.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.HarborProjectRoleEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.constant.Constant;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.ObjectReference;
import com.harmonycloud.k8s.bean.RoleBinding;
import com.harmonycloud.k8s.bean.RoleBindingList;
import com.harmonycloud.k8s.bean.Subjects;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class RoleBindingService {

	private static String ROLE_DEV_RB = "dev-rb";
	private static String ROLE_PM_RB = "pm-rb";
	private static String ROLE_TEST_RB = "test-rb";
	private static String ROLE_TM_RB = "tm-rb";

	/**
	 * 创建rolebinding
	 * @param namespace
	 * @param header
	 * @param bodys
	 * @return
	 */
	public K8SClientResponse create(String namespace, Map<String, Object> header,
									Map<String, Object> bodys,Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.ROLEBINDING);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, header, bodys,cluster);
		return response;

	}
	/**
	 * 获取namespace下的RoleBinding
	 * 
	 * @return
	 */
	public K8SClientResponse listRolebindingsByNamespace(String namespace) throws Exception{
		K8SURL url = new K8SURL();
		url.setResource(Resource.ROLEBINDING).setNamespace(namespace);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null);
		return response;
	}


	/**
	 * 获取该namespace下的RoleBinding对象
	 * 
	 * @param namespace
	 * @param RoleBindingName
	 *            eg:ROLE_TM_RB
	 * @return
	 */
	public K8SClientResponse getSpecifiedRolebindings(String namespace, String RoleBindingName,Cluster cluster) throws Exception{
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.ROLEBINDING).setSubpath(RoleBindingName);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,cluster);
		return response;
	}

	/**
	 * 获取该namespace下的RoleBinding对象
	 * 
	 * @param namespace
	 * @param RoleBindingName
	 *            eg:ROLE_TM_RB
	 * @return
	 */
	private RoleBinding getNamespacesRolebindings(String namespace, String RoleBindingName,Cluster cluster) throws Exception{
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.ROLEBINDING).setSubpath(RoleBindingName);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,cluster);
		RoleBinding r = K8SClient.converToBean(response, RoleBinding.class);
		return r;
	}

	/**
	 * 根据标签获取Rolebinding列表
	 * 
	 * @param lable
	 *            标签 eg:nephele_user_yangjipm=yangjipm
	 * @return RoleBindingList
	 */
	public K8SClientResponse getRolebindingListbyLabelSelector(String lable) throws Exception{
		/*Map<String, Object> bodys = new HashMap<>();
		bodys.put("labelSelector", lable);
		K8SURL url = new K8SURL();
		url.setResource(Resource.ROLEBINDING);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys);
		return response;*/
		Map<String, Object> bodys = new HashMap<>();
		bodys.put("labelSelector", lable);
		K8SURL url = new K8SURL();
		url.setResource(Resource.ROLEBINDING);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys);
		return response;
	}

	/**
	 * 根据该label查询namespace下的Rolebinding;lable为空时,查询namespace下的所有
	 * @param namespace
	 * @param lable
	 * @return RoleBindingList
	 */
	public K8SClientResponse getRolebindingInNamespacebyLabelSelector(String namespace,String lable) throws Exception{
		Map<String, Object> bodys = new HashMap<>();
		if(!StringUtils.isEmpty(lable)){
			bodys.put("labelSelector", lable);
		}
		K8SURL url = new K8SURL();
		url.setResource(Resource.ROLEBINDING).setNamespace(namespace);
		//先用Machine token
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys);
		return response;
	}
	public RoleBinding createRoleBinding(String name,String namespace,String tenantid,String tenantName,String role,Cluster cluster) throws Exception{
		// 组装objectMeta
		ObjectMeta objectMeta = new ObjectMeta();
		objectMeta.setName(name);
		objectMeta.setNamespace(namespace);
		objectMeta.setAnnotations(generateRoleBindingAnnotations(null, null, null));
		objectMeta.setLabels(generateRolebindingLabels(null, tenantName, tenantid));
		// 组装objectReference
		ObjectReference objectReference = new ObjectReference();
		objectReference.setName(role);
		objectReference.setApiVersion(Constant.API_VERSION_RBAC_V1);
		objectReference.setKind(CommonConstant.ROLE);

		// 组装rolebinding
		Map<String, Object> bodys = new HashMap<>();
		bodys.put(CommonConstant.KIND, CommonConstant.ROLEBINDING);
		bodys.put(CommonConstant.APIVERSION, Constant.API_VERSION_RBAC_V1);
		bodys.put(CommonConstant.METADATA, objectMeta);
		bodys.put(CommonConstant.SUBJECTS, generateSubjects(null));
		bodys.put(CommonConstant.ROLEREF, objectReference);
		Map<String, Object> headers = new HashMap<>();
		headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
		// 调用k8s接口生成rolebinding
		K8SClientResponse k8SClientResponse = this.create(namespace, headers, bodys, cluster);
		if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
			throw new MarsRuntimeException(k8SClientResponse.getBody());
		}
		RoleBinding r = K8SClient.converToBean(k8SClientResponse, RoleBinding.class);
		return r;
	}
	private Map<String, Object> generateRolebindingLabels(List<String> users, String tenantName, String tenantId) throws Exception {

		Map<String, Object> rolebindingLables = new HashMap<>();

		if (null != users) {
			for (String user : users) {
				rolebindingLables.put("nephele_user_" + user, user);
			}
		}
		rolebindingLables.put("nephele_tenant_" + tenantName, tenantName);
		rolebindingLables.put("nephele_tenantid_" + tenantId, tenantId);

		return rolebindingLables;
	}
	private Map<String, Object> generateRoleBindingAnnotations(String projectId, String role, String userId) throws Exception {

		Map<String, Object> annotations = new HashMap<>();

		// harbor projectId
		if (!StringUtils.isEmpty(projectId)) {
			annotations.put(CommonConstant.PROJECTID, projectId);
		}

		if (!StringUtils.isEmpty(role)) {

			if (role.equals(HarborProjectRoleEnum.DEV.getRole())) {
				annotations.put(CommonConstant.PROJECT, projectId);
				annotations.put(CommonConstant.VERBS, 2);
			}
			if (role.equals(HarborProjectRoleEnum.WATCHER.getRole())) {
				annotations.put(CommonConstant.PROJECT, projectId);
				annotations.put(CommonConstant.VERBS, 3);
			}
		}

		annotations.put(CommonConstant.USERID, userId == null ? "" : userId);
		return annotations;

	}

	private List<Subjects> generateSubjects(List<String> users) throws Exception {
		List<Subjects> subjectsList = new ArrayList<>();

		if (null != users) {
			for (String user : users) {
				Subjects subjects = new Subjects();
				subjects.setKind("User");
				subjects.setName(user);
				subjectsList.add(subjects);
			}
		}

		return subjectsList;
	}
	/**
	 * 新增用户到该namespace下的RoleBinding
	 * 
	 * @param namespace
	 * @param role
	 *            eg:pm-rb
	 * @param username
	 *            用户名 eg:zgl
	 * @return
	 */
	public K8SClientResponse addUserToRoleBinding(String namespace, String role, String username,Cluster cluster,String tenantid) throws Exception{
		String roleName = role;
		if (!roleName.endsWith("-rb")) {
			roleName = roleName + "-rb";
		}
		// 查询该rolebinding
		RoleBinding roleBinding = this.getNamespacesRolebindings(namespace, roleName,cluster);
		// 为空是新角色,则重新创建
		if (roleBinding == null){
			String tenantname = namespace.split(CommonConstant.LINE)[0];
			roleBinding = this.createRoleBinding(roleName, namespace, tenantid, tenantname, roleName.split("-rb")[0], cluster);
		}
		// 更新rolebinding
		String apiVersion = roleBinding.getApiVersion();
		ObjectMeta objectMeta = roleBinding.getMetadata();
		//更新label
		Map<String, Object> labels = objectMeta.getLabels();
		labels.put("nephele_user_"+username, username);
		objectMeta.setLabels(labels);
		
		ObjectReference objectReference = roleBinding.getRoleRef();
		String king = roleBinding.getKind();
		List<Subjects> subjects = roleBinding.getSubjects();
		if (subjects == null){
			subjects =  new ArrayList<>();
		}
		//遍历subjects如果没有则增加用户
		if(this.isInSubjects(subjects, username) == false){
			Subjects subject = new Subjects();
			subject.setName(username);
			subject.setApiVersion(Constant.API_VERSION_RBAC_V1);
			subject.setKind("User");
			subjects.add(subject);
		}
		Map<String, Object> bodys = new HashMap<>();
		bodys.put("metadata", objectMeta);
		bodys.put("roleRef", objectReference);
		bodys.put("subjects", subjects);
		bodys.put("kind", king);
		bodys.put("apiVersion", apiVersion);
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		K8SURL url = new K8SURL();
		url.setResource(Resource.ROLEBINDING).setNamespace(namespace).setSubpath(roleName);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys,cluster);
		return response;
	}

	/**
	 * 查询rolebinding明细
	 * 
	 * @param bodys
	 * @param header
	 * @param namespace
	 * @param rolebinding
	 * @return
	 */
	public K8SClientResponse getRoleBindingDetail(Map<String, Object> bodys, Map<String, Object> header,
			String namespace, String rolebinding,Cluster cluster) throws Exception{
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.ROLEBINDING);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, header, bodys,cluster);
		return response;
	}

	/**
	 * 删除该namespace下的RoleBinding中的用户
	 * @param namespace
	 * @param role
	 * @param username
	 * @return
	 */
	public K8SClientResponse deleteUserFormRoleBinding(String namespace, String role, String username,Cluster cluster) throws Exception{
		String roleName = role;
		if (!roleName.endsWith("-rb")) {
			roleName = roleName + "-rb";
		}
		// 查询该rolebinding
		RoleBinding roleBinding = this.getNamespacesRolebindings(namespace, roleName,cluster);
		// 更新rolebinding
		String apiVersion = roleBinding.getApiVersion();
		ObjectMeta objectMeta = roleBinding.getMetadata();
		//更新label
		Map<String, Object> labels = objectMeta.getLabels();
		labels.remove("nephele_user_"+username);
		objectMeta.setLabels(labels);
		ObjectReference objectReference = roleBinding.getRoleRef();
		String king = roleBinding.getKind();
		List<Subjects> subjects = roleBinding.getSubjects();
		// 遍历用户如果用户名相同,删除该用户
		for (Subjects subject : subjects) {
			if(subject.getName().equals(username)){
				subjects.remove(subject);
				break;
			}
		}

		Map<String, Object> bodys = new HashMap<>();
		bodys.put("metadata", objectMeta);
		bodys.put("roleRef", objectReference);
		bodys.put("subjects", subjects);
		bodys.put("kind", king);
		bodys.put("apiVersion", apiVersion);
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		K8SURL url = new K8SURL();
		url.setResource(Resource.ROLEBINDING).setNamespace(namespace).setSubpath(roleName);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys,cluster);
		return response;
	}

	
	
	/**
	 * 判断该username是否在subjects中
	 * 如果username在subjects返回true,否则返回false
	 * @param subjects
	 * @param username
	 * @return
	 */
	private boolean isInSubjects(List<Subjects> subjects,String username) throws Exception{
		if (subjects != null && subjects.size() > 0){
			for (Subjects subject : subjects) {
				if(subject != null && subject.getName().equals(username)){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 根据角色查询ClusterRole明细
	 * @param roleName 角色名称  eg:pm
	 * @return ClusterRole对象
	 */
	public K8SClientResponse listClusterRole(String roleName,Cluster cluster) throws Exception{
		K8SURL url = new K8SURL();
		url.setResource(Resource.CLUSTERROLE).setSubpath(roleName);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,cluster);
		return response;
	}


	/**
	 * 根据标签获取非harbor的Rolebinding列表
	 *
	 * @param lable
	 *            eg:nephele_tenant_yj=yj
	 * @param isHarbor
	 *            是否查询harbor的Rolebinding,
	 *            "0"查询非harbor的Rolebinding,
	 *            "1"查询harbor的Rolebinding,
	 *            "2"查询所有的Rolebinding
	 * @return RoleBindingList
	 */
	public List<RoleBinding> getRolebindingListbyLabelSelectorExceptHarbor(String lable, String isHarbor) {
		Map<String, Object> bodys = new HashMap<>();
		bodys.put("labelSelector", lable);
		K8SURL url = new K8SURL();
		url.setResource(Resource.ROLEBINDING);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys);
		// 删除harbor的rolebinding
		RoleBindingList roleBindingList = K8SClient.converToBean(response, RoleBindingList.class);
		List<RoleBinding> roleBindings = roleBindingList.getItems();
		List<RoleBinding> saveRoleBindings = new ArrayList<>();
		if (isHarbor.equals("0")) {
			for (RoleBinding roleBinding : roleBindings) {
				if (roleBinding.getMetadata().getName().startsWith("harbor")) {
					saveRoleBindings.add(roleBinding);
				}
			}
		}
		else if(isHarbor.equals("1")){
			for (RoleBinding roleBinding : roleBindings) {
				if (roleBinding.getMetadata().getName().startsWith("harbor") == false) {
					saveRoleBindings.add(roleBinding);
				}
			}
		}
		else if(isHarbor.equals("2")){
			return roleBindingList.getItems();
		}else{
			throw new RuntimeException("isHarbor 只能为0，1，2");
		}
		return saveRoleBindings;
	}
	//多集群使用
	public K8SClientResponse deleteRolebings(String namespace, String name,Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.ROLEBINDING).setName(name).setNamespace(namespace);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null,cluster);
		return response;
	}
	public K8SClientResponse deleteRolebings(String namespace, String name) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.ROLEBINDING).setName(name).setNamespace(namespace);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null);
		return response;
	}
}
