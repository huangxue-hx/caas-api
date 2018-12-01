package com.harmonycloud.k8s.bean.istio.policies;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * update by weg on 18-11-27.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Action {

    private String handler;

    private List<String> instances;

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public List<String> getInstances() {
        return instances;
    }

    public void setInstances(List<String> instances) {
        this.instances = instances;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        Action action = (Action) o;
        if (this.handler != null) {
            if (!this.handler.equals(action.handler)) {
                return false;
            }
        } else {
            if (action.handler != null) {
                return false;
            }
        }
        if (!CollectionUtils.isEmpty(this.instances)) {
            if (CollectionUtils.isEmpty(action.instances)) {
                return false;
            } else if (!checkList(this.instances, action.instances)) {
                return false;
            }
        } else {
            if (!CollectionUtils.isEmpty(action.instances)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkList(List<String> thisInstance, List<String> objInstance) {
        if (thisInstance.size() != objInstance.size()) {
            return false;
        }
        for (int i = 0; i < thisInstance.size(); i++) {
            if (!thisInstance.get(i).equals(objInstance.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getHandler() != null ? getHandler().hashCode() : 0;
        result = 31 * result + (getInstances() != null ? getInstances().hashCode() : 0);
        return result;
    }
}
