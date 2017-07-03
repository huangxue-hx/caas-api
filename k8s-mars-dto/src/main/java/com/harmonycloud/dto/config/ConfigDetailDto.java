package com.harmonycloud.dto.config;

import java.io.Serializable;

public class ConfigDetailDto implements Serializable {

    private static final long serialVersionUID = -3551823489609889016L;
    private String id;
    private String name;
    private String description;
    private String tenant;
    private String repoName;
    private String items;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
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
    
    public String getTenant() {
        return tenant;
    }
    
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }
  
}
