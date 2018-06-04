package com.harmonycloud.dto.config;

import java.io.Serializable;
import java.util.List;

public class ControllerUrlMapping implements Serializable {

    private static final long serialVersionUID = -3551823489609889016L;

    private String controllerName;
    private String controllerDesc;
    private List<MethodUrlMapping> methodUrlMappings;

    public String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    public String getControllerDesc() {
        return controllerDesc;
    }

    public void setControllerDesc(String controllerDesc) {
        this.controllerDesc = controllerDesc;
    }

    public List<MethodUrlMapping> getMethodUrlMappings() {
        return methodUrlMappings;
    }

    public void setMethodUrlMappings(List<MethodUrlMapping> methodUrlMappings) {
        this.methodUrlMappings = methodUrlMappings;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        //buffer.append(controllerName + " " + controllerDesc + "\n");
        if(methodUrlMappings != null) {
            for (int i = 0; i < methodUrlMappings.size(); i++) {
                buffer.append(controllerName + "|" + controllerDesc + "|" + methodUrlMappings.get(i).toString() + "\n");
            }
        }
        return buffer.toString();
    }
}
