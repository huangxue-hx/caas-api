package com.harmonycloud.service.application.impl;

import com.alibaba.druid.stat.TableStat.Name;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.application.NodePortClusterMapper;
import com.harmonycloud.dao.application.NodePortMapper;
import com.harmonycloud.dao.application.bean.NodePortCluster;
import com.harmonycloud.dao.application.bean.NodePortClusterExample;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.*;
import com.harmonycloud.dto.svc.SvcTcpDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.ServicesService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.platform.bean.*;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.TenantService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by czm on 2017/1/18. jmi 补充
 */
@Service
public class RouterServiceImpl implements RouterService {

	@Autowired
	private ServicesService sService;

	@Autowired
	private TenantService tService;

	@Autowired
	private NodePortMapper nodePortMapper;

	@Autowired
	private NodePortClusterMapper npcMapper;

	@Autowired
	HttpSession session;

	@Value("#{propertiesReader['clusterHost.hostname']}")
	private String hostName;

	@Value("#{propertiesReader['kube.haProxyVersion']}")
	private String haProxyVersion;

	/**
	 * 查询router列表
	 *
	 * @param namespace
	 * @return
	 */
	@Override
	public List<ParsedIngressListDto> ingList(String namespace) throws Exception {

		// 找到URL进行调用
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.INGRESS);// 资源类型怎么判断
		K8SClientResponse k = new K8sMachineClient().exec(url, HTTPMethod.GET, /* "330957b867a3462ea457bec41410624b", */null,
				null, null);
		IngressList ingressList = K8SClient.converToBean(k, IngressList.class);

		List<ParsedIngressListDto> parsedIngressLists = new ArrayList<ParsedIngressListDto>();

		if (ingressList != null) {
			for (Ingress ingress : ingressList.getItems()) {
				ParsedIngressListDto parsedIngressList = new ParsedIngressListDto();
				List<HttpRuleDto> rules = new ArrayList<>();

				ObjectMeta metadata = ingress.getMetadata();
				parsedIngressList.setNamespace(metadata.getNamespace());
				parsedIngressList.setName(metadata.getName());
				parsedIngressList.setLabels(metadata.getLabels());
				parsedIngressList.setCreateTime(metadata.getCreationTimestamp());
				parsedIngressList.setHost(ingress.getSpec().getRules().get(0).getHost());

				if (metadata.getAnnotations() != null) {
					parsedIngressList.setAnnotaion(metadata.getAnnotations().get("nephele/annotation"));
				}

				List<IngressRule> specRules = ingress.getSpec().getRules();
				for (IngressRule ingressRule : specRules) {
					HttpRuleDto rule = new HttpRuleDto();
					rule.setPath(ingressRule.getHttp().getPaths().get(0).getPath());
					rule.setService(ingressRule.getHttp().getPaths().get(0).getBackend().getServiceName());
					rule.setPort(String.valueOf(ingressRule.getHttp().getPaths().get(0).getBackend().getServicePort()));
					rules.add(rule);
					parsedIngressList.setRules(rules);
				}
				parsedIngressLists.add(parsedIngressList);
			}
		}

		return parsedIngressLists;
	}

	/**
	 * 创建router
	 *
	 * @param parsedIngressList
	 * @return
	 */
	@Override
	public ActionReturnUtil ingCreate(ParsedIngressListDto parsedIngressList) throws Exception {
		Map<String, Object> body = new HashMap<String, Object>();
		Ingress ingress = new Ingress();
		ingress.setMetadata(new ObjectMeta());
		ingress.setSpec(new IngressSpec());

		Map<String, Object> anno = new HashMap<String, Object>();
		if (parsedIngressList.getAnnotaion() != null) {
			anno.put("nephele/annotation", parsedIngressList.getAnnotaion());
			ingress.getMetadata().setAnnotations(anno);
		}
		ingress.getMetadata().setLabels(parsedIngressList.getLabels());
		ingress.getMetadata().setNamespace(parsedIngressList.getNamespace());
		List<HttpRuleDto> rules = parsedIngressList.getRules();

		List<HTTPIngressPath> path = new ArrayList<HTTPIngressPath>();

		if (rules != null && !rules.equals("")) {
			for (HttpRuleDto rule : rules) {

				HTTPIngressPath p = new HTTPIngressPath();
				IngressBackend backend = new IngressBackend();
				backend.setServiceName(rule.getService());
				backend.setServicePort(Integer.valueOf(rule.getPort()));
				p.setBackend(backend);
				p.setPath(rule.getPath());
				path.add(p);
			}
		}

		IngressRule ingressRule = new IngressRule();
		ingressRule.setHost(parsedIngressList.getHost());
		HTTPIngressRuleValue http = new HTTPIngressRuleValue();
		http.setPaths(path);
		ingressRule.setHttp(http);
		ingress.getSpec().setRules(new ArrayList<IngressRule>());
		ingress.getSpec().getRules().add(ingressRule);

		ingress.getMetadata().setName(parsedIngressList.getName());
		body = CollectionUtil.transBean2Map(ingress);
		K8SURL url = new K8SURL();
		Map<String, Object> head = new HashMap<String, Object>();
		head.put("Content-Type", "application/json");

		String namespace = parsedIngressList.getNamespace();
		url.setNamespace(namespace).setResource(Resource.INGRESS);
		// String s = JsonUtil.convertToJsonNonNull(body);
		K8SClientResponse k = new K8sMachineClient().exec(url, HTTPMethod.POST, head, body, null);
		if (!HttpStatusUtil.isSuccessStatus(k.getStatus())) {
			UnversionedStatus status = JsonUtil.jsonToPojo(k.getBody(), UnversionedStatus.class);
			return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
		}
		return ActionReturnUtil.returnSuccessWithData(k.getBody());

	}

	/**
	 * 删除HTTP应用网关
	 */
	@Override
	public ActionReturnUtil ingDelete(String namespace, String name) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.INGRESS).setName(name);
		Map<String, Object> head = new HashMap<String, Object>();
		head.put("Content-Type", "application/json");
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, head, null, null);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(response.getBody());
	}

	@Override
	public ActionReturnUtil svcList(String namespace) throws Exception {
		K8SClientResponse response = sService.doServiceByNamespace(namespace, null, null, HTTPMethod.GET);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		ServiceList svList = JsonUtil.jsonToPojo(response.getBody(), ServiceList.class);
		List<com.harmonycloud.k8s.bean.Service> services = svList.getItems();
		List<RouterSvc> routerSvcs = new ArrayList<RouterSvc>();
		if (!services.isEmpty() || services.size() > 0) {
			for (int i = 0; i < services.size(); i++) {
				boolean flag = false;
				Map<String, Object> labels = services.get(i).getMetadata().getLabels();
				if (!labels.isEmpty()) {
					for (Map.Entry<String, Object> m : labels.entrySet()) {
						if (m.getKey().indexOf("nephele") > -1) {
							flag = true;
							break;
						}
					}
				}

				if (flag) {
					RouterSvc routerSvc = new RouterSvc();
					com.harmonycloud.k8s.bean.Service svc = services.get(i);
					routerSvc.setNamespace(svc.getMetadata().getNamespace());
					routerSvc.setName(svc.getMetadata().getName());
					routerSvc.setCreateTime(svc.getMetadata().getCreationTimestamp());
					Map<String, Object> tMap = new HashMap<String, Object>();
					for (Map.Entry<String, Object> m : labels.entrySet()) {
						if (m.getKey().indexOf("nephele") < 0) {
							tMap.put(m.getKey(), m.getValue());
						}
					}
					routerSvc.setLabels(tMap);
					routerSvc.setSelector(svc.getSpec().getSelector());
					routerSvc.setRules(svc.getSpec().getPorts());
					Map<String, Object> anno = svc.getMetadata().getAnnotations();
					if (anno != null && !anno.isEmpty()) {
						if (anno.containsKey("nephele/annotation")
								&& !StringUtils.isEmpty(anno.get("nephele/annotation").toString())) {
							routerSvc.setAnnotation(anno.get("nephele/annotation").toString());
						}
						if (anno.containsKey("nephele/deployment")
								&& !StringUtils.isEmpty(anno.get("nephele/deployment").toString())) {
							routerSvc.setService(anno.get("nephele/deployment").toString());
						}
					}
					routerSvcs.add(routerSvc);
				}
			}
		}
		return ActionReturnUtil.returnSuccessWithData(routerSvcs);
	}

	@Override
	public ActionReturnUtil listSvcByName(ParsedIngressListDto parsedIngressListDto) throws Exception {
		if (parsedIngressListDto.getNamespace() == null) {
			return ActionReturnUtil.returnErrorWithMsg("namespace cannot be null!");
		}
		if (parsedIngressListDto.getLabels() == null) {
			return ActionReturnUtil.returnErrorWithMsg("labels cannot be null!");
		}
		String namespace = parsedIngressListDto.getNamespace();
		K8SClientResponse response = sService.doServiceByNamespace(namespace, null, null, HTTPMethod.GET);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		ServiceList svList = JsonUtil.jsonToPojo(response.getBody(), ServiceList.class);
		List<com.harmonycloud.k8s.bean.Service> services = svList.getItems();
		List<RouterSvc> routerSvcs = new ArrayList<RouterSvc>();
		if (!services.isEmpty() || services.size() > 0) {
			for (int i = 0; i < services.size(); i++) {
				boolean flag = false;
				Map<String, Object> labels = services.get(i).getMetadata().getLabels();
				if (!labels.isEmpty()) {
					for (Map.Entry<String, Object> m : labels.entrySet()) {
						if (m.getKey().indexOf("nephele") > -1) {
							flag = true;
							break;
						}
					}
				}

				if (flag) {
					RouterSvc routerSvc = new RouterSvc();
					com.harmonycloud.k8s.bean.Service svc = services.get(i);
					if (svc.getSpec().getSelector().equals(parsedIngressListDto.getLabels())) {
						routerSvc.setNamespace(svc.getMetadata().getNamespace());
						routerSvc.setName(svc.getMetadata().getName());
						routerSvc.setCreateTime(svc.getMetadata().getCreationTimestamp());
						Map<String, Object> tMap = new HashMap<String, Object>();
						for (Map.Entry<String, Object> m : labels.entrySet()) {
							/* if (m.getKey().indexOf("nephele") < 0) { */
							tMap.put(m.getKey(), m.getValue());
							/* } */
						}
						routerSvc.setLabels(tMap);
						routerSvc.setSelector(svc.getSpec().getSelector());
						routerSvc.setRules(svc.getSpec().getPorts());
						Map<String, Object> anno = svc.getMetadata().getAnnotations();
						if (anno != null && !anno.isEmpty()) {
							if (anno.containsKey("nephele/annotation")
									&& !StringUtils.isEmpty(anno.get("nephele/annotation").toString())) {
								routerSvc.setAnnotation(anno.get("nephele/annotation").toString());
							}
							if (anno.containsKey("nephele/deployment")
									&& !StringUtils.isEmpty(anno.get("nephele/deployment").toString())) {
								routerSvc.setService(anno.get("nephele/deployment").toString());
							}
						}
						routerSvcs.add(routerSvc);
					}
				}
			}
		}
		return ActionReturnUtil.returnSuccessWithData(routerSvcs);
	}

	@Override
	public ActionReturnUtil svcCreate(SvcRouterDto svcRouter) throws Exception {
		com.harmonycloud.k8s.bean.Service service = new com.harmonycloud.k8s.bean.Service();
		ObjectMeta meta = new ObjectMeta();
		meta.setName("routersvc" + svcRouter.getName());
		Map<String, Object> labels = new HashMap<String, Object>();
		if (svcRouter.getLabels() != null) {
			labels = svcRouter.getLabels();
		}
		labels.put("nephele_Type", "HAP");
		labels.put("type", "TCP");
		meta.setLabels(labels);
		Map<String, Object> anno = new HashMap<String, Object>();
		anno.put("nephele/deployment", svcRouter.getApp());
		if (svcRouter.getAnnotaion() != null) {
			anno.put("nephele/annotation", svcRouter.getAnnotaion());
		}
		meta.setAnnotations(anno);
		ServiceSpec serviceSpec = new ServiceSpec();
		serviceSpec.setType("ClusterIP");
		List<ServicePort> ports = new ArrayList<ServicePort>();
		serviceSpec.setPorts(ports);
		if (svcRouter.getRules() != null && !svcRouter.getRules().isEmpty()) {
			List<TcpRuleDto> rules = svcRouter.getRules();
			List<TcpRuleDto> rulesNew = new ArrayList<>();

			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
			String tenantID = (String) session.getAttribute("tenantId");

			int i = 0;
			for (i = 0; i < rules.size(); i++) {
				TcpRuleDto rule = rules.get(i);
				ServicePort servicePort = new ServicePort();
				servicePort.setName(svcRouter.getName() + "-port" + i);
				servicePort.setProtocol(rule.getProtocol());

				if (rule.getPort() != null) {
					servicePort.setPort(Integer.valueOf(rule.getPort()));
					NodePortCluster npCluster = new NodePortCluster();
					npCluster.setClusterid(Integer.valueOf((cluster.getId().toString())));
					npCluster.setNodeportid(Integer.valueOf(rule.getPort()) - 29999);
					npCluster.setStatus(2);
					npcMapper.insert(npCluster);
					servicePort.setTargetPort(Integer.valueOf(rule.getTargetPort()));
					ports.add(servicePort);
				} else {
					rulesNew.add(rules.get(i));
					// String tenantID = (String)
					// session.getAttribute("tenantId");
					// servicePort.setPort(Integer.valueOf((String)
					// getPort(tenantID).get("msg")));
				}
			}

			for (int j = 0; j < rulesNew.size(); i++, j++) {
				TcpRuleDto rule = rules.get(j);
				ServicePort servicePort = new ServicePort();
				servicePort.setName(svcRouter.getName() + "-port" + i);
				servicePort.setProtocol(rule.getProtocol());
				String port = (String) getPort(tenantID).get("msg");
				servicePort.setPort(Integer.valueOf(port));
				servicePort.setTargetPort(Integer.valueOf(rule.getTargetPort()));
				ports.add(servicePort);
			}

			serviceSpec.setPorts(ports);
		}
		// selector
		if (svcRouter.getSelector() != null && StringUtils.isNotEmpty(svcRouter.getSelector().getApp())) {
			Map<String, Object> selector = new HashMap<String, Object>();
			selector.put("app", svcRouter.getSelector().getApp());
			if (StringUtils.isNotEmpty(svcRouter.getSelector().getName())) {
				selector.put("name", svcRouter.getSelector().getName());
			}
			if (StringUtils.isNotEmpty(svcRouter.getSelector().getValue())) {
				selector.put("value", svcRouter.getSelector().getValue());
			}
			serviceSpec.setSelector(selector);
		}
		// serviceSpec.setSelector(svcRouter.getSelector());
		service.setMetadata(meta);
		service.setSpec(serviceSpec);
		ServiceStatus status = new ServiceStatus();
		status.setLoadBalancer(null);
		service.setStatus(status);
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys = CollectionUtil.transBean2Map(service);
		Map<String, Object> head = new HashMap<String, Object>();
		head.put("Content-Type", "application/json");
		K8SClientResponse response = sService.doServiceByNamespace(svcRouter.getNamespace(), head, bodys,
				HTTPMethod.POST);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			UnversionedStatus sataus = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
			return ActionReturnUtil.returnErrorWithMsg(sataus.getMessage());
		}
		com.harmonycloud.k8s.bean.Service newService = JsonUtil.jsonToPojo(response.getBody(),
				com.harmonycloud.k8s.bean.Service.class);
		return ActionReturnUtil.returnSuccessWithData(newService);
	}

	@Override
	public ActionReturnUtil createTcpSvc(SvcTcpDto svcTcpDto) throws Exception {
		if (svcTcpDto == null) {
			return ActionReturnUtil.returnErrorWithMsg("参数不能为空");
		}
		String tenantId = svcTcpDto.getTenantId();
		// 根据tenantid获取cluster
		Cluster cluster = tService.getClusterByTenantid(tenantId);
		int clusterId = tService.getClusterByTenantid(tenantId).getId().intValue();
		ArrayList<NodePortCluster> lnpc = new ArrayList<NodePortCluster>();
		// 修改nodeport_cluster中间表的status
		for (int i = 0; i < svcTcpDto.getRules().size(); i++) {
			int nodeport = Integer.valueOf(svcTcpDto.getRules().get(i).getPort());
			int nodeportId = nodePortMapper.getidbynodeport(nodeport);
			NodePortCluster npc = new NodePortCluster();
			npc.setClusterid(clusterId);
			npc.setNodeportid(nodeportId);
			npc.setStatus(2);
			lnpc.add(npc);
		}

		com.harmonycloud.k8s.bean.Service service = new com.harmonycloud.k8s.bean.Service();
		ObjectMeta meta = new ObjectMeta();
		meta.setName("routersvc" + svcTcpDto.getName());
		Map<String, Object> labels = new HashMap<String, Object>();
		if (svcTcpDto.getLabels() != null) {
			labels = svcTcpDto.getLabels();
		}
		labels.put("nephele_Type", "HAP");
		labels.put("type", "TCP");
		meta.setLabels(labels);
		Map<String, Object> anno = new HashMap<String, Object>();
		anno.put("nephele/deployment", svcTcpDto.getApp());
		if (svcTcpDto.getAnnotaion() != null) {
			anno.put("nephele/annotation", svcTcpDto.getAnnotaion());
		}
		meta.setAnnotations(anno);
		ServiceSpec serviceSpec = new ServiceSpec();
		serviceSpec.setType("ClusterIP");
		List<ServicePort> ports = new ArrayList<ServicePort>();
		if (svcTcpDto.getRules() != null && !svcTcpDto.getRules().isEmpty()) {
			List<TcpRuleDto> rules = svcTcpDto.getRules();
			for (int i = 0; i < rules.size(); i++) {
				TcpRuleDto rule = rules.get(i);
				ServicePort servicePort = new ServicePort();
				servicePort.setName(svcTcpDto.getName() + "-port" + i);
				servicePort.setProtocol(rule.getProtocol());
				servicePort.setPort(Integer.valueOf(rule.getPort()));
				servicePort.setTargetPort(Integer.valueOf(rule.getTargetPort()));
				ports.add(servicePort);
			}
			serviceSpec.setPorts(ports);
		}
		serviceSpec.setSelector(svcTcpDto.getSelector());
		service.setMetadata(meta);
		service.setSpec(serviceSpec);
		ServiceStatus status = new ServiceStatus();
		status.setLoadBalancer(null);
		service.setStatus(status);
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys = CollectionUtil.transBean2Map(service);
		Map<String, Object> head = new HashMap<String, Object>();
		head.put("Content-Type", "application/json");
		K8SClientResponse response = sService.doServiceByNamespace(svcTcpDto.getNamespace(), head, bodys,
				HTTPMethod.POST, cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			for (int i = 0; i < svcTcpDto.getRules().size(); i++) {
				int nodeport = Integer.valueOf(svcTcpDto.getRules().get(i).getPort());
				int nodeportId = nodePortMapper.getidbynodeport(nodeport);
				NodePortClusterExample example = new NodePortClusterExample();
				// 创建tcp失败释放端口
				example.createCriteria().andClusteridEqualTo(clusterId).andNodeportidEqualTo(nodeportId)
						.andStatusEqualTo(1);
				npcMapper.deleteByExample(example);
			}
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		com.harmonycloud.k8s.bean.Service newService = JsonUtil.jsonToPojo(response.getBody(),
				com.harmonycloud.k8s.bean.Service.class);
		for (int i = 0; i < lnpc.size(); i++) {
			npcMapper.updateNodePortCluster(lnpc.get(i));
		}
		return ActionReturnUtil.returnSuccessWithData(newService);
	}

	@Override
	public ActionReturnUtil createhttpsvc(SvcTcpDto svcTcpDto) throws Exception {
		com.harmonycloud.k8s.bean.Service service = new com.harmonycloud.k8s.bean.Service();
		ObjectMeta meta = new ObjectMeta();
		meta.setName("routersvc" + svcTcpDto.getName());
		Map<String, Object> labels = new HashMap<String, Object>();
		if (svcTcpDto.getLabels() != null) {
			labels = svcTcpDto.getLabels();
		}
		labels.put("nephele_Type", "HAP");
		labels.put("type", "HTTP");
		meta.setLabels(labels);
		Map<String, Object> anno = new HashMap<String, Object>();
		anno.put("nephele/deployment", svcTcpDto.getApp());
		if (svcTcpDto.getAnnotaion() != null) {
			anno.put("nephele/annotation", svcTcpDto.getAnnotaion());
		}
		meta.setAnnotations(anno);
		ServiceSpec serviceSpec = new ServiceSpec();
		serviceSpec.setType("ClusterIP");
		List<ServicePort> ports = new ArrayList<ServicePort>();
		serviceSpec.setPorts(ports);
		if (svcTcpDto.getRules() != null && !svcTcpDto.getRules().isEmpty()) {
			List<TcpRuleDto> rules = svcTcpDto.getRules();
			for (int i = 0; i < rules.size(); i++) {
				TcpRuleDto rule = rules.get(i);
				ServicePort servicePort = new ServicePort();
				servicePort.setName(svcTcpDto.getName() + "-port" + i);
				servicePort.setProtocol(rule.getProtocol());
				servicePort.setPort(Integer.valueOf(rule.getPort()));
				servicePort.setTargetPort(Integer.valueOf(rule.getTargetPort()));
				ports.add(servicePort);
			}
			serviceSpec.setPorts(ports);
		}
		serviceSpec.setSelector(svcTcpDto.getSelector());
		service.setMetadata(meta);
		service.setSpec(serviceSpec);
		ServiceStatus status = new ServiceStatus();
		status.setLoadBalancer(null);
		service.setStatus(status);
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys = CollectionUtil.transBean2Map(service);
		Map<String, Object> head = new HashMap<String, Object>();
		head.put("Content-Type", "application/json");
		K8SClientResponse response = sService.doServiceByNamespace(svcTcpDto.getNamespace(), head, bodys,
				HTTPMethod.POST);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
			return ActionReturnUtil.returnErrorWithMsg(sta.getMessage());
		}
		com.harmonycloud.k8s.bean.Service newService = JsonUtil.jsonToPojo(response.getBody(),
				com.harmonycloud.k8s.bean.Service.class);
		return ActionReturnUtil.returnSuccessWithData(newService);
	}

	@Override
	public ActionReturnUtil createHttpSvc(ParsedIngressListDto parsedIngressList) throws Exception {
		// 创建svc和ingress
		this.ingCreate(parsedIngressList);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 更新service
	 */
	@Override
	public ActionReturnUtil svcUpdate(SvcRouterUpdateDto svcRouterUpdate) throws Exception {
		// 查询
		K8SURL url1 = new K8SURL();
		url1.setNamespace(svcRouterUpdate.getNamespace()).setResource(Resource.SERVICE)
				.setSubpath(svcRouterUpdate.getName());
		Map<String, Object> head = new HashMap<String, Object>();
		head.put("Content-Type", "application/json");
		K8SClientResponse serivceResponse = new K8sMachineClient().exec(url1, HTTPMethod.GET, head, null, null);
		com.harmonycloud.k8s.bean.Service service = K8SClient.converToBean(serivceResponse,
				com.harmonycloud.k8s.bean.Service.class);
		String apiVersion = service.getApiVersion();
		String kind = service.getKind();
		ObjectMeta metadata = service.getMetadata();
		// 更新metadata
		Map<String, Object> annotations = metadata.getAnnotations();
		annotations.clear();
		annotations.put("nephele/deployment", svcRouterUpdate.getService());
		Map<String, Object> labels = metadata.getLabels();
		labels.clear();
		List<HttpLabelDto> httpLabels = svcRouterUpdate.getLabels();
		for (HttpLabelDto httpLabel : httpLabels) {
			labels.put(httpLabel.getName(), httpLabel.getValue());
		}
		labels.put("nephele_Type", "HAP");

		ServiceSpec spec = service.getSpec();
		// 更新spec
		ServiceStatus status = service.getStatus();
		// 更新spec.ports
		List<ServicePort> ports = new ArrayList<>();
		List<TcpRuleDto> rules = svcRouterUpdate.getRules();
		for (int i = 0; i < rules.size(); i++) {
			// 判断是否为编辑状态,编辑状态下不更新
			if (rules.get(i).getIsEdit() != null && rules.get(i).getIsEdit() == false) {
				ServicePort port = new ServicePort();
				port.setName(svcRouterUpdate.getName() + "-port" + i);
				port.setPort(Integer.valueOf(rules.get(i).getPort()));
				port.setTargetPort(Integer.valueOf(rules.get(i).getTargetPort()));
				port.setProtocol(rules.get(i).getProtocol());
				ports.add(port);
			}
		}
		// 更新spec.selector
		Map<String, Object> selector = new HashMap<>();
		if (svcRouterUpdate.getSelector() != null && svcRouterUpdate.getSelector().size() > 0) {
			selector.put(svcRouterUpdate.getSelector().get(0).getName(),
					svcRouterUpdate.getSelector().get(0).getValue());
		}
		spec.setPorts(ports);
		spec.setSelector(selector);
		Map<String, Object> body = new HashMap<>();
		body.put("metadata", metadata);
		body.put("spec", spec);
		body.put("kind", kind);
		body.put("apiVersion", apiVersion);
		body.put("status", status);
		String string = JsonUtil.convertToJsonNonNull(body);
		System.out.println(string);
		K8SURL url = new K8SURL();
		url.setNamespace(svcRouterUpdate.getNamespace()).setResource(Resource.SERVICE)
				.setSubpath(svcRouterUpdate.getName());
		head.put("Content-Type", "application/json");
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, head, body, null);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(response.getBody());
	}

	/**
	 * 删除service
	 */
	@Override
	public ActionReturnUtil svcDelete(String namespace, String name) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.SERVICE).setNamespace(namespace).setSubpath(name);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
			return ActionReturnUtil.returnErrorWithMsg("删除出错");
		}
		return ActionReturnUtil.returnSuccessWithData(response.getBody());
	}

	/**
	 * 删除(tcp) service,释放数据库占用30000-30050端口
	 */
	@Override
	public ActionReturnUtil deleteTcpSvc(String namespace, String name, List<Integer> ports, String tenantId)
			throws Exception {
		// 根据tenantId获取clusterId
		int clusterId = tService.getClusterByTenantid(tenantId).getId().intValue();
		// 根据tenantid获取cluster
		Cluster cluster = tService.getClusterByTenantid(tenantId);
		// 查询port并释放
		if (ports != null && ports.size() > 0) {
			for (Integer port : ports) {
				int nodeport = nodePortMapper.getidbynodeport(port);
				NodePortClusterExample example = new NodePortClusterExample();
				example.createCriteria().andClusteridEqualTo(clusterId).andNodeportidEqualTo(nodeport);
				npcMapper.deleteByExample(example);
			}
		}
		// 删除service
		K8SURL url = new K8SURL();
		url.setResource(Resource.SERVICE).setNamespace(namespace).setSubpath(name);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null, cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
			UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
			return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
		}
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 更新HTTP应用网关
	 *
	 * @param parsedIngressList
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil ingUpdate(ParsedIngressListUpdateDto parsedIngressList) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(parsedIngressList.getNamespace()).setResource(Resource.INGRESS)
				.setName(parsedIngressList.getName());
		Map<String, Object> head = new HashMap<String, Object>();
		head.put("Content-Type", "application/json");
		Map<String, Object> body = new HashMap<>();
		// 设置metadata
		ObjectMeta metadata = new ObjectMeta();
		// 处理label
		List<HttpLabelDto> labels = parsedIngressList.getLabels();
		Map<String, Object> label = new HashMap<>();
		Map<String, Object> annotations = new HashMap<>();
		if (labels != null) {
			for (HttpLabelDto httpLabel : labels) {
				label.put(httpLabel.getName(), httpLabel.getValue());
			}
		}
		if (parsedIngressList.getAnnotaion() != null) {
			annotations.put("nephele/annotation", parsedIngressList.getAnnotaion().toString());
			metadata.setAnnotations(annotations);
		}
		metadata.setLabels(label);
		metadata.setName(parsedIngressList.getName());
		metadata.setNamespace(parsedIngressList.getNamespace());
		// 设置spec
		IngressSpec spec = new IngressSpec();
		// 转换为k8s需要的结构
		List<HttpRuleDto> rules = parsedIngressList.getRules();
		List<IngressRule> listRule = new ArrayList<>();
		if (rules != null) {
			for (HttpRuleDto httpRule : rules) {
				// 判断是否为编辑状态,编辑状态下不更新
				if (httpRule.getIsEdit() != null && !httpRule.getIsEdit().equals("true")) {
					HTTPIngressRuleValue http = new HTTPIngressRuleValue();
					List<HTTPIngressPath> paths = new ArrayList<>();
					IngressRule rule = new IngressRule();
					HTTPIngressPath path = new HTTPIngressPath();
					IngressBackend backend = new IngressBackend();
					backend.setServiceName(httpRule.getService());
					backend.setServicePort(Integer.valueOf(httpRule.getPort()));
					path.setPath(httpRule.getPath());
					path.setBackend(backend);
					paths.add(path);
					http.setPaths(paths);
					rule.setHttp(http);
					rule.setHost(parsedIngressList.getHost());
					listRule.add(rule);
				}
			}
		}
		spec.setRules(listRule);
		body.put("metadata", metadata);
		body.put("spec", spec);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, head, body);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		return ActionReturnUtil.returnSuccessWithData(response.getBody());

	}

	@Override
	public ActionReturnUtil getEntry() throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.POD);
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys.put("labelSelector", "nepheleselector=nephele-entry");
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		}
		PodList podList = JsonUtil.jsonToPojo(response.getBody(), PodList.class);
		List<Pod> pods = podList.getItems();
		if (!pods.isEmpty() && pods.size() > 0) {
			for (Pod pod : pods) {
				if (!StringUtils.isEmpty(pod.getSpec().getNodeName())) {
					return ActionReturnUtil.returnSuccessWithData(pod.getSpec().getNodeName());
				}
			}
		}
		return ActionReturnUtil.returnSuccessWithData(false);
	}

	@Override
	public ActionReturnUtil getHost() throws Exception {

		return ActionReturnUtil.returnSuccessWithData(hostName);
	}

	@Override
	public ActionReturnUtil listProvider() throws Exception {
		ActionReturnUtil res = getEntry();
		if (res.isSuccess()) {
			String ip = res.get("data").toString();
			List<ProviderPlugin> provider = new ArrayList<ProviderPlugin>();
			ProviderPlugin providerPlugin = new ProviderPlugin();
			providerPlugin.setIp(ip);
			providerPlugin.setName(Constant.HAPROXY);
			providerPlugin.setVersion(haProxyVersion);
			provider.add(providerPlugin);
			return ActionReturnUtil.returnSuccessWithData(provider);
		} else {
			return res;
		}

	}

	@Override
	public ActionReturnUtil getPort(String tenantId) throws Exception {
		if (tenantId == "" || tenantId == null) {
			return ActionReturnUtil.returnErrorWithMsg("tenantId为空！");
		}
		// 根据tenantId获取clusterId
		int clusterId = tService.getClusterByTenantid(tenantId).getId().intValue();
		// 根据clusterId查询nodeport_cluster中间表,去除已使用端口;
		NodePortClusterExample example = new NodePortClusterExample();
		example.createCriteria().andClusteridEqualTo(clusterId);
		List<NodePortCluster> list = npcMapper.selectByExample(example);
		ArrayList<Integer> nlist = new ArrayList<Integer>();
		if (list == null || list.isEmpty()) {
			NodePortCluster npCluster = new NodePortCluster();
			npCluster.setClusterid(clusterId);
			npCluster.setNodeportid(1);
			npCluster.setStatus(1);
			npcMapper.insert(npCluster);
			return ActionReturnUtil.returnSuccessWithMsg("30000");
		} else {
			for (int i = 0; i < list.size(); i++) {
				nlist.add(nodePortMapper.getnodeportbyid(list.get(i).getNodeportid()));
			}
			// 生成分配 端口,增加同步
			int nodePort = 0;
			for (int j = 0; j <= 50; j++) {
				nodePort = 30000 + j;
				if (!nlist.contains(nodePort)) {
					NodePortCluster npCluster = new NodePortCluster();
					npCluster.setClusterid(clusterId);
					npCluster.setNodeportid(j + 1);
					npCluster.setStatus(1);
					npcMapper.insert(npCluster);
					return ActionReturnUtil.returnSuccessWithMsg(String.valueOf(nodePort));
				}
			}
		}
		return ActionReturnUtil.returnErrorWithMsg("获取port信息失败");
	}

	@Override
	public ActionReturnUtil getListPort(String tenantId) throws Exception {
		if (tenantId == "" || tenantId == null) {
			return ActionReturnUtil.returnErrorWithMsg("tenantId为空！");
		}
		// 根据tenantId获取clusterId
		int clusterId = tService.getClusterByTenantid(tenantId).getId().intValue();
		// 根据clusterId查询nodeport_cluster中间表,去除已使用端口;
		NodePortClusterExample example = new NodePortClusterExample();
		example.createCriteria().andClusteridEqualTo(clusterId);
		List<NodePortCluster> list = npcMapper.selectByExample(example);
		ArrayList<Integer> nlist = new ArrayList<Integer>();
		if (list == null || list.isEmpty()) {
			return ActionReturnUtil.returnSuccessWithData(null);
		} else {
			for (int i = 0; i < list.size(); i++) {
				nlist.add(nodePortMapper.getnodeportbyid(list.get(i).getNodeportid()));
			}
		}
		return ActionReturnUtil.returnSuccessWithData(nlist);
	}

	@Override
	public ActionReturnUtil checkPort(String port, String tenantId) throws Exception {
		int clusterId = tService.getClusterByTenantid(tenantId).getId().intValue();
		int nodeport = Integer.valueOf(port);
		int nodeportId = nodePortMapper.getidbynodeport(nodeport);
		NodePortClusterExample example = new NodePortClusterExample();
		example.createCriteria().andClusteridEqualTo(clusterId).andNodeportidEqualTo(nodeportId);
		List<NodePortCluster> list = npcMapper.selectByExample(example);
		if (list == null || list.isEmpty()) {
			return ActionReturnUtil.returnSuccessWithData("端口未被占用");
		}
		return ActionReturnUtil.returnSuccessWithData("true");
	}

	@Override
	public ActionReturnUtil updatePort(String oldport, String nowport, String tenantId) throws Exception {
		int clusterId = tService.getClusterByTenantid(tenantId).getId().intValue();
		int oldnodeport = Integer.valueOf(oldport);
		int oldnodeportId = nodePortMapper.getidbynodeport(oldnodeport);
		int nownodeport = Integer.valueOf(nowport);
		int nownodeportId = nodePortMapper.getidbynodeport(nownodeport);
		NodePortCluster npc = new NodePortCluster();
		npc.setClusterid(clusterId);
		npc.setNodeportid(oldnodeportId);
		npc.setUnodeport(nownodeportId);
		npc.setStatus(1);
		npcMapper.updateNodePortClusterbynodeportid(npc);
		return ActionReturnUtil.returnSuccessWithData("true");
	}

	@Override
	public ActionReturnUtil delPort(String port, String tenantId) throws Exception {
		if (StringUtils.isEmpty(port) || port == null) {
			return ActionReturnUtil.returnErrorWithMsg("端口为空！");
		}
		if (StringUtils.isEmpty(tenantId) || tenantId == null) {
			return ActionReturnUtil.returnErrorWithMsg("tenantId为空！");
		}
		// 根据tenantId获取clusterId
		int clusterId = tService.getClusterByTenantid(tenantId).getId().intValue();
		int nodeport = Integer.valueOf(port);
		int nodeportId = nodePortMapper.getidbynodeport(nodeport);
		NodePortClusterExample example = new NodePortClusterExample();
		example.createCriteria().andClusteridEqualTo(clusterId).andNodeportidEqualTo(nodeportId).andStatusEqualTo(1);
		npcMapper.deleteByExample(example);
		return ActionReturnUtil.returnSuccess();
	}

	@Override
	public List<RouterSvc> listIngressByName(ParsedIngressListDto parsedIngressListDto) throws Exception {
		List<RouterSvc> routerSvcs = new ArrayList<RouterSvc>();
		if (parsedIngressListDto.getNamespace() == null) {
			return routerSvcs;
		}
		if (parsedIngressListDto.getLabels() == null) {
			return routerSvcs;
		}
		String namespace = parsedIngressListDto.getNamespace();
		K8SClientResponse response = sService.doServiceByNamespace(namespace, null, null, HTTPMethod.GET);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			return routerSvcs;
		}
		ServiceList svList = JsonUtil.jsonToPojo(response.getBody(), ServiceList.class);
		List<com.harmonycloud.k8s.bean.Service> services = svList.getItems();

		if (!services.isEmpty() || services.size() > 0) {
			for (int i = 0; i < services.size(); i++) {
				boolean flag = false;
				Map<String, Object> labels = services.get(i).getMetadata().getLabels();
				if (!labels.isEmpty()) {
					for (Map.Entry<String, Object> m : labels.entrySet()) {
						if (m.getKey().indexOf("nephele") > -1) {
							flag = true;
							break;
						}
					}
				}

				if (flag) {
					RouterSvc routerSvc = new RouterSvc();
					com.harmonycloud.k8s.bean.Service svc = services.get(i);
					if (svc.getSpec().getSelector().equals(parsedIngressListDto.getLabels())) {
						routerSvc.setNamespace(svc.getMetadata().getNamespace());
						routerSvc.setName(svc.getMetadata().getName());
						routerSvc.setCreateTime(svc.getMetadata().getCreationTimestamp());
						Map<String, Object> tMap = new HashMap<String, Object>();
						for (Map.Entry<String, Object> m : labels.entrySet()) {
							/* if (m.getKey().indexOf("nephele") < 0) { */
							tMap.put(m.getKey(), m.getValue());
							/* } */
						}
						routerSvc.setLabels(tMap);
						routerSvc.setSelector(svc.getSpec().getSelector());
						routerSvc.setRules(svc.getSpec().getPorts());
						Map<String, Object> anno = svc.getMetadata().getAnnotations();
						if (anno != null && !anno.isEmpty()) {
							if (anno.containsKey("nephele/annotation")
									&& !StringUtils.isEmpty(anno.get("nephele/annotation").toString())) {
								routerSvc.setAnnotation(anno.get("nephele/annotation").toString());
							}
							if (anno.containsKey("nephele/deployment")
									&& !StringUtils.isEmpty(anno.get("nephele/deployment").toString())) {
								routerSvc.setService(anno.get("nephele/deployment").toString());
							}
						}
						routerSvcs.add(routerSvc);
					}
				}
			}
		}
		return routerSvcs;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getHaProxyVersion() {
		return haProxyVersion;
	}

	public void setHaProxyVersion(String haProxyVersion) {
		this.haProxyVersion = haProxyVersion;
	}

	@Override
	public ActionReturnUtil listIngressByName(String namespace, String nameList, Cluster cluster) throws Exception {
		if (StringUtils.isEmpty(namespace)) {
			return ActionReturnUtil.returnErrorWithMsg("没有分区");
		}
		if (StringUtils.isEmpty(nameList)) {
			return ActionReturnUtil.returnErrorWithMsg("应用名称不存在");
		}
		ActionReturnUtil hostRes = getEntry();
		if (!hostRes.isSuccess()) {
			return hostRes;
		}
		String ip = (String) hostRes.get("data");
		JSONArray array = new JSONArray();
		List<String> names = new ArrayList<>();
		if(nameList.contains(",")){
			String [] n = nameList.split(",");
			names = java.util.Arrays.asList(n);
		}else{
			names.add(nameList);
		}
		for(String name : names){
			// 获取ingress http
			K8SURL url = new K8SURL();
			url.setNamespace(namespace).setResource(Resource.INGRESS);// 资源类型怎么判断
			Map<String, Object> bodys = new HashMap<String, Object>();
			bodys.put("labelSelector", "app=" + name);
			K8SClientResponse k = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
			if (k.getStatus() == Constant.HTTP_404) {
				return ActionReturnUtil.returnSuccess();
			}
			if (!HttpStatusUtil.isSuccessStatus(k.getStatus()) && k.getStatus() != Constant.HTTP_404) {
				UnversionedStatus status = JsonUtil.jsonToPojo(k.getBody(), UnversionedStatus.class);
				return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
			}
			IngressList ingressList = JsonUtil.jsonToPojo(k.getBody(), IngressList.class);
			if (ingressList != null) {
				List<Ingress> list = ingressList.getItems();
				if (list != null && list.size() > 0) {
					for (Ingress in : list) {
						JSONObject json = new JSONObject();
						json.put("name", in.getMetadata().getName());
						json.put("type", "HTTP");
						JSONArray ja = new JSONArray();
						List<IngressRule> rules = in.getSpec().getRules();
						if (rules != null && rules.size() > 0) {
							IngressRule rule = rules.get(0);
							HTTPIngressRuleValue http = rule.getHttp();
							List<HTTPIngressPath> paths = http.getPaths();
							if (paths != null && paths.size() > 0) {
								for (HTTPIngressPath path : paths) {
									JSONObject j = new JSONObject();
									if (path.getBackend() != null) {
										j.put("port", path.getBackend().getServicePort());
									}
									j.put("hostname", rule.getHost() + ":30888" + path.getPath());
									j.put("ip", ip + ":30888" + path.getPath());
									ja.add(j);
								}
							}
						}
						json.put("address", ja);
						array.add(json);
					}
				}
			}
			// 获取tcp
			ActionReturnUtil tcpRes = svcList(namespace);
			if (!tcpRes.isSuccess()) {
				return tcpRes;
			}
			@SuppressWarnings("unchecked")
			List<RouterSvc> routerSvcs = (List<RouterSvc>) tcpRes.get("data");
			if (routerSvcs != null && routerSvcs.size() > 0) {
				for (RouterSvc routerSvc : routerSvcs) {
					if (routerSvc.getLabels() != null && routerSvc.getLabels().size() > 0) {
						Map<String, Object> labels = routerSvc.getLabels();
						if (labels.get("app") != null && name.equals(labels.get("app").toString())
								&& labels.get("type") != null && "TCP".equals(labels.get("type"))) {
							JSONObject json = new JSONObject();
							json.put("name", routerSvc.getName());
							json.put("type", "TCP");
							if (routerSvc.getRules() != null) {
								List<Integer> ports = new ArrayList<Integer>();
								List<ServicePort> rules = routerSvc.getRules();
								if (rules.size() > 0) {
									JSONArray ja = new JSONArray();
									for (ServicePort rule : rules) {
										JSONObject j = new JSONObject();
										j.put("port", rule.getTargetPort());
										j.put("ip", ip + ":" + rule.getPort());
										ja.add(j);
										ports.add(rule.getPort());
									}
									json.put("address", ja);
									json.put("ports", ports);
								}
							}

							array.add(json);
						}
					}
				}
			}
		}
		return ActionReturnUtil.returnSuccessWithData(array);
	}

}
