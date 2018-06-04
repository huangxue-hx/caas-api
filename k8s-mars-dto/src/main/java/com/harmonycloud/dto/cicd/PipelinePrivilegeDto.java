package com.harmonycloud.dto.cicd;

import com.harmonycloud.common.enumm.PrivilegeField;
import com.harmonycloud.common.enumm.PrivilegeType;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-1-26
 * @Modified
 */
@PrivilegeType(name = "cicdmgr", cnDesc = "流水线")
public class PipelinePrivilegeDto {

    @PrivilegeField(name = "pipelineName", cnDesc = "流水线名称")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
