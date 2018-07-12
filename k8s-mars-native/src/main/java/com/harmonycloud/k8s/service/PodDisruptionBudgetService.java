package com.harmonycloud.k8s.service;

import com.harmonycloud.common.Constant.CommonConstant;
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

    public final static String PDB_TYPE_MIN_AVAILABLE = "minAvailable";
    public final static String PDB_TYPE_MAX_UNAVAILABLE = "maxUnavailable";


    /**
     *
     * @param namespace
     * @param name
     * @param labelSelector
     * @param type    可选值：1、minAvailable；2、maxUnavailable；3、其他（K8S默认设置为minAvailable=1）
     * @param value
     * @param cluster
     * @return
     * @throws Exception
     */
    public  K8SClientResponse createPdbByType(String namespace, String name, LabelSelector labelSelector, String type, String  value, Cluster cluster) throws Exception{

        PodDisruptionBudget pdb = new PodDisruptionBudget();
        PodDisruptionBudgetSpec pdbspec;
        ObjectMeta objectMeta = new ObjectMeta();

        if(value.endsWith(CommonConstant.PERCENT)){
            pdbspec = new PodDisruptionBudgetSpec<String>();
            if(PDB_TYPE_MIN_AVAILABLE.equals(type)){
                pdbspec.setMinAvailable(value);
            }else if(PDB_TYPE_MAX_UNAVAILABLE.equals(type)){
                pdbspec.setMaxUnavailable(value);
            }
        }else {
            pdbspec = new PodDisruptionBudgetSpec<Integer>();
            if(PDB_TYPE_MIN_AVAILABLE.equals(type)){
                pdbspec.setMinAvailable(Integer.valueOf(value));
            }else if(PDB_TYPE_MAX_UNAVAILABLE.equals(type)){
                pdbspec.setMaxUnavailable(Integer.valueOf(value));
            }
        }


        objectMeta.setName(name);
        pdb.setSpec(pdbspec);
        pdbspec.setSelector(labelSelector);
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
