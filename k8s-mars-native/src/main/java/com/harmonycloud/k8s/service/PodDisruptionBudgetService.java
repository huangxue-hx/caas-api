package com.harmonycloud.k8s.service;

import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.bean.LabelSelector;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.PodDisruptionBudget;
import com.harmonycloud.k8s.bean.PodDisruptionBudgetSpec;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class PodDisruptionBudgetService {


    public K8SClientResponse createPdbByMinAvilable(String namespace, String name, LabelSelector labelSelector, int minAvilable, Cluster cluster) throws Exception{

        PodDisruptionBudget pdb = new PodDisruptionBudget();
        PodDisruptionBudgetSpec pdbspec = new PodDisruptionBudgetSpec();
        ObjectMeta objectMeta = new ObjectMeta();

        pdbspec.setSelector(labelSelector);
        pdbspec.setMinAvailable(minAvilable);

        objectMeta.setName(name);
        pdb.setSpec(pdbspec);
        pdb.setMetadata(objectMeta);

        K8SURL k8surl = new K8SURL();
        k8surl.setNamespace(namespace).setResource(Resource.POD_DISRUPTION_BUDGET);

        Map<String, Object> bodys = CollectionUtil.transBean2Map(pdb);

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");

        K8SClientResponse response =new  K8sMachineClient().exec(k8surl, HTTPMethod.POST, headers, bodys, cluster);
        return response;


    }


    public K8SClientResponse deletePdb(String namespace, String name, Cluster cluster){
        K8SURL k8SURL = new K8SURL();
        k8SURL.setNamespace(namespace).setResource(Resource.POD_DISRUPTION_BUDGET).setName(name);
        return new K8sMachineClient().exec(k8SURL, HTTPMethod.DELETE, null, null, cluster);
    }


    public boolean existPdb (String namespace, String name, Cluster cluster) {
        K8SURL k8SURL = new K8SURL();
        k8SURL.setNamespace(namespace).setResource(Resource.POD_DISRUPTION_BUDGET).setName(name);
        K8SClientResponse response = new  K8sMachineClient().exec(k8SURL, HTTPMethod.GET, null, null, cluster);
        return  HttpStatusUtil.isSuccessStatus(response.getStatus());
    }



}
