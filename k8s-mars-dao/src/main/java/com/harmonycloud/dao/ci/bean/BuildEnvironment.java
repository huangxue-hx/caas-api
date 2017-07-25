package com.harmonycloud.dao.ci.bean;

/**
 * Created by anson on 17/7/25.
 */
public class BuildEnvironment {
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
