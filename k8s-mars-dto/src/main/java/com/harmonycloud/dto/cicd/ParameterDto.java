package com.harmonycloud.dto.cicd;

import java.util.List;
import java.util.Map;

/**
 * Created by anson on 17/12/24.
 */
public class ParameterDto {
    private Integer jobId;
    private List<Map<String,Object>> parameters;

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public List<Map<String, Object>> getParameters() {
        return parameters;
    }

    public void setParameters(List<Map<String, Object>> parameters) {
        this.parameters = parameters;
    }

}
