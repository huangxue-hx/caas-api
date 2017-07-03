package com.harmonycloud.service.platform.bean;
import com.harmonycloud.service.platform.bean.HarborPolicyStatus;

/**
 * Created by root on 5/20/17.
 */
public class HarborPolicyDetail {
    private Integer project_id;
    private Integer policy_id;
    private String project_name;
    private String policy_name;
    private Integer enabled;
    private String update_time;

    public Integer getProject_id() {
        return project_id;
    }

    public void setProject_id(Integer project_id) {
        this.project_id = project_id;
    }

    public Integer getPolicy_id() {
        return policy_id;
    }

    public void setPolicy_id(Integer policy_id) {
        this.policy_id = policy_id;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getPolicy_name() {
        return policy_name;
    }

    public void setPolicy_name(String policy_name) {
        this.policy_name = policy_name;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public HarborPolicyStatus getHarborPolicyStatus() {
        return harborPolicyStatus;
    }

    public void setHarborPolicyStatus(HarborPolicyStatus harborPolicyStatus) {
        this.harborPolicyStatus = harborPolicyStatus;
    }

    private HarborPolicyStatus harborPolicyStatus;

}
