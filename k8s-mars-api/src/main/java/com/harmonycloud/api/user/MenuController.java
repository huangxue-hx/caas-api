//package com.harmonycloud.api.user;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import com.harmonycloud.common.util.ActionReturnUtil;
//import com.harmonycloud.common.util.HttpStatusUtil;
//import com.harmonycloud.k8s.bean.ClusterRole;
//import com.harmonycloud.k8s.bean.ClusterRoleList;
//import com.harmonycloud.k8s.bean.RoleBinding;
//import com.harmonycloud.k8s.bean.RoleBindingList;
//import com.harmonycloud.k8s.client.K8SClient;
//import com.harmonycloud.k8s.service.ClusterRoleService;
//import com.harmonycloud.k8s.service.RoleBindingService;
//import com.harmonycloud.k8s.util.K8SClientResponse;
//import com.harmonycloud.service.user.MenuService;
//
//@Controller
//public class MenuController {
//
//	@Autowired
//	private MenuService menuService;
//	@Autowired
//	private RoleBindingService roleBindingService;
//	@Autowired
//	private ClusterRoleService clusterRoleService;
//
//	/**
//	 * 用户登录时,根据用户的权限显示相应的列表
//	 * 
//	 * @throws Exception
//	 */
//	@ResponseBody
//	@RequestMapping(value = "/getMenu", method = RequestMethod.GET)
//	public ActionReturnUtil listMenu(HttpServletRequest request,
//			@RequestParam(value = "namespace", required = false) final String namespace) throws Exception{
//		try {
//			HttpSession session = request.getSession();
//			// 根据用户名,查询用户角色
//			String userName = String.valueOf(session.getAttribute("username"));
//			if (userName.equals("admin")) {
//				ActionReturnUtil menuList = menuService.menuList("admin");
//				return menuList;
//			}else if(StringUtils.isBlank(namespace)){
//				return ActionReturnUtil.returnSuccess();
//			}else {
//				String label = "nephele_user_" + userName + "=" + userName;
//				// 根据namespace查询当前用户角色
//				K8SClientResponse response = roleBindingService.getRolebindingInNamespacebyLabelSelector(namespace,
//						label);
//				if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
//					RoleBindingList roleBindingList = K8SClient.converToBean(response, RoleBindingList.class);
//					List<RoleBinding> items = roleBindingList.getItems();
//					List<String> roles = new ArrayList<>();
//					for (RoleBinding item : items) {
//						// 将该用户所有角色放入角色列表
//						if (item.getRoleRef().getName().startsWith("harbor") == false) {
//							roles.add(item.getRoleRef().getName());
//						}
//					}
//					// 如果用户当前namespace下的角色大于2,选出权重最大的角色
//					Map<Integer, String> roleIndex = new HashMap<>();
//					List<Integer> indexs = new ArrayList<>();
//					K8SClientResponse result = clusterRoleService.listClusterRoles();
//					if (HttpStatusUtil.isSuccessStatus(result.getStatus())) {
//						ClusterRoleList clusterRoleList = K8SClient.converToBean(result, ClusterRoleList.class);
//						List<ClusterRole> ClusterRoleItems = clusterRoleList.getItems();
//						for (ClusterRole clusterRole : ClusterRoleItems) {
//							for (String role : roles) {
//								if (clusterRole.getMetadata().getName().equals(role)) {
//									roleIndex.put(Integer.valueOf(
//											clusterRole.getMetadata().getAnnotations().get("roleIndex").toString()),
//											role);
//									indexs.add(Integer.valueOf(
//											clusterRole.getMetadata().getAnnotations().get("roleIndex").toString()));
//								}
//							}
//						}
//						// 对roleIndex处理,找出权重最大角色
//						Collections.sort(indexs);
//						Integer index = indexs.get(indexs.size() - 1);
//						// 权重最大角色
//						String role = roleIndex.get(index);
//						return menuService.menuList(role);
//					}
//				}
//			}
//			return ActionReturnUtil.returnError();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return ActionReturnUtil.returnError();
//		}
//
//	}
//
//}
