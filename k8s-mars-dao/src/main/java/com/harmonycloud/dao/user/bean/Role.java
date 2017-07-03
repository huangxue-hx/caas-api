package com.harmonycloud.dao.user.bean;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsl on 16/10/25.
 */
public class Role implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 3747617463676917317L;
	private Long id;                //编号
    private String name;            //角色名称
    private String description;     //角色描述
    private List<Long> resourceIds; //拥有的资源
    private Integer available;  //是否可用,如果不可用将不会添加给用户

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getResourceIds() {
        if(resourceIds == null) {
            resourceIds = new ArrayList<Long>();
        }
        return resourceIds;
    }

    public void setResourceIds(List<Long> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public String getResourceIdsStr() {
        if(CollectionUtils.isEmpty(resourceIds)) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        for(Long resourceId : resourceIds) {
            s.append(resourceId);
            s.append(",");
        }
        String retVal = s.toString();
        if (retVal.length() > 1){
            retVal = retVal.substring(0, retVal.length()-1);
        }
        return retVal;
    }

    public void setResourceIdsStr(String resourceIdsStr) {
        if(StringUtils.isEmpty(resourceIdsStr)) {
            return;
        }
        String[] resourceIdStrs = resourceIdsStr.split(",");
        for(String resourceIdStr : resourceIdStrs) {
            if(StringUtils.isEmpty(resourceIdStr)) {
                continue;
            }
            getResourceIds().add(Long.valueOf(resourceIdStr));
        }
    }

    public Integer getAvailable() {
        return available;
    }

    public void setAvailable(Integer available) {
        this.available = available;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;

        return !(id != null ? !id.equals(role.id) : role.id != null);
    }

    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", resourceIds=" + resourceIds +
                ", available=" + available +
                '}';
    }
}
