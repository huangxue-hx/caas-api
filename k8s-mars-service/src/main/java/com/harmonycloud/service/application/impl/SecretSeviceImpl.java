package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.k8s.bean.Secret;
import com.harmonycloud.k8s.bean.SecretList;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.SecretService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.UserService;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author jmi
 *
 */
@Service
public class SecretSeviceImpl implements SecretService {
    @Autowired
    TenantService tenantService;
    @Autowired
    UserService userService;
    @Autowired
    ClusterService clusterService;
    @Autowired
    NamespaceLocalService namespaceLocalService;
    
    @Override
    public ActionReturnUtil checkedSecret(String userName, String password) throws Exception {
        User user = userService.getUser(userName);
        List<TenantBinding> tenantBindingList = this.tenantService.listTenantsByUserName(userName);
        if (!tenantBindingList.isEmpty()) {
            //获取集群信息
            Map<String,Cluster> clusterMap = new HashMap<String,Cluster>();
            List<Cluster> clusters = clusterService.listCluster();
            for (Cluster cluster1:clusters) {
                clusterMap.put(cluster1.getId().toString(),cluster1);
            }
            for (TenantBinding tenantBinding : tenantBindingList) {
                List<NamespaceLocal> namespaceList = namespaceLocalService.getNamespaceListByTenantId(tenantBinding.getTenantId());
                if(!namespaceList.isEmpty()){
                    for (NamespaceLocal ns:namespaceList) {
                        Cluster cluster = clusterMap.get(ns.getClusterId().toString());
                        if(cluster!=null){
                            if(user.getIsAdmin() == 1){
                                userName = cluster.getHarborServer().getHarborAdminAccount();
                                password = cluster.getHarborServer().getHarborAdminPassword();
                            }
                            this.doSecret(ns.getNamespaceName(), userName, password,cluster);
                        }
                    }
                }

            }
        }


        return ActionReturnUtil.returnSuccess();
    }
    public ActionReturnUtil doSecret(String namespace, String userName, String password,Cluster cluster) throws Exception {

        String name = userName + "-secret";
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.SECRET);
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys.put("labelSelector", "nephele_user"+ "=" + userName);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys,cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
        SecretList secretList = JsonUtil.jsonToPojo(response.getBody(), SecretList.class);
        List<Secret> secrets = secretList.getItems();
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-Type", "application/json");
        if (secrets != null && secrets.size() > 0) {
            // 更新
            url.setName(name);
            K8SClientResponse update = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, produceBodys(namespace, name, userName, password,
                    cluster.getHarborServer().getHarborHost()),cluster);
            if (!HttpStatusUtil.isSuccessStatus(update.getStatus())) {
                return ActionReturnUtil.returnErrorWithMsg(update.getBody());
            }
            Secret secret = JsonUtil.jsonToPojo(update.getBody(), Secret.class);
            return ActionReturnUtil.returnSuccessWithData(secret.getMetadata().getName());
        } else {
            // 创建
            K8SClientResponse create = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, produceBodys(namespace, name, userName, password,
                    cluster.getHarborServer().getHarborHost()),cluster);
            if (!HttpStatusUtil.isSuccessStatus(create.getStatus())) {
                return ActionReturnUtil.returnErrorWithMsg(create.getBody());
            }
            Secret secret = JsonUtil.jsonToPojo(create.getBody(), Secret.class);
            return ActionReturnUtil.returnSuccessWithData(secret.getMetadata().getName());
        }
    }
    private String getDockercfgStr(String userName, String password, String domain) throws Exception {
        Base64 base64 = new Base64();
        String str = userName + ":" + password;
        String auth = new String(base64.encode(str.getBytes("UTF-8")), "UTF-8");
        Map<String, Object> dockercfgBody = new HashMap<String, Object>();
        Map<String, Object> harbor = new HashMap<String, Object>();
        harbor.put("username", userName);
        harbor.put("password", password);
        harbor.put("email", "");
        harbor.put("auth", auth);
        dockercfgBody.put(domain, harbor);
        String reString = JsonUtil.convertToJson(dockercfgBody);
        return new String(base64.encode(reString.getBytes("UTF-8")), "UTF-8");
    }

    private Map<String, Object> produceBodys(String namespace, String name, String userName, String password, String domain) throws Exception {
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys.put("kind", "Secret");
        bodys.put("apiVersion", "v1");
        bodys.put("type", "kubernetes.io/dockercfg");
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("namespace", namespace);
        meta.put("name", name);
        Map<String, Object> labels = new HashMap<String, Object>();
        labels.put("nephele_user", userName);
        meta.put("labels", labels);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(".dockercfg", getDockercfgStr(userName, password, domain));
        bodys.put("metadata", meta);
        bodys.put("data", data);
        return bodys;
    }

    public void createHarborSecret(String name, String namespace, String harborHost, String username, String password) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.SECRET);
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys.put("labelSelector", "cluster=" + name);
        Cluster topCluster = clusterService.getPlatformCluster();
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, topCluster);
        if(!HttpStatusUtil.isSuccessStatus(response.getStatus())) {

        }
        SecretList secretList = JsonUtil.jsonToPojo(response.getBody(), SecretList.class);
        List<Secret> secrets = secretList.getItems();
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-Type", "application/json");
        bodys = new HashMap<String, Object>();
        bodys.put("kind", "Secret");
        bodys.put("apiVersion", "v1");
        bodys.put("type", "kubernetes.io/dockercfg");
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("namespace", namespace);
        meta.put("name", name + "-secret");
        Map<String, Object> labels = new HashMap<String, Object>();
        labels.put("cluster", name);
        meta.put("labels", labels);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(".dockercfg", getDockercfgStr(username, password, harborHost));
        bodys.put("metadata", meta);
        bodys.put("data", data);
        if (CollectionUtils.isNotEmpty(secrets)) {
            // 更新
            url.setName(name + "-secret");
            K8SClientResponse update = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, topCluster);
            if (!HttpStatusUtil.isSuccessStatus(update.getStatus())) {
                throw new MarsRuntimeException(ErrorCodeMessage.SECRET_ADD_ERROR);
            }
        } else {
            // 创建
            K8SClientResponse create = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, topCluster);
            if (!HttpStatusUtil.isSuccessStatus(create.getStatus())) {
                throw new MarsRuntimeException(ErrorCodeMessage.SECRET_UPDATE_ERROR);
            }
        }
    }

}
