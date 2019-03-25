package com.harmonycloud.k8s.service;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.bean.StorageClass;
import com.harmonycloud.k8s.bean.StorageClassList;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xc
 * @date 2018/6/14 16:15
 */
@Service
public class ScService {

    private static final String NFS_PROVISIONER_NAME = "nfs-client-provisioner";
    /**
     * 根据StorageClass名称查询
     * @param name StorageClass名称
     * @param cluster 集群信息对象
     * @return StorageClass
     */
    public StorageClass getScByName(String name, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.APIS_STORAGECLASS_VERSION);
        url.setResource(Resource.STORAGECLASS);
        url.setSubpath(name);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null, cluster);
        if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
            return K8SClient.converToBean(response, StorageClass.class);
        }
        return null;
    }

    /**
     * 根据NfsProvisioner名称查找
     * @param name NfsProvisioner名称
     * @param cluster 集群信息对象
     * @return Deployment
     */
    public Deployment getNfsProvisionerByName(String name, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.APIS_APPS_V1);
        url.setNamespace(CommonConstant.KUBE_SYSTEM);
        url.setResource(Resource.DEPLOYMENT);
        url.setSubpath(name);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null, cluster);
        if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
            return K8SClient.converToBean(response, Deployment.class);
        }
        return null;
    }

    /**
     * 通过StorageClass名称删除StorageClass
     * @param name StorageClass名称
     * @param cluster 集群信息
     * @return K8SClientResponse
     */
    public K8SClientResponse deleteStorageClassByName(String name, Cluster cluster, String type) throws Exception {
        K8SURL url = new K8SURL();
        if (type.equals("NFS")){
            url.setNamespace(CommonConstant.KUBE_SYSTEM);
            url.setApiGroup(APIGroup.APIS_APPS_V1);
            url.setResource(Resource.DEPLOYMENT);
            url.setSubpath(NFS_PROVISIONER_NAME + "-" + name);
            K8SClientResponse scResponse = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null, cluster);
        }
        url = new K8SURL();
        url.setApiGroup(APIGroup.APIS_STORAGECLASS_VERSION);
        url.setResource(Resource.STORAGECLASS);
        url.setSubpath(name);
        return new  K8sMachineClient().exec(url, HTTPMethod.DELETE,null,null, cluster);
    }

    /**
     * 通过集群id查找所有的StorageClass
     * @param cluster 集群信息
     * @return List<StorageClass>
     */
    public List<StorageClass> litStorageClassByClusterId(Cluster cluster) {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.APIS_STORAGECLASS_VERSION);
        url.setResource(Resource.STORAGECLASS);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null, cluster);
        if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
            StorageClassList storageClassList = K8SClient.converToBean(response, StorageClassList.class);
            List<StorageClass> storageClasses = storageClassList.getItems();
            return storageClasses;
        }
        throw new MarsRuntimeException(response.getBody());
    }

}
