package com.harmonycloud.service.application.impl;


import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.scale.AutoScaleDto;
import com.harmonycloud.dto.scale.CustomMetricScaleDto;
import com.harmonycloud.dto.scale.TimeMetricScaleDto;
import com.harmonycloud.k8s.bean.CrossVersionObjectReference;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.scale.*;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.AutoScaleService;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.platform.constant.Constant;
import net.sf.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.harmonycloud.service.platform.constant.Constant.DEPLOYMENT_API_VERSION;

@Service
@Transactional(rollbackFor = Exception.class)
public class AutoScaleServiceImpl implements AutoScaleService {

	public static final String AUTO_SCALE_API_VERSION = "harmonycloud.cn/v1";
	public static final String AUTO_SCALE_TPR_KIND = "Complexpodscale";
	public static final String METRIC_SOURCE_TYPE_RESOURCE = "Resource";
	public static final String METRIC_SOURCE_TYPE_TIME = "Time";
	public static final String METRIC_SOURCE_TYPE_CUSTOM = "Custom";
	public static final String METRIC_NAME_TPS = "TPS";
	public static final String TPS_METRIC_URL = "/openapi/app/stats";

	private static Logger LOGGER = LoggerFactory.getLogger(AutoScaleServiceImpl.class);

	@Autowired
	RouterService routerService;

	@Override
	public boolean create(AutoScaleDto autoScaleDto, Cluster cluster) throws Exception {
		if(autoScaleDto.getTargetTps() != null){
			checkDeploymentCreatedService(autoScaleDto.getNamespace(), autoScaleDto.getDeploymentName(), cluster);
		}
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Content-Type", "application/json");
		Map<String, Object> body = new HashMap<String, Object>();
		body = CollectionUtil.transBean2Map(this.convertCpa(autoScaleDto));
		K8SURL url = new K8SURL();
		url.setNamespace(autoScaleDto.getNamespace()).setResource(Resource.COMPLEXPODSCALER);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, body,cluster);
		if(response.getStatus() == HttpStatus.OK.value() || response.getStatus() == HttpStatus.CREATED.value()){
			return true;
		}else{
			LOGGER.error("创建自动伸缩失败，message:{}",JSONObject.toJSONString(response));
			return false;
		}
	}

	@Override
	public boolean update(AutoScaleDto autoScaleDto, Cluster cluster) throws Exception {
		if(autoScaleDto.getTargetTps() != null){
			checkDeploymentCreatedService(autoScaleDto.getNamespace(), autoScaleDto.getDeploymentName(), cluster);
		}
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Content-Type", "application/json");
		Map<String, Object> body = new HashMap<String, Object>();
		body = CollectionUtil.transBean2Map(this.convertCpa(autoScaleDto));
		K8SURL url = new K8SURL();
		url.setNamespace(autoScaleDto.getNamespace()).setResource(Resource.COMPLEXPODSCALER);
		if (body != null && (body.get("metadata")) != null){
			url.setName(((ObjectMeta) body.get("metadata")).getName());
		}
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, body, cluster);
		if(response.getStatus() == HttpStatus.OK.value()){
			return true;
		}else{
			LOGGER.error("更新自动伸缩失败，",JSONObject.toJSONString(response));
			return false;
		}
	}

	@Override
	public boolean delete(String namespace, String deploymentName, Cluster cluster) throws Exception {
		AutoScaleDto autoScaleDto = this.get(namespace, deploymentName, cluster);
		if(autoScaleDto == null){
			LOGGER.info("删除自动伸缩，未找到。 namespace:{}, deployment:{}",namespace,deploymentName);
			return true;
		}
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.COMPLEXPODSCALER).setName(getScaleName(deploymentName));
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null, cluster);
		if(response.getStatus() == HttpStatus.OK.value()){
			return true;
		}else{
			LOGGER.error("删除自动伸缩失败，message:{}",JSONObject.toJSONString(response));
			return false;
		}
	}

	@Override
	public AutoScaleDto get(String namespace, String deploymentName, Cluster cluster) throws Exception {
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys.put("labelSelector", "app=" + deploymentName);
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.COMPLEXPODSCALER).setName(getScaleName(deploymentName));
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
		if(response.getStatus() == HttpStatus.OK.value()){
			ComplexPodScale complexPodScale = JsonUtil.jsonToPojo(response.getBody(),
					ComplexPodScale.class);
			if(complexPodScale == null){
				return null;
			}
			AutoScaleDto autoScaleDto = this.convertScale(complexPodScale);
			return autoScaleDto;
		}else if(response.getStatus() == HttpStatus.NOT_FOUND.value()){
			LOGGER.info("{}/{}未设置自动伸缩",namespace,deploymentName);
			return null;
		}else{
			LOGGER.error("查询自动伸缩失败，response:{}",JSONObject.toJSONString(response));
			return null;
		}
	}

	private ComplexPodScale convertCpa(AutoScaleDto autoScaleDto) throws MarsRuntimeException {
		ComplexPodScale complexPodScale = new ComplexPodScale();
		complexPodScale.setKind(AUTO_SCALE_TPR_KIND);
		complexPodScale.setApiVersion(AUTO_SCALE_API_VERSION);
		// 设置hpa对象的metadata
		ObjectMeta meta = new ObjectMeta();
		Map<String, Object> labels = new HashMap<String, Object>();
		labels.put("app", autoScaleDto.getDeploymentName());
		meta.setName(getScaleName(autoScaleDto.getDeploymentName()));
		meta.setLabels(labels);
		meta.setCreationTimestamp(null);
		meta.setDeletionGracePeriodSeconds(null);
		meta.setDeletionTimestamp(null);
		complexPodScale.setMetadata(meta);

		// 设置hpa对象的spec
		ComplexPodScaleSpec cpaSpec = new ComplexPodScaleSpec();
		cpaSpec.setMinReplicas(autoScaleDto.getMinPods());
		cpaSpec.setMaxReplicas(autoScaleDto.getMaxPods());

		CrossVersionObjectReference targetRef = new CrossVersionObjectReference();
		targetRef.setApiVersion(DEPLOYMENT_API_VERSION);
		targetRef.setKind(Constant.DEPLOYMENT);
		targetRef.setName(autoScaleDto.getDeploymentName());
		cpaSpec.setScaleTargetRef(targetRef);

		List<MetricSpec> metricSpecs = new ArrayList<>();
		//cpu伸缩 转为资源指标伸缩类型
		if(autoScaleDto.getTargetCpuUsage() != null && autoScaleDto.getTargetCpuUsage() > 0){
			MetricSpec metricSpec = new MetricSpec(METRIC_SOURCE_TYPE_RESOURCE);
			metricSpec.setResource(new ResourceMetricSource("cpu", autoScaleDto.getTargetCpuUsage()));
			metricSpecs.add(metricSpec);
		}
		//tps伸缩 转为自定义指标伸缩类型
		if(autoScaleDto.getTargetTps() != null && autoScaleDto.getTargetTps() > 0){
			CustomMetricSource customMetricSource = new CustomMetricSource(METRIC_NAME_TPS, TPS_METRIC_URL
					+ "?namespace="+autoScaleDto.getNamespace()+"&app="+autoScaleDto.getDeploymentName(),
					autoScaleDto.getTargetTps());
			MetricSpec metricSpec = new MetricSpec(METRIC_SOURCE_TYPE_CUSTOM);
			metricSpec.setCustom(customMetricSource);
			metricSpecs.add(metricSpec);
		}
		if(!CollectionUtils.isEmpty(autoScaleDto.getTimeMetricScales())){
			for(TimeMetricScaleDto timeMetricScale: autoScaleDto.getTimeMetricScales()){
				TimeMetricSource timeMetricSource = new TimeMetricSource(timeMetricScale.getWeekday(),
						timeMetricScale.getTimeSection(), timeMetricScale.getTargetPods());
				MetricSpec metricSpec = new MetricSpec(METRIC_SOURCE_TYPE_TIME);
				metricSpec.setTime(timeMetricSource);
				metricSpecs.add(metricSpec);
			}
		}
		if(!CollectionUtils.isEmpty(autoScaleDto.getCustomMetricScales())) {
			for (CustomMetricScaleDto customMetricScale : autoScaleDto.getCustomMetricScales()) {
				if(customMetricScale.getMetricName().equalsIgnoreCase(METRIC_NAME_TPS)){
					throw new MarsRuntimeException("自定义指标名称TPS为保留字，请修改名称");
				}
				CustomMetricSource customMetricSource = new CustomMetricSource(customMetricScale.getMetricName(),
						customMetricScale.getMetricApi(), customMetricScale.getTargetValue());
				MetricSpec metricSpec = new MetricSpec();
				metricSpec.setType(METRIC_SOURCE_TYPE_CUSTOM);
				metricSpec.setCustom(customMetricSource);
				metricSpecs.add(metricSpec);
			}
		}
		if(metricSpecs.size() == 0){
			throw new MarsRuntimeException("至少需要设置一项伸缩指标");
		}
		cpaSpec.setMetrics(metricSpecs);
		complexPodScale.setSpec(cpaSpec);
		return complexPodScale;
	}

	private AutoScaleDto convertScale(ComplexPodScale complexPodScale) throws MarsRuntimeException {
		AutoScaleDto autoScaleDto = new AutoScaleDto();
		autoScaleDto.setMinPods(complexPodScale.getSpec().getMinReplicas());
		autoScaleDto.setMaxPods(complexPodScale.getSpec().getMaxReplicas());
		autoScaleDto.setNamespace(complexPodScale.getMetadata().getNamespace());
		autoScaleDto.setDeploymentName(complexPodScale.getMetadata().getName());
		ComplexPodScaleStatus status = complexPodScale.getStatus();
		Map<String, Object> statusMap = new HashMap<>();
		if(status != null) {
			autoScaleDto.setLastScaleTime(status.getLastScaleTime());
			autoScaleDto.setCurrentReplicas(status.getCurrentReplicas());
			List<MetricStatus> metricStatuses = status.getCurrentMetrics();
			if(!CollectionUtils.isEmpty(metricStatuses)) {
				for (MetricStatus metricStatus : metricStatuses) {
					if(metricStatus.getType() == null){
						continue;
					}
					switch (metricStatus.getType()) {
						case METRIC_SOURCE_TYPE_RESOURCE:
							statusMap.put(METRIC_SOURCE_TYPE_RESOURCE + "-" + metricStatus.getResource().getName(),
									metricStatus.getResource().getCurrentAverageUtilization());
							break;
						case METRIC_SOURCE_TYPE_CUSTOM:
							statusMap.put(METRIC_SOURCE_TYPE_CUSTOM + "-" + metricStatus.getCustom().getMetricName(),
									metricStatus.getCustom().getCurrentAverageValue());
							break;
						default:
							break;
					}
				}
			}
		}
		List<MetricSpec> metricSpecs = complexPodScale.getSpec().getMetrics();
		List<TimeMetricScaleDto> timeMetricScales = new ArrayList<>();
		List<CustomMetricScaleDto> customMetricScales = new ArrayList<>();
		for(MetricSpec metricSpec : metricSpecs){
			switch (metricSpec.getType()){
				case METRIC_SOURCE_TYPE_RESOURCE:
					if(metricSpec.getResource().getName().equalsIgnoreCase("cpu")){
						autoScaleDto.setTargetCpuUsage(metricSpec.getResource().getTargetAverageUtilization());
						Object currentCpuUsage = statusMap.get(METRIC_SOURCE_TYPE_RESOURCE
								+ "-" + metricSpec.getResource().getName());
						if(currentCpuUsage != null){
							autoScaleDto.setCurrentCpuUsage((Integer)currentCpuUsage);
						}
					}else{
						throw new MarsRuntimeException("不支持资源类型为"+metricSpec.getType()+"的自动伸缩");
					}
					break;
				case METRIC_SOURCE_TYPE_TIME:
					TimeMetricSource time = metricSpec.getTime();
					TimeMetricScaleDto timeMetricScale = new TimeMetricScaleDto(time.getWeekday(),
							time.getTimeSection(), time.getTargetPods());
					timeMetricScales.add(timeMetricScale);
					break;
				case METRIC_SOURCE_TYPE_CUSTOM:
					if(metricSpec.getCustom().getMetricName().equalsIgnoreCase(METRIC_NAME_TPS)){
						autoScaleDto.setTargetTps(metricSpec.getCustom().getTargetAverageValue());
						Object currentTps = statusMap.get(METRIC_SOURCE_TYPE_CUSTOM
								+ "-" + metricSpec.getCustom().getMetricName());
						if(currentTps != null){
							autoScaleDto.setCurrentTps((Long)currentTps);
						}
					}else{
						CustomMetricSource custom = metricSpec.getCustom();
						CustomMetricScaleDto customMetricScale = new CustomMetricScaleDto(custom.getMetricName(),
								custom.getMetricApiUrl(), custom.getTargetAverageValue());
						Object currentValue = statusMap.get(METRIC_SOURCE_TYPE_CUSTOM
								+ "-" + metricSpec.getCustom().getMetricName());
						if(currentValue != null){
							customMetricScale.setCurrentValue((Long)currentValue);
						}
						customMetricScales.add(customMetricScale);
					}
					break;
				default:
					throw new MarsRuntimeException("不支持的指标伸缩类型：" + metricSpec.getType());
			}
		}
		autoScaleDto.setTimeMetricScales(timeMetricScales);
		autoScaleDto.setCustomMetricScales(customMetricScales);
		return autoScaleDto;
	}

	private void checkDeploymentCreatedService(String namespace, String deploymentName, Cluster cluster) throws Exception{
		ActionReturnUtil result = routerService.listIngressByName(namespace, deploymentName, cluster);
		if(result.isSuccess()){
			JSONArray array = (JSONArray)result.get("data");
			if(array == null || array.size() == 0){
				throw new MarsRuntimeException("该应用未对外创建服务，不能根据TPS指标伸缩");
			}
		}else{
			LOGGER.error("查询应用是否对外创建服务失败, data:{}", JSONObject.toJSONString(result.get("data")));
			throw new MarsRuntimeException("查询应用是否对外创建服务失败");
		}
	}

	private String getScaleName(String deploymentName){
		return deploymentName + "-cpa";
	}
}
