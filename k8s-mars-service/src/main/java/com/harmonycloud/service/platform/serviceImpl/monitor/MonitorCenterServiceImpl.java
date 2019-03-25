package com.harmonycloud.service.platform.serviceImpl.monitor;


import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.Container;
import com.harmonycloud.k8s.bean.Pod;
import com.harmonycloud.k8s.bean.PodList;
import com.harmonycloud.k8s.bean.StatefulSet;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.service.PodService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.StatefulSetsService;
import com.harmonycloud.service.platform.bean.monitor.InfluxdbQuery;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.InfluxdbService;
import com.harmonycloud.service.platform.service.MonitorCenterService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MonitorCenterServiceImpl implements MonitorCenterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorCenterServiceImpl.class);

    @Autowired
    private DeploymentsService deploymentsService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private PodService podService;

    @Autowired
    private InfluxdbService influxdbService;

    @Autowired
    private StatefulSetsService statefulSetsService;

    @Override
    public ActionReturnUtil getProjectMonit(String tenantId, String projectId, String namespaceList, String rangeType) throws Exception {
        ActionReturnUtil result = deploymentsService.listDeployments(tenantId, null, namespaceList, null, projectId, null);
        List<Map<String, Object>> statefulSetList = statefulSetsService.listStatefulSets(tenantId, null, namespaceList, null, projectId, null);
        List<Map<String,Object>> deployList = (ArrayList<Map<String,Object>>)result.getData();
        List<Pod> allPodList = new ArrayList<>();
        //容器内存与cpu配额
        List<Object> requestsList = new ArrayList<>();
        deployList.addAll(statefulSetList);
        for (Map<String,Object> oneDeploy : deployList) {
            String status = oneDeploy.containsKey("status")? oneDeploy.get("status").toString() : null;
            if (StringUtils.isNotBlank(status) && Constant.SERVICE_START.equals(status)) {
                String name = String.valueOf(oneDeploy.get("name"));
                String svcNamespace = String.valueOf(oneDeploy.get("namespace"));
                Cluster cluster = namespaceLocalService.getClusterByNamespaceName(svcNamespace);
                PodList podList = !Constant.STATEFULSET.equals(oneDeploy.get("serviceType"))?
                        podService.getPodByServiceName(svcNamespace, name, HTTPMethod.GET, cluster, Constant.TYPE_DEPLOYMENT) :
                        podService.getPodByServiceName(svcNamespace, name, HTTPMethod.GET, cluster, Constant.TYPE_STATEFULSET);
                List<Pod> podListInSvc = podList.getItems();
                for(Pod pod : podListInSvc) {
                    List<Container> containers = pod.getSpec().getContainers();
                    for(Container container:containers){
                        requestsList.add(container.getResources().getRequests());
                    }
                    allPodList.add(pod);
                }
            }
        }
        //项目cpu总配额
        double cpuTotal = 0;
        //项目内存总配额
        double memoryTotal = 0;
        for(Object obj : requestsList){
            Map<String,Object> requests = (HashMap<String,Object>)obj;
            //cpu
            String cpu = requests.get("cpu").toString();
            if (cpu != null) {
                //0.1core为100m
                if (cpu.contains("m")) {
                    String cpuStr = cpu.replace("m","");
                    cpuTotal = cpuTotal+Double.valueOf(cpuStr);
                } else {
                    //cpu为1core时,container里配置为1,而不是1000m
                    cpuTotal = cpuTotal + Double.valueOf(cpu) * CommonConstant.NUM_THOUSAND;
                }
            }

            //内存
            String memory = requests.get("memory").toString();
            if (memory != null) {
                //128MB为128Mi,1024MB为1Gi
                if (memory.contains("Mi")) {
                    String memoryStr = memory.replace("Mi","");
                    memoryTotal = memoryTotal + Double.valueOf(memoryStr);
                } else if (memory.contains("Gi")) {
                    String memoryStr = memory.replace("Gi","");
                    memoryTotal = memoryTotal + Double.valueOf(memoryStr) * CommonConstant.NUM_SIZE_MEMORY;
                }
            }
        }
        String[] targetArr={"cpu","memory","disk","volume","rx","tx"};
        //所有pod在一段时间节点内cpu使用量
        List<QueryResult.Series> cpuTargetList=new ArrayList<>();
        List<QueryResult.Series> memoryTargetList=new ArrayList<>();
        List<QueryResult.Series> diskTargetList=new ArrayList<>();
        List<QueryResult.Series> volumeTargetList=new ArrayList<>();
        List<QueryResult.Series> rxTargetList=new ArrayList<>();
        List<QueryResult.Series> txTargetList=new ArrayList<>();
        for (Pod onePod : allPodList) {
            for(int i = 0; i<targetArr.length; i++){
                Cluster cluster = namespaceLocalService.getClusterByNamespaceName(onePod.getMetadata().getNamespace());
                InfluxdbQuery influxdbQuery = new InfluxdbQuery();
                influxdbQuery.setRangeType(rangeType);
                influxdbQuery.setMeasurement(targetArr[i]);
                influxdbQuery.setStartTime(onePod.getStatus().getStartTime());
                influxdbQuery.setClusterId(cluster.getId());
                influxdbQuery.setPod(onePod.getMetadata().getName());
                ActionReturnUtil response = influxdbService.podMonit(influxdbQuery, null);
                if (!response.isSuccess()) {
                    continue;
                }
                Map<String, Object> seriesMap = (Map<String, Object>) response.getData();
                String data = JsonUtil.convertToJson(seriesMap);
                QueryResult queryResult = JsonUtil.jsonToPojo(data, QueryResult.class);
                if(!Objects.isNull(queryResult) && !org.springframework.util.CollectionUtils.isEmpty(queryResult.getResults())
                        && !org.springframework.util.CollectionUtils.isEmpty(queryResult.getResults().get(0).getSeries())) {
                    QueryResult.Series series = queryResult.getResults().get(0).getSeries().get(0);
                    if("cpu".equals(targetArr[i])){cpuTargetList.add(series);}
                    if("memory".equals(targetArr[i])){memoryTargetList.add(series);}
                    if("disk".equals(targetArr[i])){diskTargetList.add(series);}
                    if("volume".equals(targetArr[i])){volumeTargetList.add(series);}
                    if("rx".equals(targetArr[i])){rxTargetList.add(series);}
                    if("tx".equals(targetArr[i])){txTargetList.add(series);}
                }
            }
        }

        List<Object[]> projectCpuUsage = getResourceUsage(cpuTargetList,"cpu");
        List<Object[]> projectMemoryUsage = getResourceUsage(memoryTargetList,"memory");
        List<Object[]> projectDiskUsage = getResourceUsage(diskTargetList,"disk");
        List<Object[]> projectVolumeUsage = getResourceUsage(volumeTargetList,"volume");
        List<Object[]> projectRxUsage = getResourceUsage(rxTargetList,"rx");
        List<Object[]> projectTXUsage = getResourceUsage(txTargetList,"tx");

        Map<String,Object> resultMap = new HashMap<>(CommonConstant.NUM_EIGHT);
        resultMap.put("cpuTotal", cpuTotal);
        resultMap.put("memoryTotal", memoryTotal);
        resultMap.put("projectCpuUsage", projectCpuUsage);
        resultMap.put("projectMemoryUsage", projectMemoryUsage);
        resultMap.put("projectDiskUsage", projectDiskUsage);
        resultMap.put("projectVolumeUsage", projectVolumeUsage);
        resultMap.put("projectRxUsage", projectRxUsage);
        resultMap.put("projectTXUsage", projectTXUsage);

        return ActionReturnUtil.returnSuccessWithData(resultMap);
    }

    private List<Object[]> getResourceUsage(List<QueryResult.Series> projectResource, String target) {
        List<Object[]> resourceUsage = new ArrayList<>();
        if (CollectionUtils.isEmpty(projectResource)){
            return null;
        }
        QueryResult.Series podSeries = new QueryResult.Series();
        for(int i=0; i<projectResource.size(); i++){
            try{
                if(CollectionUtils.isNotEmpty(projectResource.get(i).getValues())){
                    podSeries = projectResource.get(i);
                    break; }
            }catch (NullPointerException e){
                LOGGER.warn("监控实时值为空");
            }
        }
        for(int i=0;i<podSeries.getValues().size();i++){
            Object[] resNode = new Object[2];
            String time = "";
            Double Usage = 0.0;
            for(int j=0;j<projectResource.size();j++){
                try{
                    Object[] node=projectResource.get(j).getValues().get(i).toArray();  //获取每个pod的同一个时间节点的资源信息列表
                    if(node.length>=2&&node[1]!=null&&!"".equals(node[1].toString())){
                        if("memory".equals(target)||"disk".equals(target)||"volume".equals(target)){
                            Usage = Usage + Double.valueOf(node[1].toString())/1024/1024;
                        }else {
                            Usage = Usage+Double.valueOf(node[1].toString());
                        }
                    }
                    time = node[0].toString();
                }catch (NullPointerException e){
                    LOGGER.warn("监控实时值为空");
                }
            }
            resNode[0] = time;
            resNode[1] = Usage;
            resourceUsage.add(resNode);
        }
        return resourceUsage;
    }
}
