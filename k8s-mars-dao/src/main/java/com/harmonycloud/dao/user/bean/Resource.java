package com.harmonycloud.dao.user.bean;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Resource {
    private Integer id;

    private String name;

    private String type;

    private String url;

    private Integer parentId;

    private String parentIds;

    private Integer weight;

    private Date createTime;

    private Date updateTime;

    private Boolean available;

    private String transName;

    private String iconName;

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
        this.name = name == null ? null : name.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getParentIds() {
        return parentIds;
    }

    public void setParentIds(String parentIds) {
        this.parentIds = parentIds == null ? null : parentIds.trim();
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getTransName() {
        return transName;
    }

    public void setTransName(String transName) {
        this.transName = transName == null ? null : transName.trim();
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName == null ? null : iconName.trim();
    }
    @JsonIgnore
    public boolean isRootNode() {
        return parentId == 0;
    }

    /**
     * 判断是否为一级菜单
     * @return
     */
    @JsonIgnore
    public boolean isFirstLevelNode(){
        return parentId == 1;
    }

    /**
     * 判断是否为二级菜单
     * @return
     */
    @JsonIgnore
    public boolean isSecondLevelNode(){
        if (parentIds == null){
            return false;
        }
        String[] pIds = parentIds.split("/");
        return ("menu".equals(type) && pIds.length == 2);
    }

    /**
     * 判断是否为三级菜单
     * @return
     */
//    @JsonIgnore
//    public boolean isThirdLevelNode(){
//        if (parentIds == null){
//            return false;
//        }
//        String[] pIds = parentIds.split("/");
//        return (type == ResourceType.menu && pIds.length == 3);
//    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Resource resource = (Resource) o;

        return !(id != null ? !id.equals(resource.id) : resource.id != null);
    }

    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", transName='" + transName + '\'' +
                ", iconName='" + iconName + '\'' +
                ", type=" + type +
                ", url='" + url + '\'' +
                ", parentId=" + parentId +
                ", parentIds='" + parentIds + '\'' +
                ", available=" + available +
                ", weight=" + weight +
                '}';
    }
}