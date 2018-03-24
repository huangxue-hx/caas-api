package com.harmonycloud.k8s.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.cluster.StatusConditions;

public class k8sUtil {

    public static  List<StatusConditions> GetUpdateStatus(List<StatusConditions> newList, List<StatusConditions> oldList) {
        Map<String, Boolean> newMap = newList.stream().collect(Collectors.toMap(StatusConditions::getType, condition -> condition.getStatus()));
        Map<String, Boolean> oldMap = oldList.stream().collect(Collectors.toMap(StatusConditions::getType, condition -> condition.getStatus()));
        List<StatusConditions> result = new ArrayList<StatusConditions>();
        for (String key : oldMap.keySet()) {
            StatusConditions conditions = new StatusConditions();
            conditions.setType(key);
            if (newMap.containsKey(key)) {
                conditions.setStatus(newMap.get(key));
            } else {
                conditions.setStatus(oldMap.get(key));
            }
            result.add(conditions);
        }
        return result;

    }

    public static String GetNamespaceName(ObjectMeta metadata) {
        String name = metadata.getName();
        String namespace = metadata.getNamespace();
        return namespace+"--"+name;
    }
}
