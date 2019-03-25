package com.harmonycloud.service.platform.bean.harbor;

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
    private Integer target_id;
    private String target_name;
    private String target_project_name;
    private String creation_time;
    private String start_time;
    private Integer error_job_count;
    private HarborPolicyStatus harborPolicyStatus;
    private HarborPolicyFilter harborPolicyFilter;
    private String harborPolicyTrigger;

    public String getHarborPolicyTrigger() {
        return harborPolicyTrigger;
    }

    public void setHarborPolicyTrigger(String harborPolicyTrigger) {
        this.harborPolicyTrigger = harborPolicyTrigger;
    }

    public HarborPolicyFilter getHarborPolicyFilter() {
        return harborPolicyFilter;
    }

    public void setHarborPolicyFilter(HarborPolicyFilter harborPolicyFilter) {
        this.harborPolicyFilter = harborPolicyFilter;
    }

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

    public String getTarget_name() {
        return target_name;
    }

    public void setTarget_name(String target_name) {
        this.target_name = target_name;
    }

    public String getCreation_time() {
        return creation_time;
    }

    public void setCreation_time(String creation_time) {
        this.creation_time = creation_time;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public Integer getError_job_count() {
        return error_job_count;
    }

    public void setError_job_count(Integer error_job_count) {
        this.error_job_count = error_job_count;
    }

    public Integer getTarget_id() {
        return target_id;
    }

    public void setTarget_id(Integer target_id) {
        this.target_id = target_id;
    }

    public String getTarget_project_name() {
        return target_project_name;
    }

    public void setTarget_project_name(String target_project_name) {
        this.target_project_name = target_project_name;
    }
}