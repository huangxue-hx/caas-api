package com.harmonycloud.k8s.bean.istio.gitlab;

/**
 * @author xc
 * @date 2018/9/25 14:51
 */
public class TreeEntryMap {

    private String id;
    //文件名称
    private String name;
    //文件类型：tree（目录）、blob（文件）
    private String type;
    //文件模型代号：040000（目录）、100644（文件）
    private String mode;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
