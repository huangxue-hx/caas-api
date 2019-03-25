package com.harmonycloud.dto.cluster;

import java.io.Serializable;

public class ErrDeployDto implements Serializable {
    // TODO 绝大多数errDeployDto的设计及用法很不合理
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String deployName;

    private String errMsg;

    public String getDeployName() {
        return deployName;
    }

    public void setDeployName(String deployName) {
        this.deployName = deployName;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ErrDeployDto [deployName=");
        builder.append(deployName);
        builder.append(", errMsg=");
        builder.append(errMsg);
        builder.append("]");
        return builder.toString();
    }


}