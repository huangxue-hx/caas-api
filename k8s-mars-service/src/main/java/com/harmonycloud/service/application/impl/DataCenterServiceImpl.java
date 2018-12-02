package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;

import com.harmonycloud.dto.cluster.DataCenterDto;
import com.harmonycloud.k8s.bean.Namespace;
import com.harmonycloud.k8s.bean.NamespaceList;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.cluster.ClusterCRDList;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.service.ClusterCRDService;
import com.harmonycloud.k8s.service.NamespaceService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.application.DataCenterService;


import com.harmonycloud.service.cluster.ClusterService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.harmonycloud.k8s.util.DefaultClient ;
import org.springframework.util.CollectionUtils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.harmonycloud.service.platform.constant.Constant.TOP_DATACENTER;

@Service
public class DataCenterServiceImpl implements DataCenterService {
    private static final String  DATACENTER_LABEL = "kind=dataCenter";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ClusterService clusterService;
    @Autowired
    private NamespaceService namespaceService;

    private ClusterCRDService clusterCRDService = new ClusterCRDService();

    @Override
    public ActionReturnUtil listDataCenter(Boolean withCluster,Boolean isEnableCluster) throws Exception {
        Cluster cluster = DefaultClient.getDefaultCluster();
        NamespaceList namespaceList = namespaceService.getNamespacesListbyLabelSelector(DATACENTER_LABEL, cluster);
        if (namespaceList.getItems() != null && namespaceList.getItems().isEmpty()) {
            return ActionReturnUtil.returnSuccessWithData(new ArrayList<>());
        }
        List<DataCenterDto> dataCenterDtoList = new ArrayList<>();
        Map<String, List<Cluster>> dataCenterClusterMap = null;
        if(withCluster != null && withCluster){
            dataCenterClusterMap = clusterService.groupCluster(isEnableCluster);
        }
        DataCenterDto topDataCenterDto = null;
        for(Namespace ns : namespaceList.getItems()){
            Map<String, Object> anno = ns.getMetadata().getAnnotations();
            if (null == anno || StringUtils.isBlank((String)anno.get("name"))){
                continue;
            }
            DataCenterDto dataCenterDto = this.convertDto(ns);
            if(withCluster != null && withCluster && !CollectionUtils.isEmpty(dataCenterClusterMap)){
                dataCenterDto.setClusters(dataCenterClusterMap.get(dataCenterDto.getAnnotations()));
            }
            if(dataCenterDto.getName().equals(TOP_DATACENTER)){
                topDataCenterDto = dataCenterDto;
            }else {
                dataCenterDtoList.add(dataCenterDto);
            }
        }

        //将默认数据中心放在最后面，其他数据中心按创建时间排序
        dataCenterDtoList.sort((dataCenter1, dataCenter2) -> dataCenter1.getCreateDate().compareTo(dataCenter2.getCreateDate()));
        dataCenterDtoList.add(topDataCenterDto);
        return ActionReturnUtil.returnSuccessWithData(dataCenterDtoList);
    }

    @Override
    public ActionReturnUtil getDataCenter(String name) throws Exception {
        Cluster cluster = DefaultClient.getDefaultCluster();
        K8SClientResponse response = namespaceService.getNamespace(name, null, null,cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }

        Namespace ns = JsonUtil.jsonToPojo(response.getBody(), Namespace.class);
        DataCenterDto dataCenterDto = this.convertDto(ns);
        return ActionReturnUtil.returnSuccessWithData(dataCenterDto);

    }

    @Override
    public ActionReturnUtil deleteDataCenter(String name) throws Exception {
        Cluster cluster = DefaultClient.getDefaultCluster();
        ClusterCRDList clusterCRDList = clusterCRDService.listCluster(null,name,cluster);
        if (!(clusterCRDList == null ||clusterCRDList.getItems() == null ||  (  clusterCRDList.getItems() != null && clusterCRDList.getItems().isEmpty()))){
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DATACENTER_HAS_CLUSTER);
        }
        K8SClientResponse response = namespaceService.delete(null,null, HTTPMethod.DELETE, name, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnSuccessWithData(K8SClient.converToBean(response, Namespace.class));
        } else {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
    }

    @Override
    public ActionReturnUtil updateDataCenter(String name, String annotations) throws  Exception {
        Cluster cluster = DefaultClient.getDefaultCluster();
        K8SClientResponse response = namespaceService.getNamespace(name, null, null,cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
        Namespace ns = JsonUtil.jsonToPojo(response.getBody(), Namespace.class);
        Map<String, Object> annos = ns.getMetadata().getAnnotations();
        if (annos != null ){
            if (annotations.equals(annos.get("name"))){
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DATACENTER_NICENAME_SAME);

            }
            ns.getMetadata().setAnnotations(this.getAnnotations(annotations));
        } else {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DATACENTER_NOT_HAS_NICENAME);
        }
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys = CollectionUtil.transBean2Map(ns);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        K8SClientResponse updateResponse = namespaceService.update(headers,bodys,name,cluster);
        if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
            return  ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DATACENTER_UPDATE_FAIL);
        }
        return ActionReturnUtil.returnSuccess();

    }

    @Override
    public ActionReturnUtil addDataCenter(DataCenterDto dataCenterDto) throws Exception {
        if (!this.isEmpty(dataCenterDto)) {
            logger.error("dataCeneterDto Object or parameters is null");
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = DefaultClient.getDefaultCluster();
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setLabels(this.getDefaultLables());
        objectMeta.setName(dataCenterDto.getName());
        objectMeta.setAnnotations(this.getAnnotations(dataCenterDto.getAnnotations()));
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.KIND, CommonConstant.NAMESPACE);
        bodys.put(CommonConstant.METADATA, objectMeta);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);

        K8SClientResponse k8SClientResponse = namespaceService.create(headers, bodys, HTTPMethod.POST, cluster);

        if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            logger.error("Create namespace, error：" + k8SClientResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(k8SClientResponse.getBody());
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 判断对象是否为null以及对象内部参数是否为
     * @param dataCenterDto
     * @return
     * @throws Exception
     */
    private boolean isEmpty(DataCenterDto dataCenterDto) throws Exception {
        if (dataCenterDto == null) {
            return false;
        }
        String anno = dataCenterDto.getAnnotations();
        String name = dataCenterDto.getName();
        if (StringUtils.isBlank(anno) || StringUtils.isBlank(name)) {
            return false;
        }
        return true;
    }

    /**
     * label（kind = ）标识namespace 为dataceneter dataCenter 类型
     * @return
     * @throws Exception
     */
    private Map<String, Object> getDefaultLables() throws Exception {
        Map<String, Object> labels = new HashMap<>();
        labels.put("kind","dataCenter");
        return labels;
    }

    /**
     * 数据中心的中文名只能存在 namespace.metadata.annotations下
     * 查询，删除这些接口使用的依旧是namespace.metadata.name 作为唯一标识查询
     * @param annotations
     * @return
     * @throws Exception
     */
    private Map<String, Object> getAnnotations(String annotations) throws Exception {
        Map<String, Object> annot = new HashMap<>();
        annot.put("name",annotations);
        return annot;
    }

    /**
     * 转换成对应的Dto类型
     * @return
     * @throws Exception
     */
    private DataCenterDto convertDto(Namespace namespace) throws  Exception {
        DataCenterDto dataCenterDto = new DataCenterDto();
        dataCenterDto.setAnnotations((String)namespace.getMetadata().getAnnotations().get("name"));
        dataCenterDto.setName(namespace.getMetadata().getName());
        dataCenterDto.setCreateDate(DateUtil.utcToGmtDate(namespace.getMetadata().getCreationTimestamp()));
        return dataCenterDto;
    }
}
