package com.harmonycloud.service.application.impl;


import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.ServiceTypeEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dto.scale.AutoScaleDto;
import com.harmonycloud.dto.scale.CustomMetricScaleDto;
import com.harmonycloud.dto.scale.TimeMetricScaleDto;
import com.harmonycloud.k8s.bean.CrossVersionObjectReference;
import com.harmonycloud.k8s.bean.HorizontalPodAutoscaler;
import com.harmonycloud.k8s.bean.HorizontalPodAutoscalerSpec;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.scale.*;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.HorizontalPodAutoscalerService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.AutoScaleService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.application.StatefulSetsService;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.harmonycloud.common.Constant.CommonConstant.CPU;
import static com.harmonycloud.common.Constant.CommonConstant.MEMORY;
import static com.harmonycloud.service.platform.constant.Constant.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class AutoScaleServiceImpl implements AutoScaleService {

	public static final String AUTO_SCALE_API_VERSION = "harmonycloud.cn/v1";
	public static final String AUTO_SCALE_TPR_KIND = "ComplexPodScale";
	public static final String METRIC_SOURCE_TYPE_RESOURCE = "Resource";
	public static final String METRIC_SOURCE_TYPE_TIME = "Time";
	public static final String METRIC_SOURCE_TYPE_CUSTOM = "Custom";
	public static final String METRIC_NAME_TPS = "TPS";
	public static final String TPS_METRIC_URL = "/openapi/app/stats";
	public static final String SCALE_CONTROLLER_TYPE_HPA = "hpa";
	public static final String SCALE_CONTROLLER_TYPE_CPA = "cpa";
	private static final int MAX_DAY = 7 ;
	private static final int MAX_TIME = 24;
	public static final String SCALE_ALARM_TO_EMAIL = "toEmail";
	public static final String SCALE_ALARM_CC_EMAIL = "ccEmail";

	private static Logger LOGGER = LoggerFactory.getLogger(AutoScaleServiceImpl.class);

	@Autowired
	private RouterService routerService;

	@Autowired
	private NamespaceLocalService namespaceLocalService;

	@Autowired
	private HorizontalPodAutoscalerService hpaService;

	@Autowired
	private DeploymentsService dpsService;

    @Autowired
    private StatefulSetsService statefulSetsService;

	@Override
	public ActionReturnUtil create(AutoScaleDto autoScaleDto) throws Exception {
		ActionReturnUtil  result;
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(autoScaleDto.getNamespace());
		/*if(autoScaleDto.getTargetTps() != null){
			checkDeploymentCreatedService(autoScaleDto.getNamespace(), autoScaleDto.getDeploymentName(), cluster);
		}*/
		//自定义或时间段自动伸缩，使用crd
		if(!CollectionUtils.isEmpty(autoScaleDto.getCustomMetricScales())
				|| !CollectionUtils.isEmpty(autoScaleDto.getTimeMetricScales())){
			ErrorCodeMessage errorCodeMessage = checkCpaData(autoScaleDto);
			if (errorCodeMessage != null ){
					return ActionReturnUtil.returnErrorWithMsg(errorCodeMessage);
			}

		}
		//如果有非cpu和内存的指标伸缩，用自定义的controller处理弹性伸缩，如果只有cpu和内存则用k8s的hpa
		if(isCustomMetricEnabled(autoScaleDto)){
			result = this.createCpa(autoScaleDto, cluster);
			if(!result.isSuccess()){
				return result;
			}
		}else {
			result = this.createHpa(autoScaleDto, cluster);
		}

		if(result.isSuccess()){
			updateAutoscaleLabel(autoScaleDto.getNamespace(), autoScaleDto.getDeploymentName(), cluster, STATUS_ON, autoScaleDto.getServiceType());
		}

		return result;

	}

	@Override
	public ActionReturnUtil update(AutoScaleDto autoScaleDto) throws Exception {
		AssertUtil.notBlank(autoScaleDto.getNamespace(),DictEnum.NAMESPACE);
		AssertUtil.notBlank(autoScaleDto.getDeploymentName(),DictEnum.DEPLOYMENT_NAME);

		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(autoScaleDto.getNamespace());
		if (cluster == null) {
			throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
		}
		AutoScaleDto existScale = this.get(autoScaleDto.getNamespace(), autoScaleDto.getDeploymentName());
		if(existScale == null){
			throw new MarsRuntimeException(DictEnum.AUTO_SCALE.phrase(),ErrorCodeMessage.NOT_FOUND);
		}
		if(isCustomMetricEnabled(existScale)){
			if(isCustomMetricEnabled(autoScaleDto)){
				ErrorCodeMessage errorCodeMessage = checkCpaData(autoScaleDto);
				if (errorCodeMessage != null ){
					return ActionReturnUtil.returnErrorWithMsg(errorCodeMessage);
				}
				this.updateCpa(autoScaleDto, cluster);
			}else{
				this.deleteCpa(autoScaleDto.getNamespace(), autoScaleDto.getDeploymentName(), cluster);
				this.create(autoScaleDto);
			}
		}else if(isCustomMetricEnabled(autoScaleDto)){
			ErrorCodeMessage errorCodeMessage = checkCpaData(autoScaleDto);
			if (errorCodeMessage != null ){
				return ActionReturnUtil.returnErrorWithMsg(errorCodeMessage);
			}
			this.deleteHpa(autoScaleDto.getNamespace(), autoScaleDto.getDeploymentName(), cluster);
			this.create(autoScaleDto);
		}else{
			this.updateHpa(autoScaleDto, cluster);
		}
		return ActionReturnUtil.returnSuccess();
	}

	@Override
	public boolean delete(String namespace, String deploymentName) throws Exception {
		boolean result;
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
		if (cluster == null) {
			throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
		}
		AutoScaleDto autoScaleDto = this.get(namespace, deploymentName);
		if(autoScaleDto == null){
			LOGGER.info("删除自动伸缩，未找到。 namespace:{}, deployment:{}",namespace,deploymentName);
			return true;
		}

        if(isCustomMetricEnabled(autoScaleDto)){
			result = this.deleteCpa(namespace, deploymentName, cluster);
		}else {
			result = this.deleteHpa(namespace, deploymentName, cluster);
		}

		if(result){
			updateAutoscaleLabel(namespace, deploymentName, cluster, STATUS_OFF, null);
		}

		return result;
	}

	@Override
	public AutoScaleDto get(String namespace, String deploymentName) throws Exception {
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
		if (cluster == null) {
			throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
		}
		AutoScaleDto autoScaleDto = getCpa(namespace, deploymentName, cluster, null);
		if(autoScaleDto != null ){
			return autoScaleDto;
		}
		return this.getHpa(namespace, deploymentName, cluster, autoScaleDto);
	}

	private boolean isCpuOrMemoryEnabled(AutoScaleDto autoScaleDto){
		return (autoScaleDto.getTargetCpuUsage() != null && autoScaleDto.getTargetCpuUsage() > 0)
				|| (autoScaleDto.getTargetMemoryUsage() != null && autoScaleDto.getTargetMemoryUsage() > 0);
	}

	private boolean isCustomMetricEnabled(AutoScaleDto autoScaleDto){
		return !CollectionUtils.isEmpty(autoScaleDto.getCustomMetricScales())
				|| !CollectionUtils.isEmpty(autoScaleDto.getTimeMetricScales())
				|| (autoScaleDto.getTargetTps()!= null && autoScaleDto.getTargetTps() > 0);

	}



	private ActionReturnUtil createCpa(AutoScaleDto autoScaleDto, Cluster cluster) throws Exception{
		ComplexPodScale complexPodScale = this.convertCpa(autoScaleDto);
		if(complexPodScale == null){
			return ActionReturnUtil.returnSuccess();
		}
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		Map<String, Object> body = CollectionUtil.transBean2Map(complexPodScale);
		K8SURL url = new K8SURL();
		url.setNamespace(autoScaleDto.getNamespace()).setResource(Resource.COMPLEXPODSCALER);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, body,cluster);
		if(response.getStatus() == HttpStatus.OK.value() || response.getStatus() == HttpStatus.CREATED.value()){
			return ActionReturnUtil.returnSuccess();
		}else{
			LOGGER.error("创建自动伸缩失败，message:{}",JSONObject.toJSONString(response));
			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_AUTOSCALE_CREATE_FAILURE);
		}
	}

	private ErrorCodeMessage checkCpaData(AutoScaleDto autoScaleDto) throws Exception {
		Integer maxPods = autoScaleDto.getMaxPods();
		Integer minPods = autoScaleDto.getMinPods();
		if (minPods > maxPods) {
			return ErrorCodeMessage.AUTOSCALE_TIME_MAX_MIN_ERROR ;
		}
		if (!CollectionUtils.isEmpty(autoScaleDto.getTimeMetricScales())){
			Integer[] podsNum = new Integer[autoScaleDto.getTimeMetricScales().size()+1];
			int[][] checkTimes = new int[MAX_DAY][MAX_TIME*2];
			for (int i=0 ; i< autoScaleDto.getTimeMetricScales().size(); i++ ) {
				TimeMetricScaleDto timeMetricScale = autoScaleDto.getTimeMetricScales().get(i);
				if (podsNum[0] == null ){
					podsNum[0] = timeMetricScale.getNormalPods();
				}
				if(timeMetricScale.getTargetPods() > maxPods || timeMetricScale.getTargetPods() < minPods){
					return ErrorCodeMessage.AUTOSCALE_TIME_PODS_ERROR ;
				}
				podsNum[i+1] = timeMetricScale.getTargetPods();
				String weekday = timeMetricScale.getWeekday();
				String[] days = weekday.split(",");
				String timeSection =  timeMetricScale.getTimeSection();
				timeSection = timeSection.replaceAll(":00","");
				String[] times =  timeSection.split("-");
				for (String day : days){
					for(int j=Integer.valueOf(times[0]).intValue()*2+1; j< Integer.valueOf(times[1]).intValue()*2; j++ ){
						if (checkTimes[Integer.valueOf(day).intValue()][j] == 1 ) {
							return ErrorCodeMessage.AUTOSCALE_TIME_ZONE_ERROR ;
						} else {
							checkTimes[Integer.valueOf(day).intValue()][j] = 1;
						}
					}
				}
			}
			/*Arrays.sort(podsNum);
			if (podsNum[0] != minPods) {
				return ErrorCodeMessage.AUTOSCALE_TIME_MAX_MIN_ERROR;
			}
			if (podsNum[podsNum.length-1] != maxPods) {
				return ErrorCodeMessage.AUTOSCALE_TIME_MAX_MIN_ERROR ;
			}*/

		}
		return null;


	}



	private ActionReturnUtil createHpa(AutoScaleDto autoScaleDto, Cluster cluster) throws Exception {
		HorizontalPodAutoscaler hpAutoscaler = this.convertHpa(autoScaleDto);
		if(hpAutoscaler == null){
			return ActionReturnUtil.returnSuccess();
		}
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Content-Type", "application/json");
		Map<String, Object> body = CollectionUtil.transBean2Map(hpAutoscaler);
		K8SClientResponse response = hpaService.postHpautoscalerByNamespace(autoScaleDto.getNamespace(), headers, body, HTTPMethod.POST, cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			LOGGER.error("创建自动伸缩hpa失败，response：{}",JSONObject.toJSONString(response));
			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_AUTOSCALE_CREATE_FAILURE);
		}
		return ActionReturnUtil.returnSuccess();
	}

	private AutoScaleDto getCpa(String namespace, String deploymentName, Cluster cluster, AutoScaleDto autoScaleDto) throws Exception{
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
			return this.convertScale(complexPodScale, autoScaleDto);
		}else if(response.getStatus() == HttpStatus.NOT_FOUND.value()){
			return null;
		}else{
			LOGGER.error("查询cpa自动伸缩失败，response:{}",JSONObject.toJSONString(response));
			return null;
		}
	}


	private AutoScaleDto getHpa(String namespace, String name, Cluster cluster, AutoScaleDto autoScaleDto) throws Exception {
		K8SClientResponse response = hpaService.doSpecifyHpautoscaler(namespace, name, null, null, HTTPMethod.GET, cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			if(response.getStatus() == HttpStatus.NOT_FOUND.value()){
				return null;
			}
			LOGGER.error("查询hpa自动伸缩失败，response:{}",JSONObject.toJSONString(response));
			return null;
		}
		if(response.getStatus() == HttpStatus.OK.value()){
			HorizontalPodAutoscaler hpa = JsonUtil.jsonToPojo(response.getBody(), HorizontalPodAutoscaler.class);
			if(hpa == null){
				return null;
			}
			return this.convertDto(hpa, autoScaleDto);
		}else if(response.getStatus() == HttpStatus.NOT_FOUND.value()){
			return null;
		}else{
			LOGGER.error("查询cpa自动伸缩失败，response:{}",JSONObject.toJSONString(response));
			return null;
		}

	}

	private ActionReturnUtil updateCpa(AutoScaleDto autoScaleDto, Cluster cluster) throws Exception{
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys.put("labelSelector", Constant.TYPE_DEPLOYMENT + "=" + autoScaleDto.getDeploymentName());
		K8SURL url = new K8SURL();
		url.setNamespace(autoScaleDto.getNamespace()).setResource(Resource.COMPLEXPODSCALER).setName(getScaleName(autoScaleDto.getDeploymentName()));
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
		if(response.getStatus() == HttpStatus.OK.value()){
			ComplexPodScale complexPodScale = JsonUtil.jsonToPojo(response.getBody(),
					ComplexPodScale.class);
			if(complexPodScale == null) {
				LOGGER.error("更新失败，未找到自动伸缩失败，scale:{}",JSONObject.toJSONString(autoScaleDto));
				return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.AUTOSCALE_NOT_FOUND);
			}
			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Content-Type", "application/json");
			Map<String, Object> body = CollectionUtil.transBean2Map(this.convertCpaUpdate(autoScaleDto, complexPodScale));
			url = new K8SURL();
			url.setNamespace(autoScaleDto.getNamespace()).setResource(Resource.COMPLEXPODSCALER);
			if (body != null && (body.get("metadata")) != null){
				url.setName(((ObjectMeta) body.get("metadata")).getName());
			}
			K8SClientResponse responses = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, body, cluster);
			if(responses.getStatus() == HttpStatus.OK.value()){
				return ActionReturnUtil.returnSuccess();
			}else{
				LOGGER.error("更新cpa自动伸缩失败，",JSONObject.toJSONString(responses));
				return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_AUTOSCALE_UPDATE_FAILURE);
			}
		}else{
			LOGGER.error("更新cpa自动伸缩失败，",JSONObject.toJSONString(response));
			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_AUTOSCALE_UPDATE_FAILURE);
		}
	}

	private ActionReturnUtil updateHpa(AutoScaleDto autoScaleDto, Cluster cluster) throws Exception {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Content-Type", "application/json");
		Map<String, Object> body = CollectionUtil.transBean2Map(this.convertHpa(autoScaleDto));
		K8SClientResponse response = hpaService.doHpautoscalerByNamespace(autoScaleDto.getNamespace(), headers, body, HTTPMethod.PUT, cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			LOGGER.error("更新hpa自动伸缩失败，",JSONObject.toJSONString(response));
			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SERVICE_AUTOSCALE_UPDATE_FAILURE);
		}
		return ActionReturnUtil.returnSuccess();
	}

	private boolean deleteCpa(String namespace, String deploymentName, Cluster cluster) throws Exception{
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


	private boolean deleteHpa(String namespace, String name, Cluster cluster) throws Exception {
		K8SClientResponse response = hpaService.doSpecifyHpautoscaler(namespace, name, null, null, HTTPMethod.DELETE, cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			LOGGER.error("删除hpa自动伸缩失败，message:{}",JSONObject.toJSONString(response));
			return false;
		}
		return true;
	}


	private ComplexPodScale convertCpa(AutoScaleDto autoScaleDto) throws MarsRuntimeException {
		ComplexPodScale complexPodScale = new ComplexPodScale();
		complexPodScale.setKind(AUTO_SCALE_TPR_KIND);
		complexPodScale.setApiVersion(AUTO_SCALE_API_VERSION);
		// 设置hpa对象的metadata
		ObjectMeta meta = new ObjectMeta();
		meta.setName(getScaleName(autoScaleDto.getDeploymentName()));
		Map<String, Object> annotations = convertAnnotations(autoScaleDto);
		if (annotations != null) {
			meta.setAnnotations(annotations);
		}
		complexPodScale.setMetadata(meta);

		// 设置hpa对象的spec
		ComplexPodScaleSpec cpaSpec = new ComplexPodScaleSpec();
		cpaSpec.setMinReplicas(autoScaleDto.getMinPods());
		cpaSpec.setMaxReplicas(autoScaleDto.getMaxPods());
        Map<String, Object> convertResult = convertTargetRef(autoScaleDto.getDeploymentName(), autoScaleDto.getServiceType());
        meta.setLabels((Map<String, Object>) convertResult.get("label"));
        cpaSpec.setScaleTargetRef((CrossVersionObjectReference)convertResult.get("targetRef"));

		List<MetricSpec> metricSpecs = new ArrayList<>();
		//cpu伸缩 转为资源指标伸缩类型
		if(autoScaleDto.getTargetCpuUsage() != null && autoScaleDto.getTargetCpuUsage() > 0){
			MetricSpec metricSpec = new MetricSpec(METRIC_SOURCE_TYPE_RESOURCE);
			metricSpec.setResource(new ResourceMetricSource(CPU, autoScaleDto.getTargetCpuUsage()));
			metricSpecs.add(metricSpec);
		}
		if(autoScaleDto.getTargetMemoryUsage() != null && autoScaleDto.getTargetMemoryUsage() > 0){
			MetricSpec metricSpec = new MetricSpec(METRIC_SOURCE_TYPE_RESOURCE);
			metricSpec.setResource(new ResourceMetricSource(MEMORY, autoScaleDto.getTargetMemoryUsage()));
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
						timeMetricScale.getTimeSection(), timeMetricScale.getTargetPods(), timeMetricScale.getNormalPods());
				MetricSpec metricSpec = new MetricSpec(METRIC_SOURCE_TYPE_TIME);
				metricSpec.setTime(timeMetricSource);
				metricSpecs.add(metricSpec);
			}
		}
		if(!CollectionUtils.isEmpty(autoScaleDto.getCustomMetricScales())) {
			for (CustomMetricScaleDto customMetricScale : autoScaleDto.getCustomMetricScales()) {
				if(customMetricScale.getMetricName().equalsIgnoreCase(METRIC_NAME_TPS)){
					throw new MarsRuntimeException(ErrorCodeMessage.RESERVED_KEYWORD,"TPS",true);
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
			return null;
			//throw new MarsRuntimeException(ErrorCodeMessage.AUTOSCALE_NOT_SELECTED);
		}
		cpaSpec.setMetrics(metricSpecs);
		complexPodScale.setSpec(cpaSpec);
		return complexPodScale;
	}

	private ComplexPodScale convertCpaUpdate(AutoScaleDto autoScaleDto, ComplexPodScale complex) throws MarsRuntimeException {
		ComplexPodScale complexPodScale = new ComplexPodScale();
		complexPodScale.setKind(AUTO_SCALE_TPR_KIND);
		complexPodScale.setApiVersion(AUTO_SCALE_API_VERSION);
		// 设置hpa对象的metadata
		ObjectMeta meta = complex.getMetadata();
		meta.setName(getScaleName(autoScaleDto.getDeploymentName()));
		Map<String, Object> annotations = convertAnnotations(autoScaleDto);
		if (annotations != null) {
			meta.setAnnotations(annotations);
		}
		complexPodScale.setMetadata(meta);

		// 设置hpa对象的spec
		ComplexPodScaleSpec cpaSpec = complex.getSpec();
		cpaSpec.setMinReplicas(autoScaleDto.getMinPods());
		cpaSpec.setMaxReplicas(autoScaleDto.getMaxPods());

        Map<String, Object> convertResult = convertTargetRef(autoScaleDto.getDeploymentName(), autoScaleDto.getServiceType());
        meta.setLabels((Map<String, Object>) convertResult.get("label"));
        cpaSpec.setScaleTargetRef((CrossVersionObjectReference)convertResult.get("targetRef"));


		List<MetricSpec> metricSpecs = new ArrayList<>();
		//cpu伸缩 转为资源指标伸缩类型
		if(autoScaleDto.getTargetCpuUsage() != null && autoScaleDto.getTargetCpuUsage() > 0){
			MetricSpec metricSpec = new MetricSpec(METRIC_SOURCE_TYPE_RESOURCE);
			metricSpec.setResource(new ResourceMetricSource(CPU, autoScaleDto.getTargetCpuUsage()));
			metricSpecs.add(metricSpec);
		}
		if(autoScaleDto.getTargetMemoryUsage() != null && autoScaleDto.getTargetMemoryUsage() > 0){
			MetricSpec metricSpec = new MetricSpec(METRIC_SOURCE_TYPE_RESOURCE);
			metricSpec.setResource(new ResourceMetricSource(MEMORY, autoScaleDto.getTargetMemoryUsage()));
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
						timeMetricScale.getTimeSection(), timeMetricScale.getTargetPods(), timeMetricScale.getNormalPods());
				MetricSpec metricSpec = new MetricSpec(METRIC_SOURCE_TYPE_TIME);
				metricSpec.setTime(timeMetricSource);
				metricSpecs.add(metricSpec);
			}
		}
		if(!CollectionUtils.isEmpty(autoScaleDto.getCustomMetricScales())) {
			for (CustomMetricScaleDto customMetricScale : autoScaleDto.getCustomMetricScales()) {
				if(customMetricScale.getMetricName().equalsIgnoreCase(METRIC_NAME_TPS)){
					throw new MarsRuntimeException(ErrorCodeMessage.RESERVED_KEYWORD,"TPS",true);
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
			throw new MarsRuntimeException(ErrorCodeMessage.AUTOSCALE_NOT_SELECTED);
		}
		cpaSpec.setMetrics(metricSpecs);
		complexPodScale.setSpec(cpaSpec);
		return complexPodScale;
	}

	private AutoScaleDto convertScale(ComplexPodScale complexPodScale, AutoScaleDto autoScaleDto) throws MarsRuntimeException {
		if(complexPodScale == null){
			return autoScaleDto;
		}
		AutoScaleDto autoScale = autoScaleDto;
		if(autoScale == null){
			autoScale = new AutoScaleDto();
		}
		autoScale.setUid(complexPodScale.getMetadata().getUid());
		autoScale.setMinPods(complexPodScale.getSpec().getMinReplicas());
		autoScale.setMaxPods(complexPodScale.getSpec().getMaxReplicas());
		autoScale.setNamespace(complexPodScale.getMetadata().getNamespace());
		autoScale.setDeploymentName(complexPodScale.getMetadata().getName());
		autoScale.setControllerType(SCALE_CONTROLLER_TYPE_CPA);
		Map<String, Object> annottions = complexPodScale.getMetadata().getAnnotations();
		if (annottions != null){
			autoScale.setToEmail(annottions.get(SCALE_ALARM_TO_EMAIL) == null ? "" : ((String) annottions.get(SCALE_ALARM_TO_EMAIL)));
			autoScale.setCcEmail(annottions.get(SCALE_ALARM_CC_EMAIL) == null ? "" : ((String) annottions.get(SCALE_ALARM_CC_EMAIL)));
		}
		ComplexPodScaleStatus status = complexPodScale.getStatus();
		Map<String, Object> statusMap = new HashMap<>();
		if(status != null) {
			autoScale.setLastScaleTime(status.getLastScaleTime());
			autoScale.setCurrentReplicas(status.getCurrentReplicas());
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
					if(metricSpec.getResource().getName().equalsIgnoreCase(CPU)){
						autoScale.setTargetCpuUsage(metricSpec.getResource().getTargetAverageUtilization());
						Object currentCpuUsage = statusMap.get(METRIC_SOURCE_TYPE_RESOURCE
								+ "-" + metricSpec.getResource().getName());
						if(currentCpuUsage != null){
							autoScale.setCurrentCpuUsage((Integer)currentCpuUsage);
						}
					}else if(metricSpec.getResource().getName().equalsIgnoreCase(MEMORY)){
						autoScale.setTargetMemoryUsage(metricSpec.getResource().getTargetAverageUtilization());
						Object currentMemoryUsage = statusMap.get(METRIC_SOURCE_TYPE_RESOURCE
								+ "-" + metricSpec.getResource().getName());
						if(currentMemoryUsage != null){
							autoScale.setCurrentMemoryUsage((Integer)currentMemoryUsage);
						}
					}else{
						throw new MarsRuntimeException(ErrorCodeMessage.AUTOSCALE_METRIC_NOT_SUPPORT);
					}
					break;
				case METRIC_SOURCE_TYPE_TIME:
					TimeMetricSource time = metricSpec.getTime();
					TimeMetricScaleDto timeMetricScale = new TimeMetricScaleDto(time.getWeekday(),
							time.getTimeSection(), time.getTargetPods(), time.getNormalPods());
					timeMetricScales.add(timeMetricScale);
					break;
				case METRIC_SOURCE_TYPE_CUSTOM:
					if(metricSpec.getCustom().getMetricName().equalsIgnoreCase(METRIC_NAME_TPS)){
						autoScale.setTargetTps(metricSpec.getCustom().getTargetAverageValue());
						Object currentTps = statusMap.get(METRIC_SOURCE_TYPE_CUSTOM
								+ "-" + metricSpec.getCustom().getMetricName());
						if(currentTps != null){
							autoScale.setCurrentTps((Long)currentTps);
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
					throw new MarsRuntimeException(ErrorCodeMessage.AUTOSCALE_METRIC_NOT_SUPPORT,metricSpec.getType(),false);
			}
		}
		autoScale.setTimeMetricScales(timeMetricScales);
		autoScale.setCustomMetricScales(customMetricScales);
		return autoScale;
	}

	private void checkDeploymentCreatedService(String namespace, String deploymentName, Cluster cluster) throws Exception{
		ActionReturnUtil result = routerService.listIngressByName(namespace, deploymentName);
		if(result.isSuccess()){
			JSONArray array = (JSONArray)result.get("data");
			if(array == null || array.size() == 0){
				throw new MarsRuntimeException(ErrorCodeMessage.AUTOSCALE_CONDITION_REQUIRE);
			}
		}else{
			LOGGER.error("查询应用是否对外创建服务失败, data:{}", JSONObject.toJSONString(result.get("data")));
			throw new MarsRuntimeException(ErrorCodeMessage.QUERY_FAIL, DictEnum.SERVICE_OUT.phrase(),true);
		}
	}


	public AutoScaleDto convertDto(HorizontalPodAutoscaler hpa, AutoScaleDto autoScaleDto) throws Exception {
		if(hpa == null){
			return autoScaleDto;
		}
		AutoScaleDto autoScale = autoScaleDto;
		if(autoScale == null){
			autoScale = new AutoScaleDto();
		}
		autoScale.setUid(hpa.getMetadata().getUid());
		HorizontalPodAutoscalerSpec hpaSpec = hpa.getSpec();
		autoScale.setMaxPods(hpaSpec.getMaxReplicas());
		autoScale.setMinPods(hpaSpec.getMinReplicas());
		autoScale.setControllerType(SCALE_CONTROLLER_TYPE_HPA);
		Map<String, Object> annottions = hpa.getMetadata().getAnnotations();
		if (annottions != null){
			autoScale.setToEmail(annottions.get(SCALE_ALARM_TO_EMAIL) == null ? "" : ((String) annottions.get(SCALE_ALARM_TO_EMAIL)));
			autoScale.setCcEmail(annottions.get(SCALE_ALARM_CC_EMAIL) == null ? "" : ((String) annottions.get(SCALE_ALARM_CC_EMAIL)));
		}
		if(hpa.getStatus() != null){
			autoScale.setLastScaleTime(hpa.getStatus().getLastScaleTime());
		}

		List<com.harmonycloud.k8s.bean.MetricSpec> metricSpecList = hpaSpec.getMetrics();
		if (!CollectionUtils.isEmpty(metricSpecList)) {
			for (com.harmonycloud.k8s.bean.MetricSpec metric : metricSpecList) {
				com.harmonycloud.k8s.bean.ResourceMetricSource source = metric.getResource();
				if (source == null) {
					continue;
				}
				if(source.getName().equalsIgnoreCase(CPU)){
					autoScale.setTargetCpuUsage(source.getTargetAverageUtilization());
				}else if(source.getName().equalsIgnoreCase(MEMORY)){
					autoScale.setTargetMemoryUsage(source.getTargetAverageUtilization());
				}
			}
		}
		return autoScale;
	}

	public HorizontalPodAutoscaler convertHpa(AutoScaleDto autoScaleDto) throws Exception {
		HorizontalPodAutoscaler hpAutoscaler = new HorizontalPodAutoscaler();

		// 设置hpa对象的metadata
		ObjectMeta meta = new ObjectMeta();
		meta.setName(autoScaleDto.getDeploymentName() + "-" + SCALE_CONTROLLER_TYPE_HPA);
		meta.setCreationTimestamp(null);
		meta.setDeletionGracePeriodSeconds(null);
		meta.setDeletionTimestamp(null);
		Map<String, Object> annotations = convertAnnotations(autoScaleDto);
		if (annotations != null) {
			meta.setAnnotations(annotations);
		}
		hpAutoscaler.setMetadata(meta);

		// 设置hpa对象的spec
		HorizontalPodAutoscalerSpec hpaSpec = new HorizontalPodAutoscalerSpec();
		hpaSpec.setMinReplicas(autoScaleDto.getMinPods());
		hpaSpec.setMaxReplicas(autoScaleDto.getMaxPods());
        Map<String, Object> convertResult = convertTargetRef(autoScaleDto.getDeploymentName(), autoScaleDto.getServiceType());
        meta.setLabels((Map<String, Object>) convertResult.get("label"));
        hpaSpec.setScaleTargetRef((CrossVersionObjectReference)convertResult.get("targetRef"));
		List<com.harmonycloud.k8s.bean.MetricSpec> metricSpecList = new ArrayList<com.harmonycloud.k8s.bean.MetricSpec>();
		if(autoScaleDto.getTargetCpuUsage() != null && autoScaleDto.getTargetCpuUsage() > 0){
			metricSpecList.add(createMetricSpec(CPU, autoScaleDto.getTargetCpuUsage()));
		}
		if(autoScaleDto.getTargetMemoryUsage() != null && autoScaleDto.getTargetMemoryUsage() > 0){
			metricSpecList.add(createMetricSpec(CommonConstant.MEMORY, autoScaleDto.getTargetMemoryUsage()));
		}
		hpaSpec.setMetrics(metricSpecList);
		hpAutoscaler.setSpec(hpaSpec);
		return hpAutoscaler;
	}

	private com.harmonycloud.k8s.bean.MetricSpec createMetricSpec(String metricName, Integer targetValue){
		com.harmonycloud.k8s.bean.MetricSpec metric =  new com.harmonycloud.k8s.bean.MetricSpec();
		metric.setType("Resource");
		com.harmonycloud.k8s.bean.ResourceMetricSource resource = new com.harmonycloud.k8s.bean.ResourceMetricSource();
		resource.setName(metricName);
		resource.setTargetAverageUtilization(targetValue);
		metric.setResource(resource);
		return metric;
	}

	private String getScaleName(String deploymentName){
		return deploymentName + "-" + SCALE_CONTROLLER_TYPE_CPA;
	}

	//更新AutoScale标签
	private void updateAutoscaleLabel(String namespace, String name, Cluster cluster , String status, String serviceType) throws Exception{
		ActionReturnUtil actionReturnUtil = null;
		Map<String, Object> label = new HashMap<>();
		label.put(NODESELECTOR_LABELS_PRE + LABEL_AUTOSCALE, status);
        ServiceTypeEnum serviceTypeEnum = ServiceTypeEnum.DEPLOYMENT;
        if(StringUtils.isNotBlank(serviceType)) {
            serviceTypeEnum.valueOf(serviceType.toUpperCase());
        }
        switch(serviceTypeEnum){
            case DEPLOYMENT:
                actionReturnUtil = dpsService.updateLabels(namespace, name, cluster, label);
                break;
            case STATEFULSET:
                actionReturnUtil = statefulSetsService.updateLabels(namespace, name, cluster, label);
                break;
        }

		if(!actionReturnUtil.isSuccess()){
			LOGGER.error("更新自动伸缩标签失败, DeploymentName:{}, message:{}", name, actionReturnUtil.getData());
		}
	}

    private Map<String, Object> convertTargetRef(String name, String serviceType) throws MarsRuntimeException{
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> labels = new HashMap<>();
        CrossVersionObjectReference targetRef = new CrossVersionObjectReference();
        targetRef.setName(name);
        ServiceTypeEnum typeEnum = ServiceTypeEnum.DEPLOYMENT;
        if(StringUtils.isNotBlank(serviceType)) {
            typeEnum = ServiceTypeEnum.valueOf(serviceType.toUpperCase());
        }
        switch (typeEnum) {
            case DEPLOYMENT:
                labels.put(Constant.TYPE_DEPLOYMENT, name);
                targetRef.setApiVersion(DEPLOYMENT_API_VERSION);
                targetRef.setKind(Constant.DEPLOYMENT);
                break;
            case STATEFULSET:
                labels.put(Constant.TYPE_STATEFULSET, name);
                targetRef.setApiVersion(STATEFULSET_API_VERSION);
                targetRef.setKind(Constant.STATEFULSET);
                break;
            default:
                throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_TYPE_NOT_EXIST);
        }
        result.put("label", labels);
        result.put("targetRef", targetRef);
        return result;
    }

	private Map<String, Object> convertAnnotations(AutoScaleDto autoScaleDto){
		if (StringUtils.isNotBlank(autoScaleDto.getToEmail())) {
			Map<String, Object> annotations = Maps.newHashMapWithExpectedSize(2);
			annotations.put(SCALE_ALARM_TO_EMAIL, autoScaleDto.getToEmail());
			if (StringUtils.isNotBlank(autoScaleDto.getCcEmail())) {
				annotations.put(SCALE_ALARM_CC_EMAIL, autoScaleDto.getCcEmail());
			}
			return annotations;
		}
		return null;
	}
}
