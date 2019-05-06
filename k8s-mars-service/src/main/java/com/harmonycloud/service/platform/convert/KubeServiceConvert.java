package com.harmonycloud.service.platform.convert;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.UUIDUtil;
import com.harmonycloud.dto.application.CreateConfigMapDto;
import com.harmonycloud.dto.application.CreateEnvDto;
import com.harmonycloud.dto.application.CreatePortDto;
import com.harmonycloud.dto.application.PersistentVolumeDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.util.RandomNum;
import com.harmonycloud.service.platform.bean.UpdateContainer;
import com.harmonycloud.service.platform.constant.Constant;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @Author jiangmi
 * @Description 服务和应用数据组装(与kubernetes)
 * @Date created in 2017-12-20
 * @Modified
 */
public class KubeServiceConvert {

    /**
     * 更新服务时，更新kubernetes内的Container信息
     * @param dep 已经在集群内的Deployment
     * @param updateContainers 需要更新的容器信息
     * @return Deployment 新的服务对象
     * @throws Exception 内容更新失败
     */
    public static Deployment convertUpdateDeploymentData(Deployment dep, List<UpdateContainer> updateContainers) throws Exception {
        List<Container> newC = new ArrayList<Container>();
        List<Container> containers = dep.getSpec().getTemplate().getSpec().getContainers();
        Map<String, Container> ct = new HashMap<>();
        for (Container c : containers) {
            ct.put(c.getName(), c);
        }
        for (UpdateContainer cc : updateContainers) {
            Container container = ct.get(cc.getName());
            if (cc.getResource() != null) {

                //更新cpu、memory
                container = convertContainerResource(cc, container);
            }
            newC.add(container);
        }
        dep.getSpec().getTemplate().getSpec().setContainers(newC);
        return  dep;
    }

    /**
     * 更新容器内的资源（cpu、memory）
     * @param oldContainer 旧的容器
     * @param newContainer 新的容器
     * @return Container 新的容器
     * @throws Exception 容器信息更新失败
     */
    public static Container convertContainerResource(UpdateContainer oldContainer, Container newContainer) throws Exception {
        Map<String, Object> res = new HashMap<>();
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(oldContainer.getResource().getCpu());
        String result = m.replaceAll("").trim();
        res.put("cpu", result + "m");
        res.put("memory", (oldContainer.getResource().getMemory().contains("Mi") || oldContainer.getResource().getMemory().contains("Gi")) ? oldContainer.getResource().getMemory() : oldContainer.getResource().getMemory() + "Mi");
        if(oldContainer.getResource().getGpu() != null) {
            res.put(CommonConstant.NVIDIA_GPU, oldContainer.getResource().getGpu());
        }
        //get limit rate
        Integer rate = 1;
        Map<String, Object> requests = (Map<String, Object>)newContainer.getResources().getRequests();
        Map<String, Object> limits = (Map<String, Object>)newContainer.getResources().getLimits();
        if(requests.get(CommonConstant.CPU) != null && limits.get(CommonConstant.CPU) != null){
            String requestCpu = (String)requests.get(CommonConstant.CPU);
            String limitCpu = (String)limits.get(CommonConstant.CPU);
            Integer requestCpuNum = 0;
            Integer limitCpuNum = 0;
            if(requestCpu.contains("m")){
                requestCpuNum = Integer.valueOf(requestCpu.replace("m", ""));
            }else{
                requestCpuNum = CommonConstant.NUM_THOUSAND * Integer.valueOf(requestCpu);
            }
            if(limitCpu.contains("m")){
                limitCpuNum = Integer.valueOf(limitCpu.replace("m", ""));
            }else{
                limitCpuNum = CommonConstant.NUM_THOUSAND * Integer.valueOf(limitCpu);
            }
            if(requestCpuNum != null){
                rate = limitCpuNum / requestCpuNum;
            }
        }
        newContainer.getResources().setLimits(res);
        newContainer.getResources().setRequests(res);
        if (oldContainer.getLimit() != null) {
            //获取倍率
            int rate1 = oldContainer.getLimit().getCurrentRate();
            Map<String, String> resl = new HashMap<String, String>();
            if (0 == rate1) {
                resl.put("cpu", oldContainer.getLimit().getCpu());
                resl.put("memory", (oldContainer.getLimit().getMemory().contains("Mi") || oldContainer.getLimit().getMemory().contains("Gi")) ? oldContainer.getLimit().getMemory() : oldContainer.getLimit().getMemory() + "Mi");
            } else {
                int resultl = Integer.valueOf(result) * rate1;
                resl.put("cpu", resultl + "m");
                resl.put("memory", ((Map<String, Object>)newContainer.getResources().getLimits()).get("memory").toString());
            }
            if(oldContainer.getResource().getGpu() != null) {
                resl.put(CommonConstant.NVIDIA_GPU, oldContainer.getResource().getGpu());
            }
            newContainer.getResources().setLimits(resl);
        }else{
            Map<String, Object> limitRes = new HashMap<>();
            limitRes.put(CommonConstant.CPU, Integer.valueOf(result) * rate + "m");
            Integer requeseMemory = 0;
            if(oldContainer.getResource().getMemory().contains("Mi")){
                requeseMemory = Integer.valueOf(oldContainer.getResource().getMemory().replace("Mi", ""));
            }else if(oldContainer.getResource().getMemory().contains("Gi")){
                requeseMemory = Integer.valueOf(oldContainer.getResource().getMemory().replace("Gi", "")) * CommonConstant.NUM_SIZE_MEMORY;
            }else{
                requeseMemory = Integer.valueOf(oldContainer.getResource().getMemory().replace("Gi", ""));
            }

            limitRes.put(CommonConstant.MEMORY, requeseMemory * rate + "Mi");

            if(oldContainer.getResource().getGpu() != null) {
                limitRes.put(CommonConstant.NVIDIA_GPU, oldContainer.getResource().getGpu());
            }

            newContainer.getResources().setLimits(limitRes);
        }
        return newContainer;
    }

    public static PodTemplateSpec convertDeploymentUpdate(PodTemplateSpec podTemplateSpec, List<UpdateContainer> newContainers, String name, Map<String, String> containerToConfigMap, Cluster cluster) throws Exception {
        Map<String, Container> ct = new HashMap<String, Container>();
        List<Container> containers = podTemplateSpec.getSpec().getContainers();
        for (Container c : containers) {
            ct.put(c.getName(), c);
        }
        List<Volume> volumes = new ArrayList<Volume>();         //修改后的volume
        List<ServicePort> ports = new ArrayList<ServicePort>(); //修改后的端口
        List<Container> newC = new ArrayList<Container>(); //修改后的容器
        //遍历更新的container参数
        for (UpdateContainer cc : newContainers) {          //cc是新的
            //拿到需要修改的container,设置成修改后的参数
            Container container = ct.get(cc.getName());       //container为旧的
            if(cc.getSecurityContext() != null  && cc.getSecurityContext().isSecurity()){
                SecurityContext securityContext = new SecurityContext();
                if(cc.getSecurityContext().isPrivileged() == true){
                    securityContext.setPrivileged(cc.getSecurityContext().isPrivileged());
                }
                Capabilities capabilities = new Capabilities();
                if(cc.getSecurityContext().getAdd() != null && cc.getSecurityContext().getAdd().size() > 0){
                    capabilities.setAdd(cc.getSecurityContext().getAdd());
                }
                if(cc.getSecurityContext().getDrop() != null && cc.getSecurityContext().getDrop().size() > 0){
                    capabilities.setDrop(cc.getSecurityContext().getDrop());
                }
                securityContext.setCapabilities(capabilities);
                container.setSecurityContext(securityContext);
            }
            container.setName(cc.getName());
            if (StringUtils.isEmpty(cc.getTag())) {
                container.setImage(cluster.getHarborServer().getHarborAddress() +"/"+ cc.getImg());
            } else {
                container.setImage(cluster.getHarborServer().getHarborAddress() +"/" + cc.getImg() + ":" + cc.getTag());
            }

            //set cpu memory
            if (cc.getResource() != null) { //如果resource参数有更新
                container = convertContainerResource(cc, container);
            }
            if (cc.getPorts() != null && !cc.getPorts().isEmpty()) { //如果端口有更新
                List<ContainerPort> ps = new ArrayList<ContainerPort>();

                for (CreatePortDto p : cc.getPorts()) {
                    ContainerPort port = new ContainerPort();
                    port.setContainerPort(Integer.valueOf(p.getContainerPort()));
                    port.setProtocol(p.getProtocol());
                    String portName = convertPortName(p.getName(), port.getContainerPort());
                    port.setName(portName);
                    ps.add(port);
                    container.setPorts(ps);
                    ServicePort servicePort = new ServicePort();
                    servicePort.setTargetPort(Integer.valueOf(p.getContainerPort()));
                    servicePort.setPort(Integer.valueOf(p.getContainerPort()));
                    servicePort.setProtocol(p.getProtocol());
                    servicePort.setName(portName);
                    ports.add(servicePort);
                }

            }

            List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>();
            container.setVolumeMounts(volumeMounts);
            K8sResultConvert.convertConfigMap(containerToConfigMap.get(cc.getName()),cc.getName(), cc.getConfigmap(),volumes,volumeMounts);

            if (Objects.nonNull(cc.getLog()) && !StringUtils.isEmpty(cc.getLog().getMountPath())) {
                Volume emp = new Volume();
                emp.setName(Constant.VOLUME_LOGDIR_NAME + cc.getName());
                EmptyDirVolumeSource ed = new EmptyDirVolumeSource();
                ed.setMedium("");
                emp.setEmptyDir(ed);
                volumes.add(emp);
                VolumeMount volm = new VolumeMount();
                volm.setName(Constant.VOLUME_LOGDIR_NAME + cc.getName());
                volm.setMountPath(cc.getLog().getMountPath());
                volumeMounts.add(volm);
                List<CreateEnvDto> envlist = new ArrayList<>();
                CreateEnvDto env = new CreateEnvDto();
                env.setKey(Constant.PILOT_LOG_PREFIX);
                env.setValue(cc.getLog().getMountPath() + "/*");
                envlist.add(env);
                env = new CreateEnvDto();
                env.setKey(Constant.PILOT_LOG_PREFIX_TAG);
                String parentResourceType = Constant.DEPLOYMENT;
                if (podTemplateSpec.getMetadata().getLabels().containsKey(Constant.TYPE_STATEFULSET)){
                    parentResourceType = Constant.STATEFULSET;
                }else if (podTemplateSpec.getMetadata().getLabels().containsKey(Constant.TYPE_DAEMONSET)){
                    parentResourceType = Constant.DAEMONSET;
                }else if (podTemplateSpec.getMetadata().getLabels().containsKey(Constant.TYPE_DEPLOYMENT)){
                    parentResourceType = Constant.DEPLOYMENT;
                }
                env.setValue("k8s_resource_type=" + parentResourceType + ",k8s_resource_name=" + name);
                envlist.add(env);
                if (cc.getEnv()!=null){
                    cc.getEnv().addAll(envlist);
                }else {
                    cc.setEnv(envlist);
                }
            }

            //如果volume有更新
            if (cc.getStorage() != null && !cc.getStorage().isEmpty()) {
                List<PersistentVolumeDto> newVolume = cc.getStorage();
                Map<String, Object> volFlag = new HashMap<>();
                for (int i = 0; i < newVolume.size(); i++) {
                    PersistentVolumeDto vol = newVolume.get(i);
                    String type = vol.getType();
                    if(Constant.VOLUME_TYPE_NFS.equalsIgnoreCase(type)){
                        type = Constant.VOLUME_TYPE_PVC;
                    }
                    //添加选择了拉取依赖后的特殊处理
                    if (type.equals(Constant.VOLUME_TYPE_EMPTYDIR) && StringUtils.isNotBlank(vol.getName()) && vol.getName().equals("empty")) {
                        Volume volumeDep = new Volume();
                        VolumeMount volumeMountDep = new VolumeMount();
                        volumeDep.setName(vol.getName());
                        volumeMountDep.setName(vol.getName());
                        volumeMountDep.setMountPath(vol.getPath());
                        volumes.add(volumeDep);
                        volumeMounts.add(volumeMountDep);
                        continue;
                    }

                    switch (type) {
                        case Constant.VOLUME_TYPE_PVC:
                            if (!volFlag.containsKey(vol.getPvcName())) {
                                volFlag.put(vol.getPvcName(), vol.getPvcName());
                                PersistentVolumeClaimVolumeSource pvClaim = new PersistentVolumeClaimVolumeSource();
                                pvClaim.setClaimName(vol.getPvcName());
                                Volume vole = new Volume();
                                vole.setPersistentVolumeClaim(pvClaim);
                                vole.setName(vol.getPvcName().replace(".", ""));
                                volumes.add(vole);
                            }
                            VolumeMount volm = new VolumeMount();
                            volm.setName(vol.getPvcName().replace(".", ""));
                            volm.setMountPath(vol.getPath());
                            volm.setReadOnly(vol.getReadOnly());
                            volumeMounts.add(volm);
                            container.setVolumeMounts(volumeMounts);
                            break;
                        case Constant.VOLUME_TYPE_GITREPO:
                            if (!volFlag.containsKey(vol.getGitUrl())) {
                                volFlag.put(vol.getGitUrl(), RandomNum.randomNumber(8));
                                Volume gitRep = new Volume();
                                gitRep.setName(volFlag.get(vol.getGitUrl()).toString());
                                GitRepoVolumeSource gp = new GitRepoVolumeSource();
                                gp.setRepository(vol.getGitUrl());
                                gp.setRevision(vol.getRevision());
                                gitRep.setGitRepo(gp);
                                volumes.add(gitRep);
                            }
                            VolumeMount volmg = new VolumeMount();
                            volmg.setName(volFlag.get(vol.getGitUrl()).toString());
                            volmg.setReadOnly(vol.getReadOnly());
                            volmg.setMountPath(vol.getPath());
                            volumeMounts.add(volmg);
                            container.setVolumeMounts(volumeMounts);
                            break;
                        case Constant.VOLUME_TYPE_EMPTYDIR:
                            if (!volFlag.containsKey(Constant.VOLUME_TYPE_EMPTYDIR + (vol.getName() == null ? "" : vol.getName()))) {
                                volFlag.put(Constant.VOLUME_TYPE_EMPTYDIR + (vol.getName() == null ? "" : vol.getName()), StringUtils.isBlank(vol.getName())?RandomNum.getRandomString(8):vol.getName());
                                Volume empty = new Volume();
                                empty.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR + (vol.getName() == null ? "" : vol.getName())).toString());
                                EmptyDirVolumeSource ed = new EmptyDirVolumeSource();
                                if (vol.getEmptyDir() != null && "Memory".equals(vol.getEmptyDir())) {
                                    ed.setMedium(vol.getEmptyDir());//Memory
                                }
                                if (vol.getCapacity() != null) {
                                    ed.setSizeLimit(vol.getCapacity());//sizeLimit
                                }
                                empty.setEmptyDir(ed);
                                volumes.add(empty);
                            }
                            VolumeMount volme = new VolumeMount();
                            volme.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR + (vol.getName() == null ? "" : vol.getName())).toString());
                            volme.setMountPath(vol.getPath());
                            volumeMounts.add(volme);
                            container.setVolumeMounts(volumeMounts);
                            break;
                        case Constant.VOLUME_TYPE_HOSTPASTH:
                            if (!volFlag.containsKey(Constant.VOLUME_TYPE_HOSTPASTH+vol.getHostPath())) {
                                volFlag.put(Constant.VOLUME_TYPE_HOSTPASTH + vol.getHostPath(), StringUtils.isBlank(vol.getName())?RandomNum.getRandomString(8):vol.getName());
                                Volume empty = new Volume();
                                empty.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH + vol.getHostPath()).toString());
                                HostPath hp =new HostPath();
                                hp.setPath(vol.getHostPath());
                                empty.setHostPath(hp);
                                volumes.add(empty);
                            }
                            VolumeMount volmh = new VolumeMount();
                            volmh.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH + vol.getHostPath()).toString());
                            volmh.setMountPath(vol.getPath());
                            volumeMounts.add(volmh);
                            container.setVolumeMounts(volumeMounts);
                            break;
                        default:
                            break;
                    }
                }
            }
            if(cc.getImagePullPolicy() != null) {
                container.setImagePullPolicy(cc.getImagePullPolicy() );
            }
            container.setVolumeMounts(volumeMounts);
            container.setCommand(cc.getCommand());
            container.setArgs(cc.getArgs());
            container.setLivenessProbe(null);
            container.setReadinessProbe(null);

            container = convertProbe(cc, container);
            //如果环境变量有更新
            if (cc.getEnv() != null && !cc.getEnv().isEmpty()) {
                List<EnvVar> envVars = new ArrayList<EnvVar>();
                for (CreateEnvDto env : cc.getEnv()) {
                    EnvVar eVar = new EnvVar();
                    eVar.setName(env.getKey());
                    if(StringUtils.isEmpty(env.getType()) || CommonConstant.ENV_TYPE_EQUAL.equals(env.getType())) {
                        eVar.setValue(env.getValue());
                    }else if(CommonConstant.ENV_TYPE_FROM.equals(env.getType())){
                        EnvVarSource envVarSource = new EnvVarSource();
                        ObjectFieldSelector objectFieldSelector = new ObjectFieldSelector();
                        objectFieldSelector.setApiVersion(com.harmonycloud.k8s.constant.Constant.VERSION_V1);
                        objectFieldSelector.setFieldPath(env.getValue());
                        envVarSource.setFieldRef(objectFieldSelector);
                        eVar.setValueFrom(envVarSource);
                    }
                    envVars.add(eVar);
                }
                container.setEnv(envVars);
            }
            newC.add(container);
        }

        podTemplateSpec.getSpec().setContainers(newC);
        podTemplateSpec.getSpec().setVolumes(volumes);
        return podTemplateSpec;
    }

    public static Container convertProbe(UpdateContainer cc, Container container) throws Exception {
        if (cc.getLivenessProbe() != null && !cc.getLivenessProbe().isEmpty()) {
            Probe lProbe = new Probe();
            HTTPGetAction httpGet = new HTTPGetAction();
            TCPSocketAction tcp=new TCPSocketAction();
            if (cc.getLivenessProbe().getHttpGet() != null) {
                httpGet.setPath(cc.getLivenessProbe().getHttpGet().getPath());
                if (cc.getLivenessProbe().getHttpGet().getPort() == 0) {
                    httpGet.setPort(Constant.LIVENESS_PORT);
                } else {
                    httpGet.setPort(cc.getLivenessProbe().getHttpGet().getPort());
                }
                lProbe.setHttpGet(httpGet);
            }

            if (cc.getLivenessProbe().getExec() != null ) {
                if(cc.getLivenessProbe().getExec().getCommand()!=null){
                    ExecAction exec= new ExecAction();
                    exec.setCommand(cc.getLivenessProbe().getExec().getCommand());
                    lProbe.setExec(exec);
                }
            }

            if (cc.getLivenessProbe().getTcpSocket() != null) {
                if (cc.getLivenessProbe().getTcpSocket().getPort() == 0) {
                    tcp.setPort(Constant.LIVENESS_PORT);
                } else {
                    tcp.setPort(cc.getLivenessProbe().getTcpSocket().getPort());
                }
                lProbe.setTcpSocket(tcp);
            }
            lProbe.setInitialDelaySeconds(cc.getLivenessProbe().getInitialDelaySeconds());
            lProbe.setTimeoutSeconds(cc.getLivenessProbe().getTimeoutSeconds());
            lProbe.setPeriodSeconds(cc.getLivenessProbe().getPeriodSeconds());
            lProbe.setSuccessThreshold(cc.getLivenessProbe().getSuccessThreshold());
            lProbe.setFailureThreshold(cc.getLivenessProbe().getFailureThreshold());
            container.setLivenessProbe(lProbe);
        }

        if (cc.getReadinessProbe() != null  && !cc.getReadinessProbe().isEmpty()) {
            Probe rProbe = new Probe();
            HTTPGetAction httpGet = new HTTPGetAction();
            TCPSocketAction tcp=new TCPSocketAction();
            if (cc.getReadinessProbe().getHttpGet() != null) {
                httpGet.setPath(cc.getReadinessProbe().getHttpGet().getPath());
                if (cc.getReadinessProbe().getHttpGet().getPort() == 0) {
                    rProbe.getHttpGet().setPort(Constant.LIVENESS_PORT);
                } else {
                    httpGet.setPort(cc.getReadinessProbe().getHttpGet().getPort());
                }
                rProbe.setHttpGet(httpGet);
            }

            if (cc.getReadinessProbe().getExec() != null) {
                if (cc.getReadinessProbe().getExec().getCommand() != null) {
                    ExecAction exec = new ExecAction();
                    exec.setCommand(cc.getReadinessProbe().getExec().getCommand());
                    rProbe.setExec(exec);
                }
            }

            if (cc.getReadinessProbe().getTcpSocket() != null) {
                if (cc.getReadinessProbe().getTcpSocket().getPort() == 0) {
                    tcp.setPort(Constant.LIVENESS_PORT);
                } else {
                    tcp.setPort(cc.getReadinessProbe().getTcpSocket().getPort());
                }
                rProbe.setTcpSocket(tcp);
            }
            rProbe.setInitialDelaySeconds(cc.getReadinessProbe().getInitialDelaySeconds());
            rProbe.setTimeoutSeconds(cc.getReadinessProbe().getTimeoutSeconds());
            rProbe.setPeriodSeconds(cc.getReadinessProbe().getPeriodSeconds());
            rProbe.setSuccessThreshold(cc.getReadinessProbe().getSuccessThreshold());
            rProbe.setFailureThreshold(cc.getReadinessProbe().getFailureThreshold());
            container.setReadinessProbe(rProbe);
        }
        return container;
    }

    public static List<ServicePort> convertServicePort(List<Container> containerList) throws Exception {
        List<ServicePort> ports = new ArrayList();
        for (Container container : containerList) {
            for (ContainerPort port : container.getPorts()) {
                ServicePort servicePort = new ServicePort();
                if(StringUtils.isNotBlank(port.getName())) {
                    servicePort.setName(port.getName());
                }else{
                    servicePort.setName("port-" + port.getContainerPort());
                }
                servicePort.setTargetPort(port.getContainerPort());
                servicePort.setProtocol(port.getProtocol());
                servicePort.setPort(port.getContainerPort());
                ports.add(servicePort);
            }
        }
        return ports;
    }

    /**
     * 转换端口名称，端口名称前缀为http/http2/grpc/port, 后缀为端口号，以"-"分隔，如http-80,port-80
     * istio服务以http/http2/grpc开头，默认是port开始
     * @param portName 端口名
     * @param port 端口序号
     * @return
     */
    public static String convertPortName(String portName, int port) {
        if(StringUtils.isBlank(portName)){
            return "port-" + port;
        }
        if(portName.endsWith("-" + port)){
            return portName;
        }
        return portName.toLowerCase() + "-" + port;
    }

}
