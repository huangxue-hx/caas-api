package com.harmonycloud.service.platform.serviceImpl.harbor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.harmonycloud.service.platform.bean.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.ObjectReference;
import com.harmonycloud.k8s.bean.RoleBinding;
import com.harmonycloud.k8s.bean.RoleBindingList;
import com.harmonycloud.k8s.bean.Subjects;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.service.ClusterRoleService;
import com.harmonycloud.k8s.service.RoleBindingService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.harbor.HarborMemberService;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborSecurityService;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import com.harmonycloud.service.tenant.TenantService;

@Service
public class HarborProjectServiceImpl implements HarborProjectService {

	@Autowired
	HarborService hbService;

	@Autowired
	private RoleBindingService roleBindingService;

	@Autowired
	private ClusterRoleService clusterRoleService;

	@Autowired
	private HarborMemberService harborMemberService;

	@Autowired
	private HarborSecurityService harborSecurityService;
	
	@Autowired
	private TenantService tenantService;

	/**
	 * 获取该namespace下用户的harbor project
	 */
	@Override
	public ActionReturnUtil getAllImageOfUser(String namespace, String username) throws Exception {

		ActionReturnUtil projects = hbService.projectList(null, null);
		System.out.println(projects);
		List<HarborProjectDetail> harborProjectList = new ArrayList<>();
		List<String> harborProjectArr = new ArrayList<>();
		String tenantName = "";
		String tenantId = "";
		List<ProjectDto> res = new ArrayList<>();
		if (projects != null && projects.get("data") != null) {
			String result = projects.get("data").toString();
			String newRes = result.replaceAll("public", "isPublic");
			String newRes2 = newRes.replaceAll("Togglable", "togglable");
			harborProjectList = JsonUtil.jsonToList(newRes2, HarborProjectDetail.class);
		}

		String label = "nephele_user_" + username + "=" + username;
		List<TenantHarborDetail> TenantHarborList = this.getAllByUser(namespace, label);

		for (TenantHarborDetail detail : TenantHarborList) {
			if (detail.getImageVerb() != null) {
				harborProjectArr.add(detail.getHarborProjectId());
				tenantName = detail.getTenantName();
				tenantId = detail.getTenantId();
			}

		}
		for (HarborProjectDetail harborProjectDetail : harborProjectList) {
			for (String str : harborProjectArr) {
				if (str.equals(harborProjectDetail.getProject_id())) {
					ProjectDto projectDto = new ProjectDto();
					projectDto.setName(harborProjectDetail.getName());
					projectDto.setHarborid(harborProjectDetail.getProject_id());
					projectDto.setTime(harborProjectDetail.getCreation_time());
					ProjectDto.Tenant tenant = projectDto.new Tenant();
					tenant.setName(tenantName);
					tenant.setTenantId(tenantId);
					projectDto.setTenant(tenant);
					res.add(projectDto);
				}
			}
		}
		return ActionReturnUtil.returnSuccessWithData(res);
	}

	// 查询该用户下的project
	private List<TenantHarborDetail> getAllByUser(String namespace, String lable) throws Exception {
		K8SClientResponse rolebindingList = roleBindingService.getRolebindingInNamespacebyLabelSelector(namespace,
				lable);
		RoleBindingList bindingList = K8SClient.converToBean(rolebindingList, RoleBindingList.class);
		List<RoleBinding> items = bindingList.getItems();
		return this.getRolebindingRes(items);
	}

	// 处理返回结果
	private List<TenantHarborDetail> getRolebindingRes(List<RoleBinding> items) {
		List<TenantHarborDetail> tenantHarborDetails = new ArrayList<>();
		for (RoleBinding item : items) {
			TenantHarborDetail tenantHarborDetail = new TenantHarborDetail();
			tenantHarborDetail.setName(item.getMetadata().getName());
			tenantHarborDetail.setNamespace(item.getMetadata().getNamespace());
			if (item.getMetadata().getAnnotations() != null
					&& item.getMetadata().getAnnotations().get("project") != null
					&& StringUtils.isNotBlank(item.getMetadata().getAnnotations().get("project").toString())) {
				if (StringUtils.isNotBlank(item.getMetadata().getAnnotations().get("userId").toString())) {
					tenantHarborDetail.setUserId(item.getMetadata().getAnnotations().get("userId").toString());
				}
				if (StringUtils.isNotBlank(item.getMetadata().getAnnotations().get("verbs").toString())) {
					tenantHarborDetail.setImageVerb(item.getMetadata().getAnnotations().get("verbs").toString());
				}
				if (StringUtils.isNotBlank(item.getMetadata().getAnnotations().get("project").toString())) {
					tenantHarborDetail
							.setHarborProjectId(item.getMetadata().getAnnotations().get("project").toString());
				}
				tenantHarborDetail.setRole(item.getRoleRef().getName());
				tenantHarborDetail.setTime(item.getMetadata().getCreationTimestamp());
				Map<String, Object> labels = item.getMetadata().getLabels();
				Set<Entry<String, Object>> entrySet = labels.entrySet();
				for (Entry<String, Object> entry : entrySet) {
					if (entry.getKey().indexOf("nephele_tenant_") >= 0) {
						tenantHarborDetail.setTenantName(entry.getValue().toString());
					}
					if (entry.getKey().indexOf("nephele_tenantid_") >= 0) {
						tenantHarborDetail.setTenantId(entry.getValue().toString());
					}
				}
				List<String> users = new ArrayList<>();
				List<Subjects> subjects = item.getSubjects();
				for (Subjects subject : subjects) {
					users.add(subject.getName());
				}
				tenantHarborDetail.setType("RoleBinding");
				tenantHarborDetail.setUser(users);
				tenantHarborDetails.add(tenantHarborDetail);
			}
		}
		return tenantHarborDetails;
	}

	/**
	 * HarborRole权限列表
	 */
	@Override
	public ActionReturnUtil listHarborRole() throws Exception {
		String lable = "nephele_image=image";
		List<HarborRoleDto> harborRoles = new ArrayList<HarborRoleDto>();
		K8SClientResponse response = this.clusterRoleService.getClusterRoleListbyLabelSelector(lable);
		if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			RoleBindingList roleBindingList = K8SClient.converToBean(response, RoleBindingList.class);
			List<RoleBinding> items = roleBindingList.getItems();
			for (RoleBinding roleBinding : items) {
				if (Integer.valueOf(roleBinding.getMetadata().getAnnotations().get("roleIndex").toString()) == 1000) {
					HarborRoleDto harborRoleDto = new HarborRoleDto();
					harborRoleDto.setIndex("1000");
					harborRoleDto.setName(roleBinding.getMetadata().getLabels().get("nephele_roleName").toString());
					harborRoleDto.setTime(roleBinding.getMetadata().getCreationTimestamp());
					harborRoleDto.setType("ClusterRole");
					harborRoles.add(harborRoleDto);
				}
			}
		}
		return ActionReturnUtil.returnErrorWithData(harborRoles);
	}

	/**
	 * 绑定用户到Harbor上
	 */
	@Override
	public ActionReturnUtil bindingHarborUser(HarborUserBinding harborUserBinding) throws Exception {
		// TODO 如果有则增加用户
		List<HarborBindingProject> projects = harborUserBinding.getProjects();
		//获取集群
		Cluster cluster = this.tenantService.getClusterByTenantid(harborUserBinding.getTenantid());
		// 由于只传一个project，可直接获取第一个对象
		HarborBindingProject project = projects.get(0);
		Map<String, Object> bodys = new HashMap<>();
		Map<String, Object> header = new HashMap<>();
		header.put("Content-type", "application/json");

		// 设置metadata
		ObjectMeta metadata = new ObjectMeta();
		Map<String, Object> annotations = new HashMap<>();
		Map<String, Object> labels = new HashMap<>();
		labels.put("nephele_tenant_" + harborUserBinding.getTenantname(), harborUserBinding.getTenantname());
		labels.put("nephele_tenantid_" + harborUserBinding.getTenantid(), harborUserBinding.getTenantid());
		labels.put("nephele_user_" + harborUserBinding.getUser().getName(), harborUserBinding.getUser().getName());
		annotations.put("project", project.getProjectId());
		annotations.put("userId", harborUserBinding.getUser().getId());
		if (project.getRole().equals(Constant.HARBORPROJECTROLE_DEV)) {
			annotations.put("verbs", "2");
		} else {
			annotations.put("verbs", "3");
		}
		metadata.setAnnotations(annotations);
		metadata.setLabels(labels);
		metadata.setName(
				project.getRole() + "_" + project.getProjectId() + "_" + harborUserBinding.getUser().getId() + "_rb");

		// 设置roleRef
		ObjectReference roleRef = new ObjectReference();
		roleRef.setKind("ClusterRole");
		roleRef.setName(project.getRole());

		// 设置subjects
		Subjects subjects = new Subjects();
		subjects.setKind("User");
		subjects.setName(harborUserBinding.getUser().getName());
		List<Subjects> list = new ArrayList<>();
		list.add(subjects);
		bodys.put("metadata", metadata);
		bodys.put("roleRef", roleRef);
		bodys.put("subjects", list);

		K8SClientResponse response = this.roleBindingService.create(harborUserBinding.getNamespace(), header, bodys,cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		String username = harborUserBinding.getUser().getName();
		Map<String, Integer> highestRole = getHighestImageRole(username, project.getProjectId());
		String role = project.getRole();
		List<Integer> roleType = new ArrayList<Integer>();
		if (role.equals(Constant.HARBORPROJECTROLE_DEV)) {
			roleType.add(2);
		}
		if (role.equals(Constant.HARBORPROJECTROLE_WATCHER)) {
			roleType.add(3);
		}
		HarborRole harborRole = new HarborRole();
		harborRole.setUsername(username);
		if (highestRole.get("roleLength") == 1 || highestRole.get("roleLength") == 0) {
			harborRole.setRoleList(roleType);
			return harborMemberService.createRole(Integer.valueOf(project.getProjectId()), harborRole);
		} else {
			roleType.add(highestRole.get("imageVerb"));
			harborRole.setRoleList(roleType);

			// 更新role
			return harborMemberService.updateRole(Integer.valueOf(project.getProjectId()),
					Integer.valueOf(harborUserBinding.getUser().getId()), harborRole);
		}
	}

	@Override
	public ActionReturnUtil deleteHarborUser(HarborUserBinding harborUserBinding) throws Exception {
		int flag = 0;
		List<HarborBindingProject> projects = harborUserBinding.getProjects();
		HarborBindingProject project = projects.get(0);
		String name = project.getRole() + "_" + project.getProjectId() + "_" + harborUserBinding.getUser().getId()
				+ "_rb";
		K8SClientResponse response = roleBindingService.deleteRolebings(harborUserBinding.getNamespace(), name);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		flag++;
		if (flag >= projects.size()) {
			String username = harborUserBinding.getUser().getName();
			Map<String, Integer> highestRole = getHighestImageRole(username, project.getProjectId());
			String role = project.getRole();
			List<Integer> roleType = new ArrayList<Integer>();
			if (role.equals(Constant.HARBORPROJECTROLE_DEV)) {
				roleType.add(2);
			}
			if (role.equals(Constant.HARBORPROJECTROLE_WATCHER)) {
				roleType.add(3);
			}
			HarborRole harborRole = new HarborRole();
			harborRole.setUsername(username);
			if (highestRole.get("roleLength") == 0) {
				return harborMemberService.deleteRole(Integer.valueOf(project.getProjectId()),
						Integer.valueOf(harborUserBinding.getUser().getId()));
			} else {
				roleType.add(highestRole.get("imageVerb"));
				harborRole.setRoleList(roleType);

				// 更新role
				return harborMemberService.updateRole(Integer.valueOf(project.getProjectId()),
						Integer.valueOf(harborUserBinding.getUser().getId()), harborRole);
			}
		}
		return ActionReturnUtil.returnError();
	}

	/**
	 * 根据用户获取rolebing的最高权限
	 *
	 * @return
	 * @throws Exception
	 */
	private Map<String, Integer> getHighestImageRole(String userName, String projectId) throws Exception {
		Map<String, Integer> role = new HashMap<String, Integer>();
		role.put("imageVerb", 100);
		role.put("roleLength", 0);
		List<TenantHarborDetail> imageDetails = getAllByUser(null, "nephele_user_" + userName + "=" + userName);
		int i = 0;
		for (TenantHarborDetail detail : imageDetails) {
			if (!StringUtils.isEmpty(detail.getImageVerb())) {
				if (detail.getHarborProjectId().equals(projectId)) {
					if (role.get("imageVerb") > Integer.valueOf(detail.getImageVerb())) {
						role.put("imageVerb", Integer.valueOf(detail.getImageVerb()));
						i++;
					}
				}
			}
		}
		role.put("roleLength", i);
		return role;
	}

	@Override
	public ActionReturnUtil getStatistcsByNamespace(String namespace) throws Exception {
		K8SClientResponse response = roleBindingService.listRolebindingsByNamespace(namespace);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnError();
		}
		RoleBindingList roleBindingList = JsonUtil.jsonToPojo(response.getBody(), RoleBindingList.class);
		List<RoleBinding> items = roleBindingList.getItems();
		List<TenantHarborDetail> roles = getRolebindingRes(items);
		List<TenantHarborDetail> imageUser = new ArrayList<TenantHarborDetail>();
		List<String> tmpUser = new ArrayList<String>();
		for (TenantHarborDetail detail : roles) {
			if (!StringUtils.isEmpty(detail.getImageVerb())) {
				imageUser.add(detail);
				for (String name : detail.getUser()) {
					tmpUser.add(name);
				}
			}
		}
		Map<String, String> uniqueUser = new HashMap<String, String>();
		if (tmpUser != null && tmpUser.size() > 0) {
			for (String user : tmpUser) {
				if (!uniqueUser.containsKey(user)) {
					uniqueUser.put(user, user);
				}
			}

			List<Map<String, Object>> statistics = new ArrayList<Map<String, Object>>();
			for (Map.Entry<String, String> tmp : uniqueUser.entrySet()) {
				ActionReturnUtil securitys = harborSecurityService.clairStatistcs(Constant.FLAGNAME, tmp.getValue());
				Map<String, Object> res = new HashMap<String, Object>();
				res.put("name", tmp.getKey());
				res.put("data", JsonUtil.convertJsonToMap(securitys.get("data").toString()));
				statistics.add(res);
			}
			return ActionReturnUtil.returnSuccessWithData(statistics);
		}

		return ActionReturnUtil.returnSuccessWithData(imageUser);
	}


	@Override
	public ActionReturnUtil bindingUserProjects(ProjectUserBinding projectUserBinding)throws Exception {
		List<String> projects = projectUserBinding.getProjects();
		ActionReturnUtil returnUil = ActionReturnUtil.returnSuccess();
		// 获取对象
		for (int i = 0; i < projects.size(); i++) {
			String projectID = projects.get(i);
			String username = projectUserBinding.getUserName();
			returnUil = bindingProjectUser(username,projectID);
			/*
			Map<String, Object> bodys = new HashMap<>();
			Map<String, Object> header = new HashMap<>();
			header.put("Content-type", "application/json");

			String username = projectUserBinding.getUserName();

			String role = "harbor_project_admin";
			List<Integer> roleType = new ArrayList<Integer>();
			if (role.equals(Constant.HARBORPROJECTROLE_ADMIN)) {
				roleType.add(1);
			}
			HarborRole harborRole = new HarborRole();
			harborRole.setUsername(username);
			harborRole.setRoleList(roleType);
			final ActionReturnUtil role1 = harborMemberService.createRole(Integer.valueOf(projectID), harborRole);
			if((boolean)role1.get("success")==false){
				return role1;
			}
			*/
		}
		//return ActionReturnUtil.returnSuccess();
		return returnUil;
	}

	@Override
	public ActionReturnUtil bindingProjectUsers(UserProjectBiding userProjectBinding)throws Exception {
		// TODO 如果有则增加用户
		List<String> users = userProjectBinding.getUserNames();
		ActionReturnUtil returnUil = ActionReturnUtil.returnSuccess();
		// 获取对象
		for (int i = 0; i < users.size(); i++) {
			String username = users.get(i);
			String projectID = userProjectBinding.getProject();
			returnUil = bindingProjectUser(username,projectID);
			/*
			Map<String, Object> bodys = new HashMap<>();
			Map<String, Object> header = new HashMap<>();
			header.put("Content-type", "application/json");

			String projectID = userProjectBinding.getProject();

			String role = "harbor_project_admin";
			List<Integer> roleType = new ArrayList<Integer>();
			if (role.equals(Constant.HARBORPROJECTROLE_ADMIN)) {
				roleType.add(1);
			}
			HarborRole harborRole = new HarborRole();
			harborRole.setUsername(username);
			harborRole.setRoleList(roleType);
			ActionReturnUtil role1=harborMemberService.createRole(Integer.valueOf(projectID), harborRole);
			if((boolean)role1.get("success")==false){
				returnUil=role1;
			}
			*/
		}
		return returnUil;
	}
	//绑定用户到harbor的仓库project
	public ActionReturnUtil bindingProjectUser(String username,String projectID) throws Exception{
		ActionReturnUtil returnUil = ActionReturnUtil.returnSuccess();
		Map<String, Object> bodys = new HashMap<>();
		Map<String, Object> header = new HashMap<>();
		header.put("Content-type", "application/json");
		String role = "harbor_project_admin";
		List<Integer> roleType = new ArrayList<Integer>();
		if (role.equals(Constant.HARBORPROJECTROLE_ADMIN)) {
			roleType.add(1);
		}
		HarborRole harborRole = new HarborRole();
		harborRole.setUsername(username);
		harborRole.setRoleList(roleType);
		ActionReturnUtil role1= harborMemberService.createRole(Integer.valueOf(projectID), harborRole);
		if((boolean)role1.get("success")==false){
			returnUil=role1;
		}
		return returnUil;
	}

}
