package com.harmonycloud.service.platform.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by root on 5/15/17.
 */
public class DeployedService implements Serializable {

    private List<Integer> idList;

    public List<Integer> getIdList() {
        return idList;
    }

    public void setIdList(List<Integer> idList) {
        this.idList = idList;
    }
}
