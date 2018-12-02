package com.harmonycloud.k8s.service.istio;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.policies.Rule;
import com.harmonycloud.k8s.bean.istio.policies.whitelists.ListChecker;
import com.harmonycloud.k8s.bean.istio.policies.whitelists.ListCheckerList;
import com.harmonycloud.k8s.bean.istio.policies.whitelists.ListEntry;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xc
 * @date 2018/9/17 11:13
 */
@Service
public class WhiteListsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhiteListsService.class);

    public K8SClientResponse getListChecker(String namespace, String name, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setResource(Resource.LISTCHECKER).setNamespace(namespace).setName(name);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.NETWORKING_ISTIO_V1ALPHA3);
        return new K8sMachineClient().exec(url, HTTPMethod.GET, headers, null, cluster);

    }

    public K8SClientResponse getListEntry(String namespace, String name, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setResource(Resource.LISTENTRY).setNamespace(namespace).setName(name);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.NETWORKING_ISTIO_V1ALPHA3);
        return new K8sMachineClient().exec(url, HTTPMethod.GET, headers, null, cluster);

    }

    public K8SClientResponse getRule(String namespace, String name, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setResource(Resource.RULE).setNamespace(namespace).setName(name);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.NETWORKING_ISTIO_V1ALPHA3);
        return new K8sMachineClient().exec(url, HTTPMethod.GET, headers, null, cluster);

    }



    public K8SClientResponse updateListChecker(String namespace, String name, ListChecker listChecker, Cluster cluster) throws MarsRuntimeException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        K8SURL url = new K8SURL();
        url.setResource(Resource.LISTCHECKER).setNamespace(namespace).setName(name);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(listChecker);
        return  new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
    }

    public K8SClientResponse updateListEntry(String namespace, String name, ListEntry listEntry, Cluster cluster) throws MarsRuntimeException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        K8SURL url = new K8SURL();
        url.setResource(Resource.LISTENTRY).setNamespace(namespace).setName(name);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(listEntry);
        return  new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
    }

    public K8SClientResponse updateRule(String namespace, String name, Rule rule, Cluster cluster) throws MarsRuntimeException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        K8SURL url = new K8SURL();
        url.setResource(Resource.RULE).setNamespace(namespace).setName(name);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(rule);
        return  new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
    }

    public List<ListChecker> listWhiteListsPolicy(String namespace, Map<String, Object> bodys, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setResource(Resource.LISTCHECKER).setNamespace(namespace);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("get whiteLists policy error", response.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        ListCheckerList listCheckerList = JsonUtil.jsonToPojo(response.getBody(), ListCheckerList.class);
        return listCheckerList.getItems();
    }

    public K8SClientResponse createListChecker(String namespace, ListChecker listChecker, Cluster cluster)
            throws MarsRuntimeException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.LISTCHECKER);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(listChecker);
        return new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
    }

    public K8SClientResponse createListEntry(String namespace, ListEntry listEntry, Cluster cluster)
            throws MarsRuntimeException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.LISTENTRY);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(listEntry);
        return  new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);

    }

    public K8SClientResponse createWhiteRule(String namespace, Rule rule, Cluster cluster)
            throws MarsRuntimeException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.RULE);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(rule);
        return new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
    }

//    public void deleteWhiteListsPolicy(String namespace, String name, Cluster cluster) throws MarsRuntimeException {
//        K8SURL url = new K8SURL();
//        url.setNamespace(namespace).setResource(Resource.RULE).setName(name);
//        Map<String, Object> headers = new HashMap<>();
//        headers.put("Content-Type", "application/json");
//        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, null, cluster);
//        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
//            LOGGER.error("delete rule error", response.getBody());
//            throw new MarsRuntimeException(ErrorCodeMessage.POLICY_DELETE_FAILED);
//        }
//
//        url.setResource(Resource.LISTENTRY);
//        K8SClientResponse responseListEntry = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, null, cluster);
//        if (!HttpStatusUtil.isSuccessStatus(responseListEntry.getStatus()) && Constant.HTTP_404 != responseListEntry.getStatus()) {
//            LOGGER.error("delete listEntry error", responseListEntry.getBody());
//            throw new MarsRuntimeException(ErrorCodeMessage.POLICY_DELETE_FAILED);
//        }
//
//        url.setResource(Resource.LISTCHECKER);
//        K8SClientResponse responseListChecker = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, null, cluster);
//        if (!HttpStatusUtil.isSuccessStatus(responseListChecker.getStatus()) && Constant.HTTP_404 != responseListChecker.getStatus()) {
//            LOGGER.error("delete quotaSpecBinding error", responseListChecker.getBody());
//            throw new MarsRuntimeException(ErrorCodeMessage.POLICY_DELETE_FAILED);
//        }
//
//    }

    /**
     * 删除ListChecker
     * @param namespace
     * @param name
     * @param cluster
     * @throws MarsRuntimeException
     */
    public K8SClientResponse deleteListCheckerPolicy(String namespace, String name, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setName(name);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        url.setResource(Resource.LISTCHECKER);
        return  new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, null, cluster);

    }

    /***
     * 删除ListEntry
     * @param namespace
     * @param name
     * @param cluster
     * @throws MarsRuntimeException
     */
    public K8SClientResponse deleteListEntryPolicy(String namespace, String name, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setName(name);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        url.setResource(Resource.LISTENTRY);
        return  new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, null, cluster);
    }



    public K8SClientResponse getWhiteListsResource(String namespace, String name, String resourceType, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        if (CommonConstant.WHITE_LISTS_LIST_CHECKER.equals(resourceType)) {
            url.setResource(Resource.LISTCHECKER).setNamespace(namespace).setName(name);
        } else if (CommonConstant.WHITE_LISTS_LIST_ENTRY.equals(resourceType)) {
            url.setResource(Resource.LISTENTRY).setNamespace(namespace).setName(name);
        } else if (CommonConstant.ISTIO_RULE.equals(resourceType)) {
            url.setResource(Resource.RULE).setNamespace(namespace).setName(name);
        } else {
            throw new MarsRuntimeException("不支持WhiteLists类型的查询，类型：" + resourceType);
        }
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("get rateLimit resource error", response.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        return response;
    }

    /**
     * 按照资源创建顺序来逐条删除
     * @param namespace
     * @param name
     * @param cluster
     * @param num 当前资源创建排列顺序
     * @return
     * @throws MarsRuntimeException
     */
    public Map<String, Object> deleteWhiteListsPolicy(String namespace, String name, Cluster cluster, int num) throws MarsRuntimeException {
        Map<String, Object> resMap = new HashMap<>();
        K8SURL url = new K8SURL();
        url.setNamespace(namespace);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        List<String> resources = Arrays.asList(Resource.RULE, Resource.LISTENTRY, Resource.LISTCHECKER);
        for (int i = 3 - num; i < 3; i++) {
            url.setResource(resources.get(i));
            url.setName(i == 0 ? CommonConstant.WHITE_LISTS_PREFIX + name : name);
            K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, null, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
                LOGGER.error("delete " + resources.get(i) + " error", response.getBody());
                resMap.put("faileNum", 3 - i);
                resMap.put("faileResource", resources.get(i));
                return resMap;
            }
        }
        return resMap;
    }

    //通过label删除策略
    public void deleteWhiteListsPolicyByLabel(String namespace, Map<String, Object> bodys, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        K8sMachineClient k8sMachineClient = new K8sMachineClient();
        url.setNamespace(namespace).setResource(Resource.RULE);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        k8sMachineClient.exec(url, HTTPMethod.DELETE, headers, bodys, cluster);

        url.setResource(Resource.LISTENTRY);
        k8sMachineClient.exec(url, HTTPMethod.DELETE, headers, bodys, cluster);

        url.setResource(Resource.LISTCHECKER);
        k8sMachineClient.exec(url, HTTPMethod.DELETE, headers, bodys, cluster);
    }
}
