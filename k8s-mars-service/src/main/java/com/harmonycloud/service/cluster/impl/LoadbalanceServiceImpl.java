package com.harmonycloud.service.cluster.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CsvUtil;
import com.harmonycloud.common.util.HttpClientResponse;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.dao.cluster.ClusterLoadbalanceMapper;
import com.harmonycloud.dao.cluster.ClusterMapper;
import com.harmonycloud.dao.cluster.bean.Cluster;
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
	private ClusterMapper clusterMapper;

	@Autowired
	private TenantBindingMapper tenantBindingMapper;

	@Autowired
	DeploymentService dpService;

	@Autowired
	private DeploymentsService depsService;

	@Autowired
	private ClusterLoadbalanceMapper clusterLbMapper;

	@SuppressWarnings("unchecked")
	@Override
	public ActionReturnUtil getStatsByService(String app, String namespace) throws Exception {

		// 从Namespace名称中获取clusterId
		TenantBindingExample example = new TenantBindingExample();
		String tenant = null;
		if (namespace.lastIndexOf("-") > -1) {
			tenant = namespace.substring(0, namespace.lastIndexOf("-"));
		} else {
			return ActionReturnUtil.returnErrorWithMsg("namespace名称不合法");
		}
		example.createCriteria().andTenantNameEqualTo(tenant);
		List<TenantBinding> tenantBindings = tenantBindingMapper.selectByExample(example);
		if (tenantBindings == null) {
			return ActionReturnUtil.returnErrorWithMsg("查询的租户没有所属的集群");
		}
		Integer clusterId = tenantBindings.get(0).getClusterId();

		// 查询集群
		Cluster cluster = clusterMapper.findClusterById(String.valueOf(clusterId));

		ActionReturnUtil podList = depsService.podList(app, namespace, cluster);

		Integer podCount = 0;
		if (!(boolean) podList.get("success")) {
			return ActionReturnUtil.returnErrorWithMsg("未查询到Pod");
		}

		List<PodDetail> pods = (List<PodDetail>) podList.get("data");
		podCount = pods.size();
		if (podCount > 0) {
			// 从数据库中查询负载均衡的地址
			String slbUrl = null;
			ClusterLoadbalanceExample lbExample = new ClusterLoadbalanceExample();
			lbExample.createCriteria().andClusterIdEqualTo(clusterId);
			List<ClusterLoadbalance> clusterLoadbalances = clusterLbMapper.selectByExample(lbExample);
			if (clusterLoadbalances != null && !clusterLoadbalances.isEmpty()) {
				slbUrl = "http://" + clusterLoadbalances.get(0).getLoadbalanceIp() + ":"
						+ clusterLoadbalances.get(0).getLoadbalancePort();
				String url = slbUrl + "/stats;csv;norefresh";
				HttpClientResponse response = HttpClientUtil.doGet(url, null, null);
				if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
					String res = response.getBody();
					res = res.substring(res.indexOf("#") + 2);
					String[] content = res.split("\n");
					CsvUtil csvUtil = new CsvUtil(content);
					int row = csvUtil.getRowNum();
					csvUtil.getContent();
					Map<String, Integer> resMap = new HashMap<String, Integer>();
					Integer tps = 0;
					Integer sessions = 0;
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
					resMap.put("tps", tps/podCount);
					resMap.put("sessions", sessions/podCount);
					return ActionReturnUtil.returnSuccessWithData(resMap);
				} else {
					return ActionReturnUtil.returnErrorWithMsg(response.getBody());
				}
			}
		}

		return ActionReturnUtil.returnError();
	}
}
