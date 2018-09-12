package com.harmonycloud.service.platform.convert;

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
        newContainer.getResources().setLimits(res);
        newContainer.getResources().setRequests(res);
        if (oldContainer.getLimit() != null) {
            Map<String, String> resl = new HashMap<String, String>();
            Matcher ml = p.matcher(oldContainer.getLimit().getCpu());
            String resultl = ml.replaceAll("").trim();
            resl.put("cpu", resultl + "m");
            resl.put("memory", (oldContainer.getLimit().getMemory().contains("Mi") || oldContainer.getLimit().getMemory().contains("Gi")) ? oldContainer.getLimit().getMemory() : oldContainer.getLimit().getMemory() + "Mi");
            newContainer.getResources().setLimits(resl);
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
                    ps.add(port);
                    container.setPorts(ps);
                    ServicePort servicePort = new ServicePort();
                    servicePort.setTargetPort(Integer.valueOf(p.getContainerPort()));
                    servicePort.setPort(Integer.valueOf(p.getContainerPort()));
                    servicePort.setProtocol(p.getProtocol());
                    servicePort.setName(name + "-port" + ports.size());
                    ports.add(servicePort);
                }

            }

            List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>();
            container.setVolumeMounts(volumeMounts);

            if (cc.getConfigmap() != null && cc.getConfigmap().size() > 0) {
                for (CreateConfigMapDto cm : cc.getConfigmap()) {
                    if (cm != null && !StringUtils.isEmpty(cm.getPath())) {
                        String filename = cm.getFile();
//                        if(cm.getPath().contains("/")){
//                            int in = cm.getPath().lastIndexOf("/");
//                            filename = cm.getPath().substring(in+1, cm.getPath().length());
//                        }
                        Volume cMap = new Volume();
                        cMap.setName((cm.getFile() + "-" + cm.getConfigMapId()).replace(".", "-"));
                        ConfigMapVolumeSource coMap = new ConfigMapVolumeSource();
                        coMap.setName(containerToConfigMap.get(cc.getName()));
                        List<KeyToPath> items = new LinkedList<KeyToPath>();
                        KeyToPath key = new KeyToPath();
                        key.setKey(cm.getFile()+"v"+cm.getTag());
                        key.setPath(filename);
                        items.add(key);
                        coMap.setItems(items);
                        cMap.setConfigMap(coMap);
                        volumes.add(cMap);
                        VolumeMount volm = new VolumeMount();
                        volm.setName((cm.getFile() + "-" + cm.getConfigMapId()).replace(".", "-"));
                        volm.setMountPath(cm.getPath()+"/"+filename);
                        volm.setSubPath(filename);
                        volumeMounts.add(volm);
                    }
                }
            }


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
            }

            //如果volume有更新
            if (cc.getStorage() != null && !cc.getStorage().isEmpty()) {
                List<PersistentVolumeDto> newVolume = cc.getStorage();
                Map<String, Object> volFlag = new HashMap<String, Object>();
                for (int i = 0; i < newVolume.size(); i++) {
                    PersistentVolumeDto vol = newVolume.get(i);
                    switch (vol.getType()) {
                        case Constant.VOLUME_TYPE_PV:
                            if (!volFlag.containsKey(vol.getPvcName())) {
                                volFlag.put(vol.getPvcName(), vol.getPvcName());
                                PersistentVolumeClaimVolumeSource pvClaim = new PersistentVolumeClaimVolumeSource();
                                pvClaim.setClaimName(vol.getPvcName());
                                Volume vole = new Volume();
                                vole.setPersistentVolumeClaim(pvClaim);
                                vole.setName(vol.getPvcName());
                                volumes.add(vole);
                            }
                            VolumeMount volm = new VolumeMount();
                            volm.setName(vol.getPvcName());
                            volm.setMountPath(vol.getPath());
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
                            if (!volFlag.containsKey(Constant.VOLUME_TYPE_EMPTYDIR+vol.getEmptyDir()==null ? "": vol.getEmptyDir())) {
                                volFlag.put(Constant.VOLUME_TYPE_EMPTYDIR+vol.getEmptyDir()==null ? "": vol.getEmptyDir(), RandomNum.getRandomString(8));
                                Volume empty = new Volume();
                                empty.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR+vol.getEmptyDir()==null ? "": vol.getEmptyDir()).toString());
                                EmptyDirVolumeSource ed =new EmptyDirVolumeSource();
                                if(vol.getEmptyDir() != null && "Memory".equals(vol.getEmptyDir())){
                                    ed.setMedium(vol.getEmptyDir());//Memory
                                }
                                empty.setEmptyDir(ed);
                                volumes.add(empty);
                            }
                            VolumeMount volme = new VolumeMount();
                            volme.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR+vol.getEmptyDir()==null ? "": vol.getEmptyDir()).toString());
                            volme.setMountPath(vol.getPath());
                            volumeMounts.add(volme);
                            container.setVolumeMounts(volumeMounts);
                            break;
                        case Constant.VOLUME_TYPE_HOSTPASTH:
                            if (!volFlag.containsKey(Constant.VOLUME_TYPE_HOSTPASTH+vol.getHostPath())) {
                                volFlag.put(Constant.VOLUME_TYPE_HOSTPASTH+vol.getHostPath(), RandomNum.getRandomString(8));
                                Volume empty = new Volume();
                                empty.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH+vol.getHostPath()).toString());
                                HostPath hp =new HostPath();
                                hp.setPath(vol.getHostPath());
                                empty.setHostPath(hp);
                                volumes.add(empty);
                            }
                            VolumeMount volmh = new VolumeMount();
                            volmh.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH+vol.getHostPath()).toString());
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
                    eVar.setValue(env.getValue());
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
                servicePort.setName("port-" + UUID.randomUUID());
                servicePort.setTargetPort(port.getContainerPort());
                servicePort.setProtocol(port.getProtocol());
                servicePort.setPort(port.getContainerPort());
                ports.add(servicePort);
            }
        }
        return ports;
    }
}
