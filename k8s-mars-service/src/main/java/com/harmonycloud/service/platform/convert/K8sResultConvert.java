package com.harmonycloud.service.platform.convert;

import com.harmonycloud.dto.business.*;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.util.RandomNum;
import com.harmonycloud.service.platform.bean.*;
import com.harmonycloud.service.platform.constant.Constant;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 
 * @author jmi
 *
 */
public class K8sResultConvert {

	public static AppDetail convertAppDetail(Deployment dep, ServiceList serviceList, EventList eventList,
			EventList hapEve, HorizontalPodAutoscalerList hpaList, PodList podList) throws Exception {
		AppDetail appDetail = new AppDetail();

		// 封装返回值
		ObjectMeta meta = dep.getMetadata();
		appDetail.setName(meta.getName());
		appDetail.setNamespace(meta.getNamespace());
		appDetail.setVersion("v" + meta.getAnnotations().get("deployment.kubernetes.io/revision").toString());
		appDetail.setCreateTime(meta.getCreationTimestamp());
		if (!meta.getAnnotations().containsKey("updateTimestamp")
				|| StringUtils.isEmpty(meta.getAnnotations().get("updateTimestamp").toString())) {
			appDetail.setUpdateTime(meta.getCreationTimestamp());
		} else {
			appDetail.setUpdateTime(meta.getAnnotations().get("updateTimestamp").toString());
		}
		appDetail.setInstance(dep.getSpec().getReplicas());
		appDetail.setOwner(meta.getLabels().get("nephele/user").toString());
		appDetail.setHostName(dep.getSpec().getTemplate().getSpec().getHostname());
		appDetail.setRestartPolicy(dep.getSpec().getTemplate().getSpec().getRestartPolicy());
		Map<String, Object> labels = new HashMap<String, Object>();
		for (Map.Entry<String, Object> m : meta.getLabels().entrySet()) {
			if (m.getKey().indexOf("nephele/") > 0) {
				labels.put(m.getKey(), m.getValue());
			}
		}

		if (meta.getAnnotations() != null && meta.getAnnotations().containsKey("nephele/status")) {
			Integer state = Integer.valueOf(meta.getAnnotations().get("nephele/status").toString());
			switch (state) {
			case 3:
				if (dep.getStatus().getReplicas() == dep.getStatus().getAvailableReplicas()) {
					appDetail.setStatus(Constant.SERVICE_START);
				} else {
					appDetail.setStatus(Constant.SERVICE_STARTING);
				}
				break;
			case 2:
				if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
					appDetail.setStatus(Constant.SERVICE_STOPPING);
				} else {
					appDetail.setStatus(Constant.SERVICE_STOP);
				}
				break;
			default:
				if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
					appDetail.setStatus(Constant.SERVICE_START);
				} else {
					appDetail.setStatus(Constant.SERVICE_STOP);
				}
				break;
			}
		} else {
			if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
				appDetail.setStatus(Constant.SERVICE_START);
			} else {
				appDetail.setStatus(Constant.SERVICE_STOP);
			}
		}
		if (meta.getAnnotations() != null && meta.getAnnotations().containsKey("nephele/annotation")) {
			appDetail.setAnnotation(meta.getAnnotations().get("nephele/annotation").toString());
		}
		//labels
		Map<String, Object> labelMap = new HashMap<String, Object>();
        String labs = null;
        if(dep.getMetadata().getAnnotations() != null && dep.getMetadata().getAnnotations().containsKey("nephele/labels")){
            labs = dep.getMetadata().getAnnotations().get("nephele/labels").toString();
        }
        if (!StringUtils.isEmpty(labs)) {
            String[] arrLabel = labs.split(",");
            for (String l : arrLabel) {
                String[] tmp = l.split("=");
                labelMap.put(tmp[0], tmp[1]);
            }
            appDetail.setLabels(labelMap);
        }

		if (serviceList.getItems() != null && serviceList.getItems().size() > 0) {
			com.harmonycloud.k8s.bean.Service service = serviceList.getItems().get(0);
			appDetail.setClusterIP(service.getSpec().getClusterIP());
			appDetail.setServiceAddress(service.getMetadata().getNamespace() + "." + service.getMetadata().getName());
			appDetail.setInternalPorts(service.getSpec().getPorts());
			if (StringUtils.isEmpty(service.getSpec().getSessionAffinity())) {
				appDetail.setSessionAffinity("false");
			} else {
				appDetail.setSessionAffinity(service.getSpec().getSessionAffinity());
			}
		}

		appDetail.setAutoScalingHistory(hapEve.getItems());

		if (hpaList.getItems().size() == 0) {
			appDetail.setAutoScaling(false);
		} else {
			HorizontalPodAutoscaler hAutoscaler = hpaList.getItems().get(0);
			List<Integer> range = new ArrayList<Integer>();

			// 接口版本有改动 targetCpu：targetCPUUtilizationPercentage
			Integer targetCpu = hAutoscaler.getSpec().getTargetCPUUtilizationPercentage();
			range.add(hAutoscaler.getSpec().getMinReplicas());
			range.add(hAutoscaler.getSpec().getMaxReplicas());
			if (targetCpu != null) {
				HpAutoScaling autoScaling = new HpAutoScaling(targetCpu, range,
						hAutoscaler.getStatus().getCurrentCPUUtilizationPercentage(),
						hAutoscaler.getStatus().getLastScaleTime());
				appDetail.setAutoScaling(autoScaling);
			} else {
				HpAutoScaling autoScaling = new HpAutoScaling(
						hAutoscaler.getSpec().getCpuUtilization().getTargetPercentage(), range,
						hAutoscaler.getStatus().getCurrentCPUUtilizationPercentage(),
						hAutoscaler.getStatus().getLastScaleTime());
				appDetail.setAutoScaling(autoScaling);
			}

		}

		List<PodDetail> pods = new ArrayList<PodDetail>();
		for (int i = 0; i < podList.getItems().size(); i++) {
			Pod pod = podList.getItems().get(i);
			PodDetail podDetail = new PodDetail(pod.getMetadata().getName(), pod.getMetadata().getNamespace(),
					pod.getStatus().getPhase(), pod.getStatus().getPodIP(), pod.getStatus().getHostIP(),
					pod.getStatus().getStartTime());
			pods.add(podDetail);
		}
		appDetail.setPodList(pods);
		List<EventDetail> events = new ArrayList<EventDetail>();
		for (int i = 0; i < eventList.getItems().size(); i++) {
			Event event = eventList.getItems().get(i);
			EventDetail eventDetail = new EventDetail(event.getReason(), event.getMessage(), event.getFirstTimestamp(),
					event.getLastTimestamp(), event.getCount(), event.getType());
			events.add(eventDetail);
		}
		appDetail.setEvents(events);
		return appDetail;
	}

	public static List<PodDetail> podListConvert(PodList podList ,String tag) throws Exception {
		List<Pod> pods = podList.getItems();
		List<PodDetail> res = new ArrayList<PodDetail>();
		for (int i = 0; i < pods.size(); i++) {
			PodDetail podDetail = new PodDetail(pods.get(i).getMetadata().getName(),
					pods.get(i).getMetadata().getNamespace(), pods.get(i).getStatus().getPhase(),
					pods.get(i).getStatus().getPodIP(), pods.get(i).getStatus().getHostIP(),
					pods.get(i).getStatus().getStartTime());
			podDetail.setTag(tag);
			List<ContainerWithStatus> containers = new ArrayList<ContainerWithStatus>();
			List<ContainerStatus> containerStatues = pods.get(i).getStatus().getContainerStatuses();
			int flag = 0;
			if(containerStatues!=null){
				for (ContainerStatus cs : containerStatues) {
					ContainerWithStatus containerWithStatus = new ContainerWithStatus();
					containerWithStatus.setName(cs.getName());
					containerWithStatus.setRestartCount(cs.getRestartCount());
					if (cs.getState().getWaiting() != null) {
						flag = 1;
						containerWithStatus.setState(Constant.WAITING);
						containerWithStatus.setReason(cs.getState().getWaiting().getReason());
						containerWithStatus.setMessage(cs.getState().getWaiting().getMessage());
					} else if (cs.getState().getRunning() != null) {
						containerWithStatus.setState(Constant.RUNNING);
						containerWithStatus.setStartedAt(cs.getState().getRunning().getStartedAt());
					} else {
						if(flag != 1){
							flag = 2;
						}
						containerWithStatus.setState(Constant.TERMINATED);
						containerWithStatus.setExitCode(cs.getState().getTerminated().getExitCode());
						containerWithStatus.setSignal(cs.getState().getTerminated().getSignal());
						containerWithStatus.setMessage(cs.getState().getTerminated().getMessage());
						containerWithStatus.setReason(cs.getState().getTerminated().getReason());
						containerWithStatus.setStartedAt(cs.getState().getTerminated().getStartedAt());
						containerWithStatus.setFinishedAt(cs.getState().getTerminated().getFinishedAt());
					}
					containers.add(containerWithStatus);
				}
			}
			podDetail.setContainers(containers);
			if(flag == 1){
				podDetail.setStatus(Constant.WAITING);
			}else if(flag == 2){
				podDetail.setStatus(Constant.TERMINATED);
			}
			res.add(podDetail);
		}
		return res;
	}

	@SuppressWarnings("null")
	public static String convertExpression(Deployment dep, String name) throws Exception {
		Map<String, Object> selector = dep.getSpec().getSelector().getMatchLabels();
		if (selector == null && selector.isEmpty()) {
			selector.put("app", name);
		}

		// 获取所有的map的key和value，拼接成字符串
		String selExpression = "";
		for (Map.Entry<String, Object> m : selector.entrySet()) {
			selExpression += m.getKey() + '=' + m.getValue() + ',';
		}
		selExpression = selExpression.substring(0, selExpression.length() - 1);
		return selExpression;
	}

	public static List<EventDetail> convertPodEvent(List<Event> events) throws Exception {
		List<EventDetail> res = new ArrayList<EventDetail>();
		for (Event event : events) {
			EventDetail eventDetail = new EventDetail(event.getReason(), event.getMessage(), event.getFirstTimestamp(),
					event.getLastTimestamp(), event.getCount(), event.getType());
			eventDetail.setInvolvedObject(event.getInvolvedObject());
			res.add(eventDetail);
		}
		return res;
	}

	public static List<EventDetail> sortByDesc(List<EventDetail> list) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		for (EventDetail detail : list) {
			Long startTime = sdf.parse(detail.getFirstTimestamp()).getTime();
			Long endTime = sdf.parse(detail.getLastTimestamp()).getTime();
			Long interval = endTime - startTime;
			interval = interval / 1000;
			if (interval < 60) {
				detail.setSpan(interval);
				detail.setSpanMetric(Constant.SECONDS);
			} else if (interval >= 60 && interval < 3600) {
				detail.setSpan(interval / 60);
				detail.setSpanMetric(Constant.MINUTES);
			} else if (interval >= 3600 && interval < 86400) {
				detail.setSpan(interval / 3600);
				detail.setSpanMetric(Constant.HOURS);
			} else {
				detail.setSpan(interval / 86400);
				detail.setSpanMetric(Constant.DAYS);
			}
		}

		// 对last时间进行倒序
		Collections.sort(list, new Comparator<EventDetail>() {

			@Override
			public int compare(EventDetail o1, EventDetail o2){
				try {
					return Long.valueOf(sdf.parse(o2.getLastTimestamp()).getTime()).compareTo(Long.valueOf(sdf.parse(o1.getLastTimestamp()).getTime()));
				} catch (ParseException e) {
					e.printStackTrace();
					return 0;
				}
			}

		});
		return list;
	}

	@SuppressWarnings("unchecked")
	public static List<ContainerOfPodDetail> convertContainer(Deployment deployment) throws Exception {
		List<ContainerOfPodDetail> res = new ArrayList<ContainerOfPodDetail>();
		List<Container> containers = deployment.getSpec().getTemplate().getSpec().getContainers();
		if (containers != null && containers.size() > 0) {
			for (Container ct : containers) {
				ContainerOfPodDetail cOfPodDetail = new ContainerOfPodDetail(ct.getName(), ct.getImage(),
						ct.getLivenessProbe(), ct.getReadinessProbe(), ct.getPorts(), ct.getArgs(), ct.getEnv(),
						ct.getCommand());
				
				if (ct.getResources().getLimits() != null) {
					String pattern = ".*m.*";
					Pattern r = Pattern.compile(pattern);
					String cpu = ((Map<Object, Object>) ct.getResources().getLimits()).get("cpu").toString();
					Matcher m = r.matcher(cpu);
					if (!m.find()) {
						((Map<Object, Object>) ct.getResources().getLimits()).put("cpu",
								Integer.valueOf(cpu) * 1000 + "m");
					}
					cOfPodDetail.setResource(((Map<String, Object>) ct.getResources().getLimits()));
				} else {
					cOfPodDetail.setResource(null);
				}

				List<VolumeMount> volumeMounts = ct.getVolumeMounts();
				List<VolumeMountExt> vms = new ArrayList<VolumeMountExt>();
				if (volumeMounts != null && volumeMounts.size() > 0) {
					for (VolumeMount vm : volumeMounts) {
						VolumeMountExt vmExt = new VolumeMountExt(vm.getName(), vm.getReadOnly(), vm.getMountPath(),
								vm.getSubPath());
						for (Volume volume : deployment.getSpec().getTemplate().getSpec().getVolumes()) {
							if (vm.getName().equals(volume.getName())) {
								if (volume.getSecret() != null) {
									vmExt.setType("secret");
								} else if (volume.getPersistentVolumeClaim() != null) {
									vmExt.setType("nfs");
								} else if (volume.getEmptyDir() != null) {
									vmExt.setType("emptyDir");
									if(volume.getEmptyDir() != null){
										vmExt.setEmptyDir(volume.getEmptyDir().getMedium());
									}else{
										vmExt.setEmptyDir(null);
									}
									
									if (vm.getName().indexOf("logdir") == 0) {
										vmExt.setType("logDir");
									}
                                } else if (volume.getConfigMap() != null) {
                                    Map<String, Object> configMap = new HashMap<String, Object>();
                                    configMap.put("name", volume.getConfigMap().getName());
                                    configMap.put("path", vm.getMountPath());
                                    vmExt.setType("configMap");
                                    vmExt.setConfigMapName(volume.getConfigMap().getName());
                                }else if (volume.getHostPath() != null) {
									vmExt.setType("hostPath");
									vmExt.setHostPath(volume.getHostPath().getPath());
									if (vm.getName().indexOf("logdir") == 0) {
										vmExt.setType("logDir");
									}
                                }
								if (vmExt.getReadOnly() == null) {
									vmExt.setReadOnly(false);
								}
								vms.add(vmExt);
								break;
							}
						}
					}
					cOfPodDetail.setStorage(vms);
				}
				res.add(cOfPodDetail);
			}
		}
		return res;
	}

	public static List<Map<String, Object>> convertAppList(DeploymentList depList) throws Exception {
		List<Deployment> deps = depList.getItems();
		List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
		if (!deps.isEmpty()) {
			for (int i = 0; i < deps.size(); i++) {
				Deployment dep = deps.get(i);
				Map<String, Object> tMap = new HashMap<String, Object>();
				tMap.put("name", dep.getMetadata().getName());
				Map<String, Object> labelMap = new HashMap<String, Object>();
				String labels = null;
				if(dep.getMetadata().getAnnotations() != null && dep.getMetadata().getAnnotations().containsKey("nephele/labels")){
					labels = dep.getMetadata().getAnnotations().get("nephele/labels").toString();
				}
				if (!StringUtils.isEmpty(labels)) {
					String[] arrLabel = labels.split(",");
					for (String l : arrLabel) {
						String[] tmp = l.split("=");
						labelMap.put(tmp[0], tmp[1]);
					}
					tMap.put("labels", labelMap);
				}
				String status = null;
				if ( dep.getMetadata().getAnnotations() != null &&  dep.getMetadata().getAnnotations().containsKey("nephele/status")) {
					status = dep.getMetadata().getAnnotations().get("nephele/status").toString();
				}
						
				if (!StringUtils.isEmpty(status)) {
					switch (Integer.valueOf(status)) {
					case 3:
						if (dep.getStatus().getReplicas()!=null&&dep.getStatus().getReplicas() > 0
								&& (dep.getStatus().getAvailableReplicas() == dep.getStatus().getReplicas())) {
							tMap.put("status", Constant.SERVICE_START);
						} else {
							tMap.put("status", Constant.SERVICE_STARTING);
						}
						break;
					case 2:
						if (dep.getStatus().getAvailableReplicas() != null
								&& dep.getStatus().getAvailableReplicas() > 0) {
							tMap.put("status", Constant.SERVICE_STOPPING);
						} else {
							tMap.put("status", Constant.SERVICE_STOP);
						}
						break;
					default:
						if (dep.getStatus().getAvailableReplicas() != null
								&& dep.getStatus().getAvailableReplicas() > 0) {
							tMap.put("status", Constant.SERVICE_START);
						} else {
							tMap.put("status", Constant.SERVICE_STOP);
						}
						break;
					}
				} else {
					if (dep.getStatus().getAvailableReplicas() != null
							&& dep.getStatus().getAvailableReplicas() > 0) {
						tMap.put("status", Constant.SERVICE_START);
					} else {
						tMap.put("status", Constant.SERVICE_STOP);
					}
				}
				if (dep.getMetadata().getAnnotations() != null && dep.getMetadata().getAnnotations().containsKey("deployment.kubernetes.io/revision")) {
					tMap.put("version", "v" + dep.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision"));
				}
				List<String> img = new ArrayList<String>();
				List<Container> containers = dep.getSpec().getTemplate().getSpec().getContainers();
				for (Container container : containers) {
					img.add(container.getImage());
				}
				tMap.put("img", img);
				tMap.put("instance", dep.getSpec().getReplicas());
				tMap.put("createTime", dep.getMetadata().getCreationTimestamp());
				tMap.put("namespace", dep.getMetadata().getNamespace());
				tMap.put("selector", dep.getSpec().getSelector());
				res.add(tMap);
			}
		}
		return res;
	}

	public static Deployment convertAppCreate(DeploymentDetailDto detail, String userName) throws Exception {
		Deployment dep = new Deployment();
		ObjectMeta meta = new ObjectMeta();
		meta.setName(detail.getName());
		Map<String, Object> lmMap = new HashMap<String, Object>();
		lmMap.put("nephele/user", userName);
		meta.setLabels(lmMap);
		Map<String, Object> anno = new HashMap<String, Object>();
		String annotation=detail.getAnnotation();
		if( !StringUtils.isEmpty(annotation) && annotation.lastIndexOf(",") == 0){
		    annotation=annotation.substring(0, annotation.length()-1);
		}

		anno.put("nephele/annotation", annotation == null ? "" : annotation);
		anno.put("nephele/status", Constant.STARTING);
		anno.put("nephele/replicas", detail.getInstance());

		anno.put("nephele/labels", detail.getLabels() == null ? "" : detail.getLabels());
		meta.setAnnotations(anno);
		dep.setMetadata(meta);
		DeploymentSpec depSpec = new DeploymentSpec();
		depSpec.setReplicas(Integer.valueOf(detail.getInstance()));
		DeploymentStrategy strategy = new DeploymentStrategy();
		strategy.setType("Recreate");
		depSpec.setStrategy(strategy);
		dep.setSpec(depSpec);
		List<CreateContainerDto> containers = detail.getContainers();
		List<Container> cs = new ArrayList<Container>();
		List<Volume> volumes = new ArrayList<Volume>();
		if (!containers.isEmpty()) {
			for (CreateContainerDto c : containers) {
				Container container = new Container();
				container.setName(c.getName());
				if (StringUtils.isEmpty(c.getTag())) {
					container.setImage(c.getImg());
				} else {
					container.setImage(c.getImg() + ":" + c.getTag());
				}
				container.setCommand(c.getCommand());
				container.setArgs(c.getArgs());
				if (c.getLivenessProbe() != null) {
					Probe lProbe = new Probe();
					HTTPGetAction httpGet = new HTTPGetAction();
					TCPSocketAction tcp=new TCPSocketAction();
					if (c.getLivenessProbe().getHttpGet() != null) {
						httpGet.setPath(c.getLivenessProbe().getHttpGet().getPath());
						if (c.getLivenessProbe().getHttpGet().getPort() == 0) {
						    httpGet.setPort(80);
						} else {
							//lProbe.getHttpGet().setPort(c.getLivenessProbe().getHttpGet().getPort());
							httpGet.setPort(c.getLivenessProbe().getHttpGet().getPort());
						}
						lProbe.setHttpGet(httpGet);
					}
					
					if (c.getLivenessProbe().getExec() != null ) {
                        if(c.getLivenessProbe().getExec().getCommand()!=null){
                            ExecAction exec= new ExecAction();
                            exec.setCommand(c.getLivenessProbe().getExec().getCommand());
                            lProbe.setExec(exec);
                        }
                    }
					
					if (c.getLivenessProbe().getTcpSocket() != null) {
						if (c.getLivenessProbe().getTcpSocket().getPort() == 0) {
						    tcp.setPort(80);
						} else {
							tcp.setPort(c.getLivenessProbe().getTcpSocket().getPort());
						}
						lProbe.setTcpSocket(tcp);
					}
					lProbe.setInitialDelaySeconds(c.getLivenessProbe().getInitialDelaySeconds());
					lProbe.setTimeoutSeconds(c.getLivenessProbe().getTimeoutSeconds());
					lProbe.setPeriodSeconds(c.getLivenessProbe().getPeriodSeconds());
					lProbe.setSuccessThreshold(c.getLivenessProbe().getSuccessThreshold());
					lProbe.setFailureThreshold(c.getLivenessProbe().getFailureThreshold());
					container.setLivenessProbe(lProbe);
				}

                if (c.getReadinessProbe() != null) {
                    Probe rProbe = new Probe();
                    HTTPGetAction httpGet = new HTTPGetAction();
                    TCPSocketAction tcp=new TCPSocketAction();
                    if (c.getReadinessProbe().getHttpGet() != null) {
                    	httpGet.setPath(c.getReadinessProbe().getHttpGet().getPath());
                        if (c.getReadinessProbe().getHttpGet().getPort() == 0) {
                            rProbe.getHttpGet().setPort(80);
                        } else {
                            // rProbe.getHttpGet().setPort(c.getReadinessProbe().getHttpGet().getPort());
                            httpGet.setPort(c.getReadinessProbe().getHttpGet().getPort());
                        }
                        rProbe.setHttpGet(httpGet);
                    }

                    if (c.getReadinessProbe().getExec() != null) {
                        if (c.getReadinessProbe().getExec().getCommand() != null) {
                            ExecAction exec = new ExecAction();
                            exec.setCommand(c.getReadinessProbe().getExec().getCommand());
                            rProbe.setExec(exec);
                        }
                    }

                    if (c.getReadinessProbe().getTcpSocket() != null) {
                        if (c.getReadinessProbe().getTcpSocket().getPort() == 0) {
                            tcp.setPort(80);
                        } else {
                            // rProbe.getTcpSocket().setPort(c.getReadinessProbe().getTcpSocket().getPort());
                            tcp.setPort(c.getReadinessProbe().getTcpSocket().getPort());
                        }
                        rProbe.setTcpSocket(tcp);
                    }
					rProbe.setInitialDelaySeconds(c.getReadinessProbe().getInitialDelaySeconds());
					rProbe.setTimeoutSeconds(c.getReadinessProbe().getTimeoutSeconds());
					rProbe.setPeriodSeconds(c.getReadinessProbe().getPeriodSeconds());
					rProbe.setSuccessThreshold(c.getReadinessProbe().getSuccessThreshold());
					rProbe.setFailureThreshold(c.getReadinessProbe().getFailureThreshold());
					container.setReadinessProbe(rProbe);
				}

				if (c.getPorts() != null && !c.getPorts().isEmpty()) {
					List<ContainerPort> ps = new ArrayList<ContainerPort>();
					for (CreatePortDto p : c.getPorts()) {
						ContainerPort port = new ContainerPort();
						port.setContainerPort(Integer.valueOf(p.getPort()));
						port.setProtocol(p.getProtocol());
						ps.add(port);
					}
					container.setPorts(ps);
				}

				if (c.getEnv() != null && !c.getEnv().isEmpty()) {
					List<EnvVar> envVars = new ArrayList<EnvVar>();
					for (CreateEnvDto env : c.getEnv()) {
						EnvVar eVar = new EnvVar();
						eVar.setName(env.getKey());
						eVar.setValue(env.getValue());
						envVars.add(eVar);
					}
					container.setEnv(envVars);
				}

				if (c.getResource() != null) {
					ResourceRequirements limit = new ResourceRequirements();
					Map<String, String> res = new HashMap<String, String>();
					String regEx="[^0-9]";
					Pattern p = Pattern.compile(regEx);
					Matcher m = p.matcher(c.getResource().getCpu());
					String result = m.replaceAll("").trim();
					res.put("cpu", result + "m");
//					res.put("cpu", c.getResource().getCpu());
					Matcher mm = p.matcher(c.getResource().getMemory());
                    String resultm = mm.replaceAll("").trim();
                    res.put("memory", resultm + "Mi");
					/*res.put("memory", c.getResource().getMemory() + "Mi");*/
					limit.setLimits(res);
					container.setResources(limit);
				}

				List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>();
				container.setVolumeMounts(volumeMounts);
				if (c.getStorage() != null && !c.getStorage().isEmpty()) {
					Map<String, Object> volFlag = new HashMap<String, Object>();
					for (CreateVolumeDto vm : c.getStorage()) {
						if(vm.getType()!=null){
							switch (vm.getType()) {
							case Constant.VOLUME_TYPE_PV:
								if (!volFlag.containsKey(vm.getPvcName())) {
									PersistentVolumeClaimVolumeSource pvClaim = new PersistentVolumeClaimVolumeSource();
									volFlag.put(vm.getPvcName(), vm.getPvcName());
									if (vm.getReadOnly().equals("true")) {
										pvClaim.setReadOnly(true);
									}
									if (vm.getReadOnly().equals("false")) {
										pvClaim.setReadOnly(false);
									}
									pvClaim.setClaimName(vm.getPvcName());
									Volume vol = new Volume();
									vol.setPersistentVolumeClaim(pvClaim);
									vol.setName(vm.getPvcName());
									volumes.add(vol);
								}
								VolumeMount volm = new VolumeMount();
								volm.setName(vm.getPvcName());
								volm.setReadOnly(Boolean.parseBoolean(vm.getReadOnly()));
								volm.setMountPath(vm.getPath());
								volumeMounts.add(volm);
								container.setVolumeMounts(volumeMounts);
								break;
							case Constant.VOLUME_TYPE_GITREPO:
								if (!volFlag.containsKey(vm.getGitUrl())) {
									volFlag.put(vm.getGitUrl(), RandomNum.randomNumber(8));
									Volume gitRep = new Volume();
									gitRep.setName(volFlag.get(vm.getGitUrl()).toString());
									GitRepoVolumeSource gp = new GitRepoVolumeSource();
									gp.setRepository(vm.getGitUrl());
									gp.setRevision(vm.getRevision());
									gitRep.setGitRepo(gp);
									volumes.add(gitRep);
								}
								VolumeMount volmg = new VolumeMount();
								volmg.setName(volFlag.get(vm.getGitUrl()).toString());
								volmg.setReadOnly(Boolean.parseBoolean(vm.getReadOnly()));
								volmg.setMountPath(vm.getPath());
								volumeMounts.add(volmg);
								container.setVolumeMounts(volumeMounts);
								break;
							case Constant.VOLUME_TYPE_EMPTYDIR:
								if (!volFlag.containsKey(Constant.VOLUME_TYPE_EMPTYDIR+vm.getEmptyDir()==null ? "": vm.getEmptyDir())) {
									volFlag.put(Constant.VOLUME_TYPE_EMPTYDIR+vm.getEmptyDir()==null ? "": vm.getEmptyDir(), RandomNum.getRandomString(8));
									Volume empty = new Volume();
									empty.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR+vm.getEmptyDir()==null ? "": vm.getEmptyDir()).toString());
									EmptyDirVolumeSource ed =new EmptyDirVolumeSource();
									if(vm.getEmptyDir() != null && "Memory".equals(vm.getEmptyDir())){
										ed.setMedium(vm.getEmptyDir());//Memory
									}
									empty.setEmptyDir(ed);
									volumes.add(empty);
								}
								VolumeMount volme = new VolumeMount();
								volme.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR+vm.getEmptyDir()==null ? "": vm.getEmptyDir()).toString());
								volme.setMountPath(vm.getPath());
								volumeMounts.add(volme);
								container.setVolumeMounts(volumeMounts);
								break;
							case Constant.VOLUME_TYPE_HOSTPASTH:
								if (!volFlag.containsKey(Constant.VOLUME_TYPE_HOSTPASTH+vm.getHostPath())) {
									volFlag.put(Constant.VOLUME_TYPE_HOSTPASTH+vm.getHostPath(), RandomNum.getRandomString(8));
									Volume empty = new Volume();
									empty.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH+vm.getHostPath()).toString());
									HostPath hp =new HostPath();
									hp.setPath(vm.getHostPath());
									empty.setHostPath(hp);
									volumes.add(empty);
								}
								VolumeMount volmh = new VolumeMount();
								volmh.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH+vm.getHostPath()).toString());
								volmh.setMountPath(vm.getPath());
								volumeMounts.add(volmh);
								container.setVolumeMounts(volumeMounts);
								break;
							default:
								break;
							}
						}
					}
				}

				if (!StringUtils.isEmpty(c.getLog())) {
					Volume emp = new Volume();
					emp.setName("logdir" + c.getName());
					EmptyDirVolumeSource ed = new EmptyDirVolumeSource();
					ed.setMedium("");
					emp.setEmptyDir(ed);
					volumes.add(emp);
					VolumeMount volm = new VolumeMount();
					volm.setName("logdir" + c.getName());
					volm.setMountPath(c.getLog());
					volumeMounts.add(volm);
					container.setVolumeMounts(volumeMounts);
				}

				if (c.getConfigmap() != null && c.getConfigmap().size()>0) {
					for (CreateConfigMapDto cm : c.getConfigmap()) {
						if (cm != null && !StringUtils.isEmpty(cm.getPath())) {
							String filename = cm.getFile();
							if(cm.getPath().contains("/")){
								int in = cm.getPath().lastIndexOf("/");
								filename = cm.getPath().substring(in+1, cm.getPath().length());
							}
							Volume cMap = new Volume();
							cMap.setName((cm.getFile() + "v" + cm.getTag()).replace(".", "-"));
							ConfigMapVolumeSource coMap = new ConfigMapVolumeSource();
							coMap.setName(detail.getName() + c.getName());
							List<KeyToPath> items=new LinkedList<KeyToPath>();
							KeyToPath key=new KeyToPath();
							key.setKey(cm.getFile()+"v"+cm.getTag());
							key.setPath(filename);
							items.add(key);
							coMap.setItems(items);
							cMap.setConfigMap(coMap);
							volumes.add(cMap);
							VolumeMount volm = new VolumeMount();
							volm.setName((cm.getFile() + "v" + cm.getTag()).replace(".", "-"));
							volm.setMountPath(cm.getPath());
							// volm.setMountPath(c.getConfigmap().getPath()+"/"+c.getConfigmap().getFile());
							volm.setSubPath(filename);
							volumeMounts.add(volm);
							container.setVolumeMounts(volumeMounts);
						}
					}
				}

				if (!StringUtils.isEmpty(detail.getLogService()) && detail.getLogService().equals("false")) {
					Volume emp = new Volume();
					emp.setName("logdir" + c.getName());
					EmptyDirVolumeSource ed = new EmptyDirVolumeSource();
					ed.setMedium("");
					emp.setEmptyDir(ed);
					volumes.add(emp);
					VolumeMount volm = new VolumeMount();
					volm.setName("logdir" + c.getName());
					volm.setMountPath(detail.getLogPath());
					volumeMounts.add(volm);
					container.setVolumeMounts(volumeMounts);
				}
				cs.add(container);
			}
		}
		PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
		PodSpec podSpec = new PodSpec();
		podSpec.setContainers(cs);
		podSpec.setRestartPolicy(detail.getRestartPolicy());
		podSpec.setHostname(detail.getHostName());
		//node Selector
		Map<String, Object> nodeselector = new HashMap<>();
        if (detail.getNodeSelector() != null && !detail.getNodeSelector().equals("")) {
            if (detail.getNodeSelector().contains(",")) {
                String[] ns = detail.getNodeSelector().split(",");
                for (String n : ns) {
                    if (n.contains("=")) {
                        String[] s = n.split("=");
                        nodeselector.put(s[0], s[1]);
                    }
                }
            }else{
                if (detail.getNodeSelector().contains("=")) {
                    String[] s = detail.getNodeSelector().split("=");
                    nodeselector.put(s[0], s[1]);
                }
            }
        }
        podSpec.setNodeSelector(nodeselector);
        List<LocalObjectReference> imagePullSecrets = new ArrayList<>();
        LocalObjectReference e = new LocalObjectReference();
        e.setName(userName+"-secret");
        imagePullSecrets.add(e);
        podSpec.setImagePullSecrets(imagePullSecrets);
		if (volumes.size() > 0) {
			podSpec.setVolumes(volumes);
		}

		ObjectMeta metadata = new ObjectMeta();
		Map<String, Object> labels = new HashMap<>();
		labels.put("app", detail.getName());
		List<LocalObjectReference> lors = new ArrayList<LocalObjectReference>();
		LocalObjectReference lor = new LocalObjectReference();
		lor.setName(userName + "-secret");
		lors.add(lor);
		podSpec.setImagePullSecrets(lors);
		if (!StringUtils.isEmpty(detail.getLabels())) {
			String[] ls = detail.getLabels().split(",");
			for (String label : ls) {
				String[] tmp = label.split("=");
				labels.put(tmp[0], tmp[1]);
				lmMap.put(tmp[0], tmp[1]);
				meta.setLabels(lmMap);
			}
		}
		//labels-QOS
        if(detail.getLabels()!=null){
            if(detail.getLabels().contains(",")){
                String[] labs = detail.getLabels().split(",");
                if (labs != null && labs.length > 0) {
                    for (String s : labs) {
                        if (s.contains("qos") && s.contains("=")) {
                            labels.put("qos",s.split("=")[1]);
                        }
                    }
                }
            }else{
                if (detail.getLabels().contains("qos")&& detail.getLabels().contains("=")) {
                    labels.put("qos",detail.getLabels().split("=")[1]);
                }
            }
        }
		metadata.setLabels(labels);
		//annotations-QOS
		Map<String, Object> metadataanno = new HashMap<>();
		if(annotation!=null){
		    if(annotation.contains(",")){
                String[] qos = annotation.split(",");
                if (qos != null && qos.length > 0) {
                    for (String s : qos) {
                        if (s.contains("ingress")&& s.contains("=")) {
                            metadataanno.put("kubernetes.io/ingress-bandwidth",s.split("=")[1]);
                        }
                        if (s.contains("egress") && s.contains("=")) {
                            metadataanno.put("kubernetes.io/egress-bandwidth",s.split("=")[1]);
                        }
                    }
                }
            }else{
                if (annotation.contains("ingress")&& annotation.contains("=")) {
                    metadataanno.put("kubernetes.io/ingress-bandwidth",annotation.split("=")[1]);
                }
                if (annotation.contains("egress") && annotation.contains("=")) {
                    metadataanno.put("kubernetes.io/egress-bandwidth",annotation.split("=")[1]);
                }
            }
		}
		metadata.setAnnotations(metadataanno);
		podTemplateSpec.setMetadata(metadata);
		podTemplateSpec.setSpec(podSpec);
		depSpec.setTemplate(podTemplateSpec);
		dep.setSpec(depSpec);
		return dep;
	}
	
	public static Service convertAppCreateOfService(DeploymentDetailDto detail) throws Exception {
		Service service = new Service();
		ObjectMeta meta = new ObjectMeta();
		meta.setName(detail.getName());
		Map<String, Object> labels = new HashMap<String, Object>();
		labels.put("app", detail.getName());
		meta.setLabels(labels);
		ServiceSpec ss = new ServiceSpec();
		Map<String, Object> selector = new HashMap<String, Object>();
		selector.put("app", detail.getName());
		ss.setSelector(selector);
		if (!StringUtils.isEmpty(detail.getClusterIP())) {
			ss.setClusterIP(detail.getClusterIP());
		}
		List<CreateContainerDto> containers = detail.getContainers();
		if (!containers.isEmpty()) {
			List<ServicePort> spList = new ArrayList<ServicePort>();
			for (CreateContainerDto c : containers) {
				 for (int i =0 ; i<c.getPorts().size();i++) {
					 CreatePortDto port = c.getPorts().get(i);
					 ServicePort sPort = new ServicePort();
					 if (StringUtils.isEmpty(port.getProtocol())) {
						 sPort.setProtocol("TCP");
					 } else {
						 sPort.setProtocol(port.getProtocol());
					 }
					 sPort.setPort(Integer.valueOf(port.getPort()));
					 sPort.setName(detail.getName()+"-"+c.getName()+"-port"+i);
					 spList.add(sPort);
				 }
			}
			ss.setPorts(spList);
		}
		service.setSpec(ss);
		service.setMetadata(meta);
		return service;
	}
	
	public static Map<String, Object> convertAppPut(Deployment dep, ServiceList svsList, List<UpdateContainer> newContainers, String name) throws Exception {
		Map<String, Container> ct = new HashMap<String, Container>();
		List<Container> containers = dep.getSpec().getTemplate().getSpec().getContainers();
		for (Container c : containers) {
			ct.put(c.getName(), c);
		}
		Map<String, Object> vtemp = new HashMap<String, Object>();
		List<Volume> volumes = new ArrayList<Volume>();
		List<ServicePort> ports = new ArrayList<ServicePort>();
		List<Container> newC = new ArrayList<Container>();
		for (UpdateContainer cc : newContainers) {//cc为新的
			Container container = ct.get(cc.getName());//container为旧的
			container.setImage(cc.getImg());
			if (cc.getResource() != null) {
				Map<String, String> res = new HashMap<String, String>();
				res.put("cpu", cc.getResource().getCpu());
				res.put("memory", cc.getResource().getMemory());
				container.getResources().setLimits(res);
			}
			if (!cc.getPorts().isEmpty()) {
				List<ContainerPort> ps = new ArrayList<ContainerPort>();
				for (CreatePortDto p : cc.getPorts()) {
					ContainerPort port = new ContainerPort();
					port.setContainerPort(Integer.valueOf(p.getContainerPort()));
					port.setProtocol(p.getProtocol());
					ps.add(port);
					container.setPorts(ps);
					ServicePort servicePort = new ServicePort();
					servicePort.setTargetPort(Integer.valueOf(p.getContainerPort()));
					servicePort.setPort(Integer.valueOf(p.getContainerPort()));
					servicePort.setProtocol(p.getProtocol());
					servicePort.setName(name + "-port"+ports.size());
					ports.add(servicePort);
				}
				
			}
			
			List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>();
			container.setVolumeMounts(volumeMounts);
			if (cc.getStorage() != null && !cc.getStorage().isEmpty()) {
				List<UpdateVolume> newVolume = cc.getStorage();
				for (int i =0 ;i < newVolume.size();i++) {
					UpdateVolume vol = newVolume.get(i);
					if (vol.getType().equals("secret")) {
						if (!vtemp.containsKey(vol.getName())) {
							vtemp.put(vol.getName(), vol.getName());
							SecretVolumeSource secret = new SecretVolumeSource();
							secret.setSecretName(vol.getName());
							Volume v = new Volume();
							v.setSecret(secret);
							v.setName(vol.getName());
							volumes.add(v);
						}
						VolumeMount volm = new VolumeMount();
						volm.setName(vol.getName());
						volm.setMountPath(vol.getMountPath());
						volm.setReadOnly(vol.getReadOnly().equals("true"));
						volumeMounts.add(volm);
					}
					
					if (vol.getType().equals("pv")) {
						if (!vtemp.containsKey(vol.getName())) {
							vtemp.put(vol.getName(), vol.getName());
							PersistentVolumeClaimVolumeSource pvc = new PersistentVolumeClaimVolumeSource();
							pvc.setClaimName(vol.getName());
							Volume v = new Volume();
							v.setPersistentVolumeClaim(pvc);
							v.setName(vol.getName());
							volumes.add(v);
						}
						
						VolumeMount volm = new VolumeMount();
						volm.setName(vol.getName());
						volm.setMountPath(vol.getMountPath());
						volm.setReadOnly(vol.getReadOnly().equals("true"));
						volumeMounts.add(volm);
					}
					
					if (vol.getType().equals("gitRepo")) {
						if (!vtemp.containsKey(vol.getGitUrl())) {
							vtemp.put(vol.getGitUrl(), RandomNum.randomNumber(8));
							GitRepoVolumeSource gitRepo = new GitRepoVolumeSource();
							gitRepo.setRepository(vol.getGitUrl());
							gitRepo.setRevision(vol.getRevision());
							Volume v = new Volume();
							v.setGitRepo(gitRepo);
							v.setName(vol.getGitUrl());
							volumes.add(v);
						}
						
						VolumeMount volm = new VolumeMount();
						volm.setName(vol.getGitUrl());
						volm.setMountPath(vol.getMountPath());
						volm.setReadOnly(vol.getReadOnly().equals("true"));
						volumeMounts.add(volm);
					}
					
					if (vol.getType().equals("logDir")) {
						if (!vtemp.containsKey(vol.getName())) {
							vtemp.put(vol.getName(), vol.getName());
							EmptyDirVolumeSource emp = new EmptyDirVolumeSource();
							emp.setMedium("");
							Volume v = new Volume();
							v.setEmptyDir(emp);
							v.setName(vol.getName());
							volumes.add(v);
						}
						
						VolumeMount volm = new VolumeMount();
						volm.setName(vol.getName());
						volm.setMountPath(vol.getMountPath());
						volm.setReadOnly(vol.getReadOnly().equals("true"));
						volumeMounts.add(volm);
					}
					
					if (vol.getType().equals("configMap")) {
						if (!vtemp.containsKey(vol.getName())) {
							vtemp.put(vol.getName(), vol.getName());
							ConfigMapVolumeSource con = new ConfigMapVolumeSource();
							con.setName("configmap" + name + cc.getName());//configmap name s
							Volume v = new Volume();
							v.setConfigMap(con);
							v.setName(vol.getName());
							volumes.add(v);
						}
						
						VolumeMount volm = new VolumeMount();
						volm.setName(vol.getName());
						volm.setMountPath(vol.getMountPath());
						volm.setSubPath(vol.getSubPath());
						volm.setReadOnly(vol.getReadOnly().equals("true"));
						volumeMounts.add(volm);
					}
				}
			}
			container.setCommand(cc.getCommand());
			container.setArgs(cc.getArgs());
			if (cc.getEnv() != null &&!cc.getEnv().isEmpty()) {
				List<EnvVar> envVars = new ArrayList<EnvVar>();
				for (CreateEnvDto env : cc.getEnv()) {
					EnvVar eVar = new EnvVar();
					eVar.setName(env.getKey());
					eVar.setValue(env.getValue());
					envVars.add(eVar);
				}
				container.setEnv(envVars);
			}
			newC.add(container);
		}

		dep.getSpec().getTemplate().getSpec().setContainers(newC);
        dep.getSpec().getTemplate().getSpec().setVolumes(volumes);
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String updateTime = sdf.format(now);
		Map<String, Object> anno = new HashMap<String, Object>();
		anno.put("updateTimestamp", updateTime);
		dep.getMetadata().setAnnotations(anno);
		Service svc = svsList.getItems().get(0);
		svc.getSpec().setPorts(ports);
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("dep", dep);
		res.put("service", svc);
		return res;
	}
	
	@SuppressWarnings("unchecked")
	public static List<VolumeListBean> convertVolumeList(DeploymentList depList, PersistentVolumeClaimList vol) throws Exception {
		List<VolumeListBean> vms = new ArrayList<VolumeListBean>();
		List<PersistentVolumeClaim> pvcs = vol.getItems();
		if (pvcs != null && pvcs.size() > 0) {
			for (PersistentVolumeClaim pvClaim : pvcs) {
				VolumeListBean temp = new VolumeListBean();
				temp.setName(pvClaim.getMetadata().getName());
				temp.setStatus(pvClaim.getStatus().getPhase());
				String ca = ((Map<String, Object>)pvClaim.getSpec().getResources().getRequests()).get("storage").toString();
				ca = ca.substring(0,ca.indexOf("Mi"));
				temp.setCapacity(Integer.valueOf(ca));
			    temp.setCreateTime(pvClaim.getMetadata().getCreationTimestamp());
			    temp.setNamespace(pvClaim.getMetadata().getNamespace());
			    if (pvClaim.getSpec().getAccessModes().get(0).equals("ReadWriteMany")) {
			    	temp.setReadOnly(false);
			    	temp.setMultiMount(true);
			    }
			    
			    if (pvClaim.getSpec().getAccessModes().get(0).equals("ReadWriteOnce")) {
			    	temp.setReadOnly(false);
			    	temp.setMultiMount(false);
			    }
			    
			    if (pvClaim.getSpec().getAccessModes().get(0).equals("ReadOnlyMany")) {
			    	temp.setReadOnly(true);
			    	temp.setMultiMount(true);
			    }
			    vms.add(temp);
			}
		}
		List<Deployment> deployments = depList.getItems();
		if (deployments != null && deployments.size() > 0) {
			for (Deployment dep : deployments) {
				List<Volume> volumes = dep.getSpec().getTemplate().getSpec().getVolumes();
				if (volumes != null && volumes.size() > 0) {
					for (VolumeListBean tmp : vms) {
						
						for(Volume v : volumes) {
							if (v.getPersistentVolumeClaim() != null && v.getPersistentVolumeClaim().getClaimName().equals(tmp.getName())) {
								List<String> bounds = new ArrayList<String>();
								bounds.add(dep.getMetadata().getName());
								tmp.setBounds(bounds);
							}
						}
					}
				}
			}
		}
		return vms;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> convertAppPod(Pod pod, List<Event> events) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("name", pod.getMetadata().getName());
		result.put("namespace", pod.getMetadata().getNamespace());
		PodStatus podStatus = pod.getStatus();
		result.put("status", podStatus.getPhase());
		result.put("startTime", podStatus.getStartTime());
		result.put("containerAmount", pod.getSpec().getContainers().size());
		result.put("ip", podStatus.getPodIP());
		result.put("hostIp", podStatus.getHostIP());
		result.put("createTime", pod.getMetadata().getCreationTimestamp());
		List<String> volumes = new ArrayList<String>();
		for (Volume volume : pod.getSpec().getVolumes()) {
			volumes.add(volume.getName());
		}
		List<Container> containers = pod.getSpec().getContainers();
		List<ContainerStatus> containerStatusList = podStatus.getContainerStatuses();
		List<Map<String, Object>> resContainer = new ArrayList<Map<String, Object>>();
		for (int i = 0; i<containers.size(); i++) {
			Container c = containers.get(i);
			Map<String, Object> tMap = new HashMap<String, Object>();
			tMap.put("name", c.getName());
			if (containerStatusList != null) {
				ContainerStatus containerStatus = containerStatusList.get(i);
				if (containerStatus.getState().getRunning() != null) {
					tMap.put("status", Constant.RUNNING);
					tMap.put("statusDetail", containerStatus.getState().getRunning());
				} else if (containerStatus.getState().getWaiting() != null) {
					tMap.put("status", Constant.WAITING);
					tMap.put("statusDetail", containerStatus.getState().getWaiting());
				} else if (containerStatus.getState().getTerminated() != null) {
					tMap.put("status", Constant.TERMINATED);
					tMap.put("statusDetail", containerStatus.getState().getTerminated());
				}
				tMap.put("restartTime", containerStatus.getRestartCount());
			} else {
				tMap.put("status", Constant.NOTCREATED);
				ContainerStateRunning runing = new ContainerStateRunning();
				runing.setStartedAt("");
				tMap.put("statusDetail", runing);
			}
			tMap.put("img", c.getImage());
			tMap.put("resource", c.getResources().getLimits());
			if (tMap.get("resource") != null) {
				Map<String, Object> resource = (Map<String, Object>) tMap.get("resource");
				if (resource.get("cpu").toString().indexOf("m") < 0) {
					resource.put("cpu", Integer.valueOf(resource.get("cpu").toString())*1000 + "m");
				}
			}
			tMap.put("livenessProbe", c.getLivenessProbe());
			tMap.put("readinessProbe", c.getReadinessProbe());
			tMap.put("ports", c.getPorts());
			tMap.put("args", c.getArgs());
			tMap.put("env", c.getEnv());
			tMap.put("command", c.getCommand());
			List<VolumeMountExt> vExts = new ArrayList<VolumeMountExt>();
			for (VolumeMount vMount : c.getVolumeMounts()) {
				for (Volume volume : pod.getSpec().getVolumes()) {
					VolumeMountExt ext = new VolumeMountExt();
					ext.setName(vMount.getName());
					ext.setReadOnly(vMount.getReadOnly());
					ext.setMountPath(vMount.getMountPath());
					ext.setSubPath(vMount.getSubPath());
					vExts.add(ext);
					if (vMount.getName().equals(volume.getName())) {
						if (volume.getSecret() != null) {
							ext.setType(Constant.TYPE_SECRET);
						} else if(volume.getPersistentVolumeClaim() != null) {
							ext.setType(Constant.TYPE_PV);
						} else if(volume.getGitRepo() != null){
							ext.setType(Constant.TYPE_GIT);
							ext.setGitUrl(volume.getGitRepo().getRepository());
							ext.setRevision(volume.getGitRepo().getRevision());
						}
						vExts.add(ext);
						break;
					}
				}
				tMap.put("storage", vExts);
			}
			resContainer.add(tMap);
		}
		result.put("containers", resContainer);
		result.put("events", events);
 		return result;
	}
	
	public static HorizontalPodAutoscaler convertHpa(String name, String namespace, Integer max, Integer min,
			Integer cpu) throws Exception {
		HorizontalPodAutoscaler hpAutoscaler = new HorizontalPodAutoscaler();

		// 设置hpa对象的metadata
		ObjectMeta meta = new ObjectMeta();
		Map<String, Object> labels = new HashMap<String, Object>();
		labels.put("app", name);
		meta.setName(name + "-hpa");
		meta.setLabels(labels);
		meta.setCreationTimestamp(null);
		meta.setDeletionGracePeriodSeconds(null);
		meta.setDeletionTimestamp(null);
		hpAutoscaler.setMetadata(meta);

		// 设置hpa对象的spec
		HorizontalPodAutoscalerSpec hpaSpec = new HorizontalPodAutoscalerSpec();
		hpaSpec.setTargetCPUUtilizationPercentage(cpu);
		hpaSpec.setMinReplicas(min);
		hpaSpec.setMaxReplicas(max);
		CrossVersionObjectReference targetRef = new CrossVersionObjectReference();
		targetRef.setKind(Constant.DEPLOYMENT);
		targetRef.setName(name);
		hpaSpec.setScaleTargetRef(targetRef);
		hpAutoscaler.setSpec(hpaSpec);
		return hpAutoscaler;
	}
	
	/**
	 * 组装 pod template*/
	public static PodTemplateSpec convertPodTemplate(String name, List<CreateContainerDto> containers, String label, String annotation, String userName, String type, String nodeSelector, String restartPolicy)throws Exception {
		//组装pod tempate
		PodTemplateSpec podTemplate = new PodTemplateSpec();
		
		//metadata
		ObjectMeta metadata = new ObjectMeta();
		Map<String, Object> labels = new HashMap<>();
	    labels.put(type, name);
	    if (!StringUtils.isEmpty(label)) {
	    	String[] ls ={};
	    	if(label.contains(",")){
				ls = label.split(",");
	    	}else{
	    		ls[0] = label;
	    	}
	    	for (String lab : ls) {
				String[] tmp = lab.split("=");
				labels.put(tmp[0], tmp[1]);
			}
	    }
	    metadata.setLabels(labels);
	    //annotations-QOS
	    Map<String, Object> metadataanno = new HashMap<>();
	    if(annotation!=null){
			if (annotation.contains(",")) {
				String[] qos = annotation.split(",");
				if (qos != null && qos.length > 0) {
					for (String s : qos) {
						if (s.contains("ingress") && s.contains("=")) {
							metadataanno.put("kubernetes.io/ingress-bandwidth", s.split("=")[1]);
						}
						if (s.contains("egress") && s.contains("=")) {
							metadataanno.put("kubernetes.io/egress-bandwidth", s.split("=")[1]);
						}
					}
				}
			} else {
				if (annotation.contains("ingress") && annotation.contains("=")) {
					metadataanno.put("kubernetes.io/ingress-bandwidth", annotation.split("=")[1]);
				}
				if (annotation.contains("egress") && annotation.contains("=")) {
					metadataanno.put("kubernetes.io/egress-bandwidth", annotation.split("=")[1]);
				}
			}
	    }
	    metadata.setAnnotations(metadataanno);
		podTemplate.setMetadata(metadata);
		//podSpec
		PodSpec podSpec = new PodSpec();
		List<Container> cs = new ArrayList<Container>();
	    List<Volume> volumes = new ArrayList<Volume>();
	    if(containers != null && containers.size() > 0 ){
	    	for(CreateContainerDto c : containers){
	    		Container container = new Container();
	            container.setName(c.getName());
	            if (StringUtils.isEmpty(c.getTag())) {
	              container.setImage(c.getImg());
	            } else {
	              container.setImage(c.getImg() + ":" + c.getTag());
	            }
	            container.setCommand(c.getCommand());
	            container.setArgs(c.getArgs());
	            if (c.getLivenessProbe() != null) {
	              Probe lProbe = new Probe();
	              HTTPGetAction httpGet = new HTTPGetAction();
	              TCPSocketAction tcp=new TCPSocketAction();
	              if (c.getLivenessProbe().getHttpGet() != null) {
	                httpGet.setPath(c.getLivenessProbe().getHttpGet().getPath());
	                if (c.getLivenessProbe().getHttpGet().getPort() == 0) {
	                    httpGet.setPort(80);
	                } else {
	                  //lProbe.getHttpGet().setPort(c.getLivenessProbe().getHttpGet().getPort());
	                  httpGet.setPort(c.getLivenessProbe().getHttpGet().getPort());
	                }
	                lProbe.setHttpGet(httpGet);
	              }
	              
	              if (c.getLivenessProbe().getExec() != null ) {
	                            if(c.getLivenessProbe().getExec().getCommand()!=null){
	                                ExecAction exec= new ExecAction();
	                                exec.setCommand(c.getLivenessProbe().getExec().getCommand());
	                                lProbe.setExec(exec);
	                            }
	                        }
	              
	              if (c.getLivenessProbe().getTcpSocket() != null) {
	                if (c.getLivenessProbe().getTcpSocket().getPort() == 0) {
	                    tcp.setPort(80);
	                } else {
	                  tcp.setPort(c.getLivenessProbe().getTcpSocket().getPort());
	                }
	                lProbe.setTcpSocket(tcp);
	              }
	              lProbe.setInitialDelaySeconds(c.getLivenessProbe().getInitialDelaySeconds());
	              lProbe.setTimeoutSeconds(c.getLivenessProbe().getTimeoutSeconds());
	              lProbe.setPeriodSeconds(c.getLivenessProbe().getPeriodSeconds());
	              lProbe.setSuccessThreshold(c.getLivenessProbe().getSuccessThreshold());
	              lProbe.setFailureThreshold(c.getLivenessProbe().getFailureThreshold());
	              container.setLivenessProbe(lProbe);
	            }

	                    if (c.getReadinessProbe() != null) {
	                        Probe rProbe = new Probe();
	                        HTTPGetAction httpGet = new HTTPGetAction();
	                        TCPSocketAction tcp=new TCPSocketAction();
	                        if (c.getReadinessProbe().getHttpGet() != null) {
	                          httpGet.setPath(c.getReadinessProbe().getHttpGet().getPath());
	                            if (c.getReadinessProbe().getHttpGet().getPort() == 0) {
	                                rProbe.getHttpGet().setPort(80);
	                            } else {
	                                // rProbe.getHttpGet().setPort(c.getReadinessProbe().getHttpGet().getPort());
	                                httpGet.setPort(c.getReadinessProbe().getHttpGet().getPort());
	                            }
	                            rProbe.setHttpGet(httpGet);
	                        }

	                        if (c.getReadinessProbe().getExec() != null) {
	                            if (c.getReadinessProbe().getExec().getCommand() != null) {
	                                ExecAction exec = new ExecAction();
	                                exec.setCommand(c.getReadinessProbe().getExec().getCommand());
	                                rProbe.setExec(exec);
	                            }
	                        }

	                        if (c.getReadinessProbe().getTcpSocket() != null) {
	                            if (c.getReadinessProbe().getTcpSocket().getPort() == 0) {
	                                tcp.setPort(80);
	                            } else {
	                                // rProbe.getTcpSocket().setPort(c.getReadinessProbe().getTcpSocket().getPort());
	                                tcp.setPort(c.getReadinessProbe().getTcpSocket().getPort());
	                            }
	                            rProbe.setTcpSocket(tcp);
	                        }
	              rProbe.setInitialDelaySeconds(c.getReadinessProbe().getInitialDelaySeconds());
	              rProbe.setTimeoutSeconds(c.getReadinessProbe().getTimeoutSeconds());
	              rProbe.setPeriodSeconds(c.getReadinessProbe().getPeriodSeconds());
	              rProbe.setSuccessThreshold(c.getReadinessProbe().getSuccessThreshold());
	              rProbe.setFailureThreshold(c.getReadinessProbe().getFailureThreshold());
	              container.setReadinessProbe(rProbe);
	            }

	            if (c.getPorts() != null && !c.getPorts().isEmpty()) {
	              List<ContainerPort> ps = new ArrayList<ContainerPort>();
	              for (CreatePortDto p : c.getPorts()) {
	                ContainerPort port = new ContainerPort();
	                port.setContainerPort(Integer.valueOf(p.getPort()));
	                port.setProtocol(p.getProtocol());
	                ps.add(port);
	              }
	              container.setPorts(ps);
	            }

	            if (c.getEnv() != null && !c.getEnv().isEmpty()) {
	              List<EnvVar> envVars = new ArrayList<EnvVar>();
	              for (CreateEnvDto env : c.getEnv()) {
	                EnvVar eVar = new EnvVar();
	                eVar.setName(env.getKey());
	                eVar.setValue(env.getValue());
	                envVars.add(eVar);
	              }
	              container.setEnv(envVars);
	            }

	            if (c.getResource() != null) {
	              ResourceRequirements limit = new ResourceRequirements();
	              Map<String, String> res = new HashMap<String, String>();
	              String regEx="[^0-9]";
	              Pattern p = Pattern.compile(regEx);
	              Matcher m = p.matcher(c.getResource().getCpu());
	              String result = m.replaceAll("").trim();
	              res.put("cpu", result + "m");
//	              res.put("cpu", c.getResource().getCpu());
	              Matcher mm = p.matcher(c.getResource().getMemory());
	                        String resultm = mm.replaceAll("").trim();
	                        res.put("memory", resultm + "Mi");
	              /*res.put("memory", c.getResource().getMemory() + "Mi");*/
	              limit.setLimits(res);
	              container.setResources(limit);
	            }

	            List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>();
	            container.setVolumeMounts(volumeMounts);
	            if (c.getStorage() != null && !c.getStorage().isEmpty()) {
	              Map<String, Object> volFlag = new HashMap<String, Object>();
	              for (CreateVolumeDto vm : c.getStorage()) {
	                if(vm.getType()!=null){
	                  switch (vm.getType()) {
	                  case Constant.VOLUME_TYPE_PV:
	                    if (!volFlag.containsKey(vm.getPvcName())) {
	                      PersistentVolumeClaimVolumeSource pvClaim = new PersistentVolumeClaimVolumeSource();
	                      volFlag.put(vm.getPvcName(), vm.getPvcName());
	                      if (vm.getReadOnly().equals("true")) {
	                        pvClaim.setReadOnly(true);
	                      }
	                      if (vm.getReadOnly().equals("false")) {
	                        pvClaim.setReadOnly(false);
	                      }
	                      pvClaim.setClaimName(vm.getPvcName());
	                      Volume vol = new Volume();
	                      vol.setPersistentVolumeClaim(pvClaim);
	                      vol.setName(vm.getPvcName());
	                      volumes.add(vol);
	                    }
	                    VolumeMount volm = new VolumeMount();
	                    volm.setName(vm.getPvcName());
	                    volm.setReadOnly(Boolean.parseBoolean(vm.getReadOnly()));
	                    volm.setMountPath(vm.getPath());
	                    volumeMounts.add(volm);
	                    container.setVolumeMounts(volumeMounts);
	                    break;
	                  case Constant.VOLUME_TYPE_GITREPO:
	                    if (!volFlag.containsKey(vm.getGitUrl())) {
	                      volFlag.put(vm.getGitUrl(), RandomNum.randomNumber(8));
	                      Volume gitRep = new Volume();
	                      gitRep.setName(volFlag.get(vm.getGitUrl()).toString());
	                      GitRepoVolumeSource gp = new GitRepoVolumeSource();
	                      gp.setRepository(vm.getGitUrl());
	                      gp.setRevision(vm.getRevision());
	                      gitRep.setGitRepo(gp);
	                      volumes.add(gitRep);
	                    }
	                    VolumeMount volmg = new VolumeMount();
	                    volmg.setName(volFlag.get(vm.getGitUrl()).toString());
	                    volmg.setReadOnly(Boolean.parseBoolean(vm.getReadOnly()));
	                    volmg.setMountPath(vm.getPath());
	                    volumeMounts.add(volmg);
	                    container.setVolumeMounts(volumeMounts);
	                    break;
	                  case Constant.VOLUME_TYPE_EMPTYDIR:
	                    if (!volFlag.containsKey(Constant.VOLUME_TYPE_EMPTYDIR+vm.getEmptyDir()==null ? "": vm.getEmptyDir())) {
	                      volFlag.put(Constant.VOLUME_TYPE_EMPTYDIR+vm.getEmptyDir()==null ? "": vm.getEmptyDir(), RandomNum.getRandomString(8));
	                      Volume empty = new Volume();
	                      empty.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR+vm.getEmptyDir()==null ? "": vm.getEmptyDir()).toString());
	                      EmptyDirVolumeSource ed =new EmptyDirVolumeSource();
	                      if(vm.getEmptyDir() != null && "Memory".equals(vm.getEmptyDir())){
	                        ed.setMedium(vm.getEmptyDir());//Memory
	                      }
	                      empty.setEmptyDir(ed);
	                      volumes.add(empty);
	                    }
	                    VolumeMount volme = new VolumeMount();
	                    volme.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR+vm.getEmptyDir()==null ? "": vm.getEmptyDir()).toString());
	                    volme.setMountPath(vm.getPath());
	                    volumeMounts.add(volme);
	                    container.setVolumeMounts(volumeMounts);
	                    break;
	                  case Constant.VOLUME_TYPE_HOSTPASTH:
	                    if (!volFlag.containsKey(Constant.VOLUME_TYPE_HOSTPASTH+vm.getHostPath())) {
	                      volFlag.put(Constant.VOLUME_TYPE_HOSTPASTH+vm.getHostPath(), RandomNum.getRandomString(8));
	                      Volume empty = new Volume();
	                      empty.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH+vm.getHostPath()).toString());
	                      HostPath hp =new HostPath();
	                      hp.setPath(vm.getHostPath());
	                      empty.setHostPath(hp);
	                      volumes.add(empty);
	                    }
	                    VolumeMount volmh = new VolumeMount();
	                    volmh.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH+vm.getHostPath()).toString());
	                    volmh.setMountPath(vm.getPath());
	                    volumeMounts.add(volmh);
	                    container.setVolumeMounts(volumeMounts);
	                    break;
	                  default:
	                    break;
	                  }
	                }
	              }
	            }

	            if (!StringUtils.isEmpty(c.getLog())) {
	              Volume emp = new Volume();
	              emp.setName("logdir" + c.getName());
	              EmptyDirVolumeSource ed = new EmptyDirVolumeSource();
	              ed.setMedium("");
	              emp.setEmptyDir(ed);
	              volumes.add(emp);
	              VolumeMount volm = new VolumeMount();
	              volm.setName("logdir" + c.getName());
	              volm.setMountPath(c.getLog());
	              volumeMounts.add(volm);
	              container.setVolumeMounts(volumeMounts);
	            }

	            if (c.getConfigmap() != null && c.getConfigmap().size()>0) {
	              for (CreateConfigMapDto cm : c.getConfigmap()) {
	                if (cm != null && !StringUtils.isEmpty(cm.getPath())) {
	                  String filename = cm.getFile();
	                  if(cm.getPath().contains("/")){
	                    int in = cm.getPath().lastIndexOf("/");
	                    filename = cm.getPath().substring(in+1, cm.getPath().length());
	                  }
	                  Volume cMap = new Volume();
	                  cMap.setName((cm.getFile() + "v" + cm.getTag()).replace(".", "-"));
	                  ConfigMapVolumeSource coMap = new ConfigMapVolumeSource();
	                  coMap.setName(name + c.getName());
	                  List<KeyToPath> items=new LinkedList<KeyToPath>();
	                  KeyToPath key=new KeyToPath();
	                  key.setKey(cm.getFile()+"v"+cm.getTag());
	                  key.setPath(filename);
	                  items.add(key);
	                  coMap.setItems(items);
	                  cMap.setConfigMap(coMap);
	                  volumes.add(cMap);
	                  VolumeMount volm = new VolumeMount();
	                  volm.setName((cm.getFile() + "v" + cm.getTag()).replace(".", "-"));
	                  volm.setMountPath(cm.getPath());
	                  volm.setSubPath(filename);
	                  volumeMounts.add(volm);
	                  container.setVolumeMounts(volumeMounts);
	                }
	              }
	            }
	            cs.add(container);
	    	}
	    }
	    podSpec.setContainers(cs);
		Map<String, Object> nodeselector = new HashMap<>();
        if (!StringUtils.isEmpty(nodeSelector)) {
        	String[] ns = {};
            if (nodeSelector.contains(",")) {
                ns = nodeSelector.split(",");
            }else{
            	ns[0] = nodeSelector;
            }
			for (String n : ns) {
				if (n.contains("=")) {
					String[] s = n.split("=");
					nodeselector.put(s[0], s[1]);
				}
			}
        }
        podSpec.setNodeSelector(nodeselector);
        List<LocalObjectReference> imagePullSecrets = new ArrayList<>();
        LocalObjectReference e = new LocalObjectReference();
        e.setName(userName+"-secret");
        if(Constant.TYPE_JOB.equals(type)){
        	if(StringUtils.isEmpty(restartPolicy)){
        		podSpec.setRestartPolicy(Constant.RESTARTPOLICY_NERVER);
        	}else{
        		podSpec.setRestartPolicy(restartPolicy);
        	}
        }
        imagePullSecrets.add(e);
        podSpec.setImagePullSecrets(imagePullSecrets);
        if (volumes.size() > 0) {
        	podSpec.setVolumes(volumes);
        }
        podTemplate.setSpec(podSpec);
		return podTemplate;
	}
}
