package com.harmonycloud.dto.business;

import java.io.Serializable;
import java.util.List;

/**
 * Created by root on 5/18/17.
 */
public class DeployedServiceNamesDto implements Serializable {

    private List<String> nameList;

    public List<String> getNameList() {
        return nameList;
    }

    public void setNameList(List<String> nameList) {
        this.nameList = nameList;
    }
}
