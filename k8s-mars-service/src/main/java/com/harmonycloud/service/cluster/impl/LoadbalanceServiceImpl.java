package com.harmonycloud.service.cluster.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CsvUtil;
import com.harmonycloud.common.util.HttpClientResponse;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dao.cluster.bean.ClusterLoadbalance;
import com.harmonycloud.dao.cluster.bean.ClusterLoadbalanceExample;
import com.harmonycloud.dao.tenant.TenantBindingMapper;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantBindingExample;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.cluster.LoadbalanceService;
import com.harmonycloud.service.platform.bean.PodDetail;

@Service
public class LoadbalanceServiceImpl implements LoadbalanceService {

	@Autowired
	private TenantBindingMapper tenantBindingMapper;

	@Autowired
	DeploymentService dpService;

	@Autowired
	private DeploymentsService depsService;

	@Autowired
	private NamespaceLocalService namespaceLocalService;

	@SuppressWarnings("unchecked")
	@Override
	public ActionReturnUtil getStatsByService(String app, String namespace) throws Exception {

		// 从Namespace名称中获取clusterId
		TenantBindingExample example = new TenantBindingExample();
		String tenant = null;
		if (namespace.lastIndexOf("-") > -1) {
			tenant = namespace.substring(0, namespace.lastIndexOf("-"));
		} else {
			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.FORMAT_ERROR, DictEnum.NAMESPACE.phrase(),true);
		}
		example.createCriteria().andTenantNameEqualTo(tenant);
		List<TenantBinding> tenantBindings = tenantBindingMapper.selectByExample(example);
		if (tenantBindings == null) {
			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NOT_FOUND, DictEnum.NAMESPACE.phrase(),true);
		}

		// 查询集群
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
		ActionReturnUtil podList = depsService.podList(app, namespace);

		Integer podCount = 0;
		if (!(boolean) podList.get("success")) {
			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NOT_FOUND, DictEnum.POD.phrase(),true);
		}

		List<PodDetail> pods = (List<PodDetail>) podList.get("data");
		podCount = pods.size();
		if (podCount > 0) {
			Integer tps = 0;
			Integer sessions = 0;
			Map<String, Integer> resMap = new HashMap<String, Integer>();
			
			// 从数据库中查询负载均衡的地址
			String slbUrl = null;
			ClusterLoadbalanceExample lbExample = new ClusterLoadbalanceExample();
			lbExample.createCriteria().andClusterIdEqualTo(cluster.getId());
			//原先haproxy的接口调用统计信息，现在不用haproxy负载均衡了
			//List<ClusterLoadbalance> clusterLoadbalances = clusterLbMapper.selectByExample(lbExample);
			List<ClusterLoadbalance> clusterLoadbalances = null;
			if (clusterLoadbalances != null && !clusterLoadbalances.isEmpty()) {
				
				//多个haproxy，需查询每一个
				for (ClusterLoadbalance lb : clusterLoadbalances) { 
					slbUrl = "http://" + lb.getLoadbalanceIp() + ":" + lb.getLoadbalancePort();
					String url = slbUrl + "/stats;csv;norefresh";
					HttpClientResponse response = HttpClientUtil.doGet(url, null, null);
					if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
						String res = response.getBody();
						res = res.substring(res.indexOf("#") + 2);
						String[] content = res.split("\n");
						CsvUtil csvUtil = new CsvUtil(content);
						int row = csvUtil.getRowNum();
						csvUtil.getContent();
						
						// 对pods循环，根据podIp来筛选
						for (PodDetail podDetail : pods) {
							for (int i = 1; i < row; i++) {
								Map<String, Object> tMap = csvUtil.rowToJson(i);
								String svName = tMap.get("svname").toString();
								if (svName.indexOf(":") > -1) {
									String ip = svName.substring(0, svName.indexOf(":"));
									if (ip.equals(podDetail.getIp())) {
										tps = tps + Integer.valueOf((String) tMap.get("rate"));
										sessions = sessions + Integer.valueOf((String) tMap.get("stot"));
									}
								}
							}
						}
					} else {
						return ActionReturnUtil.returnErrorWithMsg(response.getBody());
					}
				}
				resMap.put("tps", tps/podCount);
				resMap.put("sessions", sessions/podCount);
				return ActionReturnUtil.returnSuccessWithData(resMap);
			}
		}

		return ActionReturnUtil.returnError();
	}
}
