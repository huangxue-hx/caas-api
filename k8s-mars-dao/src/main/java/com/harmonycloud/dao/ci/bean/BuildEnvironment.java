package com.harmonycloud.dao.ci.bean;

import java.io.Serializable;

/**
 * Created by anson on 17/7/25.
 */
public class BuildEnvironment  implements Serializable{
    Integer id;
    String name;
    String image;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
