package com.harmonycloud.k8s.service;

import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.cluster.*;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ClusterTemplateCRDService {
    public K8SClientResponse getClusterTemplate(String namespace, String name, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setApiGroup(Resource.getGroupByResource(Resource.CLUSTERTEMPLATE));
        url.setNamespace(namespace).setResource(Resource.CLUSTERTEMPLATE).setSubpath(name);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        return response;
    }

    public TemplateList listClusterTemplates(String namespace, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setApiGroup(Resource.getGroupByResource(Resource.CLUSTERTEMPLATE));
        url.setNamespace(namespace).setResource(Resource.CLUSTERTEMPLATE);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        TemplateList templateList = K8SClient.converToBean(response, TemplateList.class);
        return templateList;
    }

    public K8SClientResponse deleteClusterTemplate(String namespace, String name, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setApiGroup(Resource.getGroupByResource(Resource.CLUSTERTEMPLATE));
        url.setNamespace(namespace).setResource(Resource.CLUSTERTEMPLATE).setSubpath(name);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE,null,null, cluster);
        return response;
    }

    public K8SClientResponse addClusterTemplate(Template template, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setApiGroup(Resource.getGroupByResource(Resource.CLUSTERTEMPLATE));
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        url.setNamespace(template.getMetadata().getNamespace()).setResource(Resource.CLUSTERTEMPLATE);
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys = CollectionUtil.transBean2Map(template);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST,headers,bodys, cluster);
        return response;
    }

    public K8SClientResponse updateClusterTemplate( Template template,Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setApiGroup(Resource.getGroupByResource(Resource.CLUSTERTEMPLATE));
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        ObjectMeta meta = template.getMetadata();
        url.setNamespace(meta.getNamespace()).setResource(Resource.CLUSTERTEMPLATE).setSubpath(meta.getName());
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys = CollectionUtil.transBean2Map(template);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers,bodys, cluster);
        return response;
    }
}
