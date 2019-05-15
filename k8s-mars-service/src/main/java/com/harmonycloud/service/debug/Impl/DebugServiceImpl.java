package com.harmonycloud.service.debug.Impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.debug.DebugMapper;
import com.harmonycloud.dao.debug.bean.DebugState;
import com.harmonycloud.dto.application.istio.ServiceEntryDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.policies.Action;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.service.PodService;
import com.harmonycloud.k8s.service.ServicesService;
import com.harmonycloud.k8s.service.istio.ServiceEntryServices;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.application.ConfigMapService;
import com.harmonycloud.service.application.FileUploadToContainerService;
import com.harmonycloud.service.application.impl.FileUploadToContainerServiceImpl;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.debug.DebugService;
import com.harmonycloud.service.istio.IstioServiceEntryService;
import com.harmonycloud.service.platform.socketio.test.App;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import net.sf.json.JSONObject;
import org.bouncycastle.asn1.cms.MetaData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.beans.IntrospectionException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

import static com.harmonycloud.service.platform.constant.Constant.LABEL_PROJECT_ID;

/**
 * Created by fengjinliu on 2019/5/5.
 */
@Service
public class DebugServiceImpl implements DebugService {

    private final static Logger logger= LoggerFactory.getLogger(DebugServiceImpl.class);

    @Autowired
    private DebugMapper debugMapper;

    @Autowired
    private PodService podService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private ServiceEntryServices serviceEntryService;

    @Autowired
    private ServicesService servicesService;

    @Autowired
    private ConfigMapService configMapService;

    @Value("#{propertiesReader['upload.path']}")
    private String uploadPath;

    @Autowired
    private ServicesService sService;
    @Override
    /*
    author fjl
     */
    public boolean start(String namespace, String username, String service, String port) throws Exception {
        //1. 调用service接口，在namespace下启动pod.拼装pod,addPod
        //2. 调用service接口，修改service
        //3. 调用mapper,存储user的debug状态信息
        DebugState ds = debugMapper.getStateByUsername(username);
        if(ds!=null) {
            if (!ds.getState().equals("stop")) {
                return false;
            }
        }
        //拼接pod
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        //创建新的pod
        Pod pod=new Pod();
        Object newpod=createDebugPod(namespace,service,username,cluster).getData();
        if(newpod!=null) {
            pod = (Pod) newpod;}
        else return false;
        // 启动service
        K8SClientResponse getResponse = serviceEntryService.getService(namespace, null, cluster, service);
        //不存在service,创建新的 service。拼接
        if (!HttpStatusUtil.isSuccessStatus(getResponse.getStatus())){
            Object getService=createDebugService(namespace,cluster,service,port).getData();
            if(getService==null){
                return false;
            }
        }
        K8SClientResponse getResponseAgain = serviceEntryService.getService(namespace, null, cluster, service);
        com.harmonycloud.k8s.bean.Service newService = JsonUtil.jsonToPojo(getResponseAgain.getBody(), com.harmonycloud.k8s.bean.Service.class);
        //修改service,与debugPod关联
        Map<String, String> selector = (Map<String, String>) newService.getSpec().getSelector();
        String app = selector.get("app");
            //若是已被修改为debug模式。执行退出
            if(app.contains("-debug")){
                return false;
            }
        selector.put("app", app + "-debug");
        newService.getSpec().setSelector(selector);
        //修改service
        K8SClientResponse updateResponse = serviceEntryService.updateService(namespace, service, newService, cluster);
        if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
            return false;
        }
        //用户未曾debug过，新建debug_state信息。设置为debug中
        if(ds==null) {
            ds = new DebugState();

            ds.setNamespace(namespace);
            ds.setPodname(pod.getMetadata().getName());
            ds.setPort(port);
            ds.setService(service);
            ds.setState("build");
            ds.setUsername(username);
            debugMapper.insert(ds);
        }
        else {
            ds.setNamespace(namespace);
            ds.setPodname(pod.getMetadata().getName());
            ds.setPort(port);
            ds.setService(service);
            ds.setUsername(username);
            ds.setState("build");
            debugMapper.update(ds);
            return true;}
        return true;
    }

    public ActionReturnUtil createDebugPod(String namespace,String service,String username,Cluster cluster)throws Exception{
        Pod pod = new Pod();
        pod.setKind("Pod");
        pod.setApiVersion("v1");
        //metadata
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName("debug-proxy-"+username);
        metadata.setNamespace(namespace);
        Map<String,Object> map=new HashMap<>();
        map.put("app",service+"-debug");
        map.put("debug","proxy");
        metadata.setLabels(map);
        pod.setMetadata(metadata);
        //spec
        PodSpec spec = new PodSpec();
        spec.setRestartPolicy("Always");
        spec.setDnsPolicy("ClusterFirst");
        spec.setSchedulerName("default-scheduler");
        List<LocalObjectReference> lolist=new ArrayList<LocalObjectReference>();
        LocalObjectReference lo=new LocalObjectReference();
        lo.setName("admin-secret");
        lolist.add(lo);
        spec.setImagePullSecrets(lolist);
        //container
        List<Container> cs = new ArrayList<Container>();
        Container con = new Container();
        con.setName("proxy");
        con.setImage(cluster.getHarborServer().getHarborHost()+"/k8s-deploy/debug-proxy:v1.0");
        con.setImagePullPolicy("IfNotPresent");
        ResourceRequirements resourceRequirements = new ResourceRequirements();
        Map<String, Object> limits = new HashMap<>();
        limits.put(CommonConstant.CPU, "100m");
        limits.put(CommonConstant.MEMORY, "128Mi");
        Map<String, Object> requests = new HashMap<>();
        requests.put(CommonConstant.CPU, "100m");
        requests.put(CommonConstant.MEMORY, "128Mi");
        resourceRequirements.setLimits(limits);
        resourceRequirements.setRequests(requests);
        con.setResources(resourceRequirements);
        List<String> command = new ArrayList<String>();
        command.add("/usr/sbin/sshd");
        command.add("-D");
        con.setCommand(command);
        cs.add(con);
        spec.setContainers(cs);
        Map<String,Object> nodeSelector=new HashMap<>();
        nodeSelector.put("HarmonyCloud_Status","A");
        spec.setNodeSelector(nodeSelector);
        spec.getNodeSelector();
        pod.setSpec(spec);
        ActionReturnUtil ac=podService.addPod(namespace,pod, cluster);
        return ActionReturnUtil.returnSuccessWithData(pod);
    }

    public ActionReturnUtil createDebugService(String namespace, Cluster cluster, String service, String port) throws Exception {
        com.harmonycloud.k8s.bean.Service newService = new com.harmonycloud.k8s.bean.Service();
        // ObejectMeta
        // “metedata”，为了防止与内部service重名，所以在name前加.labels若没有传入，则定义other，labels作用一是租户、二是区分类别
        if(port==null){
            return ActionReturnUtil.returnError();//需要创建新的service时若未能获取到需要的port，返回失败
        }
        ObjectMeta meta = new ObjectMeta();
        meta.setName(service);
        Map<String, Object> labels = new HashMap<String, Object>();
        labels.put("app",service);
        meta.setNamespace(namespace);
        meta.setLabels(labels);
        // 增加spec
        ServiceSpec serviceSpec = new ServiceSpec();
        serviceSpec.setType("ClusterIP");
        Map<String,Object> rawSelector =new HashMap<>();
        rawSelector.put("app",service);
        serviceSpec.setSelector(rawSelector);

        List<ServicePort> ports = new ArrayList<ServicePort>();
        ServicePort servicePort = new ServicePort();
        servicePort.setProtocol("TCP");
        servicePort.setPort(Integer.valueOf(port));
        servicePort.setTargetPort(Integer.valueOf(port));
        ports.add(servicePort);
        serviceSpec.setPorts(ports);

        newService.setMetadata(meta);
        newService.setSpec(serviceSpec);
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys = CollectionUtil.transBean2Map(newService);
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("Content-Type", "application/json");
        K8SClientResponse response = sService.doServiceByNamespace(namespace, head, bodys, HTTPMethod.POST, cluster);
        //创建失败了
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnError();
        }
        return ActionReturnUtil.returnSuccessWithData(newService);
    }

    @Override
    public ActionReturnUtil getCommands(String namespace, String username, String service) throws Exception {
        //1. 通过服务拿到端口号。
        //2. 拼装成命令

        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);

        K8SClientResponse response = serviceEntryService.getService(namespace, null, cluster, service);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())){
            return ActionReturnUtil.returnError();
        }
        com.harmonycloud.k8s.bean.Service service1 = JsonUtil.jsonToPojo(response.getBody(), com.harmonycloud.k8s.bean.Service.class);

        String targetPort = String.valueOf(service1.getSpec().getPorts().get(0).getTargetPort());
        String[] commandlist = new String[4];
        String lastAdd="";
        commandlist[0] = "./hcdb start -n " + namespace + " -u " + username + " -d";
        commandlist[1] = "./hcdb forward -p " + targetPort;
        commandlist[2] = "./hcdb connection";
        commandlist[3] = "(密码：123456，更多详情使用./hcdb --help)";
        return ActionReturnUtil.returnSuccessWithData(commandlist);
    }

    @Override
    public Boolean checkLink(String namespace, String username, String service) throws Exception {

        //DebugState ds=debugMapper.getStateByUsername(username);
        //ds.setState("debug");
        //debugMapper.update(ds);
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        K8SClientResponse getResponseAgain = serviceEntryService.getService(namespace, null, cluster, service);
        if (!HttpStatusUtil.isSuccessStatus(getResponseAgain.getStatus())){
            return false;
        }
        com.harmonycloud.k8s.bean.Service newService = JsonUtil.jsonToPojo(getResponseAgain.getBody(), com.harmonycloud.k8s.bean.Service.class);
        String port = String.valueOf(newService.getSpec().getPorts().get(0).getPort());
        ActionReturnUtil result = HttpsClientUtil.httpGetRequest("http://"+service+"."+namespace+":"+port,null,null);
        return true;
    }

    @Override
    public DebugState checkService(String namespace,String service) throws Exception {
        return debugMapper.getStateByService(namespace,service);
    }

    @Override
    public DebugState checkUser(String username) throws Exception {
        //检查数据库的表
        return debugMapper.getStateByUsername(username);
    }

    @Override
    public boolean end(String namespace, String username, String service,String port) throws Exception {
        //1. 调用service接口，在namespace下关闭pod1
        //2. 调用service接口，修改service
        //3. 调用mapper,存储user的debug状态信息1

        DebugState ds = debugMapper.getStateByUsername(username);
        //用户正在debug才可以结束。
        if(ds==null||ds.getState().equals("stop")){
            return false;
        }
        //删除Pod
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        podService.deletePod(namespace, ds.getPodname(), cluster);

        //修改service
        K8SClientResponse getResponse = serviceEntryService.getService(namespace, null, cluster, service);
        if (!HttpStatusUtil.isSuccessStatus(getResponse.getStatus())) {
            return false;
        }
        com.harmonycloud.k8s.bean.Service newService = JsonUtil.jsonToPojo(getResponse.getBody(), com.harmonycloud.k8s.bean.Service.class);
        Map<String, String> selector = (Map<String, String>) newService.getSpec().getSelector();
        String app = selector.get("app");
        //如果
        if(app.contains("-debug")) {
            selector.put("app", app.substring(0, app.length() - 6));//去掉-debug
        }
        newService.getSpec().setSelector(selector);

        K8SClientResponse updateResponse = serviceEntryService.updateService(namespace, service, newService, cluster);
        if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
            return false;
        }
        ds.setState("stop");
        //修改用户debug状态。
        debugMapper.update(ds);
        return true;
    }

    @Override
    public List<File> getConfig(String namespace,String system)throws Exception{
        List<File> fileList=new ArrayList<>();
        String systemFile="";
        if(system.equals("windows")) {
            systemFile="hcdb.exe";
        }
        else {
            systemFile="hcdb";
        }
        Cluster cluster=namespaceLocalService.getClusterByNamespaceName(namespace);
        ConfigMap configMap=new ConfigMap();
        ActionReturnUtil result=configMapService.getConfigMapByName("kube-system","admin-conf",HTTPMethod.GET,cluster);
        if(result.getData()!=null){
            configMap=(ConfigMap)result.getData();
        }
        String dir=uploadPath+"admin-config/"+cluster.getId();
        File configDir=new File(dir);
        if(!configDir.exists()){
            configDir.mkdir();
        }
        File newConfigFile=new File(dir+"/config");
        if(!newConfigFile.exists()){
            newConfigFile.createNewFile();
            Map<String,Object> config=(Map<String,Object>)(configMap.getData());
            Object data=config.get("admin.conf");
            FileWriter fileWriter = new FileWriter(newConfigFile,false);
            fileWriter.write(data.toString());
            fileWriter.flush();
            fileWriter.close();
        }
        URL sysurl = DebugServiceImpl.class.getClassLoader().getResource("hcdb/"+system+"/"+systemFile);
        File sys = new File(sysurl.getFile());
        fileList.add(newConfigFile);
        fileList.add(sys);

        return fileList;
    }
}
