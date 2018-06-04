package com.harmonycloud.dto.application;


import java.io.Serializable;
import java.util.Date;

public class IndexInfo implements Serializable{
    private static final long serialVersionUID = -5820315910071090878L;
    private Date created;
    private String indexName;

    public IndexInfo() {
    }

    public IndexInfo(String indexName, Date created) {
        this.created = created;
        this.indexName = indexName;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
