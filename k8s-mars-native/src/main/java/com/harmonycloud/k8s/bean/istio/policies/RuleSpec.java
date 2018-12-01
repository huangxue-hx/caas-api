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
public class RuleSpec {

    private List<Action> actions;

    private String match;

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        RuleSpec spec = (RuleSpec) o;
        if (!CollectionUtils.isEmpty(this.actions)) {
            if (CollectionUtils.isEmpty(spec.actions)) {
                return false;
            } else if (!checkList(this.actions, spec.actions)) {
                return false;
            }
        } else {
            if (!CollectionUtils.isEmpty(spec.actions)) {
                return false;
            }
        }
        if (this.match != null) {
            if (!this.match.equals(spec.match)) {
                return false;
            }
        } else {
            if (spec.match != null) {
                return false;
            }
        }
        return true;
    }

    private boolean checkList(List<Action> thisRules, List<Action> objRules) {
        if (thisRules.size() != objRules.size()) {
            return false;
        }
        for (int i = 0; i < thisRules.size(); i++) {
            if (!thisRules.get(i).equals(objRules.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getActions() != null ? getActions().hashCode() : 0;
        result = 31 * result + (getMatch() != null ? getMatch().hashCode() : 0);
        return result;
    }
}
