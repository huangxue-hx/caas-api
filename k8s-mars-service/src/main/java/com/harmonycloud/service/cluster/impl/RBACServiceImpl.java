package com.harmonycloud.service.cluster.impl;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.ClusterRoleBindingService;
import com.harmonycloud.k8s.service.ClusterRoleService;
import com.harmonycloud.k8s.service.ServiceAccountService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.cluster.RBACService;
import com.harmonycloud.service.platform.constant.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RBACServiceImpl implements RBACService {
    @Autowired
    ClusterRoleService clusterRoleService;

    @Autowired
    ClusterRoleBindingService clusterRoleBindingService;

    @Autowired
    ServiceAccountService serviceAccountService;

    /**
     * @param namespace
     * @param name
     * @param cluster
     * @return
     */
    @Override
    public ActionReturnUtil getServiceAccount(String namespace, String name, Cluster cluster) {
        K8SClientResponse response = serviceAccountService.getServiceAccountByName(namespace, name, cluster);
        if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
            ServiceAccount serviceAccount = JsonUtil.jsonToPojo(response.getBody(), ServiceAccount.class);
            return ActionReturnUtil.returnSuccessWithData(serviceAccount);
        }else {
            return null;
        }
    }

    /**
     * @param namespace
     * @param name
     * @param cluster
     * @return
     */
    @Override
    public ActionReturnUtil createServiceAccount(String namespace, String name, Cluster cluster) throws Exception {
        K8SClientResponse response = serviceAccountService.createServiceAccount(namespace, name, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
            return ActionReturnUtil.returnErrorWithData(response.getBody());
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * @param namespace
     * @param clusterRoleBindingName
     * @param clusterRoleName
     * @param serviceAccountName
     * @return
     */
    @Override
    public ActionReturnUtil bindServiceWithClusterRole(String namespace, String clusterRoleBindingName, String clusterRoleName, String serviceAccountName, Cluster cluster) throws Exception{
        ClusterRoleBinding clusterRoleBinding = clusterRoleBindingService.getClusterBindingByName(clusterRoleBindingName, cluster);
        K8SClientResponse response = new K8SClientResponse();
        if(null != clusterRoleBinding){
            List<Subjects> subjectsList = clusterRoleBinding.getSubjects();
            boolean flag = false;
            for (Subjects subjects : subjectsList) {
                if(subjects.getKind().equals("ServiceAccount")
                        && subjects.getName().equals(serviceAccountName)
                        && subjects.getNamespace().equals(namespace)){
                    flag = true;
                }
            }
            if(!flag){
                Subjects subjects = new Subjects();
                subjects.setKind("ServiceAccount");
                subjects.setName(serviceAccountName);
                subjects.setNamespace(namespace);
                subjects.setApiGroup("");
                subjectsList.add(subjects);
                clusterRoleBinding.setSubjects(subjectsList);
                response = clusterRoleBindingService.replaceClusterRoleBinding(clusterRoleBinding, cluster);
            }
        }else {
            clusterRoleBinding = new ClusterRoleBinding();

            ObjectMeta metadata = new ObjectMeta();
            metadata.setName(Constant.CLUSTER_ROLE_BINDING_ONLINESHOP);
            clusterRoleBinding.setMetadata(metadata);

            List<Subjects> subjectsList = new ArrayList<Subjects>();
            Subjects subjects = new Subjects();
            subjects.setKind("ServiceAccount");
            subjects.setName(serviceAccountName);
            subjects.setNamespace(namespace);
            subjects.setApiGroup("");
            subjectsList.add(subjects);
            clusterRoleBinding.setSubjects(subjectsList);

            ObjectReference roleRef = new ObjectReference();
            roleRef.setApiGroup("");
            roleRef.setKind("ClusterRole");
            roleRef.setName(clusterRoleName);
            clusterRoleBinding.setRoleRef(roleRef);

            response = clusterRoleBindingService.createClusterRoleBinding(clusterRoleBinding, cluster);
        }

        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
            return ActionReturnUtil.returnErrorWithData(response.getBody());
        }
        return ActionReturnUtil.returnSuccess();


    }

    /**
     * @param name
     * @param cluster
     * @return
     */
    @Override
    public ActionReturnUtil getClusterRole(String name, Cluster cluster) {
        ClusterRole clusterRole = clusterRoleService.getClusterRoleByName(name, cluster);
        if(null == clusterRole){
            return ActionReturnUtil.returnError();
        }else {
            return ActionReturnUtil.returnSuccessWithData(clusterRole);
        }
    }

    /**
     * @param name
     * @param cluster
     * @return
     */
    @Override
    public ActionReturnUtil createCluserRole(String name, Cluster cluster) throws Exception {
        ClusterRole clusterRole = new ClusterRole();
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(name);
        clusterRole.setMetadata(metadata);

        List<PolicyRule> rules = new ArrayList<PolicyRule>();
        PolicyRule rule = new PolicyRule();

        List<String> apiGroups = new ArrayList<String>();
        apiGroups.add("");
        rule.setApiGroups(apiGroups);

        List<String> resources = new ArrayList<String>();
        resources.add(Resource.NAMESPACE);
        resources.add(Resource.NODE);
        resources.add(Resource.POD);
        resources.add(Resource.SERVICE);
        resources.add(Resource.ENDPOINT);
        rule.setResources(resources);

        List<String> verbs = new ArrayList<String>();
        verbs.add(Constant.GET);
        verbs.add(Constant.LIST);
        rule.setVerbs(verbs);

        rules.add(rule);
        clusterRole.setRules(rules);
        K8SClientResponse response = clusterRoleService.createClusterRole(clusterRole, cluster);

        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
            return ActionReturnUtil.returnErrorWithData(response.getBody());
        }
        return ActionReturnUtil.returnSuccess();
    }
}
