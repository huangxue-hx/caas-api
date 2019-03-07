package com.harmonycloud.dto.apiserver;


import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * @author liangli
 */
public class ClusterNamespaceDto implements Serializable {

    /**
     * 集群id，为cluster tpr的uid
     */
    private String id;
    /**
     * 集群名称
     */
    private String name;

    /**
     * 集群别名
     */
    private String aliasName;
    /**
     * 集群所属的数据中心
     */
    private String dataCenter;

    /**
     * 集群下的namespace集合
     */
    private List<JSONObject> namespaces;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    public List<JSONObject> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<JSONObject> namespaces) {
        this.namespaces = namespaces;
    }
}
